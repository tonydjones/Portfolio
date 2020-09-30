// Implements a dictionary's functionality

#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include "dictionary.h"
#include <ctype.h>

// Represents a node in a hash table
typedef struct node
{
    bool used;
    bool end;
    struct node *next[27];
}
node;

int wordcount = 0;
int s;
int add(int location, node *array[27], unsigned char dict[s], char alpha[27]);

bool compare(int letter, node *array[27], char alpha[27], const char *word);

int space(node *array[27]);

bool erase(node *array[27]);

//create separate array of alphabetic characters for later reference.
char alphabet[27];

// Number of buckets in hash table
const unsigned int N = 27;

// Hash table
node *table[27];

// Returns true if word is in dictionary else false
bool check(const char *word)
{
    //use recursive function stated later on
    return compare(0, table, alphabet, word);
}

// Hashes word to a number
unsigned int hash(const char *word)
{
    // TODO
    return 0;
}

// Loads dictionary into memory, returning true if successful else false
bool load(const char *dictionary)
{
    // load dictionary file or return "could not load dictionary" if it doesn't work
    FILE *raw = fopen(dictionary, "r");
    if (raw == NULL)
    {
        printf("Cannot open dictionary");
        return false;
    }
    //make space in hash table
    space(table);
    //populate alphabet array
    alphabet[26] = (char)39;
    for (int i = 0; i < 26; i++)
    {
        alphabet[i] = (char)(97+i);
    }
    //get size of dictionary
    fseek(raw, 0, SEEK_END);
    int size = ftell(raw);
    //reset file to start
    fseek(raw, 0, SEEK_SET);
    //make an array big enough for all characters of file
    unsigned char dict[size];
    //write bytes of file to dict array
    fread(dict, 1, size, raw);
    fclose(raw);
    //iterate through dictionary
    for (int i = 0; i < size;)
    {
        //if we find \n, reset to beginning of hash table for next word
        if (dict[i] == '\n')
        {
            i++;
        }
        //if it's a letter, begin filling out hash table
        else
        {
            i += add(i, table, dict, alphabet);
        }
    }
    return true;
}

// Returns number of words in dictionary if loaded else 0 if not yet loaded
unsigned int size(void)
{
    if (wordcount == 0)
    {
        return 0;
    }
    else
    {
        return wordcount;
    }
}

// Unloads dictionary from memory, returning true if successful else false
bool unload(void)
{
    return erase(table);
}

//add words to dictionary
int add(int location, node *array[27], unsigned char dict[s], char alpha[27])
{
    //if we find enter character, mark the previous letter to be the end and return 1
    if (dict[location] == '\n')
    {
        
        return 1;
    }
    else
    {
        int j = -1;
        do
        {
            j++;
            //check if dictionary letter matches
            if (dict[location] == alpha[j])
            {
                //check to see if next character in word is \n, if so we need to mark this as the end of the word
                if (dict[location+1] == '\n')
                {
                    array[j]->end = true;
                    wordcount++;
                }
                //check to see if current node is used
                if (array[j]->used == true)
                {
                    //continue along the chain
                    return 1 + (add(location+1, (*array[j]).next, dict, alpha));
                }
                //if node not yet activated, activate, and continue along the chain
                else
                {
                    array[j]->used = true;
                    space(array[j]->next);
                    return 1 + (add(location+1, (*array[j]).next, dict, alpha));
                }
            }
        }
        while (dict[location] != alpha[j] && j < 27);
    }
    printf("error with add");
    return 0;
}

//recursive function to check word against dictionary
bool compare(int letter, node *array[27], char alpha[27], const char *word)
{
    //if we find null character, return false (word started out fine but possibly cut short)
    if (word[letter] == '\0')
    {
        return false;
    }
    else
    {
        int j = -1;
        do
        {
            j++;
            //check if word letter matches alphabet letter
            if (tolower(word[letter]) == alpha[j])
            {
                //check to see if current node is used
                if (array[j]->used == true)
                {
                    //check to see if next character is null and this node can be a final node
                    if (word[letter+1] == '\0' && array[j]->end == true)
                    {
                        return true;
                    }
                    //if this isn't the final letter, continue along the chain
                    return (compare(letter+1, (*array[j]).next, alpha, word));
                }
                //if node not yet activated, return false
                else
                {
                    return false;
                }
            }
        }
        while (word[letter] != alpha[j] && j < 27);
    }
    printf("error with compare\n");
    return false;
}

// give space to arrays
int space(node *array[27])
{
    for (int i = 0; i < 27; i++)
    {
        array[i] = malloc(sizeof(node));
        array[i]->used = false;
        array[i]->end = false;
    }
    return 0;
}

//function to unload all subsequent data
bool erase(node *array[27])
{
    for (int i = 0; i < 27; i++)
    {
        //check if node is false
        if (array[i]->used == false)
        {
            //eliminate node immediately
            free(array[i]);
        }
        //else we need to follow the chain to get to the end, and then come back up to delete this node
        else
        {
            erase(array[i]->next);
            free(array[i]);
        }
    }
    return true;
}