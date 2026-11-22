<?php
require __DIR__.'/config.php';
$path = trim(parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH), '/');
$path = preg_replace('#^(?:.*/)?api/?#','',$path);
$method = $_SERVER['REQUEST_METHOD'];
$d = input();

try {
 if ($method==='POST' && $path==='register') {
    $name=trim($d['name']??''); $username=trim($d['username']??''); $email=trim($d['email']??''); $pass=$d['password']??'';
    if (!$name || !preg_match('/^[a-zA-Z0-9_]{3,24}$/',$username) || !filter_var($email,FILTER_VALIDATE_EMAIL) || strlen($pass)<8) out(['success'=>false,'message'=>'Data tidak valid. Username 3-24 karakter, password minimal 8 karakter.'],422);
    $q=db()->prepare('INSERT INTO users(name,username,email,password_hash) VALUES(?,?,?,?)');
    try { $q->execute([$name,$username,$email,password_hash($pass,PASSWORD_DEFAULT)]); }
    catch(PDOException $e) { out(['success'=>false,'message'=>'Username atau email sudah digunakan'],409); }
    out(['success'=>true]);
 }
 if ($method==='POST' && $path==='login') {
    $id=trim($d['identity']??''); $q=db()->prepare('SELECT * FROM users WHERE email=? OR username=? LIMIT 1'); $q->execute([$id,$id]); $u=$q->fetch();
    if (!$u || !password_verify($d['password']??'', $u['password_hash'])) out(['success'=>false,'message'=>'Username/email atau password salah'],401);
    out(['success'=>true,'token'=>make_token((int)$u['id'])]);
 }
 $u=auth_user();
 if ($method==='GET' && $path==='me') out(['success'=>true,'user'=>$u]);
 if ($method==='GET' && $path==='products') {
    $type=($_GET['type']??'')==='smm'?'smm':'ppob';
    $q=db()->prepare('SELECT id,name,price,type,description FROM products WHERE type=? AND active=1 ORDER BY id DESC'); $q->execute([$type]);
    out(['success'=>true,'products'=>$q->fetchAll()]);
 }
 if ($method==='POST' && $path==='deposit') {
    $amount=(int)($d['amount']??0);
    if ($amount<5000 || $amount>100000000) out(['success'=>false,'message'=>'Nominal deposit tidak valid'],422);
    $q=db()->prepare('INSERT INTO deposits(user_id,amount,note,status) VALUES(?,?,?,?)');
    $q->execute([$u['id'],$amount,substr(trim($d['note']??''),0,180),'pending']);
    out(['success'=>true,'deposit_id'=>(int)db()->lastInsertId()]);
 }
 if ($method==='POST' && $path==='orders') {
    $pid=(int)($d['product_id']??0); $qty=max(1,min(1000000,(int)($d['quantity']??1))); $target=trim($d['target']??'');
    if (!$target || strlen($target)>500) out(['success'=>false,'message'=>'Tujuan pesanan wajib diisi'],422);
    $pdo=db(); $pdo->beginTransaction();
    $q=$pdo->prepare('SELECT * FROM products WHERE id=? AND active=1 FOR UPDATE'); $q->execute([$pid]); $p=$q->fetch();
    if (!$p) { $pdo->rollBack(); out(['success'=>false,'message'=>'Produk tidak tersedia'],404); }
    $total=(int)$p['price']*$qty;
    $q=$pdo->prepare('SELECT balance FROM users WHERE id=? FOR UPDATE'); $q->execute([$u['id']]); $bal=(int)$q->fetchColumn();
    if ($bal<$total) { $pdo->rollBack(); out(['success'=>false,'message'=>'Saldo tidak mencukupi'],422); }
    $pdo->prepare('UPDATE users SET balance=balance-? WHERE id=?')->execute([$total,$u['id']]);
    $pdo->prepare('INSERT INTO orders(user_id,product_id,target,quantity,total,status) VALUES(?,?,?,?,?,?)')->execute([$u['id'],$pid,$target,$qty,$total,'pending']);
    $oid=(int)$pdo->lastInsertId(); $pdo->commit();
    out(['success'=>true,'order_id'=>$oid]);
 }
 if ($method==='GET' && $path==='orders') {
    $q=db()->prepare('SELECT o.id,p.name product_name,o.total,o.status,o.created_at FROM orders o JOIN products p ON p.id=o.product_id WHERE o.user_id=? ORDER BY o.id DESC LIMIT 100');
    $q->execute([$u['id']]); out(['success'=>true,'orders'=>$q->fetchAll()]);
 }
 out(['success'=>false,'message'=>'Endpoint tidak ditemukan'],404);
} catch(Throwable $e) {
    error_log((string)$e); out(['success'=>false,'message'=>'Terjadi kesalahan server'],500);
}
