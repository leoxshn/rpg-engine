package io.posidon.rpgengine.debug

interface ILogger {
    fun d(message: String)
    fun w(message: String)
    fun e(message: String)
    fun i(message: String)
}

inline operator fun ILogger.invoke(block: ILogger.() -> Unit) = block()

inline operator fun ILogger?.invoke(block: ILogger.() -> Unit) = this?.block()

inline fun ILogger.d(message: () -> String) = d(message())
inline fun ILogger.w(message: () -> String) = w(message())
inline fun ILogger.e(message: () -> String) = e(message())
inline fun ILogger.i(message: () -> String) = i(message())

inline fun ILogger.d(describable: Describable) = d(describable.describe())
inline fun ILogger.w(describable: Describable) = w(describable.describe())
inline fun ILogger.e(describable: Describable) = e(describable.describe())
inline fun ILogger.i(describable: Describable) = i(describable.describe())

inline fun ILogger.d(message: String, describable: Describable) = d(message + ' ' + describable.describe())
inline fun ILogger.w(message: String, describable: Describable) = w(message + ' ' + describable.describe())
inline fun ILogger.e(message: String, describable: Describable) = e(message + ' ' + describable.describe())
inline fun ILogger.i(message: String, describable: Describable) = i(message + ' ' + describable.describe())