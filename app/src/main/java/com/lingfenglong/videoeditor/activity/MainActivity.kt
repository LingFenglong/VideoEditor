package com.lingfenglong.videoeditor.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.FileUtils
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arthenica.ffmpegkit.FFprobeKit
import com.lingfenglong.videoeditor.R
import com.lingfenglong.videoeditor.Util
import com.lingfenglong.videoeditor.constant.Constants
import com.lingfenglong.videoeditor.entity.VideoProject
import com.lingfenglong.videoeditor.getFileNameAndExtFromUri
import com.lingfenglong.videoeditor.toJson
import com.lingfenglong.videoeditor.viewmodel.VideoEditorViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import kotlin.math.roundToLong

class MainActivity : AppCompatActivity() {
    private val TAG = javaClass.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = VideoEditorViewModel()
        viewModel.updateVideoProjectList(this)

        val pickMedia: ActivityResultLauncher<PickVisualMediaRequest> =
            registerForActivityResult(
                ActivityResultContracts.PickVisualMedia()
            ) { uri ->
                if (uri != null) {
                    val intent = Intent(this, VideoEditingActivity::class.java)
                    intent.action = Intent.ACTION_EDIT

                    val videoProject = createNewProject(this, uri)
                    intent.putExtra("videoProject", videoProject.toJson())

                    viewModel.updateVideoProjectList(this)
                    startActivity(intent)
                } else {

                }
            }


        setContent {
            MaterialTheme {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val vm = viewModel { viewModel }
                val projectList by vm.videoProjectList.collectAsState()

                AppNavigationDrawer(drawerState, pickMedia) {
                    Column {
                        AppTopBar(drawerState, scope)
                        LazyColumn {
                            items(projectList) {
                                VideoProjectItem(it)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 使用指定的视频Uri创建项目
     */
    private fun createNewProject(context: Context, videoFileUri: Uri): VideoProject {
        val projectsBaseDir = File("${context.dataDir.absolutePath}/${Constants.PROJECTS_BASE_DIR}")
        val contentResolver = context.contentResolver
        var uri: Uri

        if (!projectsBaseDir.exists() && !projectsBaseDir.mkdirs()) {
            Log.i(TAG, "createNewProject: 创建项目文件失败！")
        }

        var videoProject: VideoProject?

        contentResolver
            .openFile(videoFileUri, "r", null)
            .use {

                val originalVideoFileInputStream = FileInputStream(it!!.fileDescriptor)
                val originalVideoFileNameAndExt = getFileNameAndExtFromUri(context, videoFileUri)
                val originalVideoFileName = originalVideoFileNameAndExt.substringBeforeLast(".")
                val originalVideoFileExt = originalVideoFileNameAndExt.substringAfterLast(".")

                val projectDir = File(projectsBaseDir, originalVideoFileName)
                projectDir.mkdirs()

                val targetVideoFile = File(projectDir, originalVideoFileNameAndExt)
                FileUtils.copy(originalVideoFileInputStream, targetVideoFile.outputStream())
                uri = targetVideoFile.toUri()

                val projectInfo = File(projectDir, Constants.PROJECT_INFO)
                val mediaInformation = FFprobeKit.getMediaInformation(uri.toString())
                    .mediaInformation

                videoProject = VideoProject(
                    uri.toString(),
                    "${dataDir.absolutePath}/${originalVideoFileNameAndExt}",
                    "${dataDir.absolutePath}/${Constants.PROJECT_INFO}",
                    projectDir.absolutePath,
                    originalVideoFileName,
                    "${projectDir.absolutePath}/${Constants.PROJECT_THUMB}",
                    (mediaInformation.duration.toDouble() * 1000).roundToLong(),
                    mediaInformation
                )

                projectInfo.outputStream().use { fos ->
                    fos.write(Util.gson.toJson(videoProject).toByteArray())
                    fos.flush()
                }
            }
        return videoProject!!
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    fun AppTopBar(drawerState: DrawerState, scope: CoroutineScope) {
        CenterAlignedTopAppBar(
            title = {
                Text(text = "Video Editor")
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        scope.launch {
                            drawerState.apply {
                                if (isClosed) {
                                    open()
                                } else {
                                    close()
                                }
                            }
                        }
                    }
                ) {
                    Icon(
                        painter = rememberVectorPainter(image = Icons.Default.Menu),
                        contentDescription = stringResource(id = R.string.app_name)
                    )
                }
            },
            actions = {

            },
        )
    }


    @Composable
    fun AppNavigationDrawer(
        drawerState: DrawerState,
        pickMedia: ActivityResultLauncher<PickVisualMediaRequest>,
        content: @Composable () -> Unit
    ) {
        val context = LocalContext.current
        ModalNavigationDrawer(
            drawerContent = {
                ModalDrawerSheet {
                    Text("Video Editor", modifier = Modifier.padding(16.dp))
                    HorizontalDivider()
                    NavigationDrawerItem(
                        label = { Text(text = "New project from video") },
                        selected = false,
                        onClick = {
                            pickMedia.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.VideoOnly
                                )
                            )
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text(text = "Open project") },
                        selected = false,
                        onClick = {

                        }
                    )
                    NavigationDrawerItem(
                        label = { Text(text = "???") },
                        selected = false,
                        onClick = {

                        }
                    )
                }
            },
            drawerState = drawerState,
            gesturesEnabled = true
        ) {
            content.invoke()
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun VideoProjectItem(videoProject: VideoProject) {
        var deleteDialogVisibility by remember { mutableStateOf(false) }
        val context = LocalContext.current
        val viewModel = viewModel(modelClass = VideoEditorViewModel::class.java)

        val projectInfo by viewModel.currentVideoInfo.collectAsState()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .background(color = Color.LightGray, shape = RoundedCornerShape(8))
                .combinedClickable(
                    onLongClick = {
                        deleteDialogVisibility = true
                    },
                    onClick = {
                        val intent = Intent(context, VideoEditingActivity::class.java)
                        intent.action = Intent.ACTION_EDIT
                        intent.putExtra("videoProject", videoProject.toJson())
                        context.startActivity(intent)
                    }
                ),
        ) {
            Image(
                modifier = Modifier
                    .padding(2.dp)
                    .fillMaxHeight(),
                contentScale = ContentScale.FillHeight,
                painter = rememberVectorPainter(image = Icons.Default.VideoFile),
                contentDescription = "video file"
            )
            Column(
                modifier = Modifier
                    .padding(start = 8.dp)
            ) {
                Text(
                    modifier = Modifier.padding(top = 6.dp),
                    color = Color.Black,
                    fontSize = TextUnit(14f, TextUnitType.Sp),
                    text = videoProject.projectName
                )
                Text(
                    modifier = Modifier.padding(top = 2.dp),
                    color = Color.Gray,
                    fontSize = TextUnit(12f, TextUnitType.Sp),
                    text = videoProject.duration.toString()
                )
            }
        }

        if (deleteDialogVisibility) {
            AlertDialog(
                onDismissRequest = { deleteDialogVisibility = false },
                title = { Text(text = "提示") },
                text = { Text(text = "确定删除？") },
                confirmButton = {
                    Button(onClick = {
                        // 执行确认操作
                        deleteDialogVisibility = false
                        File(projectInfo.project.projectFilePath).deleteRecursively()
                    }) {
                        Text(text = "确定")
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        // 执行取消操作
                        deleteDialogVisibility = false
                    }) {
                        Text(text = "取消")
                    }
                }
            )
        }
    }

}