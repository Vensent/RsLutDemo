/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vensent.lut;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v8.renderscript.RenderScript;
import android.view.View;
import android.widget.ImageView;

import io.github.silvaren.easyrs.tools.Lut3D;
import io.github.silvaren.easyrs.tools.params.Lut3DParams;

public class LoadLutPngActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView mImageView;
    private RenderScript mRs;
    private Bitmap mBitmap;
    private int mFilter = 0;
    private int[] mLut3D = {
            R.drawable.lut_vintage,
            R.drawable.lut_bleach,
            R.drawable.lut_blue_crush,
            R.drawable.lut_bw_contrast,
            R.drawable.lut_instant,
            R.drawable.lut_punch,
            R.drawable.lut_washout,
            R.drawable.lut_washout_color,
            R.drawable.lut_x_process
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load_lut);
        mImageView = findViewById(R.id.imageView);
        mImageView.setOnClickListener(this);
        mRs = RenderScript.create(this);
        new Background().execute();
    }

    @Override
    public void onClick(View v) {
        mFilter = (1 + mFilter) % (mLut3D.length + 1);
        new Background().execute();
    }

    private class Background extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Void... params) {
            int redDim, greenDim, blueDim;
            int w, h;
            int[] lut;

            if (mBitmap == null) {
                mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bugs);
            }
            if (mFilter != 0) {
                Bitmap lutBitmap = BitmapFactory.decodeResource(getResources(), mLut3D[mFilter - 1]);
                w = lutBitmap.getWidth();
                h = lutBitmap.getHeight();
                redDim = w / h;
                greenDim = redDim;
                blueDim = redDim;
                int[] pixels = new int[w * h];
                lut = new int[w * h];
                lutBitmap.getPixels(pixels, 0, w, 0, 0, w, h);
                int i = 0;

                for (int r = 0; r < redDim; r++) {
                    for (int g = 0; g < greenDim; g++) {
                        int p = r + g * w;
                        for (int b = 0; b < blueDim; b++) {
                            lut[i++] = pixels[p + b * h];
                        }
                    }
                }
            } else {
                // identity filter provided for reference
                redDim = greenDim = blueDim = 32;
                lut = new int[redDim * greenDim * blueDim];
                int i = 0;
                for (int r = 0; r < redDim; r++) {
                    for (int g = 0; g < greenDim; g++) {
                        for (int b = 0; b < blueDim; b++) {
                            int bColor = (b * 255) / blueDim;
                            int gColor = (g * 255) / greenDim;
                            int rColor = (r * 255) / redDim;
                            lut[i++] = bColor | (gColor << 8) | (rColor << 16);
                        }
                    }
                }
            }

            return Lut3D.apply3dLut(mRs, mBitmap, new Lut3DParams.Cube(redDim, greenDim, blueDim, lut));
        }

        @Override
        protected void onPostExecute(Bitmap processedBitmap) {
            mImageView.setImageBitmap(processedBitmap);
        }
    }
}
