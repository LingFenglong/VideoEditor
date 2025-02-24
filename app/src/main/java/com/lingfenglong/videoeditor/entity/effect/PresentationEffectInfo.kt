package com.lingfenglong.videoeditor.entity.effect

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Compress
import androidx.media3.common.Effect

class PresentationEffectInfo(
    val wide: Int,
    val height: Int,
    effect: () -> Effect,
) : EffectInfo("压缩", { Icons.Rounded.Compress }, effect) {

    override fun doEffect() {
        
    }
}
