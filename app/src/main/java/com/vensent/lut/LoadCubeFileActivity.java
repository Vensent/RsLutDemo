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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import io.github.silvaren.easyrs.tools.Lut3D;
import io.github.silvaren.easyrs.tools.params.Lut3DParams;

public class LoadCubeFileActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "LoadCubeFileActivity";
    private ImageView mImageView;
    private RenderScript mRs;
    private Bitmap mBitmap;
    private int mFilter = 0;
    private ArrayList<Lut3DParams.Cube> mCubeList;

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
                if (cube != null) {
                    mCubeList.add(cube);
                }
            }
        }
    }

    private Lut3DParams.Cube parseCubeFile(String file) {
        int data[] = null;
        int lut3dSize = 0;
        BufferedReader reader = null;
        int i = 0;
        try {
            reader = new BufferedReader(new InputStreamReader(getAssets().open(file)));
            // do reading, usually loop until end of file reading
            String line;
            String parts[];
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#") || line.length() == 0) {
                    continue;
                }

                parts = line.toLowerCase().split("\\s+");
                if (parts.length == 0) {
                    continue;
                }

                if (parts[0].equals("title")) {
                    // optional, or do nothing.
                } else if (parts[0].equals("lut_1d_size") || parts[0].equals("lut_2d_size")) {
                    throw new Exception("Unsupported Iridas .cube lut tag: " + parts[0]);
                } else if (parts[0].equals("lut_3d_size")) {
                    if (parts.length != 2) {
                        throw new Exception("Malformed LUT_3D_SIZE tag in Iridas .cube lut.");
                    }
                    lut3dSize = Integer.parseInt(parts[1]);
                    data = new int[lut3dSize * lut3dSize * lut3dSize];
                } else if (parts[0].equals("domain_min")) {
                    if (parts.length != 4 ||
                            Float.parseFloat(parts[1]) != 0.0f ||
                            Float.parseFloat(parts[2]) != 0.0f ||
                            Float.parseFloat(parts[3]) != 0.0f) {
                        throw new Exception("domain_min is not correct.");
                    }
                } else if (parts[0].equals("domain_max")) {
                    if (parts.length != 4 ||
                            Float.parseFloat(parts[1]) != 1.0f ||
                            Float.parseFloat(parts[2]) != 1.0f ||
                            Float.parseFloat(parts[3]) != 1.0f) {
                        throw new Exception("domain_max is not correct.");
                    }
                } else {
                    // It must be a float triple!
                    if (data == null || data.length == 0) {
                        throw new Exception("The file doesn't contain 'lut_3d_size'.");
                    }

                    // In a .cube file, each data line contains 3 floats.
                    // Please note: the blue component goes first!!!
                    data[i++] = getRGBColorValue(Float.parseFloat(parts[0]),
                            Float.parseFloat(parts[1]),
                            Float.parseFloat(parts[2]));
                }
            }
        } catch (IOException e) {
            //log the exception
        } catch (NumberFormatException e) {
            Log.d(TAG, "Converting string to digit failed.");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }

        if (data == null) {
            return null;
        } else {
            return new Lut3DParams.Cube(lut3dSize, lut3dSize, lut3dSize, data);
        }
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

    /**
     * Each RGB component is a 8-bit int.
     * R is in the high 8-bit, G is in the middle, and B is in the low 8-bit.
     *
     * @param b The coefficient of blue component according to 255.
     * @param g The coefficient of green component according to 255.
     * @param r The coefficient of red component according to 255.
     * @return the value of RGB, represented in a 24-bit int.
     */
    private int getRGBColorValue(float b, float g, float r) {
        int bcol = (int) (255 * clamp(b, 0.f, 1.f));
        int gcol = (int) (255 * clamp(g, 0.f, 1.f));
        int rcol = (int) (255 * clamp(r, 0.f, 1.f));
        return bcol | (gcol << 8) | (rcol << 16);
    }

    public static float clamp(float value, float min, float max) {
        return (value < min) ? min : (value > max) ? max : value;
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
