//include standard and cs50 and letter classification and math functions
#include <cs50.h>
#include <stdio.h>
#include <ctype.h>
#include <math.h>
//define main function
int main(void)
{
    //prompt user for text and save as string
    string text=get_string("Text: ");
    //count letters in text
    int letters=0;
    for (int i=0; text[i]!='\0'; i++)
    {
        //only count if alphabet letter
        if (isalpha(text[i]))
        {
            letters++;
        }
    }
    //after iterating through text, pring number of letters counted
    printf("%i letter(s)\n",letters);
    //iterate through text to count words
    int words=0;
    for (int j=0;text[j]!='\0';j++)
    {
        //check if we're on the first character, special rules
        if (j==0)
        {
            //if it's a character, increment counter. if not, do nothing
            if isalpha(text[j])
            {
                words++;
            }
        }
        //check if current character is an alpha and if previous character is a space or "
        if (isalpha(text[j]) && (isblank(text[j-1]) || text[j-1]=='"'))
        {
            //increment word counter
            words++;
        }
    }
    //display number of words counted
    printf("%i word(s)\n", words);
    //count sentences by counting periods, question marks, and exclamation points
    int sentences=0;
    for (int k=0;text[k]!='\0';k++)
    {
        //check if char is period ,exclamation, or question
        if (text[k]=='.'||text[k]==(int)'!'||text[k]==(int)'?')
        {
            //increment counter
            sentences++;
        }
    }
    //print sentences counted
    printf("%i sentence(s)\n",sentences);
    //process formula to calculate grade level
    float L = ((float)letters/words)*100;
    float S = ((float)sentences/words)*100;
    float grade = .0588*L-.296*S-15.8;
    //print float grade
    printf("%f\n",grade);
    //check if grade is above 16
    if (grade>=16)
    {
        //print 16+
        printf("Grade 16+");
    }
    //check if grade is less than 1
    else if (grade<1)
    {
        //print before grade 1
        printf("Before Grade 1");
    }
    //output all other grade values
    else
    {
        printf("Grade %i",(int)round(grade));
    }
    //finish with a new line for next input
    printf("\n");
}