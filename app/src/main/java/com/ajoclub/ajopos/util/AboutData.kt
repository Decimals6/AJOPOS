package com.ajoclub.ajopos.util

data class TutorialGuide(
    val id: Int,
    val title: String,
    val steps: List<String>,
    var isExpanded: Boolean = false
)

object AboutDataProvider {

    fun getTutorials(): List<TutorialGuide> {
        return listOf(
            TutorialGuide(
                id = 1,
                title = "1. Mengelola Master Data",
                steps = listOf(
                    "Buka drawer menu melalui tombol garis tiga di pojok kiri atas.",
                    "Pilih menu 'Master Data'.",
                    "Tambahkan Kategori terlebih dahulu pada tab kategori di navigasi bawah.",
                    "Masuk ke tab Produk/Item, lalu tambahkan produk baru. Pastikan nama, harga, dan kategori sudah sesuai.",
                    "Perubahan harga produk tidak akan mengubah nominal nota lama yang sudah diterbitkan.",
                    "Data yang dihapus menggunakan sistem soft-delete (aman dan riwayat transaksi tetap terjaga).",
                    "Gunakan menu 'POS' pada bar bawah untuk kembali ke halaman kasir."
                )
            ),
            TutorialGuide(
                id = 2,
                title = "2. Setup & Koneksi Printer",
                steps = listOf(
                    "Buka drawer menu, lalu pilih 'Settings'.",
                    "Pilih Mode Printer: 'None' (tanpa cetak), 'Single' (hanya struk kasir/pelanggan), atau 'Double' (cetak struk kasir sekaligus tiket order dapur).",
                    "Buka 'Device Manager' pada halaman Settings.",
                    "Aktifkan Bluetooth HP dan pastikan izin perangkat sekitar (Nearby Devices) telah diizinkan.",
                    "Pasangkan (Pairing) printer thermal lewat pengaturan Bluetooth HP (PIN default biasanya 0000 atau 1234).",
                    "Kembali ke aplikasi, klik 'Ubah' pada slot printer yang diinginkan, lalu pilih nama dan MAC address printer yang sesuai.",
                    "Tekan 'Test Print' untuk memeriksa respons printer.",
                    "Jika berhasil, nota akan otomatis tercetak setiap kali transaksi baru selesai dibuat."
                )
            ),
            TutorialGuide(
                id = 3,
                title = "3. Mengatur Format Header Nota",
                steps = listOf(
                    "Buka drawer menu, lalu pilih 'Settings'.",
                    "Pilih 'Pengaturan Format Nota'.",
                    "Lengkapi informasi toko: Nama Toko, Alamat, dan Nomor Telepon.",
                    "Tentukan ukuran kertas printer yang digunakan (58 mm atau 80 mm).",
                    "Tekan tombol 'Simpan Pengaturan' agar konfigurasi tersimpan.",
                    "Gunakan tombol 'Preview Nota' untuk melihat simulasi hasil cetak sebelum digunakan."
                )
            ),
            TutorialGuide(
                id = 4,
                title = "4. Melakukan Transaksi / Kasir",
                steps = listOf(
                    "Pastikan menu produk dan printer sudah diatur sebelumnya.",
                    "Buka drawer menu, lalu pilih 'Dashboard'.",
                    "Ketuk produk pada daftar katalog untuk memasukkannya ke keranjang.",
                    "Untuk menambah kuantitas, ketuk kembali produk tersebut atau gunakan tombol '+' di panel keranjang.",
                    "Gunakan ikon tempat sampah untuk membatalkan item dari keranjang.",
                    "Tekan 'Lanjut Pembayaran', lalu pilih tipe pesanan (Dine In atau Takeaway).",
                    "Ketik nama pelanggan (isi strip '-' jika umum), pilih metode pembayaran, lalu tekan 'Buat Nota'.",
                    "Pesanan otomatis tersimpan di database lokal dan struk akan langsung tercetak."
                )
            ),
            TutorialGuide(
                id = 5,
                title = "5. Mencetak Ulang Nota Lama",
                steps = listOf(
                    "Buka drawer menu, lalu masuk ke halaman 'Transaksi / Riwayat'.",
                    "Cari dan pilih nota transaksi yang ingin dicetak kembali.",
                    "Tekan tombol 'Cetak Ulang' untuk mencetak kembali nota ke printer kasir."
                )
            )
        )
    }
}