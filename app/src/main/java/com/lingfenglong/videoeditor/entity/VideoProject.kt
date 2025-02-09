package com.lingfenglong.videoeditor.entity

import com.arthenica.ffmpegkit.MediaInformation
import org.json.JSONObject
import java.io.Serializable

data class VideoProject(
    val videoFileUri: String,
    val videoFilePath: String,
    val videoInfoPath: String,
    val projectFilePath: String,
    val projectName: String,
    val thumb: String,
    val duration: Long,
    val mediaInformation: MediaInformation?
) : Serializable {

    companion object {
        val EMPTY = VideoProject("", "", "", "", "", "", 0, null)
    }
}
