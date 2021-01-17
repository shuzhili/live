package cn.live.livetest.stream.packer.flv;

import java.nio.ByteBuffer;

import cn.live.livetest.stream.amf.AmfMap;
import cn.live.livetest.stream.amf.AmfString;

public class FlvPackerHelper2 {
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

    public static void writeFlvTagHeader(ByteBuffer buffer, int type, int dataSize, int timestamp) {
        /**
         * 第1个byte为记录着tag的类型，音频（0x8），视频（0x9），脚本（0x12）；
         * 第2-4bytes是数据区的长度，UI24类型的值，也就是tag data的长度；注：这个长度等于最后的Tag Size-11
         * 第5-7个bytes是时间戳，UI24类型的值，单位是毫秒，类型为0x12脚本类型数据，则时间戳为0，时间戳控制着文件播放的速度，可以根据音视频的帧率类设置；
         * 第8个byte是扩展时间戳，当24位数值不够时，该字节作为最高位将时间戳扩展为32位值；
         * 时间戳占4个字节 其中第四个字节是高位 前三个字节是低位(每个tag的5~8字节)
         *
         * 第9-11个bytes是streamID，UI24类型的值，但是总为0；
         * tag header 长度为1+3+3+1+3=11。
         */
        int sizeAndType = (dataSize & 0x00FFFFFF) | ((type & 0x1f) << 24);
        buffer.putInt(sizeAndType);
        int time = ((timestamp << 8) & 0xFFFFFF00) | ((timestamp >> 24) & 0x000000FF);
        buffer.putInt(time);
        buffer.put((byte) 0);
        buffer.put((byte) 0);
        buffer.put((byte) 0);
    }

    public static byte[] writeFlvMetaData(int width, int height, int fps, int audioRate, int audioSize, boolean isStereo) {
        AmfString metaDataHeader = new AmfString("onMetaData", false);
        AmfMap amfMap = new AmfMap();
        amfMap.setProperty("width", width);
        amfMap.setProperty("height", height);
        amfMap.setProperty("framerate", fps);
        amfMap.setProperty("videocodecid", FlvVideoCodecID.AVC);
        amfMap.setProperty("audiosamplerate", audioRate);
        amfMap.setProperty("audiosamplesize", audioSize);
        if (isStereo) {
            amfMap.setProperty("stereo", true);
        } else {
            amfMap.setProperty("stereo", false);
        }
        amfMap.setProperty("audiocodecid", FlvAudio.AAC);
        int size = amfMap.getSize() + metaDataHeader.getSize();
        ByteBuffer amfBuffer = ByteBuffer.allocate(size);
        amfBuffer.put(metaDataHeader.getBytes());
        amfBuffer.put(amfMap.getBytes());
        return amfBuffer.array();
    }

    public static void writeFirstVideoTag(ByteBuffer buffer,byte[] sps,byte[] pps){
    }

    public class FlvVideoCodecID {
        public final static int Reserved = 0;
        public final static int Reserved1 = 1;
        public final static int Reserved2 = 9;

        public final static int Disabled = 8;

        public final static int SorensonH263 = 2;
        public final static int ScreenVideo = 3;
        public final static int On2VP6 = 4;
        public final static int On2VP6WithAlphaChannel = 5;
        public final static int ScreenVideoVersion2 = 6;
        public final static int AVC = 7;
    }

    public class FlvAudio {
        public final static int LINEAR_PCM = 0;
        public final static int AD_PCM = 1;
        public final static int MP3 = 2;
        public final static int LINEAR_PCM_LE = 3;
        public final static int NELLYMOSER_16_MONO = 4;
        public final static int NELLYMOSER_8_MONO = 5;
        public final static int NELLYMOSER = 6;
        public final static int G711_A = 7;
        public final static int G711_MU = 8;
        public final static int RESERVED = 9;
        public final static int AAC = 10;
        public final static int SPEEX = 11;
        public final static int MP3_8 = 14;
        public final static int DEVICE_SPECIFIC = 15;
    }

    //AAC规格有三种：LC-AAC（最基本的），HE-AAC（AACPlus v1），HE-AAC v2（AACPlus v2）
    public class FlvAACObjectType {
        public final static int Reserved = 0;
        public final static int AACMain = 1;
        public final static int AACLc = 2;
        public final static int AACSSR = 3;
        // AAC HE = LC+SBR
        public final static int AACHE = 5;
        // AAC HEv2 = LC+SBR+PS
        public final static int AACHEV2 = 29;
    }

