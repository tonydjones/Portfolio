import itertools
import random
import copy

class Minesweeper():
    """
    Minesweeper game representation
    """

    def __init__(self, height=8, width=8, mines=8):

        # Set initial width, height, and number of mines
        self.height = height
        self.width = width
        self.mines = set()

        # Initialize an empty field with no mines
        self.board = []
        for i in range(self.height):
            row = []
            for j in range(self.width):
                row.append(False)
            self.board.append(row)

        # Add mines randomly
        while len(self.mines) != mines:
            i = random.randrange(height)
            j = random.randrange(width)
            if not self.board[i][j]:
                self.mines.add((i, j))
                self.board[i][j] = True

        # At first, player has found no mines
        self.mines_found = set()

    def print(self):
        """
        Prints a text-based representation
        of where mines are located.
        """
        for i in range(self.height):
            print("--" * self.width + "-")
            for j in range(self.width):
                if self.board[i][j]:
                    print("|X", end="")
                else:
                    print("| ", end="")
            print("|")
        print("--" * self.width + "-")

    def is_mine(self, cell):
        i, j = cell
        return self.board[i][j]

    def nearby_mines(self, cell):
        """
        Returns the number of mines that are
        within one row and column of a given cell,
        not including the cell itself.
        """

        # Keep count of nearby mines
        count = 0

        # Loop over all cells within one row and column
        for i in range(cell[0] - 1, cell[0] + 2):
            for j in range(cell[1] - 1, cell[1] + 2):

                # Ignore the cell itself
                if (i, j) == cell:
                    continue

                # Update count if cell in bounds and is mine
                if 0 <= i < self.height and 0 <= j < self.width:
                    if self.board[i][j]:
                        count += 1

        return count

    def won(self):
        """
        Checks if all mines have been flagged.
        """
        return self.mines_found == self.mines


class Sentence():
    """
    Logical statement about a Minesweeper game
    A sentence consists of a set of board cells,
    and a count of the number of those cells which are mines.
    """

    def __init__(self, cells, count):
        self.cells = set(cells)
        self.count = count

    def __eq__(self, other):
        return self.cells == other.cells and self.count == other.count

    def __str__(self):
        return f"{self.cells} = {self.count}"

    def known_mines(self):     
        #if count = number of cells, return whole set, they are all mines
        if self.count == len(self.cells):
            return self.cells
        else:
            return set()
        

    def known_safes(self):
        #if count = 0, return whole set, they are all safe
        if self.count == 0:
            return self.cells
        else:
            return set()

    def mark_mine(self, cell):
        #check if cell is in this sentence
        if cell in self.cells:
            #remove cell from sentence and subtract 1 from mine count
            self.count -= 1
            self.cells.remove(cell)

    def mark_safe(self, cell):
        #check if cell is in this sentence
        if cell in self.cells:
            #remove cell from sentence but DON'T subtract from count,
            #since it was not a mine 
            self.cells.remove(cell)


