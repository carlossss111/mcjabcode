# Using High-Capacity Encodings to represent Game Data
('Hybrid-Arcade' project for short)

## Description
This is part of a research project for the University of Nottingham. The research is about analysing and selecting  different physical data representations (e.g. barcodes) so that a large amount of game data can be stored and transported physically without using a network or a database.

Minecraft was selected as an example a, since it is very modifiable and it has 3-dimensional spaces that would need a high capacity to be stored. The mod should work like so:
1. A player can select a 3-dimensional space in the game.
1. The selected space can be printed out as an encoding on a card.
1. Later, the encoding can be scanned to bring the selected space back into the game's memory, even on other game-worlds or PCs.
1. The selected space can be placed back into the game, along with all the blocks that were originally in the space.

## Compiling and Running
The project uses the Java JVM with Gradle for most of the work, and uses compiled C code for the 
[JabCode data encodings](https://jabcode.org/) because it's open-source library is written in C. These 
instructions are for a Linux operating system.

### C Compilation
The libraries used by the core jabcode library need to be compiled with special compiler instructions
so that they work within the Java Native Interface (JNI). There's a lot of Makefiles to get through...

#### Libz
* Download and extract the latest version: https://www.zlib.net/. 
* Edit the `Makefile` so that the CFLAGS and SFLAGs contain the correct:
```makefile
CFLAGS=-O3 -fPIC -D_LARGEFILE64_SOURCE=1 -DHAVE_HIDDEN -D_REENTRANT -D_POSIX_C_SOURCE
SFLAGS=-O3 -fPIC -D_LARGEFILE64_SOURCE=1 -DHAVE_HIDDEN -D_REENTRANT -D_POSIX_C_SOURCE
```
* Compile with the following commands:
```
./configure
make clean
make
```
* Verify that `libz.a` exists inside of the local directory.

#### Libpng16
* Download and extract the latest version: http://www.libpng.org/.
* Compile with the following commands. You will get some errors about libraries failing to install in
/usr/local/lib, ignore them because we don't want that to happen anyway:
```
./configure CFLAGS="-fPIC -D_REENTRANT -D_POSIX_C_SOURCE" CXXFLAGS="-fPIC -D_REENTRANT -D_POSIX_C_SOURCE" --libdir=/home/$USER/Downloads/libpng_compiled
make clean
make install
```
* Verify that `libpng16.a` exists inside of the `~/Downloads/libpng_compiled` directory.

#### Jabcode Fork
* Clone the forked jabcode repository: https://github.com/carlossss111/jabcode_fork
* Copy the `libz.a` and `libpng16.a` that have just been compiled, and move them into `src/jabcode/lib`, replacing
the already existing library files.
* Compile with the Makefile inside of `src/jabcode`. The binary should be created inside `src/jabcode/build`.
* Copy the `libjabcode.a` file into this repository's `src/main/c/clib`.

#### Compile Locally
Running this repository's makefile with the correct statically linked binaries, you should see libJabEncoder.so 
be created in the resources/encoding directory.
```
make compile
```
If there are errors, they may be because `-fPIC -D_REENTRANT -D_POSIX_C_SOURCE` was not
set when compiling **every** library.

Changes to the `src/main/c/JabEncoder.c` require this makefile to be re-run before running.

### Running
When the C library is compiled properly tests can be run with:
```
./gradlew test
```
The application can be run with:
```
./gradlew runClient
```
The Java code of course doesn't need to be compiled manually as it is handled by the JVM.

## Authors
Written by: Daniel Robinson / psydr2@nottingham.ac.uk

Supervised by: Steve Bagley

## Licenses
* The code written by me is licensed under GNU GPLv3 in LICENSE.txt, and a copy of the source code will be made available.
* The Minecraft Forge library is licensed under GNU LGPLv2 and is supplied in the forgeinfo directory.
