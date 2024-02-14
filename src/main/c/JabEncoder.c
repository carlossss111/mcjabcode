#include "JabEncoder.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "jabcode.h"

#define NUM_OF_COLOURS 8
#define NUM_OF_SYMBOLS 1

struct jab_png{
	jab_byte* buffer;
	jab_int32 size;
}typedef jab_png;

jab_png* myEncode(jab_byte *streamIn, jab_int32 streamInLength){
	// Initialise encoding
	jab_encode* encoding = createEncode(NUM_OF_COLOURS, NUM_OF_SYMBOLS);
	jab_data* encodingData = (jab_data*) malloc(sizeof(jab_data) + streamInLength * sizeof(jab_char));
	encodingData->length = streamInLength;
	memcpy(encodingData->data, streamIn, streamInLength);

	// Generate JABcode
	int exitCode = generateJABCode(encoding, encodingData);
	if(exitCode != 0){
		free(encodingData);
		destroyEncode(encoding);
		return NULL;
	}

	// Convert to PNG and copy to memory
	jab_png *png = malloc(sizeof(jab_png));
	png->size = getImageSize(encoding->bitmap);
	png->buffer = malloc(sizeof(jab_char) * png->size);
	saveImageToMemory(encoding->bitmap, png->buffer, png->size);

	free(encodingData);
	destroyEncode(encoding);
    return png;
}

JNIEXPORT jbyteArray JNICALL Java_uk_ac_nottingham_hybridarcade_encoding_JabEncoder_saveEncoding
  (JNIEnv* env, jobject obj, jbyteArray jStreamIn){
	
	// Types: Java -> C
	jab_byte *streamIn = (*env)->GetByteArrayElements(env, jStreamIn, 0);
	jab_int32 streamInLength = (*env)->GetArrayLength(env, jStreamIn);

	// Do Work
	jab_png *png = myEncode(streamIn, streamInLength);
	if(!png){
		(*env)->ReleaseByteArrayElements(env, jStreamIn, streamIn, 0);
		return NULL;
	}

	// Types: C -> Java
	jbyteArray streamOut = (*env)->NewByteArray(env, png->size);
	(*env)->SetByteArrayRegion(env, streamOut, 0, png->size, png->buffer);

	free(png->buffer);
	free(png);
	(*env)->ReleaseByteArrayElements(env, jStreamIn, streamIn, 0);
	return streamOut;
}

