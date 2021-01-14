package cn.live.livetest.camera.render;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

import cn.live.livetest.camera.CameraHolder;

public class RenderSurfaceView extends GLSurfaceView {

    private MyRender myRender;

    public RenderSurfaceView(Context context) {
        super(context);
        init();
    }

    public RenderSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        myRender = new MyRender(this);
        setEGLContextClientVersion(2);
        setRenderer(myRender);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        SurfaceHolder holder = getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(mSurfaceHolderCallback);
    }

    private SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {

        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            CameraHolder.instance().stopPreview();
            CameraHolder.instance().releaseCamera();
        }
    };

    public MyRender getRender() {
        return myRender;
    }
}
























