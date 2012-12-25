/*
 * raw_sokcet_op.h
 *
 *  Created on: Nov 25, 2012
 *      Author: fabio
 */

#ifndef RAW_SOCKET_OP_H_
#define RAW_SOCKET_OP_H_

#include <asm/types.h>
#include "fast_broadcast.h"
#include "sckt_descr.h"

#define SOCKET_OP_ERROR 0

typedef struct pkt_buffer pkt_buffer;

typedef struct pkt_buffer {

	unsigned char *buffer;
	unsigned int length;
	pkt_buffer *next;
}pkt_buffer;

int send_data(char* dest, struct sckt_descr *soc_desc, char* data, int length, int prot_type);
char* read_data(struct sckt_descr * socket_descriptor, int prot_type, char **src_add, unsigned int *read);
//void clear_pkt_buffer(pkt_buffer *buff);

#endif /* RAW_SOKCET_OP_H_ */
