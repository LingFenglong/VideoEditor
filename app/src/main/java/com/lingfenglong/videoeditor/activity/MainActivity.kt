package com.lingfenglong.videoeditor.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.FileUtils
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arthenica.ffmpegkit.FFprobeKit
import com.lingfenglong.videoeditor.R
import com.lingfenglong.videoeditor.Util
import com.lingfenglong.videoeditor.constant.Constants
import com.lingfenglong.videoeditor.entity.VideoProject
import com.lingfenglong.videoeditor.getFileNameAndExtFromUri
import com.lingfenglong.videoeditor.timeFormat
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

        val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it) {
                    recreate()
                } else {
                    val text = "permission_denied_grant_video_permissions"
                    val duration = Toast.LENGTH_SHORT
                    val toast = Toast.makeText(this, text, duration)
                    toast.show()
                }
            }
        if (checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
            requestPermission.launch(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        val vm = VideoEditorViewModel(application)
        vm.updateVideoProjectList(this)

        val pickMedia: ActivityResultLauncher<PickVisualMediaRequest> =
            registerForActivityResult(
                ActivityResultContracts.PickVisualMedia()
            ) { uri ->
                if (uri != null) {
                    val intent = Intent(this, VideoEditingActivity::class.java)
                    intent.action = Intent.ACTION_EDIT

                    val videoProject = createNewProject(this, uri)
                    vm.addVideoProject(videoProject)
                    vm.updateVideoProjectList(this)
                    videoProject.startEditing(this)
                } else {

                }
            }


        setContent {
            MaterialTheme {
                val viewModel = viewModel { vm }
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                val projectList by viewModel.videoProjectList.collectAsState()
                Log.i("project list", "updateVideoProjectList: $projectList")

                AppNavigationDrawer(drawerState, pickMedia) {
                    Column {
                        AppTopBar(drawerState, scope)
                        LazyColumn {
                            items(projectList, { it.videoFileUri }) {
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

                // create project dir
                val projectDir = File(projectsBaseDir, originalVideoFileName)
                projectDir.mkdirs()

                // copy origin video file
                val targetVideoFile = File(projectDir, originalVideoFileNameAndExt)
                FileUtils.copy(originalVideoFileInputStream, targetVideoFile.outputStream())
                uri = targetVideoFile.toUri()

                // create project info file
                // get video information(frames, duration etc) by ffmpeg
                val projectInfo = File(projectDir, Constants.PROJECT_INFO)
                val mediaInformation = FFprobeKit.getMediaInformation(uri.toString())
                    .mediaInformation


                // create videoProject object
                videoProject = VideoProject(
                    videoFileUri = uri.toString(),
                    videoFilePath = "${dataDir.absolutePath}/${originalVideoFileNameAndExt}",
                    videoInfoPath = "${dataDir.absolutePath}/${Constants.PROJECT_INFO}",
                    projectFilePath = projectDir.absolutePath,
                    projectName = originalVideoFileName,
                    thumb = "${projectDir.absolutePath}/${Constants.PROJECT_THUMB}",
                    duration = (mediaInformation.duration.toDouble() * 1000).roundToLong(),
                    frames = mediaInformation.streams[0].getStringProperty("nb_frames").toLong(),
                    frameRate = mediaInformation.streams[0].realFrameRate.substringBeforeLast("/").toFloat(),
                    format = mediaInformation.format,
                    effectInfoList = mutableListOf()
                )
                // save project info project.info
                projectInfo.outputStream().use { fos ->
                    fos.write(Util.gson.toJson(videoProject).toByteArray())
                    fos.flush()
                }

                // create thumb file
                File("${projectDir.absolutePath}/${Constants.PROJECT_THUMB}").apply {
                    createNewFile()
                    val mediaMetadataRetriever = MediaMetadataRetriever()
                    mediaMetadataRetriever.setDataSource(context, uri)
                    mediaMetadataRetriever
                        .getFrameAtIndex(0)?.compress(Bitmap.CompressFormat.PNG, 100, this.outputStream())
                }
            }
        return videoProject!!
    }

    @Composable
    fun AppTopBar(drawerState: DrawerState, scope: CoroutineScope) {
        CenterAlignedTopAppBar(
            title = {
                Text(text = "视频编辑器")
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview
fun VideoProjectItem(
    @PreviewParameter(provider = VideoProjectPreviewParameterProvider::class) videoProject: VideoProject
) {
    val context = LocalContext.current
    var videoProjectDialogVisible by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .combinedClickable(
                onClick = {
                    val intent = Intent(context, VideoEditingActivity::class.java)
                    intent.action = Intent.ACTION_EDIT
                    intent.putExtra("videoProject", videoProject.toJson())
                    context.startActivity(intent)
                }
            ),
        shape = RoundedCornerShape(8),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier
                        .size(80.dp, 80.dp)
                ) {
                    Image(
                        modifier = Modifier.padding(4.dp),
                        contentScale = ContentScale.Crop,
                        bitmap = BitmapFactory.decodeFile(videoProject.thumb).asImageBitmap(),
                        contentDescription = "thumb"
                    )
                }

                Spacer(modifier = Modifier.padding(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.SpaceBetween
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
                        text = videoProject.duration.timeFormat()
                    )
                }
            }

            IconButton(
                modifier = Modifier
                    .wrapContentSize()
                    .align(alignment = Alignment.TopEnd),
                onClick = { videoProjectDialogVisible = true }
            ) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "more operation",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(40.dp, 40.dp)
                )
            }
        }
    }

    if (videoProjectDialogVisible) {
        VideoProjectDetailDialog(
            videoProject = videoProject,
            onDismissRequest = { videoProjectDialogVisible = false }
        )
    }
}

@Composable
fun VideoProjectDetailDialog(
    videoProject: VideoProject,
    onDismissRequest: () -> Unit
) {
    Log.i("project detail", "current project detail: $videoProject")

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var deleteDialogVisibility by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(modifier = Modifier) {
            Text(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .fillMaxWidth(),
                text = "详细信息",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "项目名称")
                Text(text = videoProject.projectName)
            }
            Spacer(modifier = Modifier.padding(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "视频编码")
                Text(text = videoProject.format)
            }
            Spacer(modifier = Modifier.padding(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "时长")
                Text(text = videoProject.duration.timeFormat())
            }
            Spacer(modifier = Modifier.padding(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "帧数")
                Text(text = videoProject.frames.toString())
            }
            Spacer(modifier = Modifier.padding(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                FilledTonalButton(
                    onClick = { deleteDialogVisibility = true },
                ) {
                    Text(text = "删除")
                }

                FilledTonalButton(
                    onClick = { videoProject.startEditing(context); onDismissRequest() }
                ) {
                    Text(text = "编辑")
                }
            }
        }
    }

    if (deleteDialogVisibility) {
        val viewModel = viewModel(modelClass = VideoEditorViewModel::class.java)

        AlertDialog(
            onDismissRequest = { deleteDialogVisibility = false },
            title = { Text(text = "提示") },
            text = { Text(text = "确定删除？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeVideoProject(videoProject)
                    onDismissRequest()
                    deleteDialogVisibility = false
                }) {
                    Text(text = "确定")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    deleteDialogVisibility = false
                }) {
                    Text(text = "取消")
                }
            }
        )
    }
}

class VideoProjectPreviewParameterProvider : PreviewParameterProvider<VideoProject> {
    override val values: Sequence<VideoProject>
        get() = sequenceOf(
            VideoProject(
                videoFileUri = "file:///data/user/0/com.lingfenglong.videoeditor/projects/1000000033/1000000033.mp4",
                videoFilePath = "/data/user/0/com.lingfenglong.videoeditor/1000000033.mp4",
                videoInfoPath = "/data/user/0/com.lingfenglong.videoeditor/project.info",
                projectFilePath = "/data/user/0/com.lingfenglong.videoeditor/projects/1000000033",
                projectName = "1000000033",
                thumb = "/data/user/0/com.lingfenglong.videoeditor/projects/1000000033/thumb.png",
                duration = 20615,
                frames = 30L,
                frameRate = 30_000F,
                format = "MP4",
                effectInfoList = mutableListOf()
            )
        )
}