package cn.live.livetest.camera.render;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.FloatBuffer;

import cn.live.livetest.camera.CameraData;
import cn.live.livetest.camera.CameraHolder;

public class FboRender {
    private final FloatBuffer mVtxBuf = GlUtil.createSquareVtx();
    private final float[] mPosMtx = GlUtil.createIdentityMtx();

    protected int mTextureId = -1;

    private int mProgram = -1;
    private int maPositionHandle = -1;
    private int maTexCoordHandle = -1;
    private int muPosMtxHandle = -1;
    private int muTexMtxHandle = -1;

    private final int[] mFboId = new int[]{0};
    private final int[] mRboId = new int[]{0};
    private final int[] mTexId = new int[]{0};

    private String mVertex;
    private String mFragment;

    private int mWidth=720;
    private int mHeight=1280;

    public FboRender() {
    }

    public void prepare() {
        mVertex = RenderShaderText.SHARDE_NULL_VERTEX;
        mFragment = RenderShaderText.SHARDE_NULL_FRAGMENT;
        loadShaderAndParams();
        createFboTexture();
    }

    private void initSize() {
        if (CameraHolder.instance().getState() != CameraHolder.State.PREVIEW) {
            return;
        }
        CameraData cameraData = CameraHolder.instance().getCameraData();
        int width = cameraData.cameraWidth;
        int height = cameraData.cameraHeight;
        if (CameraHolder.instance().isLandscape()) {
            mWidth = Math.max(width, height);
            mHeight = Math.min(width, height);
        } else {
            mWidth = Math.min(width, height);
            mHeight = Math.max(width, height);
        }
        Log.e("lsz","width="+mWidth+"height="+mHeight);
    }

    public void loadShaderAndParams() {
        mProgram = GlUtil.createProgram(mVertex, mFragment);
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "position");
        maTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");

        muPosMtxHandle   = GLES20.glGetUniformLocation(mProgram, "uPosMtx");
        muTexMtxHandle   = GLES20.glGetUniformLocation(mProgram, "uTexMtx");
    }

    public void createFboTexture() {
        Log.e("lsz","width="+mWidth+"height="+mHeight);
        if(CameraHolder.instance().getState() != CameraHolder.State.PREVIEW) {
            return;
        }
        GLES20.glGenFramebuffers(1, mFboId, 0);
        GLES20.glGenRenderbuffers(1, mRboId, 0);

        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, mRboId[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER,
                GLES20.GL_DEPTH_COMPONENT16, mWidth, mHeight);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER,
                GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, mRboId[0]);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER,0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFboId[0]);

        GLES20.glGenTextures(1, mTexId, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexId[0]);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                mWidth, mHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
                GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mTexId[0], 0);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) !=
                GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e("lsz",GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)+"");
            throw new RuntimeException("glCheckFramebufferStatus()");
        }
    }

    public void draw(final float[] tex_mtx) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFboId[0]);
        GLES20.glViewport(0, 0, mWidth, mHeight);
        GLES20.glClearColor(0f, 0f, 0f, 1f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);

        mVtxBuf.position(0);
        GLES20.glVertexAttribPointer(maPositionHandle,
                3, GLES20.GL_FLOAT, false, 4 * (3 + 2), mVtxBuf);
        GLES20.glEnableVertexAttribArray(maPositionHandle);

        mVtxBuf.position(3);
        GLES20.glVertexAttribPointer(maTexCoordHandle,
                2, GLES20.GL_FLOAT, false, 4 * (3 + 2), mVtxBuf);
        GLES20.glEnableVertexAttribArray(maTexCoordHandle);

        if (muPosMtxHandle >= 0)
            GLES20.glUniformMatrix4fv(muPosMtxHandle, 1, false, mPosMtx, 0);

        if (muTexMtxHandle >= 0)
            GLES20.glUniformMatrix4fv(muTexMtxHandle, 1, false, tex_mtx, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    public void setTextureId(int mSurfaceTextureId) {
        mTextureId = mSurfaceTextureId;
    }

    public int getTextureId() {
        return mTexId[0];
    }
}























