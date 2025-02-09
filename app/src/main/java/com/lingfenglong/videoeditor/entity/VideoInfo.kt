package com.lingfenglong.videoeditor.entity

import android.net.Uri

/**
 * 记录当前正在编辑的视频信息
 */
class VideoInfo(
    // Uri 地址
    var uri: Uri,

    // 每毫秒的帧数
    var framePerMillisecond: Float,

    // 时长
    var duration: Long,

    // 总帧数
    var frames: Long,

    var project: VideoProject
) {
    companion object {
        val EMPTY = VideoInfo(Uri.EMPTY, 0F, 0, 0, VideoProject.EMPTY)
    }
}