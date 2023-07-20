package com.ensody.reactivestate

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.transformLatest

/**
 * Executes each lambda in a [Flow] using [conflatedMap].
 *
 * Computes first and last element and intermediate when possible. The first computation starts immediately and we throw
 * away all intermediate changes until the computation is finished and then recompute again for the last change that
 * happened in the meantime.
 *
 * This is useful e.g. when you have a constant stream of events (e.g. WebSocket change notifications, keyboard input,
 * mouse moved, etc.) and you want to show intermediate results.
 *
 * If you only want to show the latest result you can use [latestWorker].
 *
 * @param timeoutMillis Additional delay before the last element is computed (throwing away intermediate elements).
 */
public fun <T, R> Flow<T>.conflatedWorker(timeoutMillis: Long = 0, transform: FlowTransform<T, R>): Flow<R> =
    conflatedTransform(timeoutMillis, transform)

/**
 * Executes each lambda in a [Flow] using [debounce] and [mapLatest].
 *
 * Warning: This will not compute anything if new entries keep coming in at a rate faster than [timeoutMillis]!
 * This also adds a delay before the first execution!
 *
 * Especially in UIs you'll usually want to:
 *
 * - execute as quickly as possible, so that the UI feels snappy
 * - get intermediate results to provide some feedback
 *
 * This is why you'll usually want to use [conflatedWorker].
 *
 * @param timeoutMillis The [debounce] timeout.
 */
public fun <T, R> Flow<T>.latestWorker(timeoutMillis: Long = 0, transform: FlowTransform<T, R>): Flow<R> =
    debounce(timeoutMillis).transformLatest(transform)

/**
 * Executes each lambda in a [Flow] using [debounce] and [map].
 *
 * Warning: This will not compute anything if new entries keep coming in at a rate faster than [timeoutMillis]!
 * This also adds a delay before the first execution!
 *
 * Especially in UIs you'll usually want to:
 *
 * - execute as quickly as possible, so that the UI feels snappy
 * - get intermediate results to provide some feedback
 *
 * This is why you'll usually want to use [conflatedWorker].
 *
 * @param timeoutMillis The [debounce] timeout.
 */
public fun <T, R> Flow<T>.debounceWorker(timeoutMillis: Long = 0, transform: FlowTransform<T, R>): Flow<R> =
    debounce(timeoutMillis).transform(transform)

/**
 * Maps a conflated [Flow] with [timeoutMillis] delay between the first and last element.
 *
 * Maps first and last element and intermediate when possible. The first map starts immediately and we throw away all
 * intermediate changes until the computation is finished and then we map again for the last change that happened in the
 * meantime.
 *
 * @param timeoutMillis Additional delay before the last element is mapped (throwing away intermediate elements).
 */
public inline fun <T, R> Flow<T>.conflatedMap(
    timeoutMillis: Long = 0,
    crossinline transform: suspend (value: T) -> R,
): Flow<R> = conflatedTransform(timeoutMillis = timeoutMillis) { emit(transform(it)) }

/** Transforms a conflated [Flow] with [timeoutMillis] delay between the first and last element. */
public inline fun <T, R> Flow<T>.conflatedTransform(
    timeoutMillis: Long = 0,
    crossinline transform: suspend FlowCollector<R>.(value: T) -> Unit,
): Flow<R> = conflate().transform(transform).addDelay(timeoutMillis)

/** Adds a [timeoutMillis] delay to a [Flow]. If delay is zero or negative this is a no-op. */
public fun <T> Flow<T>.addDelay(timeoutMillis: Long): Flow<T> =
    if (timeoutMillis <= 0L) this else onEach { delay(timeoutMillis) }
