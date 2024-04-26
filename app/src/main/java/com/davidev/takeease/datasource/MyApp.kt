package com.davidev.takeease.datasource

import android.app.Application


class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        TaskDataSource.dbHelper = DBHelper(this)
    }


}
