package com.example.setaside

import android.app.Application
import com.example.setaside.data.remote.RetrofitClient

class SetAsideApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitClient.init(this)
    }
}
