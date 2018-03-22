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
}
