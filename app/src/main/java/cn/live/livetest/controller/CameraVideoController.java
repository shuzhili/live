package cn.live.livetest.controller;

import android.os.Build;
import android.os.Bundle;

import cn.live.livetest.camera.encode.MyEncoder;
import cn.live.livetest.camera.encode.OnVideoEncodeListener;
import cn.live.livetest.camera.render.MyRender;
import cn.live.livetest.camera.video.VideoConfiguration;

public class CameraVideoController implements IVideoController {
    private MyRender myRender;
    private MyEncoder myEncoder;
    private VideoConfiguration mVideoConfiguration = VideoConfiguration.createDefault();
    private OnVideoEncodeListener mListener;

    public CameraVideoController(MyRender render) {
        myRender = render;
        myRender.setVideoConfiguration(mVideoConfiguration);
    }


    @Override
    public void start() {
        if (mListener == null) {
            return;
        }
        myEncoder = new MyEncoder(mVideoConfiguration);
        myEncoder.setVideoEncodeListener(mListener);
        myEncoder.startMediaCodec();
        myRender.setVideoEncoder(myEncoder);
    }

    @Override
    public void stop() {
        myRender.setVideoEncoder(null);
        if (myEncoder != null) {
            myEncoder.setVideoEncodeListener(null);
            myEncoder.stop();
            myEncoder = null;
        }
    }

    @Override
    public void pause() {
        if (myEncoder != null) {
            myEncoder.setPause(true);
        }
    }

    @Override
    public void resume() {
        if (myEncoder != null) {
            myEncoder.setPause(false);
        }
    }

    @Override
    public boolean setVideoBps(int bps) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return false;
        } else {
            if (myEncoder != null) {
                myEncoder.setEncoderBps(bps);
                return true;
            }
        }
        return false;
    }

    @Override
    public void setVideoEncoderListener(OnVideoEncodeListener listener) {
        mListener = listener;
    }

    @Override
    public void setVideoConfiguration(VideoConfiguration configuration) {
        this.mVideoConfiguration = configuration;
        myRender.setVideoConfiguration(mVideoConfiguration);
    }
}
