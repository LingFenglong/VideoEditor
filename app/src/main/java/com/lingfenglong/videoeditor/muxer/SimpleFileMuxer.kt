package com.lingfenglong.videoeditor.muxer

import android.media.MediaCodec
import androidx.media3.common.Format
import androidx.media3.common.Metadata
import androidx.media3.muxer.Muxer
import androidx.media3.muxer.Muxer.MuxerException
import com.google.common.collect.ImmutableList
import java.io.File
import java.nio.ByteBuffer

class SimpleFileMuxer(private val muxer: Muxer) : Muxer {

    /**
     * Adds a track of the given media format.
     *
     * @param format The [Format] of the track.
     * @return The [TrackToken] for this track, which should be passed to [     ][.writeSampleData].
     * @throws MuxerException If the muxer encounters a problem while adding the track.
     */
    override fun addTrack(format: Format): Muxer.TrackToken {
        return muxer.addTrack(format)
    }

    /**
     * Writes encoded sample data.
     *
     * @param trackToken The [TrackToken] of the track, previously returned by [     ][.addTrack].
     * @param byteBuffer A buffer containing the sample data to write to the container.
     * @param bufferInfo The [BufferInfo] of the sample.
     * @throws MuxerException If the muxer fails to write the sample.
     */
    override fun writeSampleData(
        trackToken: Muxer.TrackToken,
        byteBuffer: ByteBuffer,
        bufferInfo: MediaCodec.BufferInfo,
    ) {
        muxer.writeSampleData(trackToken, byteBuffer, bufferInfo)
    }

    /** Adds [metadata][Metadata.Entry] about the output file.  */
    override fun addMetadataEntry(metadataEntry: Metadata.Entry) {
        muxer.addMetadataEntry(metadataEntry)
    }

    /**
     * Closes the file.
     *
     *
     * The muxer cannot be used anymore once this method returns.
     *
     * @throws MuxerException If the muxer fails to finish writing the output.
     */
    override fun close() {
        muxer.close()
    }

//    class Factory : Muxer.Factory {
//        /**
//         * Returns a new [Muxer].
//         *
//         * @param path The path to the output file.
//         * @throws MuxerException If an error occurs opening the output file for writing.
//         */
//        override fun create(path: String): Muxer {
//            val file = File(path)
////            if (file.exists()) {
//////                throw MuxerException("File $path already exists", MuxerException.Type.UNEXPECTED)
////            } else {
////
////            }
//
//        }
//
//        /**
//         * Returns the supported sample [MIME types][MimeTypes] for the given [ ].
//         */
//        override fun getSupportedSampleMimeTypes(trackType: Int): ImmutableList<String> {
//            TODO("Not yet implemented")
//        }
//    }
}