//include standard and cs50 operations
#include <cs50.h>
#include <stdio.h>

//define main function
int main(void)
{
    //make "size"
    int size;
    //prompt for size, repeat if not between 1 and 8
    do 
{
    size = get_int("Height: ");
}
while (size<1 || size>8);


//this loop determines how many rows to make
for (int repeat=1; repeat-1<size; repeat++)
{
    //this loop will determine how many spaces in each row
    for (int sign=size; sign>repeat; sign--)
    {
        //print a space
        printf(" ");
    }
    //this loop will determine and print the correct number of # signs at the end of the row
    for (int sign=0; sign<repeat; sign++)
    {
        //print a # sign
        printf("#");
    }
    //start new line for next row of pyramid
    printf("\n");
}
}

