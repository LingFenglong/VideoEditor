package com.lingfenglong.videoeditor.view

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Label
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.RangeSliderState
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.GenericFontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.TextOverlay
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.lingfenglong.videoeditor.entity.effect.WaterMarkEffectInfo
import com.lingfenglong.videoeditor.viewmodel.VideoEditorViewModel

class EffectUtilView {
}


//@Preview(showSystemUi = true)

@Composable
fun TrimClipEffect() {
    val viewModel = viewModel(modelClass = VideoEditorViewModel::class.java)
    val currentVideoInfo by viewModel.currentVideoInfo.collectAsState()

    val startInteractionSource = remember { MutableInteractionSource() }
    val endInteractionSource = remember { MutableInteractionSource() }
    val startThumbAndTrackColors = SliderDefaults.colors(thumbColor = Color.Blue, activeTrackColor = Color.Red)
    val endThumbColors = SliderDefaults.colors(thumbColor = Color.Green)

    val rangeSliderState = remember {
        RangeSliderState(
            activeRangeStart = 0f,
            activeRangeEnd = currentVideoInfo.frames.toFloat(),
            valueRange = 0f..currentVideoInfo.frames.toFloat(),
        )
    }

    val mediaMetadataRetriever = MediaMetadataRetriever()
    mediaMetadataRetriever.setDataSource(LocalContext.current, currentVideoInfo.uri)

    val scope = rememberCoroutineScope()
//    var startImage: ImageBitmap = mediaMetadataRetriever.getFrameAtIndex(rangeSliderState.activeRangeStart.toInt())!!.asImageBitmap()
//    var endImage: ImageBitmap = mediaMetadataRetriever.getFrameAtIndex(rangeSliderState.activeRangeEnd.toInt())!!.asImageBitmap()
//    rangeSliderState.onValueChangeFinished = {
//        scope.launch {
//            startImage = mediaMetadataRetriever.getFrameAtIndex(rangeSliderState.activeRangeStart.toInt())!!.asImageBitmap()
//            endImage = mediaMetadataRetriever.getFrameAtIndex(rangeSliderState.activeRangeEnd.toInt())!!.asImageBitmap()
//        }
//    }

    val tooltipState = remember { TooltipState() }

    DrawerEffectView {
        Column {

            // 进度条
            Row {
                RangeSlider(
                    state = rangeSliderState,
                    startThumb = {
                        Label(
                            label = {
                                TooltipBox(
                                    positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
                                    tooltip = {
                                        RichTooltip(
                                            title = { Text(text = "title") },
                                            action = {},
                                            caretSize = TooltipDefaults.caretSize
                                        ) {
                                            Text(text = "rich tool text")
                                        }
                                    },
                                    state = tooltipState
                                ) {
                                    Card(shape = MaterialTheme.shapes.small) {
//                                        Image(
//                                            bitmap = startImage,
//                                            contentDescription = "activeRangeStart"
//                                        )
                                    }
                                }
                            },
                            interactionSource = startInteractionSource
                        ) {
                            SliderDefaults.Thumb(
                                interactionSource = startInteractionSource,
                                colors = startThumbAndTrackColors
                            )
                        }
                    },
                    endThumb = {
                        Label(
                            label = {
                                Card(shape = RectangleShape) {
//                                    Image(
//                                        contentScale = ContentScale.Fit,
//                                        bitmap = endImage,
//                                        contentDescription = "activeRangeEnd"
//                                    )
                                }
                            },
                            interactionSource = endInteractionSource
                        ) {
                            SliderDefaults.Thumb(
                                interactionSource = endInteractionSource,
                                colors = endThumbColors
                            )
                        }
                    }
                )
            }

            // 取消 和 确认
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = {
                        viewModel.setMagicToolButtonVisible(true)
                        viewModel.setEffectVisibleId(0)
                    }
                ) {
                    Icon(
                        painter = rememberVectorPainter(image = Icons.Default.Cancel),
                        contentDescription = "取消"
                    )
                }
                IconButton(
                    onClick = {}
                ) {
                    Icon(
                        painter = rememberVectorPainter(image = Icons.Default.ConfirmationNumber),
                        contentDescription = "确定"
                    )
                }
            }
        }
    }
}

@Composable
fun MergeEffect() {

}

//@Preview(showSystemUi = true)

