PREFIX 	=
CC 	= $(PREFIX)gcc
CFLAGS	 = -O2 -std=c11

TARGET = lib

OBJECTS = $(patsubst %.c,%.o,$(wildcard *.c))

compile:
	$(CC) -c -fPIC -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux src/main/c/JabEncoder.c -o src/main/c/JabEncoder.o
	$(CC) -shared -fPIC -o lib/libjabcode.so src/main/c/JabEncoder.o -lc
	rm src/main/c/JabEncoder.o

clean:
	rm lib/*.so
