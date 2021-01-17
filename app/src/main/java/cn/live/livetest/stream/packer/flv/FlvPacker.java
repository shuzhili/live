package cn.live.livetest.stream.packer.flv;

import android.media.MediaCodec;

import java.nio.ByteBuffer;

import cn.live.livetest.stream.packer.AnnexbHelper;
import cn.live.livetest.stream.packer.Packer;

import static cn.live.livetest.stream.packer.flv.FlvPackerHelper.AUDIO_HEADER_SIZE;
import static cn.live.livetest.stream.packer.flv.FlvPackerHelper.AUDIO_SPECIFIC_CONFIG_SIZE;
import static cn.live.livetest.stream.packer.flv.FlvPackerHelper.FLV_HEAD_SIZE;
import static cn.live.livetest.stream.packer.flv.FlvPackerHelper.FLV_TAG_HEADER_SIZE;
import static cn.live.livetest.stream.packer.flv.FlvPackerHelper.PRE_SIZE;
import static cn.live.livetest.stream.packer.flv.FlvPackerHelper.VIDEO_HEADER_SIZE;
import static cn.live.livetest.stream.packer.flv.FlvPackerHelper.VIDEO_SPECIFIC_CONFIG_EXTEND_SIZE;

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
        if (packetListener == null) return;
        //下面四个方法不懂
        writeFlvHeader();
        writeMateData();
        writeFirstVideoTag(sps, pps);
        writeFirstAudioTag();
        mStartTime = System.currentTimeMillis();
        isHeaderWrite = true;
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
        int videoPacketSize = VIDEO_HEADER_SIZE + data.length;
        int dataSize = videoPacketSize + FLV_TAG_HEADER_SIZE;
        int size = dataSize + PRE_SIZE;
        ByteBuffer buffer = ByteBuffer.allocate(size);
        FlvPackerHelper.writeFlvTagHeader(buffer, FlvPackerHelper.FlvTag.Video, videoPacketSize, compositionTime);
        FlvPackerHelper.writeH264Packet(buffer, data, isKeyFrame);
        buffer.putInt(dataSize);
        packetListener.onPacket(buffer.array(), packetType);
    }

    //封装flvheader flvHeadersize=9; PRE_SIZE=是干啥的？flv tag中的persize?
    private void writeFlvHeader() {
        int size = FLV_HEAD_SIZE + PRE_SIZE;//9
        ByteBuffer headerBuffer = ByteBuffer.allocate(size);
        //写入 flv+version+flags+DataOffset
        FlvPackerHelper.writeFlvHeader(headerBuffer, true, true);
        headerBuffer.putInt(0);
        packetListener.onPacket(headerBuffer.array(), HEADER);
    }

    //flv文件中的元信息，是一些描述flv文件各类属性的信息。这些信息以AMF格式保存在文件的起始部分
    private void writeMateData() {
        byte[] metaData = FlvPackerHelper.writeFlvMetaData(mVideoWidth, mVideoHeight,
                mVideoFps, mAudioSampleRate, mAudioSampleSize, mIsStereo);
        int dataSize = metaData.length + FLV_TAG_HEADER_SIZE;
        int size = dataSize + PRE_SIZE;
        ByteBuffer buffer = ByteBuffer.allocate(size);
        FlvPackerHelper.writeFlvTagHeader(buffer, FlvPackerHelper.FlvTag.Script, metaData.length, 0);
        buffer.put(metaData);
        buffer.putInt(dataSize);
        packetListener.onPacket(buffer.array(), METADATA);
    }

    private void writeFirstVideoTag(byte[] sps, byte[] pps) {
        int firstPacketSize = VIDEO_HEADER_SIZE + VIDEO_SPECIFIC_CONFIG_EXTEND_SIZE + sps.length + pps.length;
        int dataSize = firstPacketSize + FLV_TAG_HEADER_SIZE;
        int size = dataSize + PRE_SIZE;
        ByteBuffer buffer = ByteBuffer.allocate(size);
        FlvPackerHelper.writeFlvTagHeader(buffer, FlvPackerHelper.FlvTag.Video, firstPacketSize, 0);
        FlvPackerHelper.writeFirstVideoTag(buffer, sps, pps);
        buffer.putInt(dataSize);
        packetListener.onPacket(buffer.array(), FIRST_VIDEO);
    }

    private void writeFirstAudioTag() {
        int firstAudioPacketSize = AUDIO_SPECIFIC_CONFIG_SIZE + AUDIO_HEADER_SIZE;
        int dataSize = FLV_TAG_HEADER_SIZE + firstAudioPacketSize;
        int size = dataSize + PRE_SIZE;
        ByteBuffer buffer = ByteBuffer.allocate(size);
        FlvPackerHelper.writeFlvTagHeader(buffer, FlvPackerHelper.FlvTag.Audio, firstAudioPacketSize, 0);
        FlvPackerHelper.writeFirstAudioTag(buffer, mAudioSampleRate, mIsStereo, mAudioSampleSize);
        buffer.putInt(dataSize);
        packetListener.onPacket(buffer.array(), FIRST_AUDIO);
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
































