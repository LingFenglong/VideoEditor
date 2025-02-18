package com.lingfenglong.videoeditor

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.SpeedChangeEffect
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import com.lingfenglong.videoeditor.entity.effect.EffectInfo
import com.lingfenglong.videoeditor.entity.ExportSettings
import com.lingfenglong.videoeditor.entity.VideoProject

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
//            .setMuxerFactory() TODO
//            .addListener()
            .build()
//            .start()

//        val composition = Composition.Builder()
//            .setHdrMode()
//            .setEffects()
//            .build()

        val progressRunnable = object : Runnable {
            override fun run() {
                val progressState = transformer.getProgress(progressHolder)

                when (progressState) {
                    Transformer.PROGRESS_STATE_NOT_STARTED -> {

                    }
                    Transformer.PROGRESS_STATE_WAITING_FOR_AVAILABILITY -> {
                    }
                    Transformer.PROGRESS_STATE_AVAILABLE -> {
                        currentProgress = progressHolder.progress.toFloat()
                    }
                    Transformer.PROGRESS_STATE_UNAVAILABLE -> {
                    }
                }

                // 如果转换尚未完成，继续查询进度
                if (progressState != Transformer.PROGRESS_STATE_NOT_STARTED) {
                    handler.postDelayed(this, 500) // 每 500ms 轮询一次
                }
            }
        }
        transformer.getProgress(progressHolder)
        transformer.addListener(object : Transformer.Listener {
            override fun onCompleted(
                composition: Composition,
                exportResult: ExportResult,
            ) {
                super.onCompleted(composition, exportResult)
                handler.removeCallbacks(progressRunnable)
            }

            override fun onError(
                composition: Composition,
                exportResult: ExportResult,
                exportException: ExportException,
            ) {
                super.onError(
                    composition,
                    exportResult,
                    exportException
                )
                handler.removeCallbacks(progressRunnable)
            }
        })

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