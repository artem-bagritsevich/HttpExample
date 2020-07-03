package com.example.httpexample.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import com.example.httpexample.R
import com.example.httpexample.util.BOOKS_URI
import com.example.httpexample.util.ENDPOINT
import com.example.httpexample.util.TITLE
import com.example.httpexample.util.toast
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        buttonAddBook.setOnClickListener {
            val book = editTextBookName.text
            Thread {
                addBook(book.toString())
            }.start()
        }
        buttonOkHttp.setOnClickListener {
            startActivity(
                Intent(this, OkHttpActivity::class.java)
            )
        }
        Thread {
            getBooksAndShowIt()
        }.start()
    }

    override fun onRestart() {
        super.onRestart()
        Thread {
            getBooksAndShowIt()
        }.start()
    }

    @WorkerThread
    fun getBooksAndShowIt() {
        var httpUrlConnection: HttpURLConnection? = null

        try {
            httpUrlConnection = URL(ENDPOINT + BOOKS_URI).openConnection() as HttpURLConnection
            httpUrlConnection.apply {
                connectTimeout = 10000 // 10 seconds
                requestMethod = "GET"
                doInput = true
            }
            if (httpUrlConnection.responseCode != HttpURLConnection.HTTP_OK) {
                toast(
                    getString(R.string.error_unsuccessful_request, httpUrlConnection.responseCode)
                )
                return
            }
            val streamReader = InputStreamReader(httpUrlConnection.inputStream)
            var text = ""
            streamReader.use {
                text = it.readText()
            }

            val books = mutableListOf<String>()
            val json = JSONArray(text)
            for (i in 0 until json.length()) {
                val jsonBook = json.getJSONObject(i)
                val title = jsonBook.getString(TITLE)
                books.add(title)
            }
            httpUrlConnection.disconnect()

            Handler(Looper.getMainLooper()).post {
                textViewBookList.text = books.reduce { acc, s -> "$acc\n$s" }
            }
        } catch (e: Exception) {
            toast(getString(R.string.error_exception))
            e.printStackTrace()
        } finally {
            httpUrlConnection?.disconnect()
        }
    }

    @WorkerThread
    fun addBook(book: String) {
        var httpUrlConnection: HttpURLConnection? = null

        try {
            httpUrlConnection = URL(ENDPOINT + BOOKS_URI).openConnection() as HttpURLConnection
            val body = JSONObject().apply {
                put(TITLE, book)
            }
            httpUrlConnection.apply {
                connectTimeout = 10000 // 10 seconds
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
            OutputStreamWriter(httpUrlConnection.outputStream).use {
                it.write(body.toString())
            }
            httpUrlConnection.responseCode
            httpUrlConnection.disconnect()
            getBooksAndShowIt()
        } catch (e: Exception) {
            toast(getString(R.string.error_exception))
            e.printStackTrace()
        } finally {
            httpUrlConnection?.disconnect()
        }
    }
}