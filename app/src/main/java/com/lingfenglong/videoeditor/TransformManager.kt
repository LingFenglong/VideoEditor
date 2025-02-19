package com.lingfenglong.videoeditor

import android.content.Context
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.effect.SpeedChangeEffect
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.transformer.DefaultMuxer
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import com.lingfenglong.videoeditor.entity.ExportSettings
import com.lingfenglong.videoeditor.entity.VideoProject
import com.lingfenglong.videoeditor.entity.effect.EffectInfo

class TransformManager(
    private val videoProject: VideoProject,
) {
    lateinit var exoPlayer: ExoPlayer
    lateinit var transformer: Transformer
    private val effectInfoList: MutableList<EffectInfo> = ArrayList()
    private val audioProcessor: MutableList<AudioProcessor> = ArrayList()
    private lateinit var trimmedMedia: MediaItem

    companion object {
        val EMPTY = TransformManager(VideoProject.EMPTY)
    }

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

        val mediaItem = MediaItem.Builder()
            .setUri(videoProject.videoFileUri)
            .build()

        val editedMediaItem = EditedMediaItem.Builder(mediaItem)
            .setEffects(Effects(audioProcessor, effects))
            .setRemoveVideo(exportSettings.exportVideo.not())
            .setRemoveAudio(exportSettings.exportAudio.not())
            .build()

        transformer = Transformer.Builder(context)
            .setVideoMimeType(exportSettings.videoMimeType)
            .setAudioMimeType(exportSettings.audioMimeType)
            .setMuxerFactory(DefaultMuxer.Factory())
//            .addListener()
            .build()
//            .start()

//        val composition = Composition.Builder()
//            .setHdrMode()
//            .setEffects()
//            .build()

//        context.contentResolver.openFileDescriptor((exportSettings.exportPath + "/" + exportSettings.exportName).toUri(), "rw")?.use {
//            transformer.start
//        }
            transformer.start(editedMediaItem, exportSettings.exportPath + "/" + exportSettings.exportName)
    }

    private fun getEffects() = effectInfoList.map { it.effect() }.toMutableList()

    fun addEffectInfo(effectInfo: EffectInfo) {
        effectInfoList.add(effectInfo)
        updateVideoEffect()
    }

    private fun updateVideoEffect() {
        exoPlayer.apply {
            stop()
            setVideoEffects(getEffects())
            prepare()
        }
    }

    fun getProgress(): Float {
        val progressHolder = ProgressHolder()

        return when(transformer.getProgress(progressHolder)) {
            Transformer.PROGRESS_STATE_UNAVAILABLE -> -1F
            Transformer.PROGRESS_STATE_NOT_STARTED -> 0F
            else -> progressHolder.progress / 100F
        }
    }
}