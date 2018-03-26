package com.vensent.lut;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsic3DLUT;
import android.support.v8.renderscript.ScriptIntrinsicBlend;
import android.support.v8.renderscript.Type;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.java.vensent.renderer.ScriptC_set_alpha;

/**
 * Created by vensent on 3/24/18.
 */

public class AdjustStrengthBlendingActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
    private static final String TAG = "AdjustStrengthBlendingActivity";
    private ImageView mImageView;
    private RenderScript mRs;
    private Bitmap mBitmap;
    private ScriptIntrinsic3DLUT mScriptLut;
    private ScriptIntrinsicBlend mBlendScript;
    private ScriptC_set_alpha mSetAlphaScript;
    private Allocation mAllocCube;
    private Allocation mAllocIn;
    private Allocation mAllocOut;
    private Allocation mOriginalAllocation;
    private Allocation mLoadAllocation;
    private Bitmap mOutputBitmap;
    private SeekBar mStrengthSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adjust_strength_lut);
        mImageView = findViewById(R.id.image_view);
        mStrengthSeekBar = findViewById(R.id.strength_seek_bar);
        mStrengthSeekBar.setOnSeekBarChangeListener(this);
        mRs = RenderScript.create(this);
        new Background().execute();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Log.d(TAG, "onProgressChanged progress is: " + progress);
        if (mBlendScript == null) {
            mBlendScript = ScriptIntrinsicBlend.create(mRs, mOriginalAllocation.getElement());
            mSetAlphaScript = new ScriptC_set_alpha(mRs);
            mAllocOut = Allocation.createTyped(mRs, mLoadAllocation.getType());
            mAllocIn = Allocation.createTyped(mRs, mOriginalAllocation.getType());
        }

        Log.d(TAG, "onProgressChanged blending started.");

        mAllocIn.copyFrom(mOriginalAllocation);
        mAllocOut.copyFrom(mLoadAllocation);

        short strength = (short) (255 * progress / 100);

        mSetAlphaScript.set_alpha_value(strength);
        mSetAlphaScript.forEach_filter(mAllocOut, mAllocOut);
        mSetAlphaScript.set_alpha_value(strength);
        mSetAlphaScript.forEach_filter(mAllocIn, mAllocIn);

        mBlendScript.forEachDstAtop(mAllocIn, mAllocOut);

        mSetAlphaScript.set_alpha_value((short) 255);
        mSetAlphaScript.forEach_filter(mAllocOut, mAllocOut);
        mAllocOut.copyTo(mOutputBitmap);

        Log.d(TAG, "onProgressChanged blending end.");

        mImageView.setImageBitmap(mOutputBitmap);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private class Background extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Void... params) {

            if (mBitmap == null) {
                mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bugs);
            }
            int w, h;
            int redDim, greenDim, blueDim;

            Bitmap lutBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.lut_bw_contrast);
            w = lutBitmap.getWidth();
            h = lutBitmap.getHeight();
            redDim = w / h;
            greenDim = redDim;
            blueDim = redDim;
            int[] pixels = new int[w * h];
            int lut[] = new int[w * h];
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
            if (mScriptLut == null) {
                mScriptLut = ScriptIntrinsic3DLUT.create(mRs, Element.U8_4(mRs));
            }
            Type.Builder tb = new Type.Builder(mRs, Element.U8_4(mRs));
            tb.setX(redDim).setY(greenDim).setZ(blueDim);
            Type t = tb.create();
            mAllocCube = Allocation.createTyped(mRs, t);
            mAllocCube.copyFromUnchecked(lut);
            mScriptLut.setLUT(mAllocCube);

            mOutputBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), mBitmap.getConfig());
            mOriginalAllocation = Allocation.createFromBitmap(mRs, mBitmap);
            mLoadAllocation = Allocation.createFromBitmap(mRs, mOutputBitmap);

            mScriptLut.forEach(mOriginalAllocation, mLoadAllocation);

            mLoadAllocation.copyTo(mOutputBitmap);
            return mBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap processedBitmap) {
            mImageView.setImageBitmap(processedBitmap);
        }
    }
}