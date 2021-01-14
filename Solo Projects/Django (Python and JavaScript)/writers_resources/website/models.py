from django.contrib.auth.models import AbstractUser
from django.db import models
from jsonfield import JSONField
from datetime import datetime


class User(AbstractUser):
    timestamp = models.DateTimeField(blank=True, null=True)
    connections = models.ManyToManyField("User", blank=True, default=None)
    connections_count = models.IntegerField(default=0)
    messages = models.ManyToManyField("Message", blank=True, default=None)
    pen_name = models.CharField(blank=True, null=True, default=None, max_length=100)
    summary = models.CharField(blank=True, null=True, default=None, max_length=1000)

    author = models.BooleanField(default=False, blank=False, null=False)
    author_fantasy = models.BooleanField(default=False, blank=False, null=False)
    author_scifi = models.BooleanField(default=False, blank=False, null=False)
    author_historical = models.BooleanField(default=False, blank=False, null=False)
    author_bio = models.BooleanField(default=False, blank=False, null=False)
    author_realistic = models.BooleanField(default=False, blank=False, null=False)
    author_children = models.BooleanField(default=False, blank=False, null=False)
    author_young = models.BooleanField(default=False, blank=False, null=False)
    author_adult = models.BooleanField(default=False, blank=False, null=False)
    author_spiritual = models.BooleanField(default=False, blank=False, null=False)
    author_help = models.BooleanField(default=False, blank=False, null=False)
    author_short = models.BooleanField(default=False, blank=False, null=False)
    author_novel = models.BooleanField(default=False, blank=False, null=False)
    author_poetry = models.BooleanField(default=False, blank=False, null=False)
    author_comic = models.BooleanField(default=False, blank=False, null=False)
    editor = models.BooleanField(default=False, blank=False, null=False)
    editor_fantasy = models.BooleanField(default=False, blank=False, null=False)
    editor_scifi = models.BooleanField(default=False, blank=False, null=False)
    editor_historical = models.BooleanField(default=False, blank=False, null=False)
    editor_bio = models.BooleanField(default=False, blank=False, null=False)
    editor_realistic = models.BooleanField(default=False, blank=False, null=False)
    editor_children = models.BooleanField(default=False, blank=False, null=False)
    editor_young = models.BooleanField(default=False, blank=False, null=False)
    editor_adult = models.BooleanField(default=False, blank=False, null=False)
    editor_spiritual = models.BooleanField(default=False, blank=False, null=False)
    editor_help = models.BooleanField(default=False, blank=False, null=False)
    editor_short = models.BooleanField(default=False, blank=False, null=False)
    editor_novel = models.BooleanField(default=False, blank=False, null=False)
    editor_poetry = models.BooleanField(default=False, blank=False, null=False)
    editor_comic = models.BooleanField(default=False, blank=False, null=False)
    agent = models.BooleanField(default=False, blank=False, null=False)
    agent_fantasy = models.BooleanField(default=False, blank=False, null=False)
    agent_scifi = models.BooleanField(default=False, blank=False, null=False)
    agent_historical = models.BooleanField(default=False, blank=False, null=False)
    agent_bio = models.BooleanField(default=False, blank=False, null=False)
    agent_realistic = models.BooleanField(default=False, blank=False, null=False)
    agent_children = models.BooleanField(default=False, blank=False, null=False)
    agent_young = models.BooleanField(default=False, blank=False, null=False)
    agent_adult = models.BooleanField(default=False, blank=False, null=False)
    agent_spiritual = models.BooleanField(default=False, blank=False, null=False)
    agent_help = models.BooleanField(default=False, blank=False, null=False)
    agent_short = models.BooleanField(default=False, blank=False, null=False)
    agent_novel = models.BooleanField(default=False, blank=False, null=False)
    agent_poetry = models.BooleanField(default=False, blank=False, null=False)
    agent_comic = models.BooleanField(default=False, blank=False, null=False)
    publisher = models.BooleanField(default=False, blank=False, null=False)
    publisher_fantasy = models.BooleanField(default=False, blank=False, null=False)
    publisher_scifi = models.BooleanField(default=False, blank=False, null=False)
    publisher_historical = models.BooleanField(default=False, blank=False, null=False)
    publisher_bio = models.BooleanField(default=False, blank=False, null=False)
    publisher_realistic = models.BooleanField(default=False, blank=False, null=False)
    publisher_children = models.BooleanField(default=False, blank=False, null=False)
    publisher_young = models.BooleanField(default=False, blank=False, null=False)
    publisher_adult = models.BooleanField(default=False, blank=False, null=False)
    publisher_spiritual = models.BooleanField(default=False, blank=False, null=False)
    publisher_help = models.BooleanField(default=False, blank=False, null=False)
    publisher_short = models.BooleanField(default=False, blank=False, null=False)
    publisher_novel = models.BooleanField(default=False, blank=False, null=False)
    publisher_poetry = models.BooleanField(default=False, blank=False, null=False)
    publisher_comic = models.BooleanField(default=False, blank=False, null=False)
    proofreader = models.BooleanField(default=False, blank=False, null=False)

    author_titles = models.ManyToManyField("Title", blank=True, null=True, default=None, related_name="author")
    author_count = models.IntegerField(default=0)

    editor_titles = models.ManyToManyField("Title", blank=True, null=True, default=None, related_name="editor")
    editor_count = models.IntegerField(default=0)

    agent_titles = models.ManyToManyField("Title", blank=True, null=True, default=None, related_name="agent")
    agent_count = models.IntegerField(default=0)

    publisher_titles = models.ManyToManyField("Title", blank=True, null=True, default=None, related_name="publisher")
    publisher_count = models.IntegerField(default=0)

    proofreader_titles = models.ManyToManyField("Title", blank=True, null=True, default=None, related_name="proofreader")
    proofreader_count = models.IntegerField(default=0)

    def get_time(self):
        return {
            "timestamp": self.timestamp.strftime("%b %#d %Y, %#I:%M %p")
            }


class Title(models.Model):
    title = models.CharField(blank=True, default=None, max_length=100)
    owner = models.ForeignKey("User", blank=True, default=None, on_delete=models.CASCADE)



class Message(models.Model):
    sender = models.ForeignKey("User", blank=True, default=None, on_delete=models.CASCADE, related_name="sent")
    receiver = models.ForeignKey("User", blank=True, default=None, on_delete=models.CASCADE, related_name="received")
    body = models.TextField(blank=True, default=None, null=True)
