# AJOPOS — Offline Point of Sale System

AJOPOS adalah aplikasi kasir (Point of Sale) Android offline-first yang dirancang untuk operasional transaksi usaha ritel, minimarket, kafe, dan UMKM. Aplikasi ini bekerja secara mandiri tanpa ketergantungan koneksi internet dengan memanfaatkan basis data lokal Room SQLite.

---

## Fitur Utama

- **Pemindaian Barcode & Mode Kasir Continuous**:
  - Pemindaian kode barcode barang secara langsung menggunakan kamera perangkat.
  - Mode pemindaian berkelanjutan (Continuous Scan) untuk mempercepat entri transaksi dalam jumlah banyak.

- **Integrasi Pencetakan Bluetooth Thermal**:
  - Koneksi langsung ke printer thermal Bluetooth (ESC/POS).
  - Kustomisasi cetak nota mencakup nama toko, alamat, nomor kontak, pesan header/footer, serta mode printer tunggal atau ganda (kasir & dapur).

- **Generasi QRIS Dinamis dari QRIS Statis**:
  - Konversi string QRIS statis toko menjadi kode QRIS dinamis ber-nominal otomatis sesuai total transaksi belanja.

- **Dukungan Tema Terang & Gelap (Light & Dark Mode)**:
  - Antarmuka responsif yang dapat menyesuaikan preferensi sistem perangkat (DayNight) atau diatur secara manual melalui menu Pengaturan.

- **Manajemen Katalog & Master Data**:
  - Pengelolaan data produk, kategori, harga, gambar, barcode, dan persediaan barang (CRUD).

- **Pencadangan & Pemulihan Basis Data (Backup & Restore)**:
  - Fasilitas ekspor dan impor file database lokal (`.db`) menggunakan Storage Access Framework (SAF).

- **Antarmuka Responsif Ponsel & Tablet**:
  - Optimasi antarmuka ponsel menggunakan Floating Checkout Bar & BottomSheet Cart.
  - Tampilan layar lebar/tablet menggunakan skema 2-Pane Split Screen.

---

## Spesifikasi Teknis & Dependensi

- **Bahasa Pemrograman**: Kotlin 2.1.0 (Target JVM 1.8)
- **Arsitektur**: MVVM (Model-View-ViewModel) dengan Repository Pattern
- **Penyimpanan Data**: Android Room SQLite (v2.5.1 / v2.8.2)
- **Komponen UI**: Native XML Layouts dengan ViewBinding & DataBinding (Material Components DayNight)
- **Navigasi**: Jetpack Navigation Component dengan SafeArgs
- **Pemrosesan Barcode**: ZXing Android Embedded 4.3.0 & ZXing Core 3.5.1
- **Pemrosesan Gambar**: Glide 4.16.0
- **Penjadwalan Latar Belakang**: Android WorkManager 2.9.0

---

## Panduan Kompilasi & Pengujian

1. Kloning repository:
   ```bash
   git clone https://github.com/Decimals6/AJOPOS.git
   cd AJOPOS
   ```

2. Kompilasi APK menggunakan Gradle Wrapper:
   ```bash
   ./gradlew assembleDebug   # Untuk build versi Debug
   ./gradlew assembleRelease # Untuk build versi Release
   ```

3. Lokasi output kompilasi APK:
   - Debug: `app/build/outputs/apk/debug/AJOPOS.apk`
   - Release: `app/build/outputs/apk/release/AJOPOS.apk`

---

## Hak Cipta

Dikelola dan dikembangkan oleh AJOCLUB.  
Hak Cipta © 2026 AJOCLUB. All rights reserved.
