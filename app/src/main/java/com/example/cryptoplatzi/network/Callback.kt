package com.example.cryptoplatzi.network

import java.lang.Exception

/*2° Creamos una interfaz llamada Callback, este tendrá como función obtener el resultado de la operación
//hecha en la base de datos, es decir se ejecutará la función onSucces si la operación hacia la base fue correcta
//esta función no retornará nada, y la función onFailed simplemente es una función en que nos devolverá un error
//en caso en que la operación hacia la base de datos hubo un problema o error, este tampoco nos devolverá nada, solo nos
//mostrará el error*/
interface Callback <T>{
    fun onSuccess(result: T?)

    fun onFailed(exception: Exception)
}