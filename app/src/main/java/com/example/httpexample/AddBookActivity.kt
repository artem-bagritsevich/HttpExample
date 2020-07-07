package com.example.httpexample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.httpexample.databinding.ActivityAddBookBinding

class AddBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBookBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intentBook = intent.getStringExtra(BOOK)
        val intentId = intent.getIntExtra(ID, -1)
        if (intentBook != null) {
            binding.addButton.text = getString(R.string.update)
            binding.bookEditText.setText(intentBook)
        }

        binding.addButton.setOnClickListener {
            val book = binding.bookEditText.text.toString()
            if (book.isEmpty()) {
                Toast.makeText(this, "Book field is empty!", Toast.LENGTH_LONG)
                    .show()
            } else {
                val intent = Intent()
                intent.putExtra(BOOK, book)
                intent.putExtra(ID, intentId)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }
}