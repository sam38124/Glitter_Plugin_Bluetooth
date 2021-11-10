package com.orange.ble_plugin

import java.util.*

class JzClock2{
    var past = Date()

    fun zeroing(){
        past = Date()
    }
    fun stop():Double{
        return getDatePoor(Date(),past)
    }
    fun getDatePoor(endDate: Date, nowDate: Date): Double {
        val diff = endDate.time - nowDate.time
        return diff.toDouble() / 1000
    }
}