@Composable
fun CompressEffect() {
    val viewModel = viewModel(modelClass = VideoEditorViewModel::class.java)
    val currentVideoInfo by viewModel.currentVideoInfo.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var dialogVisible by remember { mutableStateOf(true) }

    var width by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (!dialogVisible) {
            viewModel.setMagicToolButtonVisible(true)
            viewModel.setEffectVisibleId(0)
        } else {
            BasicAlertDialog(
                modifier = Modifier,
                onDismissRequest = { dialogVisible = false },
            ) {
                Card(modifier = Modifier, shape = CardDefaults.shape) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            modifier = Modifier.padding(bottom = 16.dp),
                            text = "调整分辨率",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineSmall
                        )

                        TextField(
                            value = width,
                            onValueChange = { width = it },
                            label = {
                                Text("宽度")
                            },
                            suffix = { Text("px") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        TextField(
                            value = height,
                            onValueChange = { height = it },
                            label = {
                                Text("高度")
                            },
                            suffix = { Text("px") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            TextButton(
                                onClick = { dialogVisible = false },
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
    }
}


@Preview(showSystemUi = true)
@Composable
fun WaterMarkEffect() {
    val viewModel = viewModel(modelClass =  VideoEditorViewModel::class)
    val transformManager = viewModel.transformManager

    var text by remember { mutableStateOf("Text") }
    var fontSize by remember { mutableIntStateOf(12) }
    var fontColor by remember { mutableStateOf(Color.Black) }
    var backgroundColor by remember { mutableStateOf(Color.Transparent) }
    var fontStyle by remember { mutableStateOf(FontStyle.Normal) }
    var fontFamily by remember { mutableStateOf(FontFamily.Default) }

    var fontColorPickerDialogVisible by remember { mutableStateOf(false) }
    var backgroundColorPickerDialogVisible by remember { mutableStateOf(false) }

    var fontStyleDropdownMenuExpanded by remember { mutableStateOf(false) }
    var fontFamilyDropdownMenuExpanded by remember { mutableStateOf(false) }
    var dialogVisible by remember { mutableStateOf(true) }
    val fontColorController = rememberColorPickerController()
    val backgroundColorController = rememberColorPickerController()

    if (dialogVisible) {
        Dialog(
            onDismissRequest = { dialogVisible = false }
        ) {
            Card() {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = 12.dp),
                        text = "添加水印",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    // 回显示例
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .heightIn(80.dp, 160.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            modifier = Modifier
                                .background(backgroundColor)
                                .wrapContentSize(),
                            text = text,
                            color = fontColor,
                            fontSize = fontSize.sp,
                            fontFamily = fontFamily,
                            fontStyle = fontStyle,
                            textAlign = TextAlign.Center,
                        )
                    }

                    // 字体内容
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        value = text,
                        onValueChange = { text = it },
                        label = { Text("内容") }
                    )

                    Spacer(modifier = Modifier.padding(6.dp))

                    // 字体大小
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        value = if (fontSize == 0) "" else fontSize.toString(),
                        onValueChange = {
                            fontSize = if (it.isEmpty()) 0 else it.toInt().coerceIn(1, 72)
                        },
                        label = { Text("字体大小") },
                        suffix = { Text("px") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.padding(6.dp))

;                    Row(
                        modifier = Modifier,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1F, true)
                                .padding(start = 12.dp, end = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // 文字颜色
                            OutlinedButton(
                                modifier = Modifier,
                                shape = RoundedCornerShape(12),
                                onClick = { fontColorPickerDialogVisible = true; dialogVisible = true },
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("文字颜色")
                                    Card(
                                        modifier = Modifier
                                            .background(color = fontColor)
                                            .size(40.dp, 40.dp),
                                        shape = CircleShape
                                    ) {}
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .weight(1F, true)
                                .padding(start = 6.dp, end = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // 背景颜色
                            OutlinedButton(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { backgroundColorPickerDialogVisible = true; dialogVisible = true },
                            ) {
                                Text("背景颜色")
                                Card(
                                    modifier = Modifier
                                        .background(color = backgroundColor)
                                        .size(40.dp, 40.dp),
                                    shape = CircleShape
                                ) {}
                            }
                        }
                    }

                    Spacer(modifier = Modifier.padding(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 字体
                        ExposedDropdownMenuBox(
                            modifier = Modifier
                                .weight(1F, true)
                                .padding(start = 12.dp, end = 6.dp),
                            expanded = fontFamilyDropdownMenuExpanded,
                            onExpandedChange = { fontFamilyDropdownMenuExpanded = fontFamilyDropdownMenuExpanded.not() }
                        ) {
                            OutlinedTextField(
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryEditable, true),
                                label = { Text("字体") },
                                value = when (fontFamily) {
                                    is GenericFontFamily -> (fontFamily as GenericFontFamily).name
                                    else -> "默认"
                                },
                                readOnly = true,
                                onValueChange = { },
                            )

                            ExposedDropdownMenu(
                                expanded = fontFamilyDropdownMenuExpanded,
                                onDismissRequest = { fontFamilyDropdownMenuExpanded = fontFamilyDropdownMenuExpanded.not() }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("默认") },
                                    onClick = { fontFamily = FontFamily.Default; fontFamilyDropdownMenuExpanded = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(FontFamily.Cursive.name) },
                                    onClick = { fontFamily = FontFamily.Cursive; fontFamilyDropdownMenuExpanded = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(FontFamily.SansSerif.name) },
                                    onClick = { fontFamily = FontFamily.SansSerif; fontFamilyDropdownMenuExpanded = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(FontFamily.Serif.name) },
                                    onClick = { fontFamily = FontFamily.Serif; fontFamilyDropdownMenuExpanded = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(FontFamily.Monospace.name) },
                                    onClick = { fontFamily = FontFamily.Monospace;fontFamilyDropdownMenuExpanded = false }
                                )
                            }
                        }

                        // 样式
                        ExposedDropdownMenuBox(
                            modifier = Modifier
                                .weight(1F, false)
                                .padding(start = 6.dp, end = 12.dp),
                            expanded = fontStyleDropdownMenuExpanded,
                            onExpandedChange = { fontStyleDropdownMenuExpanded = fontStyleDropdownMenuExpanded.not() }
                        ) {
                            OutlinedTextField(
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true),
                                label = { Text("样式") },
                                value = when(fontStyle) {
                                    FontStyle.Normal -> "无"
                                    FontStyle.Italic -> "斜体"
                                    else -> { "error" }
                                },
                                readOnly = true,
                                onValueChange = { fontStyleDropdownMenuExpanded = false },
                            )

                            ExposedDropdownMenu(
                                expanded = fontStyleDropdownMenuExpanded,
                                onDismissRequest = { fontStyleDropdownMenuExpanded = fontStyleDropdownMenuExpanded.not() }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("无") },
                                    onClick = { fontStyle = FontStyle.Normal; fontStyleDropdownMenuExpanded = false }
                                )

                                DropdownMenuItem(
                                    text = { Text("斜体") },
                                    onClick = { fontStyle = FontStyle.Italic; fontStyleDropdownMenuExpanded = false }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(
                            onClick = { dialogVisible = false },
                        ) {
                            Text("取消")
                        }
                        TextButton(
                            onClick = {
//                                viewModel.addEffectInfo(WaterMarkEffectInfo(effect = { OverlayEffect(listOf(TextOverlay.createStaticBitmapOverlay(
//                                Bitmap.createBitmap(1,1, Bitmap.Config.RGBA_F16)))) }))
                                dialogVisible = false
                            }
                        ) {
                            Text("确定")
                        }
                    }
                }
            }
        }
    }

    // 文字颜色选择器
    if (fontColorPickerDialogVisible) {
        Dialog(onDismissRequest = { fontColorPickerDialogVisible = false }) {
            HsvColorPicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp)
                    .padding(10.dp),
                controller = fontColorController,
                onColorChanged = { colorEnvelope ->
                    fontColor = colorEnvelope.color
//                    fontColorPickerDialogVisible = false
                }
            )
        }
    }

    // 背景颜色选择器
    if (backgroundColorPickerDialogVisible) {
        BasicAlertDialog(onDismissRequest = { backgroundColorPickerDialogVisible = false }) {
            HsvColorPicker(
                modifier = Modifier
                    .height(450.dp)
                    .padding(10.dp),
                controller = backgroundColorController,
                onColorChanged = { colorEnvelope ->
                    backgroundColor = colorEnvelope.color
//                    backgroundColorPickerDialogVisible = false
                }
            )
        }
    }
}


@Composable
fun DrawerEffectView(bottomDrawerView: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        bottomDrawerView.invoke()
    }
}