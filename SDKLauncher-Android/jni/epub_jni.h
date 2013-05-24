/*!
 * \author chtian
 * \brief The epub java native interface define
 * \file epub_jni.h
 * */

#include <jni.h>
#include <string.h>

#ifndef _EPUB_JNI_H_
#define _EPUB_JNI_H_
#ifdef __cplusplus
extern "C" {
#endif

jobject Java_com_readium_EPubJNI_openBook(JNIEnv* env,
										jobject this,
										jstring jPath);


void Java_com_readium_EPubJNI_closeBook(JNIEnv* env,
										jobject this,
										jobject container);

#ifdef __cplusplus
}
#endif
#endif
