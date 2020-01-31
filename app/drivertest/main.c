/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * File:   main.c
 * Author: wolfi
 *
 * Created on 24. Jänner 2020, 20:49
 */

#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <errno.h>
#include <unistd.h>
#include <string.h>

void printError()
{
	char* msg = strerror(errno);
	fprintf(stderr, "An error occurred (errno=%d): %s\n", errno, msg);
}

int main(int argc, char** argv)
{
	int result = EXIT_SUCCESS;
	int f = open("/dev/stell2019", O_RDWR);
	if (f < 0) {
		printError();
		result = (EXIT_FAILURE);
		goto exit;
	}
	unsigned char buffer[4];
	size_t bread = read(f, buffer, sizeof(buffer));
	if (bread == -1) {
		printError();
		goto cleanup;
	}
	bread = read(f, buffer, sizeof(buffer));
	if (bread == -1) {
		printError();
		goto cleanup;
	}
cleanup:
	close(f);
exit:
	return result;
}

