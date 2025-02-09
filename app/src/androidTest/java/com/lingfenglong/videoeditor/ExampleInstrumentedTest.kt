package com.lingfenglong.videoeditor

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.FFprobeKit
import com.arthenica.ffmpegkit.MediaInformation
import kotlinx.coroutines.awaitAll

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.io.File

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.lingfenglong.videoeditor", appContext.packageName)
    }

    @Test
    fun ffmpegKitTest() {
        val video = File("/storage/emulated/0/Download/test.mp4")
        println("read = ${video.canRead()}  write = ${video.canWrite()}")
        println(video)

        val mediaInformationSession = FFprobeKit.getMediaInformation(video.absolutePath)
        val mediaInformation = mediaInformationSession.mediaInformation
        println(mediaInformation.format)
        println(mediaInformation.size)
        println(mediaInformation.tags)
        println(mediaInformation.bitrate)
        println(mediaInformation.streams)
        println(mediaInformation.chapters)
        println(mediaInformation.duration)
        println(mediaInformation.filename)
        println(mediaInformation.formatProperties)
        println(mediaInformation.longFormat)
        println(mediaInformation.startTime)
        println(mediaInformation.allProperties)

        Thread.sleep(10_000)
    }
}