package com.github.gam2046.xposed.base

import android.app.Application
import android.content.pm.ApplicationInfo
import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * 基础Hook入口，一个Entry类代表对一个应用程序的Hook需求
 * Create Date 2018-02-21
 * @author forDream
 */
abstract class BaseEntry {
    /**
     * 是否输出调试信息
     */
    protected var outputDebugInformation = true

    private class LogWrapperBaseMethodHook(val realHook: BaseMethodHook, val outputDebugInformation: Boolean) :
        BaseMethodHook() {
        private fun debugLog(
            targetPackageName: String,
            targetProcessName: String,
            targetClassLoader: ClassLoader?,
            targetApplication: Application?,
            targetApplicationInfo: ApplicationInfo,
            param: XC_MethodHook.MethodHookParam,
            logDirection: LogDirection
        ) {
            val currentObj: Any? = param.thisObject

            // target object information
            val builder = StringBuilder()
            builder.append("===========DEBUG METHOD $logDirection INFO BGN===========\n")
            builder.append("=>").append("packageName: ").append(targetPackageName).append('\n')
            builder.append("=>").append("processName: ").append(targetProcessName).append('\n')
            builder.append("=>").append("application: ").append(targetApplication).append('\n')
            builder.append("=>").append("applicationInfo: ").append(targetApplicationInfo).append('\n')
            builder.append("=>").append("classloader: ").append(targetClassLoader).append('\n')
            builder.append("=>").append("currentClass: ").append(currentObj?.javaClass?.name).append('\n')
            builder.append("=>").append("methodName: ").append(param.method.name).append('\n')
            if (logDirection == LogDirection.AfterInvoke) {
                builder.append("=>").append("methodResult: ").append(param.result).append('\n')
            } else if (logDirection == LogDirection.BeforeInvoke) {
                Throwable().stackTrace.forEach {
                    builder.append("=>Stacks: ")
                        .append("${it.className} ${it.methodName} ${it.fileName} Line ${it.lineNumber}").append('\n')
                }
                // current method sign
                val methods = currentObj?.javaClass?.declaredMethods
                methods?.forEach {
                    it.isAccessible = true
                    builder.append("=>Method :")
                        .append(currentObj.javaClass.name)
                        .append(it.toString())
                        .append('\n')
                }
            }

            // method args
            param.args?.forEach { builder.append("=>Method Args: ").append(it).append('\n') }

            // current object field

            val fields = currentObj?.javaClass?.declaredFields
            fields?.forEach {
                it.isAccessible = true
                builder.append("=>Field ")
                    .append("${it.type.name} ${currentObj.javaClass.name}.${it.name} = ")
                    .append(this@LogWrapperBaseMethodHook.argsToString(it.get(currentObj)))
                    .append('\n')
            }

            builder.append("===========DEBUG METHOD $logDirection INFO END===========")
            Log.d(RealEntry.LOG_TAG, builder.toString())
        }

        override fun beforeHooked(
            targetPackageName: String,
            targetProcessName: String,
            targetClassLoader: ClassLoader?,
            targetApplication: Application?,
            targetApplicationInfo: ApplicationInfo,
            param: XC_MethodHook.MethodHookParam
        ) {
            // double check
            if (this.outputDebugInformation) {
                this.debugLog(
                    targetPackageName,
                    targetProcessName,
                    targetClassLoader,
                    targetApplication,
                    targetApplicationInfo,
                    param,
                    LogDirection.BeforeInvoke
                )
            }

            this.realHook.beforeHooked(
                targetPackageName,
                targetProcessName,
                targetClassLoader,
                targetApplication,
                targetApplicationInfo,
                param
            )
        }

        override fun afterHooked(
            targetPackageName: String,
            targetProcessName: String,
            targetClassLoader: ClassLoader?,
            targetApplication: Application?,
            targetApplicationInfo: ApplicationInfo,
            param: XC_MethodHook.MethodHookParam
        ) {
            // double check
            if (this.outputDebugInformation) {
                this.debugLog(
                    targetPackageName,
                    targetProcessName,
                    targetClassLoader,
                    targetApplication,
                    targetApplicationInfo,
                    param,
                    LogDirection.AfterInvoke
                )
            }

            this.realHook.afterHooked(
                targetPackageName,
                targetProcessName,
                targetClassLoader,
                targetApplication,
                targetApplicationInfo,
                param
            )
        }

    }

