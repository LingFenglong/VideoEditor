package com.lingfenglong.videoeditor.view

import android.media.MediaMetadataRetriever
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Label
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import com.lingfenglong.videoeditor.viewmodel.VideoEditorViewModel
import kotlinx.coroutines.launch

class EffectUtilView {
}

@androidx.annotation.OptIn(UnstableApi::class)
@Preview(showSystemUi = true)
@OptIn(ExperimentalMaterial3Api::class)
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompressEffect() {
    val viewModel = viewModel(modelClass = VideoEditorViewModel::class.java)
    val currentVideoInfo by viewModel.currentVideoInfo.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var dialogVisible by remember { mutableStateOf(true) }

    var wide by remember { mutableStateOf("") }
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
                            value = wide,
                            onValueChange = { wide = it },
                            label = {
                                Text("宽度")
                            },
                            suffix = { Text("px") },
                        )
                        TextField(
                            value = height,
                            onValueChange = { height = it },
                            label = {
                                Text("高度")
                            },
                            suffix = { Text("px") }
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

@Composable
fun WaterMarkEffect() {

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

@Composable
fun DialogEffectView() {

}