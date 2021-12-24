package com.orange.ble_plugin

import android.app.ActivityManager
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.jianzhi.glitter.GlitterActivity
import com.jianzhi.glitter.JavaScriptInterFace
import com.jianzhi.glitter.RequestFunction
import com.jianzhi.jzblehelper.BleHelper
import com.jianzhi.jzblehelper.callback.BleCallBack
import com.jianzhi.jzblehelper.models.BleBinary
import java.nio.charset.StandardCharsets
import java.util.concurrent.CopyOnWriteArrayList

/*
   * Ble開發套件
   * */

class Glitter_BLE(var context: Context, var scanFilter: Array<String>? = null, var scanTiming: Double = 1.0) {
    var bleHelper: BleHelper = BleHelper(context, BleInterFace())
    val handler: Handler = Handler(Looper.getMainLooper())
    var bleMap: MutableMap<String, JzClock2> = mutableMapOf()
    var callBack: RequestFunction? = null
    fun create() {
        val glitterName = "Glitter_BLE"
        //Start
        GlitterActivity.addJavacScriptInterFace(JavaScriptInterFace("${glitterName}_Start") {
            it.responseValue["result"] = true
            it.finish()
        })
        //StartScan
        GlitterActivity.addJavacScriptInterFace(JavaScriptInterFace("${glitterName}_StartScan") {
            if(appOnForeground()){
                it.responseValue["result"] = bleHelper.startScan()
            }else{
                it.responseValue["result"] = false
            }
            it.finish()
        })
        //StopScan
        GlitterActivity.addJavacScriptInterFace(JavaScriptInterFace("${glitterName}_StopScan") {
            bleHelper.stopScan()
            it.responseValue["result"] = true
            it.finish()
        })
        //WriteHex
        GlitterActivity.addJavacScriptInterFace(JavaScriptInterFace("${glitterName}_WriteHex") {
            try {
                bleHelper.writeHex(
                    it.receiveValue["data"].toString(),
                    it.receiveValue["rxChannel"].toString(),
                    it.receiveValue["txChannel"].toString(),
                )
                it.responseValue["result"] = true
                it.finish()
            } catch (e: Exception) {
                e.printStackTrace()
                it.responseValue["result"] = false
                it.finish()
            }
        })
        //WriteUtf
        GlitterActivity.addJavacScriptInterFace(JavaScriptInterFace("${glitterName}_WriteUtf") {
            try {
                bleHelper.writeUtf(
                    it.receiveValue["data"].toString(),
                    it.receiveValue["rxChannel"].toString(),
                    it.receiveValue["txChannel"].toString()
                )
                it.responseValue["result"] = true
                it.finish()
            } catch (e: Exception) {
                e.printStackTrace()
                it.responseValue["result"] = false
                it.finish()
            }

        })
        //WriteBytes
        GlitterActivity.addJavacScriptInterFace(JavaScriptInterFace("${glitterName}_WriteBytes") {
            try {
                bleHelper.writeBytes(
                    it.receiveValue["data"] as ByteArray,
                    it.receiveValue["rxChannel"].toString(),
                    it.receiveValue["txChannel"].toString()
                )
                it.responseValue["result"] = true
                it.finish()
            } catch (e: Exception) {
                e.printStackTrace()
                it.responseValue["result"] = false
                it.finish()
            }
        })
        //IsOpen
        GlitterActivity.addJavacScriptInterFace(JavaScriptInterFace("${glitterName}_IsOpen") {
            it.responseValue["result"] = bleHelper.bleadapter.isEnabled
            it.finish()
        })
        //IsDiscovering
        GlitterActivity.addJavacScriptInterFace(JavaScriptInterFace("${glitterName}_IsDiscovering") {
            it.responseValue["result"] = BleHelper.isScanning
            it.finish()
        })
        //Connect
        GlitterActivity.addJavacScriptInterFace(JavaScriptInterFace("${glitterName}_Connect") { request ->
            val timeOut = (request.receiveValue["timeOut"] as Double).toInt()
            bleHelper.connect(request.receiveValue["address"].toString(), timeOut) {
                request.responseValue["result"] = it
                request.finish()
            }
        })
        //DisConnect
        GlitterActivity.addJavacScriptInterFace(JavaScriptInterFace("${glitterName}_DisConnect") {
            bleHelper.disconnect()
            it.responseValue["result"] = true
            it.finish()
        })
        //IsConnect
        GlitterActivity.addJavacScriptInterFace(JavaScriptInterFace("${glitterName}_IsConnect") {
            it.responseValue["result"] = bleHelper.isConnect()
            it.finish()
        })
        //NeedPermission
        GlitterActivity.addJavacScriptInterFace(JavaScriptInterFace("${glitterName}_NeedPermission") { request ->
            var requestSuccess = 0
            var requestCount = 0
            val notPermission: ArrayList<String> = arrayListOf()
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) arrayOf(
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_ADVERTISE,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) else arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            GlitterActivity.instance().getPermission(permission, object : GlitterActivity.permission_C {
                override fun requestSuccess(a: String) {
                    requestCount += 1
                    requestSuccess += 1
                    if (requestCount == permission.size) {
                        request.responseValue["result"] =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) false else (!isOpenGps())
                        request.finish()
                    }
                }

                override fun requestFalse(a: String) {
                    requestCount += 1
                    notPermission.add(a.toString())
                    if (requestCount == permission.size) {
                        request.responseValue["notPermission"] = notPermission
                        request.responseValue["result"] = true
                        request.finish()
                    }
                }
            })
        })
        //SetCallBack
        GlitterActivity.addJavacScriptInterFace(JavaScriptInterFace("${glitterName}_SetCallBack") { request ->
            callBack = request
        })
    }

    inner class BleInterFace : BleCallBack {
        override fun needGPS() {
            if (callBack != null) {
                callBack!!.responseValue.clear()
                callBack!!.responseValue["function"] = "needGPS"
                callBack!!.callBack()
            }
        }

        override fun onConnectFalse() {
            if (callBack != null) {
                callBack!!.responseValue.clear()
                callBack!!.responseValue["function"] = "onConnectFalse"
                callBack!!.callBack()
            }
        }

        override fun onConnectSuccess() {
            if (callBack != null) {
                callBack!!.responseValue.clear()
                callBack!!.responseValue["function"] = "onConnectSuccess"
                callBack!!.callBack()
            }
        }

        override fun onConnecting() {
            if (callBack != null) {
                callBack!!.responseValue.clear()
                callBack!!.responseValue["function"] = "onConnecting"
                callBack!!.callBack()
            }
        }

        override fun onDisconnect() {
            if (callBack != null) {
                callBack!!.responseValue.clear()
                callBack!!.responseValue["function"] = "onDisconnect"
                callBack!!.callBack()
            }
        }

        override fun requestPermission(permission: ArrayList<String>) {
            //當藍牙權限不足時觸發
            for (i in permission) {
                Log.e("JzBleMessage", "權限不足請先請求權限${i}")
            }
            if (callBack != null) {
                callBack!!.responseValue.clear()
                callBack!!.responseValue["function"] = "requestPermission"
                callBack!!.responseValue["data"] = permission
                callBack!!.callBack()
            }
        }

        override fun rx(a: BleBinary) {
            val map: MutableMap<String, Any> = mutableMapOf()
            map["readHEX"] = a.readHEX()
            map["readBytes"] = a.readBytes()
            map["readUTF"] = a.readUTF()
            if (callBack != null) {
                callBack!!.responseValue.clear()
                callBack!!.responseValue["function"] = "rx"
                callBack!!.responseValue["data"] = map
                callBack!!.callBack()
            }
        }

        var clock = JzClock2()
        var scanList: CopyOnWriteArrayList<MutableMap<String, Any>> = CopyOnWriteArrayList()
        override fun scanBack(device: BluetoothDevice, scanRecord: BleBinary, rssi: Int) {
            try {
                val map: MutableMap<String, Any> = mutableMapOf()
                map["name"] = if (device.name == null) "undefine" else device.name
                map["address"] = device.address
                map["readHEX"] = scanRecord.readHEX()
                map["rssi"] = rssi
                map["readBytes"] = scanRecord.readBytes()
                map["readUTF"] = String(map["readBytes"] as ByteArray, StandardCharsets.UTF_8);
                scanList.add(map)
                if (clock.stop() > 1) {
                    clock.zeroing()
                    if (callBack != null) {
                        callBack!!.responseValue.clear()
                        callBack!!.responseValue["function"] = "scanBack"
                        callBack!!.responseValue["device"] = scanList.clone()
                        callBack!!.callBack()

                        scanList.clear()
                    }
                }
            } catch (e: Exception) {
            }
        }


        override fun tx(b: BleBinary) {
            val map: MutableMap<String, Any> = mutableMapOf()
            map["readHEX"] = b.readHEX()
            map["readBytes"] = b.readBytes()
            map["readUTF"] = b.readUTF()
            Log.e("JzBleMessage", "TX:" + b.readHEX())
            if (callBack != null) {
                callBack!!.responseValue.clear()
                callBack!!.responseValue["function"] = "tx"
                callBack!!.responseValue["data"] = map
                callBack!!.callBack()
            }
        }
    }

    /**
     * 判斷GPS是否開啟，GPS或者AGPS開啟一個就認為是開啟的
     */
    fun isOpenGps(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // 通過GPS衛星定位，定位級別可以精確到街（通過24顆衛星定位，在室外和空曠的地方定位準確、速度快）
        val gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        // 通過WLAN或移動網路(3G/2G)確定的位置（也稱作AGPS，輔助GPS定位。主要用於在室內或遮蓋物（建築群或茂密的深林等）密集的地方定位）
        val network =
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return gps || network
    }
    open fun appOnForeground(): Boolean {
        val appProcesses: List<ActivityManager.RunningAppProcessInfo> =
            (GlitterActivity.instance().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).runningAppProcesses
        for (appProcess in appProcesses) {
            if (appProcess.processName.equals(GlitterActivity.instance().applicationContext.packageName) && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true
            }
        }
        return false
    }
}

