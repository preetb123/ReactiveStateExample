package com.ensody.reactivestate

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
fun getDispatchersIO(): CoroutineDispatcher =
    Dispatchers.IO
