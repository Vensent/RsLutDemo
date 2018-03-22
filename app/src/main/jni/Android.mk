LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_LDFLAGS   := -llog
LOCAL_MODULE    := libjni_creativecube
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := creative_cube.c
LOCAL_CFLAGS    += -ffast-math -O3 -funroll-loops
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := libjni_readingassets
LOCAL_SRC_FILES := reading_assets.cpp pystring.h pystring.cpp
LOCAL_LDLIBS    += -llog
LOCAL_LDLIBS    += -landroid
include $(BUILD_SHARED_LIBRARY)