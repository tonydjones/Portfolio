#import argv, exit
import sys
import cs50

# check number of arguments
if len(sys.argv) != 2:
    print("Incorrect number of command line arguments")
    exit()
else:
    house = sys.argv[1]

# open the db in sqlite
db = cs50.SQL("sqlite:///students.db")

# execute action in sqlite to retrieve list of student names in the house
population = db.execute("SELECT first, middle, last, birth FROM students WHERE house = ? ORDER BY last, first", house)

#iterate through population and print ther data as "Full Name, born year"
for student in population:
    if student["middle"] == None:
        print(student["first"], student["last"] + ", born", student["birth"])
    else:
        print(student["first"], student["middle"], student["last"] + ", born", student["birth"])