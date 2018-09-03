package com.github.qezt.phonenumberidentifier


class Application : android.app.Application() {

    override fun onCreate() {
        super.onCreate()
        app = this
        //        Dexter.initialize(this);
    }

    companion object {
        lateinit var app: Application
    }
}
