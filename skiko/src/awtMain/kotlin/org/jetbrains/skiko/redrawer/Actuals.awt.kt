package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.swing.Swing

actual val MainUIDispatcher: MainCoroutineDispatcher
    get() = Dispatchers.Swing
