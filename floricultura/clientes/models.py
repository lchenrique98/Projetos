
from django.db import models


class Cliente(models.Model):
    nome = models.CharField(max_length=100)
    cpf = models.CharField(max_length=11, unique=True)
    telefone = models.CharField(max_length=20)
    email = models.EmailField()

    # Endereço
    rua = models.CharField(max_length=200)
    numero_casa = models.CharField(max_length=10)
    complemento = models.CharField(max_length=100, blank=True)
    bairro = models.CharField(max_length=100)
    cep = models.CharField(max_length=8)
    cidade = models.CharField(max_length=100)
    estado = models.CharField(max_length=2)

    # Controle de ativação do cliente
    ativo = models.BooleanField(default=True)

    def __str__(self):
        return self.nome

    class Meta:
        verbose_name = "Cliente"
        verbose_name_plural = "Clientes"
