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

import android.opengl.GLES20;
import android.support.annotation.CallSuper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public abstract class GLScene {
    private static final float SQUARE_COORDS[] = {
            1.0f, -1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            -1.0f, 1.0f,
    };
    private static final float TEXTURE_COORDS[] = {
            1.0f, 0.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
    };
    private static FloatBuffer VERTEX_BUF, TEXTURE_COORD_BUF;
    static {
        // Setup default Buffers
        VERTEX_BUF = ByteBuffer.allocateDirect(SQUARE_COORDS.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        VERTEX_BUF.put(SQUARE_COORDS);
        VERTEX_BUF.position(0);

        TEXTURE_COORD_BUF = ByteBuffer.allocateDirect(TEXTURE_COORDS.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        TEXTURE_COORD_BUF.put(TEXTURE_COORDS);
        TEXTURE_COORD_BUF.position(0);
    }

    private long mStartTime = System.currentTimeMillis();
    private int iFrame = 0;


    @CallSuper
    public void reset() {
        iFrame = 0;
        mStartTime = System.currentTimeMillis();
    }

    public final void draw(int canvasWidth, int canvasHeight) {
        onDraw(canvasWidth, canvasHeight);
        iFrame++;
    }

    public abstract void onDraw(int canvasWidth, int canvasHeight);

    protected void runShandertoyProgram(
            int program, int[] iResolution, int[] iChannels, int[][] iChannelResolutions) {

        runShandertoyProgram(program, VERTEX_BUF, TEXTURE_COORD_BUF, iResolution, iChannels, iChannelResolutions);
    }

    protected void runShandertoyProgram(
            int program, FloatBuffer vertex, FloatBuffer textureCoord,
            int[] iResolution, int[] iChannels, int[][] iChannelResolutions) {

        GLES20.glUseProgram(program);

        int iResolutionLocation = GLES20.glGetUniformLocation(program, "iResolution");
        GLES20.glUniform3fv(iResolutionLocation, 1,
                FloatBuffer.wrap(new float[]{(float) iResolution[0], (float) iResolution[1], 1.0f}));

        float time = ((float) (System.currentTimeMillis() - mStartTime)) / 1000.0f;
        int iGlobalTimeLocation = GLES20.glGetUniformLocation(program, "iTime");
        GLES20.glUniform1f(iGlobalTimeLocation, time);

        int iFrameLocation = GLES20.glGetUniformLocation(program, "iFrame");
        GLES20.glUniform1i(iFrameLocation, iFrame);

        int vPositionLocation = GLES20.glGetAttribLocation(program, "vPosition");
        GLES20.glEnableVertexAttribArray(vPositionLocation);
        GLES20.glVertexAttribPointer(vPositionLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, vertex);

        int vTexCoordLocation = GLES20.glGetAttribLocation(program, "vTexCoord");
        GLES20.glEnableVertexAttribArray(vTexCoordLocation);
        GLES20.glVertexAttribPointer(vTexCoordLocation, 2, GLES20.GL_FLOAT, false, 4 * 2, textureCoord);

        for (int i = 0; i < iChannels.length; i++) {
            int sTextureLocation = GLES20.glGetUniformLocation(program, "iChannel" + i);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, iChannels[i]);
            GLES20.glUniform1i(sTextureLocation, i);
        }

        float _iChannelResolutions[] = new float[iChannelResolutions.length * 3];
        for (int i = 0; i < iChannelResolutions.length; i++) {
            _iChannelResolutions[i * 3] = iChannelResolutions[i][0];
            _iChannelResolutions[i * 3 + 1] = iChannelResolutions[i][1];
            _iChannelResolutions[i * 3 + 2] = 1.0f;
        }

        int iChannelResolutionLocation = GLES20.glGetUniformLocation(program, "iChannelResolution");
        GLES20.glUniform3fv(iChannelResolutionLocation,
                _iChannelResolutions.length, FloatBuffer.wrap(_iChannelResolutions));

        // Draw
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }
}
