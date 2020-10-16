from django.contrib.auth.models import AbstractUser
from django.db import models


class User(AbstractUser):
    following = models.ManyToManyField("User", related_name="followers")
    my_posts = models.ManyToManyField("Post", related_name="post_owner")
    following_posts = models.ManyToManyField("Post", related_name="post_follower")
    liked_posts = models.ManyToManyField("Post", related_name="post_likers")

class Post(models.Model):
    owner = models.ForeignKey("User", on_delete=models.CASCADE, related_name="posts", default=None)
    timestamp = models.DateTimeField(blank=True)
    body = models.TextField(blank=True)
    likes = models.IntegerField(default=0, blank=True)

    def serialize(self):
        return {
            "id": self.id,
            "owner": self.owner.username,
            "likes": self.likes,
            "body": self.body,
            "timestamp": self.timestamp.strftime("%b %#d %Y, %#I:%M %p")
            }
