//include standard and cs50 command sets
#include <stdio.h>
#include <cs50.h>

//code to carry out
int main(void)
{
    //ask for the user's name
    string name = get_string("What is your name?\n");
    //display the user's name
    printf("hello, %s\n", name);
}
