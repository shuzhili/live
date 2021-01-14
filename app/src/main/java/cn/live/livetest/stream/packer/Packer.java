package cn.live.livetest.stream.packer;

import android.media.MediaCodec;

import java.nio.ByteBuffer;

public interface Packer {
    interface OnPacketListener{
        void onPacket(byte[] data,int packetType);
    }
    void setPacketListener(OnPacketListener listener);
    void onVideoData(ByteBuffer bb, MediaCodec.BufferInfo bi);
    void onAudioData(ByteBuffer bb,MediaCodec.BufferInfo bi);
    void start();
    void stop();

    interface OnAudioPacketListener extends OnPacketListener{
        int getInputSamples();
        void sendAudioInfo(int frequency,int channels);
    }
    interface OnVideoPacketListener extends OnPacketListener{

    }
}



















