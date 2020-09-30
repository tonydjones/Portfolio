#include <cs50.h>
#include <stdio.h>
#include <string.h>

// Max number of candidates
#define MAX 9

// preferences[i][j] is number of voters who prefer i over j
int preferences[MAX][MAX];

// locked[i][j] means i is locked in over j
bool locked[MAX][MAX];

// Each pair has a winner, loser
typedef struct
{
    int winner;
    int loser;
}
pair;

// Array of candidates
string candidates[MAX];
pair pairs[MAX * (MAX - 1) / 2];

int pair_count;
int candidate_count;

// Function prototypes
bool vote(int rank, string name, int ranks[]);
void record_preferences(int ranks[]);
void add_pairs(void);
void sort_pairs(void);
void lock_pairs(void);
void print_winner(void);

int main(int argc, string argv[])
{
    // Check for invalid usage
    if (argc < 2)
    {
        printf("Usage: tideman [candidate ...]\n");
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
        candidates[i] = argv[i + 1];
    }

    // Clear graph of locked in pairs
    for (int i = 0; i < candidate_count; i++)
    {
        for (int j = 0; j < candidate_count; j++)
        {
            locked[i][j] = false;
        }
    }

    pair_count = 0;
    int voter_count = get_int("Number of voters: ");

    // Query for votes
    for (int i = 0; i < voter_count; i++)
    {
        // ranks[i] is voter's ith preference
        int ranks[candidate_count];

        // Query for each rank
        for (int j = 0; j < candidate_count; j++)
        {
            string name = get_string("Rank %i: ", j + 1);

            if (!vote(j, name, ranks))
            {
                printf("Invalid vote.\n");
                return 3;
            }
        }

        record_preferences(ranks);

        printf("\n");
    }

    add_pairs();
    sort_pairs();
    lock_pairs();
    print_winner();
    return 0;
}

// Update ranks given a new vote
bool vote(int rank, string name, int ranks[])
{
    // iterate through the names of the candidates
    for (int i = 0; i < candidate_count; i++)
    {
        //check if candidate name is same as vote name
        if (strcmp(candidates[i], name) == 0)
        {
            //add this voter's candidate (as the candidate's place in the candidate array) preference to global rank array
            ranks[rank] = i;
            //return true, complete function
            return true;
        }
    }
    return false;
}

// Update preferences given one voter's ranks
void record_preferences(int ranks[])
{
    // iterate through the voter's ranks
    for (int i = 0; i < candidate_count; i++)
    {
        //iterate through ranks again starting one spot ahead of i
        for (int j = i + 1; j < candidate_count; j++)
        {
            //increment the counter to indicate this voter prefers the candidate they ranked i over the candidate they ranked j
            preferences[ranks[i]][ranks[j]]++;
        }
    }
    return;
}

// Record pairs of candidates where one is preferred over the other
void add_pairs(void)
{
    //iterate through preferences array
    for (int i = 0; i < candidate_count; i++)
    {
        //iterate through preferences again starting one spot ahead of i
        for (int j = i + 1; j < candidate_count; j++)
        {
            //compare preferences[i][j] and [j][i] to determine if voters prefer i over j
            if (preferences[i][j] > preferences[j][i])
            {
                //if yes, make i the winner and j the loser in a pair structure
                pairs[pair_count].winner = i;
                pairs[pair_count].loser = j;
                //increment pairs counter so next pair gets added in next slot in array
                pair_count++;
            }
            //check if more voters prefer j over i
            if (preferences[i][j] < preferences[j][i])
            {
                //if yes, make i the winner and j the loser in a pair structure
                pairs[pair_count].winner = j;
                pairs[pair_count].loser = i;
                //increment pairs counter so next pair gets added in next slot in array
                pair_count++;
            }
        }
    }
    return;
}

