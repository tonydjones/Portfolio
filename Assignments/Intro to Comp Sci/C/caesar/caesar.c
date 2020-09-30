//include cs50, standard, and letter recognition functions
#include <cs50.h>
#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>

//define main function, should accept arguments
int main(int argc, string argv[])
{
    //if no arguments entered, return and print error message
    if (argc!=2)
    {
        printf("Error 1: Incorrect number of key values provided\n");
        return 1;
    }
    //check to make sure argument is digits only
    for (int b=0; argv[1][b]!='\0'; b++)
    {
        if (isdigit(argv[1][b])==0)
        {
            printf("Usage: ./caesar key\n");
            return 1;   
        }
    }
    //prompt user for plaintext
    string plain = get_string("plaintext: ");
    //set key value
    int key = atoi(argv[1])%26;
    //printf("%i\n",key);
    //prepare ciphertext with cipher string
    string cipher=plain;
    //prepare ACSII value keeper
    int value;
    //iterate through plain string
    for (int a=0; plain[a]!='\0';a++)
    {
        //check to see if char is alpha
        if (isalpha(plain[a]))
        {
            //convert char of plain to ASCII number called value
            value = (int) plain[a];
            //printf("%c\n",plain[a]);
            //printf("%i\n", value);
            //check if char is uppercase
            if (isupper(plain[a]))
            {
                //if uppercase, subtract 64 to get 1-26 numbers
                value-=65;
                //add key value and get ramainder when divided by 26
                value = (value+key);
                //re-convert value to a character in cipher string
                cipher[a] = (char)((value%26)+65);
            }
            //if lowercase
            if (islower(plain[a]))
            {
                //subtract 96 to get 1-26 numbers
                value=value-97;
                //printf("%i\n",value);
                //add key value and get remainder when divided by 26
                value = (value+key);
                //printf ("%i\n", value);
                //re-convert value to a character in cipher string
                cipher[a] = (char)((value%26)+97);
                //printf("%c\n",cipher[a]);
            }
        }
    }
    //after iterating through plain string, print cipher string and return 0
    printf("ciphertext: %s\n", cipher);
    return 0;
}