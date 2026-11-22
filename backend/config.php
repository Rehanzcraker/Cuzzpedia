<?php
declare(strict_types=1);
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: Content-Type, Authorization');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') { http_response_code(204); exit; }

const DB_HOST = 'localhost';
const DB_NAME = 'ISI_NAMA_DATABASE';
const DB_USER = 'ISI_USERNAME_DATABASE';
const DB_PASS = 'ISI_PASSWORD_DATABASE';
const APP_SECRET = 'GANTI_DENGAN_SECRET_RANDOM_PANJANG_MINIMAL_32_KARAKTER';

function db(): PDO {
    static $pdo = null;
    if (!$pdo) {
        $pdo = new PDO('mysql:host='.DB_HOST.';dbname='.DB_NAME.';charset=utf8mb4', DB_USER, DB_PASS, [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC
        ]);
    }
    return $pdo;
}
function out(array $data, int $code=200): never {
    http_response_code($code); echo json_encode($data, JSON_UNESCAPED_UNICODE); exit;
}
function input(): array {
    $d = json_decode(file_get_contents('php://input'), true);
    return is_array($d) ? $d : [];
}
function auth_user(): array {
    $h = $_SERVER['HTTP_AUTHORIZATION'] ?? '';
    if (!preg_match('/Bearer\s+(.+)/i', $h, $m)) out(['success'=>false,'message'=>'Belum login'],401);
    $token = $m[1];
    $parts = explode('.', $token);
    if (count($parts)!==2 || !hash_equals(hash_hmac('sha256',$parts[0],APP_SECRET),$parts[1])) out(['success'=>false,'message'=>'Token tidak valid'],401);
    $payload = json_decode(base64_decode($parts[0]), true);
    if (!$payload || ($payload['exp'] ?? 0) < time()) out(['success'=>false,'message'=>'Sesi kedaluwarsa'],401);
    $q=db()->prepare('SELECT id,name,username,email,balance,role FROM users WHERE id=?');
    $q->execute([(int)$payload['uid']]); $u=$q->fetch();
    if (!$u) out(['success'=>false,'message'=>'Akun tidak ditemukan'],401);
    return $u;
}
function make_token(int $uid): string {
    $p=rtrim(strtr(base64_encode(json_encode(['uid'=>$uid,'exp'=>time()+86400*14])), '+/', '-_'),'=');
    return $p.'.'.hash_hmac('sha256',$p,APP_SECRET);
}
