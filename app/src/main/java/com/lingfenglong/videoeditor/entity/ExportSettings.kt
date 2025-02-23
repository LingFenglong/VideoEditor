package com.lingfenglong.videoeditor.entity

import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.MimeTypes
import java.time.Instant

class ExportSettings(
    exportPath: String,
    exportName: String,
    lossless: Boolean,
    videoMimeType: String,
    audioMimeType: String,
    exportVideo: Boolean,
    exportAudio: Boolean,
    speed: Float,
    frameRate: Float
) {
    var exportPath: String by mutableStateOf(exportPath)
    var exportName: String by mutableStateOf(exportName)
    var lossless: Boolean by mutableStateOf(lossless)
    var videoMimeType: String by mutableStateOf(videoMimeType)
    var audioMimeType: String by mutableStateOf(audioMimeType)
    var exportVideo: Boolean by mutableStateOf(exportVideo)
    var exportAudio: Boolean by mutableStateOf(exportAudio)
    var speed: Float by mutableFloatStateOf(speed)
    var frameRate: Float by mutableFloatStateOf(frameRate)

    companion object {
        val DEFAULT = ExportSettings(
            exportPath = Environment.getExternalStorageDirectory().absolutePath + "/Movies",
            exportName = Instant.now().nano.toString() + ".mp4",
            lossless = false,
            videoMimeType = MimeTypes.VIDEO_MP4V,
            audioMimeType = MimeTypes.AUDIO_AAC,
            exportVideo = true,
            exportAudio = true,
            speed = 1F,
            frameRate = 30F
        )
    }

    override fun toString(): String {
        return "ExportSettings(exportPath='$exportPath', exportName='$exportName', lossless=$lossless, videoMimeType='$videoMimeType', audioMimeType='$audioMimeType', exportVideo=$exportVideo, exportAudio=$exportAudio, speed=$speed, frameRate=$frameRate)"
    }
}