package cn.live.livetest.camera;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class CameraUtils {

    private static Camera.PreviewCallback sPreviewCallback;
    private static CameraCapture.OnChangeSizeListener sScreenListener;
    private static int sRotation = 90;
    private static int sCameraId;
    private static int sWidth;
    private static int sHeight;
    private static Camera sCamera;

    public static List<CameraData> getAllCamerasData(boolean isBackFirst) {
        ArrayList<CameraData> cameraDatas = new ArrayList<>();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                CameraData cameraData = new CameraData(i, CameraData.FACING_FRONT);
                if (isBackFirst) {
                    cameraDatas.add(cameraData);
                } else {
                    cameraDatas.add(0, cameraData);
                }
            } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                CameraData cameraData = new CameraData(i, CameraData.FACING_BACK);
                if (isBackFirst) {
                    cameraDatas.add(0, cameraData);
                } else {
                    cameraDatas.add(cameraData);
                }
            }

        }

        return cameraDatas;
    }

    public static void initCameraParams(Camera camera, CameraData cameraData, boolean isTouchMode, CameraConfiguration configuration) {
        boolean isLandscape = (configuration.orientation != CameraConfiguration.Orientation.PORTRAIT);
        int cameraWidth = Math.max(configuration.height, configuration.width);
        int cameraHeight = Math.min(configuration.height, configuration.width);
        sWidth = cameraWidth;
        sHeight = cameraHeight;
        sRotation = configuration.rotation;
        sCameraId = configuration.facing.ordinal();
        sCamera = camera;

        Camera.Parameters parameters = camera.getParameters();
        setPreviewFormat(camera, parameters);
        setPreviewFps(camera, configuration.fps, parameters);
        setPreviewSize(camera, cameraData, cameraWidth, cameraHeight, parameters);

        setPreviewCallback();
        cameraData.hasLight = supportFlash(camera);
        setOrientation(cameraData, isLandscape, camera);
        setFocusMode(camera, cameraData, isTouchMode);
    }


    private static void setPreviewFormat(Camera camera, Camera.Parameters parameters) {
        try {
            parameters.setPreviewFormat(ImageFormat.NV21);
            camera.setParameters(parameters);
        } catch (Exception e) {

        }
    }

    private static void setPreviewFps(Camera camera, int fps, Camera.Parameters parameters) {
        if (BlackListHelper.deviceInFpsBlacklisted()) {
            fps = 15;
        }
        try {
            parameters.setPreviewFrameRate(fps);
            camera.setParameters(parameters);
        } catch (Exception e) {
        }
        int[] range = adaptPreviewFps(fps, parameters.getSupportedPreviewFpsRange());
        try {
            parameters.setPreviewFpsRange(range[0], range[1]);
            camera.setParameters(parameters);
        } catch (Exception e) {

        }
    }

    private static int[] adaptPreviewFps(int fps, List<int[]> supportedPreviewFpsRange) {
        fps *= 1000;
        int[] closestRange = supportedPreviewFpsRange.get(0);
        int measure = Math.abs(closestRange[0] - fps) + Math.abs(closestRange[1] - fps);
        for (int[] range : supportedPreviewFpsRange) {
            if (range[0] <= fps && range[1] >= fps) {
                int curMeasure = Math.abs(range[0] - fps) + Math.abs(range[1] - fps);
                if (curMeasure < measure) {
                    closestRange = range;
                    measure = curMeasure;
                }
            }
        }
        return closestRange;
    }

    private static void setPreviewSize(Camera camera, CameraData cameraData, int cameraWidth, int cameraHeight, Camera.Parameters parameters) {
        Camera.Size size = getOptimalPreviewSize(camera, cameraWidth, cameraHeight);
    }

    private static Camera.Size getOptimalPreviewSize(Camera camera, int cameraWidth, int cameraHeight) {
        Camera.Size optimalSize = null;
        double minHeightDiff = Double.MAX_VALUE;
        double minWidthDiff = Double.MAX_VALUE;
        List<Camera.Size> sizes = camera.getParameters().getSupportedPreviewSizes();
        if (sizes == null) return null;
        //找到宽度差距最小的
        for (Camera.Size size : sizes) {
            if (Math.abs(size.width - cameraWidth) < minWidthDiff) {
                minWidthDiff = Math.abs(size.width - cameraWidth);
            }
        }
        //在宽度差距最小的里面，找到高度差距最小的
        for (Camera.Size size : sizes) {
            if (Math.abs(size.width - cameraWidth) == minWidthDiff) {
                if (Math.abs(size.height - cameraHeight) < minHeightDiff) {
                    optimalSize = size;
                    minHeightDiff = Math.abs(size.height - cameraHeight);
                }
            }
        }
        return optimalSize;
    }

    private static void setPreviewCallback() {
        byte[] buffer = new byte[sHeight * sWidth * 3 / 2];
        sCamera.addCallbackBuffer(buffer);
        sCamera.setPreviewCallbackWithBuffer(myCallback);
    }

    private static boolean supportFlash(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes == null) {
            return false;
        }
        for (String flashMode : flashModes) {
            if (Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode)) {
                return true;
            }
        }
        return false;
    }

    private static void setOrientation(CameraData cameraData, boolean isLandscape, Camera camera) {
        int orientation = getDisplayOrientation(cameraData.cameraId);
        if (isLandscape) {
            orientation = orientation - 90;
        }
        camera.setDisplayOrientation(orientation);
    }

    private static int getDisplayOrientation(int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation + 360) % 360;

        }
        return result;
    }

    private static void setFocusMode(Camera camera, CameraData cameraData, boolean isTouchMode) {
        boolean supportTouchFocus = false;
        if (camera != null && camera.getParameters().getMaxNumFocusAreas() != 0) {
            supportTouchFocus = true;
        }
        cameraData.supportTouchFocus = supportTouchFocus;
        if(!cameraData.supportTouchFocus){
            setAutoFocusMode(camera);
        }else{
            if(!isTouchMode){
                cameraData.touchFocusMode=false;
                setAutoFocusMode(camera);
            }else{
                cameraData.touchFocusMode=true;
            }
        }
    }

    public static void setAutoFocusMode(Camera camera) {
        try {
            Camera.Parameters parameters=camera.getParameters();
            List<String> focusModes=parameters.getSupportedFocusModes();
            if (focusModes.size() > 0 && focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                camera.setParameters(parameters);
            } else if (focusModes.size() > 0) {
                parameters.setFocusMode(focusModes.get(0));
                camera.setParameters(parameters);
            }
        }catch (Exception e){

        }
    }

    public static void setTouchFocusMode(Camera camera) {
        try {
            Camera.Parameters parameters = camera.getParameters();
            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes.size() > 0 && focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                camera.setParameters(parameters);
            } else if (focusModes.size() > 0) {
                parameters.setFocusMode(focusModes.get(0));
                camera.setParameters(parameters);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static Camera.PreviewCallback myCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (sPreviewCallback != null) {
                sPreviewCallback.onPreviewFrame(data, camera);
            }
            camera.addCallbackBuffer(data);
        }
    };

    public static void stop() {
        if(sPreviewCallback!=null){
            sPreviewCallback=null;
        }
        if(sCamera!=null){
            sCamera.release();
            sCamera=null;
        }
    }


    public static void setPreviewCallback(Camera.PreviewCallback previewCallback) {
    }


    public static void setOnChangedSizeListener(CameraCapture.OnChangeSizeListener listener) {
        sScreenListener = listener;
    }

    public static void setPreviewOrientation(int rotation) {
        sRotation = rotation;
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(sCameraId, info);
        int degrees = 0;
        switch (sRotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                if (sScreenListener != null)
                    sScreenListener.onChange(sHeight, sWidth);
                break;
            case Surface.ROTATION_90: // 横屏 左边是头部(home键在右边)
                degrees = 90;
                if (sScreenListener != null)
                    sScreenListener.onChange(sWidth, sHeight);
                break;
            case Surface.ROTATION_270:// 横屏 头部在右边
                degrees = 270;
                if (sScreenListener != null)
                    sScreenListener.onChange(sWidth, sHeight);
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        //设置角度
        sCamera.setDisplayOrientation(result);
    }

    public static void checkCameraService(Context context) throws Exception {
        DevicePolicyManager devicePolicyManager=(DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if(devicePolicyManager.getCameraDisabled(null)){
            throw new Exception("cameraDisable");
        }
        List<CameraData> allCamerasData = getAllCamerasData(false);
        if(allCamerasData.size()==0){
            throw new Exception("no camera data");
        }
    }
}




















