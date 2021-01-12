package cn.live.livetest.camera;

public class CameraConfiguration {

    public static final int DEFAULT_HEIGHT = 1280;
    public static final int DEFAULT_WIDTH = 720;
    public static final int DEFAULT_FPS = 15;
    public static final int DEFAULT_ROTATION = 0;
    public static final Facing DEFAULT_FACING = Facing.FRONT;
    public static final Orientation DEFAULT_ORIENTATION = Orientation.PORTRAIT;
    public static final FocusMode DEFAULT_FOCUSMODE = FocusMode.AUTO;

    public CameraConfiguration(Builder builder) {
        height=builder.height;
        width=builder.width;
        facing = builder.facing;
        fps = builder.fps;
        orientation = builder.orientation;
        focusMode = builder.focusMode;
        rotation = builder.rotation;
    }

    public enum Facing {
        FRONT, BACK
    }

    public enum Orientation {
        LANDSCAPE, PORTRAIT
    }

    public enum FocusMode {
        AUTO, TOUCH
    }

    public final int height;
    public final int width;
    public final int fps;
    public final int rotation;
    public final Facing facing;
    public final Orientation orientation;
    public final FocusMode focusMode;

    public static CameraConfiguration createDefault() {
        return new Builder().build();
    }

    //是将一个复杂的对象的构建与它的表示分离，使
    //得同样的构建过程可以创建不同的表示。创建者模式隐藏了复杂对象的创建过程，
    //它把复杂对象的创建过程加以抽象，通过子类继承或者重载的方式，动态的创建具有复合属性的对象。
    public static class Builder {
        private int height=DEFAULT_HEIGHT;
        private int width=DEFAULT_WIDTH;
        private int fps=DEFAULT_FPS;
        private int rotation=DEFAULT_ROTATION;
        private Facing facing=DEFAULT_FACING;
        private Orientation orientation=DEFAULT_ORIENTATION;
        private FocusMode focusMode=DEFAULT_FOCUSMODE;

        public Builder setPreview(int height,int width){
            this.height=height;
            this.width=width;
            return this;
        }

        public Builder setFacing(Facing facing){
            this.facing=facing;
            return this;
        }

        public Builder setOrientation(Orientation orientation){
            this.orientation=orientation;
            return this;
        }

        public Builder setFps(int fps){
            this.fps=fps;
            return this;
        }

        public Builder setFocusMode(FocusMode focusMode) {
            this.focusMode = focusMode;
            return this;
        }

        public Builder setRotation(int rot) {
            this.rotation = rot;
            return this;
        }

        public CameraConfiguration build(){return new CameraConfiguration(this);}
    }
}






















