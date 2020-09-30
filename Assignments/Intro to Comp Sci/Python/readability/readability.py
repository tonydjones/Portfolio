from cs50 import get_string

text = get_string("Text: ")

words = 0
sentences = 0
letters = 0

for i in range(len(text)):
    if i == 0:
        if text[i].isalpha():
            words += 1
    elif text[i].isalpha() and (text[i-1] == " " or text[i-1] == '"'):
        words +=1
    if text[i].isalpha():
        letters += 1
    if text[i] in ["!","?","."]:
        sentences += 1


L = (letters/words)*100;
S = (sentences/words)*100;
index = 0.0588 * L - 0.296 * S - 15.8
grade = round(.0588*L-.296*S-15.8)

if grade >= 16:
    print("Grade 16+")
elif grade < 1:
    print("Before Grade 1")
else:
    print("Grade " + str(grade))