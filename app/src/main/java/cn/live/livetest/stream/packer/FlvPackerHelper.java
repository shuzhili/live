package cn.live.livetest.stream.packer;

import java.nio.ByteBuffer;

public class FlvPackerHelper {
    public static final int FLV_HEAD_SIZE = 9;
    public static final int VIDEO_HEAD_SIZE = 5;
    public static final int AUDIO_HEAD_SIZE = 2;
    public static final int FLV_TAG_HEAD_SIZE = 11;
    public static final int PRE_SIZE = 4;
    public static final int AUDIO_SPECIFIC_CONFIG_SIZE = 2;
    public static final int VIDEO_SPECIFIC_CONFIG_EXTEND_SIZE = 11;

    public static void writeFlvHeader(ByteBuffer buffer, boolean hasVideo, boolean hasAudio) {
        byte[] signature = new byte[]{'F', 'L', 'V'};
        byte version = (byte) 0x01;
        byte videoFlag = hasVideo ? (byte) 0x01 : 0x00;
        byte audioFlag = hasAudio ? (byte) 0x04 : 0x00;
        byte flags = (byte) (videoFlag | audioFlag);
        byte[] offset = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x09};

        buffer.put(signature);//3字节 signature
        buffer.put(version);//1字节 version
        buffer.put(flags);//1字节 flag
        buffer.put(offset);//4字节 headSize
    }

    public static void writeFlvTagHeader(ByteBuffer buffer,int type,int dataSize,int timestamp){
        /**
         * 第1个byte为记录着tag的类型，音频（0x8），视频（0x9），脚本（0x12）；
         * 第2-4bytes是数据区的长度，UI24类型的值，也就是tag data的长度；注：这个长度等于最后的Tag Size-11
         * 第5-7个bytes是时间戳，UI24类型的值，单位是毫秒，类型为0x12脚本类型数据，则时间戳为0，时间戳控制着文件播放的速度，可以根据音视频的帧率类设置；
         * 第8个byte是扩展时间戳，当24位数值不够时，该字节作为最高位将时间戳扩展为32位值；
         * 第9-11个bytes是streamID，UI24类型的值，但是总为0；
         * tag header 长度为1+3+3+1+3=11。
         */
        int sizeAndType=
    }
}

































