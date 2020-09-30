##import modules to read csv files and interpret arguments
import csv
from sys import argv, exit

##function to check if given correct unmber of arguments
def check(argv):
    if len(argv) != 3:
        print("Missing command-line arguments")
        exit()

##function to import csv and translate into a list of lists
def get_csv(c):
    file = open(c, "r")
    with file:
        data_list1 = csv.reader(file)
        data_list = []
        for row in data_list1:
            data_list.append(row)
    return data_list

##function to import DNA sequence and save as a string
def get_dna(dna):
    file = open(dna)
    sequence = ""
    for char in file:
        sequence += char
    return sequence

##function to translate data list into a dictionary where the keys are names and the values are dictionaries (where the values are the repeat sequence
##and the keys are number of repeats)
def make_dict(rawdata):
    data = {}
    for i in rawdata:
        tmp = {}
        for j in range(len(i)-1):
            try:
                tmp[rawdata[0][j+1]] = int(i[j+1])
            except:
                tmp[rawdata[0][j+1]] = 0
        data[i[0]] = tmp
    return data

##function to iterate through DNA sequence and look for the designated repeat
def update_data(data, sequence, repeat):
    for i in range(len(sequence)):
        seq = ""
        for j in range(len(repeat)):
            if j + i < len(sequence):
                seq += sequence[i+j]
        if seq == repeat:
            reps = count_repeats(repeat, sequence, i)
            if reps > data["name"][repeat]:
                data["name"][repeat] = reps


##function to count repeats once found and update data if necessary
def count_repeats(repeat, sequence, location):
    count = 0
    while location < (len(sequence) - len(repeat)):
        seq = ""
        for j in range (len(repeat)):
            seq += sequence[j + location]
        if seq == repeat:
            count +=1
        else:
            return count
        location += (len(repeat))
    return count

##function to compare individual data with sequence data
def find_match(data):
    names = list(data.keys())
    for i in range(len(data.keys())-1):
        for seq in data["name"].keys():
            match = names[i + 1]
            if data["name"][seq] != data[names[i+1]][seq]:
                match = "No match"
                break
        if match == "No match":
            continue
        return match
    return match

##check arguments
check(argv)

##import csv and convert to readable data
rawdata = get_csv(argv[1])

##import DNA and store as string
sequence = get_dna(argv[2])

##turn data into 2D dictionary
data = make_dict(rawdata)

##find repeats in sequence and update data as necessary
for seq in data["name"].keys():
    update_data(data, sequence, seq)

##check for a DNA match and print result
print(find_match(data))