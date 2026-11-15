from django.db import models

# Create your models here.

class Fornecedor(models.Model):
    nome = models.CharField(max_length=100)
    endereco = models.TextField()
    telefone = models.CharField(max_length=20)
    email = models.EmailField()
    cnpj = models.CharField(max_length=14, unique=True)
    
    def __str__(self):
        return self.nome

    class Meta:
        verbose_name = "Fornecedor"
        verbose_name_plural = "Fornecedores"
    

    

    