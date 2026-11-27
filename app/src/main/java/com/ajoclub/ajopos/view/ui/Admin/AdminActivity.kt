package com.ajoclub.ajopos.view.ui.Admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.ajoclub.ajopos.R
import com.ajoclub.ajopos.databinding.ActivityAdminBinding
import com.ajoclub.ajopos.model.SessionManager
import com.ajoclub.ajopos.view.ui.main.MainActivity

import com.ajoclub.ajopos.util.ThemePrefs

class AdminActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAdminBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemePrefs.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        sessionManager = SessionManager(this)


        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        val navController = navHostFragment?.navController

        if (navController != null) {
            // setup navigation otomatis
            binding.bottomNavBar.setupWithNavController(navController)

            // listener khusus untuk logout
            binding.bottomNavBar.setOnItemSelectedListener { item ->
                if (item.itemId ==  R.id.nav_pos) {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                } else {
                    NavigationUI.onNavDestinationSelected(item, navController)
                }
            }


            // hide/show logic optional
            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.addProdukFragment, R.id.editProdukFragment -> {
                        binding.bottomNavBar.visibility = View.GONE
                    }
                    else -> {
                        binding.bottomNavBar.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}
