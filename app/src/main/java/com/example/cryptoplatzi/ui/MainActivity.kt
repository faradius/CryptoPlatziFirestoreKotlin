package com.example.cryptoplatzi.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.example.cryptoplatzi.R
import com.example.cryptoplatzi.model.Constants
import com.example.cryptoplatzi.model.User
import com.example.cryptoplatzi.network.Callback
import com.example.cryptoplatzi.network.FirestoreService
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.Exception

//4° procedemos a ejecutar codigo que ya se ha preparado anteriormente, la idea de este activity es
//simular un login haciendo uso de firestore para ello hacemos lo siguiente
class MainActivity : AppCompatActivity() {

    //Creamos la variable auth de tipo FirebaseAuth y le asignamos la instancia hacia nuestra cuenta de firebase
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    /*Aqui creamos una variable llamada firestoreService de tipo FirestoreService, esto con el fin de hacer uso
    //de la clase FirestoreService que dentro estan las funciones que haran peticiones a la base de datos*/
    lateinit var firestoreService: FirestoreService

    //Creamos una variable para el manejo de errores por consola que será utilizado mas adelante mediante la clase Log
    private val TAG = "LoginActivity"

    //Declaramos nuestros widgets
    lateinit var etUserName:EditText
    lateinit var btnLogin:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //En la variable firestoreService hacemos la instancia de la clase FireStoreService y le pasamos en el parametro
        //la instancia o referencia a nuestra base de datos
        firestoreService = FirestoreService(FirebaseFirestore.getInstance())

        //Vinculamos los widgets de nuestra interfaz grafica
        etUserName = findViewById(R.id.etUser)
        btnLogin = findViewById(R.id.btnLogin)

        //Ejecutamos el metodo cuando se precione el boton para loguearse,
        // para entender que hace este metodo procedemos a entender los metodos creados en esta clase
        onStartClicked()

    }

    /*3°-M: por ultimo creamos una función que nos permitirá autenticarnos de forma aunonima dentro de la aplicación y le mandaremos el nombre de usuario al
    * metodo saveUserAndStarMainActivity para que registre el usuario dentro de la base de datos y podamos pasar a la siguiente activity*/
    private fun onStartClicked(){
        //Habilitamos el evento OnclickListener
        btnLogin.setOnClickListener { view->
            //Al hacer click al boton de login desactivaremos el boton temporalmente
            view.isEnabled = false
            //Ejecutamos la función de autenticación Anonima y le agregamos la función de verificar si la autenticación se hizo correctamente
            auth.signInAnonymously().addOnCompleteListener { task ->
                //Preguntamos si la autenticación es exitoso entonces...
                if (task.isSuccessful){
                    //capturaremos en una variable el valor obtenido en el EdtitextUserName
                    val userDato = etUserName.text.toString()

                    //Despues, en la clase firestoreService ejecutamos el metodo findUserById para ver si el usuario que desea ingresar a la app
                    //ya se encuentra registrado en la base de datos, por lo que a la función le pasamos el username obtenido del editText y le pasamos
                    //un objeto de tipo Callback<User> que representa al modelo de datos que se desea obtener en esa respuesta
                    firestoreService.findUserById(userDato, object : Callback<User>{
                        //despues de implementar la interfaz se implementarán sus metodos, uno indica la respuesta satisfactoria y el otro una respuesta erronea
                        //en el primer metodo se espera como parametro un resultado de tipo User
                        override fun onSuccess(result: User?) {
                            //por lo que validamos que si el usuario no se encuentra registrado, entraremos en la condición y procederemos a guardar la información
                            // del usuario en la base de datos con las siguientes instrucciones...
                            if (result == null){
                                //Creamos una variable e instanciamos de la clase User para hacer uso del modelo de datos
                                val user = User()
                                //Colocamos el valor obtenido del editext y se lo pasamos a la variable username derivado del modelo User
                                user.username = userDato
                                //Se lo pasamos al metodo el valor obtenido como primer parametro y la vista
                                saveUserAndStarMainActivity(user,view)
                            }else{
                                //Ahora si el usuario ya se encuentra registrado en la base de datos pues simplemente pasamos a la siguiente activity
                                starMainActivity(userDato)
                            }
                        }

                        override fun onFailed(exception: Exception) {
                            showErrorMessge(view)
                        }

                    })



                }else{
                    //Si la autenticación anonima falla va a mostrar un error en panatalla y volvemos a activar el boton del login
                    showErrorMessge(view)
                    view.isEnabled
                }
            }
        }
    }

    /*2°-M: Creamos este metodo para guardar la información del usuario en la base de datos y le pasamos el valor del usuario al metodo
    //starMainActivity, este metodo pide dos parametros uno hace referencia al modelo de datos que necesitamos obtener lo cual debe de ser de tipo User
    //y el otro parametro es una vista, esto es para que podamos hacer uso del metodo showErrorMessage ya que este necesita obtener una vista para poder
    //el SnackBar*/
    private fun saveUserAndStarMainActivity(user: User, view:View){
        /*la variable firestoreService tiene la referencia a la base de datos y a su vez nos permite hacer uso de los metodos
        //que tiene la clase FirestoreService el cual vamos a ejecutar el metodo setDocument y este nos pedira algunos datos
        //los cuales son:
        //user-> representa el modelo de datos de tipo User
        //Constants.USER_COLLECTION_NAME -> representa el nombre de la colección
        //user.name -> vamos a poner del modelo User el nombre del usuario
        //object: Callback<Void> -> hacemos uso de la interfaz e implementamos sus metodos

        Recordemos que este metodo nos permitirá crear un documento con su respectiva información en la base de datos*/
        firestoreService.setDocument(user, Constants.USERS_COLLECTION_NAME, user.username, object :Callback<Void>{

            /*Si la operación en la base de datos fue exitosa le pasaremos al metodo starMainActivity el nombre de usuario
            //este metodo se encargará de pasarle esa información a la otra activity*/
            override fun onSuccess(result: Void?) {
                starMainActivity(user.username)
            }

            //Si la operación falla ejecutaremos la función showErrorMessage el nos mostrará en la interfaz de la app un mensaje de error al conectarse a la base de datos
            override fun onFailed(exception: Exception) {
                showErrorMessge(view)
                //con esta linea de comando vemos en si cual es el error que ocurrio en la app por medio de la consola
                Log.e(TAG, "error",exception)
                //si ocurrio un error con la base de datos activamos el boton login para que este pueda ser clickiado nuevamente
                view.isEnabled = true
            }

        })
    }

    //Metodo secundario: solo se creo este metodo para mostrar un mensaje de error en pantalla por medio de Snackbar
    private fun showErrorMessge(view:View) {
        Snackbar.make(view, "Error while connecting to the server", Snackbar.LENGTH_LONG)
            .setAction("Info", null).show()
    }

    /*1°-M: este primer metodo es creado es para poder enviar datos de un activity a otra y necesita recibir como parametro
    //el nombre de usuario lo cual será este dato que le pasaremos a la otra activity*/
    private fun starMainActivity(username:String){
        val intent = Intent(this, TraderActivity::class.java)
        intent.putExtra(Constants.USERNAME_KEY,username)
        startActivity(intent)
        //Destruye el activity despues de pasar a la otra activity, esto evitara que al presionar el boton atras ya no nos va a permitir ver el activity login
        finish()
    }

}