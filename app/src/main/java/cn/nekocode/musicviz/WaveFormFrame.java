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

package cn.nekocode.musicviz;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public class WaveFormFrame {
    private byte[] mRawWaveForm;


    public WaveFormFrame(byte[] waveform, int offset, int len) {
        if (offset + len > waveform.length) throw new RuntimeException("Illegal offset and len");

        mRawWaveForm = new byte[len];
        System.arraycopy(waveform, offset, mRawWaveForm, 0, len);
    }

    public byte[] getRawWaveForm() {
        return mRawWaveForm;
    }
}
