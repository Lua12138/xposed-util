package com.github.gam2046.xposed.base

import android.app.AndroidAppHelper
import android.util.Log
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Create Date 2018-02-21
 * @author forDream
 */
class RealEntry : IXposedHookLoadPackage {
    companion object {
        const val LOG_TAG = "forDream-XPOSED"
    }

    override fun handleLoadPackage(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        // 获得所有entry
        try {
            // 固定查找此类
            val clazz = Class.forName("com.github.gam2046.xposed.EntryPoint")
            val instance = clazz.newInstance() as EntryConfiguration

            val entries = instance.entries()

            entries.filter { it.tryHook(loadPackageParam.packageName, loadPackageParam) }
                .forEach {
                    it.doHookEntry(
                        AndroidAppHelper.currentPackageName(),
                        AndroidAppHelper.currentProcessName(),
                        loadPackageParam.classLoader,
                        AndroidAppHelper.currentApplication(),
                        AndroidAppHelper.currentApplicationInfo(),
                        loadPackageParam
                    )
                }
        } catch (e: ClassNotFoundException) {
            Log.e(LOG_TAG, "Cannot found Entry")
        }
    }
}

interface EntryConfiguration {
    fun entries(): Array<BaseEntry>
}