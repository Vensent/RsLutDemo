// Created by vensent on 3/22/18.
//

#include <jni.h>
#include <sys/types.h>
#include <algorithm>
#include <string>
#include <malloc.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <android/log.h>
#include <stdio.h>
#include <cctype>
#include <sstream>
#include "pystring.h"

#define TAG "ReadingAssets" // 这个是自定义的LOG的标识
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__) // 定义LOGD类型
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__) // 定义LOGI类型
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__) // 定义LOGW类型
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__) // 定义LOGE类型
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,TAG ,__VA_ARGS__) // 定义LOGF类型
#define CLAMP(a, min, max)    ((a)<(min)?(min):((a)>(max)?(max):(a)))

/*******************************************************************************
* Function Name  : Java_com_jni_ReadingAssets_readingCubeFileFromAssets
* Description    : 定义：public native void  readFromAssets(AssetManager ass,String filename);
* Input          : AssetManager对象 filename资源名
* Output         : None
* Return         : None
*******************************************************************************/
int Parse_Buffer(int *lut, char *buffer);

extern "C"
JNIEXPORT jint JNICALL
Java_com_jni_ReadingAssets_readingCubeFileFromAssets(JNIEnv *env, jclass type, jintArray out_,
                                                     jobject assetManager, jstring filename_) {
    jint *out = env->GetIntArrayElements(out_, NULL);
    const char *fileName = env->GetStringUTFChars(filename_, 0);

    LOGD("ReadAssets started.");
    AAssetManager *mgr = AAssetManager_fromJava(env, assetManager);
    if (mgr == NULL) {
        LOGE(" %s", "AAssetManager==NULL");
        return -1;
    }

    /*获取文件名并打开*/
    AAsset *asset = AAssetManager_open(mgr, fileName, AASSET_MODE_UNKNOWN);
    if (asset == NULL) {
        LOGE(" %s", "asset==NULL");
        return -1;
    }

    /*获取文件大小*/
    unsigned int bufferSize = (unsigned int) AAsset_getLength(asset);
    char *buffer = (char *) malloc((size_t) (bufferSize + 1));
    buffer[bufferSize] = 0;
    AAsset_read(asset, buffer, (size_t) bufferSize);
    int result = Parse_Buffer(out, buffer);

    free(buffer);
    /*关闭文件*/
    AAsset_close(asset);

    env->ReleaseIntArrayElements(out_, out, 0);
    env->ReleaseStringUTFChars(filename_, fileName);
    LOGD("ReadAssets finished.");
    return result;
}

int Get_RGB_Color_Value(float r, float g, float b) {
    int rcol = (int) (255 * CLAMP(r, 0.f, 1.f));
    int gcol = (int) (255 * CLAMP(g, 0.f, 1.f));
    int bcol = (int) (255 * CLAMP(b, 0.f, 1.f));
    return rcol | (gcol << 8) | (bcol << 16);
}

bool StringToInt(int *ival, const char *str) {
    if (!str) return false;

    std::istringstream inputStringstream(str);
    int x;
    if (!(inputStringstream >> x)) {
        return false;
    }

    if (ival) *ival = x;
    return true;
}

bool StringToFloat(float *fval, const char *str) {
    if (!str) return false;

    std::istringstream inputStringstream(str);
    float x;
    if (!(inputStringstream >> x)) {
        return false;
    }

    if (fval) *fval = x;
    return true;
}

int Parse_Buffer(int *lut, char *buffer) {
    float domain_min[] = {0.0f, 0.0f, 0.0f};
    float domain_max[] = {1.0f, 1.0f, 1.0f};
    int lut3dSize = 0;
    int i = 0;
    std::istringstream inputStringstream(buffer);
    std::string line;
    std::vector<std::string> parts;

    while (std::getline(inputStringstream, line)) {
        if (pystring::startswith(line, "#")) continue;

        // Strip, lowercase, and split the line
        pystring::split(pystring::lower(pystring::strip(line)), parts);
        if (parts.empty()) continue;

        if (parts[0] == "title") {
            // Optional, and currently unhandled
        } else if (parts[0] == "lut_1d_size" ||
                   parts[0] == "lut_2d_size") {
            LOGE("Unsupported Iridas .cube lut tag: ");
        } else if (parts[0] == "lut_3d_size") {
            if (parts.size() != 2) {
                LOGE("Malformed LUT_3D_SIZE tag in Iridas .cube lut.");
            }
            StringToInt(&lut3dSize, parts[1].c_str());
        } else if (parts[0] == "domain_min") {
            if (parts.size() != 4 ||
                !StringToFloat(&domain_min[0], parts[1].c_str()) ||
                !StringToFloat(&domain_min[1], parts[2].c_str()) ||
                !StringToFloat(&domain_min[2], parts[3].c_str())) {
                LOGE("domain_min is not correct.");
            }
        } else if (parts[0] == "domain_max") {
            if (parts.size() != 4 ||
                !StringToFloat(&domain_max[0], parts[1].c_str()) ||
                !StringToFloat(&domain_max[1], parts[2].c_str()) ||
                !StringToFloat(&domain_max[2], parts[3].c_str())) {
                LOGE("domain_max is not correct.");
            }
        } else {
            // It must be a float triple!
            if (lut == 0) {
                LOGE("The file doesn't contain 'lut_3d_size'.");
            }

            // In a .cube file, each data line contains 3 floats.
            // Please note: the blue component goes first!!!
            float bValue, gValue, rValue;
            StringToFloat(&bValue, parts[0].c_str());
            StringToFloat(&gValue, parts[1].c_str());
            StringToFloat(&rValue, parts[2].c_str());
            lut[i++] = Get_RGB_Color_Value(bValue, gValue, rValue);
        }
    }

    return lut3dSize;
}
