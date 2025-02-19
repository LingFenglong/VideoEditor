package com.lingfenglong.videoeditor

import android.media.MediaFormat
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lingfenglong.videoeditor.entity.ExportSettings
import com.lingfenglong.videoeditor.entity.effect.EffectInfo
import com.lingfenglong.videoeditor.viewmodel.VideoEditorViewModel

class Components {

}


class EffectListPreviewParameterProvider : PreviewParameterProvider<List<EffectInfo>> {
    override val values: Sequence<List<EffectInfo>>
        get() = sequenceOf(
            listOf(
//                EffectInfo("裁剪", { Icons.Filled.Crop }, { Crop(1F, 1F, 1F, 1F) }),
//                EffectInfo("裁剪", { Icons.Filled.Crop }, { Crop(1F, 1F, 1F, 1F) }),
//                EffectInfo("裁剪", { Icons.Filled.Crop }, { Crop(1F, 1F, 1F, 1F) }),
//                EffectInfo("裁剪", { Icons.Filled.Crop }, { Crop(1F, 1F, 1F, 1F) })
            )
        )
}

/**
 * 视频编辑图层
 */


@Preview(showSystemUi = false)
@Composable
fun VideoEditingHistory(
    onDismissRequest: () -> Unit = {},
    effectInfoList: List<EffectInfo> = listOf(),
) {
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
                    items(effectInfoList) { EffectInfoItem(it) }
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


class EffectInfoPreviewParameterProvider : PreviewParameterProvider<EffectInfo> {
    override val values: Sequence<EffectInfo>
        get() = sequenceOf(
//            EffectInfo("裁剪", { Icons.Filled.Crop }, { Crop(1F, 1F,1F,1F) })
        )
}



@Preview
@Composable
fun EffectInfoItem(
    @PreviewParameter(provider = EffectInfoPreviewParameterProvider::class) effectInfo: EffectInfo,
) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row() {
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

        IconButton(onClick = { /*TODO*/ }) {
            Icon(painter = rememberVectorPainter(image = Icons.Filled.Delete), contentDescription = "删除")
        }
    }
}

/**
 * 导出对话框
 */
@Composable
//@Preview(showSystemUi = true, showBackground = false)
fun ExportDialog(
    onDismissRequest: () -> Unit
) {
    val viewModel = viewModel(modelClass = VideoEditorViewModel::class.java)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val transformManager by viewModel.transformerManager.collectAsState()

    val videoProject by viewModel.currentProject.collectAsState()
    var dropdownMenuExpand by remember { mutableStateOf(false) }
    val exportSettings by remember { mutableStateOf(ExportSettings.DEFAULT) }
    var exportName by remember { mutableStateOf(exportSettings.exportName) }
    var exportFormat by remember { mutableStateOf(exportSettings.videoMimeType) }
    var exportFormatText by remember { mutableStateOf("MP4") }
    var exportLossless by remember { mutableStateOf(exportSettings.lossless) }

    var currentProgress by remember { mutableFloatStateOf(0F) }
    var exporting by remember { mutableStateOf(false) }

    exportSettings.exportName = videoProject.projectName

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
                        TextField(
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true),
                            value = exportFormatText,
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
                                text = { Text(text = "MP4") },
                                onClick = {
                                    exportFormatText = "MP4"
                                    exportFormat = MediaFormat.MIMETYPE_VIDEO_MPEG4
                                    dropdownMenuExpand = false
                                    // TODO: set the export name
                                    //    exportSettings.exportName = project.name
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(text = "HEVC") },
                                onClick = {
                                    exportFormatText = "HEVC"
                                    exportFormat = MediaFormat.MIMETYPE_VIDEO_HEVC
                                    dropdownMenuExpand = false
                                    // TODO: set the export name
                                    //    exportSettings.exportName = project.name
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(text = "AVC") },
                                onClick = {
                                    exportFormatText = "AVC"
                                    exportFormat = MediaFormat.MIMETYPE_VIDEO_AVC
                                    dropdownMenuExpand = false
                                    // TODO: set the export name
                                    //    exportSettings.exportName = project.name
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(text = "AVC1") },
                                onClick = {
                                    exportFormatText = "AVC1"
                                    exportFormat = MediaFormat.MIMETYPE_VIDEO_AV1
                                    dropdownMenuExpand = false
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
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    Switch(
                        checked = exportLossless,
                        onCheckedChange = {
                            exportLossless = exportLossless.not()
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
                            onDismissRequest()

                            val handler = Handler(Looper.getMainLooper())
                            handler.postDelayed(
                                object : Runnable {
                                    override fun run() {
                                        currentProgress = transformManager.getProgress()
                                        if (currentProgress != 1F) {
                                            handler.postDelayed(this, 100)
                                        }
                                    }
                                }, 100L
                            )
                        }
                    ) {
                        Text("确定")
                    }
                }
            }
        }

        if (exporting) {
            ProgressIndicator { currentProgress }
        }
    }
}

@Composable
fun ProgressIndicator(
    currentProgress: () -> Float
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LinearProgressIndicator(
            progress = currentProgress,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}