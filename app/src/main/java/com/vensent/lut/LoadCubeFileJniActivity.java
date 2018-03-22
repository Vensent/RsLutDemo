package com.vensent.lut;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v8.renderscript.RenderScript;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.jni.ReadingAssets;

import java.io.IOException;
import java.util.ArrayList;

import io.github.silvaren.easyrs.tools.Lut3D;
import io.github.silvaren.easyrs.tools.params.Lut3DParams;

public class LoadCubeFileJniActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoadCubeFileJniActivity";
    private ImageView mImageView;
    private RenderScript mRs;
    private Bitmap mBitmap;
    private int mFilter = 0;
    private ArrayList<Lut3DParams.Cube> mCubeList;
    private static final int SMALL_SIZE = 17;
    private static final int LARGE_SIZE = 33;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load_lut);
        mImageView = findViewById(R.id.imageView);
        mImageView.setOnClickListener(this);
        init();
    }

    private void init() {
        mCubeList = new ArrayList<>();
        mRs = RenderScript.create(this);
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bugs);

        new Background().execute();
        new Thread(new Runnable() {
            @Override
            public void run() {
                readAssetsCubeFiles();
            }
        }).start();
    }

    private void readAssetsCubeFiles() {
        String fileNames[] = null;
        try {
            fileNames = getAssets().list("");
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert fileNames != null;
        for (String file : fileNames) {
            if (file.endsWith(".cube")) {
                Log.d(TAG, "start parsing: " + file + ".");
                Lut3DParams.Cube cube = parseCubeFile(file);
                Log.d(TAG, "reading " + file + " finished.");
                mCubeList.add(cube);
            }
        }
    }

    private Lut3DParams.Cube parseCubeFile(String file) {
        int lut[];
        if (file.equals("BleachBypass3.cube") || file.equals("Strip3.cube")) {
            lut = new int[LARGE_SIZE * LARGE_SIZE * LARGE_SIZE];
        } else {
            lut = new int[SMALL_SIZE * SMALL_SIZE * SMALL_SIZE];
        }
        int size = ReadingAssets.readingCubeFileFromAssets(lut, getAssets(), file);
        return new Lut3DParams.Cube(size, size, size, lut);
    }

    @Override
    public void onClick(View v) {
        if (mCubeList.size() == 0) {
            mFilter = 0;
            Toast.makeText(this, "Still loading...", Toast.LENGTH_SHORT).show();
        } else {
            mFilter = (1 + mFilter) % (mCubeList.size() + 1);
        }
        new Background().execute();
    }

    class Background extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Void... params) {
            if (mFilter != 0) {
                return Lut3D.apply3dLut(mRs, mBitmap, mCubeList.get(mFilter - 1));
            } else {
                // identity filter provided for reference
                int redDim, greenDim, blueDim;
                int[] lut;
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

                return Lut3D.apply3dLut(mRs, mBitmap, new Lut3DParams.Cube(redDim, greenDim, blueDim, lut));
            }
        }

        @Override
        protected void onPostExecute(Bitmap processedBitmap) {
            mImageView.setImageBitmap(processedBitmap);
        }
    }
}
