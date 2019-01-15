package com.github.gam2046.xposed.base


import android.app.AndroidAppHelper
import android.app.Application
import android.content.pm.ApplicationInfo
import android.util.Log
import de.robv.android.xposed.XC_MethodHook

/**
 * 自定义Activity方法hook类
 * Create Date 2018-02-20
 * @author forDream
 */
abstract class BaseMethodHook : XC_MethodHook() {

    fun methodArgsLog(param: MethodHookParam, tag: String) {
        val builder = StringBuilder()
        builder.append("===========DEBUG INFO METHOD ${param.thisObject.javaClass.name}.${param.method.name} $tag BGN===========\n")

        if (param.args != null && param.args.isNotEmpty()) {
            var count = 0
            param.args.forEach {
                builder.append("=>")
                    .append("Method Args $count\t")
                    .append(it.javaClass.name)
                    .append('\t')
                    .append(this.argsToString(it))
                    .append('\n')
                count++
            }
        } else {
            builder.append("=>").append("No Args").append('\n')
        }

        builder.append("===========DEBUG INFO METHOD ${param.thisObject.javaClass.name}.${param.method.name} $tag END===========")
        Log.d(RealEntry.LOG_TAG, builder.toString())
    }

    fun argsToString(args: Any?): String =
        when (args) {
            is Array<*> -> {
                val builder = StringBuilder()
                builder.append('[')
                args.forEach { builder.append(this.argsToString(it ?: "<null>")).append(", ") }
                builder.append(']')
                builder.toString()
            }
            else -> {
                args.toString()
            }
        }


    final override fun afterHookedMethod(param: MethodHookParam) {
        super.afterHookedMethod(param)

        this.afterHooked(
            AndroidAppHelper.currentPackageName(),
            AndroidAppHelper.currentProcessName(),
            AndroidAppHelper.currentApplication()?.classLoader,
            AndroidAppHelper.currentApplication(),
            AndroidAppHelper.currentApplicationInfo(),
            param
        )

//        this.methodArgsLog(param,"After")
    }

    final override fun beforeHookedMethod(param: MethodHookParam) {
        super.beforeHookedMethod(param)

//        this.methodArgsLog(param,"Before")

        this.beforeHooked(
            AndroidAppHelper.currentPackageName(),
            AndroidAppHelper.currentProcessName(),
            AndroidAppHelper.currentApplication()?.classLoader,
            AndroidAppHelper.currentApplication(),
            AndroidAppHelper.currentApplicationInfo(),
            param
        )
    }

    abstract fun beforeHooked(
        targetPackageName: String,
        targetProcessName: String,
        targetClassLoader: ClassLoader?,
        targetApplication: Application?,
        targetApplicationInfo: ApplicationInfo,
        param: MethodHookParam
    )

    abstract fun afterHooked(
        targetPackageName: String,
        targetProcessName: String,
        targetClassLoader: ClassLoader?,
        targetApplication: Application?,
        targetApplicationInfo: ApplicationInfo,
        param: MethodHookParam
    )
}