
from django.db import models
from decimal import Decimal
from clientes.models import Cliente
from django.core.validators import MinValueValidator


class Produto(models.Model):

    CATEGORIAS = [
        ('flores', 'Flores'),
        ('plantas', 'Plantas'),
        ('vasos', 'Vasos'),
        ('adubos', 'Adubos'),
        ('terras', 'Terras'),
    ]

    UNIDADES = [
        ('unidade', 'Unidade'),
        ('pacote', 'Pacote'),
        ('kg', 'Quilograma (kg)'),
        ('litro', 'Litro'),
    ]

    nome = models.CharField(max_length=100)
    categoria = models.CharField(max_length=20, choices=CATEGORIAS)
    descricao = models.TextField(blank=True)
    preco_custo = models.DecimalField(max_digits=10, decimal_places=2)
    preco_venda = models.DecimalField(max_digits=10, decimal_places=2)
    quantidade_estoque = models.PositiveIntegerField(default=0)
    estoque_minimo = models.PositiveIntegerField(default=5)

    unidade_medida = models.CharField(
        max_length=20,
        choices=UNIDADES,
        default='unidade'
    )

    foto = models.ImageField(
        upload_to='produtos/',
        blank=True,
        null=True
    )

    fornecedor = models.ForeignKey(
        'fornecedores.Fornecedor',
        on_delete=models.PROTECT,
        related_name='produtos',
        null=True,
        blank=True
    )

    uploaded_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return self.nome


class Venda(models.Model):

    FORMAS_PAGAMENTO = [
        ('dinheiro', 'Dinheiro'),
        ('pix', 'Pix'),
        ('cartao_credito', 'Cartão de crédito'),
        ('cartao_debito', 'Cartão de débito'),
        ('outro', 'Outro'),
    ]

    cliente = models.ForeignKey(
        Cliente,
        on_delete=models.PROTECT,
        related_name='compras'
    )

    data_venda = models.DateTimeField(auto_now_add=True)

    forma_pagamento = models.CharField(
        max_length=20,
        choices=FORMAS_PAGAMENTO,
        default='dinheiro'
    )

    observacoes = models.TextField(blank=True)

    def __str__(self):
        return f"Venda #{self.id} - {self.cliente.nome}"

    @property
    def total(self):
        return sum(
            (item.subtotal for item in self.itens.all()),
            Decimal('0.00')
        )


class ItemVenda(models.Model):

    venda = models.ForeignKey(
        Venda,
        on_delete=models.CASCADE,
        related_name='itens'
    )

    produto = models.ForeignKey(
        Produto,
        on_delete=models.PROTECT
    )

    quantidade = models.PositiveIntegerField(
        validators=[MinValueValidator(1)]
    )

    preco_unitario = models.DecimalField(
        max_digits=10,
        decimal_places=2
    )

    @property
    def subtotal(self):
        return self.quantidade * self.preco_unitario

    def __str__(self):
        return f"{self.produto.nome} - {self.quantidade}"


class Encomenda(models.Model):

    STATUS = [
        ('pendente', 'Pendente'),
        ('em_preparo', 'Em preparo'),
        ('pronta', 'Pronta'),
        ('entregue', 'Entregue'),
        ('cancelada', 'Cancelada'),
    ]

    # ordem do fluxo normal (cancelada fica fora)
    FLUXO = ['pendente', 'em_preparo', 'pronta', 'entregue']

    TIPOS_ENTREGA = [
        ('retirada', 'Retirada na loja'),
        ('entrega', 'Entrega no endereço'),
    ]

    cliente = models.ForeignKey(
        Cliente,
        on_delete=models.PROTECT,
        related_name='encomendas'
    )
    descricao = models.TextField()
    valor = models.DecimalField(max_digits=10, decimal_places=2)
    sinal = models.DecimalField(max_digits=10, decimal_places=2, default=0)

    data_entrega = models.DateField()
    hora_entrega = models.TimeField(null=True, blank=True)
    tipo_entrega = models.CharField(
        max_length=20,
        choices=TIPOS_ENTREGA,
        default='retirada'
    )
    endereco_entrega = models.CharField(max_length=255, blank=True)

    status = models.CharField(max_length=20, choices=STATUS, default='pendente')
    observacoes = models.TextField(blank=True)
    criado_em = models.DateTimeField(auto_now_add=True)

    # preenchido quando a encomenda é marcada como entregue (entra no faturamento do painel)
    entregue_em = models.DateTimeField(null=True, blank=True)

    class Meta:
        ordering = ['data_entrega', 'hora_entrega']

    def __str__(self):
        return f"Encomenda #{self.id} - {self.cliente.nome}"

    @property
    def saldo(self):
        """Quanto falta o cliente pagar."""
        return max(self.valor - self.sinal, Decimal('0.00'))

    @property
    def finalizada(self):
        return self.status in ('entregue', 'cancelada')

    @property
    def atrasada(self):
        from django.utils import timezone
        return not self.finalizada and self.data_entrega < timezone.localdate()

    @property
    def proximo_status(self):
        """Próximo passo do fluxo (ou None se já terminou)."""
        if self.status not in self.FLUXO:
            return None
        i = self.FLUXO.index(self.status)
        return self.FLUXO[i + 1] if i + 1 < len(self.FLUXO) else None

    @property
    def proximo_status_display(self):
        return dict(self.STATUS).get(self.proximo_status, '')
