package com.orange.ble_plugin

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.jianzhi.glitter.GlitterActivity
import com.jianzhi.glitter.JavaScriptInterFace
import com.jianzhi.glitter.util.GpsUtil
import com.jianzhi.jzblehelper.BleHelper
import com.jianzhi.jzblehelper.callback.BleCallBack
import com.jianzhi.jzblehelper.models.BleBinary
import java.nio.charset.StandardCharsets

/*
   * Ble開發套件
   * */

 class Glitter_BLE(var context: Context) {
     var bleHelper: BleHelper = BleHelper(context,BleInterFace())
     val handler : Handler =Handler(Looper.getMainLooper())
      fun create(){
         val glitterName="Glitter_BLE"
         //Start
         GlitterActivity.addJavacScriptInterFace(JavaScriptInterFace( "${glitterName}Start") {
             it.responseValue["result"] = true
             it.finish()
         })
         //StartScan
         GlitterActivity.addJavacScriptInterFace(JavaScriptInterFace( "${glitterName}StartScan") {
             bleHelper.startScan()
             it.responseValue["result"] = true
             it.finish()
         })
         //StopScan
         GlitterActivity.addJavacScriptInterFace(JavaScriptInterFace( "${glitterName}StopScan") {
             bleHelper.stopScan()
             it.responseValue["result"] = true
             it.finish()
         })
         //WriteHex
         GlitterActivity.addJavacScriptInterFace(JavaScriptInterFace( "${glitterName}WriteHex") {
             bleHelper.writeHex(
                 it.receiveValue["data"].toString(),
                 it.receiveValue["rxChannel"].toString(),
                 it.receiveValue["txChannel"].toString(),
             )
             it.responseValue["result"] = true
             it.finish()
         })
         //WriteUtf
         GlitterActivity.addJavacScriptInterFace(JavaScriptInterFace( "${glitterName}WriteUtf") {
             bleHelper.writeUtf(
                 it.receiveValue["data"].toString(),
                 it.receiveValue["rxChannel"].toString(),
                 it.receiveValue["txChannel"].toString())
             it.responseValue["result"] = true
             it.finish()
         })
         //WriteBytes
         GlitterActivity.addJavacScriptInterFace(JavaScriptInterFace( "${glitterName}WriteBytes") {
             bleHelper.writeBytes(
                 it.receiveValue["data"] as ByteArray,
                 it.receiveValue["rxChannel"].toString(),
                 it.receiveValue["txChannel"].toString()
             )
             it.responseValue["result"] = true
             it.finish()
         })
         //IsOpen
         GlitterActivity.addJavacScriptInterFace(JavaScriptInterFace( "${glitterName}IsOpen") {
             it.responseValue["result"] =bleHelper.bleadapter.isEnabled
             it.finish()
         })
         //IsDiscovering
         GlitterActivity.addJavacScriptInterFace(JavaScriptInterFace( "${glitterName}IsDiscovering") {
             it.responseValue["result"] = bleHelper.bleadapter.isDiscovering
             it.finish()
         })
         //Connect
         GlitterActivity.addJavacScriptInterFace(JavaScriptInterFace( "${glitterName}Connect") { request ->
             val timeOut = (request.receiveValue["timeOut"] as Double).toInt()
             bleHelper.connect(request.receiveValue["address"].toString(), timeOut) {
                 request.responseValue["result"] = it
                 request.finish()
             }
         })
         //DisConnect
         GlitterActivity.addJavacScriptInterFace(JavaScriptInterFace( "${glitterName}DisConnect") {
             bleHelper.disconnect()
             it.responseValue["result"] = true
             it.finish()
         })
         //IsConnect
         GlitterActivity.addJavacScriptInterFace(JavaScriptInterFace( "${glitterName}IsConnect") {
             it.responseValue["result"] = bleHelper.isConnect()
             it.finish()
         })
         //GpsIsEnable
          GlitterActivity.addJavacScriptInterFace(JavaScriptInterFace("${glitterName}gpsEnable"){
              for (permission in arrayOf(
                  android.Manifest.permission.ACCESS_COARSE_LOCATION,
                  android.Manifest.permission.ACCESS_FINE_LOCATION
              )) {
                  val permissionCheck =
                      ContextCompat.checkSelfPermission(context, permission)
                  if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                      it.responseValue["result"] = false
                      it.finish()
                  }else{
                      it.responseValue["result"] = isOpenGps()
                      it.finish()
                  }
              }

          })
     }
   inner class BleInterFace:BleCallBack{
       override fun needGPS() {

        //   handler.post { GlitterActivity.instance().webRoot.evaluateJavascript("glitter.share.bleCallBack.needGPS()", null) }
       }

       override fun onConnectFalse() {
           handler.post { GlitterActivity.instance().webRoot.evaluateJavascript("glitter.share.bleCallBack.onConnectFalse()", null) }
       }

       override fun onConnectSuccess() {
           handler.post { GlitterActivity.instance().webRoot.evaluateJavascript("glitter.share.bleCallBack.onConnectSuccess()", null) }
       }

       override fun onConnecting() {
           handler.post { GlitterActivity.instance().webRoot.evaluateJavascript("glitter.share.bleCallBack.onConnecting()", null) }
       }

       override fun onDisconnect() {
           handler.post { GlitterActivity.instance().webRoot.evaluateJavascript("glitter.share.bleCallBack.onDisconnect()", null) }
       }

       override fun requestPermission(permission: ArrayList<String>) {
           //當藍牙權限不足時觸發
           for (i in permission) {
               Log.e("JzBleMessage", "權限不足請先請求權限${i}")
           }
           handler.post {
               GlitterActivity.instance().webRoot.evaluateJavascript(
                   "glitter.share.bleCallBack.requestPermission(${
                       Gson().toJson(
                           permission
                       )
                   })", null
               )
           }
       }

       override fun rx(a: BleBinary) {
           Thread {
               val map: MutableMap<String, Any> = mutableMapOf()
               map["readHEX"] = a.readHEX()
               map["readBytes"] = a.readBytes()
               map["readUTF"] = a.readUTF()
               // Log.e("JzBleMessage","RX:"+a.readHEX())
               handler.post {
                   GlitterActivity.instance().webRoot.evaluateJavascript(
                       "glitter.share.bleCallBack.rx(" + Gson().toJson(map) + ")",
                       null
                   )
               }
           }.start()

       }

       override fun scanBack(device: BluetoothDevice, scanRecord: BleBinary, rssi: Int) {
           try {
               val map: MutableMap<String, Any> = mutableMapOf()
               map["name"] = if (device.name == null) "undefine" else device.name
               map["address"] = device.address
               val rec: MutableMap<String, Any> = mutableMapOf()
               rec["readHEX"] = scanRecord.readHEX()
               rec["readBytes"] = scanRecord.readBytes()
               rec["readUTF"] = String(rec["readBytes"] as ByteArray, StandardCharsets.UTF_8);
               handler.post {
                   GlitterActivity.instance().webRoot.evaluateJavascript(
                       "glitter.share.bleCallBack.scanBack(" + Gson().toJson(map) + "," + Gson().toJson(
                           rec
                       ) + ",$rssi)", null
                   )
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
           handler.post {
               GlitterActivity.instance().webRoot.evaluateJavascript(
                   "glitter.share.bleCallBack.tx(" + Gson().toJson(map) + ")",
                   null
               )
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
 }

