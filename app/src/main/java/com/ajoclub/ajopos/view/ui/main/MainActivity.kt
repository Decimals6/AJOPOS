package com.ajoclub.ajopos.view.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.ajoclub.ajopos.R
import com.ajoclub.ajopos.databinding.ActivityMainBinding
import com.ajoclub.ajopos.model.SessionManager
import com.ajoclub.ajopos.view.ui.Admin.AdminActivity

import com.ajoclub.ajopos.util.ThemePrefs

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var sessionManager: SessionManager

    private val btPermissions = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    )

    // ============================================================
    // RUNTIME PERMISSION HANDLER (Android 12+)
    // ============================================================

    private val requestBtPermissions =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { perms ->

            val scanGranted = perms[Manifest.permission.BLUETOOTH_SCAN] == true
            val connectGranted = perms[Manifest.permission.BLUETOOTH_CONNECT] == true

            if (!scanGranted || !connectGranted) {
                AlertDialog.Builder(this)
                    .setTitle("Izin Diperlukan")
                    .setMessage("Aplikasi membutuhkan izin Bluetooth agar bisa terhubung ke printer.")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }

    private fun ensureBluetoothPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return

        val needRequest = btPermissions.any {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (needRequest) {
            requestBtPermissions.launch(btPermissions)
        }
    }

    // ============================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemePrefs.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        ViewCompat.setOnApplyWindowInsetsListener(binding.rootCoordinator) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//
//            // Toolbar turun
//            binding.toolbar.setPadding(
//                binding.toolbar.paddingLeft,
//                systemBars.top,
//                binding.toolbar.paddingRight,
//                binding.toolbar.paddingBottom
//            )
//
//            // NAV HOST → pake frame container, bukan fragment
//            binding.navHostContainer.setPadding(
//                binding.navHostContainer.paddingLeft,
//                binding.navHostContainer.paddingTop,
//                binding.navHostContainer.paddingRight,
//                systemBars.bottom
//            )
//
//            insets
//        }

        // Cek & request permission di awal
        ensureBluetoothPermissions()

        // setup toolbar
        setSupportActionBar(binding.toolbar)
//        sessionManager = SessionManager(this)

        // setup nav host
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // konfigurasi drawer navigation
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.posFragment,
                R.id.inventoryFragment,
                R.id.reportFragment,
                R.id.qrisSettingsFragment,
                R.id.settingsFragment,
                R.id.aboutFragment
            ),
            binding.drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navigationView.setupWithNavController(navController)

        // listener menu drawer
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.logout_action -> {
                    showLogoutDialog()
                    true
                }

                R.id.admin_menu -> {
                    startActivity(
                        Intent(this, AdminActivity::class.java)
                    )
                    binding.drawerLayout.closeDrawers()
                    true
                }

                else -> {
                    NavigationUI.onNavDestinationSelected(menuItem, navController)
                    binding.drawerLayout.closeDrawers()
                    true
                }
            }
        }
    }

    // ============================================================
    // LOGOUT HANDLER
    // ============================================================

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Yakin ingin keluar?")
            .setPositiveButton("Ya") { _, _ ->
//                sessionManager.logout()
//
//                val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
//                sharedPref.edit().clear().apply()
//
//                val intent = Intent(this, LoginActivity::class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                startActivity(intent)
                finish()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // ============================================================

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}
