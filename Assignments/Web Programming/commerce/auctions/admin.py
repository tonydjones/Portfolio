from django.contrib import admin

from .models import Comment, User, Item, Bid

admin.site.register(Item)
admin.site.register(User)
admin.site.register(Bid)
admin.site.register(Comment)
