import sys

from crossword import *


class CrosswordCreator():

    def __init__(self, crossword):
        """
        Create new CSP crossword generate.
        """
        self.crossword = crossword
        self.domains = {
            var: self.crossword.words.copy()
            for var in self.crossword.variables
        }

    def letter_grid(self, assignment):
        """
        Return 2D array representing a given assignment.
        """
        letters = [
            [None for _ in range(self.crossword.width)]
            for _ in range(self.crossword.height)
        ]
        for variable, word in assignment.items():
            direction = variable.direction
            for k in range(len(word)):
                i = variable.i + (k if direction == Variable.DOWN else 0)
                j = variable.j + (k if direction == Variable.ACROSS else 0)
                letters[i][j] = word[k]
        return letters

    def print(self, assignment):
        """
        Print crossword assignment to the terminal.
        """
        letters = self.letter_grid(assignment)
        for i in range(self.crossword.height):
            for j in range(self.crossword.width):
                if self.crossword.structure[i][j]:
                    print(letters[i][j] or " ", end="")
                else:
                    print("█", end="")
            print()

    def save(self, assignment, filename):
        """
        Save crossword assignment to an image file.
        """
        from PIL import Image, ImageDraw, ImageFont
        cell_size = 100
        cell_border = 2
        interior_size = cell_size - 2 * cell_border
        letters = self.letter_grid(assignment)

        # Create a blank canvas
        img = Image.new(
            "RGBA",
            (self.crossword.width * cell_size,
             self.crossword.height * cell_size),
            "black"
        )
        font = ImageFont.truetype("assets/fonts/OpenSans-Regular.ttf", 80)
        draw = ImageDraw.Draw(img)

        for i in range(self.crossword.height):
            for j in range(self.crossword.width):

                rect = [
                    (j * cell_size + cell_border,
                     i * cell_size + cell_border),
                    ((j + 1) * cell_size - cell_border,
                     (i + 1) * cell_size - cell_border)
                ]
                if self.crossword.structure[i][j]:
                    draw.rectangle(rect, fill="white")
                    if letters[i][j]:
                        w, h = draw.textsize(letters[i][j], font=font)
                        draw.text(
                            (rect[0][0] + ((interior_size - w) / 2),
                             rect[0][1] + ((interior_size - h) / 2) - 10),
                            letters[i][j], fill="black", font=font
                        )

        img.save(filename)

    def solve(self):
        """
        Enforce node and arc consistency, and then solve the CSP.
        """
        self.enforce_node_consistency()
        self.ac3()
        return self.backtrack(dict())

    def enforce_node_consistency(self):
        """
        Update `self.domains` such that each variable is node-consistent.
        (Remove any values that are inconsistent with a variable's unary
         constraints; in this case, the length of the word.)
        """
        #Make iterable word list
        word_list = self.crossword.words.copy()
        #iterate through dictionary of variables
        for var in self.domains:
            #iterate through words of word list
            for word in word_list:
                #if length of word is not equal to length of variable
                if len(word) != var.length:
                    #remove word from variable's domain of choices
                    self.domains[var].remove(word)
                    #Test printing functions
                    #print(word + " is not " + str(var.length) + " letters long.")
                #else:
                    #print(word + " is " + str(var.length) + " letters long.")

    def revise(self, x, y):
        """
        Make variable `x` arc consistent with variable `y`.
        To do so, remove values from `self.domains[x]` for which there is no
        possible corresponding value for `y` in `self.domains[y]`.

        Return True if a revision was made to the domain of `x`; return
        False if no revision was made.
        """
        #Make list of variable overlaps
        overlap = self.crossword.overlaps[x,y]
        #If there are no overlaps, terminate function
        if overlap == None:
            return False
        else:
            #Boolean to keep track of whether revisions were made to x
            revised = False
        #get indices from the overlap
        x_index = overlap[0]
        y_index = overlap[1]
        #Copy list of x domain for iterating
        x_domain = self.domains[x].copy()
        #check words in x domain
        for x_word in x_domain:
            #temporary works boolean to keep track of whether y has compatible options
            works = False
            #Check words in y domain
            for y_word in self.domains[y]:
                #if the y word is compatible with the x word, set wors to True
                if y_word[y_index] == x_word[x_index]:
                    works = True
                    #Print function for testing
                    #print(x_word + " letter " + str(x_index) + " compatible with " + y_word + " letter " + str(y_index))
                    break
            #After checking all y domain words, if works is still false,
            #We could not find any compatible options for the x word. Remove
            #And set revised to true
            if works == False:
                self.domains[x].remove(x_word)
                revised = True
                #Print function for testing
                #print(x_word + " incompatible.")

        #After iterating through all words, return revised status
        return revised

                
    def ac3(self, arcs=None):
        """
        Update `self.domains` such that each variable is arc consistent.
        If `arcs` is None, begin with initial list of all arcs in the problem.
        Otherwise, use `arcs` as the initial list of arcs to make consistent.

        Return True if arc consistency is enforced and no domains are empty;
        return False if one or more domains end up empty.
        """
        #if no arcs, then make our own list of all arcs in crossword
        if arcs == None:
            #empty list to populate
            arcs = []
            #iterate through variables
            for var in self.domains:
                #get neighbors from crossword
                for neighbor in self.crossword.neighbors(var):
                    #add each variable-neighbor tuple to list of arcs
                    arcs.append((var, neighbor))

        #iterate through list of arcs as long as there are still arcs in it
        while len(arcs) > 0:
            #assign an arc and remove it from the queue
            arc = arcs[0]
            arcs.remove(arc)
            #process the arc
            if self.revise(arc[0], arc[1]):
                #Testing print function
                #print("Arc revised")
                if len(self.domains[arc[0]]) == 0:
                    #If the domain of variable has no options, return false, no solution
                    return False
                #If still possible, add more arcs to the queue based on neighbors of modified domain
                for neighbor in self.crossword.neighbors(arc[0]):
                    #add each variable-neighbor tuple to list of arcs
                    arcs.append((neighbor, arc[0]))
            #else:
                #print("Arc NOT revised")
        #Make it through all the arcs, return true
        return True
            

    def assignment_complete(self, assignment):
        """
        Return True if `assignment` is complete (i.e., assigns a value to each
        crossword variable); return False otherwise.
        """
        #iterate through variables
        for var in self.domains:
            #if the variable is not in the assignment, the assignment is incomplete
            if var not in assignment.keys():
                return False
            #if the variable has no assignment, return false
            if assignment[var] == None:
                return False
        #if we make it to the end, return true
        return True

    def consistent(self, assignment):
        """
        Return True if `assignment` is consistent (i.e., words fit in crossword
        puzzle without conflicting characters); return False otherwise.
        """
        #iterate through variables in assignment
        for var in assignment:
            #check all other values in assignment for duplication
            for var_2 in assignment:
                #don't check itself
                if var_2 == var:
                    continue
                #if assignment is the same, not consistent, return false
                if assignment[var_2] == assignment[var]:
                    return False
            #check that the variable's lenggth matches the chosen word
            if var.length != len(assignment[var]):
                return False
            #Check neighbors for conflict
            for neighbor in self.crossword.neighbors(var):
                #if the neighbor has not been assigned, skip
                if neighbor not in assignment.keys():
                    continue
                #Get the overlap of var and neighbor
                overlap = self.crossword.overlaps[var, neighbor]
                #assign indices
                var_index = overlap[0]
                neighbor_index = overlap[1]
                #check for conflict at indices. If conflict, return false
                if assignment[var][var_index] != assignment[neighbor][neighbor_index]:
                    return False

        #If we make it to the end, no issues, return true
        return True

    def order_domain_values(self, var, assignment):
        """
        Return a list of values in the domain of `var`, in order by
        the number of values they rule out for neighboring variables.
        The first value in the list, for example, should be the one
        that rules out the fewest values among the neighbors of `var`.
        """
        #Make list for sorting domains
        sorted_domains = []
        #make dictionary for tracking constraint counts
        counts = dict()
        #iterate through words of var domain
        for word in self.domains[var]:
            #add count to counts dictionary
            counts[word] = self.count_changes(var, word, assignment)
        #iterate through dictionary as long as it still has members
        while len(counts) > 0:
            highest_cost = -1
            #iterate through words of var domain
            for word in counts.keys():
                #if cost is higher than current highest cost, reassign highest cost
                if counts[word] > highest_cost:
                    highest_cost = counts[word]
                    worst_word = word
            #After finding the word with the highest cost, insert into list
            sorted_domains.insert(0,worst_word)
            #remove the word from the dictionary
            del counts[worst_word]

        #Once the dictionary is out of words, return the sorted list
        return sorted_domains

    def count_changes(self, var, word, assignment):
        """
        Returns an integer counting how constraining this word would be to
        its neighbors if applied. the count adds one for every option removed
        from neighbors
        """
        #Create counter
        count = 0
        #Get list of neighbors
        for neighbor in self.crossword.neighbors(var):
            #Make sure neighbor is not already assigned. If so, skip
            if neighbor in assignment.keys() and assignment[neighbor] != None:
                continue
            #Make tuple of variable overlap
            overlap = self.crossword.overlaps[var,neighbor]
            #get indices from the overlap
            var_index = overlap[0]
            neighbor_index = overlap[1]
            #Check words in neighbor domain
            for neighbor_word in self.domains[neighbor]:
                #if the neighbor word is incompatible with the var word, increment counter
                if word[var_index] != neighbor_word[neighbor_index]:
                    count += 1
            #After checking all y domain words, return count of constraints
        return count
        

    def select_unassigned_variable(self, assignment):
        """
        Return an unassigned variable not already part of `assignment`.
        Choose the variable with the minimum number of remaining values
        in its domain. If there is a tie, choose the variable with the highest
        degree. If there is a tie, any of the tied variables are acceptable
        return values.
        """
        #Start by making counters for choices and nodes
        fewest_choices = None
        most_arcs = 0
        #Go through all variables
        for var in self.domains.keys():
            #If variable is already assigned, skip. otherwise carry on
            if var in assignment.keys() and assignment[var] != None:
                continue
            #get values for how many choices and how many connected nodes
            #for this particular variable
            choices = len(self.domains[var])
            arcs = len(self.crossword.neighbors(var))
            #Check if this node has fewer choices than the current fewest
            if fewest_choices == None or fewest_choices > choices:
                fewest_choices = choices
                most_arcs = arcs
                variable = var

            #if number of choices is equal, compare arcs
            elif fewest_choices == choices:
                if most_arcs < arcs:
                    most_arcs = arcs
                    variable = var

        #After checking all unassigned variables, return the current best variable
        return variable

        

    def backtrack(self, assignment):
        """
        Using Backtracking Search, take as input a partial assignment for the
        crossword and return a complete assignment if possible to do so.

        `assignment` is a mapping from variables (keys) to words (values).

        If no assignment is possible, return None.
        """
        #if assignment is complete, function will return the assignment
        if self.assignment_complete(assignment):
            return assignment
        #else we'll start processing. start by picking a variable to assign
        variable = self.select_unassigned_variable(assignment)
        #get list of options for the variable, ordered by
        options = self.order_domain_values(variable, assignment)
        #if there are no options, return None
        if len(options) == 0:
            return None
        #iterate through the options, try the best one first
        for option in options:
            #assign the variable to the chosen option
            assignment[variable] = option
            #Check the assignment for consistency. if not, try next option
            if not self.consistent(assignment):
                continue
            #Check further down the line to see if the assignment works
            #If it returns None, we failed somewhere down the line, try next option
            if self.backtrack(assignment) == None:
                continue
            #if we get a solid return, that's our assignment!
            else:
                return self.backtrack(assignment)
        #If we iterate through all options without returning an assignment,
        #it failed. return None.
        return None
                
        


def main():

    # Check usage
    if len(sys.argv) not in [3, 4]:
        sys.exit("Usage: python generate.py structure words [output]")

    # Parse command-line arguments
    structure = sys.argv[1]
    words = sys.argv[2]
    output = sys.argv[3] if len(sys.argv) == 4 else None

    # Generate crossword
    crossword = Crossword(structure, words)
    creator = CrosswordCreator(crossword)
    assignment = creator.solve()

    # Print result
    if assignment is None:
        print("No solution.")
    else:
        creator.print(assignment)
        if output:
            creator.save(assignment, output)


if __name__ == "__main__":
    main()
