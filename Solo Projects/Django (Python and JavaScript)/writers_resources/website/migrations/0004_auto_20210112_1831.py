# Generated by Django 3.1.2 on 2021-01-12 23:31

from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('website', '0003_auto_20210112_1829'),
    ]

    operations = [
        migrations.AlterField(
            model_name='user',
            name='agent_titles',
            field=models.ForeignKey(blank=True, default=None, null=True, on_delete=django.db.models.deletion.CASCADE, related_name='agent', to='website.title'),
        ),
        migrations.AlterField(
            model_name='user',
            name='author_titles',
            field=models.ForeignKey(blank=True, default=None, null=True, on_delete=django.db.models.deletion.CASCADE, related_name='author', to='website.title'),
        ),
        migrations.AlterField(
            model_name='user',
            name='editor_titles',
            field=models.ForeignKey(blank=True, default=None, null=True, on_delete=django.db.models.deletion.CASCADE, related_name='editor', to='website.title'),
        ),
        migrations.AlterField(
            model_name='user',
            name='proofreader_titles',
            field=models.ForeignKey(blank=True, default=None, null=True, on_delete=django.db.models.deletion.CASCADE, related_name='proofreader', to='website.title'),
        ),
        migrations.AlterField(
            model_name='user',
            name='publisher_titles',
            field=models.ForeignKey(blank=True, default=None, null=True, on_delete=django.db.models.deletion.CASCADE, related_name='publisher', to='website.title'),
        ),
    ]
