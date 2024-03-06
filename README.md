# Using High-Capacity Encodings to represent Game Data
('Hybrid-Arcade' project for short)

## Description
This is part of a research project for the University of Nottingham. The research is about analysing and selecting  different physical data representations (e.g. barcodes) so that a large amount of game data can be stored and transported physically without using a network or a database.

Minecraft was selected as an example a, since it is very modifiable and it has 3-dimensional spaces that would need a high capacity to be stored. The mod should work like so:
1. A player can select a 3-dimensional space in the game.
1. The selected space can be printed out as an encoding on a card or saved as an image.
1. Later, the encoding can be scanned to bring the selected space back into the game's memory, even on other game-worlds or PCs.
1. The selected space can be placed back into the game, along with all the blocks that were originally in the space.

## Setup
There are 3 stages to setup: compiling the C dependencies, compling the C local binary and connecting a scanner webserver.

The project uses the Java JVM with Gradle for most of the work, and uses compiled C code for the 
[JabCode data encodings][jabcode_library] because it's open-source library is written in C. These 
instructions are for a Linux operating system.

### Compiling Dependencies
The libraries used by the core jabcode library need to be compiled with special compiler instructions
so that they work within the Java Native Interface (JNI).

#### Libz
* Download and extract the latest version: https://www.zlib.net/. 
* Edit the `Makefile` so that the CFLAGS and SFLAGs contain the following:
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

### Compiling Local Files
Running this repository's makefile with the correct statically linked binaries, you should see libJabEncoder.so 
be created in the resources/encoding directory.
```
make compile
```
If there are errors, they may be because `-fPIC -D_REENTRANT -D_POSIX_C_SOURCE` was not
set when compiling **every** library.

Changes to the `src/main/c/JabEncoder.c` require this makefile to be re-run before running.

### Setting up the Scanner
To use the scanning functionality, a local network server with a camera needs to be setup on the network to listen for requests 
and return a PNG image of what it can see in-front of it. The scanner needs to be listening at **http[]()://raspberrypi.local:5000**.
There is no need to connect it to the internet.

This can be done with a Raspberry Pi, a simple [Flask server][flask] and the [Picamera2 library][picam].
This repository contains [the python file][pyfile] used for this purpose.

The webserver can be started with e.g.
```
flask --app scanner.py run --host=0.0.0.0
```

### Setting up the Printer
The game uses the system print drivers and a connected printer, there is no configuration needed.

## Running the Game Client
Given that the C library is compiled, tests can be run with
```
./gradlew test
```
and the application can be run with
```
./gradlew runClient
```
The Java code doesn't need to be compiled manually as it is handled by the JVM.

## Usage In-game
In a new 'creative world' give yourself 'Marker Blocks', some other building blocks of your choice, 
a 'Red Magic Wand' and a 'Blue Magic Wand'. Now you can:

### Copy to encoding
* Build something and surround it with marker blocks in two 'L' shapes.
* Left-click the marker blocks with the Red Wand to select them.
* Right-click after selecting to copy them. This will save an encoding to the disk!

Copying a House:

![Copied House][copy_image]

The encoding using the House's data:

![Created Encoding][copy_barcode]

### Paste from encoding
* Place your new encoding in-front of the scanner, using a phone or a print-out.
* Right-click the Blue Wand on the ground to scan.
* Place down a marker block and left-click it with the Wand to paste the blocks back into the game!

The house replicated somewhere entirely new by scanning the encoding:

![Pasted House][paste_image]

## Javadocs
Generate javadocs to the `docs/` directory with
```
./gradlew javadoc
```
and open in a web browser.

## Authors
Written by: Daniel Robinson / psydr2@nottingham.ac.uk

Supervised by: Steve Bagley

## Licenses
* The project is licensed under GNU GPLv3, see [LICENSE.TXT][license]. A copy of the source code may be made available if not already attached with this file.
* Minecraft Forge Library is provided with GNU LGPLv2.1, [see link][mcf_license].
* JabCode Library is provided with GNU LGPLv2.1, [see link][jcode_license], and I have modified it in a [related repository][jabcode_fork] in compliance with part 2 of the license.
* LibPNG is provided with a permissive license, [see link][lpng_license].
* ZLib is provided with a permissive license, [see link][zlib_license].
* Dependencies imported from Gradle like Log4j and JUnit have licenses which allow their use.

[jabcode_library]: https://jabcode.org/ "Jabcode Library"
[license]: LICENSE.txt "Project License"
[jabcode_fork]: https://github.com/carlossss111/jabcode_fork "Jabcode Fork"
[jcode_license]: https://github.com/jabcode/jabcode/blob/master/LICENSE "Jabcode License"
[mcf_license]: https://github.com/MinecraftForge/MinecraftForge/blob/1.20.x/LICENSE.txt "Minecraft Forge Library License"
[lpng_license]: http://www.libpng.org/pub/png/src/libpng-LICENSE.txt "LibPNG License"
[zlib_license]: https://www.zlib.net/zlib_license.html "ZLib License"
[flask]: https://flask.palletsprojects.com/en/3.0.x/ "Flask Website"
[picam]: https://datasheets.raspberrypi.com/camera/picamera2-manual.pdf "Picamera 2 Datasheet"
[pyfile]: src/external/python/scanner.py "Scanner Python File"

[copy_image]: images/copy_house.png "Image of house being copied" 
[copy_barcode]: images/copy_barcode.png "Image of barcode produced by copying house"
[paste_image]: images/paste_house.png "Image of house being pasted from the barcode"