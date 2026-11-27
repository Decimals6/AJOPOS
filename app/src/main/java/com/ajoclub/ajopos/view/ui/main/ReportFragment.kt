package com.ajoclub.ajopos.ui.report

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ajoclub.ajopos.databinding.FragmentReportBinding
import com.google.android.material.datepicker.MaterialDatePicker
import java.io.File
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReportViewModel by viewModels()
    private val adapter = TopProductAdapter()
    private val rupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.rvTopProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTopProducts.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnDateFilter.setOnClickListener {
            showDateRangePicker()
        }

        binding.btnExportCsv.setOnClickListener {
            viewModel.exportCsv()
        }
    }

    private fun showDateRangePicker() {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Pilih Rentang Tanggal")
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val startUtc = selection.first ?: return@addOnPositiveButtonClickListener
            val endUtc = selection.second ?: return@addOnPositiveButtonClickListener

            // Konversi rentang waktu UTC picker ke Local time 00:00:00 s.d 23:59:59
            val cal = Calendar.getInstance()
            cal.timeInMillis = startUtc
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val localStart = cal.timeInMillis

            cal.timeInMillis = endUtc
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            val localEnd = cal.timeInMillis

            viewModel.setDateRange(localStart, localEnd)
        }

        picker.show(childFragmentManager, "DATE_RANGE_PICKER")
    }

    private fun observeViewModel() {
        viewModel.selectedDateRangeText.observe(viewLifecycleOwner) { text ->
            binding.tvDateRange.text = text
        }

        viewModel.summary.observe(viewLifecycleOwner) { summary ->
            binding.tvTotalOmzet.text = rupiah.format(summary.totalOmzet)
            binding.tvTotalTransaksi.text = "${summary.totalTransaksi} Nota"
            binding.tvRataRata.text = rupiah.format(summary.rataRata)
        }

        viewModel.topProducts.observe(viewLifecycleOwner) { list ->
            if (list.isNullOrEmpty()) {
                binding.rvTopProducts.visibility = View.GONE
                binding.tvEmptyTopProducts.visibility = View.VISIBLE
            } else {
                binding.rvTopProducts.visibility = View.VISIBLE
                binding.tvEmptyTopProducts.visibility = View.GONE
                adapter.submitList(list)
            }
        }

        viewModel.csvExportFile.observe(viewLifecycleOwner) { file ->
            if (file != null) {
                shareCsvFile(file)
                viewModel.resetCsvFile()
            }
        }
    }

    private fun shareCsvFile(file: File) {
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "Laporan Penjualan POS")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(intent, "Bagikan Laporan CSV"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ReportFragment()
    }
}