/*
 * raw_socket.c
 *
 *  Created on: Nov 23, 2012
 *      Author: fabio
 */

#include <sys/socket.h>
#include <linux/if.h>
#include <linux/if_packet.h>
#include <linux/if_ether.h>
#include <linux/if_arp.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <errno.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <linux/wireless.h>
#include "../lib/sckt_descr.h"
#include "../lib/raw_socket.h"
#include "../lib/conversion_helper.h"
#include "../lib/fast_broadcast.h"
#include "../lib/ieee_80211_radiotap.h"
#include "../lib/ieee_80211.h"
#include "../lib/wifi_nic.h"



int filter_packet(unsigned char *buffer, unsigned char* src_addr);
unsigned char* set_radiotap_hdr(unsigned char *src_mac, unsigned char *dest_mac, unsigned char *data, int *length, int prot_type);
unsigned char* set_ethernet_hdr(unsigned char *src_mac, unsigned char *dest_mac, struct sckt_descr * sd, unsigned char *data, int *length, int prot_type, struct sockaddr_ll *socket_address);



//length must include \0
int send_raw_eth_packet(unsigned char *dest_mac, struct sckt_descr *sd, unsigned char *data, int length, int prot_type, int ad_hoc)
{
	int send_result = 0;
	unsigned char src_mac[6] = {0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF};
	if(sd->nic)
		memcpy(src_mac, sd->nic->mac, ETH_ALEN);
	//if we are connected to an AP we can simply send ethernet frames
	if(!ad_hoc)
	{
		struct sockaddr_ll socket_address;
		unsigned char* pkt = set_ethernet_hdr(sd->nic->mac, dest_mac, sd, data, &length, prot_type, &socket_address);
		//send the packet
		send_result = sendto(sd->fd, pkt, ETH_FRAME_LEN, 0, (struct sockaddr*)&socket_address, sizeof(socket_address));
	}
	//no AP, we need to use radiotap + ieee 802.11 header
	else
	{
		unsigned char *pkt = set_radiotap_hdr(sd->nic->mac, dest_mac, data, &length, ETH_P_ALL);
		send_result = write(sd->fd, (void*) pkt, length);
		free(pkt);
	}
	//printf("sent %i bytes\n", send_result);
	//error handling
	if (send_result == -1)
	{
		//fprintf(stderr, "Transmission error: %s\n", strerror(errno));
		return RAW_ETH_TRANSMISSION_ERROR;
	}
	return RAW_ETH_TRANSMISSION_SUCCESS;
}

//returns a pointer to a buffered packet stripped of link level headers and
//sets source as the sender address in an allocated memory block (needs free)
//-1 on error and errno is set accordingly
unsigned char* read_eth_pkt(struct sckt_descr *sd, unsigned char **source, int protocol)
{
	//printf("raw_socket.c::read_eth_pkt\n");
	unsigned char *buffer = malloc(BIG_ENOUGH_DATA_BUFFER);
	*source = malloc(ETH_ALEN);
	memset(buffer, 0, ETH_FRAME_LEN);
	int while_f = 0;
	int read_B=0;
	while(!while_f)
	{
		read_B = read(sd->fd, (void *)buffer, BIG_ENOUGH_DATA_BUFFER);
		if(read_B ==-1) {
			//printf("socket read error: %i \n", errno);
			free(buffer);
			return 0;
		}
		while_f = filter_packet(buffer, *source);
	}
	unsigned char *pkt = malloc(read_B-while_f);
	memcpy(pkt, buffer+while_f, read_B-while_f);
	free(buffer);
	return pkt;
}

