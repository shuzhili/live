package cn.live.livetest.camera;

public class CameraData {
    public static final int FACING_FRONT=1;
    public static final int FACING_BACK=2;

    public int cameraId;
    public int cameraFacing;
    public int cameraWidth;
    public int cameraHeight;
    public boolean hasLight;
    public boolean supportTouchFocus;
    public boolean touchFocusMode;

    public CameraData(int id,int facing,int width,int height){
        cameraId=id;
        cameraFacing=facing;
        cameraWidth=width;
        cameraHeight=height;
    }

    public CameraData(int id,int facing){
        cameraId=id;
        cameraFacing=facing;
    }
}














