/*
 * wifi_nic.h
 *
 *  Created on: Dec 17, 2012
 *      Author: fabio
 */

#ifndef WIFI_NIC_H_
#define WIFI_NIC_H_

#ifndef IF_NAMESIZE
#define IF_NAMESIZE 16
#endif

typedef struct wifi_nic {
	char if_name[IF_NAMESIZE];
	int if_index;
}wifi_nic;

wifi_nic* get_wifi_nic(int socket_descriptor);
wifi_nic* get_wifi_nic_with_ip(int socket_descriptor);
void clear_nic(wifi_nic *nic);

#endif /* WIFI_NIC_H_ */