class MinesweeperAI():
    """
    Minesweeper game player
    """

    def __init__(self, height=8, width=8):

        # Set initial height and width
        self.height = height
        self.width = width

        # Keep track of which cells have been clicked on
        self.moves_made = set()

        # Keep track of cells known to be safe or mines
        self.mines = set()
        self.safes = set()

        # List of sentences about the game known to be true
        self.knowledge = []

    def mark_mine(self, cell):
        """
        Marks a cell as a mine, and updates all knowledge
        to mark that cell as a mine as well.
        """
        self.mines.add(cell)
        for sentence in self.knowledge:
            sentence.mark_mine(cell)

    def mark_safe(self, cell):
        """
        Marks a cell as safe, and updates all knowledge
        to mark that cell as safe as well.
        """
        self.safes.add(cell)
        for sentence in self.knowledge:
            sentence.mark_safe(cell)

    def add_knowledge(self, cell, count):
        """
        Called when the Minesweeper board tells us, for a given
        safe cell, how many neighboring cells have mines in them.

        This function should:
            1) mark the cell as a move that has been made
            2) mark the cell as safe
            3) add a new sentence to the AI's knowledge base
               based on the value of `cell` and `count`
            4) mark any additional cells as safe or as mines
               if it can be concluded based on the AI's knowledge base
            5) add any new sentences to the AI's knowledge base
               if they can be inferred from existing knowledge
        """
        #Add to moves made
        self.moves_made.add(cell)
        #Mark cell as safe
        self.mark_safe(cell)
        #Get a tuple x,y from the cell and generate set
        x = cell[0]
        y = cell[1]
        cells = set()
        #iterate through all cells surrounding the move
        for i in range(-1,2):
            for j in range(-1,2):
                #exclude the center cell
                if j == 0 and i == 0:
                    continue
                #exclude cells outside the game
                elif i+x < 0 or i+x >= self.width or j+y < 0 or j+y >= self.height:
                    continue
                else:
                    #Add tuple to cells set
                    cells.add((i + x, j + y))
        #After getting all the cells, generate sentence
        info = Sentence(cells, count)

        #process new information. This is what actually adds info to knowledge base
        self.process(info, [])
        #this function updates safe and mines
        self.check()

        #If sentence is empty, remove
        for compare in self.knowledge:
            if len(compare.cells) == 0:
                self.knowledge.remove(compare)


    
                        

    def process(self, sentence, processed):
        processed.append(sentence.cells)
        #Run through knowledge base comparing new sentence to previous sentences
        for compare in self.knowledge:
            #check to see if either sentence is a subset of the other
            if sentence.cells.issubset(compare.cells):
                #If so, add inferred sentence to knowledge base
                new_count = compare.count - sentence.count
                new_cells = set()
                for unit in compare.cells:
                    if unit not in sentence.cells:
                        new_cells.add(unit)
                #recursively process the new sentence if not done already
                inferred = Sentence(new_cells, new_count)
                if inferred.cells not in processed:
                    self.process(inferred, processed)

            elif compare.cells.issubset(sentence.cells):
                new_count = sentence.count - compare.count
                new_cells = set()
                for unit in sentence.cells:
                    if unit not in compare.cells:
                        new_cells.add(unit)
                inferred = Sentence(new_cells, new_count)
                if inferred.cells not in processed:
                    self.process(inferred, processed)

        #Add current sentence to the set
        self.knowledge.append(sentence)



    def check(self):
        #after making changes, check each sentence for safe moves
        for sentence in self.knowledge:
            safe = copy.deepcopy(sentence.known_safes())
            not_safe = copy.deepcopy(sentence.known_mines())
            for unit in safe:
                self.mark_safe(unit)
            #Same for mines
            for unit in not_safe:
                self.mark_mine(unit)

                
    def make_safe_move(self):
        """
        Returns a safe cell to choose on the Minesweeper board.
        The move must be known to be safe, and not already a move
        that has been made.

        This function may use the knowledge in self.mines, self.safes
        and self.moves_made, but should not modify any of those values.
        """
        #iterate through safe moves until you find one that has not yet been played
        for move in self.safes:
            if move not in self.moves_made:
                return move
        #If we make it through the end of the list, return None
        return None

    def make_random_move(self):
        """
        Returns a move to make on the Minesweeper board.
        Should choose randomly among cells that:
            1) have not already been chosen, and
            2) are not known to be mines
        """
        choice = None
        options = []
        #generate full moves list
        for i in range(self.width):
            for j in range(self.height):
                #make sure move has not been made
                if (i,j) not in self.moves_made:
                    #make sure move is not a mine
                    if (i,j) not in self.mines:
                        options.append((i,j))
        #if there are no options, return None
        if len(options) == 0:
            return None

        #pick a random option from generated list
        choice = random.choice(options)
        return choice

        """
        For kicks and giggles I wrote this extra bit to determine a
        rough intuitive probability for each option based on the knowledge
        base, so rather than picking a choice randomly the AI can choose
        the option that is, at least intuitively, least likely to blow up.
        Better to take the 1/8 chance than the 1/3 chance, right?
        """
        best_chance = 1
        #iterate through generated options
        for option in options:
            #Could set chance to 1/8, but the AI wouldn't actually know that. I
            #only know it because I can read the code...But for the purposes of this
            #drill we'll say the AI doesn't know how many bombs are placed.
            #Better then to pick a square we know nothing about than one that
            #has a 1/8 chance of exploding. Gather more information that way.
            chance = 0
            for sentence in self.knowledge:
                #look to see if current option is in sentences
                if option in sentence.cells:
                    #use sentence count and length of cell set to calculate probability
                    prob = sentence.count / len(sentence.cells)
                    if prob > chance:
                        #Looking for the highest explosive probability for this square
                        chance = prob
            if chance < best_chance:
                #If this option has lower odds of exploding than current best, it becomes
                #the optimal
                best_chance = chance
                choice = option

        #return choice
        