unsigned char* set_radiotap_hdr(unsigned char *src_mac, unsigned char *dest_mac, unsigned char *data, int *length, int prot_type)
{
	unsigned int frame_len = CONNLESS_HDR_LEN+(*length);
	unsigned char *buffer = malloc(frame_len);
	memset((void*)buffer, 0, frame_len);
	unsigned char *buffer_iterator = buffer;
	//setting radiotap header
	struct ieee80211_radiotap_header *radiotap_hdr = (struct ieee80211_radiotap_header*) buffer;
	radiotap_hdr->it_version = 0;
	radiotap_hdr->it_len = sizeof(struct ieee80211_radiotap_header)+sizeof(struct ieee80211_radiotap_data);

	radiotap_hdr->it_present = RADIOTAP_F_RATE;
	buffer_iterator += sizeof(radiotap_hdr);
	//setting radiotap data
	struct ieee80211_radiotap_data *rtap_data = (struct ieee80211_radiotap_data*) buffer_iterator;
	rtap_data->rate = 2;//2*500KB
	//setting ieee 802.11 header
	buffer_iterator += sizeof(struct ieee80211_radiotap_data);
	struct ieee80211_header_3addr *dot80211_hdr = (struct ieee80211_header_3addr*) buffer_iterator;
	dot80211_hdr->frame_control = (IEEE80211_FC_VER | (IEEE80211_FC_FTYPE & IEEE80211_FTYPE_DATA));
	dot80211_hdr->duration = 0x0000;
	//toDS && fromDS == 0 -> AdHoc mode. address1 is destination, address2 is source and address3 is BSSID
	memcpy((void*)dot80211_hdr->addr1, (void*)dest_mac, ETH_ALEN);
	memcpy((void*)dot80211_hdr->addr2, (void*)src_mac, ETH_ALEN);
	memcpy((void*)dot80211_hdr->addr3, (void*)src_mac, ETH_ALEN);
	//sequence control is automatically set by the driver

	buffer_iterator += sizeof(struct ieee80211_header_3addr);
	struct ieeeLLC_header_8B *llc = (struct ieeeLLC_header_8B*)buffer_iterator;
	llc->dsap = LLC_PROT_SNAP;
	llc->ssap = LLC_PROT_SNAP;
	llc->ctrl = LLC_UNNUMBERED_INFO;
	//organization id
	llc->snap[0] = 0x00;
	llc->snap[1] = 0x00;
	llc->snap[2] = 0x00;
	//protocol type
	__u16 prot = ETHERTYPE_FBRDCST;
	__u8 *temp = &prot;
	memcpy(&llc->snap[3], temp+1, 1);
	memcpy(&llc->snap[4], temp, 1);
	//printf("debug: %02X%02X \t", llc->snap[3], llc->snap[4]);
	buffer_iterator += sizeof(struct ieeeLLC_header_8B);
	memcpy((void*)buffer_iterator, (void*)data, *length);
	*length = frame_len;
	return buffer;
}

