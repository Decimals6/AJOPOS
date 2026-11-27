package com.ajoclub.ajopos.view.ui.main

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.ajoclub.ajopos.databinding.FragmentNotaDetailBinding
import com.ajoclub.ajopos.view.adapter.main.NotaDetailAdapter
import com.ajoclub.ajopos.viewmodel.NotaDetailViewModel
import java.text.NumberFormat
import java.util.*

class NotaDetailFragment : Fragment() {

    private lateinit var binding: FragmentNotaDetailBinding
    private lateinit var vm: NotaDetailViewModel
    private lateinit var adapter: NotaDetailAdapter
    private val args by navArgs<NotaDetailFragmentArgs>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotaDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm = ViewModelProvider(this)[NotaDetailViewModel::class.java]
        adapter = NotaDetailAdapter()

        binding.rvDetailItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDetailItems.adapter = adapter

        val localId = args.id   // <===== AMBIL ARGUMEN YANG BENAR
        vm.loadNotaAndItems(localId)

        binding.btnReprint.setOnClickListener {
            // langsung cetak ulang tanpa membuat PrintJob
            vm.reprintDirect(localId)

            // observe status -> tampilkan toast
            vm.reprintStatus.observe(viewLifecycleOwner) { event ->
                val msg = event.getContentIfNotHandled()
                if (msg != null) {
                    if (msg == "SUCCESS") {
                        Toast.makeText(requireContext(), "Cetak ulang berhasil", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Cetak ulang gagal: $msg", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        vm.reprintStatus.observe(viewLifecycleOwner) { event ->
            val msg = event.getContentIfNotHandled()
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }

            // re-enable setelah ada hasil (success/fail)
            binding.btnReprint.isEnabled = true
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        vm.nota.observe(viewLifecycleOwner) { nota ->
            nota?.let {
                binding.tvLocalId.text = it.localId
                binding.tvDate.text = formatDate(it.waktu)
                binding.tvPelanggan.text = it.pelanggan ?: "-"
                binding.tvTotal.text = formatRupiah(it.total)
            }
        }

        vm.items.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }
    }

    private fun formatDate(time: Long): String {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy HH:mm", Locale("in", "ID"))
        return sdf.format(Date(time))
    }

    private fun formatRupiah(value: Double): String {
        val fmt = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        fmt.maximumFractionDigits = 0
        return fmt.format(value)
    }
}