from logic import *

AKnight = Symbol("A is a Knight")
AKnave = Symbol("A is a Knave")

BKnight = Symbol("B is a Knight")
BKnave = Symbol("B is a Knave")

CKnight = Symbol("C is a Knight")
CKnave = Symbol("C is a Knave")

# Puzzle 0
# A says "I am both a knight and a knave."
knowledge0 = And(
    #A is either a knight or knave
    Or(AKnight, AKnave),
    #But not both
    Not(And(AKnight, AKnave)),
    #If A is a knight, the statement he made must be true
    Biconditional(AKnight, And(AKnight, AKnave))
)

# Puzzle 1
# A says "We are both knaves."
# B says nothing.
knowledge1 = And(
    #A and B are either knights or knaves but not both
    Or(AKnight, AKnave),
    Not(And(AKnight, AKnave)),
    Or(BKnight, BKnave),
    Not(And(BKnight, BKnave)),
    #If A is a knight, Then both A and B are Knaves
    Biconditional(AKnight, And(AKnave,BKnave))
)

# Puzzle 2
# A says "We are the same kind."
# B says "We are of different kinds."
knowledge2 = And(
    #A and B are either knights or knaves but not both
    Or(AKnight, AKnave),
    Not(And(AKnight, AKnave)),
    Or(BKnight, BKnave),
    Not(And(BKnight, BKnave)),
    #If A is a knight, then either both A and B are knights, or they are both knaves
    Biconditional(AKnight, Or(And(AKnight,BKnight), And(AKnave,BKnave))),
    #If B is a knight, then between A and B one is a knight and one is a knave
    Biconditional(BKnight, Or(And(AKnight,BKnave), And(AKnave,BKnight)))
)

# Puzzle 3
# A says either "I am a knight." or "I am a knave.", but you don't know which.
# B says "A said 'I am a knave'."
# B says "C is a knave."
# C says "A is a knight."
knowledge3 = And(
    #A B and C are either knights or knaves but not both
    Or(AKnight, AKnave),
    Not(And(AKnight, AKnave)),
    Or(BKnight, BKnave),
    Not(And(BKnight, BKnave)),
    Or(CKnight, CKnave),
    Not(And(CKnight, CKnave)),
    #If C is a knight, then A is a knight, and same if both knaves
    Biconditional(CKnight, AKnight),
    Biconditional(CKnave, AKnave),
    #If B is a knight, then C is a knave, and vice versa
    Biconditional(BKnight, CKnave),
    Biconditional(BKnave, CKnight),
    #If A and B are both knights, then A is a knave, one way implication
    Implication(And(BKnight, AKnight),AKnave),
    #If B is a knave and A is a knight, then A is a knight
    Implication(And(BKnave, AKnight),AKnight),
    #If B is a knight and A is a knave, then A is a knight
    Implication(And(BKnight, AKnave),AKnight),
    #If A and B are both knaves, then A is a knave, one way implication
    Implication(And(BKnave, AKnave),AKnave)
)


def main():
    symbols = [AKnight, AKnave, BKnight, BKnave, CKnight, CKnave]
    puzzles = [
        ("Puzzle 0", knowledge0),
        ("Puzzle 1", knowledge1),
        ("Puzzle 2", knowledge2),
        ("Puzzle 3", knowledge3)
    ]
    for puzzle, knowledge in puzzles:
        print(puzzle)
        if len(knowledge.conjuncts) == 0:
            print("    Not yet implemented.")
        else:
            for symbol in symbols:
                if model_check(knowledge, symbol):
                    print(f"    {symbol}")


if __name__ == "__main__":
    main()
