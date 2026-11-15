from datetime import datetime, time

from django.db import migrations, models
from django.utils import timezone


class PreencherEntregues:
    """Encomendas que já estavam entregues: usa a data de entrega como data de entrega real."""

    @staticmethod
    def executar(apps, schema_editor):
        Encomenda = apps.get_model('floricultura', 'Encomenda')
        fuso = timezone.get_current_timezone()
        for e in Encomenda.objects.filter(status='entregue', entregue_em__isnull=True):
            e.entregue_em = timezone.make_aware(
                datetime.combine(e.data_entrega, e.hora_entrega or time(12, 0)), fuso
            )
            e.save(update_fields=['entregue_em'])


class Migration(migrations.Migration):

    dependencies = [
        ('floricultura', '0006_encomenda'),
    ]

    operations = [
        migrations.AddField(
            model_name='encomenda',
            name='entregue_em',
            field=models.DateTimeField(blank=True, null=True),
        ),
        migrations.RunPython(PreencherEntregues.executar, migrations.RunPython.noop),
    ]
