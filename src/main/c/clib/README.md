Libraries are already compiled in the repository but may need to be re-compiled for the target system
if they are causing errors.

## Libz and Libpng
These are used for creating a PNG image of the barcode. Download each of them here:
* https://www.zlib.net/
* http://www.libpng.org/

Follow the compilation instructions and configure with the PIC flag, e.g.
```
make clean
./configure CFLAGS=-fPIC CXXFLAGS=-fPIC
make
```
Then copy the `.a` files into this directory.

## Jabcode
I have forked the JABCode library to provide some extra functionality for this project. Download here:
* https://github.com/carlossss111/jabcode_fork

Compile with:
```
cd src/jabcode
make
```
and again copy the `.a` file from the build directory into this directory.

### Licenses
The licenses are attached in this directory.