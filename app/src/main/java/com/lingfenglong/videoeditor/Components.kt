package com.lingfenglong.videoeditor

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MimeTypes
import com.lingfenglong.videoeditor.constant.Constants.Companion.APP_TAG
import com.lingfenglong.videoeditor.entity.ExportSettings
import com.lingfenglong.videoeditor.entity.effect.EffectInfo
import com.lingfenglong.videoeditor.viewmodel.VideoEditorViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.roundToInt

class Components {

}

/**
 * 视频编辑图层
 */

@Composable
fun VideoEditingHistory(
    onDismissRequest: () -> Unit = {}
) {
    val viewModel = viewModel(modelClass = VideoEditorViewModel::class)
    val transformManager = viewModel.transformManager

    val effectInfoList: MutableList<EffectInfo> = remember { mutableStateListOf() }
    effectInfoList += transformManager.getEffectInfoList()

    Log.i(APP_TAG, "VideoEditingHistory: effect info list $effectInfoList")

    BasicAlertDialog(
        modifier = Modifier,
        onDismissRequest = onDismissRequest
    ) {
        Card(
            modifier = Modifier,
            shape = CardDefaults.shape,
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        text = "视频编辑图层",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(effectInfoList) {
                        EffectInfoItem(it) {
                            effectInfoList.remove(it);
                            transformManager.removeEffectInfo(it)
                        }
                    }
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
                        onClick = { onDismissRequest() }
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}

@Composable
fun EffectInfoItem(
    effectInfo: EffectInfo,
    onDelete: () -> Unit,
) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row {
            Icon(
                modifier = Modifier.wrapContentSize(),
                painter = rememberVectorPainter(effectInfo.icon()),
                contentDescription = "效果图标"
            )
            Text(
                modifier = Modifier.wrapContentSize(),
                text = effectInfo.name,
                textAlign = TextAlign.Center
            )
        }

        IconButton(onClick = { onDelete() }) {
            Icon(painter = rememberVectorPainter(image = Icons.Filled.Delete), contentDescription = "删除")
        }
    }
}

/**
 * 导出对话框
 */
@Composable
fun ExportDialog(
    onDismissRequest: () -> Unit
) {
    val viewModel = viewModel(modelClass = VideoEditorViewModel::class.java)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val transformManager = viewModel.transformManager

    var dropdownMenuExpand by remember { mutableStateOf(false) }
    val exportSettings by remember { mutableStateOf(ExportSettings.DEFAULT) }
    var videoMimeTypeText by remember { mutableStateOf(MimeTypes.VIDEO_MP4V) }
    val audioMimeTypeText by remember { mutableStateOf(MimeTypes.AUDIO_AAC) }

    var currentProgress by remember { mutableFloatStateOf(0F) }
    var exporting by remember { mutableStateOf(false) }

    exportSettings.exportName = transformManager.videoProject.projectName

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
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth(),
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
                    TextField(value = exportSettings.exportName, onValueChange = { exportSettings.exportName = it })
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
                        TextField(
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true),
                            value = videoMimeTypeText,
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    painter = rememberVectorPainter(image = Icons.Outlined.ArrowDropDown),
                                    contentDescription = "export format"
                                )
                            },
                            onValueChange = { }
                        )

                        ExposedDropdownMenu(
                            expanded = dropdownMenuExpand,
                            onDismissRequest = { dropdownMenuExpand = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(text = "H263") },
                                onClick = {
                                    videoMimeTypeText = "H263"
                                    exportSettings.videoMimeType = MimeTypes.VIDEO_H263
                                    dropdownMenuExpand = false
                                    // TODO: set the export name
                                    // exportSettings.exportName = project.name
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(text = "AVC") },
                                onClick = {
                                    videoMimeTypeText = "AVC"
                                    exportSettings.videoMimeType = MimeTypes.VIDEO_H264
                                    dropdownMenuExpand = false
                                    // TODO: set the export name
                                    // exportSettings.exportName = project.name
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(text = "HEVC") },
                                onClick = {
                                    videoMimeTypeText = "HEVC"
                                    exportSettings.videoMimeType = MimeTypes.VIDEO_H265
                                    dropdownMenuExpand = false
                                    // TODO: set the export name
                                    // exportSettings.exportName = project.name
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(text = "MP4V") },
                                onClick = {
                                    videoMimeTypeText = "MP4V"
                                    exportSettings.videoMimeType = MimeTypes.VIDEO_MP4V
                                    dropdownMenuExpand = false
                                    // TODO: set the export name
                                    // exportSettings.exportName = project.name
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
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    Switch(
                        checked = exportSettings.lossless,
                        onCheckedChange = {
                            exportSettings.lossless = exportSettings.lossless.not()
                        }
                    )
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
                        onClick = {
                            transformManager.export(context, exportSettings)
                            exporting = true
//                            onDismissRequest()

                            val handler = Handler(Looper.getMainLooper())
                            handler.postDelayed(
                                object : Runnable {
                                    override fun run() {
                                        currentProgress = transformManager.getProgress()
                                        if (currentProgress < 1F) {
                                            handler.postDelayed(this, 200)
                                        }
                                    }
                                }, 200L
                            )
                        }
                    ) {
                        Text("确定")
                    }
                }
            }
        }

        if (exporting) {
            ProgressIndicatorDialog(
                currentProgress = { currentProgress },
                onCancel = { transformManager.cancel(); },
                onDismissRequest = { exporting = false }
            )
        }
    }
}

