from django.contrib import admin

from .models import User, Prescription, Request, Entry

admin.site.register(User)
admin.site.register(Prescription)
admin.site.register(Request)
admin.site.register(Entry)
