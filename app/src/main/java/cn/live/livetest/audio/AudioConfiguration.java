package cn.live.livetest.audio;

import android.media.AudioFormat;
import android.media.MediaCodecInfo;

public class AudioConfiguration {
    public static final int DEFAULT_FREQUENCY = 44100;
    public static final int DEFAULT_MAX_BPS = 64;
    public static final int DEFAULT_MIN_BPS = 32;
    public static final int DEFAULT_ADTS = 0;
    public static final String DEFAULT_MIME = "audio/mp4a-latm";
    public static final int DEFAULT_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public static final int DEFAULT_AAC_PROFILE = MediaCodecInfo.CodecProfileLevel.AACObjectLC;
    public static final int DEFAULT_CHANNEL_COUNT = 1;
    public static final boolean DEFAULT_AEC = false;
    public static final boolean DEFAULT_MEDIA_CODEC = true;
    //比特率（BitRate）：每秒音频占用的比特数量，单位是 bps（Bit Per Second）
    public final int minBps;
    public final int maxBps;
    public final int frequency;
    public final int encoding;
    public final int channelCount;
    public final int adts;
    public final int aacProfile;
    public final String mime;
    public final boolean aec;
    public final boolean mediaCodec;

    private AudioConfiguration(Builder builder) {
        this.minBps = builder.minBps;
        this.maxBps = builder.maxBps;
        this.frequency = builder.frequency;
        this.encoding = builder.encoding;
        this.channelCount = builder.channelCount;
        this.adts = builder.adts;
        this.aacProfile = builder.aacProfile;
        this.mime = builder.mime;
        this.aec = builder.aec;
        this.mediaCodec = builder.mediaCodec;
    }

    public static AudioConfiguration createDefault() {
        return new Builder().build();
    }

    public static class Builder {
        public int minBps = DEFAULT_MIN_BPS;
        public int maxBps = DEFAULT_MAX_BPS;
        public int frequency = DEFAULT_FREQUENCY;
        public int encoding = DEFAULT_AUDIO_ENCODING;
        public int channelCount = DEFAULT_CHANNEL_COUNT;
        public int adts = DEFAULT_ADTS;
        public int aacProfile = DEFAULT_AAC_PROFILE;
        public String mime = DEFAULT_MIME;
        public boolean aec = DEFAULT_AEC;
        public boolean mediaCodec = DEFAULT_MEDIA_CODEC;

        public Builder setMinBps(int maxBps, int minBps) {
            this.maxBps = maxBps;
            this.minBps = minBps;
            return this;
        }

        public Builder setFrequency(int frequency) {
            this.frequency = frequency;
            return this;
        }

        public Builder setEncoding(int encoding) {
            this.encoding = encoding;
            return this;
        }

        public Builder setChannelCount(int channelCount) {
            this.channelCount = channelCount;
            return this;
        }

        public Builder setAdts(int adts) {
            this.adts = adts;
            return this;
        }

        public Builder setAacProfile(int aacProfile) {
            this.aacProfile = aacProfile;
            return this;
        }

        public Builder setMime(String mime) {
            this.mime = mime;
            return this;
        }

        public Builder setAec(boolean aec) {
            this.aec = aec;
            return this;
        }

        public Builder setMediaCodec(boolean mediaCodec) {
            this.mediaCodec = mediaCodec;
            return this;
        }

        public AudioConfiguration build() {
            return new AudioConfiguration(this);
        }
    }
}


















