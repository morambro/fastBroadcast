/*
 * main.c
 *
 *  Created on: Nov 23, 2012
 *      Author: fabio
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <linux/wireless.h>
#include "../lib/sckt_descr.h"
#include "../lib/raw_socket_op.h"
#include "../lib/raw_socket.h"
#include "../lib/conversion_helper.h"
#include "../lib/fast_broadcast.h"

#define MAX_DATA_SIZE 1500

void raw_sock_read()
{
	short int flag = 1;
	struct sckt_descr *sd = open_socket(ETH_P_802_2, ACTION_BIND_SOCKET, NIC_TYPE_WIFI);
	if(!sd)
		flag = 0;
	sd->set_iwr_mode(sd, IW_MODE_MONITOR);
	char *sender;
	unsigned int read=0;
	while(flag)
	{
		read=0;
		char *data = read_data(sd, ETHERTYPE_FBRDCST, &sender, &read);
		free(sender);
		if(!data)
		{
			fprintf(stderr, "Error occured, aborting\n");
			flag = 0;
		}
		else
		{
			printf("Fast Broadcast Data: %s\n",data);
			if(!strcmp(data, "bye"))
				flag = 0;
			free(data);
		}
	}
	clear_socket(sd);
}

void raw_socket_send(char *dest_address)
{
	int flag = 1;
	struct sckt_descr *sd = open_socket(ETHERTYPE_FBRDCST, ACTION_BIND_SOCKET, NIC_TYPE_WIFI);
	sd->set_iwr_mode(sd, IW_MODE_MONITOR);
	if(!sd)
		flag=0;
	while(flag)
	{
		printf("Enter the data you wish to send, exit to terminate.\n");
		char input[MAX_DATA_SIZE];
		fflush(stdout);
		fflush(stdin);
		if(fgets(input, MAX_DATA_SIZE, stdin))
		{
			*(strchr(input, '\n')) = '\0';
			if(!strcmp("exit", input))
				flag = 0;
			else
			{
				int length = strlen(input);
				printf("input length: %i\n",length);
				send_data(dest_address, sd, input, length, 0);
			}
		}
	}
	clear_socket(sd);
}

char* get_arg(char *arg, char** argv, int argc)
{
	int i=1;
	for(; i<argc; i++)
	{
		if(!strcmp(argv[i],arg))
			return argv[i+1];
	}
	return 0;
}

void print_usage(FILE* stream)
{
	fprintf(stream, "ERROR!\n"
			"usage: raw_socket\n"
			"use -r <recipient MAC address> if you wish to send to a specific machine\n"
			"use -s <data> if you wish to send data\n"
			"use -l if you wish to listen for incoming packets\n");
}


int main(int argc, char **argv)
{
	if(argc<1)
	{

		return -1;
	}
	short int flag = -1;
	char* recipient = 0;
	int i=1;
	for(; i<argc; ++i) {
		if(!strcmp(argv[i], "-s"))
		{
			flag = 0;
		}
		if(!strcmp(argv[i], "-l"))
		{
			flag = 1;
		}
		if(!strcmp(argv[i], "-r"))
		{
			++i;
			recipient = argv[i];
		}
	}
	switch(flag)
	{
	case 0:
		printf("%s\n", *(argv+1));
		if(!recipient)
			recipient="FFFFFFFFFFFF";
		raw_socket_send(recipient);
		break;
	case 1:
		raw_sock_read();
		break;
	default:
		print_usage(stderr);
		break;
	}
	return 1;
}
