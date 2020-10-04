from django.contrib.auth.models import AbstractUser
from django.db import models

class Item(models.Model):
    title = models.CharField(max_length=64)
    price = models.DecimalField(decimal_places=2, max_digits=8, null=True)
    current_price = models.DecimalField(decimal_places=2, max_digits=8, default=0, null=True)
    description = models.CharField(max_length=200)
    image = models.URLField(default=None, null=True)
    category = models.CharField(max_length=64, default=None, null=True)
    active = models.BooleanField(default=True)
    owner = models.ForeignKey('User', related_name="selling", on_delete=models.CASCADE, null=True)
    buyer = models.ForeignKey('User', related_name="buying", on_delete=models.SET_NULL, default=None, null=True)


class User(AbstractUser):
    watching = models.ManyToManyField(Item, blank=True, related_name="watchers")


class Bid(models.Model):
    owner = models.ForeignKey(User, blank=True, null=True, related_name="bids", on_delete=models.CASCADE)
    item = models.ForeignKey(Item, null=True, related_name="bids", on_delete=models.CASCADE)
    value = models.DecimalField(decimal_places=2, max_digits=8, null=True)

class Comment(models.Model):
    commenter = models.ForeignKey(User, blank=True, null=True, related_name="comments", on_delete=models.CASCADE)
    text = models.CharField(max_length=200, null=True)
    item = models.ForeignKey(Item, null=True, related_name="comments", on_delete=models.CASCADE)
