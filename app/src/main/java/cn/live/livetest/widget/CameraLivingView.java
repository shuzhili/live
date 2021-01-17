package cn.live.livetest.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.PowerManager;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cn.live.livetest.audio.AudioConfiguration;
import cn.live.livetest.camera.CameraConfiguration;
import cn.live.livetest.camera.CameraHolder;
import cn.live.livetest.camera.CameraListener;
import cn.live.livetest.camera.CameraView;
import cn.live.livetest.camera.video.VideoConfiguration;
import cn.live.livetest.controller.AudioController;
import cn.live.livetest.controller.CameraVideoController;
import cn.live.livetest.controller.StreamController;
import cn.live.livetest.stream.packer.Packer;
import cn.live.livetest.stream.sender.Sender;
import cn.live.livetest.stream.sender.rtmp.RtmpNativeSender;
import cn.live.livetest.utils.WeakHandler;

public class CameraLivingView extends CameraView {

    public static final String TAG = "CameraLivingView";
    private Context mContext;
    private PowerManager.WakeLock mWakeLock;
    private VideoConfiguration mVideoConfiguration = VideoConfiguration.createDefault();
    private AudioConfiguration mAudioConfiguration = AudioConfiguration.createDefault();
    private CameraListener mOutCameraOpenListener;
    private LivingStartListener mLivingStartListener;
    private WeakHandler mHandler = new WeakHandler();
    private StreamController mStreamController;

    public interface LivingStartListener {
        void startError(int error);

        void startSuccess();
    }

    public CameraLivingView(@NonNull Context context) {
        this(context, null);
    }

    public CameraLivingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraLivingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        CameraVideoController videoController = new CameraVideoController(render);
        AudioController audioController = new AudioController();
        mStreamController = new StreamController(videoController, audioController);
        render.setCameraOpenListener(mOutCameraOpenListener);
    }

    @SuppressLint("InvalidWakeLockTag")
    public void init() {
        PowerManager powerManager = (PowerManager) mContext.getSystemService(getContext().POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                PowerManager.ON_AFTER_RELEASE, TAG);
    }

    public void setLivingStartListener(LivingStartListener listener) {
        mLivingStartListener = listener;
    }

    public void setPacker(Packer packer) {
        mStreamController.setPacker(packer);
    }

    public void setSender(Sender sender) {
        mStreamController.setSender(sender);
        if (sender instanceof RtmpNativeSender) {
            RtmpNativeSender rtmpNativeSender = (RtmpNativeSender) sender;
            rtmpNativeSender.setMediaCodec(true);
        }
    }

    public void setVideoConfiguration(VideoConfiguration videoConfiguration) {
        mVideoConfiguration = videoConfiguration;
        mStreamController.setVideoConfiguration(videoConfiguration);
    }
    public void setAudioConfiguration(AudioConfiguration audioConfiguration){
        mAudioConfiguration=audioConfiguration;
        mStreamController.setAudioConfiguration(audioConfiguration);
    }
    public void setCameraConfiguration(CameraConfiguration cameraConfiguration){
        CameraHolder.instance().setConfiguration(cameraConfiguration);
    }

}
