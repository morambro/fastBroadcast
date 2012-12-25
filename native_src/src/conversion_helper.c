/*
 * conversion_helper.c
 *
 *  Created on: Nov 25, 2012
 *      Author: fabio
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "../lib/conversion_helper.h"

//returns the hex representation for the given string. Must call free() on returned value once finished
unsigned char* cstrtohex(char *str)
{
	char substr[2] = "__";
	int len = strlen(str);
	unsigned char *a = malloc(sizeof(unsigned char)*(len/2)+1);
	memset((void*)a, 0, sizeof(unsigned char)*(len/2)+1);
	int i;
	for(i = 0; i < len/2; i++) {
		substr[0] = str[i*2];
		substr[1] = str[i*2+1];
		sscanf( substr, "%hx", &a[i] );
	}
	return a;
}

char* hextocstr(unsigned char *hex, int lenght)
{
	char *converted = malloc(lenght*2 + 1);
	int i;
	for(i=0;i<lenght;i++)
		sprintf(&converted[i*2], "%02X", *(hex+i));
	return converted;
}
