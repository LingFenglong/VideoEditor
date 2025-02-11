package com.lingfenglong.videoeditor

import android.media.MediaFormat
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.lingfenglong.videoeditor.entity.EffectInfo
import com.lingfenglong.videoeditor.entity.ExportSettings
import kotlin.concurrent.thread

class Components {

}

class EffectListPreviewParameterProvider : PreviewParameterProvider<List<EffectInfo>> {
    override val values: Sequence<List<EffectInfo>>
        get() = sequenceOf(listOf(
            EffectInfo(1, "裁剪"),
            EffectInfo(2, "删除"),
            EffectInfo(3, "编辑"),
            EffectInfo(4, "更新")
        ))

}

/**
 * 视频编辑图层
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showSystemUi = false)
@Composable
fun VideoEditingHistory(
    onDismissRequest: () -> Unit = {},
//    @PreviewParameter(provider = EffectListPreviewParameterProvider::class) effectInfoList: List<EffectInfo>
    effectInfoList: List<EffectInfo> = listOf(
        EffectInfo(1, "裁剪"),
        EffectInfo(2, "删除"),
        EffectInfo(3, "编辑"),
        EffectInfo(4, "更新"),
    )
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
                        modifier = Modifier.padding(bottom = 16.dp),
                        text = "视频编辑图层",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(effectInfoList) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                modifier = Modifier,
                                text = it.effectName,
                                textAlign = TextAlign.Center
                            )

                            IconButton(onClick = { /*TODO*/ }) {
                                Icon(painter = rememberVectorPainter(image = Icons.Filled.Delete), contentDescription = "删除")
                            }
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
                        onClick = { TODO() }
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}

class OnDismissRequestParameterProvider : PreviewParameterProvider<() -> Unit> {
    override val values: Sequence<() -> Unit>
        get() = sequenceOf({})
}

/**
 * 导出对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
//@Preview(showSystemUi = true, showBackground = false)
fun ExportDialog(
    @PreviewParameter(OnDismissRequestParameterProvider::class) onDismissRequest: () -> Unit
) {
    var dropdownMenuExpand by remember { mutableStateOf(false) }
    val exportSettings by remember { mutableStateOf(ExportSettings.DEFAULT) }
    var exportName by remember { mutableStateOf(exportSettings.exportName) }
    var exportFormat by remember { mutableStateOf(exportSettings.exportFormat) }
    var exportFormatText by remember { mutableStateOf("MP4") }
    var exportLossless by remember { mutableStateOf(exportSettings.lossless) }

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
                        onClick = { TODO() }
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}