package com.example.cryptoplatzi.model

class Crypto(var name:String ="", var imageUrl: String ="", var available: Int=0) {
    fun getDocumentId():String{
        return name.toLowerCase()
    }
}