package cn.live.livetest.stream.packer;

import android.media.MediaCodec;

import java.nio.ByteBuffer;

public class FlvPacker implements Packer, AnnexbHelper.AnnexbNaluListener {

    public static final int HEADER = 0;
    public static final int METADATA = 1;
    public static final int FIRST_VIDEO = 2;
    public static final int FIRST_AUDIO = 3;
    public static final int AUDIO = 4;
    public static final int KET_FRAME = 5;
    public static final int INTER_FRAME = 6;

    private OnPacketListener packetListener;
    private boolean isHeaderWrite;
    private boolean isKeyFrameWrite;

    private long mStartTime;
    private int mVideoWidth, mVideoHeight, mVideoFps;
    private int mAudioSampleRate, mAudioSampleSize;
    private boolean mIsStereo;

    private AnnexbHelper mAnnexbHelper;

    public FlvPacker() {
        mAnnexbHelper = new AnnexbHelper();
    }

    @Override
    public void setPacketListener(OnPacketListener listener) {
        packetListener = listener;
    }

    @Override
    public void onVideoData(ByteBuffer bb, MediaCodec.BufferInfo bi) {
        mAnnexbHelper.analyseVideoData(bb, bi);
    }

    @Override
    public void onAudioData(ByteBuffer bb, MediaCodec.BufferInfo bi) {

    }

    @Override
    public void start() {
        mAnnexbHelper.setAnnexbNaluListener(this);
    }

    @Override
    public void stop() {
        isHeaderWrite = false;
        isKeyFrameWrite = false;
        mAnnexbHelper.stop();
    }

    @Override
    public void onSpsPps(byte[] sps, byte[] pps) {

    }

    @Override
    public void onVideo(byte[] data, boolean isKeyFrame) {
        if (packetListener == null || !isHeaderWrite) {
            return;
        }
        int compositionTime = (int) (System.currentTimeMillis() - mStartTime);
        int packetType = INTER_FRAME;
        if (isKeyFrame) {
            isHeaderWrite = true;
            packetType = KET_FRAME;
        }
        if (!isKeyFrameWrite) {
            return;
        }
        int videoPacketSize=VIDEO_
    }

    public void initAudioParams(int sampleRate, int sampleSize, boolean isStereo) {
        mAudioSampleRate = sampleRate;
        mAudioSampleSize = sampleSize;
        mIsStereo = isStereo;
    }

    public void initVideoParams(int width, int height, int fps) {
        mVideoWidth = width;
        mVideoHeight = height;
        mVideoFps = fps;
    }
}
































