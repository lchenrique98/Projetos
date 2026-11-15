from datetime import datetime, time

from django.db.models import DecimalField, ExpressionWrapper, F, Sum
from django.utils import timezone
from django.views.generic import TemplateView

from clientes.models import Cliente
from floricultura.models import Encomenda, ItemVenda, Produto


class HomeView(TemplateView):
    """Página de entrada: resumo da floricultura e encomenda rápida."""

    template_name = 'core/home.html'

    def get_context_data(self, **kwargs):
        contexto = super().get_context_data(**kwargs)
        hoje = timezone.localdate()

        abertas = Encomenda.objects.filter(status__in=['pendente', 'em_preparo', 'pronta'])

        contexto.update({
            'clientes': Cliente.objects.filter(ativo=True).order_by('nome'),
            'clientes_ativos': Cliente.objects.filter(ativo=True).count(),
            'vendas_mes': self.faturamento_mes(hoje),
            'encomendas_abertas': abertas.count(),
            'produtos_repor': Produto.objects.filter(
                quantidade_estoque__lte=F('estoque_minimo')
            ).count(),
            'proxima_entrega': abertas.filter(
                data_entrega__gte=hoje
            ).select_related('cliente').order_by('data_entrega', 'hora_entrega').first(),
            'hoje': hoje,
        })
        return contexto

    @staticmethod
    def faturamento_mes(hoje):
        """Vendas do mês + encomendas entregues no mês (mesma regra do painel)."""
        fuso = timezone.get_current_timezone()
        inicio_mes = timezone.make_aware(datetime.combine(hoje.replace(day=1), time.min), fuso)

        valor_item = ExpressionWrapper(
            F('quantidade') * F('preco_unitario'),
            output_field=DecimalField(max_digits=12, decimal_places=2)
        )
        vendas = ItemVenda.objects.filter(
            venda__data_venda__gte=inicio_mes
        ).aggregate(total=Sum(valor_item))['total'] or 0

        encomendas = Encomenda.objects.filter(
            status='entregue', entregue_em__gte=inicio_mes
        ).aggregate(total=Sum('valor'))['total'] or 0

        return vendas + encomendas
