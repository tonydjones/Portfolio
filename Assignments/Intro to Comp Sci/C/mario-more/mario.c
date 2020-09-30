//include standard and cs50 functions
#include <cs50.h>
#include <stdio.h>
//define main function
int main(void)
{
    //create "size"
    int size;
    //prompt for size and repeat if not between 1 and 8
    do
    {
        size=get_int("Height: ");
    }
    while (size<1||size>8);
    //create a loop to determine number of rows
    for (int row=1;row-1<size;row++)
    {
        //create a loop for first set of spaces
        for (int space=size;space>row;space--)
        {
            //print a space
            printf(" ");
        }
        //create a loop for first set of #
        for(int blocks=0; blocks<row; blocks++)
        {
            //print a #
            printf("#");
        }
        //make a space between pyramids
        printf("  ");
        //make a loop for second set of #
        for(int newblocks=0;newblocks<row;newblocks++)
        {
            //print a #
            printf("#");
        }
        //new line for the next row of the pyramid
        printf("\n");
    }

}
