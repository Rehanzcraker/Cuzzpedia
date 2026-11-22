CREATE TABLE users (
 id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR(100) NOT NULL,
 username VARCHAR(24) NOT NULL UNIQUE,
 email VARCHAR(190) NOT NULL UNIQUE,
 password_hash VARCHAR(255) NOT NULL,
 balance BIGINT NOT NULL DEFAULT 0,
 role ENUM('member','admin') NOT NULL DEFAULT 'member',
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE products (
 id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR(180) NOT NULL,
 type ENUM('smm','ppob') NOT NULL,
 price BIGINT UNSIGNED NOT NULL,
 description TEXT NULL,
 supplier_code VARCHAR(120) NULL,
 active TINYINT(1) NOT NULL DEFAULT 1,
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE deposits (
 id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
 user_id BIGINT UNSIGNED NOT NULL,
 amount BIGINT UNSIGNED NOT NULL,
 note VARCHAR(180) NULL,
 proof_path VARCHAR(255) NULL,
 status ENUM('pending','approved','rejected') NOT NULL DEFAULT 'pending',
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE orders (
 id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
 user_id BIGINT UNSIGNED NOT NULL,
 product_id BIGINT UNSIGNED NOT NULL,
 target VARCHAR(500) NOT NULL,
 quantity BIGINT UNSIGNED NOT NULL DEFAULT 1,
 total BIGINT UNSIGNED NOT NULL,
 status ENUM('pending','processing','success','failed') NOT NULL DEFAULT 'pending',
 supplier_order_id VARCHAR(120) NULL,
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(user_id) REFERENCES users(id),
 FOREIGN KEY(product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO products(name,type,price,description,supplier_code) VALUES
('Instagram Followers - 100','smm',5000,'Contoh produk, ganti harga dan supplier code',''),
('TikTok Views - 1000','smm',3000,'Contoh produk, ganti harga dan supplier code',''),
('Pulsa Telkomsel 5.000','ppob',6500,'Contoh produk PPOB',''),
('Paket Data 1 GB','ppob',12000,'Contoh produk PPOB',''),
('Token PLN 20.000','ppob',22500,'Contoh produk PPOB','');
