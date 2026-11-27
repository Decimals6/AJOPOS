package com.ajoclub.ajopos.view.ui.Admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.ajoclub.ajopos.databinding.FragmentAddEditKategoriBinding
import com.ajoclub.ajopos.model.data.Kategori
import com.ajoclub.ajopos.viewmodel.KategoriViewModel

class AddKategoriFragment : Fragment() {

    private lateinit var binding: FragmentAddEditKategoriBinding
    private lateinit var viewModel: KategoriViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddEditKategoriBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[KategoriViewModel::class.java]

        binding.apply {
            btnSimpanKategori.setOnClickListener {
                val nama = etNamaKategori.text.toString().trim()
                val keterangan = etKeteranganKategori.text.toString().trim()

                if (nama.isEmpty()) {
                    Toast.makeText(requireContext(), "Nama kategori tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val currentTime = System.currentTimeMillis()

                val kategori = Kategori(
                    id = 0, // kalau Room kamu auto-generate false, nanti bisa kamu ubah manual atau generate sendiri
                    nama = nama,
                    keterangan = keterangan,
                    created_at = currentTime,
                    updated_at = currentTime,
                    deleted = 0
                )

                viewModel.addKategori(kategori)
                Toast.makeText(requireContext(), "Kategori berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                Navigation.findNavController(it).popBackStack()
            }


            btnBack.setOnClickListener {
                Navigation.findNavController(it).popBackStack()
            }
        }
    }
}
