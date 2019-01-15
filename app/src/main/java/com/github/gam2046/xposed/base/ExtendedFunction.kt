package com.github.gam2046.xposed.base


import android.app.AndroidAppHelper
import android.os.Handler
import android.os.Looper
import android.widget.Toast

/**
 * 一些扩展函数
 * Created by forDream on 2018/3/3.
 */
fun Any.showToast(message: String, duration: Int = Toast.LENGTH_LONG) {
    Handler(Looper.getMainLooper()).post {
        Toast.makeText(
            AndroidAppHelper.currentApplication().applicationContext,
            message,
            duration
        )
            .show()
    }
}

fun Any.printCurrentStacks() =
    Throwable().printStackTrace()