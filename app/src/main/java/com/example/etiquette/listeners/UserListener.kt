package com.example.etiquette.listeners

import com.example.etiquette.models.User

interface UserListener {
    fun onUserClicked(user: User)
}