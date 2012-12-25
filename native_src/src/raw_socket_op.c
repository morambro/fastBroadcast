/*
 * raw_socket_op.c
 *
 *  Created on: Nov 25, 2012
 *      Author: fabio
 */

#include <math.h>
#include <sys/types.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <asm/types.h>
#include "../lib/sckt_descr.h"
#include "../lib/raw_socket_op.h"
#include "../lib/raw_socket.h"
#include "../lib/conversion_helper.h"


void clear_pkt_buffer(pkt_buffer *buff);

//sends data to the specified hex MAC address. length must include the string termination character '\0'
//prot_type included for future expansion
int send_data(char* dest, struct sckt_descr *sd , char* data, int length, int prot_type)
{
	//printf("raw_socket_op.c::send_raw_data\n");
	//include '\0'
	++length;
	unsigned int prog_counter = 0;
	unsigned char *hex_addr = cstrtohex(dest);
	unsigned char *buffer = malloc(FBRDCST_FRAME_LEN);
	memset(buffer, 0, FBRDCST_FRAME_LEN);
	fst_brd_hdr *head = (fst_brd_hdr*) buffer;
	unsigned char *payload = buffer+FBRDCST_H_LEN;
	int pkt_nbr = ceil(((double)length)/((double)FBRDCST_DATA_LEN));
	int i=0;
	int res=0;
	unsigned char *data_it = data;
	//printf("length: %i, FastBroadcast length: %i, pkt_nbr: %i\n", length, FBRDCST_DATA_LEN, pkt_nbr);
	for (i=0; i<pkt_nbr; i++) {
		if(prog_counter >= FBRDCST_MAX_PROG)
			prog_counter = 0;
		//initialization
		head->has_more = 0;
		//set progressive packet number
		head->prog = prog_counter;
		//set payload length
		//if only one frame is needed
		if(pkt_nbr==1)
		{
			head->length = length;
		}
		//if it's not the last packet, use full FBRDCST_DATA_LEN. Moreover, has_more is set to 1
		else if(i!=(pkt_nbr-1))
		{
			head->length = FBRDCST_DATA_LEN;
			head->has_more = pkt_nbr-(i+1);
		}
		//if it's the last packet, the size is length - the total size of already sent frame's data
		else if(i==(pkt_nbr-1))
		{
			head->length = length-(i*FBRDCST_DATA_LEN);
		}
		//total frame length
		int frm_len = head->length+FBRDCST_H_LEN;
		memcpy(payload, data, head->length);
		res = send_raw_eth_packet(hex_addr, sd, (unsigned char*)buffer, frm_len, ETHERTYPE_FBRDCST, 1);
		if(res<0)
			return res;
		data_it += head->length;
		prog_counter++;
	}
	free(buffer);
	free(hex_addr);
	return res;
}

//returns received packets with prot_type protocol. src_add points to a newly allocated memory
//block, it should be freed once finished
//prot_type not yet used, included for future expansion
//TODO: packet ordering
char* read_data(struct sckt_descr *sd, int prot_type, char **src_add, unsigned int *read)
{
	char read_more = 0;
	char flag = 1;
	unsigned char *pkt;
	char* result;
	pkt_buffer *pkt_buff = malloc(sizeof(pkt_buffer));
	memset(pkt_buff, 0, sizeof(pkt_buffer));
	pkt_buffer *iterator = pkt_buff;
	*read=0;
	unsigned char *raw_add;
	while(flag)
	{
		pkt = read_eth_pkt(sd, &raw_add, prot_type);
		if(!pkt)
		{
			free(pkt_buff);
			return SOCKET_OP_ERROR;
		}
		fst_brd_hdr *hdr = (fst_brd_hdr *)pkt;
		iterator->buffer = pkt+FBRDCST_H_LEN;
		iterator->length = hdr->length;
		*read += hdr->length;
		//printf("Fast Broadcast Packet: Progressive: %i; Has More: %i; Length: %i; TotalLen: %i; Data: %s\n", hdr->prog, hdr->has_more, hdr->length, *read, (char*)iterator->buffer);
		if(!hdr->has_more)
			flag=0;
		else
		{
			read_more = 1;
			iterator = iterator->next;
			iterator = malloc(sizeof(pkt_buffer));
			memset(iterator, 0, sizeof(pkt_buffer));
		}
	}
	*src_add = hextocstr(raw_add, ETH_ALEN);
	free(raw_add);
	if(!read_more)
	{
		unsigned char *result = malloc(pkt_buff->length+1);
		memset(result, 0, pkt_buff->length+1);
		memcpy((void*) result, pkt_buff->buffer, pkt_buff->length);
		*(result+*read) = '\0';
		free(pkt_buff);
		free(pkt);
		return result;
	}
	result = malloc(*read);
	iterator = pkt_buff;
	unsigned char *res_it = result;
	while(iterator)
	{
		memcpy(res_it, iterator->buffer, iterator->length);
		if(iterator->next)
			res_it += iterator->length;
		iterator = iterator->next;
	}
	clear_pkt_buffer(pkt_buff);
	return result;
}

void clear_pkt_buffer(pkt_buffer *buff) {
	pkt_buffer *n;
	while(buff)
	{
		n = buff->next;
		free(buff);
		buff = n;
	}
}
