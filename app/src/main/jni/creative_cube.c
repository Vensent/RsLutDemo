#include <math.h>
#include <jni.h>
#include <stdlib.h>

#define MAXOF2(a, b) ((a)>(b)?(a):(b))
#define CLAMP(a, min, max)    ((a)<(min)?(min):((a)>(max)?(max):(a)))
#define CLAMPTOP(a, val)    ((a)<(val)?(a):(val))

void basicLumaContrast(float *r, float *g, float *b, float bleachAmount);

float smoothstepInverse(float x);

float Log3G10ExposureChangeSmooth(float x, float stops);

void BleachBoost(float *r, float *g, float *b, float stopsBoost, float bleachAmount);

float log3G10OffsetInverseV3(float x);

float softClampTop(float x, float a, float b, float c);

float log3G10OffsetV3(float x);

float smoothstep(float x);

float *createDefaultCube(int size) {
    unsigned int bigSize = size * size * size;
    float cube[bigSize * 3];

    int i, j, k;
    int ce;
    float ii, jj, kk;
    int kBase, jBase;
    const int size2 = size * size;
    const float size1 = size - 1.0f;
    for (k = 0; k < size; k++) {
        kBase = size2 * k;
        for (j = 0; j < size; j++) {
            jBase = size * j;
            for (i = 0; i < size; i++) {
                ce = (i + jBase + kBase) * 3;

                ii = ((float) i) / size1;
                jj = ((float) j) / size1;
                kk = ((float) k) / size1;

                cube[ce] = ii;
                cube[ce + 1] = jj;
                cube[ce + 2] = kk;
            }
        }
    }

    return cube;
}

int Get_RGB_Color_Value(float r, float g, float b) {
    int rcol = (int) (255 * CLAMP(r, 0.f, 1.f));
    int gcol = (int) (255 * CLAMP(g, 0.f, 1.f));
    int bcol = (int) (255 * CLAMP(b, 0.f, 1.f));
    return rcol | (gcol << 8) | (bcol << 16);
}

void *CreativeLut_BleachBypass(int *out, int size, float amount) {
    float *cube = createDefaultCube(size);

    const float stopsBoost = amount; //seems good for exposure boost at amount/2 but induces hard clip.....

    int i, j, k;
    int ce;
    float ii, jj, kk;
    int kBase, jBase;
    const int size2 = size * size;
    int m = 0;
    for (k = 0; k < size; k++) {
        kBase = size2 * k;
        for (j = 0; j < size; j++) {
            jBase = size * j;
            for (i = 0; i < size; i++) {
                ce = (i + jBase + kBase) * 3;

                ii = cube[ce];
                jj = cube[ce + 1];
                kk = cube[ce + 2];

                BleachBoost(&ii, &jj, &kk, stopsBoost, amount);

                cube[ce] = ii;
                cube[ce + 1] = jj;
                cube[ce + 2] = kk;

                out[m++] = Get_RGB_Color_Value(ii, jj, kk);
            }
        }
    }

    return out;
}

void *CreativeLut_3Strip(int *out, int size, float amount) {
    float *cube = createDefaultCube(size);

    int i, j, k;
    int ce;
    float ii, jj, kk;
    int kBase, jBase;
    const int size2 = size * size;
    int m = 0;

    float rMatte, gMatte, bMatte;

    for (k = 0; k < size; k++) {
        kBase = size2 * k;
        for (j = 0; j < size; j++) {
            jBase = size * j;
            for (i = 0; i < size; i++) {
                ce = (i + jBase + kBase) * 3;

                ii = cube[ce];
                jj = cube[ce + 1];
                kk = cube[ce + 2];

                //do algorithm here

                rMatte = ii - (jj + kk) / 2.0f;
                gMatte = jj - (ii + kk) / 2.0f;
                bMatte = kk - (jj + ii) / 2.0f;

                //try a gamut clamp or soft clamp here.....
                rMatte = CLAMP(1.0f - rMatte, 0.0f, 1.0f);
                gMatte = CLAMP(1.0f - gMatte, 0.0f, 1.0f);
                bMatte = CLAMP(1.0f - bMatte, 0.0f, 1.0f);

                ii = ii * gMatte * bMatte * amount + ii * (1.0f - amount);
                jj = jj * rMatte * bMatte * amount + jj * (1.0f - amount);
                kk = kk * rMatte * gMatte * amount + kk * (1.0f - amount);

                cube[ce] = ii;
                cube[ce + 1] = jj;
                cube[ce + 2] = kk;

                out[m++] = Get_RGB_Color_Value(ii, jj, kk);
            }
        }
    }

    return out;
}

