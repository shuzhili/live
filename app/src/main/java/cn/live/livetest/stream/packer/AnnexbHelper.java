package cn.live.livetest.stream.packer;

import android.media.MediaCodec;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * 在H.264/AVC视频编码标准中，整个系统框架被分为了两个层面：视频编码层面（VCL）和网络抽象层面（NAL）。
 * 其中，前者负责有效表示视频数据的内容，而后者则负责格式化数据并提供头信息，以保证数据适合各种信道和存储介质上的传输。
 * NAL单元是NAL的基本语法结构，它包含一个字节的头信息和一系列来自VCL的称为原始字节序列载荷（RBSP）的字节流。
 * 0：未规定
 * 1：非IDR图像中不采用数据划分的片段
 * 2：非IDR图像中A类数据划分片段
 * 3：非IDR图像中B类数据划分片段
 * 4：非IDR图像中C类数据划分片段
 * 5：IDR图像的片段
 * 6：补充增强信息（SEI）
 * 7：序列参数集（SPS）
 * 8：图像参数集（PPS）
 * 9：分割符
 * 10：序列结束符
 * 11：流结束符
 * 12：填充数据
 * 13：序列参数集扩展
 * 14：带前缀的NAL单元
 * 15：子序列参数集
 * 16 – 18：保留
 * 19：不采用数据划分的辅助编码图像片段
 * 20：编码片段扩展
 * 21 – 23：保留
 * 24 – 31：未规定
 * <p>
 * [StartCode][NALU Header][NALU Payload]三部分=NALU单元
 * StartCode，是一个NALU单元开始，必须是00 00 00 01 或者00 00 01。
 * 一个NALU = 一组对应于视频编码的NALU头部信息 + 一个原始字节序列负荷(RBSP,Raw Byte Sequence Payload).
 * NALUHeader=1bit(fobbiden_zero_bit)+2bit(NRI 0-3 nalu重要性)+5bit(nalu type)
 * 0x1F=0001 111&NALUHADER=type
 * 00 00 00 01 06: SEI信息
 * 00 00 00 01 67: 0x67&0x1f = 0x07 :SPS
 * 00 00 00 01 68: 0x68&0x1f = 0x08 :PPS
 * 00 00 00 01 65: 0x65&0x1f = 0x05: IDR Slice
 */

public class AnnexbHelper {
    //nalu_type 对应h264
    public final static int NonIDR = 1;
    public final static int IDR = 5;
    public final static int SEI = 6;
    public final static int SPS = 7;
    public final static int PPS = 8;
    public final static int AccessUnitDelimiter = 9;

    private byte[] mPps;
    private byte[] mSps;
    private boolean mUploadPpsSps = true;

    public void analyseVideoData(ByteBuffer bb,MediaCodec.BufferInfo bi){
        bb.position(bi.offset);
        bb.limit(bi.offset+bi.size);

        ArrayList<byte[]> frames=new ArrayList<>();
        boolean isKeyFrame=false;
        while(bb.position()<bi.offset+bi.size){
            byte[] frame=annexbDemux(bb,bi);
            if(frame==null){
                break;
            }
            if(isAccessUnitDelimiter(frame)){
                continue;
            }
            if(isPps(frame)){
                mPps=frame;
                continue;
            }
            if(isSps(frame)){
                mSps=frame;
                continue;
            }
            if(isKeyFrame(frame)){
                isKeyFrame=true;
            }else {
                isKeyFrame=false;
            }

            byte[] naluHeader=buildNaluHeader(frame.length);
            frames.add(naluHeader);
            frames.add(frame);
        }
    }

    //从硬编数据取出一帧nal
    private byte[] annexbDemux(ByteBuffer bb, MediaCodec.BufferInfo bi) {
        AnnexbSearch annexbSearch = new AnnexbSearch();
        avcStartWithAnnexb(annexbSearch, bb, bi);
        if (!annexbSearch.match || annexbSearch.startCode < 3) {
            return null;
        }
        for (int i = 0; i < annexbSearch.startCode; i++) {
            bb.get();
        }
        //slice方法的作用，就是做数据分割，将当前的position到limit之间的数据分割出来，
        // 返回一个新的ByteBuffer,同时,mark标记重置为-1。不过这里注意，
        // 分割出来的数据的容量刚好就是数据长度，而不是被分割之前的长度。
        ByteBuffer frameBuffer = bb.slice();
        int pos = bb.position();

        while (bb.position() < bi.offset + bi.size) {
            avcStartWithAnnexb(annexbSearch, bb, bi);
            if (annexbSearch.match) {
                break;
            }
            bb.get();
        }

        int size = bb.position() - pos;
        byte[] frameBytes = new byte[size];
        frameBuffer.get(frameBytes);
        return frameBytes;
    }

    //从硬编出的bytebuffer中查找nal
    private void avcStartWithAnnexb(AnnexbSearch as, ByteBuffer bb, MediaCodec.BufferInfo bi) {
        as.match = false;
        as.startCode = 0;
        int pos = bb.position();
        while (pos < bi.offset + bi.size - 3) {
            if (bb.get(pos) != 0x00 || bb.get(pos + 1) != 0x00) {
                break;
            }
            //match 00 00 00 01
            if (bb.get(pos + 2) == 0x01) {
                as.match = true;
                as.startCode = pos + 3 - bb.position();
                break;
            }
            pos++;
        }
    }
    //这里为啥是4 放入length是干啥的？
    private byte[] buildNaluHeader(int length) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(length);
        return buffer.array();
    }

    //序列参数集
    private boolean isSps(byte[] frame) {
        if (frame.length < 1) return false;
        return (frame[0] & 0x1f) == PPS;
    }

    //图像参数集
    private boolean isPps(byte[] frame) {
        if (frame.length < 1) return false;
        return (frame[0] & 0x1f) == PPS;
    }

    private boolean isKeyFrame(byte[] frame) {
        if (frame.length < 1) return false;
        return (frame[0] & 0x1f) == IDR;
    }

    private static boolean isAccessUnitDelimiter(byte[] frame) {
        if (frame.length < 1) return false;
        int nal_unit_type = (frame[0] & 0x1f);
        return nal_unit_type == AccessUnitDelimiter;
    }

    class AnnexbSearch {
        public int startCode = 0;
        public boolean match = false;
    }
}








































