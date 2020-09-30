"""
Tic Tac Toe Player
"""

import math
import copy

X = "X"
O = "O"
EMPTY = None


def initial_state():
    """
    Returns starting state of the board.
    """
    return [[EMPTY, EMPTY, EMPTY],
            [EMPTY, EMPTY, EMPTY],
            [EMPTY, EMPTY, EMPTY]]


def player(board):
    # counter for empty spaces
    empty = 0
    #iterate through rows
    for row in board:
        #iterate through spaces of each row
        for space in row:
            #if space is empty, increment counter
            if space == None:
                empty+=1

    #If number of empty spaces is odd, X's turn. Otherwise O
    if empty%2 == 1:
        return X
    else:
        return O


def actions(board):
    #list to hold moves
    moves = set()
    #iterator for rows
    for i in range(3):
        #iterator for spaces in row
        for j in range(3):
            #If space is empty, add coordinates to moves set
            if board[i][j] == None:
                moves.add((i,j))

    return moves


def result(board, action):
    #Check if board space is taken
    if board[action[0]][action[1]] != None:
        raise NameError('Invalid Move')

    #if valid move, generate new cloned board, not original
    new_board = copy.deepcopy(board)

    #add the move to the new board
    new_board[action[0]][action[1]] = player(board)

    return new_board


def winner(board):
    #Check for row victories
    for i in range(3):
        if board[i][0] != None:
            if board[i][0] == board[i][1]:
                if board [i][0] == board[i][2]:
                    return board[i][0]
                
    #Check for column victories
    for i in range(3):
        if board[0][i] != None:
            if board[0][i] == board[1][i]:
                if board [0][i] == board[2][i]:
                    return board[0][i]

    #Check for diagonal victories
    if board[0][0] != None:
        if board[0][0] == board[1][1]:
            if board [0][0] == board[2][2]:
                return board[0][0]

    if board[2][0] != None:
        if board[2][0] == board[1][1]:
            if board [2][0] == board[0][2]:
                return board[2][0]

    return None


def terminal(board):

    #Check for winner
    if winner(board) != None:
        return True

    # counter for empty spaces
    empty = 0
    #iterate through rows
    for row in board:
        #iterate through spaces of each row
        for space in row:
            #if space is empty, increment counter
            if space == None:
                empty+=1

    #If number of empty spaces is 0, game over
    if empty == 0:
        return True


    return False
    


def utility(board):

    if winner(board) == X:
        return 1
    if winner(board) == O:
        return -1
    
    return 0

def minimax(board):

    #return none if terminal
    if terminal(board):
        return None

    #determine player which will determine decision polarity
    if player(board) == X:
        polarity = 1
    else:
        polarity = -1

    #Holder for utility comparison
    util = -2
    
    #generate list of possible actions
    choices = actions(board)

    #iterate through actions
    for action in choices:

        #check to see if winning action, and if so do it immediately
        if terminal(result(board,action)):
            return action
        
        #Recursive function to search future prospects, come up with future terminal board given optimal play
        future = copy.deepcopy(potential(board, action))
        
        #determine value of future board (guaranteed to be terminal)
        value = (utility(future) * polarity)
        #if value is greater than current utility, consider this the optimal move
        if value > util:
            util = value
            optimal = action

    return optimal


#Function to determine potential utility
def potential(board, action):

    new = result(board, action)

    #return new board if terminal
    if terminal(new):
        return new

    #if not terminal, look at potential future actions. Calculate optimal moves in the future
    else:

        #determine player which will determine decision polarity
        if player(new) == X:
            polarity = 1
        else:
            polarity = -1
        
        choices = actions(new)
        util = -2
        for choice in choices:
            #recursion into the future to generate terminal board
            endgame = copy.deepcopy(potential(new, choice))
            #calculate utility based on polarity
            value = (utility(endgame) * polarity)
            #if value is greater than current utility, consider this the optimal move
            if value > util:
                util = value
                optimal = copy.deepcopy(endgame)
                #if it's a winning action, perform immediately
                if util == 1:
                    return optimal
                
        #After iterating through all potential choices, return optimal outcome
        return optimal
                
        
    














            
