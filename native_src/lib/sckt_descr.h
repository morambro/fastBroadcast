/*
 * socket_descriptor.h
 *
 *  Created on: Dec 17, 2012
 *      Author: fabio
 */

#ifndef SCKT_DESCR_H_
#define SCKT_DESCR_H_

#include <linux/types.h>
#include "../lib/wifi_nic.h"

#define ACTION_BIND_SOCKET			20
#define NIC_TYPE_WIFI				50


struct sckt_descr
{
	int fd;
	__u16 orig_flags;
	__u32 orig_mode;
	__u8 rfmon_mode;
	wifi_nic *nic;
	int (*set_iwr_mode)(struct sckt_descr* sd, int mode);
};

//creates a socket for protocol. nic_type is only needed if action == ACTION_BIND_SOCKET
struct sckt_descr* open_socket(int protocol, int action, int nic_type);
void clear_socket(struct sckt_descr *sd);

#endif /* SOCKET_DESCRIPTOR_H_ */