void *CreativeLut_Filum2(int *out, int size, float amount) {
    float *cube = createDefaultCube(size);

    int i, j, k;
    int ce;
    float ii, jj, kk;
    int kBase, jBase;
    const int size2 = size * size;
    float contrastAmount = 1.0f;
    float baseLevel = -0.005f;

    float r, g, b, c, m, y;

    float vector[3] = {0.8f, 0.90f, 1.0f};

    float midGrey = 1.0f / 3.0f;
    c = baseLevel + (1.0f - midGrey);
    m = baseLevel + (1.0f - midGrey);
    y = baseLevel + (1.0f - midGrey);
    basicLumaContrast(&c, &m, &y, contrastAmount);
    r = (1.0f - c) - baseLevel;
    g = (1.0f - c) - baseLevel;
    b = (1.0f - c) - baseLevel;

    float midGreyCorrectionRatio = midGrey / g;

    int x = 0;
    for (k = 0; k < size; k++) {
        kBase = size2 * k;
        for (j = 0; j < size; j++) {
            jBase = size * j;
            for (i = 0; i < size; i++) {
                ce = (i + jBase + kBase) * 3;

                ii = cube[ce];
                jj = cube[ce + 1];
                kk = cube[ce + 2];

                //do algorithm here

                r = ii;
                g = jj;
                b = kk;

                c = baseLevel + (1.0f - r * vector[0]);
                m = baseLevel + (1.0f - g * vector[1]);
                y = baseLevel + (1.0f - b * vector[2]);

                basicLumaContrast(&c, &m, &y, contrastAmount);

                ii = midGreyCorrectionRatio * (1.0f - c) / vector[0] - baseLevel;
                jj = midGreyCorrectionRatio * (1.0f - m) / vector[1] - baseLevel;
                kk = midGreyCorrectionRatio * (1.0f - y) / vector[2] - baseLevel;

                ii = (amount * ii) + ((1.0f - amount) * r);
                jj = (amount * jj) + ((1.0f - amount) * g);
                kk = (amount * kk) + ((1.0f - amount) * b);

                cube[ce] = ii;
                cube[ce + 1] = jj;
                cube[ce + 2] = kk;

                out[x++] = Get_RGB_Color_Value(ii, jj, kk);
            }
        }
    }

    return out;
}


void basicLumaContrast(float *r, float *g, float *b, float bleachAmount) {
    float ii = *r;
    float jj = *g;
    float kk = *b;

    //float L = MAXOF2(ii, MAXOF2(jj,kk));
    float L = sqrt(ii * ii + jj * jj + kk * kk) / 1.7321f;

    if (L > 0.0f) {

        float C = smoothstep(L);

        float ratio = (C / L);

        float R = ii * ratio;
        float G = jj * ratio;
        float B = kk * ratio;

        *r = smoothstepInverse(R) * bleachAmount + ii * (1.0f - bleachAmount);
        *g = smoothstepInverse(G) * bleachAmount + jj * (1.0f - bleachAmount);
        *b = smoothstepInverse(B) * bleachAmount + kk * (1.0f - bleachAmount);

    }
}

float smoothstepInverse(float x) {
    // Scale, bias and saturate x to 0..1 range
    x = CLAMP(x, 0.0f, 1.0f);
    // Evaluate polynomial
    return x + (x - (x * x * (3.0f - 2.0f * x)));
}

