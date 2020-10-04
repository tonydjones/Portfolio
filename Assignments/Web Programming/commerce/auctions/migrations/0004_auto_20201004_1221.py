# Generated by Django 3.1.1 on 2020-10-04 16:21

from django.conf import settings
from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('auctions', '0003_item_active'),
    ]

    operations = [
        migrations.AddField(
            model_name='item',
            name='buyer',
            field=models.ForeignKey(default=None, null=True, on_delete=django.db.models.deletion.SET_NULL, related_name='buying', to=settings.AUTH_USER_MODEL),
        ),
        migrations.AddField(
            model_name='item',
            name='owner',
            field=models.ForeignKey(null=True, on_delete=django.db.models.deletion.CASCADE, related_name='selling', to=settings.AUTH_USER_MODEL),
        ),
    ]
