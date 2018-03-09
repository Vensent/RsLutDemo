# RsLutDemo

## Intro
Forked and inspired from [RsLutDemo](https://github.com/RenderScript/RsLutDemo). This project has added some other demos. Like:
* How to get an expected LUT array from JNI.
* How to process a _.cube_ file and get the expected LUT array.

## Tool
With the tool in [easyRS](https://github.com/silvaren/easyrs), we can easily use the _RenderScript_ and
the _Allocation_ to handle the input bitmap.  And in this way, we don't need care too much 
about the detail of implementation.

However, [easyRS](https://github.com/silvaren/easyrs) **cannot** be directly imported through:
```groovy
android {
    ...
    defaultConfig {
        ...
        renderscriptTargetApi 16
        renderscriptSupportModeEnabled true
    }
    ...
}

dependencies {
    ...
    compile 'io.github.silvaren:easyrs:0.5.3'
}
```

Because we cannot instantiate a _Lut3DParams.Cube()_. I also opened an [issue](https://github.com/silvaren/easyrs/issues/4) for this lovely tool.
Therefore I modified a little and imported this tool manually.

## Identify filter reference
```java
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
```

## Inspiration
I was inspired in the open project: [OpenColorIO](https://github.com/imageworks/OpenColorIO). It has some ways to 
process the LUT files like _.cube_, _.lut_, etc.
For example, [FileFormatIridasCube.cpp](https://github.com/imageworks/OpenColorIO/blob/master/src/core/FileFormatIridasCube.cpp) shows how to 
process a _.cube_ file.

## Further study
May add some other demos, like:
* Shows how to process a _.lut_ file.
* Apply the LUT 3D to the real time camera preview.