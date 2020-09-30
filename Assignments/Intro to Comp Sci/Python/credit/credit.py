from cs50 import get_int

number = str(get_int("Number: "))

code = number[0] + number [1]

id = "INVALID"

if len(number) == 15:
    if code == "34" or code == "37":
        id = "AMEX"

if len(number) == 13 or len(number) == 16:
    if number[0] == "4":
        id = "VISA"

if len(number) == 16:
    if int(code) > 50 and int(code) < 56:
        id = "MASTERCARD"

calc = int(number)

sum = 0
counter = 1

while calc >= 1:
    if counter % 2 == 0:
        product = (calc % 10) * 2
        while product >= 1:
            sum += product % 10
            product = int(product / 10)
    else:
        sum += calc % 10
    counter += 1
    calc = int(calc / 10)

if sum % 10 != 0:
    id = "INVALID"

print(id)