    /**
     * 由子类返回，是否需要处理该应用程序
     * @return 如果需要处理该应用则返回true，否则返回false
     */
    abstract fun tryHook(
        targetPackageName: String,
        loadPackageParam: XC_LoadPackage.LoadPackageParam
    ): Boolean

    /**
     * 进行hook的具体返回
     */
    abstract fun doHook(
        targetPackageName: String,
        targetProcessName: String,
        targetClassLoader: ClassLoader,
        targetApplication: Application?,
        targetApplicationInfo: ApplicationInfo,
        loadPackageParam: XC_LoadPackage.LoadPackageParam
    )

    internal fun doHookEntry(
        targetPackageName: String,
        targetProcessName: String,
        targetClassLoader: ClassLoader,
        targetApplication: Application?,
        targetApplicationInfo: ApplicationInfo,
        loadPackageParam: XC_LoadPackage.LoadPackageParam
    ) {

        if (this.outputDebugInformation) {
            // application information
            val builder = StringBuilder()
            builder.append("===========DEBUG ENTRY INFO BGN===========\n")
            builder.append("=>").append("packageName: ").append(targetPackageName).append('\n')
            builder.append("=>").append("processName: ").append(targetProcessName).append('\n')
            builder.append("=>").append("application: ").append(targetApplication).append('\n')
            builder.append("=>").append("applicationInfo: ").append(targetApplicationInfo).append('\n')
            builder.append("===========DEBUG ENTRY INFO END===========")
            Log.d(RealEntry.LOG_TAG, builder.toString())
        }
        this.doHook(
            targetPackageName,
            targetProcessName,
            targetClassLoader,
            targetApplication,
            targetApplicationInfo,
            loadPackageParam
        )
    }

    protected open fun argsToString(args: Any): String =
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


    fun hookAllMethods(
        targetClass: Class<*>,
        targetMethodName: String,
        methodHook: BaseMethodHook
    ): Set<XC_MethodHook.Unhook> {
        var methodHookProxy = methodHook

        if (this.outputDebugInformation) {
            methodHookProxy = LogWrapperBaseMethodHook(methodHook, this.outputDebugInformation)
        }

        return XposedBridge.hookAllMethods(targetClass, targetMethodName, methodHookProxy)
    }

    /**
     * 注册一个构造方法的钩子
     *
     * @param targetClassName 目标类名
     * @param targetClassLoader 目标类加载器
     * @param targetMethodName 目标方法名
     * @param methodHook 钩子
     * @param targetMethodArgsType 目标方法参数类型签名 字符串表达式或者Class对象
     */
    fun findAndHookConstructor(
        targetClassName: String,
        targetClassLoader: ClassLoader,
        targetMethodName: String,
        methodHook: BaseMethodHook,
        vararg targetMethodArgsType: Any
    ): XC_MethodHook.Unhook {
        var methodHookProxy = methodHook

        if (this.outputDebugInformation) {
            methodHookProxy = LogWrapperBaseMethodHook(methodHook, this.outputDebugInformation)
        }

        // register
        return XposedHelpers.findAndHookConstructor(
            targetClassName,
            targetClassLoader,
            targetMethodName,
            *targetMethodArgsType,
            methodHookProxy
        )
    }

    /**
     * 注册一个方法钩子
     * @param targetClassName 目标类名
     * @param targetClassLoader 目标类加载器
     * @param targetMethodName 目标方法名
     * @param methodHook 钩子
     * @param targetMethodArgsType 目标方法参数类型签名 字符串表达式或者Class对象
     */
    fun findAndHookMethod(
        targetClassName: String,
        targetClassLoader: ClassLoader,
        targetMethodName: String,
        methodHook: BaseMethodHook,
        vararg targetMethodArgsType: Any
    ): XC_MethodHook.Unhook {
        var methodHookProxy = methodHook

        if (this.outputDebugInformation) {
            methodHookProxy = LogWrapperBaseMethodHook(methodHook, this.outputDebugInformation)
        }

        // register
        return XposedHelpers.findAndHookMethod(
            targetClassName,
            targetClassLoader,
            targetMethodName,
            *targetMethodArgsType,
            methodHookProxy
        )
    }
}

private enum class LogDirection {
    BeforeInvoke, AfterInvoke
}