# AGENT_RULES.md — AJOPOS

## 1. Identitas & Tech Stack
- **Nama App**: AJOPOS (Output APK: `AJOPOS.apk`, Theme: `Theme.AJOPOS`)
- **Package Name**: `com.ajoclub.ajopos`
- **SDK & Compatibility**: `minSdk = 24`, `targetSdk = 35`, `compileSdk = 35`, Java / Kotlin JVM `1.8`
- **Bahasa & Build**: Kotlin `2.1.0`, AGP `8.6.0`, Kapt (`kotlin-kapt`), Navigation SafeArgs
- **UI Framework**: Native Android XML Layouts dengan **ViewBinding** dan **DataBinding** (Material Components)
- **Arsitektur**: MVVM (Model-View-ViewModel) + Repository Pattern + LiveData / Coroutines + WorkManager
- **Library Utama**:
  - **Database**: Room `2.5.1` / `2.8.2`
  - **Navigation**: Jetpack Navigation Component
  - **Printing**: ESC/POS Thermal Printer via Bluetooth (`BluetoothPrinterManager`, `ReceiptFormater`)
  - **Background Work**: WorkManager `2.9.0` (`PrintJobWorker`)
  - **Media & Barcode**: Glide `4.16.0`, ZXing Embedded `4.3.0` (Scanner & QRIS Generator)

---

## 2. Struktur Proyek & Navigasi
### Package Structure (`com.ajoclub.ajopos`)
- `model/`
  - `data/`: Data class / Entity (`User`, `Produk`, `Kategori`, `Nota`, `DetailNota`, `PrintJob`, `NotaWithDetail`, `ProdukWithKategori`, dll.)
  - `dao/`: DAO interfaces (`UserDao`, `ProdukDao`, `KategoriDao`, `NotaDao`, `DetailNotaDao`, `PrintJobDao`)
  - `database/`: `AppDatabase.kt` (Room Database), `DatabaseSeeder.kt`
  - `SessionManager.kt`: SharedPreferences auth/session
- `repository/`: `MenuRepository`, `TransaksiRepository`
- `viewmodel/`: `HomeViewModel`, `NotaViewModel`, `ProdukViewModel`, `ReportViewModel`, `KategoriViewModel`, `TransaksiViewModel`, `NotaDetailViewModel`
- `view/`
  - `ui/main/`: Layar utama (`MainActivity`, `HomeFragment`, `PosFragment`, `InventoryFragment`, `ReportFragment`, `SettingsFragment`, `NotaDetailFragment`, `PrinterPrefFragment`, `DeviceManagerFragment`, `AboutFragment`, `BackupRestoreFragment`, `QrisSettings`, `CreateNotaDialog`, `QrisPaymentDialog`)
  - `ui/Admin/`: Layar manajemen data (`AdminActivity`, `ProdukFragment`, `KategoriFragment`, `AddProdukFragment`, `EditProdukFragment`, `AddKategoriFragment`, `EditKategoriFragment`)
  - `ui/auth/` / `ui/`: `LoginActivity`, `LoginFragment`
  - `adapter/`: RecyclerView Adapters (`CartAdapter`, `PosProdukAdapter`, `ProdukAdapter`, `KategoriAdapter`, `NotaListAdapter`, dll.)
- `bt/`: Modul pencetakan Bluetooth (`BluetoothPrinterManager`, `ReceiptFormater`)
- `worker/`: `PrintJobWorker`
- `util/`: Helper utilities (`DatabaseBackupHelper`, `QrisHelper`, `PrinterPrefs`, `ImageStorageHelper`, `PortraitCaptureActivity`, `ThemePrefs`, `Util`)

### Alur Navigasi
- **Host Activities**:
  - `MainActivity`: Host utama dengan Drawer/Bottom Nav -> `main_nav_graph.xml` (Start: `HomeFragment`)
  - `AdminActivity`: Host manajemen admin -> `admin_nav_graph.xml` (Start: `nav_kategori`)
  - `LoginActivity`: Flow autentikasi
  - `PortraitCaptureActivity`: ZXing Camera Scanner

