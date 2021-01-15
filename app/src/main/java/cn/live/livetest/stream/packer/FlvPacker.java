package cn.live.livetest.stream.packer;

import android.media.MediaCodec;

import java.nio.ByteBuffer;

public class FlvPacker implements Packer,AnnexbHelper.AnnexbNaluListener{
    @Override
    public void setPacketListener(OnPacketListener listener) {

    }

    @Override
    public void onVideoData(ByteBuffer bb, MediaCodec.BufferInfo bi) {

    }

    @Override
    public void onAudioData(ByteBuffer bb, MediaCodec.BufferInfo bi) {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void onSpsPps(byte[] sps, byte[] pps) {

    }

    @Override
    public void onVideo(byte[] data, boolean isKeyFrame) {

    }
}








