LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OPENCV_CAMERA_MODULES:=on
OPENCV_INSTALL_MODULES:=on
OPENCV_LIB_TYPE:=SHARED

#ifdef OPENCV_ANDROID_SDK
#  ifneq ("","$(wildcard $(OPENCV_ANDROID_SDK)/OpenCV.mk)")
#    include ${OPENCV_ANDROID_SDK}/OpenCV.mk
#  else
#    include ${OPENCV_ANDROID_SDK}/sdk/native/jni/OpenCV.mk
#  endif
#else
#  include your opencv.mk
#endif

include /your path/OpenCV.mk

LOCAL_SRC_FILES  := OpenCVWrapper_jni.cpp
LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_LDLIBS     += -llog -ldl -ljnigraphics

LOCAL_MODULE     := OpenCV_Wrapper

include $(BUILD_SHARED_LIBRARY)
