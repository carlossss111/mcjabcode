#include "JabEncoder.h"

JNIEXPORT void JNICALL Java_uk_ac_nottingham_hybridarcade_encoding_JabEncoder_printJab
  (JNIEnv* jniEnv, jobject javaObject, jint testInt){
    printf("Hello World from C!\n");
    printf("%d\n", testInt);


}


