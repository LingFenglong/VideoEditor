package com.lingfenglong.videoeditor.entity.effect

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Merge
import androidx.media3.common.Effect

class MergeEffectInfo(
    val uri: Uri,
    val position: Position,
    effect: () -> Effect
) : EffectInfo("合并", { Icons.Default.Merge }, effect) {

    override fun doEffect() {

    }

    enum class Position {
        BEFORE,
        AFTER,
        NONE
    }
}