package com.github.qezt.phonenumberidentifier

import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.experimental.*
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.EmptyCoroutineContext


/**
 * Example Usage:
 *
 * val deferred = (1..1000_000L).map { n ->
 *     async (CommonPool) {
 *         n
 *     }
 * }
 * runBlocking {
 *     val sum = deferred.sumByDouble { it.await().toDouble() }
 *     println("$sum")
 * }
 * // It takes about 10 seconds in my Pixel XL
 */
@Suppress("unused")
object Coroutine {

    private const val TAG = "Coroutine"

    private fun <T> async(
            context: CoroutineContext,
            start: CoroutineStart = CoroutineStart.DEFAULT,
            parent: Job? = null,
            block: suspend CoroutineScope.() -> T
    ) = kotlinx.coroutines.experimental.async(context, start, parent, {
        try {
            block()
        } catch (e : JobCancellationException) {
            // ignored
            Log.i(TAG, "Job cancelled")
            throw e
        } catch (e : Exception) {
            Log.e(TAG, "caught by Coroutine")
            e.printStackTrace()
            Log.e(TAG, "stacktrace ended")
            throw e
        }
    })

    fun <T> asyncOnUI(
            start: CoroutineStart = CoroutineStart.DEFAULT,
            parent : Job? = null,
            block: suspend CoroutineScope.() -> T
    ) = async(UIPool, start, parent, block)

    fun <T> asyncOnWorkers(
            start: CoroutineStart = CoroutineStart.DEFAULT,
            parent : Job? = null,
            block: suspend CoroutineScope.() -> T
    ) = async(WorkerPool, start, parent, block)

    private fun launch(
            context: CoroutineContext = DefaultDispatcher,
            start: CoroutineStart = CoroutineStart.DEFAULT,
            parent : Job? = null,
            block: suspend CoroutineScope.() -> Unit
    ) = kotlinx.coroutines.experimental.launch(context, start, parent, {
        try {
            block()
        } catch (e : JobCancellationException) {
            // ignored
            Log.i(TAG, "Job cancelled")
            throw e
        } catch (e : Exception) {
            Log.e(TAG, "caught by Coroutine")
            e.printStackTrace()
            Log.e(TAG, "stacktrace ended")
            throw e
        }
    })

    fun launchOnUI(
            start: CoroutineStart = CoroutineStart.DEFAULT,
            parent : Job? = null,
            block: suspend CoroutineScope.() -> Unit
    ) = launch(UIPool, start, parent, block)

    fun launchOnWorkers(
            start: CoroutineStart = CoroutineStart.DEFAULT,
            parent : Job? = null,
            block: suspend CoroutineScope.() -> Unit
    ) = launch(WorkerPool, start, parent, block)

    fun <T> runBlocking(context: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> T): T
            = kotlinx.coroutines.experimental.runBlocking(context, block)

    suspend fun delay(millis : Long) = kotlinx.coroutines.experimental.delay(millis)

    suspend inline fun <T> cancellableCoroutine (
            holdCancellability: Boolean = false,
            crossinline block: (CancellableContinuation<T>) -> Unit
    ): T = suspendCancellableCoroutine(holdCancellability = holdCancellability, block = block)


    private val WorkerPool by lazy {
        kotlinx.coroutines.experimental.CommonPool
    }

    private val UIPool by lazy {
        kotlinx.coroutines.experimental.android.UI
    }

//    class Delayer(private val duration : Long = Consts.PROGRESS_ANIMATION_DELAY) {
//        private val startTime = SystemClock.elapsedRealtime()
//        suspend fun sync() {
//            val supposedDelay = startTime + duration - SystemClock.elapsedRealtime()
//            if (supposedDelay > 0) {
//                Coroutine.delay(supposedDelay)
//            }
//        }
//    }

    suspend fun waitFor(maxDuration: Long = 3000,
                                 checkInterval : Long = 50,
                                 condition : () -> Boolean) : Boolean {
        val endTime = SystemClock.elapsedRealtime() + maxDuration
        while (true) {
            if (condition()) return true
            delay(checkInterval)
            if (SystemClock.elapsedRealtime() >= endTime) return false
        }
    }
}
