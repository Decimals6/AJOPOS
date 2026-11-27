package com.ajoclub.ajopos.view.ui.Admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.ajoclub.ajopos.databinding.FragmentAddEditKategoriBinding
import com.ajoclub.ajopos.model.data.Kategori
import com.ajoclub.ajopos.viewmodel.KategoriViewModel

class EditKategoriFragment : Fragment() {

    private lateinit var binding: FragmentAddEditKategoriBinding
    private lateinit var viewModel: KategoriViewModel
    private lateinit var kategori: Kategori

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddEditKategoriBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(KategoriViewModel::class.java)

        binding.tvFormTitle.text = "Edit Kategori"
        binding.btnSimpanKategori.text = "Simpan Perubahan"

        // ambil ID dari argument (dikirim dari adapter via nav)
        val id = EditKategoriFragmentArgs.fromBundle(requireArguments()).id
        viewModel.getKategoriById(id)
        observeViewModel()

        binding.btnSimpanKategori.setOnClickListener {
            val nama = binding.etNamaKategori.text.toString().trim()
            val keterangan = binding.etKeteranganKategori.text.toString().trim()

            if (nama.isEmpty()) {
                Toast.makeText(requireContext(), "Nama kategori tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedKategori = kategori.copy(
                nama = nama,
                keterangan = keterangan,
                updated_at = System.currentTimeMillis() // timestamp baru
            )

            viewModel.updateKategori(updatedKategori)
            Toast.makeText(requireContext(), "Kategori berhasil diperbarui", Toast.LENGTH_SHORT).show()
            Navigation.findNavController(it).popBackStack()
        }

        binding.btnBack.setOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }
    }

    private fun observeViewModel() {
        viewModel.selectedKategoriLD.observe(viewLifecycleOwner, Observer {
            kategori = it ?: return@Observer
            binding.etNamaKategori.setText(it.nama)
            binding.etKeteranganKategori.setText(it.keterangan)
        })
    }
}