    public class FlvAACProfile {
        public final static int Reserved = 3;
        public final static int Main = 0;
        public final static int LC = 1;
        public final static int SSR = 2;
    }

    public class FlvAudioSampleRate {
        public final static int Reserved = 15;
        public final static int R96000 = 0;
        public final static int R88200 = 1;
        public final static int R64000 = 2;
        public final static int R48000 = 3;
        public final static int R44100 = 4;
        public final static int R32000 = 5;
        public final static int R24000 = 6;
        public final static int R22050 = 7;
        public final static int R16000 = 8;
        public final static int R12000 = 9;
        public final static int R11025 = 10;
        public final static int R8000 = 11;
        public final static int R7350 = 12;
    }

    public class FlvAudioSampleSize {
        public final static int PCM_8 = 0;
        public final static int PCM_16 = 1;
    }

    public class FlvAudioSampleType {
        public final static int MONO = 0;
        public final static int STEREO = 1;
    }

    public class FlvMessageType {
        public final static int FLV = 0x100;
    }

    /**
     * Table 7-1 – NAL unit type codes, syntax element categories, and NAL unit type classes
     * H.264-AVC-ISO_IEC_14496-10-2012.pdf, page 83.
     */
    public class FlvAvcNaluType {
        // Unspecified
        public final static int Reserved = 0;

        // Coded slice of a non-IDR picture slice_layer_without_partitioning_rbsp( )
        public final static int NonIDR = 1;
        // Coded slice data partition A slice_data_partition_a_layer_rbsp( )
        public final static int DataPartitionA = 2;
        // Coded slice data partition B slice_data_partition_b_layer_rbsp( )
        public final static int DataPartitionB = 3;
        // Coded slice data partition C slice_data_partition_c_layer_rbsp( )
        public final static int DataPartitionC = 4;
        // Coded slice of an IDR picture slice_layer_without_partitioning_rbsp( )
        public final static int IDR = 5;
        // Supplemental enhancement information (SEI) sei_rbsp( )
        public final static int SEI = 6;
        // Sequence parameter set seq_parameter_set_rbsp( )
        public final static int SPS = 7;
        // Picture parameter set pic_parameter_set_rbsp( )
        public final static int PPS = 8;
        // Access unit delimiter access_unit_delimiter_rbsp( )
        public final static int AccessUnitDelimiter = 9;
        // End of sequence end_of_seq_rbsp( )
        public final static int EOSequence = 10;
        // End of stream end_of_stream_rbsp( )
        public final static int EOStream = 11;
        // Filler data filler_data_rbsp( )
        public final static int FilterData = 12;
        // Sequence parameter set extension seq_parameter_set_extension_rbsp( )
        public final static int SPSExt = 13;
        // Prefix NAL unit prefix_nal_unit_rbsp( )
        public final static int PrefixNALU = 14;
        // Subset sequence parameter set subset_seq_parameter_set_rbsp( )
        public final static int SubsetSPS = 15;
        // Coded slice of an auxiliary coded picture without partitioning slice_layer_without_partitioning_rbsp( )
        public final static int LayerWithoutPartition = 19;
        // Coded slice extension slice_layer_extension_rbsp( )
        public final static int CodedSliceExt = 20;
    }


    /**
     * 0 = Number type  //DOUBLE(8个字节的double数据)
     * 1 = Boolean type //UI8(1个字节)
     * 2 = String type   //SCRIPTDATASTRING
     * 3 = Object type  //SCRIPTDATAOBJECT[n]
     * 4 = MovieClip type  //SCRIPTDATASTRING
     * 5 = Null type
     * 6 = Undefined type
     * 7 = Reference type  //UI16(2个字节)
     * 8 = ECMA array type  //SCRIPTDATAVARIABLE[ECMAArrayLength]
     * 10 = Strict array type  //SCRIPTDATAVARIABLE[n]
     * 11 = Date type  //SCRIPTDATADATE
     * 12 = Long string type  //SCRIPTDATALONGSTRING
     */
    public class FlvMetaValueType {
        public final static int NumberType = 0;
        public final static int BooleanType = 1;
        public final static int StringType = 2;
        public final static int ObjectType = 3;
        public final static int MovieClipType = 4;
        public final static int NullType = 5;
        public final static int UndefinedType = 6;
        public final static int ReferenceType = 7;
        public final static int ECMAArrayType = 8;
        public final static int StrictArrayType = 10;
        public final static int DateType = 11;
        public final static int LongStringType = 12;
    }
}

































