package cn.live.livetest.audio;

import android.media.MediaCodec;

import java.nio.ByteBuffer;

public class AudioEncoder {
    private MediaCodec mMediaCodec;
    private OnAudioEncodeListener mListener;
    private AudioConfiguration mAudioConfiguration;
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();

    public void setOnAudioEncodeListener(OnAudioEncodeListener listener) {
        this.mListener = listener;
    }

    public AudioEncoder(AudioConfiguration configuration) {
        mAudioConfiguration = configuration;
    }

    void prepareEncoder() {
        mMediaCodec = AudioMediaCodec.getAudioMediaCodec(mAudioConfiguration);
        mMediaCodec.start();
    }

    public synchronized void stop() {
        if (mMediaCodec != null) {
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
        }
    }

    public synchronized void offerEncoder(byte[] inputData) {
        if (mMediaCodec == null) {
            return;
        }
        ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
        ByteBuffer[] outputBuffers = mMediaCodec.getOutputBuffers();
        int inputBufferIndex = mMediaCodec.dequeueInputBuffer(12000);
        if (inputBufferIndex > 0) {
            ByteBuffer buffer = inputBuffers[inputBufferIndex];
            buffer.clear();
            buffer.put(inputData);
            mMediaCodec.queueInputBuffer(inputBufferIndex, 0, inputData.length, 0, 0);
        }
        int outputIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo, 12000);
        while (outputIndex > 0) {
            ByteBuffer outputBuffer = outputBuffers[outputIndex];
            if (mListener != null) {
                mListener.onAudioEncode(outputBuffer, mBufferInfo);
            }
            mMediaCodec.releaseOutputBuffer(outputIndex, false);
            outputIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo, 0);
        }
    }
}





