package com.lingfenglong.videoeditor.entity

import androidx.compose.ui.graphics.vector.ImageVector

data class VideoEditingTool(
    val id: Int,
    val name: String,
    val icon: ImageVector,
    val makeEffect: () -> Unit
)
