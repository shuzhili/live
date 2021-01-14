package cn.live.livetest.camera.encode;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.RequiresApi;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

import cn.live.livetest.camera.video.VideoConfiguration;

@TargetApi(18)
public class MyEncoder {
    private MediaCodec mMediaCodec;
    private VideoConfiguration mVideoConfiguration;

    private HandlerThread mHandlerThread;
    private Handler mEncoderHandler;
    private MediaCodec.BufferInfo mBufferInfo;
    private boolean isStarted = false;

    private EncodeEglSurface decodeSurface;
    private ReentrantLock encoderLock;

    public MyEncoder(VideoConfiguration configuration) {
        mVideoConfiguration = configuration;
    }

    public boolean startMediaCodec() {
        try {
            decodeSurface = new EncodeEglSurface(mMediaCodec.createInputSurface());
            mMediaCodec.start();
        } catch (Exception e) {
            releaseEncoder();
            throw (RuntimeException) e;
        }
        return true;
    }

    public void makeCurrent() {
        decodeSurface.makeCurrent();
    }

    public void swapBuffers() {
        decodeSurface.swapBuffers();
        decodeSurface.setPresentationTime(System.nanoTime());
    }

    public void prepareEncoder() {
        mMediaCodec = VideoMediaCodec.getVideoMediaCodec(mVideoConfiguration);
        mHandlerThread = new HandlerThread("video_encode");
        mHandlerThread.start();
        mEncoderHandler = new Handler(mHandlerThread.getLooper());
        mBufferInfo = new MediaCodec.BufferInfo();
        isStarted = true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public boolean setEncoderBps(int bps) {
        Bundle bundle = new Bundle();
        bundle.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, bps * 1024);
        mMediaCodec.setParameters(bundle);
        return true;
    }

    public void startSwapData() {
        mEncoderHandler.post(swapDataRunnable);
    }

    private void encodeRun() {
        ByteBuffer[] outBuffers = mMediaCodec.getOutputBuffers();
        while (isStarted) {
            encoderLock.lock();
            if (mMediaCodec != null) {
                int outBufferIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo, 12000);
                if (outBufferIndex > 0) {
                    ByteBuffer bb = outBuffers[outBufferIndex];
                    if (mListener != null) {
                        mListener.onVideoEncode(bb, mBufferInfo);
                    }
                    mMediaCodec.releaseOutputBuffer(outBufferIndex, false);
                } else {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                encoderLock.unlock();
            } else {
                encoderLock.unlock();
                break;
            }
        }
    }

    public void stop() {
        isStarted = false;
        mEncoderHandler.removeCallbacks(null);
        mHandlerThread.quit();
        encoderLock.lock();
        releaseEncoder();
        encoderLock.unlock();
    }

    private void releaseEncoder() {
        if (mMediaCodec != null) {
            mMediaCodec.signalEndOfInputStream();
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
        }
        if (mHandlerThread != null) {
            mHandlerThread.getLooper().quitSafely();
        }
        if (decodeSurface != null) {
            decodeSurface.release();
            decodeSurface = null;
        }
    }

    private Runnable swapDataRunnable = new Runnable() {
        @Override
        public void run() {
            encodeRun();
        }
    };

    private OnVideoEncodeListener mListener;

    public void setVideoEncodeListener(OnVideoEncodeListener listener) {
        mListener = listener;
    }
}










