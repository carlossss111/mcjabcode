# Compiling Dependencies
The libraries used by the core jabcode library need to be compiled with special compiler instructions
so that they work within the Java Native Interface (JNI).

They have been precompiled for you in the 'src/main/c/clib' directory, but the instructions are below
should you need to recompile them yourself.

#### Libz
* Download and extract the latest version: https://www.zlib.net/.
* Edit the `Makefile` so that the CFLAGS and SFLAGs contain the following:
```makefile
CFLAGS=-O3 -fPIC -D_LARGEFILE64_SOURCE=1 -DHAVE_HIDDEN -D_REENTRANT -D_POSIX_C_SOURCE
SFLAGS=-O3 -fPIC -D_LARGEFILE64_SOURCE=1 -DHAVE_HIDDEN -D_REENTRANT -D_POSIX_C_SOURCE
```
* __If you are targeting windows__ then also change the compiler, archiver and linker:
```makefile
CC=x86_64-w64-mingw32-gcc
LD=x86_64-w64-mingw32-ld
AR=x86_64-w64-mingw32-ar
AS=x86_64-w64-mingw32-as
```

* Then compile with the following commands:
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
* __Linux Target__:
```
./configure CFLAGS="-fPIC -D_REENTRANT -D_POSIX_C_SOURCE" CXXFLAGS="-fPIC -D_REENTRANT -D_POSIX_C_SOURCE" --libdir=/home/$USER/Downloads/libpng_compiled
make clean
make install
```
* __Windows Target__:
```
LDFLAGS="-L../zlib-1.2.11" CC="x86_64-w64-mingw32-gcc" CPPFLAGS="-I../zlib-1.2.11" CFLAGS="-I../zlib-1.2.11 -fPIC -D_REENTRANT -D_POSIX_C_SOURCE" CXXFLAGS="-fPIC -D_REENTRANT -D_POSIX_C_SOURCE" CC="x86_64-w64-mingw32-gcc" LD="x86_64-w64-mingw32-ld" AR="x86_64-w64-mingw32-ar" AS="x86_64-w64-mingw32-as"  ./configure --prefix=/usr/bin/x86_64-w64-mingw32 --host=x86-windows --libdir=/home/$USER/Downloads/libpng_compiled
make clean
make install
```
* Verify that `libpng16.a` exists inside of the `~/Downloads/libpng_compiled` directory.

#### Jabcode Fork
* Clone the forked jabcode repository: https://github.com/carlossss111/jabcode_fork
* Copy the `libz.a` and `libpng16.a` that have just been compiled, and move them into `src/jabcode/lib`, replacing
  the already existing library files.
* Compile with the relevant Makefile inside of `src/jabcode`. The binary should be created inside `src/jabcode/build`.
* If you have compiled for __Linux__, copy only the `libjabcode.a` file into this repository's `src/main/c/clib`.
* Else, if you have compiled for __Windows__, copy the `libjabcode.a` and the `libz.a` and `libpng16.a` into `src/main/c/clib/win64`.

### Compiling Local Files
Running this repository's makefile with the correct statically linked binaries, you should see libJabEncoder.so
be created in the resources/encoding directory.
```bash
# Linux Target
./gradlew compileCLibrary
# Windows Target
./gradlew compileCLibraryWin
```

Changes to the `src/main/c/JabEncoder.c` require this gradle command to be re-run before running.