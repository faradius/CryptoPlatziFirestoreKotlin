package com.example.cryptoplatzi.network

import com.example.cryptoplatzi.model.Constants
import com.example.cryptoplatzi.model.Crypto
import com.example.cryptoplatzi.model.User
import com.google.firebase.firestore.FirebaseFirestore

/*3° Creamos una clase llamada FirestoreService, en esta clase pondremos todas las operaciones que queramos que haga
//la app con la base de datos, la clase requerira de un parametro de tipo FirebaseFirestore, es decir necesitaremos obtener
//la instancia o referencia hacia que base de datos vamos a trabajar, una vez obtenido ya podremos trabajar con los metodos
//creados en esta clase*/
class FirestoreService(val firebaseFirestore: FirebaseFirestore) {
    /*Dentro de la clase creamos las funciones de los diferentes operaciones que haremos a la base de datos, es decir,
    //tendremos que crear metodos que soliciten a la base como leer un dato, escribir cierta información a la base, eliminar alguna información
    //o actualizar datos*/

    /*Empezaremos con una función que nos permita crear un nuevo documento a la base de datos
    //La funcion setDocument nos pedira los siguientes parametros:
    //data: Any -> aqui le pasaremos el modelo de datos y este puede contener cualquier tipo de datos por ello indicamos que es una variable de tipo any
    //colletionName: String -> Aqui le pasaremos el nombre que le pondremos a la colleción de documentos
    //id: String -> Aqui le pondremos como será llamado el documento
    //callback: Callback<Void> -> Aqui mandaremos a llamar la interfaz que se encargará de gestionar si la operación es satisfactoria o erronea
    //al aclarar esto pasaremos a la siguiente linea de codigo*/
    fun setDocument(data: Any, collectionName: String, id: String, callback: Callback<Void>){
        /*la varibale firebaseFirestore contendrá la referencia de la base de datos, esto nos ayudará a ubicarnos a la raiz de la base
        //por lo tanto, una vez ubicados en la base procederemos a crear un documento con su respectiva información y para ello
        //haremos lo siguiente, dentro de la base creamos una collección y dentro de ello pondremos un documento y dentro del documento el modelo de datos
        //para hacer eso se usa esta linea de codigo:
        //firebaseFirestore.collection(collectionName).document(id).set(data)
        //firebaseFirestore -> nombre de la base
        //collection(colecctionName) -> que nombre le pondremos a la colección
        //document(id)-> que nombre le pondremos al documento
        //set(data)-> que datos va a llevar ese documento*/
        firebaseFirestore.collection(collectionName).document(id).set(data)
                /*Despues agregamos los metodos predefinidos de firestore que son addOnSuccessListener y addOnFailureListener
                //estos metodos simplemente se ejecutarán algo si la operación fue correcta o erronea haciendo y
                //dentro de cada metodo hacemos uso de la interfaz que construimos*/
            .addOnSuccessListener { callback.onSuccess(null) }
            .addOnFailureListener { exception-> callback.onFailed(exception) }
    }

    fun updateUser(user: User,callback: Callback<User>?){
        firebaseFirestore.collection(Constants.USERS_COLLECTION_NAME).document(user.username)
            .update("cryptoList",user.cryptoList)
            .addOnSuccessListener { result ->
                if (callback != null)
                    callback.onSuccess(user)
            }
            .addOnFailureListener { exception -> callback!!.onFailed(exception) }
    }

    fun updateCrypto(crypto: Crypto){
        firebaseFirestore.collection(Constants.CRYPTO_COLLECTION_NAME).document(crypto.getDocumentId())
            .update("available", crypto.available)
    }


    /*Esta función se encarga de hacer una consulta a la base de datos para obtener todas las criptomonedas
    //el callback indica que la respuesta que se espera en el parametro es una lista de tipo crypto*/
    fun getCryptos(callback: Callback<List<Crypto>>){
        /*Para obtener la información de todas la criptomonedas es necesario hacer referencia a la ubicación de la colección
        //despues con el metodo get se obtiene la información de todos los documentos que contiene la colección cryptos*/
        firebaseFirestore.collection(Constants.CRYPTO_COLLECTION_NAME).get()
                //Agregamos un SuccesListener para validar que la respuesta de la base es correcta
            .addOnSuccessListener { cryptoListDBresult ->
                //Si la respuesta de la solicitud es correcta recorreremos cada documento que se encuentra en la colección
                for (document in cryptoListDBresult){
                    //En una variable se almacenará el resultado obtenido y se creara un objeto del modelo Crypto
                    val cryptoList = cryptoListDBresult.toObjects(Crypto::class.java)
                    //Aqui validamos que si la obtención de los datos es correcta le pasaremos la lista de criptomonedas
                    callback!!.onSuccess(cryptoList)
                    //terminamos la iteración cuando termine de devolvernos toda la información
                    break
                }
        }
                //En caso de que falle la solicitud a los datos nos regresará el motivo del error
            .addOnFailureListener { exception -> callback!!.onFailed(exception) }
    }

    //Esta función se encargará de buscar un usuario en especifico en la colección de users en base a su ID
    //La función espera como parametro el id del usuario y una respuesta de tipo User
    fun findUserById(id: String, callback: Callback<User>){
        //Realizamos una busqueda en la colección users y dentro de ello buscamos el documento con el id correspondiente
        //despues con el metodo get obtenemos el resultado
        firebaseFirestore.collection(Constants.USERS_COLLECTION_NAME).document(id).get()
                //Despues agregamos el SuccessListener para verificar si la respuesta es satisfactoria
            .addOnSuccessListener { userResult ->
                //Si la respuesta a la petición solicitada es correcta validamos que el dato obtenido no este vacio
                if (userResult.data != null){
                    //si se cumple regresaremos una respuesta con la información del usuario en base al modelo User
                    callback.onSuccess(userResult.toObject(User::class.java))
                }else{
                    //pero si la respuesta a la petición solicitada es correcta pero no tiene nada ese usuario pues devolveremos un null
                    callback.onSuccess(null)
                }
            }//y si hay algun tipo de error lo verificamos con el FailureListener
            .addOnFailureListener {exception -> callback.onFailed(exception)  }
    }

    fun listenForUpdates(cryptos: List<Crypto>, listener:RealtimeDataListener<Crypto>){
        val cryptoReference = firebaseFirestore.collection(Constants.CRYPTO_COLLECTION_NAME)
        for (crypto in cryptos){
            cryptoReference.document(crypto.getDocumentId()).addSnapshotListener { snapshot, error ->
                if(error != null){
                    listener.onError(error)
                }
                if (snapshot != null && snapshot.exists()){
                    listener.onDataChange(snapshot.toObject(Crypto::class.java)!!)
                }
            }
        }
    }

    fun listenForUpdates(user: User, listener:RealtimeDataListener<User>){
        val usersReference = firebaseFirestore.collection(Constants.USERS_COLLECTION_NAME)
        usersReference.document(user.username).addSnapshotListener { snapshot, error ->
            if(error != null){
                listener.onError(error)
            }
            if (snapshot != null && snapshot.exists()){
                listener.onDataChange(snapshot.toObject(User::class.java)!!)
            }
        }
    }

}