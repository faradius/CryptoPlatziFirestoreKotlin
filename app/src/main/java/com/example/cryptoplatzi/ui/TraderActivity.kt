package com.example.cryptoplatzi.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cryptoplatzi.R
import com.example.cryptoplatzi.adapter.CryptosAdapter
import com.example.cryptoplatzi.adapter.CryptosAdapterListener
import com.example.cryptoplatzi.model.Constants
import com.example.cryptoplatzi.model.Crypto
import com.example.cryptoplatzi.model.User
import com.example.cryptoplatzi.network.Callback
import com.example.cryptoplatzi.network.FirestoreService
import com.example.cryptoplatzi.network.RealtimeDataListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.lang.Exception

class TraderActivity : AppCompatActivity(), CryptosAdapterListener {

    lateinit var fab:FloatingActionButton
    lateinit var recyclerView: RecyclerView
    lateinit var infoPanel: LinearLayout
    lateinit var usernameTextView : TextView
    lateinit var firestoreService: FirestoreService
    private val cryptosAdapter: CryptosAdapter = CryptosAdapter(this)
    private var username:String? = null
    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trader)
        firestoreService = FirestoreService(FirebaseFirestore.getInstance())
        usernameTextView = findViewById(R.id.usernameTextView)
        fab = findViewById(R.id.fab)
        recyclerView = findViewById(R.id.recyclerView)
        infoPanel = findViewById(R.id.infoPanel)

        username = intent.extras!![Constants.USERNAME_KEY]!!.toString()
        usernameTextView.text = username

        configureRecyclerView()
        loadCryptos()

        fab.setOnClickListener { view ->
            Snackbar.make(view, getString(R.string.generating_new_cryptos), Snackbar.LENGTH_SHORT)
                .setAction("Info", null).show()
            generateCryptoCurrenciesRandom()

        }
    }

    private fun generateCryptoCurrenciesRandom() {
        for (crypto in cryptosAdapter.cryptoList){
            val amount = (1..10).random()
            crypto.available += amount
            firestoreService.updateCrypto(crypto)
        }
    }

    private fun loadCryptos() {
        firestoreService.getCryptos(object: Callback<List<Crypto>>{
            override fun onSuccess(cryptoList: List<Crypto>?) {
                firestoreService.findUserById(username!!,object: Callback<User>{
                    override fun onSuccess(result: User?) {
                        user = result
                        if (user!!.cryptoList == null){
                            val userCryptoList = mutableListOf<Crypto>()

                            for (crypto in cryptoList!!){
                                val cryptoUser = Crypto()
                                cryptoUser.name = crypto.name
                                cryptoUser.available = crypto.available
                                cryptoUser.imageUrl = crypto.imageUrl
                                userCryptoList.add(cryptoUser)
                            }
                            user!!.cryptoList = userCryptoList
                            firestoreService.updateUser(user!!,null)
                        }
                        loadUserCryptos()
                        addRealtimeDatabaseListeners(user!!,cryptoList!!)
                    }

                    override fun onFailed(exception: Exception) {
                        showGeneralServerErrorMessage()
                    }
                })

                this@TraderActivity.runOnUiThread {
                    cryptosAdapter.cryptoList = cryptoList!!
                    cryptosAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailed(exception: Exception) {
                Log.e("TraderActivity", "error loading cryptos", exception)
                showGeneralServerErrorMessage()
            }

        })
    }

    private fun addRealtimeDatabaseListeners(user: User, cryptoList: List<Crypto>) {
        firestoreService.listenForUpdates(user,object :RealtimeDataListener<User>{
            override fun onDataChange(updatedData: User) {
                this@TraderActivity.user=updatedData
                loadUserCryptos()
            }

            override fun onError(exception: Exception) {
                showGeneralServerErrorMessage()
            }

        })

        firestoreService.listenForUpdates(cryptoList,object :RealtimeDataListener<Crypto>{
            override fun onDataChange(updatedData: Crypto) {
                var position = 0
                for (crypto in cryptosAdapter.cryptoList){
                    if (crypto.name.equals(updatedData.name)){
                        crypto.available = updatedData.available
                        cryptosAdapter.notifyItemChanged(position)
                    }
                    position++
                }
            }

            override fun onError(exception: Exception) {
                showGeneralServerErrorMessage()
            }

        })
    }

    private fun loadUserCryptos() {
        runOnUiThread {
            if (user != null && user!!.cryptoList != null){
               infoPanel.removeAllViews()
                for (crypto in user!!.cryptoList!!){
                    addUserCryptoInfoRow(crypto)
                }
            }
        }
    }

    private fun addUserCryptoInfoRow(crypto: Crypto) {
        val view= LayoutInflater.from(this).inflate(R.layout.coin_info, infoPanel,false)
        view.findViewById<TextView>(R.id.coinLabel).text = getString(R.string.coin_info, crypto.name, crypto.available.toString())
        Picasso.get().load(crypto.imageUrl).into(view.findViewById<ImageView>(R.id.coinIcon))
        infoPanel.addView(view)
    }

    private fun configureRecyclerView() {
        recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = cryptosAdapter
    }

    fun showGeneralServerErrorMessage() {
        Snackbar.make(fab, getString(R.string.error_while_connecting_to_the_server), Snackbar.LENGTH_LONG)
            .setAction("Info", null).show()
    }

    override fun onBuyCryptoClicked(crypto: Crypto) {
        if (crypto.available > 0){
            for (userCrypto in user!!.cryptoList!!){
                if (userCrypto.name == crypto.name){
                    userCrypto.available += 1
                    break
                }
            }
            crypto.available--

            firestoreService.updateUser(user!!,null)
            firestoreService.updateCrypto(crypto)
        }
    }
}