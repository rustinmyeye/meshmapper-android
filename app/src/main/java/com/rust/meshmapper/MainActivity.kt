package com.rust.meshmapper

import android.content.Context
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.net.toUri
import com.google.android.material.button.MaterialButton
import com.google.androidbrowserhelper.trusted.TwaLauncher

class MainActivity : AppCompatActivity() {

    private lateinit var layoutMain: LinearLayout
    private lateinit var layoutExplore: LinearLayout
    private lateinit var twaLauncher: TwaLauncher
    private lateinit var btnPinned: MaterialButton
    
    private val prefs by lazy { getSharedPreferences("MeshMapperPrefs", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.statusBarColor = ContextCompat.getColor(this, R.color.github_dark)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.github_dark)

        layoutMain = findViewById(R.id.layout_main)
        layoutExplore = findViewById(R.id.layout_explore)
        btnPinned = findViewById(R.id.btn_pinned)
        twaLauncher = TwaLauncher(this)

        updatePinnedButton()

        findViewById<Button>(R.id.btn_wardrive).setOnClickListener {
            twaLauncher.launch("https://wardrive.meshmapper.net".toUri())
        }

        findViewById<Button>(R.id.btn_explore).setOnClickListener {
            showExploreMenu()
        }

        findViewById<Button>(R.id.btn_home).setOnClickListener {
            twaLauncher.launch("https://meshmapper.net/".toUri())
        }

        findViewById<Button>(R.id.btn_back).setOnClickListener {
            layoutExplore.isVisible = false
            layoutMain.isVisible = true
        }

        setupCityGrid()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (layoutExplore.isVisible) {
                    layoutExplore.isVisible = false
                    layoutMain.isVisible = true
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun updatePinnedButton() {
        val code = prefs.getString("pinned_code", null)
        val name = prefs.getString("pinned_name", null)
        
        if (code != null && name != null) {
            btnPinned.isVisible = true
            btnPinned.text = "Explore $name ($code)"
            btnPinned.setOnClickListener {
                twaLauncher.launch("https://${code.lowercase()}.meshmapper.net".toUri())
            }
            btnPinned.setOnLongClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Remove Pin")
                    .setMessage("Do you want to remove $name from your start screen?")
                    .setPositiveButton("Remove") { _, _ ->
                        prefs.edit().clear().apply()
                        updatePinnedButton()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
                true
            }
        } else {
            btnPinned.isVisible = false
        }
    }

    private fun showExploreMenu() {
        layoutMain.isVisible = false
        layoutExplore.isVisible = true
    }

    private fun setupCityGrid() {
        val grid = findViewById<GridLayout>(R.id.grid_cities)
        val cities = listOf(
            "YOW" to "Ottawa",
            "YYZ" to "Toronto",
            "YYC" to "Calgary",
            "YVR" to "Vancouver",
            "YYJ" to "Victoria",
            "YKF" to "Waterloo",
            "YCD" to "Nanaimo",
            "YQQ" to "Courtenay",
            "YSE" to "Squamish",
            "YQA" to "Muskoka",
            "MSN" to "Madison",
            "BNE" to "Brisbane",
            "KTW" to "Katowice",
            "BLX" to "Meano"
        )

        for ((code, name) in cities) {
            val button = MaterialButton(this, null, com.google.android.material.R.attr.materialButtonStyle).apply {
                text = "$code\n$name"
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(12, 12, 12, 12)
                }
                isAllCaps = false
                textSize = 14f
                cornerRadius = 16
                
                backgroundTintList = ColorStateList.valueOf(0x338B949E.toInt()) // subtle gray
                setTextColor(ContextCompat.getColor(context, R.color.white))
                rippleColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.purple_200))
                
                setOnClickListener {
                    twaLauncher.launch("https://${code.lowercase()}.meshmapper.net".toUri())
                }
                
                setOnLongClickListener {
                    prefs.edit()
                        .putString("pinned_code", code)
                        .putString("pinned_name", name)
                        .apply()
                    updatePinnedButton()
                    Toast.makeText(context, "$name pinned to start screen", Toast.LENGTH_SHORT).show()
                    true
                }
            }
            grid.addView(button)
        }
    }
}