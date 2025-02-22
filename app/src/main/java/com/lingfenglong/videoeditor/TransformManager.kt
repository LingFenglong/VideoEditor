package com.lingfenglong.videoeditor

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.transformer.Composition
import androidx.media3.transformer.DefaultMuxer
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.TransformationRequest
import androidx.media3.transformer.Transformer
import com.lingfenglong.videoeditor.entity.ExportSettings
import com.lingfenglong.videoeditor.entity.VideoProject
import com.lingfenglong.videoeditor.entity.effect.EffectInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.log

class TransformManager(
    videoProject: VideoProject,
) {
    var videoProject: VideoProject by mutableStateOf(videoProject)
    lateinit var exoPlayer: ExoPlayer

    private lateinit var transformer: Transformer

    private val effectInfoList: MutableList<EffectInfo> = mutableStateListOf()
    private val audioProcessor: MutableList<AudioProcessor> = mutableStateListOf()
    private lateinit var trimmedMedia: MediaItem
    private lateinit var exportProcessLogLaunch: Job

    companion object {
        val EMPTY = TransformManager(VideoProject.EMPTY)
    }

    fun export(
        context: Context,
        exportSettings: ExportSettings
    ) {
//        // 获取 effects
//        val effects = getEffects().apply {
//            if (exportSettings.speed > 0) {
//                add(SpeedChangeEffect(exportSettings.speed))
//            }
//            if (exportSettings.frameRate > 0) {
//                add(SpeedChangeEffect(exportSettings.frameRate))
//            }
//        }
//
        val mediaItem = MediaItem.Builder()
            .setUri(videoProject.videoFileUri)
            .build()
//
//        val editedMediaItem = EditedMediaItem.Builder(mediaItem)
//            .setEffects(Effects(audioProcessor, effects))
//            .setRemoveVideo(exportSettings.exportVideo.not())
//            .setRemoveAudio(exportSettings.exportAudio.not())
//            .build()

        val outputPath = exportSettings.exportPath + "/" + exportSettings.exportName + ".mp4"

        if (File(outputPath).createNewFile()) {
            Log.e("VideoEditor", "create output file success: $outputPath")
        } else {
            Log.i("VideoEditor", "create output file failed: $outputPath")
        }

        transformer = Transformer.Builder(context)
            .setMuxerFactory(DefaultMuxer.Factory())
//            .setVideoMimeType(exportSettings.videoMimeType)
//            .setAudioMimeType(exportSettings.audioMimeType)
            .setVideoMimeType(MimeTypes.VIDEO_H264)
            .setAudioMimeType(MimeTypes.AUDIO_AAC)
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
        transformer.addListener(object : Transformer.Listener {
            override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                super.onCompleted(composition, exportResult)
                Log.i("VideoEditor", "onCompleted: ${exportResult.toJson()}")
                exportProcessLogLaunch.cancel()
            }

            override fun onError(
                composition: Composition,
                exportResult: ExportResult,
                exportException: ExportException,
            ) {
                super.onError(composition, exportResult, exportException)
                Log.e("VideoEditor", "onError: ${exportResult.toJson()}", exportException)
                exportProcessLogLaunch.cancel()
            }

            override fun onFallbackApplied(
                composition: Composition,
                originalTransformationRequest: TransformationRequest,
                fallbackTransformationRequest: TransformationRequest,
            ) {
                super.onFallbackApplied(
                    composition,
                    originalTransformationRequest,
                    fallbackTransformationRequest
                )
            }
        })

        transformer.start(mediaItem, outputPath)

        Log.i("VideoEditor", "exporting video: $outputPath")
        exportProcessLogLaunch = CoroutineScope(Dispatchers.Main).launch {
            var currentProcess = this@TransformManager.getProgress()
            while (currentProcess < 1F) {
                Log.i(
                    "VideoEditor",
                    "export process: ${currentProcess * 100}%, status: ${
                        transformer.getProgress(ProgressHolder())
                    }"
                )
                delay(500)
                currentProcess = getProgress()
            }
        }
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