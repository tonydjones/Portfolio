from cs50 import get_float

change = 0

while change <=0:
    change = 100 * get_float("Change owed: ")

coins = 0

while change >= 25:
    change -= 25
    coins +=1

while change >= 10:
    change -= 10
    coins += 1

while change >= 5:
    change -=5
    coins +=1

while change >= 1:
    change -=1
    coins +=1

print(coins)