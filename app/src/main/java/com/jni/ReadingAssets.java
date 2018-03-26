package com.jni;

import android.content.res.AssetManager;

/**
 * Created by vensent on 3/22/18.
 */

public class ReadingAssets {

    static {
        System.loadLibrary("jni_readingassets");
    }

    public static native int readingCubeFileFromAssets(int out[], AssetManager assetsManager, String fileName);

    public static native void getIdenticalCubeLut(float identicalLut[], int size);

    public static native void getStrengthCubeLut(int out[], float originalLut[], float identicalLut[], int size, float strength);

    public static native int readingCubeFileDetailFromAssets(float[] originalLut, AssetManager assets, String file);
}
