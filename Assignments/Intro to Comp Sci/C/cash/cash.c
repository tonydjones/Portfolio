//include stanard and cs50 and math functions
#include <cs50.h>
#include <stdio.h>
#include <math.h>
//define main function
int main(void)
{
    //make change
    int change;
    
    //prompt for change and repeat if not positive. convert to cent value by multiplying by 100 and rounding up
    do
    {
        change=round(100*get_float("Change owed: "));

    }
    while (change<0);
    //create value for counting coins
    int coins=0;
    //deduct quarters from the change owed until no longer possible and increment counter
    while (change>=25)
    {
        //deduct 25 cents
        change-=25;
        //increment coin counter
        coins+=1;
    }
    //deduct dimes from change owed until no longer possible and increment counter
    while (change>=10)
    {
        //deduct 10 cents
        change-=10;
        //increment coin counter
        coins+=1;
    }
    //deduct nickels from change and increment counter
    while (change>=5)
    {
        //deduct 5 cents
        change-=5;
        //increment coin counter
        coins+=1;
    }
    //deduct pennies from change and increment counter
    while (change>=1)
    {
        //deduct one cent
        change-=1;
        //increment coin counter
        coins+=1;
    }
    //print number of coins needed
    printf("%i\n", coins);
}
