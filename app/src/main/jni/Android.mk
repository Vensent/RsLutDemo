LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_LDFLAGS   := -llog
LOCAL_MODULE    := libjni_creativecube
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := creative_cube.c
LOCAL_CFLAGS    += -ffast-math -O3 -funroll-loops
include $(BUILD_SHARED_LIBRARY)
