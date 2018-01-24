/*
 * Copyright 2017 nekocode
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.nekocode.musicviz.render;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.view.TextureView;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

import cn.nekocode.musicviz.FFTFrame;
import cn.nekocode.musicviz.WaveFormFrame;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public class VisualizerRenderer implements Runnable, TextureView.SurfaceTextureListener {
    private static final int EGL_OPENGL_ES2_BIT = 4;
    private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
    private static final int DRAW_INTERVAL = 1000 / 30;
    private static final float DBM_INTERP = 0.75f;

    private Thread mRenderThread;
    private Context mContext;
    private SurfaceTexture mSurfaceTexture;
    private int mSurfaceWidth, mSurfaceHeight;

    private EGLDisplay mEglDisplay;
    private EGLSurface mEglSurface;
    private EGLContext mEglContext;
    private EGL10 mEgl10;

    private SceneController mSceneController;
    private int mTextureWidth;
    private WaveFormFrame mWaveFormFrame;
    private FFTFrame mFFTFrame;
    private float[] mLastDbmArray;


    public VisualizerRenderer(Context context, int textureWidth) {
        this.mContext = context;
        this.mTextureWidth = textureWidth;
    }

    public void setSceneController(SceneController controller) {
        mSceneController = controller;
    }

    public void updateWaveFormFrame(WaveFormFrame frame) {
        this.mWaveFormFrame = frame;
    }

    public void updateFFTFrame(FFTFrame frame) {
        this.mFFTFrame = frame;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        mSurfaceWidth = -width;
        mSurfaceHeight = -height;
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mRenderThread != null && mRenderThread.isAlive()) {
            mRenderThread.interrupt();
        }

        return true;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mRenderThread != null && mRenderThread.isAlive()) {
            mRenderThread.interrupt();
        }
        mRenderThread = new Thread(this);

        mSurfaceTexture = surface;
        mSurfaceWidth = -width;
        mSurfaceHeight = -height;

        // Start rendering
        mRenderThread.start();
    }

    @Override
    public void run() {
        if (!initGL(mSurfaceTexture)) {
            throw new RuntimeException("Initializing OpenGL failed");
        }

        final byte[] buf = new byte[mTextureWidth * 2];
        final int audioTexId = genAudioTexture(buf, mTextureWidth);
        if (mSceneController != null) {
            mSceneController.onSetup(mContext, audioTexId, mTextureWidth);
        }


        // Render loop
        while (!Thread.currentThread().isInterrupted()) {
            try {
                if (mSurfaceWidth < 0 && mSurfaceHeight < 0)
                    GLES20.glViewport(0, 0, mSurfaceWidth = -mSurfaceWidth, mSurfaceHeight = -mSurfaceHeight);

                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                GLES20.glClearColor(1f, 0f, 0f, 0f);


                // Prepare the audio texture
                fillFFT(buf, 0, mTextureWidth);
                if (mWaveFormFrame != null) {
                    System.arraycopy(mWaveFormFrame.getRawWaveForm(), 0, buf, mTextureWidth, mTextureWidth);
                }
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, audioTexId);
                GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, mTextureWidth, 2,
                        GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, ByteBuffer.wrap(buf));


                // Draw
                GLScene scene;
                if (mSceneController != null && (scene = mSceneController.getActivedScene()) != null) {
                    scene.draw(mSurfaceWidth, mSurfaceHeight);
                }

                // Flush
                GLES20.glFlush();
                mEgl10.eglSwapBuffers(mEglDisplay, mEglSurface);

                Thread.sleep(DRAW_INTERVAL);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Release something
        GLES20.glDeleteTextures(1, new int[]{audioTexId}, 0);
    }

    private void fillFFT(byte[] buf, int offset, int len) {
        if (mFFTFrame == null) return;
        if (offset + len > buf.length) return;

        float[] dBmArray = new float[len];
        mFFTFrame.calculate(len, dBmArray);

        float maxDbm = Float.MIN_VALUE;
        float minDbm = Float.MAX_VALUE;
        for (int i = 0; i < len; i++) {
            float dbm = dBmArray[i];
            if (dbm < 0f) dbm = 0f;
            if (dbm > 1f) dbm = 1f;

            if (mLastDbmArray != null) {
                float oldDbm = mLastDbmArray[i];
                if (oldDbm - dbm > 0.025f) {
                    dbm = oldDbm - 0.025f;
                } else {
                    dbm = mLastDbmArray[i] * DBM_INTERP + dbm * (1f - DBM_INTERP);
                }
            }

            if (dbm > maxDbm) maxDbm = dbm;
            if (dbm < minDbm) minDbm = dbm;

            dBmArray[i] = dbm;
            buf[offset + i] = (byte) (dbm * 255);
        }

//        float midDbm = (maxDbm + minDbm) / 2f;
//
//        for (int i = 0; i < len; i++) {
//            float dbm = dBmArray[i];
//            dbm = midDbm * 0.2f + dbm * 0.8f;
//            buf[offset + i] = (byte) (dbm * 255);
//        }
        mLastDbmArray = dBmArray;
    }

    private int genAudioTexture(byte[] buf, int width) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        final int[] genBuf = new int[1];
        GLES20.glGenTextures(1, genBuf, 0);
        final int texId = genBuf[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);
        GLES20.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_LUMINANCE,
                width, 2, 0, GL10.GL_LUMINANCE, GL10.GL_UNSIGNED_BYTE,
                ByteBuffer.wrap(buf));

        return texId;
    }

    private boolean initGL(SurfaceTexture texture) {
        mEgl10 = (EGL10) EGLContext.getEGL();

        mEglDisplay = mEgl10.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        if (mEglDisplay == EGL10.EGL_NO_DISPLAY) return false;

        int[] version = new int[2];
        if (!mEgl10.eglInitialize(mEglDisplay, version)) return false;

        int[] configsCount = new int[1];
        EGLConfig[] configs = new EGLConfig[1];
        int[] configSpec = {
                EGL10.EGL_RENDERABLE_TYPE,
                EGL_OPENGL_ES2_BIT,
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_DEPTH_SIZE, 0,
                EGL10.EGL_STENCIL_SIZE, 0,
                EGL10.EGL_NONE
        };

        EGLConfig eglConfig = null;
        if (!mEgl10.eglChooseConfig(mEglDisplay, configSpec, configs, 1, configsCount)) {
            return false;
        } else if (configsCount[0] > 0) {
            eglConfig = configs[0];
        }
        if (eglConfig == null) return false;

        mEglContext = mEgl10.eglCreateContext(
                mEglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT,
                new int[]{EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE});
        mEglSurface = mEgl10.eglCreateWindowSurface(mEglDisplay, eglConfig, texture, null);

        if (mEglSurface == null || mEglSurface == EGL10.EGL_NO_SURFACE) return false;
        if (!mEgl10.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) return false;

        return true;
    }
}