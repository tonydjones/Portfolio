//include cs50, standard, and math functions
#include <cs50.h>
#include <math.h>
#include <stdio.h>
//define main function
int main(void)
{
    //define validity integer. if valid=1, we're good
    int valid=0;
    //make credit card number(ccn)
    long ccn;
    //prompt for ccn from user
    ccn=get_long("Number: ");
    //determine number of digits
    int digits=0;
    long ccn2=ccn;
    while (ccn2!=0)
    {
        ccn2/=10;
        digits++;
    }
    //create counter called verify for product addition multiple of 10 verification
    int verify=0;
    //create a digit keeper called digit
    int digit;
    //create a value for our calculated products
    int product;
    //create a value for our first and second digits
    int first=0;
    int second=0;
    //cycle through digits starting from the last digit and working right to left
    for (int repeat=1;ccn>=1;repeat++)
    {
        digit = ccn%10;
    //if the digit is one of the every-other digits, multiply by 2 and call this product
        if (repeat%2==0)
        {
            product=digit*2;
    //add digits of product to a counter called verify
            for (;product>=1;)
            {
                verify+=(product%10);
                product/=10;
            }
        }
        else
        {
            //if it's not one of the multiplied digits, just add the digit to verify
            verify+=digit;
        }
    //if we are looking at the second digit, save it to second
        if (repeat==digits-1)
        {
            second=digit;
        }
    //if we are looking at first digit, save it to first
        if (repeat==digits)
        {
            first=digit;
        }
        //divide by 10 and repeat the loop until we get through every digit
        ccn/=10;
    }
    //we should now have number of digits, first and second digits, and the 10x verification
    //begin with 10x verification
    if (verify%10==0)
    {
        //check for AMEX digits count
        if (digits==15)
        {
            //check for AMEX first digit 3
            if (first==3)
            {
                //check for AMEX second digit 4 or 7
                if (second==4||second==7)
                {
                    //print AMEX and set valid to 1
                    printf("AMEX");
                    valid=1;
                }
            }
        }
        //check for MC digit count
        if (digits==16)
        {
            //check for MC first digit 5
            if (first==5)
            {
                //check for MC second digit 1-5
                if (second>0&&second<6)
                {
                    //print MASTERCARD and change valid to 1
                    printf("MASTERCARD");
                    valid=1;
                }
            }
        }
        //check for VISA digit count
        if (digits==13||digits==16)
        {
            //check for Visa first number 4
            if (first==4)
            {
                //print VISA and set valid to 1
                printf("VISA");
                valid=1;
            }
        }
    }
    //if valid still equals 0, print invalid
    if (valid==0)
    {
        printf("INVALID");
    }
    //printf("%i\n",verify);
    //printf("%i\n", first);
    //printf("%i\n", second);
    //printf("%i\n", digits);
    //print a line for the next command
    printf("\n");
}
