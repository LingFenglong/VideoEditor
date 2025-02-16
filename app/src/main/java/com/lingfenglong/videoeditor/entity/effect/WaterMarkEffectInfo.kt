package com.lingfenglong.videoeditor.entity.effect

import androidx.annotation.OptIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.BrandingWatermark
import androidx.media3.common.Effect
import androidx.media3.common.util.UnstableApi

@OptIn(UnstableApi::class)
class WaterMarkEffectInfo(
    effect: () -> Effect
) : EffectInfo("水印", { Icons.AutoMirrored.Rounded.BrandingWatermark }, effect) {

    override fun doEffect() {
        TODO("Not yet implemented")
    }

}