/**
 * libjabcode - JABCode Encoding/Decoding Library
 *
 * Copyright 2016 by Fraunhofer SIT. All rights reserved.
 * See LICENSE file for full terms of use and distribution.
 *
 * Contact: Huajian Liu <liu@sit.fraunhofer.de>
 *			Waldemar Berchtold <waldemar.berchtold@sit.fraunhofer.de>
 *
 * @file pseudo_random.h
 * @brief Psuedorandom header
 */

#include <inttypes.h>

#ifndef UINT32_MAX
#define UINT32_MAX 4294967295
#endif

void setSeed(uint64_t seed);
uint32_t lcg64_temper();
