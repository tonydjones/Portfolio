import os
import random
import re
import sys
import copy
#from numpy.random import choice

DAMPING = 0.85
SAMPLES = 10000


def main():
    if len(sys.argv) != 2:
        sys.exit("Usage: python pagerank.py corpus")
    corpus = crawl(sys.argv[1])
    ranks = sample_pagerank(corpus, DAMPING, SAMPLES)
    print(f"PageRank Results from Sampling (n = {SAMPLES})")
    for page in sorted(ranks):
        print(f"  {page}: {ranks[page]:.4f}")
    ranks = iterate_pagerank(corpus, DAMPING)
    print(f"PageRank Results from Iteration")
    for page in sorted(ranks):
        print(f"  {page}: {ranks[page]:.4f}")


def crawl(directory):
    """
    Parse a directory of HTML pages and check for links to other pages.
    Return a dictionary where each key is a page, and values are
    a list of all other pages in the corpus that are linked to by the page.
    """
    pages = dict()

    # Extract all links from HTML files
    for filename in os.listdir(directory):
        if not filename.endswith(".html"):
            continue
        with open(os.path.join(directory, filename)) as f:
            contents = f.read()
            links = re.findall(r"<a\s+(?:[^>]*?)href=\"([^\"]*)\"", contents)
            pages[filename] = set(links) - {filename}

    # Only include links to other pages in the corpus
    for filename in pages:
        pages[filename] = set(
            link for link in pages[filename]
            if link in pages
        )

    return pages


def transition_model(corpus, page, damping_factor):
    """
    Return a probability distribution over which page to visit next,
    given a current page.

    With probability `damping_factor`, choose a link at random
    linked to by `page`. With probability `1 - damping_factor`, choose
    a link at random chosen from all pages in the corpus.
    """
    #New dictionary for storing values
    model = {}
    #Make list of keys from corpus
    keys = corpus.keys()
    #Iterate through keys
    for target in keys:
        #if the target page is in the set of linked pages of the current page. Does not apply if page links to itself.
        if target in corpus[page] and target != page:
            #increment counter
            probability = damping_factor / len(corpus[page]) + ((1 - damping_factor) / len(keys))
            model[target] = probability
        #Otherwise, probability only factors the random possibility
        else:
            probability = (1 - damping_factor) / len(keys)
            model[target] = probability
            
    #After checking every page, return probability dictionary
    return model
                


def sample_pagerank(corpus, damping_factor, n):
    """
    Return PageRank values for each page by sampling `n` pages
    according to transition model, starting with a page at random.

    Return a dictionary where keys are page names, and values are
    their estimated PageRank value (a value between 0 and 1). All
    PageRank values should sum to 1.
    """
    #New dictionary for storing values
    model = dict()
    #Make list of keys from corpus
    keys = list(corpus.keys())
    #Perform action n times
    for i in range(n):
        #If it's our first run, populate the model list and generate a random page to start
        if i == 0:
            for site in keys:
                model[site] = 0
            page = random.choice(keys)
            model[page] += float(1/n)
        #If it's not our first time...
        else:
            #prepare lists for sites and respective probabilities
            options = []
            probabilities = []
            #generate probability dictionary
            tmp_model = transition_model(corpus, page, damping_factor)
            #iterate through all sites
            for site in keys:
                #Add site and respective probability to lists
                options.append(site)
                probabilities.append(tmp_model[site])
            #Pick from generated lists given probabilities
            pagelist = random.choices(options, weights = probabilities, k = 1)
            page = pagelist[0]
            model[page] = model[page] + float(1/n)

    #After n repetitions, return completed model
    return model


def iterate_pagerank(corpus, damping_factor):
    """
    Return PageRank values for each page by iteratively updating
    PageRank values until convergence.

    Return a dictionary where keys are page names, and values are
    their estimated PageRank value (a value between 0 and 1). All
    PageRank values should sum to 1.
    """
    #Generate dictionary of initial pageranks
    old_model = {}
    #Make list of keys from corpus
    keys = corpus.keys()
    for site in keys:
        old_model[site] = 1 / len(keys)
    print(old_model)

    difference = 1
    while difference > .001:
        #Dictionary for new model
        new_model = {}
        difference = 0
        for target in keys:
            pr = 0
            for sender in keys:
                if target != sender and target in corpus[sender]:
                    pr += damping_factor * old_model[sender] / len(corpus[sender])
                elif len(corpus[sender]) == 0:
                    pr += damping_factor * old_model[sender] / len(keys)
            pr += (1 - damping_factor) / len(keys)
            new_model[target] = pr
            diff = new_model[target] - old_model[target]
            if abs(diff) > difference:
                difference = abs(diff)
        old_model = copy.deepcopy(new_model)
    return old_model


if __name__ == "__main__":
    main()
