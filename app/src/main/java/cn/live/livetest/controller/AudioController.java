package cn.live.livetest.controller;

import android.media.AudioRecord;

import cn.live.livetest.audio.AudioConfiguration;
import cn.live.livetest.audio.AudioProcessor;
import cn.live.livetest.audio.AudioUtils;
import cn.live.livetest.audio.OnAudioEncodeListener;

public class AudioController implements IAudioController {

    private OnAudioEncodeListener mListener;
    private AudioRecord mAudioRecord;
    private AudioProcessor mAudioProcessor;
    private boolean mMute;
    private AudioConfiguration mAudioConfiguration;

    public AudioController() {
        mAudioConfiguration = AudioConfiguration.createDefault();
    }


    @Override
    public void start() {
        mAudioRecord = AudioUtils.getAudioRecord(mAudioConfiguration);
        try {
            mAudioRecord.startRecording();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mAudioProcessor = new AudioProcessor(mAudioRecord, mAudioConfiguration);
        mAudioProcessor.setAudioEncoderListener(mListener);
        mAudioProcessor.start();
        mAudioProcessor.setMute(mMute);
    }

    @Override
    public void stop() {
        if (mAudioProcessor != null) {
            mAudioProcessor.stopEncode();
        }
        if (mAudioRecord != null) {
            try {
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void pause() {
        if (mAudioRecord != null) {
            mAudioRecord.stop();
        }
        if (mAudioProcessor != null) {
            mAudioProcessor.pauseEncode(true);
        }
    }

    @Override
    public void resume() {
        if (mAudioRecord != null) {
            mAudioRecord.startRecording();
        }
        if (mAudioProcessor != null) {
            mAudioProcessor.pauseEncode(false);
        }
    }

    @Override
    public void mute(boolean mute) {
        mMute = mute;
        if (mAudioProcessor != null) {
            mAudioProcessor.setMute(mute);
        }
    }

    @Override
    public int getSessionId() {
        if (mAudioRecord != null) {
            return mAudioRecord.getAudioSessionId();
        } else {
            return -1;
        }
    }

    @Override
    public void setAudioConfiguration(AudioConfiguration audioConfiguration) {
        this.mAudioConfiguration = mAudioConfiguration;
    }

    @Override
    public void setAudioEncodeListener(OnAudioEncodeListener listener) {
        mListener = listener;
    }
}
