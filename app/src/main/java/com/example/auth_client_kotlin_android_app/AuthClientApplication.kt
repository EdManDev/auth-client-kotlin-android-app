package com.example.auth_client_kotlin_android_app

import android.app.Application
import com.example.auth_client_kotlin_android_app.data.network.TokenManager

class AuthClientApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        TokenManager.init(this)
    }
}