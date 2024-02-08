PREFIX 	=
CC 	= $(PREFIX)gcc
CFLAGS	 = -O2 -std=c11

compile:
	$(CC) $(CFLAGS) -c -fPIC -I. -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -Ijabcode -Isrc/main/c/jabcode/include -O2 -std=c11 src/main/c/JabEncoder.c -o src/main/c/JabEncoder.o
	$(CC) $(CFLAGS) -shared -fPIC src/main/c/JabEncoder.o -Lsrc/main/c/jabcode/build -ljabcode -Lsrc/main/c/jabcode/lib -ltiff -lpng16 -lz -lm -O2 -std=c11 -o src/main/resources/encoding/libJabEncoder.so -lc
	rm src/main/c/JabEncoder.o

clean:
	rm src/main/resources/encoding/*.so
