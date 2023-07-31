package com.training.myphone;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import java.io.IOException;
import java.nio.ByteBuffer;


public class AudioTrimmer {

    public static String trimAudio(String inputPath, String outputPath, long startUs, long endUs) {
        try {
            MediaExtractor extractor = new MediaExtractor();
            extractor.setDataSource(inputPath);

            int audioTrackIndex = getAudioTrackIndex(extractor);
            if (audioTrackIndex < 0) {
                extractor.release();
                return null; // No audio track found in the media file
            }

            extractor.selectTrack(audioTrackIndex);

            MediaFormat inputFormat = extractor.getTrackFormat(audioTrackIndex);
            MediaMuxer muxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            int audioTrackIndexOut = muxer.addTrack(inputFormat);
            muxer.start();

            ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

            long sampleTime;
            long trimStartTimeUs = startUs;
            long trimEndTimeUs = endUs;
            extractor.seekTo(trimStartTimeUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC);

            while ((sampleTime = extractor.getSampleTime()) < trimEndTimeUs) {
                if (sampleTime >= trimStartTimeUs) {
                    int sampleSize = extractor.readSampleData(buffer, 0);
                    if (sampleSize < 0) {
                        break;
                    }
                    int flags = extractor.getSampleFlags();
                    muxer.writeSampleData(audioTrackIndexOut, buffer, info);
                    extractor.advance();
                } else {
                    extractor.advance();
                }
            }

            extractor.release();
            muxer.stop();
            muxer.release();

            return outputPath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static int getAudioTrackIndex(MediaExtractor extractor) {
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("audio/")) {
                return i;
            }
        }
        return -1;
    }
}
