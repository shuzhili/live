package cn.live.livetest.camera.render;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GlUtil {
    public static FloatBuffer createSquareVertexBuffer() {
        final float vtx[] = {
                -1f, 1f, 0f, 0f, 1f,
                -1f, -1f, 0f, 0f, 0f,
                1f, 1f, 0f, 1f, 1f,
                1f, -1f, 0f, 1f, 0f,
        };

        ByteBuffer bb=ByteBuffer.allocateDirect(4*vtx.length);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb=bb.asFloatBuffer();
        fb.put(vtx);
        fb.position(0);
        return fb;
    }

    public static FloatBuffer createVertexBuffer() {
        final float vtx[] = {
                -1f, 1f, 0f,
                -1f, -1f, 0f,
                1f, 1f, 0f,
                1f, -1f, 0f,
    };

        ByteBuffer bb=ByteBuffer.allocateDirect(4*vtx.length);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb=bb.asFloatBuffer();
        fb.put(vtx);
        fb.position(0);
        return fb;
    }

    public static FloatBuffer createTexCoordBuffer(){
        final float vtx[]={
                0f,1f,
                0f,0f,
                1f,1f,
                1f,0f,
        };

        ByteBuffer bb=ByteBuffer.allocateDirect(vtx.length*4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer floatBuffer = bb.asFloatBuffer();
        floatBuffer.put(vtx);
        floatBuffer.position(0);
        return floatBuffer;
    }

    public static float[] createIdentityMtx(){
        float[] m=new float[16];
        Matrix.setIdentityM(m,0);
        return m;
    }

    public static int loadShader(int shaderType,String source){
        int shader= GLES20.glCreateShader(shaderType);
        GLES20.glShaderSource(shader,source);
        GLES20.glCompileShader(shader);
        int[] compiled=new int[1];
        GLES20.glGetShaderiv(shader,GLES20.GL_COMPILE_STATUS,compiled,0);
        if(compiled[0]==0){
            GLES20.glDeleteShader(shader);
            shader=0;
        }
        return shader;
    }

    public static int createProgram(String vertexSource,String fragmentSource){
        int vs=loadShader(GLES20.GL_VERTEX_SHADER,vertexSource);
        int fs=loadShader(GLES20.GL_FRAGMENT_SHADER,fragmentSource);
        int program=GLES20.glCreateProgram();
        GLES20.glAttachShader(program,vs);
        GLES20.glAttachShader(program,fs);
        GLES20.glLinkProgram(program);
        int[] linkStatus=new int[1];
        GLES20.glGetProgramiv(program,GLES20.GL_LINK_STATUS,linkStatus,0);
        if(linkStatus[0]!=GLES20.GL_TRUE){
            GLES20.glDeleteProgram(program);
            program=0;
        }
        return program;
    }
}


















