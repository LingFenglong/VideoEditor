package com.lingfenglong.videoeditor.entity

import android.media.MediaFormat
import android.os.Environment
import androidx.media3.common.MimeTypes
import java.time.Instant

data class ExportSettings(
    var exportPath: String,
    var exportName: String,
    var lossless: Boolean,
    var videoMimeType: String,
    var audioMimeType: String,
    var exportVideo: Boolean,
    var exportAudio: Boolean,
    var speed: Float,
    var frameRate: Float
) {
    companion object {
        val DEFAULT = ExportSettings(
            exportPath = Environment.getExternalStorageDirectory().absolutePath + "/Movies",
            exportName = Instant.now().nano.toString() + ".mp4",
            lossless = false,
            videoMimeType = MimeTypes.VIDEO_MP4V,
            audioMimeType = MimeTypes.AUDIO_AAC,
//MimeTypes.AUDIO_AAC
//MimeTypes.AUDIO_AMR_NB
//MimeTypes.AUDIO_AMR_WB
            exportVideo = true,
            exportAudio = true,
            speed = 1F,
            frameRate = 30F
        )
    }
}