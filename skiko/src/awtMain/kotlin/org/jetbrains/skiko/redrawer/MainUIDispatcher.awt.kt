package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.swing.Swing

val MainUIDispatcher: MainCoroutineDispatcher
    get() = Dispatchers.Swing
