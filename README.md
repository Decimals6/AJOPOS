# AJOPOS — Offline Point of Sale (POS) System

![AJOPOS Version](https://img.shields.io/badge/version-v1.0.0-blue.svg)
![Android SDK](https://img.shields.io/badge/minSdk-24%20%7C%20targetSdk-35-success.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-purple.svg)
![License](https://img.shields.io/badge/License-MIT-green.svg)

**AJOPOS** adalah aplikasi Kasir / Point of Sale (POS) Android modern berbasis **Offline-First** yang dirancang untuk UMKM, ritel, minimarket, kafe, dan restoran. Bekerja 100% offline tanpa ketergantungan koneksi internet menggunakan database lokal **Room SQLite**.

---

## 🚀 Fitur Unggulan (v1.0.0)

- 📷 **Barcode Scanner & Mode Minimarket**:
  - Dukungan kamera internal sebagai pemindai barcode seketika (*Instant Barcode Scanner*).
  - Mode **Continuous Scan** (pemindaian berkelanjutan ala kasir minimarket/Indomaret) untuk menambah barang ke keranjang belanja secara cepat.

- 🖨️ **Integrasi Thermal Bluetooth Printer**:
  - Koneksi dan pencetakan nota transaksi via **Thermal Bluetooth Printer** (ESC/POS).
  - Kustomisasi format nota: nama toko, alamat, nomor telepon, catatan header & footer, serta pengaturan mode printer (Single / Double Printer).

- 📱 **Pembuatan QRIS Dinamis Ber-Nominal dari QRIS Statis**:
  - Konversi otomatis dari kode QRIS statis toko menjadi **QRIS Dinamis ber-nominal presisi** sesuai total transaksi belanja pengguna.
  - Memudahkan pelanggan melakukan pembayaran cashless tanpa perlu menginput nominal transfer secara manual.

- 🌗 **Dual Theme Support (Light & Dark Mode)**:
  - Tampilan antarmuka modern yang secara otomatis mengikuti pengaturan sistem perangkat (DayNight) atau diatur secara manual melalui menu Pengaturan.
  - Skema warna Midnight Dongker Navy (`#070D1D`) yang elegan, bersih, dan kontras tinggi.

- 📊 **Katalog Produk, Kategori & Manajemen Stok (CRUD)**:
  - Manajemen data master produk dan kategori dengan pengelompokan yang terorganisir.
  - Foto produk dari galeri internal, kode barcode, harga, dan keterangan item.

- 📦 **Backup & Restore Database Lokal**:
  - Ekspor dan impor cadangan database lokal (`.db`) melalui Storage Access Framework (SAF) untuk mengamankan data transaksi usaha.

- 📱💻 **Desain Responsif (Smartphone & Tablet)**:
  - Tampilan dioptimalkan untuk ponsel dengan **Floating Checkout Bar & BottomSheet Cart**.
  - Tampilan tablet/layar lebar menggunakan **2-Pane Split Screen** (katalog produk di kiri & keranjang kasir di kanan).

---

## 🛠️ Tech Stack & Library Utama

- **Language**: Kotlin 2.1.0 (JVM 1.8)
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
- **Database**: Android Room SQLite (`2.5.1` / `2.8.2`)
- **UI Framework**: Native Android XML Layouts dengan ViewBinding & DataBinding (Material Components DayNight)
- **Navigation**: Jetpack Navigation Component dengan SafeArgs
- **Barcode & Camera**: `zxing-android-embedded` 4.3.0 & `com.google.zxing:core`
- **Image Loading**: Glide 4.16.0
- **Background Jobs**: WorkManager 2.9.0 (`PrintJobWorker`)

---

## 📦 Kompilasi & Install (Build APK)

1. Clone repository ini:
   ```bash
   git clone https://github.com/Decimals6/AJOPOS.git
   cd AJOPOS
   ```
2. Jalankan kompilasi APK Debug atau Release menggunakan Gradle:
   ```bash
   ./gradlew assembleDebug   # Untuk versi Debug
   ./gradlew assembleRelease # Untuk versi Release
   ```
3. File APK hasil kompilasi dapat ditemukan di:
   - Debug: `app/build/outputs/apk/debug/AJOPOS.apk`
   - Release: `app/build/outputs/apk/release/AJOPOS.apk`

---

## 📄 Hak Cipta & Lisensi

Dikelola dan dikembangkan oleh **AJOCLUB**.
Hak Cipta © 2026 AJOCLUB. All rights reserved.
