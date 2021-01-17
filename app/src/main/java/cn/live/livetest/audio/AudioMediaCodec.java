package cn.live.livetest.audio;

import android.media.MediaCodec;
import android.media.MediaFormat;

public class AudioMediaCodec {
    public static MediaCodec getAudioMediaCodec(AudioConfiguration configuration) {
        MediaFormat mediaFormat = MediaFormat.createAudioFormat(configuration.mime, configuration.frequency, configuration.channelCount);
        if (configuration.mime.equals(AudioConfiguration.DEFAULT_MIME)) {
            mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, configuration.aacProfile);
        }
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, configuration.maxBps * 1024);
        mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, configuration.frequency);
        int maxInputSize = AudioUtils.getRecordBufferSize(configuration);
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, maxInputSize);
        mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, configuration.channelCount);

        MediaCodec mediaCodec = null;
        try {
            mediaCodec = MediaCodec.createEncoderByType(configuration.mime);
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (Exception e) {
            e.printStackTrace();
            if (mediaCodec != null) {
                mediaCodec.stop();
                mediaCodec.release();
                mediaCodec = null;
            }
        }
        return mediaCodec;
    }
}