unsigned char* set_ethernet_hdr(unsigned char *src_mac, unsigned char *dest_mac, struct sckt_descr * socket_descriptior, unsigned char *data, int *length, int prot_type, struct sockaddr_ll *socket_address)
{
	unsigned char *padded_data = malloc(ETH_DATA_LEN);
	memset(padded_data, 0, ETH_DATA_LEN);
	//always use full ethernet payload, add padding if necessary
	if(*length<ETH_DATA_LEN) {
		memcpy((void*)padded_data, (void*)data, *length);
		*length = ETH_DATA_LEN;
	}
	int eth_frame_length = ETH_HLEN+*length;
	void* buffer = malloc(eth_frame_length);
	memset(buffer, 0, eth_frame_length);

	//skip the first 14 bytes of the frame (ethernet header)
	unsigned char* eth_pkt_data = buffer + ETH_HLEN;

	//ethhdr is a struct defining an ethernet header, so after the cast eh points to the header of our packet
	//(contained in buffer). We can now access our packet header through this struct
	struct ethhdr *eh = (struct ethhdr *)buffer;

	//our MAC address c4:85:08:3b:f3:76

	//must always be set to AF_PACKET (low level address interface => low level packet family)
	socket_address->sll_family = AF_PACKET;
	socket_address->sll_protocol = htons(prot_type);

	//index of the network device
	socket_address->sll_ifindex = socket_descriptior->nic->if_index;;

	//ARP hardware 802.11 identifier
	socket_address->sll_hatype = ARPHRD_IEEE80211;

	//target is another host, use PACKET_BROADCAST for physical layer broadcast
	socket_address->sll_pkttype = PACKET_OTHERHOST;

	//address length
	socket_address->sll_halen = ETH_ALEN;

	int i;
	//printf("building destination address\n");
	for (i = 0; i < ETH_ALEN; i++)
		socket_address->sll_addr[i] = *(dest_mac+i);

	socket_address->sll_addr[6]  = 0x00;//not used for MAC address
	socket_address->sll_addr[7]  = 0x00;//not used for MAC address

	//start of header frame setting
	//first octet: destination
	memcpy((void*)buffer, (void*)dest_mac, ETH_ALEN);
	//second octet: source mac
	memcpy((void*)(buffer+ETH_ALEN), (void*)src_mac, ETH_ALEN);
	//Packet type ID
	eh->h_proto = htons(prot_type);
	memcpy((void*)eth_pkt_data, (void*)padded_data, ETH_DATA_LEN);
	fst_brd_hdr *head = (fst_brd_hdr*)padded_data;
	//printf("Fast Broadcast Packet: Progressive: %i; Has More: %i; Length: %i; Data: %s\n", head->prog, head->has_more, head->length, padded_data+FBRDCST_H_LEN);
	//free allocated memory
	free(padded_data);
	return buffer;
}



//used for debug
#define BYTETOBINARYPATTERN "%d%d%d%d%d%d%d%d"
#define BYTETOBINARY(byte)  \
		(byte & 0x80 ? 1 : 0), \
		(byte & 0x40 ? 1 : 0), \
		(byte & 0x20 ? 1 : 0), \
		(byte & 0x10 ? 1 : 0), \
		(byte & 0x08 ? 1 : 0), \
		(byte & 0x04 ? 1 : 0), \
		(byte & 0x02 ? 1 : 0), \
		(byte & 0x01 ? 1 : 0)



int filter_packet(unsigned char *buffer, unsigned char* src_addr)
{
	int offset=0;
	struct ieee80211_radiotap_header *radiotap_hdr = (struct ieee80211_radiotap_header*) buffer;
	unsigned char *buffer_iterator = buffer;
	offset += radiotap_hdr->it_len;
	buffer_iterator += offset;
	struct ieee80211_header_3addr *dot80211_hdr = (struct ieee80211_header_3addr*) buffer_iterator;
	if((dot80211_hdr->frame_control & (IEEE80211_FTYPE_DATA)) && !(dot80211_hdr->frame_control & (IEEE80211_FC_TODS | IEEE80211_FC_FROMDS)))
	{
		offset += sizeof(struct ieee80211_header_3addr);
		buffer_iterator += sizeof(struct ieee80211_header_3addr);
		struct ieeeLLC_header_8B *llc = (struct ieeeLLC_header_8B*) buffer_iterator;
		//printf("debug: %02X %02X\t", llc->dsap, llc->ssap);
		//printf("debug2: %02X %02X %02X %02X %02X\t", llc->snap[0], llc->snap[1],llc->snap[2],llc->snap[3],llc->snap[4]);
		if(llc->dsap == 0xAA && llc->ssap == 0xAA)
		{
			__u16 snap2;
			__u8 *temp = &snap2;
			memcpy(temp+1, &llc->snap[3], 1);
			memcpy(temp, &llc->snap[4], 1);
			//printf("debug2: %04X\n", snap2);
			if(snap2 & ETHERTYPE_FBRDCST)
			{
				memcpy(src_addr, dot80211_hdr->addr2, ETH_ALEN);
				offset += sizeof(struct ieeeLLC_header_8B);
				return offset;
			}
		}
		else
			return 0;
	}
	else
	{
		return 0;
	}
	return 0;
}
