package cn.live.livetest.controller;

import android.media.MediaCodec;

import java.nio.ByteBuffer;

import cn.live.livetest.audio.AudioConfiguration;
import cn.live.livetest.audio.OnAudioEncodeListener;
import cn.live.livetest.camera.encode.OnVideoEncodeListener;
import cn.live.livetest.camera.video.VideoConfiguration;
import cn.live.livetest.stream.packer.Packer;
import cn.live.livetest.stream.sender.Sender;
import cn.live.livetest.utils.SopCastUtils;

public class StreamController implements OnAudioEncodeListener, OnVideoEncodeListener, Packer.OnPacketListener {
    private Packer mPacker;
    private Sender mSender;
    public IVideoController mVideoController;
    public IAudioController mAudioController;

    public StreamController(IVideoController videoController, IAudioController audioController) {
        this.mAudioController = audioController;
        this.mVideoController = videoController;
    }

    public void setVideoConfiguration(VideoConfiguration videoConfiguration) {
        mVideoController.setVideoConfiguration(videoConfiguration);
    }

    public void setAudioConfiguration(AudioConfiguration audioConfiguration) {
        mAudioController.setAudioConfiguration(audioConfiguration);
    }

    public void setPacker(Packer packer) {
        mPacker = packer;
        mPacker.setPacketListener(this);
    }

    public synchronized void start() {
        SopCastUtils.processNotUI(new SopCastUtils.INotUIProcessor() {
            @Override
            public void process() {
                if (mPacker == null) {
                    return;
                }
                if (mSender == null) {
                    return;
                }
                mPacker.start();
                mSender.start();
                mVideoController.setVideoEncoderListener(StreamController.this);
                mAudioController.setAudioEncodeListener(StreamController.this);
            }
        });
    }

    public synchronized void stop() {
        SopCastUtils.processNotUI(new SopCastUtils.INotUIProcessor() {
            @Override
            public void process() {
                mVideoController.setVideoEncoderListener(null);
                mAudioController.setAudioEncodeListener(null);
                mAudioController.stop();
                mVideoController.stop();
                if (mSender != null) {
                    mSender.stop();
                }
                if (mPacker != null) {
                    mPacker.stop();
                }
            }
        });
    }

    public synchronized void pause() {
        SopCastUtils.processNotUI(new SopCastUtils.INotUIProcessor() {
            @Override
            public void process() {
                mAudioController.pause();
                mVideoController.pause();
            }
        });
    }

    public synchronized void resume() {
        SopCastUtils.processNotUI(new SopCastUtils.INotUIProcessor() {
            @Override
            public void process() {
                mAudioController.resume();
                mVideoController.resume();
            }
        });
    }

    public void mute(boolean mute) {
        mAudioController.mute(mute);
    }

    public int getSessionId() {
        return mAudioController.getSessionId();
    }

    public boolean setVideoBps(int bps) {
        boolean b = mVideoController.setVideoBps(bps);
        return b;
    }

    @Override
    public void onAudioEncode(ByteBuffer bb, MediaCodec.BufferInfo bi) {
        if (mPacker != null) {
            mPacker.onAudioData(bb, bi);
        }
    }

    @Override
    public void onVideoEncode(ByteBuffer bb, MediaCodec.BufferInfo bi) {
        if (mPacker != null) {
            mPacker.onVideoData(bb, bi);
        }
    }

    @Override
    public void onPacket(byte[] data, int packetType) {
        if (mSender != null) {
            mSender.onData(data, packetType);
        }
    }


}
