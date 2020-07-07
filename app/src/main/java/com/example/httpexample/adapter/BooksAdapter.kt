package com.example.httpexample.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.httpexample.R
import com.example.httpexample.model.Book
import com.example.httpexample.swipe.ItemTouchHelperAdapter

class BooksAdapter(private val listener: BookRecyclerListener) :
    RecyclerView.Adapter<BooksViewHolder>(), ItemTouchHelperAdapter {
    private var bookList = listOf<Book>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BooksViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_item, parent, false)

        val viewHolder = BooksViewHolder(view)

        view.setOnClickListener {
            val position = viewHolder.adapterPosition
            if (position == RecyclerView.NO_POSITION) {
                return@setOnClickListener
            }
            listener.updateItem(position)
        }

        return viewHolder
    }

    override fun getItemCount(): Int {
        return bookList.size
    }

    override fun onBindViewHolder(holder: BooksViewHolder, position: Int) {
        val listItem = bookList[holder.adapterPosition]
        holder.book.text = listItem.name
    }

    fun setList(books: List<Book>) {
        bookList = books
        notifyDataSetChanged()
    }

    override fun onItemDismiss(position: Int) {
        listener.deleteItem(position)
    }
}