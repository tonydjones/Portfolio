import csv
import sys
import sklearn

from sklearn.model_selection import train_test_split
from sklearn.neighbors import KNeighborsClassifier

TEST_SIZE = 0.4


def main():

    # Check command-line arguments
    if len(sys.argv) != 2:
        sys.exit("Usage: python shopping.py data")

    # Load data from spreadsheet and split into train and test sets
    evidence, labels = load_data(sys.argv[1])
    X_train, X_test, y_train, y_test = train_test_split(
        evidence, labels, test_size=TEST_SIZE
    )

    # Train model and make predictions
    model = train_model(X_train, y_train)
    predictions = model.predict(X_test)
    sensitivity, specificity = evaluate(y_test, predictions)

    # Print results
    print(f"Correct: {(y_test == predictions).sum()}")
    print(f"Incorrect: {(y_test != predictions).sum()}")
    print(f"True Positive Rate: {100 * sensitivity:.2f}%")
    print(f"True Negative Rate: {100 * specificity:.2f}%")


def load_data(filename):
    """
    Load shopping data from a CSV file `filename` and convert into a list of
    evidence lists and a list of labels. Return a tuple (evidence, labels).

    evidence should be a list of lists, where each list contains the
    following values, in order:
        - Administrative, an integer
        - Administrative_Duration, a floating point number
        - Informational, an integer
        - Informational_Duration, a floating point number
        - ProductRelated, an integer
        - ProductRelated_Duration, a floating point number
        - BounceRates, a floating point number
        - ExitRates, a floating point number
        - PageValues, a floating point number
        - SpecialDay, a floating point number
        - Month, an index from 0 (January) to 11 (December)
        - OperatingSystems, an integer
        - Browser, an integer
        - Region, an integer
        - TrafficType, an integer
        - VisitorType, an integer 0 (not returning) or 1 (returning)
        - Weekend, an integer 0 (if false) or 1 (if true)

    labels should be the corresponding list of labels, where each label
    is 1 if Revenue is true, and 0 otherwise.
    """
    #Admittedly copy-pasted from Heredity project cuz I'm resourceful like that
    #Makes 2 lists, one for evidence and one for labels
    evidence = []
    labels = []
    #Open csv file
    with open("shopping.csv") as f:
        reader = csv.reader(f)
        next(reader)
        #Iterate through user rows of file
        for row in reader:
            i = 0
            tmp_list = []
            for column in row:
                if i in [0,2,4,11,12,13,14]:
                    column = int(column)
                if i in [1,3,5,6,7,8,9]:
                    column = float(column)
                if i == 10:
                    if column == "Jan":
                        column = 0
                    if column == "Feb":
                        column = 1
                    if column == "Mar":
                        column = 2
                    if column == "Apr":
                        column = 3
                    if column == "May":
                        column = 4
                    if column == "June":
                        column = 5
                    if column == "Jul":
                        column = 6
                    if column == "Aug":
                        column = 7
                    if column == "Sep":
                        column = 8
                    if column == "Oct":
                        column = 9
                    if column == "Nov":
                        column = 10
                    if column == "Dec":
                        column = 11
                if i in [15,16]:
                    if column == "Returning_Visitor" or column == "TRUE":
                        column = 1
                    else:
                        column = 0
                if i == 17:
                    if column == "TRUE":
                        column = 1
                    else:
                        column = 0
                    labels.append(column)
                else:
                    tmp_list.append(column)
                    i+=1
            evidence.append(tmp_list)
            
    return (evidence,labels)


def train_model(evidence, labels):
    """
    Given a list of evidence lists and a list of labels, return a
    fitted k-nearest neighbor model (k=1) trained on the data.
    """
    model = sklearn.neighbors.KNeighborsClassifier(n_neighbors = 1)
    model.fit(evidence,labels)
    return model


def evaluate(labels, predictions):
    """
    Given a list of actual labels and a list of predicted labels,
    return a tuple (sensitivity, specificty).

    Assume each label is either a 1 (positive) or 0 (negative).

    `sensitivity` should be a floating-point value from 0 to 1
    representing the "true positive rate": the proportion of
    actual positive labels that were accurately identified.

    `specificity` should be a floating-point value from 0 to 1
    representing the "true negative rate": the proportion of
    actual negative labels that were accurately identified.
    """
    positive_count = 0
    positive = 0
    negative_count = 0
    negative = 0
    for i in range(len(labels)):
        if labels[i] == 1:
            positive_count+=1
            if predictions[i] == 1:
                positive +=1
        else:
            negative_count+=1
            if predictions[i] == 0:
                negative +=1

    sensitivity = positive / positive_count
    specificity = negative / negative_count

    return (sensitivity, specificity)


if __name__ == "__main__":
    main()
