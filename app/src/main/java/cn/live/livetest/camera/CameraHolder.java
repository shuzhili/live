package cn.live.livetest.camera;

import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class CameraHolder {
    private static final String TAG = "CameraHolder";
    private final static int FOCUS_WIDTH = 80;
    private final static int FOCUS_HEIGHT = 80;

    private List<CameraData> mCameraDatas;
    private Camera mCameraDevice;
    private CameraData mCameraData;
    private State mState;
    private SurfaceTexture mTexture;
    private boolean isTouchMode = false;
    private boolean isOpenBackFirst = false;
    private CameraConfiguration mConfiguration = CameraConfiguration.createDefault();

    public enum State {
        INIT, OPENED, PREVIEW
    }

    private static CameraHolder sHolder;

    public static synchronized CameraHolder instance() {
        if (sHolder == null) {
            sHolder = new CameraHolder();
        }
        return sHolder;
    }

    private CameraHolder() {
        mState = State.INIT;
    }

    public int getNumberOfCameras() {
        return Camera.getNumberOfCameras();
    }

    public CameraData getCameraData() {
        return mCameraData;
    }

    public boolean isLandscape() {
        return (mConfiguration.orientation != CameraConfiguration.Orientation.PORTRAIT);
    }

    public synchronized Camera openCamera() {
        if (mCameraDatas == null || mCameraDatas.size() == 0) {
            mCameraDatas = CameraUtils.getAllCamerasData(isOpenBackFirst);
        }
        CameraData cameraData = mCameraDatas.get(0);
        if (mCameraDevice != null && mCameraData == cameraData) {
            return mCameraDevice;
        }
        if (mCameraDevice != null) {
            releaseCamera();
        }

        try {
            mCameraDevice = Camera.open(cameraData.cameraId);
        } catch (RuntimeException e) {
            Log.e(TAG, e.getMessage());
        }
        if (mCameraDevice == null) {
            Log.e(TAG, "mCameraDevice==null");
        }

        try {
            CameraUtils.initCameraParams(mCameraDevice, cameraData, isTouchMode, mConfiguration);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            mCameraDevice.release();
            mCameraDevice = null;
        }
        mCameraData = cameraData;
        mState = State.OPENED;
        return mCameraDevice;
    }

    public void setSurfaceTexture(SurfaceTexture texture) {
        mTexture = texture;
        if (mState == State.PREVIEW && mCameraDevice != null && mTexture != null) {
            try {
                mCameraDevice.setPreviewTexture(mTexture);
            } catch (Exception e) {
                releaseCamera();
            }
        }
    }

    public State getState() {
        return mState;
    }

    public void setConfiguration(CameraConfiguration configuration) {
        isTouchMode = (configuration.focusMode != CameraConfiguration.FocusMode.AUTO);
        isOpenBackFirst = (configuration.facing != CameraConfiguration.Facing.FRONT);
        mConfiguration = configuration;
    }

    public synchronized void startPreview() {
        if (mState != State.OPENED || mCameraDevice == null || mTexture == null) {
            return;
        }
        try {
            mCameraDevice.setPreviewTexture(mTexture);
            mCameraDevice.startPreview();
            mState = State.PREVIEW;
        } catch (Exception e) {
            releaseCamera();
        }
    }

    public synchronized void stopPreview() {
        if (mState != State.PREVIEW || mCameraDevice == null) {
            return;
        }
        mCameraDevice.setPreviewCallback(null);
        Camera.Parameters cameraParameters = mCameraDevice.getParameters();
        if (cameraParameters != null &&
                cameraParameters.getSupportedFlashModes() != null &&
                !cameraParameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)) {
            cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }
        mCameraDevice.setParameters(cameraParameters);
        mCameraDevice.stopPreview();
        mState = State.OPENED;
    }

    public synchronized void releaseCamera() {
        if (mState == State.PREVIEW) stopPreview();
        if(mState!=State.OPENED||mCameraDevice==null)return;
        mCameraDevice.release();
        mCameraDevice=null;
        mCameraData=null;
        mState=State.INIT;
        CameraUtils.stop();
    }
    public void release(){
        mCameraData=null;
        mTexture=null;
        isTouchMode=false;
        isOpenBackFirst=false;
        mConfiguration=CameraConfiguration.createDefault();
    }

    public void setFocusPoint(int x,int y){
        if(mState!=State.PREVIEW||mCameraDevice==null)return;
        if(x<-1000||x>1000||y<-1000||y>1000){
            return;
        }
        Camera.Parameters parameters=mCameraDevice.getParameters();
        if(parameters!=null&&parameters.getMaxNumFocusAreas()>0){
            List<Camera.Area> focusArea=new ArrayList<>();
            focusArea.add(new Camera.Area(new Rect(x,y,x+FOCUS_WIDTH,y+FOCUS_HEIGHT),1000));
            parameters.setFocusAreas(focusArea);
            try{
                mCameraDevice.setParameters(parameters);
            }catch (Exception e){

            }
        }
    }

    public boolean doAutofocus(Camera.AutoFocusCallback focusCallback){
        if(mState!=State.PREVIEW||mCameraDevice==null)return false;
        Camera.Parameters parameters=mCameraDevice.getParameters();
        if(parameters.isAutoExposureLockSupported()){
            parameters.setAutoExposureLock(false);
        }
        if(parameters.isAutoWhiteBalanceLockSupported()){
            parameters.setAutoWhiteBalanceLock(false);
        }
        mCameraDevice.setParameters(parameters);
        mCameraDevice.cancelAutoFocus();
        mCameraDevice.autoFocus(focusCallback);
        return true;
    }

    public void changeFocusMode(boolean touchMode){
        if(mState!=State.PREVIEW||mCameraDevice==null||mCameraData==null){
            return;
        }
        isTouchMode=touchMode;
        mCameraData.touchFocusMode=touchMode;
        if(touchMode){
            CameraUtils.setTouchFocusMode(mCameraDevice);
        }else{
            CameraUtils.setAutoFocusMode(mCameraDevice);
        }
    }

    public float cameraZoom(boolean isBig){
        if(mState!=State.PREVIEW||mCameraDevice==null||mCameraData==null){
            return -1;
        }
        Camera.Parameters parameters=mCameraDevice.getParameters();
        if(isBig){
            parameters.setZoom(Math.min(parameters.getZoom()+1,parameters.getZoom()));
        }else{
            parameters.setZoom(Math.max(parameters.getZoom()-1,0));
        }
        mCameraDevice.setParameters(parameters);
        return (float) parameters.getZoom()/parameters.getMaxZoom();
    }

    public boolean switchCamera(){
        if(mState!=State.PREVIEW){
            return false;
        }
        try{
            CameraData cameraData=mCameraDatas.remove(1);
            mCameraDatas.add(0,cameraData);
            openCamera();
            startPreview();
            return true;
        }catch (Exception e){
            CameraData cameraData=mCameraDatas.remove(1);
            mCameraDatas.add(0,cameraData);
            try{
                openCamera();
                startPreview();
            }catch (Exception e2){
                e2.printStackTrace();
            }
            return false;
        }
    }

    public boolean switchLight(){
        if(mState!=State.PREVIEW||mCameraDevice==null||mCameraData==null){
            return false;
        }

        if(!mCameraData.hasLight){
            return false;
        }
        Camera.Parameters cameraParameters=mCameraDevice.getParameters();
        if(cameraParameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)){
            cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        }else{
            cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }
        try {
            mCameraDevice.setParameters(cameraParameters);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 设置当屏幕发生改变需要设置的监听
     *
     * @param listener
     */
    public void setOnChangedSizeListener(CameraCapture.OnChangeSizeListener listener) {
        CameraUtils.setOnChangedSizeListener(listener);
    }

    public void setOnChangeSizeListener(){}

    public void setPreviewCallback(Camera.PreviewCallback previewCallback){
        CameraUtils.setPreviewCallback(previewCallback);
    }

    public void setPreviewOrientation(int rotation){
        CameraUtils.setPreviewOrientation(rotation);
    }
}






















