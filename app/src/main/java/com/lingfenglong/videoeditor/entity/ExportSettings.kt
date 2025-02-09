package com.lingfenglong.videoeditor.entity

import android.media.MediaFormat
import android.os.Environment
import java.time.Instant

data class ExportSettings(
    var exportPath: String,
    var exportName: String,
    var lossless: Boolean,
    var exportFormat: String
) {
    companion object {
        val DEFAULT = ExportSettings(
            exportPath = Environment.getExternalStorageDirectory().absolutePath + "/Movies",
            exportName = Instant.now().nano.toString() + ".mp4",
            lossless = false,
            exportFormat = MediaFormat.MIMETYPE_VIDEO_MPEG4
        )
    }
}