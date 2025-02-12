package com.lingfenglong.videoeditor

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.SpeedChangeEffect
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.Transformer
import com.lingfenglong.videoeditor.entity.EffectInfo
import com.lingfenglong.videoeditor.entity.ExportSettings
import com.lingfenglong.videoeditor.entity.VideoProject

@UnstableApi
class TransformManager {
    private val projectInfo: VideoProject? = null

    private var exoPlayer: ExoPlayer? = null
    private lateinit var transformer: Transformer
    private val effectInfoList: MutableList<EffectInfo> = ArrayList()
    private val audioProcessor: MutableList<AudioProcessor> = ArrayList()
    private lateinit var trimmedMedia: MediaItem

    fun export(
        context: Context,
        exportSettings: ExportSettings
    ) {
        // 获取 effects
        val effects = getEffects().apply {
            if (exportSettings.speed > 0) {
                add(SpeedChangeEffect(exportSettings.speed))
            }
            if (exportSettings.frameRate > 0) {
                add(SpeedChangeEffect(exportSettings.frameRate))
            }
        }

        val editedMediaItem = EditedMediaItem.Builder(trimmedMedia)
            .setEffects(Effects(audioProcessor, effects))
            .setRemoveVideo(exportSettings.exportVideo.not())
            .setRemoveAudio(exportSettings.exportAudio.not())
            .build()

        transformer = Transformer.Builder(context)
            .setVideoMimeType(exportSettings.videoMimeType)
            .setAudioMimeType(exportSettings.audioMimeType)
//            .setMuxerFactory() TODO
//            .addListener()
            .build()
//            .start()

//        val composition = Composition.Builder()
//            .setHdrMode()
//            .setEffects()
//            .build()

        transformer.start(editedMediaItem, exportSettings.exportPath)
    }

    private fun getEffects() = effectInfoList.map { it.effect() }.toMutableList()

    fun addEffectInfo(effectInfo: EffectInfo) {
        effectInfoList.add(effectInfo)
        updateVideoEffect()
    }

    private fun updateVideoEffect() {
        exoPlayer?.apply {
            stop()
            setVideoEffects(getEffects())
            prepare()
        }
    }

}