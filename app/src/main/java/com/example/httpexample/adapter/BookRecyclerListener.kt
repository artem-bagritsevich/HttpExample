package com.example.httpexample.adapter

interface BookRecyclerListener {
    fun deleteItem(position: Int)
    fun updateItem(position: Int)
}