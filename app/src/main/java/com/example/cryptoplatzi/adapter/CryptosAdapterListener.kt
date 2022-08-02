package com.example.cryptoplatzi.adapter

import com.example.cryptoplatzi.model.Crypto

interface CryptosAdapterListener {

    fun onBuyCryptoClicked(crypto:Crypto)

}