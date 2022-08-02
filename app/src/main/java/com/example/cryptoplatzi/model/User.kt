package com.example.cryptoplatzi.model

// 1° Se crea el modelo de datos que va a contener el documento en firestore y para ello creamos una clase
//llamada User y este contendra el nombre de usuario y una lista de criptomonedas que tendra el a disposicion el usuario
class User {
    /*las variables las inicializamos para que registren información por default en caso de no llenar la información correspondiente en la app
    //la variable username va hacer la llave y la asiganción va hacer el valor en la base de datos
    //por lo que en la base de datos quedaria asi: Ej. username: "Alex" */
    var username: String =""
    var cryptoList: List<Crypto>? = null
}
