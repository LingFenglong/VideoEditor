package com.lingfenglong.videoeditor

import android.util.Log
import com.arthenica.ffmpegkit.FFprobeKit
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(MockitoJUnitRunner::class)
class ExampleUnitTest {
    @Before
    fun setUp() {
        val mockedLog = mockStatic(Log::class.java)
        mockedLog.run {
            `when`<Int> { Log.i(anyString(), anyString()) }.thenReturn(1)
        }
    }


}