package com.example.httpexample.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.httpexample.R

class BooksViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val book: TextView = itemView.findViewById(R.id.bookText)
}