package com.lingfenglong.videoeditor

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.effect.SpeedChangeEffect
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.transformer.Composition
import androidx.media3.transformer.DefaultMuxer
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.TransformationRequest
import androidx.media3.transformer.Transformer
import com.lingfenglong.videoeditor.constant.Constants.Companion.APP_TAG
import com.lingfenglong.videoeditor.entity.ExportSettings
import com.lingfenglong.videoeditor.entity.VideoProject
import com.lingfenglong.videoeditor.entity.effect.EffectInfo
import com.lingfenglong.videoeditor.entity.effect.MergeEffectInfo
import com.lingfenglong.videoeditor.entity.effect.NoneEffect
import com.lingfenglong.videoeditor.entity.effect.TrimEffectInfo
import com.lingfenglong.videoeditor.entity.effect.WaterMarkEffectInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class TransformManager(
    videoProject: VideoProject,
) {
    var videoProject: VideoProject by mutableStateOf(videoProject)
    lateinit var exoPlayer: ExoPlayer
    private lateinit var transformer: Transformer

    private val mediaItem: MediaItem = MediaItem.Builder()
        .setUri(videoProject.videoFileUri)
        .build()

    private var trimmedMediaItem: MediaItem? = null

    private val effectInfoList: MutableList<EffectInfo> = mutableStateListOf()
    private val audioProcessor: MutableList<AudioProcessor> = mutableStateListOf()
    private var trimEffectInfo: TrimEffectInfo? = null
    private val mergeEffectInfoList: MutableList<MergeEffectInfo> = mutableListOf(
        MergeEffectInfo(videoProject.videoFileUri.toUri(), MergeEffectInfo.Position.NONE, { NoneEffect })
    )

    private lateinit var exportProcessLogLaunch: Job

    companion object {
        val EMPTY = TransformManager(VideoProject.EMPTY)
    }

    fun export(
        context: Context,
        exportSettings: ExportSettings
    ) {
        Log.i(APP_TAG, "export settings: $exportSettings")

//        // 获取 effects
//        val effects = getEffects().apply {
//            if (exportSettings.speed > 0) {
//                add(SpeedChangeEffect(exportSettings.speed))
//            }
//            if (exportSettings.frameRate > 0) {
//                add(SpeedChangeEffect(exportSettings.frameRate))
//            }
//        }

        val editedMediaItem = EditedMediaItem.Builder(trimmedMediaItem ?: mediaItem)
//            .setEffects(Effects(audioProcessor, effects))
            .setRemoveVideo(exportSettings.exportVideo.not())
            .setRemoveAudio(exportSettings.exportAudio.not())
            .build()

        val outputPath = exportSettings.exportPath + "/" + exportSettings.exportName + ".mp4"

        if (File(outputPath).createNewFile()) {
            Log.e(APP_TAG, "create output file success: $outputPath")
        } else {
            Log.i(APP_TAG, "create output file failed: $outputPath")
        }

        transformer = Transformer.Builder(context)
            .setMuxerFactory(DefaultMuxer.Factory())
            .setVideoMimeType(exportSettings.videoMimeType)
            .setAudioMimeType(exportSettings.audioMimeType)
//            .setVideoMimeType(MimeTypes.VIDEO_H264)
//            .setAudioMimeType(MimeTypes.AUDIO_AAC)
//            .addListener()
            .build()
//            .start()

        val composition: Composition = Composition.Builder(
            EditedMediaItemSequence.Builder(editedMediaItem).build(),
            EditedMediaItemSequence.Builder(
                mergeEffectInfoList
                    .map { MediaItem.Builder().setUri(it.uri).build() }
                    .map { EditedMediaItem.Builder(it).build() }
            ).build()
        ).build()

        Log.i(APP_TAG, "export: ${composition.sequences}")
        transformer.addListener(object : Transformer.Listener {
            override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                super.onCompleted(composition, exportResult)
                Log.i(APP_TAG, "onCompleted: ${exportResult.toJson()}")
                exportProcessLogLaunch.cancel()
            }

            override fun onError(
                composition: Composition,
                exportResult: ExportResult,
                exportException: ExportException,
            ) {
                super.onError(composition, exportResult, exportException)
                Log.e(APP_TAG, "onError: ${exportResult.toJson()}", exportException)
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

//        transformer.start(trimmedMediaItem ?: mediaItem, outputPath)
        transformer.start(composition, outputPath)

        Log.i(APP_TAG, "exporting video: $outputPath")
        exportProcessLogLaunch = CoroutineScope(Dispatchers.Main).launch {
            var currentProcess = this@TransformManager.getProgress()
            while (currentProcess < 1F) {
                Log.i(
                    APP_TAG,
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

    private fun updateVideoEffect() {
        exoPlayer.apply {
            stop()
            setMediaItem(trimmedMediaItem ?: mediaItem)
            setVideoEffects(getEffects())
            prepare()
        }
    }

    fun getProgress(): Float {
        val progressHolder = ProgressHolder()

        return when(transformer.getProgress(progressHolder)) {
            Transformer.PROGRESS_STATE_UNAVAILABLE -> -1F
            Transformer.PROGRESS_STATE_NOT_STARTED -> 1F
            else -> progressHolder.progress / 100F
        }
    }

    fun cancel() {
        transformer.cancel()

        // delete output file
        transformer.javaClass.getDeclaredField("outputFilePath").apply {
            isAccessible = true
            val outputFile = get(transformer) as String
            CoroutineScope(Dispatchers.Main).launch {
                File(outputFile).delete()
            }
        }
    }

    fun getEffectInfoList(): List<EffectInfo> {
        val res = ArrayList<EffectInfo>()
        trimEffectInfo?.let { res += it }
        res += effectInfoList
        return res
    }

    fun addEffectInfo(effectInfo: EffectInfo) {
        when(effectInfo) {
            is TrimEffectInfo -> {
                trimEffectInfo = effectInfo
                trimmedMediaItem = MediaItem.Builder()
                    .setUri(videoProject.videoFileUri)
                    .setClippingConfiguration(
                        MediaItem.ClippingConfiguration.Builder()
                            .setStartPositionMs(effectInfo.start)
                            .setEndPositionMs(effectInfo.end)
                            .build()
                    )
                    .build()
            }

            is MergeEffectInfo -> {
                mergeEffectInfoList += effectInfo
            }

            is WaterMarkEffectInfo -> {
                effectInfoList += effectInfo
            }
        }
        updateVideoEffect()
    }

    fun removeEffectInfo(effectInfo: EffectInfo) {
        when(effectInfo) {
            is TrimEffectInfo -> {
                trimEffectInfo = null
                trimmedMediaItem = null
            }

            is MergeEffectInfo -> {
                if (effectInfo.position == MergeEffectInfo.Position.NONE) {
                    "不能删除该片段".makeToast()
                } else {
                    mergeEffectInfoList.remove(effectInfo)
                }
            }


        }
        updateVideoEffect()
    }
}