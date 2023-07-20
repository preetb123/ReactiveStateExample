package com.ensody.reactivestate

import kotlinx.coroutines.CancellationException

fun Throwable.isFatal(): Boolean = this is VirtualMachineError || this is ThreadDeath || this is InterruptedException || this is LinkageError ||
        this is CancellationException

/** Throws this exception if it's fatal. Otherwise returns it. */
@Suppress("NOTHING_TO_INLINE")
public inline fun <T : Throwable> T.throwIfFatal(): T =
    if (isFatal()) throw this else this

/** Similar to the stdlib [runCatching], but uses [throwIfFatal] to re-throw fatal exceptions immediately. */
public inline fun <T> runCatchingNonFatal(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: Throwable) {
        Result.failure(e.throwIfFatal())
    }

/**
 * Similar to the stdlib [runCatching][Unit.runCatching], but uses [throwIfFatal] to re-throw fatal exceptions
 * immediately.
 */
public inline fun <T, R> T.runCatchingNonFatal(block: T.() -> R): Result<R> =
    try {
        Result.success(block())
    } catch (e: Throwable) {
        Result.failure(e.throwIfFatal())
    }
