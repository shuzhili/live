package cn.live.livetest.controller;

import cn.live.livetest.camera.encode.OnVideoEncodeListener;
import cn.live.livetest.camera.video.VideoConfiguration;

public interface IVideoController {
    void start();

    void stop();

    void pause();

    void resume();

    boolean setVideoBps(int bps);

    void setVideoEncoderListener(OnVideoEncodeListener listener);

    void setVideoConfiguration(VideoConfiguration configuration);
}
