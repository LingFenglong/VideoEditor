package com.lingfenglong.videoeditor.activity

import android.annotation.SuppressLint
import android.media.MediaFormat
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ReadMore
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.arthenica.ffmpegkit.FFmpegKit
import com.lingfenglong.videoeditor.constant.VideoEditingTools
import com.lingfenglong.videoeditor.entity.ExportSettings
import com.lingfenglong.videoeditor.entity.VideoEditingTool
import com.lingfenglong.videoeditor.entity.VideoInfo
import com.lingfenglong.videoeditor.entity.VideoProject
import com.lingfenglong.videoeditor.toObject
import com.lingfenglong.videoeditor.viewmodel.VideoEditorViewModel
import java.util.concurrent.TimeUnit

class VideoEditingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val project = intent.getStringExtra("videoProject")!!.toObject(VideoProject::class.java)
        val uri = project.videoFileUri

        setContent {
            MaterialTheme {
                VideoEditingPage(project)
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun VideoEditingPage(videoProject: VideoProject) {
    Scaffold(
        topBar = {
            AppVideoEditingTopBar()
        },
        content = {
            VideoPlayer(videoProject, it)
            AppToolFloatingButton()
        },
        bottomBar = {

        }
    )
}

@Composable
fun AppVideoEditingTopBar() {
    var exportDialogVisible by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            colors = IconButtonDefaults.iconButtonColors(),
            onClick = {

            }
        ) {
            Icon(
                tint = Color.Black,
                painter = rememberVectorPainter(image = Icons.AutoMirrored.Filled.ArrowBack),
                contentDescription = "后退"
            )
        }

        IconButton(
            colors = IconButtonDefaults.iconButtonColors(),
            onClick = {

            }
        ) {
            Icon(
                tint = Color.Black,
                painter = rememberVectorPainter(image = Icons.AutoMirrored.Filled.ArrowForward),
                contentDescription = "前进"
            )
        }

        IconButton(
            colors = IconButtonDefaults.iconButtonColors(),
            onClick = {

            }
        ) {
            Icon(
                tint = Color.Black,
                painter = rememberVectorPainter(image = Icons.AutoMirrored.Filled.ReadMore),
                contentDescription = "图层"
            )
        }

        IconButton(
            colors = IconButtonDefaults.iconButtonColors(),
            onClick = { exportDialogVisible = true }
        ) {
            Icon(
                tint = Color.Black,
                painter = rememberVectorPainter(image = Icons.Default.Save),
                contentDescription = "保存"
            )
        }

        if (exportDialogVisible) {
            ExportDialog(
                onDismissRequest = { exportDialogVisible = false }
            )
        }
    }
}

class OnDismissRequestParameterProvider : PreviewParameterProvider<() -> Unit> {
    override val values: Sequence<() -> Unit>
        get() = sequenceOf({})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun ExportDialog(
    @PreviewParameter(OnDismissRequestParameterProvider::class) onDismissRequest: () -> Unit
) {
    var dropdownMenuExpand by remember { mutableStateOf(false) }
    val exportSettings by remember { mutableStateOf(ExportSettings.DEFAULT) }
    var exportName by remember { mutableStateOf(exportSettings.exportName) }
    var exportFormat by remember { mutableStateOf(exportSettings.exportFormat) }

    // TODO: set the export name
//    exportSettings.exportName = project.name

    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier
    ) {
        Card(modifier = Modifier, shape = CardDefaults.shape) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    modifier = Modifier.padding(bottom = 16.dp),
                    text = "导出",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineSmall
                )

