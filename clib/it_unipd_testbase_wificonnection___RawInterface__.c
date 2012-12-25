#include <sys/socket.h>
#include <linux/if_packet.h>
#include <linux/if_ether.h>
#include <linux/wireless.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <malloc.h>
#include "it_unipd_testbase_wificonnection___RawInterface__.h"
#include "raw_socket_op.h"
#include "../lib/conversion_helper.h"
#include "../lib/sckt_descr.h"


JNIEXPORT jlong JNICALL Java_it_unipd_testbase_wificonnection__1_1RawInterface_1_1_m_1get_1raw_1socket(JNIEnv *env, jclass caller)
{
	struct sckt_descr *sd = open_socket(ETH_P_802_2, ACTION_BIND_SOCKET, NIC_TYPE_WIFI);
	sd->set_iwr_mode(sd, IW_MODE_MONITOR);
	return (long)sd;
}

JNIEXPORT jint JNICALL Java_it_unipd_testbase_wificonnection__1_1RawInterface_1_1_native_1send(JNIEnv *env, jclass caller, jlong sd_ptr, jstring str, jobjectArray error)
{
	struct sckt_descr *sd = (struct sckt_descr*) sd_ptr;
	const char *nat_str = (*env)->GetStringUTFChars(env, str, 0);
	char *s = nat_str;
	char dest_address[] = "FFFFFFFFFFFF";
	int flag = 1;
	if(!sd)
		return;
	int length = strlen(nat_str);
	flag = send_data(dest_address, sd, s, length, 0);
	if(flag<0)
		(*env)->SetObjectArrayElement(env, error, 0, (*env)->NewStringUTF(env, strerror(errno)));
	(*env)->ReleaseStringUTFChars(env, str, nat_str);
	return flag;
}


JNIEXPORT jstring JNICALL Java_it_unipd_testbase_wificonnection__1_1RawInterface_1_1_native_1read(JNIEnv*env, jclass caller, jlong sd_ptr, jintArray read_arr)
{
	struct sckt_descr *sd = (struct sckt_descr*) sd_ptr;
	int read = 0;
	char *sender;
	char *data = read_data(sd, ETHERTYPE_FBRDCST, &sender, &read);
	jint r = read;
	if(!read)
		r = -1;
	(*env)->SetIntArrayRegion(env, read_arr, 0, 1, &r);
	free(sender);
	return (*env)->NewStringUTF(env, data);
}

JNIEXPORT void JNICALL Java_it_unipd_testbase_wificonnection__1_1RawInterface_1_1_m_1clear_1raw_1socket(JNIEnv *env, jclass caller, jlong sd_ptr)
{
	struct sckt_descr *sd = (struct sckt_descr*) sd_ptr;
	clear_socket(sd);
}
