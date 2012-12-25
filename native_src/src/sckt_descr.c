/*
 * socket_descriptor.c
 *
 *  Created on: Dec 17, 2012
 *      Author: fabio
 */

#include <sys/socket.h>
#include <linux/if_packet.h>
#include <linux/if_ether.h>
#include <linux/wireless.h>
#include <arpa/inet.h>
#include <sys/ioctl.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <malloc.h>
#include <errno.h>
#include "../lib/sckt_descr.h"

int set_wext_mode(struct sckt_descr *sd, int mode);

struct sckt_descr* open_socket(int protocol, int action, int nic_type)
{
	//get raw socket descriptor
	struct sckt_descr *sd = malloc(sizeof(struct sckt_descr));
	memset(sd, 0, sizeof(struct sckt_descr));
	sd->fd = socket(AF_PACKET, SOCK_RAW, htons(protocol));
	printf("sd: %i\n", sd->fd);
	if(sd->fd==-1) {
		fprintf(stderr, "Socket creation error: %s\n",strerror(errno));
		return 0;
	}
	if(action == ACTION_BIND_SOCKET)
	{
		wifi_nic *nic=0;
		if(nic_type == NIC_TYPE_WIFI)
		{
			//get wifi interface index and name
			nic = get_wifi_nic(sd->fd);
			if(!nic)
			{
				fprintf(stderr, "Error occurred while getting wifi nic, aborting\n");
				close(sd->fd);
				exit(1);
			}
		}
		sd->nic = nic;

		//get wifi interface original flags
		struct ifreq ifr;
		strcpy(ifr.ifr_name, sd->nic->if_name);
		if(ioctl(sd->fd, SIOCGIFFLAGS, &ifr)<0)
		{
			fprintf(stderr, "Error creating socket descriptor. Can not get interface flags. %s\n", strerror(errno));
			close(sd->fd);
			clear_nic(nic);
			return 0;
		}
		struct iwreq iwr;
		strcpy(iwr.ifr_name, sd->nic->if_name);
		if(ioctl(sd->fd, SIOCGIWMODE, &iwr)<0)
		{
			fprintf(stderr, "Error creating socket descriptor. Can not get interface mode flags. %s\n", strerror(errno));
			close(sd->fd);
			clear_nic(nic);
			return 0;
		}
		sd->orig_mode = iwr.u.mode;
		printf("Interface initial mode is %i\n", sd->orig_mode);
		//bind socket to wifi interface
		struct sockaddr_ll ll;
		memset(&ll, 0, sizeof(ll));
		ll.sll_family = AF_PACKET;
		ll.sll_ifindex = nic->if_index;
		ll.sll_protocol = htons(protocol);
		if ( bind( sd->fd, (struct sockaddr *) &ll, sizeof(ll) ) < 0 ) {
			fprintf(stderr, "Error binding socket descriptor to interface %s: %s", nic->if_name, strerror(errno));
			clear_socket(sd);
			return 0;
		}
	}
	sd->set_iwr_mode = set_wext_mode;
	return sd;
}

void clear_socket(struct sckt_descr *sd)
{
	struct ifreq ifr;
	memset((void*)&ifr, 0, sizeof(ifr));
	strcpy(ifr.ifr_name, sd->nic->if_name);
	if(ioctl(sd->fd, SIOCSIFFLAGS, &ifr)!=-1)
		printf("Interface flags successfully cleared.\n");
	else
		fprintf(stderr, "Error while clearing interface flags. %s\n", strerror(errno));
	sd->set_iwr_mode(sd, sd->orig_mode);
	ifr.ifr_flags = sd->orig_flags;
	if(ioctl(sd->fd, SIOCSIFFLAGS, &ifr)!=-1)
		printf("Interface flags successfully reset.\n");
	else
		fprintf(stderr, "Error while resetting interface flags. %s\n", strerror(errno));
	clear_nic(sd->nic);
	close(sd->fd);
	printf("Socket closed.\n");
	free(sd);
}

void init_network(struct sckt_descr *sd)
{
	write(sd->fd, (void*)"init", sizeof("init"));
}

int set_wext_mode(struct sckt_descr *sd, int mode)
{
	struct iwreq iwr;
	struct ifreq ifr;
	strcpy(iwr.ifr_name, sd->nic->if_name);
	strcpy(ifr.ifr_name, sd->nic->if_name);
	if(ioctl(sd->fd, SIOCGIWMODE, &iwr)<0)
	{
		fprintf(stderr, "Error getting interface mode. %s\n", strerror(errno));
		return -1;
	}
	if(iwr.u.mode == mode)
		return 0;
	if(ioctl(sd->fd, SIOCGIFFLAGS, &ifr)<0)
	{
		fprintf(stderr, "Error getting interface flags. %s\n", strerror(errno));
		return -1;
	}
	ifr.ifr_flags &= ~IFF_UP;
	if(ioctl(sd->fd, SIOCSIFFLAGS, &ifr)<0)
	{
		fprintf(stderr, "Error shutting down interface. %s\n", strerror(errno));
		return -1;
	}
	iwr.u.mode = mode;
	if(ioctl(sd->fd, SIOCSIWMODE, &iwr)<0)
	{
		fprintf(stderr, "Error setting interface mode %i. %s\n", mode, strerror(errno));
		return -1;
	}
	ifr.ifr_flags |= IFF_UP;
	if(ioctl(sd->fd, SIOCSIFFLAGS, &ifr)<0)
	{
		fprintf(stderr, "Error bringing up interface. %s\n", strerror(errno));
		return -1;
	}
	init_network(sd);
	return 0;
}
