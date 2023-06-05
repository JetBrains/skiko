package org.jetbrains.skiko.context

import org.jetbrains.skiko.SkiaLayer

internal abstract class JvmContextHandler(layer: SkiaLayer) : SkiaLayerContextHandler(layer, layer::draw)