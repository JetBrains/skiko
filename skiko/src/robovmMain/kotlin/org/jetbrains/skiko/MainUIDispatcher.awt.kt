package org.jetbrains.skiko

import kotlinx.coroutines.CoroutineDispatcher

// FIXME: have to keep it at .awt name as .desktop implementations uses actual that point to it
val MainUIDispatcher: CoroutineDispatcher
    get() = SkikoDispatchers.Main
