package com.lingfenglong.videoeditor.entity.effect

import androidx.annotation.OptIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Compress
import androidx.media3.common.Effect
import androidx.media3.common.util.UnstableApi

@OptIn(UnstableApi::class)
class CompressEffectInfo(
    val wide: Int,
    val height: Int,
    effect: () -> Effect,
) : EffectInfo("压缩", { Icons.Rounded.Compress }, effect) {

    override fun doEffect() {
        TODO("Not yet implemented")
    }
}
