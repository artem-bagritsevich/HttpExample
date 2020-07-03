package com.example.httpexample.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import com.example.httpexample.R
import com.example.httpexample.util.BOOKS_URI
import com.example.httpexample.util.ENDPOINT
import com.example.httpexample.util.TITLE
import com.example.httpexample.util.toast
import kotlinx.android.synthetic.main.activity_okhttp.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import java.util.concurrent.TimeUnit

class OkHttpActivity : AppCompatActivity() {

    private val okHttpClient by lazy {
        OkHttpClient().newBuilder().connectTimeout(10, TimeUnit.SECONDS).build()
    }
    private val typeJson = "application/json".toMediaType()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_okhttp)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        buttonAddBook2.setOnClickListener {
            val book = editTextBookName2.text
            Thread {
                addBook(book.toString())
            }.start()
        }
        Thread {
            getBooksAndShowIt()
        }.start()
    }

    @WorkerThread
    fun getBooksAndShowIt() {
        val request: Request = Request.Builder()
            .url(ENDPOINT + BOOKS_URI)
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    toast(getString(R.string.error_unsuccessful_request, response.code))
                    return
                }

                val resultString = response.body?.string() ?: ""
                val bookList = mutableListOf<String>()
                val jsonBookArray = JSONArray(resultString)

                for (i in 0 until jsonBookArray.length()) {
                    val book = jsonBookArray.getJSONObject(i)
                    bookList.add(book.getString(TITLE))
                }

                runOnUiThread {
                    textViewBookList2.text = bookList.reduce { acc, s -> "$acc\n$s" }
                }
            }
        } catch (e: Exception) {
            toast(getString(R.string.error_exception))
            e.printStackTrace()
        }
    }

    @WorkerThread
    fun addBook(book: String) {
        val requestBody = "{\"$TITLE\": \"$book\"}".toRequestBody(contentType = typeJson)
        val request = Request.Builder()
            .url(ENDPOINT + BOOKS_URI)
            .post(requestBody)
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    toast(getString(R.string.error_unsuccessful_request, response.code))
                } else {
                    toast(getString(R.string.successful_request))
                    getBooksAndShowIt()
                }
            }
        } catch (e: Exception) {
            toast(getString(R.string.error_exception))
            e.printStackTrace()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }
}