/*
 * fast_broadcast.h
 *
 *  Created on: Nov 25, 2012
 *      Author: fabio
 */

#ifndef FAST_BROADCAST_H_
#define FAST_BROADCAST_H_

#include <linux/if_ether.h>
#include <asm/types.h>

#define ETHERTYPE_FBRDCST 0x0700
//FastBroadcast frame size
#define FBRDCST_FRAME_LEN ETH_DATA_LEN
//FastBroadcast header size
#define FBRDCST_H_LEN 8
//FastBroadcast data size
#define FBRDCST_DATA_LEN FBRDCST_FRAME_LEN-FBRDCST_H_LEN
//max progressive number, using unsigned 32-bit integer
#define FBRDCST_MAX_PROG 4294967295

//FastBroadcast header
typedef struct fst_brd_hdr
{
	__u32 prog;		//progressive packet number
	__u8 has_more;	//true if other packets follows
	__u8 do_not_use;//padding to 4B + 4B
	__u16 length;	//payload length
}fst_brd_hdr;

//FastBroadcast data packet
typedef struct fst_brd_pkt
{
	char *source_address;
	unsigned char *payload;
	int payload_size;
}fst_brd_pkt;

#endif /* FAST_BROADCAST_H_ */
