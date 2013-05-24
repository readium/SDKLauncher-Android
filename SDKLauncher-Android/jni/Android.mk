LOCAL_PATH  := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := epubjni
LOCAL_C_INCLUDES := $(LOCAL_PATH)
LOCAL_SRC_FILES := epub_jni.c

include $(BUILD_SHARED_LIBRARY)
