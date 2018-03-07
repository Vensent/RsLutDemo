package com.jni;

/**
 * Created by vensent on 3/2/18.
 */

public class CreativeCube {

    static {
        System.loadLibrary("jni_creativecube");
    }

    public static native void creativeCubeBleachBypass(int out[], int size, float amount);

    public static native void creativeCube3Strip(int out[], int size, float amount);

    public static native void creativeCubeFilum2(int out[], int size, float amount);
}
