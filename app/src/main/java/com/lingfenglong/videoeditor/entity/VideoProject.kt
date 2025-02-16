package com.lingfenglong.videoeditor.entity

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.lingfenglong.videoeditor.activity.VideoEditingActivity
import com.lingfenglong.videoeditor.entity.effect.EffectInfo
import com.lingfenglong.videoeditor.toJson
import java.io.Serializable

data class VideoProject(
    val videoFileUri: String,
    val videoFilePath: String,
    val videoInfoPath: String,
    val projectFilePath: String,
    val projectName: String,
    val thumb: String,
    val duration: Long,
    var frames: Long,
    var frameRate: Float,
    var format: String,
    val effectInfoList: MutableList<EffectInfo>
) : Serializable {

    companion object {
        val EMPTY = VideoProject(
            videoFileUri = "",
            videoFilePath = "",
            videoInfoPath = "",
            projectFilePath = "",
            projectName = "",
            thumb = "",
            duration = 0,
            frames = 0L,
            frameRate = 0F,
            format = "",
            effectInfoList = mutableListOf()
        )
    }

    fun startEditing(context: Context) {
        val intent = Intent(context, VideoEditingActivity::class.java)
        intent.action = Intent.ACTION_EDIT
        intent.putExtra("videoProject", this.toJson())
        context.startActivity(intent)
    }
}