@Composable
fun ProgressIndicatorDialog(
    currentProgress: () -> Float,
    onCancel: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val exporting = currentProgress() < 1
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (exporting) "正在导出" else "导出完成",
                    style = MaterialTheme.typography.headlineSmall,
                )

                Spacer(modifier = Modifier.padding(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier,
                        strokeCap = StrokeCap.Round,
                        progress = currentProgress,
                    )
                    Text(
                        text = "${(currentProgress() * 100).roundToInt()}%"
                    )
                }

                Spacer(modifier = Modifier.padding(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (exporting) {
                        FilledTonalButton(onClick = { onCancel(); onDismissRequest() }) {
                            Icon(painter = rememberVectorPainter(image = Icons.Filled.Close), contentDescription = "取消导出")
                            Text(text = "取消导出")
                        }
                    } else {
                        FilledTonalButton(onClick = { onDismissRequest() }) {
                            Icon(painter = rememberVectorPainter(image = Icons.Filled.Check), contentDescription = "确定")
                            Text(text = "确定")
                        }
                    }
                }

                Spacer(modifier = Modifier.padding(3.dp))
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun ProgressIndicatorPreview() {
    var currentProgress by remember { mutableFloatStateOf(0F) }
    var dialogVisibility by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(dialogVisibility) {
        if (dialogVisibility) {
            coroutineScope.launch {
                while (dialogVisibility && currentProgress < 1F) {
                    currentProgress += 0.01F
                    delay(100)
                }
            }
        }
    }

    Box(
        contentAlignment = Alignment.Center
    ) {
        ExtendedFloatingActionButton(onClick = { dialogVisibility = true; currentProgress = 0F }) {
            Text(text = "Button")
        }
    }

    if (dialogVisibility) {
        ProgressIndicatorDialog(
            currentProgress = { currentProgress },
            onCancel = { currentProgress = 0F; dialogVisibility = false },
            onDismissRequest = { dialogVisibility = false }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FrameSequence(
    interval: Float
) {
    val viewModel = viewModel(modelClass = VideoEditorViewModel::class)
    val videoProject = viewModel.transformManager.videoProject
    val videoFilePath = videoProject.videoFilePath
    val outputPath = "${videoProject.projectFilePath}/frames"
    val exoPlayer = viewModel.transformManager.exoPlayer

    val frameList: MutableList<ImageBitmap> = remember { mutableStateListOf() }

    val lazyListState = rememberLazyListState(0, -5)

    val outputFile = File(outputPath)
    if (!outputFile.exists() && !outputFile.mkdirs()) {
        Log.e(APP_TAG, "FrameSequence: failed to mkdir $outputPath", RuntimeException("创建文件失败"))
    }
    
    LaunchedEffect(key1 = exoPlayer.currentPosition) {
        while (true) {
            val currentPosition = exoPlayer.currentPosition
            val currentFrameIndex = (currentPosition.toFloat() / exoPlayer.duration * frameList.size).toInt()
            lazyListState.animateScrollToItem(currentFrameIndex, -5)
            delay(100)
        }
    }

//    val ffmpegCommand = "-i $videoFilePath -vf 'fps=1' -q:v 31 ${outputPath}/output_%03d.jpg"
//    FFmpegKit.executeAsync(
//        /* command */ ffmpegCommand,
//        /* completeCallback */ {
//            val size = File(outputPath).listFiles()!!.size
//            for (i in 1..size) {
//                frameList += BitmapFactory.decodeFile("$outputPath/output_${"%03d".format(i)}.jpg").asImageBitmap()
//            }
//        },
//        /* logCallback */ {
//        },
//        /* statisticsCallback */ {
//        }
//    ).apply {
//        Log.i(APP_TAG, "FrameSequence: $command")
//        Log.i(APP_TAG, "FrameSequence: $returnCode")
//    }

//    val size = File(outputPath).listFiles()!!.size
//    for (i in 1..size) {
//        frameList += BitmapFactory.decodeFile("$outputPath/output_${"%03d".format(i)}.jpg").asImageBitmap()
//    }

    Box {
        LazyRow(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .height(30.dp)
                .align(Alignment.Center),
            state = lazyListState
        ) {
            items(frameList) {
                Image(
                    modifier = Modifier.height(60.dp),
                    bitmap = it,
                    contentDescription = "frame"
                )
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                text = exoPlayer.currentPosition.timeFormat(),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Image(
                modifier = Modifier
                    .height(48.dp)
                    .align(Alignment.CenterHorizontally)
                    .alpha(0.25F),
                painter = painterResource(id = R.drawable.cursor),
                contentDescription = "cursor"
            )
            Spacer(modifier = Modifier.padding(12.dp))
        }

        // Left and right sliders to select the video region
//        Box(
//            modifier = Modifier
//                .align(Alignment.CenterStart)
//                .padding(16.dp)
//        ) {
//            Slider(
//                value = currentFrameIndex.toFloat(),
//                onValueChange = { newValue ->
//
//                },
//                valueRange = 0f..(frameList.size - 1).toFloat(),
//                modifier = Modifier.width(200.dp)
//            )
//        }
//
//        Box(
//            modifier = Modifier
//                .align(Alignment.CenterEnd)
//                .padding(16.dp)
//        ) {
//            Slider(
//                value = currentFrameIndex.toFloat(),
//                onValueChange = { newValue ->
//
//                },
//                valueRange = 0f..(frameList.size - 1).toFloat(),
//                modifier = Modifier.width(200.dp)
//            )
//        }
    }
}