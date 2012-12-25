/*
 * ieee_80211_radiotap.h
 *
 *  Created on: Dec 16, 2012
 *      Author: fabio
 */

#ifndef IEEE_80211_RADIOTAP_H_
#define IEEE_80211_RADIOTAP_H_

#include <linux/types.h>

#define RADIOTAP_F_RATE			1<<2

struct ieee80211_radiotap_header
{
	__u8	it_version;
	__u8	pad;
	__le16	it_len;
	__le32	it_present;
}__attribute__((__packed__));

struct ieee80211_radiotap_data {
    __u8 rate;
    __u8 pad[3];
} __attribute__ ((packed));


#endif /* IEEE_80211_RADIOTAP_H_ */
