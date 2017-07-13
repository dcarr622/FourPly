LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

include $(LOCAL_PATH)/../../../../OpenCV-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := argraffiti
LOCAL_SRC_FILES := argraffiti.cpp
LOCAL_LDLIBS +=  -llog -ldl -latomic
LOCAL_CFLAGS := -std=c++11

include $(BUILD_SHARED_LIBRARY)