/*
* Author: Daniel Robinson 2024
*/

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

jab_png* myEncode(jab_byte *streamIn, jab_int32 streamInLength, jab_int32 eccLevel){
	// Initialise encoding
	jab_encode* encoding = createEncode(NUM_OF_COLOURS, NUM_OF_SYMBOLS);
	jab_data* encodingData = (jab_data*) malloc(sizeof(jab_data) + streamInLength * sizeof(jab_char));
	encodingData->length = streamInLength;
	memcpy(encodingData->data, streamIn, streamInLength);

	// Error Correction Level
	if(eccLevel < 0 || eccLevel > 10){
		printf("ECC Level is not between 0 and 10, exiting.\n");
		fflush(stdout);
		free(encodingData);
		destroyEncode(encoding);
		return NULL;
	}
	encoding->symbol_ecc_levels[0] = eccLevel;

	// Generate JABcode
	int exitCode = generateJABCode(encoding, encodingData);
	if(exitCode != 0){
	    printf("C Encode Status: %d\n", exitCode);
	    fflush(stdout);
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
  (JNIEnv* env, jobject obj, jbyteArray jStreamIn, jint jEccLevel){
	
	// Types: Java -> C
	jab_byte *streamIn = (*env)->GetByteArrayElements(env, jStreamIn, NULL);
	jab_int32 streamInLength = (*env)->GetArrayLength(env, jStreamIn);

	// Do Work
	jab_png *png = myEncode(streamIn, streamInLength, jEccLevel);
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

jab_data* myDecode(jab_byte *streamIn, jab_int32 streamInLength) {
	// Load PNG image from memory to a Bitmap
	jab_bitmap* bitmap = readImageFromMemory(streamIn, streamInLength);
	if(!bitmap){
		return NULL;
	}

	// Decode bitmap
	jab_int32 exitCode = 0;
	//jab_data* decodeData = decodeJABCodeEx(bitmap, NORMAL_DECODE, &exitCode, symbols, MAX_SYMBOL_NUMBER);
	jab_data* decodeData = decodeJABCode(bitmap, COMPATIBLE_DECODE, &exitCode);
	if(exitCode != 3){
		printf("C Decode Status: %d\n", exitCode);
		fflush(stdout);
		free(bitmap);
		return NULL;
	}

	free(bitmap);
	return decodeData;
}

JNIEXPORT jbyteArray JNICALL Java_uk_ac_nottingham_hybridarcade_encoding_JabEncoder_readEncoding
  (JNIEnv* env, jobject obj, jbyteArray jStreamIn) {
	// Types: Java -> C
	jab_byte *streamIn = (*env)->GetByteArrayElements(env, jStreamIn, 0);
	jab_int32 streamInLength = (*env)->GetArrayLength(env, jStreamIn);

	// Do Work
	jab_data *dataStruct = myDecode(streamIn, streamInLength);
	if(!dataStruct){
		(*env)->ReleaseByteArrayElements(env, jStreamIn, streamIn, 0);
		return NULL;
	}

	// Types: C -> Java
	jbyteArray streamOut = (*env)->NewByteArray(env, dataStruct->length);
	(*env)->SetByteArrayRegion(env, streamOut, 0, dataStruct->length, dataStruct->data);

	free(dataStruct);
	(*env)->ReleaseByteArrayElements(env, jStreamIn, streamIn, 0);
	return streamOut;
}

