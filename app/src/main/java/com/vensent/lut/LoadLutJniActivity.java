package com.vensent.lut;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v8.renderscript.RenderScript;
import android.view.View;
import android.widget.ImageView;

import com.jni.CreativeCube;

import io.github.silvaren.easyrs.tools.Lut3D;
import io.github.silvaren.easyrs.tools.params.Lut3DParams;

public class LoadLutJniActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mImageView;
    private RenderScript mRs;
    private Bitmap mBitmap;
    private int mFilter = 0;
    private static final int SIZE = 33;
    private static final int IDENTITY = 0;
    private static final int BLEACH_BYPASS = 1;
    private static final int STRIP3 = 2;
    private static final int FILUM2 = 3;
    private static final float BLEACH_BYPASS_STRENGTH = 2.f;
    private static final float STRIP3_STRENGTH = 2.f;
    private static final float FILM2_STRENGTH = 2.f;

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
        mFilter = (1 + mFilter) % (FILUM2 + 1);
        new Background().execute();
    }


    class Background extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Void... params) {
            int redDim, greenDim, blueDim;
            redDim = greenDim = blueDim = SIZE;
            if (mBitmap == null) {
                mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bugs);
            }
            int lut[] = new int[redDim * greenDim * blueDim];
            switch (mFilter) {
                case IDENTITY: {
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
                break;
                case BLEACH_BYPASS: {
                    CreativeCube.creativeCubeBleachBypass(lut, SIZE, BLEACH_BYPASS_STRENGTH);
                }
                break;
                case STRIP3: {
                    CreativeCube.creativeCube3Strip(lut, SIZE, STRIP3_STRENGTH);
                }
                break;
                case FILUM2: {
                    CreativeCube.creativeCubeFilum2(lut, SIZE, FILM2_STRENGTH);
                }
                break;
            }

            return Lut3D.apply3dLut(mRs, mBitmap, new Lut3DParams.Cube(redDim, greenDim, blueDim, lut));
        }

        @Override
        protected void onPostExecute(Bitmap processedBitmap) {
            mImageView.setImageBitmap(processedBitmap);
        }
    }
}
