from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('clientes', '0002_cliente_ativo'),
        ('floricultura', '0005_venda_forma_pagamento'),
    ]

    operations = [
        migrations.CreateModel(
            name='Encomenda',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('descricao', models.TextField()),
                ('valor', models.DecimalField(decimal_places=2, max_digits=10)),
                ('sinal', models.DecimalField(decimal_places=2, default=0, max_digits=10)),
                ('data_entrega', models.DateField()),
                ('hora_entrega', models.TimeField(blank=True, null=True)),
                ('tipo_entrega', models.CharField(choices=[('retirada', 'Retirada na loja'), ('entrega', 'Entrega no endereço')], default='retirada', max_length=20)),
                ('endereco_entrega', models.CharField(blank=True, max_length=255)),
                ('status', models.CharField(choices=[('pendente', 'Pendente'), ('em_preparo', 'Em preparo'), ('pronta', 'Pronta'), ('entregue', 'Entregue'), ('cancelada', 'Cancelada')], default='pendente', max_length=20)),
                ('observacoes', models.TextField(blank=True)),
                ('criado_em', models.DateTimeField(auto_now_add=True)),
                ('cliente', models.ForeignKey(on_delete=django.db.models.deletion.PROTECT, related_name='encomendas', to='clientes.cliente')),
            ],
            options={
                'ordering': ['data_entrega', 'hora_entrega'],
            },
        ),
    ]
