package cn.live.livetest.camera.encode;

import android.annotation.TargetApi;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import cn.live.livetest.camera.CameraData;
import cn.live.livetest.camera.CameraHolder;
import cn.live.livetest.camera.render.GlUtil;

@TargetApi(18)
public class EncodeRenderSurfaceTexture {

    private final FloatBuffer mNormalVtxBuf = GlUtil.createVertexBuffer();
    private final FloatBuffer mNormalTexCoordBuf = GlUtil.createTexCoordBuffer();

    private FloatBuffer mCameraTexCoordBuffer;

    private final float[] mSymmetryMtx = GlUtil.createIdentityMtx();
    private final float[] mNormalMtx = GlUtil.createIdentityMtx();

    private int mFboTexId;
    private MyEncoder myEncoder;

    private EGLDisplay mSavedEglDisplay;
    private EGLSurface mSavedEglDrawSurface;
    private EGLSurface mSavedEglReadSurface;
    private EGLContext mSavedEglContext;

    private int mProgram = -1;
    private int maPositionHandle = -1;
    private int maTexCoordHandle = -1;
    private int muSamplerHandle = -1;
    private int muPosMtxHandle = -1;

    private int mVideoWidth = 720;
    private int mVideoHeight = 1280;

    public EncodeRenderSurfaceTexture(MyEncoder encoder, int fboId) {
        this.mFboTexId = fboId;
        this.myEncoder = encoder;
        setVideoSize(720,1280);
    }

    public void setTextureId(int textureId) {
        mFboTexId = textureId;
    }

    public void setVideoSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        initCameraTexCoordBuffer();
    }

    private void initCameraTexCoordBuffer() {
        int cameraWidth, cameraHeight;
        CameraData cameraData = CameraHolder.instance().getCameraData();
        int width = cameraData.cameraWidth;
        int height = cameraData.cameraHeight;
        if(CameraHolder.instance().isLandscape()) {
            cameraWidth = Math.max(width, height);
            cameraHeight = Math.min(width, height);
        } else {
            cameraWidth = Math.min(width, height);
            cameraHeight = Math.max(width, height);
        }
        float hRatio = mVideoWidth / ((float)cameraWidth);
        float vRatio = mVideoHeight / ((float)cameraHeight);

        float ratio;
        if(hRatio > vRatio) {
            ratio = mVideoHeight / (cameraHeight * hRatio);
            final float vtx[] = {
                    //UV
                    0f, 0.5f + ratio/2,
                    0f, 0.5f - ratio/2,
                    1f, 0.5f + ratio/2,
                    1f, 0.5f - ratio/2,
            };
            ByteBuffer bb = ByteBuffer.allocateDirect(4 * vtx.length);
            bb.order(ByteOrder.nativeOrder());
            mCameraTexCoordBuffer = bb.asFloatBuffer();
            mCameraTexCoordBuffer.put(vtx);
            mCameraTexCoordBuffer.position(0);
        } else {
            ratio = mVideoWidth/ (cameraWidth * vRatio);
            final float vtx[] = {
                    //UV
                    0.5f - ratio/2, 1f,
                    0.5f - ratio/2, 0f,
                    0.5f + ratio/2, 1f,
                    0.5f + ratio/2, 0f,
            };
            ByteBuffer bb = ByteBuffer.allocateDirect(4 * vtx.length);
            bb.order(ByteOrder.nativeOrder());
            mCameraTexCoordBuffer = bb.asFloatBuffer();
            mCameraTexCoordBuffer.put(vtx);
            mCameraTexCoordBuffer.position(0);
        }
    }

    public void draw() {
        saveRenderState();
        {
            if (myEncoder.startMediaCodec()) {
                myEncoder.startSwapData();
                myEncoder.makeCurrent();
                initGL();
            } else {
                myEncoder.makeCurrent();
            }

            GLES20.glViewport(0, 0, 720, 1280);
            GLES20.glClearColor(0f, 0f, 0f, 1f);
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

            GLES20.glUseProgram(mProgram);

            mNormalVtxBuf.position(0);
            GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
                    4 * 3, mNormalVtxBuf);
            GLES20.glEnableVertexAttribArray(maPositionHandle);

            mCameraTexCoordBuffer.position(0);
            GLES20.glVertexAttribPointer(maTexCoordHandle,2,GLES20.GL_FLOAT,false,
                    4*2,mCameraTexCoordBuffer);
            GLES20.glEnableVertexAttribArray(maTexCoordHandle);

            GLES20.glUniform1i(muPosMtxHandle,0);

            CameraData cameraData=CameraHolder.instance().getCameraData();
            if(cameraData!=null){
                int facing=cameraData.cameraFacing;
                if(muPosMtxHandle>=0){
                    if(facing==CameraData.FACING_FRONT){
                        GLES20.glUniformMatrix4fv(muPosMtxHandle,1,false,mSymmetryMtx,0);
                    }else{
                        GLES20.glUniformMatrix4fv(muPosMtxHandle,1,false,mNormalMtx,0);
                    }
                }
            }
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mFboTexId);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);

            myEncoder.swapBuffers();
        }
        restoreRenderState();
    }

    private void initGL() {
        final String vertexShader = "attribute vec4 position;\n" +
                "varying vec2 textureCoordinate;\n" +
                "uniform mat4 uPosMtx;\n" +
                "void main(){\n" +
                " gl_Position=uPosMtx*position;\n" +
                " textureCoordinate = inputTextureCoordinate.xy;\n" +
                "}\n";
        final String fragmentShader = "precision mediump float;\n" +
                "uniform sampler2D uSampler;\n" +
                "varying vec2 textureCoordinate;\n" +
                "void main(){\n" +
                "  gl_FragmentColor=texture2D(uSampler,textureCoordinate);\n" +
                "}\n";

    }

    private void saveRenderState() {
        mSavedEglDisplay = EGL14.eglGetCurrentDisplay();
        mSavedEglDrawSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW);
        mSavedEglReadSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_READ);
        mSavedEglContext = EGL14.eglGetCurrentContext();
    }

    private void restoreRenderState() {
        if (!EGL14.eglMakeCurrent(mSavedEglDisplay, mSavedEglDrawSurface,
                mSavedEglReadSurface, mSavedEglContext)) {
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }
}




























