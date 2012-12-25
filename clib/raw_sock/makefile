TARGET = raw_socket
CC = gcc
CFLAGS = -g -Wall -I. -lm -fPIC
SRC = ${RELPATH}src/
LIB = ${RELPATH}lib/

all : $(TARGET)
	@echo all done

$(TARGET) : main.o raw_socket.o conversion_helper.o raw_socket_op.o wifi_nic.o sckt_descr.o
	@echo 'building target $@: $^'
	$(CC) -o $@ $^ $(CFLAGS)

main.o : $(SRC)main.c $(LIB)raw_socket.h $(LIB)conversion_helper.h $(LIB)fast_broadcast.h $(LIB)raw_socket_op.h
	@echo 'building target $@: $<'
	$(CC) -o $@ -c $< $(CFLAGS)

raw_socket.o : $(SRC)raw_socket.c $(LIB)wifi_nic.h $(LIB)raw_socket.h $(LIB)conversion_helper.h $(LIB)fast_broadcast.h
	@echo 'building target $@: $<'
	$(CC) -o $@ -c $< $(CFLAGS)

conversion_helper.o : $(SRC)conversion_helper.c $(LIB)conversion_helper.h
	@echo 'building target $@: $<'
	$(CC) -o $@ -c $< $(CFLAGS)

raw_socket_op.o : $(SRC)raw_socket_op.c $(LIB)raw_socket_op.h $(LIB)raw_socket.h $(LIB)conversion_helper.h $(LIB)ieee_80211_radiotap.h $(LIB)ieee_80211.h
	@echo 'building target $@: $<'
	$(CC) -o $@ -c $< $(CFLAGS)

wifi_nic.o : $(SRC)wifi_nic.c $(LIB)wifi_nic.h
	@echo 'building target $@: $<'
	$(CC) -o $@ -c $< $(CFLAGS)

sckt_descr.o: $(SRC)sckt_descr.c $(LIB)sckt_descr.h
	@echo 'building target $@: $<'
	$(CC) -o $@ -c $< $(CFLAGS)

shared:
	gcc -shared -o libraw_socket.so raw_socket.o conversion_helper.o raw_socket_op.o wifi_nic.o sckt_descr.o

clean:
	rm -f *.o raw_socket
	@echo 'cleaned'
