PREFIX 	=
CC 	= $(PREFIX)gcc
CFLAGS	 = -O2 -std=c11 -fPIC

INCLUDE_DIRS = -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -Isrc/main/c/cinclude
LINK_DIRS = -Lsrc/main/c/clib
LINKS = -ljabcode -lpng16 -lz -lm -lc
RM = rm

compile:
	$(CC) $(CFLAGS) $(INCLUDE_DIRS) -c src/main/c/JabEncoder.c -o src/main/c/JabEncoder.o
	$(CC) $(CFLAGS) -shared src/main/c/JabEncoder.o $(LINK_DIRS) $(LINKS) -o src/main/resources/encoding/libJabEncoder.so
	$(RM) src/main/c/JabEncoder.o

clean:
	$(RM) src/main/resources/encoding/libJabEncoder.so
