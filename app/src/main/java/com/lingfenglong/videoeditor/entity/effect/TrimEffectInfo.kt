package com.lingfenglong.videoeditor.entity.effect

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.media3.common.Effect

class TrimEffectInfo(
    val start: Long,
    val end: Long,
    effect: () -> Effect
) : EffectInfo("分割", { Icons.AutoMirrored.Filled.CallSplit }, effect) {
    override fun doEffect() {

    }

    override fun toString(): String {
        return "TrimEffectInfo(start=$start, end=$end)"
    }
}