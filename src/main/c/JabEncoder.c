#include "JabEncoder.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "jabcode.h"

#define NUM_OF_COLOURS 8
#define NUM_OF_SYMBOLS 1

#define FILE_PATH "jabcode.png"

JNIEXPORT void JNICALL Java_uk_ac_nottingham_hybridarcade_encoding_JabEncoder_saveEncoding
  (JNIEnv* env, jobject javaObject, jbyteArray byteData){
	
	// Convert Types to something sensible
	jab_int32 streamLength = (*env)->GetArrayLength(env, byteData);
	jab_byte *jabStream = (*env)->GetByteArrayElements(env, byteData, 0);

	// Initialise encoding and data
	jab_encode* encoding = createEncode(NUM_OF_COLOURS, NUM_OF_SYMBOLS);
	jab_data* jabDataPtr = (jab_data*) malloc(sizeof(jab_data) + streamLength * sizeof(jab_char));
	jabDataPtr->length = streamLength;
	memcpy(jabDataPtr->data, jabStream, streamLength);

	// Generate the JABcode
	int exitCode = generateJABCode(encoding, jabDataPtr);
	printf("GENERATE EXIT CODE: %d\n", exitCode);
	
	// Save to filesystem
	// Ideally in later versions, this will just be returned as a bitmap
	saveImage(encoding->bitmap, FILE_PATH);

	// Free the memory
	free(jabDataPtr);
	free(encoding);
	(*env)->ReleaseByteArrayElements(env, byteData, jabStream, 0);

	return;
}

