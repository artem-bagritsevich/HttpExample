package com.example.httpexample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.httpexample.adapter.BookRecyclerListener
import com.example.httpexample.adapter.BooksAdapter
import com.example.httpexample.databinding.ActivityMainBinding
import com.example.httpexample.model.Book
import com.example.httpexample.swipe.SwipeController
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

const val REQUEST_CODE_ADD = 1
const val REQUEST_CODE_UPDATE = 2
private const val ENDPOINT = "http://10.0.2.2:3000"  // Im using json-server running on my localhost and emulator
private const val BOOKS_URI = "/books"
private const val TITLE = "title"
const val BOOK = "Book"
const val ID = "id"

class MainActivity : AppCompatActivity(), BookRecyclerListener {

    private lateinit var binding: ActivityMainBinding

    private val adapter = BooksAdapter(this)

    private val books = mutableListOf<Book>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.fab.setOnClickListener {
            startActivityForResult(Intent(this, AddBookActivity::class.java),
                REQUEST_CODE_ADD)
        }

        //attach swipe to recyclerview
        val swipeController = SwipeController(adapter)
        val itemTouchHelper = ItemTouchHelper(swipeController)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        Thread {
            getBooksAndShowIt()
        }.start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || data == null) {
            return
        }
        val name = data.getStringExtra(BOOK) ?: ""
        val id = data. getIntExtra(ID, -1)
        val book = Book(id, name)
        when (requestCode) {
            REQUEST_CODE_ADD -> {
                Thread {
                    addBook(book.name)
                }.start()
            }
            REQUEST_CODE_UPDATE -> {
                Thread {
                    updateBook(book)
                }.start()
            }
        }
    }

    //GET request with HttpURLConnection
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
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(this, getString(R.string.error),
                        Toast.LENGTH_SHORT).show()
                }
                return
            }
            val streamReader = InputStreamReader(httpUrlConnection.inputStream)
            var text = ""
            streamReader.use {
                text = it.readText()
            }

            books.clear()

            val json = JSONArray(text)
            for (i in 0 until json.length()) {
                val jsonBook = json.getJSONObject(i)
                val title = jsonBook.getString(TITLE)
                val id = jsonBook.getInt(ID)
                books.add(Book(id, title))
            }

            Handler(Looper.getMainLooper()).post {
                adapter.setList(books)
            }
        }
        catch (ex: Exception) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(this, getString(R.string.error, ex.message),
                    Toast.LENGTH_SHORT).show()
            }
        }
        finally {
            httpUrlConnection?.disconnect()
        }
    }

    //POST request with HttpURLConnection
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
        }
        catch (ex: Exception) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(this, getString(R.string.error, ex.message),
                    Toast.LENGTH_SHORT).show()
            }
        }
        finally {
            httpUrlConnection?.disconnect()
        }
        getBooksAndShowIt()
    }

    //delete request with okhttp
    @WorkerThread
    fun deleteBook(position: Int) {
        try {
            val book = books[position]
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("$ENDPOINT$BOOKS_URI/${book.id}")
                .delete()
                .build()
            client.newCall(request).execute()
        }
        catch (ex: Exception) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(this, getString(R.string.error, ex.toString()),
                    Toast.LENGTH_SHORT).show()
            }
            ex.printStackTrace()
        }
        getBooksAndShowIt()
    }

    //put request with okhttp
    @WorkerThread
    fun updateBook(book: Book) {
        try {
            val client = OkHttpClient()
            val body = JSONObject().apply {
                put(TITLE, book.name)
                put(ID, book.id)
            }

            val json = "application/json".toMediaType()
            val requestBody = body.toString().toRequestBody(json)
            val request = Request.Builder()
                .url("$ENDPOINT$BOOKS_URI/${book.id}")
                .put(requestBody)
                .build()
            client.newCall(request).execute()
        }
        catch (ex: Exception) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(this, getString(R.string.error, ex.toString()),
                    Toast.LENGTH_SHORT).show()
            }
            ex.printStackTrace()
        }
        getBooksAndShowIt()
    }

    override fun deleteItem(position: Int) {
        Thread {
            deleteBook(position)
        }.start()
    }

    override fun updateItem(position: Int) {
        val intent = Intent(this, AddBookActivity::class.java)
        val book = books[position]
        intent.putExtra(BOOK, book.name)
        intent.putExtra(ID, book.id)
        startActivityForResult(intent, REQUEST_CODE_UPDATE)
    }
}