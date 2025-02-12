package com.lingfenglong.videoeditor.entity

import android.media.MediaFormat
import android.os.Environment
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
            videoMimeType = MediaFormat.MIMETYPE_VIDEO_MPEG4,
            audioMimeType = MediaFormat.MIMETYPE_AUDIO_MPEGH_LC_L3,
            exportVideo = true,
            exportAudio = true,
            speed = 1F,
            frameRate = 30F
        )
    }
}