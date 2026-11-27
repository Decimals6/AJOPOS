package com.ajoclub.ajopos.view.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.ajoclub.ajopos.databinding.ActivityLoginBinding
import com.ajoclub.ajopos.model.SessionManager
import com.ajoclub.ajopos.view.ui.Admin.AdminActivity
import com.ajoclub.ajopos.view.ui.main.MainActivity// ganti sesuai nama activity POS kamu

import com.ajoclub.ajopos.util.ThemePrefs

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    // hardcoded credentials (development-only)
    // format: username -> Pair(password, role)
    // role: "admin" or "pos"
    private val DUMMY_USERS = mapOf(
        "admin" to Pair("admin123", "admin"),
        "owner" to Pair("owner123", "admin"),
        "kasir" to Pair("pos123", "pos"),
        "cashier" to Pair("cash123", "pos")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemePrefs.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // If already logged in, redirect
        if (sessionManager.isLoggedIn()) {
            val role = sessionManager.getRole()
            navigateToRoleScreen(role)
            finish()
            return
        }

        binding.btnLogin.setOnClickListener {
            val username = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dummy = DUMMY_USERS[username]
            if (dummy == null) {
                Toast.makeText(this, "Email tidak ditemukan (dev-only)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val (expectedPassword, role) = dummy
            if (password != expectedPassword) {
                Toast.makeText(this, "Password salah (dev-only)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // sukses — simpan session (userId dummy pakai hash dari username supaya unik)
            val userId = username.hashCode()
            sessionManager.saveLogin(userId, username, role)

            // redirect sesuai role
            navigateToRoleScreen(role)

            // clean exit from login
            finish()
        }
    }

    private fun navigateToRoleScreen(role: String?) {
        when (role) {
            "admin" -> startActivity(Intent(this, AdminActivity::class.java))
            "pos" -> startActivity(Intent(this, MainActivity::class.java))
            else -> Toast.makeText(this, "Role tidak dikenali", Toast.LENGTH_SHORT).show()
        }
    }
}
