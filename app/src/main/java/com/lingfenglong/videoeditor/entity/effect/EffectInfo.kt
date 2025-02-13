package com.lingfenglong.videoeditor.entity.effect

import androidx.annotation.OptIn
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.media3.common.Effect
import androidx.media3.common.util.UnstableApi

@OptIn(UnstableApi::class)
abstract class EffectInfo(
    val name: String,
    val icon: () -> ImageVector,
    val effect: () -> Effect
) {
    abstract fun doEffect()
}
