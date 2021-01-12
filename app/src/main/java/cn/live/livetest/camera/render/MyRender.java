package cn.live.livetest.camera.render;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import cn.live.livetest.camera.CameraHolder;
import cn.live.livetest.camera.CameraUtils;

public class MyRender implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    private boolean isCameraOpen = false;
    private int mSurfaceTextureId = -1;
    private SurfaceTexture mSurfaceTexture;
    private GLSurfaceView mGLSurfaceView;

    private RenderScreen mRenderScreen;

    public MyRender(GLSurfaceView glSurfaceView) {
        mGLSurfaceView = glSurfaceView;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        initSurfaceTexture();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        startCameraPreview();
        if(isCameraOpen){
            if(mRenderScreen==null){
                initScreenTexture();
            }
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {

    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }

    private void initSurfaceTexture() {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        mSurfaceTextureId = textures[0];
        mSurfaceTexture = new SurfaceTexture(mSurfaceTextureId);
        mSurfaceTexture.setOnFrameAvailableListener(this);
        //去除深度测试
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        //去除背面渲染
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        //去除颜色混合
        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mSurfaceTextureId);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    }

    private void startCameraPreview() {
        try {
            CameraUtils.checkCameraService(mGLSurfaceView.getContext());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        CameraHolder.State state = CameraHolder.instance().getState();
        CameraHolder.instance().setSurfaceTexture(mSurfaceTexture);
        if (state != CameraHolder.State.PREVIEW) {
            try {
                CameraHolder.instance().openCamera();
                CameraHolder.instance().startPreview();
                isCameraOpen = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initScreenTexture(){
        mRenderScreen=new RenderScreen(mSurfaceTextureId);
    }
}

































