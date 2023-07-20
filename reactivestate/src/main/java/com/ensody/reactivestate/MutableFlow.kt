package com.ensody.reactivestate

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * A [Flow] where you can [emit]/[tryEmit] values into (backed by a [Channel]).
 */
public interface MutableFlow<T> : Flow<T>, FlowCollector<T> {
    /** Adds a value to this Flow if there's still capacity left. */
    public fun tryEmit(value: T): Boolean

    public fun isEmpty(): Boolean

    public fun cancel()
}

/** Creates a [MutableFlow]. */
public fun <T> MutableFlow(
    capacity: Int = Channel.RENDEZVOUS,
    onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND,
): MutableFlow<T> =
    MutableFlowImpl(
        Channel(
            capacity = capacity,
            onBufferOverflow = onBufferOverflow,
        ),
    )

private class MutableFlowImpl<T>(
    private val flow: Channel<T>,
) : MutableFlow<T>, Flow<T> by flow.receiveAsFlow() {

    override fun tryEmit(value: T): Boolean =
        flow.trySend(value).isSuccess

    override suspend fun emit(value: T) {
        flow.send(value)
    }

    override fun isEmpty(): Boolean =
        flow.isEmpty

    override fun cancel() {
        flow.cancel()
    }
}
