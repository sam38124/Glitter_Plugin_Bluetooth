package com.orange.glitter_plugin_bluetooth

import android.app.Application
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.jianzhi.glitter.GlitterActivity
import com.orange.ble_plugin.Glitter_BLE


class   MyApp : Application() {
    var handler = Handler(Looper.getMainLooper())
    override fun onCreate() {
        super.onCreate()
        Glitter_BLE(applicationContext).create()
        GlitterActivity.setUp("file:///android_asset/appData",appName = "appData")
        GlitterActivity.addActivityResult(object : GlitterActivity.ResultCallBack {
            override fun resultBack(requestCode: Int, resultCode: Int, data: Intent) {

            }
        })
    }
}

