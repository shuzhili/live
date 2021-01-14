package cn.live.livetest.stream.packer;

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
 *
 * NaluHeader+RBSP=NALU
 * NALUHeader=1bit(fobbiden_zero_bit)+2bit(NRI 0-3 nalu重要性)+5bit(nalu type)
 * 0x1F=0001 111&NALUHADER=type
 *  00 00 00 01 06: SEI信息
 *  00 00 00 01 67: 0x67&0x1f = 0x07 :SPS
 *  00 00 00 01 68: 0x68&0x1f = 0x08 :PPS
 *  00 00 00 01 65: 0x65&0x1f = 0x05: IDR Slice
 *
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

    private static boolean isAccessUnitDelimiter(byte[] frame) {
        if (frame.length < 1) {
            return false;
        }
        int nal_unit_type = (frame[0] & 0x1f);
        return nal_unit_type == AccessUnitDelimiter;
    }
}








































