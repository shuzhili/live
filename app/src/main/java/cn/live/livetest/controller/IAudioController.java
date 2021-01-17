package cn.live.livetest.controller;

import cn.live.livetest.audio.AudioConfiguration;
import cn.live.livetest.audio.OnAudioEncodeListener;

public interface IAudioController {
    void start();
    void stop();
    void pause();
    void resume();
    void mute(boolean mute);
    int getSessionId();
    void setAudioConfiguration(AudioConfiguration audioConfiguration);
    void setAudioEncodeListener(OnAudioEncodeListener listener);
}
