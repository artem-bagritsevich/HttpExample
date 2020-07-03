package com.example.httpexample.util

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.toast(text: String) {
    runOnUiThread {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}