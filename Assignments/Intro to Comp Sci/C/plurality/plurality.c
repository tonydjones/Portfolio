#include <cs50.h>
#include <stdio.h>
#include <string.h>

// Max number of candidates
#define MAX 9

// Candidates have name and vote count
typedef struct
{
    string name;
    int votes;
}
candidate;

// Array of candidates
candidate candidates[MAX];

// Number of candidates
int candidate_count;

// Function prototypes
bool vote(string name);
void print_winner(void);

int main(int argc, string argv[])
{
    // Check for invalid usage
    if (argc < 2)
    {
        printf("Usage: plurality [candidate ...]\n");
        return 1;
    }

    // Populate array of candidates
    candidate_count = argc - 1;
    if (candidate_count > MAX)
    {
        printf("Maximum number of candidates is %i\n", MAX);
        return 2;
    }
    for (int i = 0; i < candidate_count; i++)
    {
        candidates[i].name = argv[i + 1];
        candidates[i].votes = 0;
    }

    int voter_count = get_int("Number of voters: ");

    // Loop over all voters
    for (int i = 0; i < voter_count; i++)
    {
        string name = get_string("Vote: ");

        // Check for invalid vote
        if (!vote(name))
        {
            printf("Invalid vote.\n");
        }
    }

    // Display winner of election
    print_winner();
}

// Update vote totals given a new vote
bool vote(string name)
{
    // iterate through the names of the candidates
    for (int i=0;candidates[i].name!='\0';i++)
    {
        //check if candidate name is same as vote name
        if (strcmp(candidates[i].name,name)==0)
        {
            //increment candidate's vote counter
            candidates[i].votes++;
            return true;
        }
    }
    return false;
}

// Print the winner (or winners) of the election
void print_winner(void)
{
    //create a counter to hold the current highest number of votes
    int highest=0;
    //create counter to determine number of candidates with highest number of votes
    int victors=0;
    
    // iterate through list of candidates
    for (int i=0;i<candidate_count;i++)
    {
        //check if current candidate votes are equal to current highest value
        if (candidates[i].votes==highest)
        {
            //increment number of victors by 1
            victors++;
        }
        //check if candidate votes are greater than current highest value
        if (candidates[i].votes>highest)
        {
            //set counter to be current candidate's number of votes
            highest=candidates[i].votes;
            //set number of victors to 1
            victors=1;
        }
    }
    //create an array just the right size for number  of winners, although those winners are not yet input
    string winner[victors];
    //create a counter called assigned to keep track of how any winners we have assigned to the array thus far
    int assigned=0;
    //iterate through list of candidates
    for (int i=0;assigned<victors;i++)
    {
        //check if current candidate has the highest number of votes
        if (candidates[i].votes==highest)
        {
            //add candidate's name to the winner's array
            winner[assigned]=candidates[i].name;
            //increment assigned counter
            assigned++;
        }
    }
    //print winners' names
    for (int i=0;i<victors;i++)
    {
        printf("%s\n", winner[i]);
    }
    return;
}

