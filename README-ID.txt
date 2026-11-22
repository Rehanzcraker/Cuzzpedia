CUZZPEDIA ANDROID + BACKEND (STARTER PROJECT)

ISI
- app/: proyek Android native Kotlin
- backend/: REST API PHP + MySQL
- backend/schema.sql: struktur database dan produk demo

PENTING
Ini adalah starter project fungsional untuk login, daftar, melihat produk,
membuat deposit pending, membuat order dari saldo, dan melihat riwayat.
Integrasi supplier otomatis, upload bukti QRIS, dashboard admin, serta
validasi status transaksi perlu dikonfigurasi/ditambahkan sebelum produksi.
Produk demo bukan produk aktif dari supplier.

A. DATABASE
1. Buat database MySQL baru di hosting.
2. Import backend/schema.sql lewat phpMyAdmin.
3. Edit backend/config.php:
   DB_HOST, DB_NAME, DB_USER, DB_PASS.
4. Ganti APP_SECRET dengan string acak panjang minimal 32 karakter.
5. Upload isi folder backend ke folder publik hosting, misalnya public_html/api/.
6. Pastikan URL API menjadi https://domain-anda.com/api.

B. ANDROID
1. Ekstrak ZIP.
2. Buka folder CUZZPEDIA-ANDROID menggunakan Android Studio.
3. Tunggu Gradle Sync.
4. Buka app/src/main/java/com/cuzzpedia/app/MainActivity.kt.
5. Ubah:
   private var apiBase = "https://YOUR-DOMAIN.com/api"
   menjadi URL API milik Anda, contoh:
   private var apiBase = "https://domainanda.com/api"
6. Build > Build APK(s).
7. APK debug ada di app/build/outputs/apk/debug/app-debug.apk.

CATATAN KEAMANAN & PRODUKSI
- Gunakan HTTPS, jangan gunakan HTTP untuk kredensial/token.
- Jangan menaruh API key supplier di aplikasi Android.
- Simpan API key hanya di backend dan panggil supplier dari server.
- Tambahkan dashboard admin untuk menyetujui deposit, mengelola produk,
  mengubah status order, serta melakukan refund bila order gagal.
- Endpoint order saat ini membuat status pending dan memotong saldo.
  Admin/backend wajib memproses atau mengembalikan saldo jika gagal.
- Deposit hanya membuat permintaan pending; saldo tidak otomatis bertambah.
- Jangan gunakan produk demo untuk transaksi uang sungguhan.
- Tambahkan rate limit, CSRF/session controls untuk panel admin, audit log,
  validasi callback supplier, dan backup database sebelum peluncuran.
