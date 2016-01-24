LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

include ../../../../../OpenCV-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := argraffiti
LOCAL_SRC_FILES := argraffiti.cpp
LOCAL_LDLIBS +=  -llog -ldl
LOCAL_CFLAGS := -std=c++11

include $(BUILD_SHARED_LIBRARY)