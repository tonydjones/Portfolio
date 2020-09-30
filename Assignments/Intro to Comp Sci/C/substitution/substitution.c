//include cs50, standard, string conversion, and letter recognition functions
#include <cs50.h>
#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>

//define main function, should accept arguments
int main(int argc, string argv[])
{
    //if wrong number of arguments entered, return and print error message
    if (argc!=2)
    {
        printf("Error 1: Incorrect number of keys provided\n");
        return 1;
    }
    //create key string for later use
    string key=argv[1];
    //create char holder for later use
    char c;
    //check to make sure argument is alphabet only and contains 26 characters
    int count = 0;
    for (int b=0; argv[1][b]!='\0'; b++)
    {
        if (isalpha(argv[1][b])==0)
        {
            printf("Error: Invalid Characters\n");
            return 1;
        }
        else
        {
            count++;
            //convert character to uppercase and save as temporary char
            c = toupper(argv[1][b]);
        }
        //iterate through key string and check if currecnt char c is already in it. if yes, return error
        for (int d=0; d<b;d++)
        {
            if (key[d]==c)
            {
                printf("Error: Duplicated Character\n");
                return 1;
            }
        }
        //after iterating through key, if char is not yet in key string, add to key string
        key[b]=c;
    }
    if (count!=26)
    {
        printf("Error: Not Enough Characters\n");
        return 1;
    }
    //prompt user for plaintext
    string plain = get_string("plaintext: ");
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
            //check if char is uppercase
            if (isupper(plain[a]))
            {
                //if uppercase, subtract 65 to get 0-25 numbers
                value-=65;
                //go to key[value] to find the character at that point and add that letter to cipher string
                cipher[a] = key[value];
            }
            //check if lowercase
            if (islower(plain[a]))
            {
                //if lowercase, subtract 97 to get 0-25 numbers
                value-=97;
                //go to key[value] to find character at that point and convert to lowercase, then add that letter to cipher string
                cipher[a] = tolower(key[value]);
            }
        }
    }
    //after iterating through plain string, print cipher string and return 0
    printf("ciphertext: %s\n", cipher);
    return 0;
}