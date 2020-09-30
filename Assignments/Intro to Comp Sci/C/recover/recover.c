#include <stdio.h>
#include <stdlib.h>
#include <string.h>

long int size;
int jpg_start(unsigned char bytes[size], long int location);

int main(int argc, char *argv[])
{
    //check to determine proper number of arguments, return error if not
    if (argc != 2)
    {
        printf("Usage: ./recover image");
        return 1;
    }
    //open input file and save to memory as FILE type called "file"
    FILE *file = fopen(argv[1], "r");
    //if file can't be opened, inform user and return error
    if (file == NULL)
    {
        printf("Cannot open file");
        return 1;
    }
    //find size of file
    fseek(file, 0, SEEK_END);
    size = ftell(file);
    //reset file to start
    fseek(file, 0, SEEK_SET);
    //make an array big enough for all bytes of file
    unsigned char bytes[size];
    //write bytes of file to bytes array
    fread(bytes, 1, size, file);
    //make jpg counter
    int jpg = 0;
    //iterate through bytes array
    for (long int i = 0; i < size; i++)
    {
        //check if start of jpg
        if (jpg_start(bytes,i) == 0)
        {
            //create file number for jpg storage
            char jpgid[8];
            sprintf(jpgid, "%.3i.jpg", jpg);
            //increment jpg counter
            jpg++;
            //start compiling image from bytes
            FILE *newjpg = fopen(jpgid,"a");
            for (long int j = i; (jpg_start(bytes,j) == 1 || j == i) && j < size; j++)
                {
                    //add bytes to jpg
                    fprintf(newjpg, "%c", bytes[j]);
                    if (j == i)
                    {
                        printf("%li\n",j);
                    }
                }
        }
    }
    return 0;
}

int jpg_start(unsigned char bytes[size], long int location)
{
    //check if we're at the end of the array
    if (location + 3 >= size)
    {
        return 1;
    }
    //check if first character is 0xff
    else if (bytes[location] != 0xff)
    {
        return 1;
    }
    //check if second character is 0xd8
    else if (bytes[location + 1] != 0xd8)
    {
        return 1;
    }
    //check if third character is 0xff
    else if (bytes[location +2] != 0xff)
    {
        return 1;
    }
    //check if fourth character is 0xe-
    else if (bytes[location+3] == 0xe0 || bytes[location+3] == 0xe1 || bytes[location+3] == 0xe2 || bytes[location+3] == 0xe3 || bytes[location+3] == 0xe4 || bytes[location+3] == 0xe5 || bytes[location+3] == 0xe6 || bytes[location+3] == 0xe7 || bytes[location+3] == 0xe8 || bytes[location+3] == 0xe9 || bytes[location+3] == 0xea || bytes[location+3] == 0xeb || bytes[location+3] == 0xec || bytes[location+3] == 0xed || bytes[location+3] == 0xee || bytes[location+3] == 0xef)
    {
        return 0;
    }
    else
    {
        return 1;
    }
}