from django.shortcuts import render
from django import forms
import os
import random

from . import util

import markdown2


def index(request):
    return render(request, "encyclopedia/index.html", {
        "entries": util.list_entries()
    })

def page(request, title):
    try:
        data = markdown2.markdown(util.get_entry(title))
        return render(request, "encyclopedia/page.html", {
        "title": title,
        "contents": data
    })
    except TypeError:
        return render(request, "encyclopedia/error.html")

def search(request):
    title = request.POST['search']
    if len(title) > 0:
        try: 
            data = markdown2.markdown(util.get_entry(title))
            return render(request, "encyclopedia/page.html", {
            "title": title,
            "contents": data
        })
        except TypeError:
            entries = util.list_entries()
            results = []
            for entry in entries:
                if title.lower() in entry.lower():
                    results.append(entry)
            return render(request, "encyclopedia/search.html", {
                "results":results
            })

class NewPage(forms.Form):
    title = forms.CharField(label="Title", min_length=1)
    content = forms.CharField(label="Content", widget=forms.Textarea, min_length=1)

class EditPage(forms.Form):
    title = forms.CharField(widget=forms.HiddenInput)
    content = forms.CharField(label="", widget=forms.Textarea, min_length=1)

def new(request):
    if request.method == "POST":
        form = NewPage(request.POST)
        if form.is_valid():
            title = form.cleaned_data['title']
            content = form.cleaned_data['content']
            try:
                file = open(f'entries/{title}.md', 'x')
                file.write(content)
                file.close()
                data = markdown2.markdown(util.get_entry(title))
                return render(request, "encyclopedia/page.html", {
                "contents":data,
                "title": title
            })
            except FileExistsError:
                return render(request, "encyclopedia/new.html", {
        "form":form,
        "FileError":True,
        "title": title
    })
        else:
            return render(request, "encyclopedia/new.html", {
        "form":form
    })
    return render(request, "encyclopedia/new.html", {
        "form":NewPage()
    })

def edit(request):
    if request.method == "POST":
        form = EditPage(request.POST)
        if form.is_valid():
            title = form.cleaned_data['title']
            content = form.cleaned_data['content']
            file = open(f'entries/{title}.md', 'w')
            file.write(content)
            file.close()
            data = markdown2.markdown(util.get_entry(title))
            return render(request, "encyclopedia/page.html", {
            "contents":data,
            "title": title
            })
        else:
            return render(request, "encyclopedia/edit.html", {
        "form":form
    })

    title = request.GET['title']
    form = EditPage(initial={'content':util.get_entry(title), 'title':title})
    return render(request, "encyclopedia/edit.html", {
        "form":form
    })

def choose(request):
    entries = util.list_entries()
    title = random.choice(entries)
    data = markdown2.markdown(util.get_entry(title))
    return render(request, "encyclopedia/page.html", {
    "title": title,
    "contents": data
    })


    
    
    