#import argv, exit, dictreader, and sqlite
from sys import argv, exit
from csv import DictReader
import cs50

#define function for finding names
def get_names(data):
    names = []
    name = ""
    for char in data:
        if char == " ":
            names.append(name)
            name = ""
        else:
            name += char
    names.append(name)
    if len(names) == 2:
        names.insert(1, None)
    return names

# check number of arguments
if len(argv) != 2:
    print("Incorrect number of command line arguments")
    exit()
    
# create empty database called students
open("students.db", "w").close()

#open empty database in sqlite
db = cs50.SQL("sqlite:///students.db")

# create table in the database
db.execute("CREATE TABLE students (first TEXT, middle TEXT, last TEXT, house TEXT, birth NUMERIC)")

#open csv
with open(argv[1], "r") as students:

    # create dictreader
    reader = DictReader(students)

    # iterate through rows of the reader
    for row in reader:

        # get names
        names = get_names(row["name"])

        #set values
        first = names[0]
        middle = names[1]
        last = names[2]
        house = row["house"]
        birth = row["birth"]

        #insert students into the student table
        db.execute("INSERT INTO students (first, middle, last, house, birth) VALUES(?,?,?,?,?)", first, middle, last, house, birth)
