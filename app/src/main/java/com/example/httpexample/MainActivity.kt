package com.example.httpexample

import android.os.Bundle
import android.widget.Toast
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import java.util.concurrent.TimeUnit

private const val ENDPOINT = "http://10.0.2.2:3000"  // Im using json-server running on my localhost and emulator
private const val BOOKS_URI = "/books"
private const val TITLE = "title"

class MainActivity : AppCompatActivity() {

    private val okHttpClient by lazy {
        OkHttpClient().newBuilder().connectTimeout(10, TimeUnit.SECONDS).build()
    }
    private val typeJson = "application/json".toMediaType()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button.setOnClickListener {
            val book = editText.text
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
                    textView.text = bookList.reduce { acc, s -> "$acc\n$s" }
                }
            }
        } catch (e: Exception) {
            toast(getString(R.string.error_exception))
            e.printStackTrace()
        }

        /*val httpUrlConnection = URL(ENDPOINT + BOOKS_URI).openConnection() as HttpURLConnection
        httpUrlConnection.apply {
            connectTimeout = 10000 // 10 seconds
            requestMethod = "GET"
            doInput = true
        }
        if (httpUrlConnection.responseCode != HttpURLConnection.HTTP_OK) {
            // show error toast
            return
        }
        val streamReader = InputStreamReader(httpUrlConnection.inputStream)
        var text: String = ""
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
            textView.text = books.reduce { acc, s -> "$acc\n$s" }
        }*/
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

        /*val httpUrlConnection = URL(ENDPOINT + BOOKS_URI).openConnection() as HttpURLConnection
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
        getBooksAndShowIt()*/
    }
}

fun AppCompatActivity.toast(text: String) {
    runOnUiThread {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}