package com.lingfenglong.videoeditor.constant

import android.graphics.Bitmap.CompressFormat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.BrandingWatermark
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.Merge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lingfenglong.videoeditor.entity.VideoEditingTool
import com.lingfenglong.videoeditor.view.CompressEffect
import com.lingfenglong.videoeditor.view.MergeEffect
import com.lingfenglong.videoeditor.view.TrimClipEffect
import com.lingfenglong.videoeditor.view.WaterMarkEffect
import com.lingfenglong.videoeditor.viewmodel.VideoEditorViewModel

class VideoEditingTools {
    companion object {
        @Composable
        fun getAllTools(): List<VideoEditingTool> {
            val viewModel = viewModel(modelClass = VideoEditorViewModel::class.java)

            // 视频分割
            val trimClipEffect = VideoEditingTool(1, "分割", Icons.Rounded.ContentCut) {
                viewModel.setEffectVisibleId(1)
            }

            // 视频合并
            val mergeEffect = VideoEditingTool(2, "合并", Icons.Rounded.Merge) {
                viewModel.setEffectVisibleId(2)
            }

            // 压缩分辨率
            val compressEffect = VideoEditingTool(3, "压缩", Icons.Rounded.Compress) {
                viewModel.setEffectVisibleId(3)
            }

            // 添加水印
            val watermarkEffect = VideoEditingTool(4, "水印", Icons.AutoMirrored.Rounded.BrandingWatermark) {
                viewModel.setEffectVisibleId(4)
            }

            return listOf(
                trimClipEffect,
                mergeEffect,
                compressEffect,
                watermarkEffect
            )
        }

        @Composable
        fun ShowEffectById(effectVisibleId: Int) {
            when(effectVisibleId) {
                1 -> TrimClipEffect()
                2 -> MergeEffect()
                3 -> CompressEffect()
                4 -> WaterMarkEffect()
                else -> throw IllegalArgumentException("effectVisibleId 不存在")
            }
        }
    }
}