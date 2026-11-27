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
import com.ajoclub.ajopos.databinding.FragmentProdukBinding
import com.ajoclub.ajopos.view.adapter.admin.ProdukAdapter
import com.ajoclub.ajopos.viewmodel.ProdukViewModel

class ProdukFragment : Fragment() {
    private lateinit var binding: FragmentProdukBinding
    private lateinit var viewModel: ProdukViewModel
    private lateinit var produkAdapter: ProdukAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProdukBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewModel
        viewModel = ViewModelProvider(this)[ProdukViewModel::class.java]

        // Adapter & RecyclerView setup
        produkAdapter = ProdukAdapter(
            arrayListOf(),
            onItemClick = { produk ->
                val action = ProdukFragmentDirections.actionToEditProdukFragment(produk.produk.id)
                Navigation.findNavController(view).navigate(action)
            },
            viewModel = viewModel
        )

        binding.rvProduk.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = produkAdapter
        }

        // Floating Action Button → ke tambah produk
        binding.fabAddProduk.setOnClickListener {
            val action = ProdukFragmentDirections.actionToAddProdukFragment()
            Navigation.findNavController(it).navigate(action)
        }

        // Load data produk aktif
        viewModel.getAllProduk()

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.produkLD.observe(viewLifecycleOwner, Observer { list ->
            produkAdapter.updateProdukList(list)
            if (list.isEmpty()) {
                binding.rvProduk.visibility = View.GONE
            } else {
                binding.rvProduk.visibility = View.VISIBLE
            }
        })
    }
}
