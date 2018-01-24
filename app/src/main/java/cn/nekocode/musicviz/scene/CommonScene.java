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

package cn.nekocode.musicviz.scene;

import android.content.Context;

import cn.nekocode.musicviz.R;
import cn.nekocode.musicviz.render.GLScene;
import cn.nekocode.musicviz.render.MyGLUtils;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public class CommonScene extends GLScene {
    private int mProgram;
    private int mAudioTextureId;
    private int mTexttureWidth;


    public CommonScene(Context context, int audioTextureId, int textureWidth, int programResId) {
        mProgram = MyGLUtils.buildProgram(context, R.raw.vertext, programResId);
        mAudioTextureId = audioTextureId;
        mTexttureWidth = textureWidth;
    }

    @Override
    public void onDraw(int canvasWidth, int canvasHeight) {
        runShandertoyProgram(
                mProgram,
                new int[]{canvasWidth, canvasHeight},
                new int[]{mAudioTextureId},
                new int[][]{new int[]{mTexttureWidth, 2}}
        );
    }
}
