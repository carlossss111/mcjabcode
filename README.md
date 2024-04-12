# Using High-Capacity Encodings to represent Game Data
('Hybrid-Arcade' project for short)

## Description
This is part of a dissertation for the University of Nottingham. The research is about analysing and selecting  different physical data representations (e.g. barcodes) and compression methods so that a large amount of game data can be stored and transported physically without using a network or a database.

This project allows a 3D space in Minecraft to be selected and printed physically with a barcode that represents all the blocks inside the 3D space. The barcode can then be scanned later to recreate the blocks in another Minecraft world.

### Functionality

In a new 'creative world' give yourself 'Marker Blocks', some other building blocks of your choice,
a 'Red Magic Wand' and a 'Blue Magic Wand'. Now you can:

#### Copy to encoding
1. Build something and surround it with marker blocks in two 'L' shapes.
2. Left-click the marker blocks with the Copy Wand to select them.
3. Right-click after selecting to copy them. This will save an encoding to the disk!

Copying a House:

![Copied House][copy_image]

The encoding using the House's data (as of v1.5.3):

![Created Encoding][copy_barcode]

#### Paste from encoding
* Place your new encoding in-front of the scanner, using a phone or a print-out.
* Right-click the Paste Wand on the ground to scan.
* Place down a marker block and left-click it with the wand to paste the blocks back into the game!

The house replicated somewhere entirely new by scanning the encoding:

![Pasted House][paste_image]

## Building
To build and install the modification, first compile the C library on a linux machine and then run the build command to compile and build the Java code. Requires GCC for a linux target and x86_64-w64-mingw32-gcc for a windows target.
```bash
# Linux Target
./gradlew compileCLibrary build
# Windows Target
./gradlew compileCLibraryWin build
```
The built library will be located in 'build/libs/jar'.

The C library uses the jabcode, png and Z libraries that have been precompiled for x86_64 linux and windows at 'src/main/c/clib'. If there are issues linking them on your target system them please refer to [Library Help document][library_help].

If at this point you would like to skip the installation and run the program in a test environment,
run `./gradlew runClient`.

## Installing
1. Locate the hybrid-arcade jar in 'build/libs/jar', the 'blockmap256.json' in 'build/resources/main/data/hybridarcade/blockmap256.json' and the library files in 'build/resources/main/encoding'.
2. Install the [Minecraft Forge Mod Loader v47.2.1][forge_download].
3. Move the .jar, .json and library files (DLL for windows, SO for linux) into your .minecraft/mods folder.
4. Start the minecraft launcher and select the Forge option. Go to advanced options and add '-Djava.library.path=PATH/TO/YOUR/MODS/FOLDER' to the JVM argument string.

The modification should now be installed and ready to go!

## Adding a Scanner
By default, the scanner is mocked with `MOCK_SCANNER = true` enabled in PasteItem.java, therefore it reads an image at 'mock_barcode.png' in the minecraft folder. To enable the scanner set this to false and set up a network scanner.

A local network server with a camera needs to be setup on the network to listen for requests 
and return a PNG image of what it can see in-front of it. The scanner needs to be listening at **http[]()://raspberrypi.local:5000**.
There is no need to connect it to the internet.

This can be done with a Raspberry Pi, a simple [Flask server][flask] and the [Picamera2 library][picam].
This repository contains [the python file][pyfile] used for this purpose.

The webserver can be started with e.g.
```
flask --app scanner.py run --host=0.0.0.0
```

## Adding a Printer
By default, the printer is mocked with `MOCK_PRINTER = true` enabled in CopyItem.java, therefore it saves an image at 'mock_barcode.png' in the minecraft folder. To enable the printer set this to false and connect a printer to the network.

The game uses the system print drivers and a connected printer, there is no additional configuration needed.

## Dev Tasks
The application can be run in a locally built test environment with:
```
./gradlew runClient
```

Javadocs for all public classes can be generated to the `docs/` directory with:
```
./gradlew javadoc
```

Unit tests can be run with:
```
./gradlew test
```

Performance tests can be run and logged to the `logs/` directory with
```
./gradlew readAnalysis
./gradlew writeAnalysis
```

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

[minecraft_EULA]: https://www.minecraft.net/en-us/eula
[library_help]: src/main/c/clib/LIBRARY_README.md
[forge_download]: https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html
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