                Row(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Text(
                        modifier = Modifier.wrapContentSize(),
                        text = "导出名字",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    TextField(value = exportName, onValueChange = { exportName = it })
                }

                Row(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        modifier = Modifier.wrapContentSize(),
                        text = "导出位置",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    TextField(
                        value = exportSettings.exportPath,
                        onValueChange = { exportSettings.exportPath = it },
                        maxLines = 1
                    )
                }

                Row(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        modifier = Modifier.wrapContentSize(),
                        text = "导出格式",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium
                    )
                    ExposedDropdownMenuBox(
                        expanded = dropdownMenuExpand,
                        onExpandedChange = { dropdownMenuExpand = dropdownMenuExpand.not() }
                    ) {
//                        TextField(
//                            modifire = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true),
//                            value = exportFormat,
//                            onValueChange = { },
//                        )

                        ExposedDropdownMenu(
                            expanded = dropdownMenuExpand,
                            onDismissRequest = { dropdownMenuExpand = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(text = "mp4") },
                                onClick = {
                                    exportSettings.exportFormat = MediaFormat.MIMETYPE_VIDEO_MPEG4
                                    // TODO: set the export name
                                    //    exportSettings.exportName = project.name
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(text = "hevc") },
                                onClick = {
                                    exportSettings.exportFormat = MediaFormat.MIMETYPE_VIDEO_HEVC
                                    // TODO: set the export name
                                    //    exportSettings.exportName = project.name
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(text = "avc") },
                                onClick = {
                                    exportSettings.exportFormat = MediaFormat.MIMETYPE_VIDEO_AVC
                                    // TODO: set the export name
                                    //    exportSettings.exportName = project.name
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(text = "avc1") },
                                onClick = {
                                    exportSettings.exportFormat = MediaFormat.MIMETYPE_VIDEO_AV1
                                    // TODO: set the export name
                                    //    exportSettings.exportName = project.name
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        modifier = Modifier.wrapContentSize(),
                        text = "无损导出",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Switch(checked = exportSettings.lossless, onCheckedChange = { exportSettings.lossless = exportSettings.lossless.not() })
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    TextButton(
                        onClick = { onDismissRequest() },
                    ) {
                        Text("取消")
                    }
                    TextButton(
                        onClick = { TODO() }
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppToolFloatingButton() {
    val viewModel = viewModel(modelClass = VideoEditorViewModel::class.java)
    val effectVisibleId by viewModel.effectVisibleId.collectAsState()
    val showFloatActionButton by viewModel.magicToolButtonVisible.collectAsState()

    var showBottomSheet by remember { mutableStateOf(false) }

    // 按钮
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 160.dp, end = 16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        AnimatedVisibility(visible = showFloatActionButton, enter = fadeIn(), exit = fadeOut()) {
            FloatingActionButton(
                onClick = {
                    showBottomSheet = true
                    viewModel.setMagicToolButtonVisible(false)
                }
            ) {
                Icon(Icons.Filled.Add, "Floating action button")
            }
        }
    }

    // 底部工具栏
    val videoEditingTools: List<VideoEditingTool> = VideoEditingTools.getAllTools()
    val bottomSheetState = rememberModalBottomSheetState()
    if (showBottomSheet) {
        ModalBottomSheet(
            modifier = Modifier
                .fillMaxWidth(),
            onDismissRequest = {
                showBottomSheet = false
                viewModel.setMagicToolButtonVisible(true)
            },
            sheetState = bottomSheetState,
        ) {
            LazyRow {
                items(videoEditingTools) {
                    AppVideoEditingTool(it)
                }
            }
        }
    }


    if (effectVisibleId != 0) {
        viewModel.setMagicToolButtonVisible(false)
        showBottomSheet = false
        VideoEditingTools.ShowEffectById(effectVisibleId)
    }
}

/**
 * 编辑工具列表
 */
@Composable
fun AppVideoEditingTool(videoEditingTool: VideoEditingTool) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .height(120.dp)
            .padding(horizontal = 6.dp, vertical = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        onClick = { videoEditingTool.makeEffect() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = rememberVectorPainter(image = videoEditingTool.icon),
                    contentDescription = "Description"
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = videoEditingTool.name,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VideoPlayer(videoProject: VideoProject, paddingValues: PaddingValues) {
    val context = LocalContext.current

    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            stop()
            setMediaItem(MediaItem.fromUri(videoProject.videoFileUri))
//        setVideoEffects(mutableListOf())
            prepare()
            play()
        }
    }

    val viewModel = viewModel(VideoEditorViewModel::class)
    val controlsVisible by viewModel.controlsVisible.collectAsState()

    var listenerRepeating by remember { mutableStateOf(false) }

    var isPlaying by remember { mutableStateOf(player.isPlaying) }
    var playbackState by remember { mutableIntStateOf(player.playbackState) }

    var framePerMillisecond by remember { mutableFloatStateOf(0f) }
    var duration by remember { mutableLongStateOf(0) }
    var frames by remember { mutableLongStateOf(0) }
    var currentTime by remember { mutableLongStateOf(0L) }
    var currentFrame by remember { mutableLongStateOf(0L) }

    Box(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBarsIgnoringVisibility)
            .padding(top = paddingValues.calculateTopPadding())
    ) {

        DisposableEffect(key1 = Unit) {
            val listenerHandler = Handler(Looper.getMainLooper())
            val playerListener = object : Player.Listener {
                override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
                    super.onAvailableCommandsChanged(availableCommands)
                }

                @androidx.annotation.OptIn(UnstableApi::class)
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when(playbackState) {
                        Player.STATE_READY -> {

                        }
                        else -> {}
                    }

                }

                @androidx.annotation.OptIn(UnstableApi::class)
                override fun onEvents(player2: Player, events: Player.Events) {
                    super.onEvents(player, events)

                    if (duration == 0L && events.contains(Player.EVENT_IS_PLAYING_CHANGED) && player.isPlaying) {
                        FFmpegKit.executeAsync(
                            "ffprobe -v error -select_streams v:0 -show_entries stream=nb_frames -of default=noprint_wrappers=1 -of csv=p=0" +
                                    "url") {

                        }

                        // 记录视频相关信息
                        framePerMillisecond = (player.videoFormat?.frameRate ?: 0f) / 1000f
                        duration = player.duration
                        frames = (duration * framePerMillisecond).toLong()

                        viewModel.updateCurrentVideoInfo(VideoInfo(videoProject.videoFileUri.toUri(), framePerMillisecond, duration, frames, videoProject))

                        Log.i("视频信息", "onEvents: player.videoFormat = ${player.videoFormat}")
                        Log.i("视频信息", "onEvents: player.videoFormat.frameRate = ${player.videoFormat?.frameRate}")
                        Log.i("视频信息", "onEvents: framePerMillisecond = $framePerMillisecond")
                        Log.i("视频信息", "onEvents: duration = $duration")
                        Log.i("视频信息", "onEvents: frames = $frames")
                    }

                    isPlaying = player.isPlaying
                    playbackState = player.playbackState

                    if (isPlaying) {
                        if (!listenerRepeating) {
                            listenerRepeating = true
                            listenerHandler.post(
                                object : Runnable {
                                    override fun run() {
                                        currentTime = player.currentPosition.coerceIn(0, duration)
                                        Log.i("test", "run: ${frames}")
                                        currentFrame = (currentTime * framePerMillisecond).toLong().coerceIn(0, frames)
                                        if (listenerRepeating) {
                                            listenerHandler.postDelayed(this, 100L)
                                        }
                                    }
                                }
                            )
                        }
                    } else {
                        listenerRepeating = false
                        currentTime =
                            player.currentPosition.coerceAtLeast(0L)
                        currentFrame = (currentTime * framePerMillisecond).toLong().coerceIn(0, frames)
                        Log.i("current time", "$currentTime")
                        Log.i("current frame", "$currentFrame")
                    }
                }
            }

            player.addListener(playerListener)

            onDispose {
                player.removeListener(playerListener)
                player.release()
            }
        }

        AndroidView(
            modifier = Modifier
                .align(Alignment.Center)
                .clickable { viewModel.setControlsVisible(controlsVisible.not()) },
            factory = {
                PlayerView(it).apply {
                    this.player = player
                }
//                textureView = TextureView(context).apply {
//                    layoutParams = LayoutParams(
//                        LayoutParams.MATCH_PARENT,
//                        LayoutParams.MATCH_PARENT
//                    )
//                }
//                textureView!!
            }
        )

        val videoPlayerControlsStates = VideoPlayerControlsStates(
            isPlaying = { isPlaying },
            currentTime = { currentTime },
            currentFrames = { currentFrame },
            duration = { duration },
            frames = { frames },
        )
        AppVideoPlayerControls(player, videoPlayerControlsStates)
    }

}


class VideoPlayerControlsStates(
    var isPlaying: () -> Boolean,
    var currentTime: () -> Long,
    var currentFrames: () -> Long,
    var duration: () -> Long,
    var frames: () -> Long,
)

@Composable
private fun AppVideoPlayerControls(
    player: ExoPlayer,
    videoPlayerControlsStates: VideoPlayerControlsStates,
) {
    val viewModel = viewModel(VideoEditorViewModel::class)
    val controlsVisible by viewModel.controlsVisible.collectAsState()
    val currentPosition by viewModel.videoCurrentPosition.collectAsState()
    val currentProject by viewModel.currentProject.collectAsState()

    val isVideoPlaying = remember(videoPlayerControlsStates.isPlaying()) {videoPlayerControlsStates.isPlaying() }
    val currentTime = remember(videoPlayerControlsStates.currentTime()) {videoPlayerControlsStates.currentTime() }
    val currentFrames = remember(videoPlayerControlsStates.currentFrames()) {videoPlayerControlsStates.currentFrames() }
    val duration = remember(videoPlayerControlsStates.duration()) {videoPlayerControlsStates.duration() }
    val frames = remember(videoPlayerControlsStates.frames()) {videoPlayerControlsStates.frames() }

    if (controlsVisible) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
//            AppVideoEditingTopBar()

            Row(){}

            // 后退  播放/暂停  前进
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
//                IconButton(
//                    modifier = Modifier.size(40.dp),
//                    onClick = {
//                    }
//                ) {
//                    Icon(
//                        modifier = Modifier.fillMaxSize(),
//                        imageVector = Icons.Filled.Replay5,
//                        contentDescription = "后退5秒",
//                    )
//                }

                IconButton(
                    modifier = Modifier.size(40.dp),
                    onClick = {
                        if (isVideoPlaying) {
                            Log.i("controls pause button", "AppVideoPlayerControls: 暂停播放, playbackState: ${player.playbackState}")
                            player.pause()
                        } else {
                            Log.i("controls play button", "AppVideoPlayerControls: 继续播放, playbackState: ${player.playbackState}")
                            player.play()
                    }
                }
                ) {
                    Icon(
                        modifier = Modifier.fillMaxSize(),
                        imageVector = when {
                            isVideoPlaying -> {
                                Icons.Filled.Pause
                            }

                            isVideoPlaying.not() && currentPosition == currentProject.mediaInformation!!.duration.toLong() -> {
                                Icons.Filled.Replay
                            }

                            else -> {
                                Icons.Filled.PlayArrow
                            }
                        },
                        contentDescription = "开始/暂停",
                    )
                }

//                IconButton(modifier = Modifier.size(40.dp), onClick = {}) {
//                    Icon(
//                        modifier = Modifier.fillMaxSize(),
//                        imageVector = Icons.Filled.Forward10,
//                        contentDescription = "前进10秒",
//                    )
//                }
            }

            Column {
                // 进度条
                Row(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        colors = SliderDefaults.colors(
                            inactiveTrackColor = MaterialTheme.colorScheme.inversePrimary
                        ),
                        enabled = true,
                        value = currentTime.toFloat(),
                        onValueChange = { player.seekTo(it.toLong()) },
                        valueRange = 0f..duration.toFloat(),
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 时间
                    Row(
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        Text(
                            modifier = Modifier
                                .weight(2f, false),
                            color = Color.White,
                            text = "${currentTime.formatMinSec()}/${duration.formatMinSec()}"
                        )
                    }

                    // 帧
                    Row(
                        modifier = Modifier
                            .weight(2f, false)
                            .padding(end = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            modifier = Modifier
                                .wrapContentWidth(),
                            onClick = {}
                        ) {
                            Icon(
                                tint = Color.White,
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "左"
                            )
                        }
                        Text(
                            modifier = Modifier
                                .wrapContentWidth()
                                .clickable { },
                            text = "$currentFrames/$frames",
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                        IconButton(
                            modifier = Modifier
                                .wrapContentWidth(),
                            onClick = {}
                        ) {
                            Icon(
                                tint = Color.White,
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "右"
                            )
                        }
                    }
                }
            }
        }
    }
}

fun Long.formatMinSec(): String {
    return if (this == 0L) {
        "00:00"
    } else {
        String.format(
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(this),
            TimeUnit.MILLISECONDS.toSeconds(this) -
                    TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(this)
                    )
        )
    }
}