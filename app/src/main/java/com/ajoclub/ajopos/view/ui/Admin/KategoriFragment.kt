package com.ajoclub.ajopos.view.ui.Admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.ajoclub.ajopos.databinding.FragmentKategoriBinding
import com.ajoclub.ajopos.view.adapter.admin.KategoriAdapter
import com.ajoclub.ajopos.viewmodel.KategoriViewModel

class KategoriFragment : Fragment() {
    private lateinit var binding: FragmentKategoriBinding
    private lateinit var viewModel: KategoriViewModel
    private lateinit var kategoriAdapter: KategoriAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentKategoriBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewModel
        viewModel = ViewModelProvider(this)[KategoriViewModel::class.java]

        // Adapter & Recycler setup
        kategoriAdapter = KategoriAdapter(
            arrayListOf(),
            onItemClick = { kategori ->
                val action = KategoriFragmentDirections.actionToEditKategoriFragment(kategori.id)
                Navigation.findNavController(view).navigate(action)
            },
            viewModel = viewModel
        )


        binding.rvKategori.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = kategoriAdapter
        }

        // Floating Action Button → ke tambah kategori
        binding.fabAddKategori.setOnClickListener {
            val action = KategoriFragmentDirections.actionToAddKategoriFragment()
            Navigation.findNavController(it).navigate(action)
        }

        // Load data kategori aktif
        viewModel.getAllKategori()

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.kategoriLD.observe(viewLifecycleOwner, Observer { list ->
            kategoriAdapter.updateKategoriList(list)
            if (list.isEmpty()) {
                binding.rvKategori.visibility = View.GONE
            } else {
                binding.rvKategori.visibility = View.VISIBLE
            }
        })
    }
}
