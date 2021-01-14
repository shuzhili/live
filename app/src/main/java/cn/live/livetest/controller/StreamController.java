package cn.live.livetest.controller;

import android.media.MediaCodec;

import java.nio.ByteBuffer;

import cn.live.livetest.camera.encode.OnVideoEncodeListener;
import cn.live.livetest.camera.video.VideoConfiguration;
import cn.live.livetest.stream.packer.Packer;

public class StreamController implements OnVideoEncodeListener , Packer.OnPacketListener {

    public IVideoController mVideoController;

    public StreamController(IVideoController videoController) {
        this.mVideoController = videoController;
    }

    public void setVideoConfiguration(VideoConfiguration videoConfiguration){
        mVideoController.setVideoConfiguration(videoConfiguration);
    }

    @Override
    public void onVideoEncode(ByteBuffer bb, MediaCodec.BufferInfo bi) {

    }

    @Override
    public void onPacket(byte[] data, int packetType) {

    }
}
