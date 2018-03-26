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
import android.support.v8.renderscript.Type;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.jni.ReadingAssets;

public class AdjustStrengthLutActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "AdjustStrengthLutActivity";
    private ImageView mImageView;
    private RenderScript mRs;
    private Bitmap mBitmap;
    private ScriptIntrinsic3DLUT mScriptLut;
    private Allocation mAllocCube;
    private Allocation mOriginalAllocation;
    private Allocation mLoadAllocation;
    private Bitmap mOutputBitmap;
    private SeekBar mStrengthSeekBar;
    private float[] mOriginalLut;
    private float[] mIdenticalLut;
    private int[] mTargetLut;
    private int mSize;
    private static final int SMALL_SIZE = 17;
    private static final int LARGE_SIZE = 33;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adjust_strength_lut);
        mImageView = findViewById(R.id.image_view);
        if (mBitmap == null) {
            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bugs);
            mImageView.setImageBitmap(mBitmap);
        }
        mStrengthSeekBar = findViewById(R.id.strength_seek_bar);
        mStrengthSeekBar.setOnSeekBarChangeListener(this);
        mRs = RenderScript.create(this);
        new Background().execute();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mTargetLut == null) {
            mTargetLut = new int[mSize * mSize * mSize];
        }

        Log.d(TAG, "onProgressChanged operation started.");
        ReadingAssets.getStrengthCubeLut(mTargetLut, mOriginalLut, mIdenticalLut, mSize, (float) progress / 100);

        mAllocCube.copyFromUnchecked(mTargetLut);
        mScriptLut.setLUT(mAllocCube);

        mScriptLut.forEach(mOriginalAllocation, mLoadAllocation);
        mLoadAllocation.copyTo(mOutputBitmap);
        Log.d(TAG, "onProgressChanged operation end.");
        mImageView.setImageBitmap(mOutputBitmap);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private class Background extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            Log.d(TAG, "Background operation started.");

            if (mBitmap == null) {
                mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bugs);
            }
            String file = "NightFromDay.cube";
            if (file.equals("BleachBypass3.cube") || file.equals("Strip3.cube")) {
                mOriginalLut = new float[LARGE_SIZE * LARGE_SIZE * LARGE_SIZE * 3];
            } else {
                mOriginalLut = new float[SMALL_SIZE * SMALL_SIZE * SMALL_SIZE * 3];
            }
            mSize = ReadingAssets.readingCubeFileDetailFromAssets(mOriginalLut, getAssets(), file);
            if (mScriptLut == null) {
                mScriptLut = ScriptIntrinsic3DLUT.create(mRs, Element.U8_4(mRs));
            }
            Type.Builder tb = new Type.Builder(mRs, Element.U8_4(mRs));
            tb.setX(mSize).setY(mSize).setZ(mSize);
            Type t = tb.create();
            mAllocCube = Allocation.createTyped(mRs, t);

            mOutputBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), mBitmap.getConfig());
            mOriginalAllocation = Allocation.createFromBitmap(mRs, mBitmap);
            mLoadAllocation = Allocation.createFromBitmap(mRs, mOutputBitmap);

            mIdenticalLut = new float[mSize * mSize * mSize * 3];
            ReadingAssets.getIdenticalCubeLut(mIdenticalLut, mSize);

            Log.d(TAG, "Background operation ended.");

            return null;
        }
    }
}
