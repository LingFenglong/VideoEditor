package com.lingfenglong.videoeditor.entity.effect

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.media3.common.Effect

class RotateEffectInfo(effect: () -> Effect) : EffectInfo("旋转", { Icons.AutoMirrored.Filled.RotateLeft }, effect) {
    override fun doEffect() {

    }
}