---

## 3. Database & Penyimpanan Lokal
- **Database Engine**: Room SQLite (`ajopos_database`)
- **Versi Skema**: `DATABASE_VERSION = 10`
- **Tabel / Entity**:
  1. `users` (`id`, `username`, `password`, `role`)
  2. `kategori` (`id`, `nama`)
  3. `produk` (`id`, `nama`, `harga`, `stok`, `id_kategori`, `is_active`, `foto_produk`)
  4. `nota` (`id`, `id_user`, `total_harga`, `bayar`, `kembali`, `tanggal`, `status_pembayaran`, `metode_pembayaran`, `id_pelanggan`, `nama_pelanggan`)
  5. `detail_nota` (`id`, `id_nota`, `id_produk`, `jumlah`, `harga_satuan`, `subtotal`)
  6. `print_jobs` (`id`, `printJobId`, `notaId`, `status`, `payloadJson`, `retryCount`, `createdAt`, `updatedAt`)
- **Mekanisme Backup / Restore**:
  - Class `DatabaseBackupHelper`: Melakukan export/import file `.db` melalui SAF Storage.
  - Menggunakan `PRAGMA wal_checkpoint(FULL)` sebelum export, serta validasi `user_version` SQLite sebelum restore.
- **Penyimpanan Lokal Lain**:
  - `SessionManager`: Mengelola kredensial/role pengguna.
  - `PrinterPrefs`: SharedPreferences preferensi printer & alamat MAC Bluetooth.
  - `ThemePrefs`: SharedPreferences mode tema (`system`, `light`, `dark`).

---

## 4. Konvensi UI/UX Saat Ini
- **File Tema & Base Style**: `res/values/themes.xml` & `res/values-night/themes.xml` (`Theme.AJOPOS` berbasis `Theme.MaterialComponents.DayNight.NoActionBar`).
- **Skema Warna Identity** (`res/values/colors.xml` & `res/values-night/colors.xml` - Midnight Dongker Navy):
  - Header & Primary Dark: `#070D1D` (`header_bg`, `primary` Light Mode)
  - Surface Dark Mode: `#0E1A34` (`surface` Dark Mode)
  - Accent Container: `#182B50` (`highlight` Dark Mode)
  - Text Colors: `#F8FAFC` (`on_surface` Dark Mode), `#070D1D` (`on_surface` Light Mode), `#94A3B8` (`text_secondary` Dark Mode)
- **Layout & Layout Responsif**:
  - Floating Checkout Bar + BottomSheet Cart di ponsel ([fragment_home.xml](file:///D:/JOB/AJOCLUB/POS/android/app/src/main/res/layout/fragment_home.xml)).
  - Layout khusus tablet/layar lebar (2-Pane Split Screen) di [fragment_home.xml (sw600dp)](file:///D:/JOB/AJOCLUB/POS/android/app/src/main/res/layout-sw600dp/fragment_home.xml).

---

## 5. Aturan Wajib Modifikasi
1. **Proteksi Logika Bisnis & Database**: Dilarang mengubah skema Room DB v10 (relasi Nota, DetailNota, Produk, Kategori, User, PrintJob), flow pencetakan bluetooth, atau kalkulasi transaksi kasir tanpa izin eksplisit.
2. **Menjaga Kompatibilitas**: Dilarang merusak versi Gradle, `minSdk` (24), `targetSdk` (35), JVM 1.8, atau dependensi library lama.
3. **Kompatibilitas ViewBinding/DataBinding**: Setiap perombakan UI/UX pada XML wajib mempertahankan ID view dan binding class yang terhubung di Fragment/Activity/Adapter.
4. **Format Respon**: Respon dalam obrolan wajib singkat, padat, dan langsung pada inti solusi.
5. **Aturan Strict Version Control (Git)**: **DILARANG** melakukan commit, push, pull, atau perubahan pada repository Git secara otomatis tanpa konfirmasi dan persetujuan eksplisit dari pengguna terlebih dahulu.
