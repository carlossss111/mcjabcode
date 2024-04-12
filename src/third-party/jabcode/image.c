/*
 * Modified 28 Feb, 2024. Daniel R
*/

/**
 * libjabcode - JABCode Encoding/Decoding Library
 *
 * Copyright 2016 by Fraunhofer SIT. All rights reserved.
 * See LICENSE file for full terms of use and distribution.
 *
 * Contact: Huajian Liu <liu@sit.fraunhofer.de>
 *			Waldemar Berchtold <waldemar.berchtold@sit.fraunhofer.de>
 *
 * @file image.c
 * @brief Read and save png image
 */

#include <stddef.h>
#include <stdlib.h>
#include <string.h>
#include "jabcode.h"
#include "png.h"

/**
 * @brief Initialises PNG image
 * @param bitmap the code bitmap
 * @param image the image to initialise
 */
void initImageStructure(jab_bitmap* bitmap, png_image* image) {
    image->version = PNG_IMAGE_VERSION;

    if(bitmap->channel_count == 4)
    {
		image->format = PNG_FORMAT_RGBA;
		image->flags  = PNG_FORMAT_FLAG_ALPHA | PNG_FORMAT_FLAG_COLOR;
    }
    else
    {
		image->format = PNG_FORMAT_GRAY;
    }

    image->width  = bitmap->width;
    image->height = bitmap->height;
}

/**
 * @brief Save code bitmap in RGB as png image
 * @param bitmap the code bitmap
 * @param filename the image filename
 * @return JAB_SUCCESS | JAB_FAILURE
*/
jab_boolean saveImage(jab_bitmap* bitmap, jab_char* filename)
{
    png_image image;
    memset(&image, 0, sizeof(image));
    initImageStructure(bitmap, &image);

    if (png_image_write_to_file(&image,
        filename,
        0/*convert_to_8bit*/,
        bitmap->pixel,
        0/*row_stride*/,
        NULL/*colormap*/) == 0)
	{
		reportError(image.message);
		reportError("Saving png image failed");
		return JAB_FAILURE;
	}
	return JAB_SUCCESS;
}

/**
 * @brief Save code bitmap in RGB to memory buffer
 * @param bitmap the code bitmap
 * @param buffer the memory to write to
 * @param bufferSize the size of the memory to write to
 * @return int JAB_SUCCESS | JAB_FAILURE
 */
int saveImageToMemory(jab_bitmap* bitmap, jab_char* buffer, size_t bufferSize)
{
    png_image image;
    memset(&image, 0, sizeof(image));
    initImageStructure(bitmap, &image);

    // write to buffer
    if(png_image_write_to_memory(
        &image,
        buffer,
        &bufferSize,
        0/*convert_to_8bit*/,
        bitmap->pixel,
        0/*row_stride*/,
        NULL/*colormap*/) == 0)
	{
		reportError(image.message);
		reportError("Storing png image to memory failed");
		return JAB_FAILURE;
    }
        
    return JAB_SUCCESS;
}

/**
 * @brief Get the memory size of the bitmap if it were a PNG image
 * @param bitmap the code bitmap
 * @return jab_int32 expected size of
 */
jab_int32 getImageSize(jab_bitmap* bitmap)
{
    png_image image;
    memset(&image, 0, sizeof(image));
    initImageStructure(bitmap, &image);
	
	// get buffer size
	png_alloc_size_t bufferSize;
	if(png_image_write_get_memory_size(image, bufferSize, 0, bitmap->pixel, 0, NULL) == 0){
		return 0;
	}
	return bufferSize;
}


