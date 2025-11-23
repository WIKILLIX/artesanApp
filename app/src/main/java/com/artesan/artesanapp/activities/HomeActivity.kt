package com.artesan.artesanapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.artesan.artesanapp.R
import com.artesan.artesanapp.fragments.CartFragment
import com.artesan.artesanapp.fragments.CreateProductFragment
import com.artesan.artesanapp.fragments.MapFragment
import com.artesan.artesanapp.fragments.ProductListFragment
import com.artesan.artesanapp.fragments.UserManagementFragment
import com.artesan.artesanapp.services.AuthService
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var authService: AuthService
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        authService = AuthService(this)

        // Check if user is logged in
        if (!authService.isLoggedIn()) {
            navigateToWelcome()
            return
        }

        toolbar = findViewById(R.id.toolbar)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        setSupportActionBar(toolbar)
        supportActionBar?.title = "ArtesanApp"

        setupBottomNavigation()

        // Load initial fragment
        if (savedInstanceState == null) {
            loadFragment(ProductListFragment())
        }
    }

    private fun setupBottomNavigation() {
        // Hide admin-only options for non-admin users
        if (!authService.isAdmin()) {
            bottomNavigation.menu.findItem(R.id.nav_create_product).isVisible = false
            bottomNavigation.menu.findItem(R.id.nav_users).isVisible = false
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_products -> {
                    loadFragment(ProductListFragment())
                    true
                }
                R.id.nav_cart -> {
                    loadFragment(CartFragment())
                    true
                }
                R.id.nav_map -> {
                    loadFragment(MapFragment())
                    true
                }
                R.id.nav_users -> {
                    if (authService.isAdmin()) {
                        loadFragment(UserManagementFragment())
                        true
                    } else {
                        false
                    }
                }
                R.id.nav_create_product -> {
                    if (authService.isAdmin()) {
                        loadFragment(CreateProductFragment())
                        true
                    } else {
                        false
                    }
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                authService.logout()
                navigateToWelcome()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navigateToWelcome() {
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
