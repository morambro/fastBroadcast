/*
 * ieee80211.h
 *
 *  Created on: Dec 16, 2012
 *      Author: fabio
 */

#ifndef IEEE80211_H_
#define IEEE80211_H_

//802.11 constants
#define IEEE80211_MAX_FRAG_THRESHOLD    		2352
#define IEEE80211_MAX_RTS_THRESHOLD     		2353
#define IEEE80211_MAX_AID               		2007
#define IEEE80211_MAX_TIM_LEN           		251
#define IEEE80211_MAX_FRAME_LEN					2352
#define IEEE80211_MAX_DATA_LEN					2304
#define IEEE80211_MAX_SSID_LEN          		32

#define IEEE80211_FC_VER						0x0000
#define IEEE80211_FC_FTYPE						0x000C
#define IEEE80211_FC_STYPE						0x00F0
#define IEEE80211_FC_TODS						0x0100
#define IEEE80211_FC_FROMDS						0x0200
#define IEEE80211_FC_MOREFRAGS					0x0400
#define IEEE80211_FC_RETRY						0x0800
#define IEEE80211_FC_PWRMG						0x1000
#define IEEE80211_FC_MOREDATA					0x2000
#define IEEE80211_FC_WEP						0x4000
#define IEEE80211_FC_STRICT						0x8000

#define IEEE80211_FTYPE_MGMT					0x0000
#define IEEE80211_FTYPE_CTRL					0x0004
#define IEEE80211_FTYPE_DATA					0x0008

//only data subtypes defined here
#define IEEE80211_STYPE_DATA                    0x0000
#define IEEE80211_STYPE_DATA_CFACK              0x0010
#define IEEE80211_STYPE_DATA_CFPOLL             0x0020
#define IEEE80211_STYPE_DATA_CFACKPOLL          0x0030
#define IEEE80211_STYPE_NULLFUNC                0x0040
#define IEEE80211_STYPE_CFACK                   0x0050
#define IEEE80211_STYPE_CFPOLL                  0x0060
#define IEEE80211_STYPE_CFACKPOLL               0x0070
#define IEEE80211_STYPE_QOS_DATA                0x0080
#define IEEE80211_STYPE_QOS_DATA_CFACK          0x0090
#define IEEE80211_STYPE_QOS_DATA_CFPOLL         0x00A0
#define IEEE80211_STYPE_QOS_DATA_CFACKPOLL      0x00B0
#define IEEE80211_STYPE_QOS_NULLFUNC            0x00C0
#define IEEE80211_STYPE_QOS_CFACK               0x00D0
#define IEEE80211_STYPE_QOS_CFPOLL              0x00E0
#define IEEE80211_STYPE_QOS_CFACKPOLL           0x00F0

#define LLC_PROT_SNAP							0xAA
#define LLC_PROT_SNAP_SIZE						5
#define LLC_UNNUMBERED_INFO						3

struct ieee80211_header {
	__le16 frame_control;
	__le16 duration;
	__u8 addr1[6];
	__u8 addr2[6];
	__u8 addr3[6];
	__le16 seq_control;
	__u8 addr4[6];
}__attribute__((__packed__));

struct ieee80211_header_3addr {
	__le16 frame_control;
	__le16 duration;
	__u8 addr1[6];
	__u8 addr2[6];
	__u8 addr3[6];
	__le16 seq_control;
	__le16 qos;
}__attribute__((__packed__));

struct ieeeLLC_header_3B {
	__u8 dsap;
	__u8 ssap;
	__u8 ctrl;
};

struct ieeeLLC_header_8B {
	__u8 dsap;
	__u8 ssap;
	__u8 ctrl;
	__u8 snap[5];
};

#endif /* IEEE80211_H_ */
