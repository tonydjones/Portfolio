from django.contrib import admin

from .models import User, Title, Message

admin.site.register(User)
admin.site.register(Title)
admin.site.register(Message)
