package com.ajoclub.ajopos.view.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ajoclub.ajopos.databinding.FragmentPosBinding
import com.ajoclub.ajopos.model.data.Nota
import com.ajoclub.ajopos.view.adapter.main.NotaListAdapter
import com.ajoclub.ajopos.viewmodel.NotaViewModel
import androidx.navigation.fragment.findNavController

class PosFragment : Fragment() {

    private var _binding: FragmentPosBinding? = null
    private val binding get() = _binding!!

    private lateinit var notaViewModel: NotaViewModel
    private lateinit var adapter: NotaListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notaViewModel = ViewModelProvider(this)[NotaViewModel::class.java]

        adapter = NotaListAdapter { nota -> onNotaClicked(nota) }

        binding.rvNotas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PosFragment.adapter
        }



        notaViewModel.notas.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        // initial load
        notaViewModel.loadNotas()
    }

    private fun onNotaClicked(nota: Nota) {
        // navigate to detail fragment — you need to create fragment and nav action
        // Example using Safe Args:
         val action = PosFragmentDirections.actionPosFragmentToNotaDetailFragment(nota.localId)
         findNavController().navigate(action)

    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}