// Sort pairs in decreasing order by strength of victory
void sort_pairs(void)
{
    //create a new empty array to populate with pairs
    pair sortedpairs[pair_count];
    //create keeper to determine current most favorable pairing
    int record = 0;
    //create another keeper to determine previous most favorable pairing
    int pastrecord;
    //iterate through the current sortedpairs list
    for (int i = 0; i < pair_count;)
    {
        //iterate through original pairs list
        for (int j = 0; j < pair_count; j++)
        {
            //if this pairing is current most preferable pairing (by referring to preferences array) and less than best pairing of previous iteration
            if (preferences[pairs[j].winner][pairs[j].loser] > record && (preferences[pairs[j].winner][pairs[j].loser] < pastrecord
            || pastrecord == '\0'))
            {
                //change current record to current favorable pairing
                record = preferences[pairs[j].winner][pairs[j].loser];
            }
        }
        //iterate through original pairs list again
        for (int j = 0; j < pair_count; j++)
        {
            //if current pairing matches the current record
            if (preferences[pairs[j].winner][pairs[j].loser] == record)
            {
                //add pairing to sorted pairs list
                sortedpairs[i].winner = pairs[j].winner;
                sortedpairs[i].loser = pairs[j].loser;
                //increment sortedpairs counter so that next pair goes into the next slot
                i++;
            }
        }
        //reset past record to current record and current record to 0
        pastrecord = record;
        record = 0;
    }
    //iterate through sorted pairs array
    for (int i = 0; i < pair_count; i++)
    {
        //transfer sortedpair data over to pair
        pairs[i].winner = sortedpairs[i].winner;
        pairs[i].loser = sortedpairs[i].loser;
    }
    return;
}
//check_loop prototype
bool check_loop(int candidate, int original);

// Lock pairs into the candidate graph in order, without creating cycles
void lock_pairs(void)
{
    //iterate through pairs list
    for (int i = 0; i < pair_count; i++)
    {
        //check to make sure pair won't result in a loop
        if (check_loop(pairs[i].loser, pairs[i].winner) == true)
        {
            //if we're safe, lock in the pairing
            locked[pairs[i].winner][pairs[i].loser] = true;
        }
    }
    return;
}

// Print the winner of the election
void print_winner(void)
{
    //create bool winner as trigger for printing name
    bool winner = false;
    //iterate through the array of locked pairs (this one is for winners)
    for (int i = 0; i < candidate_count; i++)
    {
        //iterate through array of locked pairs again (this one is for losers)
        for (int j = 0; j < candidate_count; j++)
        {
            //check to see if current pair is locked
            if (locked[i][j] == true)
            {
                //set bool winner to true
                winner = true;
                //iterate through array of locked pairs again
                for (int k = 0; k < candidate_count; k++)
                {
                    //check to see if current pair winner is ever a locked-in loser
                    if (locked[k][i] == true)
                    {
                        //set winner to false (false alarm)
                        winner = false;
                    }
                }
                //check to see if still winner
                if (winner == true)
                {
                    //print winner's name and terminate
                    printf("%s\n", candidates[i]);
                    return;
                }
            }
        }
    }
    return;
}

// check to see if previous pair in chain is locked. return true if good to go, false if it would create a loop
bool check_loop(int candidate, int original)
{
    //check all of the favorable pairings in pair array
    for (int i = 0; i < pair_count; i++)
    {
        //check if current pairing favors candidate
        if (pairs[i].winner == candidate)
        {
            //check if pair is locked via the locked array
            if (locked[pairs[i].winner][pairs[i].loser] == true)
            {
                //check if locked into current "original" (candidate where the loop started)
                if (pairs[i].loser == original)
                {
                    //if so, return false, this is a loop
                    return false;
                }
                //if it's not a loop yet, check_loop again, go further along the chain.
                if (check_loop(pairs[i].loser, original) == false)
                {
                    //if it comes back false, there's a loop later down the line, return false
                    return false;
                }
            }
        }
    }
    //if we make it all the way here, there is no loop yet in the locked chains, return true
    return true;
}