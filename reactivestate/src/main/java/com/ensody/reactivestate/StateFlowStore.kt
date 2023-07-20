package com.ensody.reactivestate

import kotlin.properties.ReadOnlyProperty

/**
 * Base interface for a temporary observable key-value store.
 *
 * This is useful for multiplatform projects and also for abstracting away `SavedStateHandle`,
 * e.g. so you can write tests without Robolectric.
 */
public interface StateFlowStore {
    public operator fun contains(key: String): Boolean

    public fun <T> getData(key: String, default: T): MutableValueFlow<T>
}

/** For use with `by` delegation. Returns the [StateFlowStore] entry for the key that equals the property name. */
public fun <T> StateFlowStore.getData(default: T): ReadOnlyProperty<Any?, MutableValueFlow<T>> =
    propertyName { getData(it, default) }

/** A [StateFlowStore] that can be used for unit tests or non-Android parts of multiplatform projects. */
public class InMemoryStateFlowStore(
    /** Optional underlying data which can be used to store and restore the whole state. */
    public val underlyingData: MutableMap<String, Any?> = mutableMapOf(),
) : StateFlowStore {
    private val store = mutableMapOf<String, MutableValueFlow<*>>()

    override fun contains(key: String): Boolean = key in store

    override fun <T> getData(key: String, default: T): MutableValueFlow<T> =
        getData(key, default, null)

    @Suppress("UNCHECKED_CAST")
    public fun <T> getData(key: String, default: T, setter: ((value: T) -> Unit)?): MutableValueFlow<T> =
        store.getOrPut(key) {
            val data = MutableValueFlow(underlyingData.getOrElse(key) { default } as T) { value ->
                setter?.invoke(value)
                underlyingData[key] = value
            }
            store[key] = data
            data
        } as MutableValueFlow<T>
}

/**
 * A wrapper [StateFlowStore] that prefixes every key with a namespace.
 *
 * This is useful for preventing name clashes when passing [StateFlowStore]s to sub-components.
 */
public class NamespacedStateFlowStore(
    private val store: StateFlowStore,
    private val namespace: String,
) : StateFlowStore {
    override fun contains(key: String): Boolean = encodeKey(key) in store

    override fun <T> getData(key: String, default: T): MutableValueFlow<T> =
        store.getData(encodeKey(key), default)

    private fun encodeKey(key: String): String =
        "$namespace<<$key"
}
