package com.lingfenglong.videoeditor.entity

import androidx.annotation.OptIn
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.media3.common.Effect
import androidx.media3.common.util.UnstableApi

@OptIn(UnstableApi::class)
data class EffectInfo(
    val name: String,
    val icon: () -> ImageVector,
    val effect: () -> Effect,
)