void BleachBoost(float *r, float *g, float *b, float stopsBoost, float bleachAmount) {
    float ii = *r;
    float jj = *g;
    float kk = *b;

    ii = Log3G10ExposureChangeSmooth(ii, stopsBoost);
    jj = Log3G10ExposureChangeSmooth(jj, stopsBoost);
    kk = Log3G10ExposureChangeSmooth(kk, stopsBoost);

    //do bleach here
    float L = MAXOF2(ii, MAXOF2(jj, kk));
    if (L > 0.0f) {

        float C = smoothstep(smoothstep(L));
        float ratio = (C / L);

        float R = ii * ratio;
        float G = jj * ratio;
        float B = kk * ratio;

        *r = smoothstepInverse(R) * bleachAmount + ii * (1.0f - bleachAmount);
        *g = smoothstepInverse(G) * bleachAmount + jj * (1.0f - bleachAmount);
        *b = smoothstepInverse(B) * bleachAmount + kk * (1.0f - bleachAmount);

    }
}

float Log3G10ExposureChangeSmooth(float x, float stops) {
    const float gain = powf(2.0f, stops);
    const float end = 0.18f * 1024.0f;
    const float A = end / (gain * 2.0f);//start point
    const float B = end * gain;//end x
    const float C = end;//end y

    x = log3G10OffsetInverseV3(x);

    x = x > 0.0f ? x * gain : x;
    x = softClampTop(x, A, B, C);
    x = log3G10OffsetV3(x);

    x = CLAMPTOP(x, 1.0f);

    return x;
}

float log3G10OffsetInverseV3(float x) {
    const float a = 0.224282f;
    const float b = 155.975327f;
    const float c = 0.01f;
    const float g = 15.1927f;

    if (x < 0.0f) {
        return (x / g) - c;
    }
    const float output = (powf(10.0f, x / a) - 1.0f) / b;
    return output - c;
}

float softClampTop(float x, float a, float b, float c) {
    //a < c < b
    if (x < a) {
        return x;
    }
    if (x > b) {
        return c;
    }
    const float p = (a - b) / (a - c);
    return powf((b - x) / (b - a), p) * (a - c) + c;
}

float log3G10OffsetV3(float x) {
    //Log3G targets mid grey to 1/3
    const float a = 0.224282f;
    const float b = 155.975327f;
    const float c = 0.01f;
    const float g = 15.1927f;

    x = x + c;

    if (x < 0.0f) {
        return x * g;
    }

    const float output = a * log10f((x * b) + 1.0f);
    return output;
}


float smoothstep(float x) {
    // Scale, bias and saturate x to 0..1 range
    x = CLAMP(x, 0.0f, 1.0f);
    // Evaluate polynomial
    return x * x * (3.0f - 2.0f * x);
}

JNIEXPORT void JNICALL
Java_com_jni_CreativeCube_creativeCubeBleachBypass(JNIEnv *env, jclass type, jintArray out_,
                                                   jint size,
                                                   jfloat amount) {
    jint *out = (*env)->GetIntArrayElements(env, out_, NULL);

    CreativeLut_BleachBypass(out, size, amount);

    (*env)->ReleaseIntArrayElements(env, out_, out, 0);
}

JNIEXPORT void JNICALL
Java_com_jni_CreativeCube_creativeCubeFilum2(JNIEnv *env, jclass type, jintArray out_, jint size,
                                             jfloat amount) {
    jint *out = (*env)->GetIntArrayElements(env, out_, NULL);

    CreativeLut_Filum2(out, size, amount);

    (*env)->ReleaseIntArrayElements(env, out_, out, 0);
}

JNIEXPORT void JNICALL
Java_com_jni_CreativeCube_creativeCube3Strip(JNIEnv *env, jclass type, jintArray out_, jint size,
                                             jfloat amount) {
    jint *out = (*env)->GetIntArrayElements(env, out_, NULL);

    CreativeLut_3Strip(out, size, amount);

    (*env)->ReleaseIntArrayElements(env, out_, out, 0);
}