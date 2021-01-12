package cn.live.livetest.camera;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

import java.util.Iterator;
import java.util.List;

import cn.live.livetest.Constants;


public class CameraCapture implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private static final String TAG = "CameraCapture";

    private Camera mCamera;
    private int mRotation;
    private int cameraId;
    private int width;
    private int height;

    private byte[] buffer;
    private byte[] bytes;

    private SurfaceHolder mSurfaceHolder;
    private Camera.PreviewCallback mPreviewCallback;

    public void setPreviewDisplay(SurfaceHolder surfaceHolder) {
        mSurfaceHolder = surfaceHolder;
        mSurfaceHolder.addCallback(this);
    }

    public CameraCapture(int rotation, int cameraId, int width, int height) {
        this.mRotation = rotation;
        this.cameraId = cameraId;
        this.width = width;
        this.height = height;
    }

    public void switchCamera() {
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        stopPreview();
        startPreview();

    }

    private void stopPreview() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private void startPreview() {
        try {
            mCamera = Camera.open(cameraId);
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewFormat(ImageFormat.NV21);
            setPreviewSize(parameters);
            setPreviewOrientation(mRotation);
            mCamera.setParameters(parameters);
            buffer = new byte[width * height * 3 / 2];
            bytes = new byte[buffer.length];
            mCamera.addCallbackBuffer(buffer);
            mCamera.setPreviewCallbackWithBuffer(this);
            mCamera.setPreviewDisplay(mSurfaceHolder);
            setFocusMode(true);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setFocusMode(boolean isTouchMode) {
        Constants.supportTouchFocus = supportTouchFocus(mCamera);
        if (!Constants.supportTouchFocus) {

        } else {
            if (!isTouchMode) {
                Constants.touchFocusMode = false;
                setAutoFocusMode(mCamera);
            } else {
                Constants.touchFocusMode = true;
            }
        }
    }

    public void setAutoFocusMode(Camera camera) {
        try {
            Camera.Parameters parameters = camera.getParameters();
            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes.size() > 0 && focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                camera.setParameters(parameters);
            } else if (focusModes.size() > 0) {
                parameters.setFocusMode(focusModes.get(0));
                camera.setParameters(parameters);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean supportTouchFocus(Camera camera) {
        if (camera != null) {
            return (camera.getParameters().getMaxNumFocusAreas() != 0);
        }
        return false;
    }

    private void setPreviewSize(Camera.Parameters parameters) {
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        Camera.Size size = supportedPreviewSizes.get(0);
        int m = Math.abs(size.height * size.width - width * height);
        supportedPreviewSizes.remove(0);
        Iterator<Camera.Size> iterator = supportedPreviewSizes.iterator();
        while (iterator.hasNext()) {
            Camera.Size next = iterator.next();
            int n = Math.abs(next.height * next.width - width * height);
            if (n < m) {
                m = n;
                size = next;
            }
        }
        width = size.width;
        height = size.height;
        parameters.setPreviewSize(width, height);
    }

    public void setPreviewOrientation(int orientation) {
        mRotation = orientation;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        int degrees = 0;
        switch (mRotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                mOnChangeSizeListener.onChange(height, width);
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                mOnChangeSizeListener.onChange(width, height);
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                mOnChangeSizeListener.onChange(width, height);
                break;
        }

        int result;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (cameraInfo.orientation - degrees + 360) % 360;
        }
        mCamera.setDisplayOrientation(result);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        stopPreview();
        startPreview();
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        stopPreview();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        switch (mRotation) {
            case Surface.ROTATION_0:
                rotation90(data);
                break;
            case Surface.ROTATION_90:
                bytes = data;
                break;
            case Surface.ROTATION_270:
                break;
        }
        mPreviewCallback.onPreviewFrame(bytes,camera);
        camera.addCallbackBuffer(buffer);
    }

    private void rotation90(byte[] data) {
        int index = 0;
        int ySize = width * height;

        int uvHeight = height / 2;
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            for (int i = 0; i < width; i++) {
                for (int j = height - 1; j >= 0; j--) {
                    bytes[index++] = data[width * j + i];
                }
            }

            //每次处理两个数据
            for (int i = 0; i < width; i += 2) {
                for (int j = uvHeight - 1; j >= 0; j--) {
                    // v
                    bytes[index++] = data[ySize + width * j + i];
                    // u
                    bytes[index++] = data[ySize + width * j + i + 1];
                }
            }
        }else{
            //逆时针旋转90度
            for (int i = 0; i < width; i++) {
                int nPos = width - 1;
                for (int j = 0; j < height; j++) {
                    bytes[index++] = data[nPos - i];
                    nPos += width;
                }
            }
            //u v
            for (int i = 0; i < width; i += 2) {
                int nPos = ySize + width - 1;
                for (int j = 0; j < uvHeight; j++) {
                    bytes[index++] = data[nPos - i - 1];
                    bytes[index++] = data[nPos - i];
                    nPos += width;
                }
            }
        }
    }

    private OnChangeSizeListener mOnChangeSizeListener;

    public void setOnChangeSizeListener(OnChangeSizeListener listener) {
        this.mOnChangeSizeListener = listener;
    }

    public interface OnChangeSizeListener {
        void onChange(int w, int h);
    }
}
