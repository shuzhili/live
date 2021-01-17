package cn.live.livetest.camera;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cn.live.livetest.R;
import cn.live.livetest.camera.render.MyRender;
import cn.live.livetest.camera.render.RenderSurfaceView;

public class CameraView extends FrameLayout {

    private boolean isMediaOverlay = true;
    private float mAspectRatio = 9.0f / 16;

    protected MyRender render;
    private RenderSurfaceView renderSurfaceView;

    private ScaleGestureDetector mZoomGestureDetector;
    private CameraZoomListener cameraZoomListener;
    private boolean isRenderSurfaceViewShowing = true;

    public CameraView(@NonNull Context context) {
        this(context, null);
    }

    public CameraView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_camera_view, this, true);
        renderSurfaceView = view.findViewById(R.id.render_surface_view);
        renderSurfaceView.setZOrderMediaOverlay(isMediaOverlay);
        render = renderSurfaceView.getRender();
        mZoomGestureDetector = new ScaleGestureDetector(context, new ZoomGestureListener());
    }

    @Override
    public void setVisibility(int visibility) {
        int currentVisibility = getVisibility();
        if (visibility == currentVisibility) {
            return;
        }
        switch (visibility) {
            case VISIBLE:
                addRenderSurfaceView();
                break;
            case GONE:
                removeRenderSurfaceView();
                break;
            case INVISIBLE:
                removeRenderSurfaceView();
                break;
        }
        super.setVisibility(visibility);
    }

    private void addRenderSurfaceView() {
        if (!isRenderSurfaceViewShowing) {
            LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            addView(renderSurfaceView, 0, layoutParams);
            isRenderSurfaceViewShowing = true;
        }
    }

    private void removeRenderSurfaceView() {
        if (isRenderSurfaceViewShowing) {
            removeView(renderSurfaceView);
            isRenderSurfaceViewShowing = false;
        }
    }

    private class ZoomGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float progress = 0;
            if (detector.getScaleFactor() > 1.0f) {
                progress = CameraHolder.instance().cameraZoom(true);
            } else if (detector.getScaleFactor() < 1.0f) {
                progress = CameraHolder.instance().cameraZoom(false);
            }
            if (cameraZoomListener != null) {
                cameraZoomListener.onZoomProgress(progress);
            }
            return super.onScale(detector);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);

        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.AT_MOST) {
            heightSpecSize = (int) (widthMeasureSpec / mAspectRatio);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSpecSize, MeasureSpec.EXACTLY);
        } else if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.EXACTLY) {
            widthSpecSize = (int) (heightSpecSize * mAspectRatio);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSpecSize, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setCameraZoomListener(CameraZoomListener listener) {
        cameraZoomListener = listener;
    }
}






















