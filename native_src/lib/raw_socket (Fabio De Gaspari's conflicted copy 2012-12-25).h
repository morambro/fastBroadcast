/*
 * raw_socket.h
 *
 *  Created on: Nov 24, 2012
 *      Author: fabio
 */

#ifndef RAW_SOCKET_H_
#define RAW_SOCKET_H_

#define RAW_ETH_TRANSMISSION_ERROR 		0
#define RAW_ETH_TRANSMISSION_SUCCESS 	1
#define RAW_AD_HOC_MODE 				2
#define RAW_PROMISC_MODE 				3

#define CONNLESS_HDR_LEN				sizeof(struct ieee80211_radiotap_header)+sizeof(struct ieee80211_radiotap_data)+sizeof(struct ieee80211_header_3addr)+sizeof(struct ieeeLLC_header_8B)
#define BIG_ENOUGH_DATA_BUFFER			5000

//wrapper for data+length of an ethernet packet
typedef struct eth_pkt
{
	unsigned char *pkt;
	unsigned int length;
}eth_pkt;


//returns a pointer to a buffered packet stripped of link level headers. -1 on error and errno is set accordingly
unsigned char* read_eth_pkt(struct sckt_descr *sd, unsigned char **source, int protocol);
//returns RAW_ETH_TRANSMISSION_SUCCESS or RAW_ETH_TRANSMISSION_ERROR
int send_raw_eth_packet(unsigned char* dest, struct sckt_descr *sd , unsigned char* data, int length, int prot_type, int ad_hoc);

#endif /* RAW_SOCKET_H_ */
