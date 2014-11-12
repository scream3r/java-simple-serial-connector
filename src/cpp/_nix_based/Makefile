CC = $(CROSS_COMPILE)g++
CFLAGS = -Wall -c -fpic

all:
	$(CC) $(CFLAGS) jssc.cpp
	$(CC) -shared -o libjSSC-$(VER)_$(ARCH).so jssc.o

clean:
	rm -rf *.o *.so
