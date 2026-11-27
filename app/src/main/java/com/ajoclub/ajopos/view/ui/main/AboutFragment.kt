package com.ajoclub.ajopos.view.ui.main

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ajoclub.ajopos.R
import com.ajoclub.ajopos.util.AboutDataProvider
import com.ajoclub.ajopos.view.adapter.main.TutorialAdapter

class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvAppVersion: TextView = view.findViewById(R.id.tvAppVersion)
        val btnWebsite: LinearLayout = view.findViewById(R.id.btnWebsite)
        val btnInstagram: LinearLayout = view.findViewById(R.id.btnInstagram)
        val btnWhatsapp: LinearLayout = view.findViewById(R.id.btnWhatsapp)
        val rvTutorials: RecyclerView = view.findViewById(R.id.rvTutorials)

        // 1. Set App Version Dinamis dari Gradle
        tvAppVersion.text = getAppVersionString()

        // 2. Setup Recycler View Tutorial
        rvTutorials.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = TutorialAdapter(AboutDataProvider.getTutorials())
        }

        // 3. Hyperlink Action: Website
        btnWebsite.setOnClickListener {
            openUrl("https://www.ajocorp.com")
        }

        // 4. Hyperlink Action: Instagram
        btnInstagram.setOnClickListener {
            val igIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/_u/ajoclub")).apply {
                setPackage("com.instagram.android")
            }
            try {
                startActivity(igIntent)
            } catch (e: Exception) {
                // Fallback jika tidak ada aplikasi Instagram
                openUrl("https://www.instagram.com/ajoclub")
            }
        }

        // 5. Hyperlink Action: WhatsApp
        btnWhatsapp.setOnClickListener {
            val phone = "6285188627365"
            val message = "Halo CS AJOCLUB, saya membutuhkan bantuan seputar aplikasi AJOPOS."
            val waUrl = "https://api.whatsapp.com/send?phone=$phone&text=${Uri.encode(message)}"
            openUrl(waUrl)
        }
    }

    private fun getAppVersionString(): String {
        return try {
            val pInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireContext().packageManager.getPackageInfo(
                    requireContext().packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            }
            "v${pInfo.versionName} (Build ${pInfo.versionCode})"
        } catch (e: Exception) {
            "v1.0.0"
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}