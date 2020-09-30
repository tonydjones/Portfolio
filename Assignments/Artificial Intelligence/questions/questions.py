import nltk
import sys
import string
import math
import os

#nltk.download('stopwords')

FILE_MATCHES = 1
SENTENCE_MATCHES = 1


def main():

    # Check command-line arguments
    if len(sys.argv) != 2:
        sys.exit("Usage: python questions.py corpus")

    # Calculate IDF values across files
    files = load_files(sys.argv[1])
    file_words = {
        filename: tokenize(files[filename])
        for filename in files
    }
    file_idfs = compute_idfs(file_words)

    # Prompt user for query
    query = set(tokenize(input("Query: ")))

    # Determine top file matches according to TF-IDF
    filenames = top_files(query, file_words, file_idfs, n=FILE_MATCHES)

    # Extract sentences from top files
    sentences = dict()
    for filename in filenames:
        for passage in files[filename].split("\n"):
            for sentence in nltk.sent_tokenize(passage):
                tokens = tokenize(sentence)
                if tokens:
                    sentences[sentence] = tokens

    # Compute IDF values across sentences
    idfs = compute_idfs(sentences)

    # Determine top sentence matches
    matches = top_sentences(query, sentences, idfs, n=SENTENCE_MATCHES)
    for match in matches:
        print(match)


def load_files(directory):
    """
    Given a directory name, return a dictionary mapping the filename of each
    `.txt` file inside that directory to the file's contents as a string.
    """
    files = os.scandir(directory)
    information = dict()
    for file in files:
        f = open(file.path, "r", encoding="utf8")
        information[file.name] = f.read()

    return information


def tokenize(document):
    """
    Given a document (represented as a string), return a list of all of the
    words in that document, in order.

    Process document by coverting all words to lowercase, and removing any
    punctuation or English stopwords.
    """
    words = nltk.word_tokenize(document.lower())
    for word in words:
        if word in string.punctuation or word in nltk.corpus.stopwords.words("english"):
            words.remove(word)

    words.sort()
    
    return words


def compute_idfs(documents):
    """
    Given a dictionary of `documents` that maps names of documents to a list
    of words, return a dictionary that maps words to their IDF values.

    Any word that appears in at least one of the documents should be in the
    resulting dictionary.
    """
    idf_values = dict()
    for document in documents.keys():
        for word in documents[document]:
            if word not in idf_values.keys():
                count = 0
                for text in documents.keys():
                    if word in documents[text]:
                        count+=1
                idf_values[word] = math.log(len(documents.keys()) / count)

    return idf_values

def top_files(query, files, idfs, n):
    """
    Given a `query` (a set of words), `files` (a dictionary mapping names of
    files to a list of their words), and `idfs` (a dictionary mapping words
    to their IDF values), return a list of the filenames of the the `n` top
    files that match the query, ranked according to tf-idf.
    """
    tf_idf_values = dict()
    for file in files.keys():
        tf_idf_sum = 0
        for word1 in query:
            count = 0
            for word2 in files[file]:
                if word1 == word2:
                    count+=1
            tf_idf_sum += count * idfs[word1]
        tf_idf_values[file] = tf_idf_sum

    file_list = []

    for i in range(n):
        highest = None
        for file in tf_idf_values.keys():
            if highest == None or tf_idf_values[file] > highest:
                highest = tf_idf_values[file]
                best_file = file
        file_list.append(best_file)
        del tf_idf_values[best_file]

    return file_list

def top_sentences(query, sentences, idfs, n):
    """
    Given a `query` (a set of words), `sentences` (a dictionary mapping
    sentences to a list of their words), and `idfs` (a dictionary mapping words
    to their IDF values), return a list of the `n` top sentences that match
    the query, ranked according to idf. If there are ties, preference should
    be given to sentences that have a higher query term density.
    """
    mwm = dict()
    qtd = dict()
    for sentence in sentences.keys():
        mwm_value = 0
        for word in query:
            if word in sentences[sentence]:
                mwm_value += idfs[word]
        mwm[sentence] = mwm_value
        count = 0
        for word in sentences[sentence]:
            if word in query:
                count += 1
        qtd[sentence] = count / len(sentences[sentence])

    sentence_list = []

    for i in range(n):
        best_mwm = None
        for sentence in mwm.keys():
            if best_mwm == None or mwm[sentence] > best_mwm:
                best_mwm = mwm[sentence]
                best_qtd = qtd[sentence]
                best_sentence = sentence
            elif mwm[sentence] == best_mwm:
                if qtd[sentence] > best_qtd:
                    best_mwm = mwm[sentence]
                    best_qtd = qtd[sentence]
                    best_sentence = sentence
                    
        sentence_list.append(best_sentence)
        del mwm[best_sentence]

    return sentence_list


if __name__ == "__main__":
    main()