/**
 * @brief Convert a bitmap from RGB to CMYK color space
 * @param bitmap the bitmap in RGB
 * @return the bitmap in CMYK | JAB_FAILURE
*/
jab_bitmap* convertRGB2CMYK(jab_bitmap* rgb)
{
	if(rgb->channel_count < 3)
	{
		JAB_REPORT_ERROR(("Not true color RGB bitmap"))
        return JAB_FAILURE;
	}
	jab_int32 w = rgb->width;
	jab_int32 h = rgb->height;
	jab_bitmap* cmyk = (jab_bitmap *)malloc(sizeof(jab_bitmap) + w*h*BITMAP_CHANNEL_COUNT*sizeof(jab_byte));
	if(cmyk == NULL)
    {
        JAB_REPORT_ERROR(("Memory allocation for CMYK bitmap failed"))
        return JAB_FAILURE;
    }
    cmyk->width = w;
    cmyk->height= h;
    cmyk->bits_per_pixel = BITMAP_BITS_PER_PIXEL;
    cmyk->bits_per_channel = BITMAP_BITS_PER_CHANNEL;
    cmyk->channel_count = BITMAP_CHANNEL_COUNT;

	jab_int32 rgb_bytes_per_pixel = rgb->bits_per_pixel / 8;
    jab_int32 rgb_bytes_per_row = rgb->width * rgb_bytes_per_pixel;
    jab_int32 cmyk_bytes_per_pixel = rgb->bits_per_pixel / 8;
    jab_int32 cmyk_bytes_per_row = rgb->width * cmyk_bytes_per_pixel;


    for(jab_int32 i=0; i<h; i++)
	{
		for(jab_int32 j=0; j<w; j++)
		{
			jab_double r1 = (jab_double)rgb->pixel[i*rgb_bytes_per_row + j*rgb_bytes_per_pixel + 0] / 255.0;
			jab_double g1 = (jab_double)rgb->pixel[i*rgb_bytes_per_row + j*rgb_bytes_per_pixel + 1] / 255.0;
			jab_double b1 = (jab_double)rgb->pixel[i*rgb_bytes_per_row + j*rgb_bytes_per_pixel + 2] / 255.0;

			jab_double k = 1 - MAX(r1, MAX(g1, b1));

			if(k == 1)
			{
				cmyk->pixel[i*cmyk_bytes_per_row + j*cmyk_bytes_per_pixel + 0] = 0;	//C
				cmyk->pixel[i*cmyk_bytes_per_row + j*cmyk_bytes_per_pixel + 1] = 0;	//M
				cmyk->pixel[i*cmyk_bytes_per_row + j*cmyk_bytes_per_pixel + 2] = 0;	//Y
				cmyk->pixel[i*cmyk_bytes_per_row + j*cmyk_bytes_per_pixel + 3] = 255;	//K
			}
			else
			{
				cmyk->pixel[i*cmyk_bytes_per_row + j*cmyk_bytes_per_pixel + 0] = (jab_byte)((1.0 - r1 - k) / (1.0 - k) * 255);	//C
				cmyk->pixel[i*cmyk_bytes_per_row + j*cmyk_bytes_per_pixel + 1] = (jab_byte)((1.0 - g1 - k) / (1.0 - k) * 255);	//M
				cmyk->pixel[i*cmyk_bytes_per_row + j*cmyk_bytes_per_pixel + 2] = (jab_byte)((1.0 - b1 - k) / (1.0 - k) * 255);	//Y
				cmyk->pixel[i*cmyk_bytes_per_row + j*cmyk_bytes_per_pixel + 3] = (jab_byte)(k * 255);								//K
			}
		}
	}
    return cmyk;
}

/**
 * @brief Read image into code bitmap
 * @param filename the image filename
 * @return Pointer to the code bitmap read from image | NULL
*/
jab_bitmap* readImage(jab_char* filename)
{
	png_image image;
    memset(&image, 0, sizeof(image));
    image.version = PNG_IMAGE_VERSION;

	jab_bitmap* bitmap;

    if(png_image_begin_read_from_file(&image, filename))
	{
		image.format = PNG_FORMAT_RGBA;

		bitmap = (jab_bitmap *)calloc(1, sizeof(jab_bitmap) + PNG_IMAGE_SIZE(image));
		if(bitmap == NULL)
        {
			png_image_free(&image);
			reportError("Memory allocation failed");
			return NULL;
		}
		bitmap->width = image.width;
		bitmap->height= image.height;
		bitmap->bits_per_channel = BITMAP_BITS_PER_CHANNEL;
		bitmap->bits_per_pixel = BITMAP_BITS_PER_PIXEL;
		bitmap->channel_count = BITMAP_CHANNEL_COUNT;

        if(png_image_finish_read(&image,
								 NULL/*background*/,
								 bitmap->pixel,
								 0/*row_stride*/,
								 NULL/*colormap*/) == 0)
		{
			free(bitmap);
			reportError(image.message);
			reportError("Reading png image failed");
			return NULL;
		}
	}
	else
	{
		reportError(image.message);
		reportError("Opening png image failed");
		return NULL;
	}
	return bitmap;
}

jab_bitmap* readImageFromMemory(jab_byte* pngMemory, jab_int32 pngMemorySize)
{
	png_image image;
    memset(&image, 0, sizeof(image));
    image.version = PNG_IMAGE_VERSION;

	jab_bitmap* bitmap;

    if(png_image_begin_read_from_memory(&image, pngMemory, pngMemorySize))
	{
		image.format = PNG_FORMAT_RGBA;

		bitmap = (jab_bitmap *)calloc(1, sizeof(jab_bitmap) + PNG_IMAGE_SIZE(image));
		if(bitmap == NULL)
        {
			png_image_free(&image);
			reportError("Memory allocation failed");
			return NULL;
		}
		bitmap->width = image.width;
		bitmap->height= image.height;
		bitmap->bits_per_channel = BITMAP_BITS_PER_CHANNEL;
		bitmap->bits_per_pixel = BITMAP_BITS_PER_PIXEL;
		bitmap->channel_count = BITMAP_CHANNEL_COUNT;

        if(png_image_finish_read(&image,
								 NULL/*background*/,
								 bitmap->pixel,
								 0/*row_stride*/,
								 NULL/*colormap*/) == 0)
		{
			free(bitmap);
			reportError(image.message);
			reportError("Reading png image failed");
			return NULL;
		}
	}
	else
	{
		reportError(image.message);
		reportError("Opening png image failed");
		return NULL;
	}
	return bitmap;
}
