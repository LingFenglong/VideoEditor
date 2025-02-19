package com.lingfenglong.videoeditor.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lingfenglong.videoeditor.TransformManager
import com.lingfenglong.videoeditor.Util
import com.lingfenglong.videoeditor.constant.Constants
import com.lingfenglong.videoeditor.entity.VideoInfo
import com.lingfenglong.videoeditor.entity.VideoProject
import com.lingfenglong.videoeditor.entity.effect.EffectInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class VideoEditorViewModel(application: Application) : AndroidViewModel(application) {
    private val _videoProjectList = MutableStateFlow<MutableList<VideoProject>>(mutableListOf())
    val videoProjectList = _videoProjectList.asStateFlow()

    private val _magicToolButtonVisible = MutableStateFlow(true)
    val magicToolButtonVisible = _magicToolButtonVisible.asStateFlow()

    private val _controlsVisible = MutableStateFlow(false)
    val controlsVisible: StateFlow<Boolean> = _controlsVisible.asStateFlow()

    private val _transformerManager = MutableStateFlow(TransformManager.EMPTY)
    val transformerManager: StateFlow<TransformManager> = _transformerManager.asStateFlow()

    private val _currentProject = MutableStateFlow(VideoProject.EMPTY)
    val currentProject: StateFlow<VideoProject> = _currentProject.asStateFlow()

    private val _isVideoPlaying = MutableStateFlow(true)
    val isVideoPlaying: StateFlow<Boolean> = _isVideoPlaying.asStateFlow()

    private val _videoCurrentPosition = MutableStateFlow(-1L)
    val videoCurrentPosition: StateFlow<Long> = _videoCurrentPosition.asStateFlow()

    private val _currentVideoInfo = MutableStateFlow(VideoInfo.EMPTY)
    val currentVideoInfo: StateFlow<VideoInfo> = _currentVideoInfo.asStateFlow()

    /**
     * 对应 id 的 effect 是否可见
     * 0 -> effect 不可见
     */
    private val _effectVisibleId = MutableStateFlow(0)
    val effectVisibleId = _effectVisibleId.asStateFlow()

    fun setMagicToolButtonVisible(value: Boolean) {
        _magicToolButtonVisible.update { value }
    }

    fun setControlsVisible(value: Boolean) {
        _controlsVisible.update { value }
        // TODO 自动关闭
    }

    fun addVideoProject(videoProject: VideoProject) {
        _videoProjectList.update {
            it.add(videoProject)
            it
        }
    }

    fun removeVideoProject(videoProject: VideoProject) {
        _videoProjectList.update {
            it.remove(videoProject)
            viewModelScope.launch {
                File(videoProject.projectFilePath).deleteRecursively()
            }
            it
        }
    }

    fun clearVideoProjectList(videoProject: VideoProject) {
        _videoProjectList.update {
            ArrayList()
        }
    }

    fun updateVideoProjectList(context: Context) {
        _videoProjectList.update {
            val projectList = File("${context.dataDir}/${Constants.PROJECTS_BASE_DIR}")
                .listFiles()
                ?.map { dir ->
                    Util.gson.fromJson(
                        File(dir, Constants.PROJECT_INFO).inputStream().reader(),
                        VideoProject::class.java
                    )
                }

            if (projectList == null) {
                it.clear()
                it
            } else {
                projectList as ArrayList<VideoProject>
            }
        }

        Log.i("project list", "updateVideoProjectList: ${_videoProjectList.value}")
    }

    fun setVideoPlaying(isPLaying: Boolean) {
        _isVideoPlaying.update { isPLaying }
    }

    fun setEffectVisibleId(id: Int) {
        _effectVisibleId.update { id }
    }

    fun updateCurrentVideoInfo(videoInfo: VideoInfo) {
        _currentVideoInfo.update { videoInfo }
    }

    fun updateCurrentVideoProject(videoProject: VideoProject) {
        _currentProject.update {
            videoProject
        }
    }

    fun addEffectInfo(effectInfo: EffectInfo) {
        _currentProject.update {
            it.effectInfoList.add(effectInfo)
            Log.i("EffectInfo", "current video project effectInfo: ${it.effectInfoList}")
            it
        }
    }

    fun updateTransformerManager(transformManager: TransformManager) {
        _transformerManager.update { transformManager }
    }
}