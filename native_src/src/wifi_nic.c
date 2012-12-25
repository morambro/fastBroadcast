/*
 * wifi_nic.c
 *
 *  Created on: Dec 17, 2012
 *      Author: fabio
 */

#include "sys/ioctl.h"
#include <ifaddrs.h>
#include <string.h>
#include <stdio.h>
#include <malloc.h>
#include <errno.h>
#include <net/if.h>
#include "../lib/wifi_nic.h"

wifi_nic* get_wifi_nic(int socket_descriptor)
{
	const int common_names_nbr = 3;
	char *common_names[] = {"wlan0\0", "eth1\0", "eth0\0"};
	struct ifaddrs *ifaddr, *it;
	if(getifaddrs(&ifaddr)==-1)
	{
		fprintf(stderr, "Error while getting available network interfaces: %s", strerror(errno));
		return 0;
	}
	int j=0;
	for(; j<common_names_nbr; ++j)
	{
		it=ifaddr;
		while(it)
		{
			if(!strcmp(common_names[j], it->ifa_name))
			{
				struct ifreq ifr;
				strcpy(ifr.ifr_name, it->ifa_name);
				if(ioctl(socket_descriptor, SIOCGIFINDEX, &ifr)<0)
				{
					fprintf(stderr, "Error while getting %s network interface index: %s\n", ifr.ifr_name, strerror(errno));
					return 0;
				}
				wifi_nic *nic = malloc(sizeof(wifi_nic));
				memset((void*)nic, 0, sizeof(wifi_nic));
				memcpy((void*)nic, &ifr, sizeof(nic->if_index)+sizeof(nic->if_name));
				if(ioctl(socket_descriptor, SIOCGIFHWADDR, &ifr)<0)
				{
					fprintf(stderr, "Error while getting %s network interface MAC address: %s\n", ifr.ifr_name, strerror(errno));
					return 0;
				}
				memcpy((void*)nic->mac, &ifr.ifr_hwaddr.sa_data, 6);
				printf("nic name :%s; nic index: %i; nic MAC: %02X:%02X:%02X:%02X:%02X:%02X\n", nic->if_name, nic->if_index, nic->mac[0], nic->mac[1], nic->mac[2], nic->mac[3], nic->mac[4], nic->mac[5]);
				freeifaddrs(ifaddr);
				return nic;
			}
			it = it->ifa_next;
		}
	}
	freeifaddrs(ifaddr);
	fprintf(stderr, "Can not find a suitable Wi-Fi interface\n");
	return 0;
}

wifi_nic* get_wifi_nic_with_ip(int socket_descriptor)
{
	const int common_names_nbr = 3;
	char *common_names[] = {"wlan0\0", "eth1\0", "eth0\0"};
	const int base_buf_size = 1024;
	struct ifconf conf;
	int flag = 1;
	int i=1;
	while(flag)
	{
		const int cur_buf_size = base_buf_size * i;
		char buffer[cur_buf_size];
		conf.ifc_len = cur_buf_size;
		conf.ifc_buf = buffer;
		if(ioctl(socket_descriptor, SIOCGIFCONF, &conf)<0)
		{
			fprintf(stderr, "Error while getting available network interfaces: %s", strerror(errno));
			return 0;
		}
		if(cur_buf_size == conf.ifc_len)
		{
			//chances are cur_buf_size was too small
			++i;
			continue;
		}
		//we have our nic list, iterate through
		flag = 0;
		struct ifreq *ifr = conf.ifc_req;
		int nic_number = conf.ifc_len /sizeof(struct ifreq);
		int j=0;
		for(; j<common_names_nbr; ++j)
			for(i=0; i<nic_number; ++i)
			{
				struct ifreq *curr_ifr = (ifr+i);
				fflush(stdout);
				printf("comparing %s and %s\n", common_names[j], curr_ifr->ifr_name);
				if(!strcmp(common_names[j], curr_ifr->ifr_name))
				{
					if(ioctl(socket_descriptor, SIOCGIFINDEX, curr_ifr)<0)
					{
						fprintf(stderr, "Error while getting %s network interface index: %s\n", curr_ifr->ifr_name, strerror(errno));
						return 0;
					}
					//wifi_nic nic = { .if_name = curr_ifr->ifr_name, .if_index = curr_ifr->ifr_ifindex};
					wifi_nic *nic = (wifi_nic*) curr_ifr;
					//printf("nic name :%s; nic index: %i\n", nic->if_name, nic->if_index);
					return nic;
				}
			}
	}
	fprintf(stderr, "Can not find a suitable Wi-Fi interface\n");
	return 0;
}

void clear_nic(wifi_nic *nic)
{
	free(nic);
}
