package com.example.etiquette.models

import java.io.Serializable

data class User(
    val name: String = "",
    val email: String = "",
    val image: String = "",
    val token: String = "",
    var id: String = ""
) : Serializable