package cn.live.livetest.camera.render;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import cn.live.livetest.camera.CameraData;
import cn.live.livetest.camera.CameraHolder;

public class RenderScreen {
    private final FloatBuffer mNormalVtxBuf = GlUtil.createVertexBuffer();
    private final FloatBuffer mNormalTexCoordBuf = GlUtil.createTexCoordBuffer();
    private FloatBuffer mCameraTexCoordBuffer;

    private final float[] mPosMtx = GlUtil.createIdentityMtx();

    private int mFboTexId;

    private int mProgram = -1;
    private int mPositionHandler = -1;
    private int mInputTextureHandler = -1;
    private int muPosMtxHandler = -1;
    private int muSamplerHandler = -1;

    private int mScreenW;
    private int mScreenH;


    public RenderScreen(int id) {
        mFboTexId = id;
        initGL();
    }

    private void initGL() {
        if (GLES20.glGetError() != GLES20.GL_NO_ERROR) {
            return;
        }
        final String vertexShader =
                //
                "attribute vec4 position;\n" +//向量vec4
                        "attribute vec4 inputTextureCoordinate;\n" + //向量vec4
                        "uniform   mat4 uPosMtx;\n" +//数组mat4
                        "varying   vec2 textureCoordinate;\n" + //varying vec2
                        "void main() {\n" +
                        "  gl_Position = uPosMtx * position;\n" +
                        "  textureCoordinate   = inputTextureCoordinate.xy;\n" +
                        "}\n";
        final String fragmentShader =
                //
                "precision mediump float;\n" +
                        "uniform sampler2D uSampler;\n" +
                        "varying vec2  textureCoordinate;\n" +
                        "void main() {\n" +
                        "  gl_FragColor = texture2D(uSampler, textureCoordinate);\n" +
                        "}\n";

        mProgram = GlUtil.createProgram(vertexShader, fragmentShader);
        mPositionHandler = GLES20.glGetAttribLocation(mProgram, "position");
        mInputTextureHandler = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
        int uPosMtx = GLES20.glGetUniformLocation(mProgram, "uPosMtx");
        int uSampler = GLES20.glGetUniformLocation(mProgram, "uSampler");
        if (GLES20.glGetError() != GLES20.GL_NO_ERROR) {
            return;
        }
    }

    public void setTextureId(int textureId) {
        mFboTexId = textureId;

    }

    private void initCameraTexCoordBuffer() {
        int cameraWidth, cameraHeight;
        CameraData cameraData = CameraHolder.instance().getCameraData();
        int width = cameraData.cameraWidth;
        int height = cameraData.cameraHeight;

        if (CameraHolder.instance().isLandscape()) {
            cameraWidth = Math.max(width, height);
            cameraHeight = Math.min(width, height);
        } else {
            cameraWidth = Math.min(width, height);
            cameraHeight = Math.max(width, height);
        }

        float hRatio = mScreenW / ((float) cameraWidth);
        float vRatio = mScreenH / ((float) cameraHeight);

        final float vtx[] = {
                0f, 1f,
                0f, 0f,
                1f, 1f,
                1f, 0f
        };
        ByteBuffer bb = ByteBuffer.allocateDirect(4 * vtx.length);
        bb.order(ByteOrder.nativeOrder());
        mCameraTexCoordBuffer = bb.asFloatBuffer();
        mCameraTexCoordBuffer.put(vtx);
        mCameraTexCoordBuffer.position(0);
    }

    public void setScreenSize(int width, int height) {
        mScreenW = width;
        mScreenH = height;
        initCameraTexCoordBuffer();
    }

    public void draw() {
        if (mScreenW <= 0 || mScreenH <= 0) {
            return;
        }

        GLES20.glViewport(0, 0, mScreenW, mScreenH);

        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);

        mNormalVtxBuf.position(0);
        GLES20.glVertexAttribPointer(mPositionHandler, 3, GLES20.GL_FLOAT,
                false, 4 * 3, mNormalVtxBuf);
        GLES20.glEnableVertexAttribArray(mPositionHandler);

        mCameraTexCoordBuffer.position(0);
        GLES20.glVertexAttribPointer(mInputTextureHandler, 2, GLES20.GL_FLOAT,
                false, 4 * 2, mCameraTexCoordBuffer);

        GLES20.glUniformMatrix4fv(muPosMtxHandler,1,false,mPosMtx,0);
        GLES20.glUniform1i(muSamplerHandler,0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mFboTexId);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);
    }
}

























