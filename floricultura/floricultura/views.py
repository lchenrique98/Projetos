from decimal import Decimal, InvalidOperation
from datetime import datetime, time, timedelta

from django.shortcuts import render, redirect, get_object_or_404
from django.contrib import messages
from django.db import transaction
from django.db.models import Q, F, Sum, DecimalField, ExpressionWrapper, ProtectedError
from django.utils import timezone
from django.views import View

from .models import Produto, Venda, ItemVenda, Encomenda
from clientes.models import Cliente
from fornecedores.models import Fornecedor


class ErroValidacao(Exception):
    """Erro de validação com mensagem amigável para mostrar ao usuário."""
    pass


# =========================================================
# DASHBOARD
# =========================================================

class PainelView(View):

    template_name = 'floricultura/painel.html'

    def get(self, request):
        agora = timezone.localtime()
        hoje = agora.date()

        # Início do dia e do mês (com fuso), sem usar __date (evita problema de fuso no MySQL)
        fuso = timezone.get_current_timezone()
        inicio_dia = timezone.make_aware(datetime.combine(hoje, time.min), fuso)
        inicio_mes = timezone.make_aware(datetime.combine(hoje.replace(day=1), time.min), fuso)

        valor_item = ExpressionWrapper(
            F('quantidade') * F('preco_unitario'),
            output_field=DecimalField(max_digits=12, decimal_places=2)
        )

        total_hoje = ItemVenda.objects.filter(
            venda__data_venda__gte=inicio_dia
        ).aggregate(total=Sum(valor_item))['total'] or 0

        total_mes = ItemVenda.objects.filter(
            venda__data_venda__gte=inicio_mes
        ).aggregate(total=Sum(valor_item))['total'] or 0

        vendas_hoje = Venda.objects.filter(data_venda__gte=inicio_dia).count()
        vendas_mes = Venda.objects.filter(data_venda__gte=inicio_mes).count()

        # Encomendas entregues também entram no faturamento (pelo dia em que foram entregues)
        entregues_hoje = Encomenda.objects.filter(status='entregue', entregue_em__gte=inicio_dia)
        entregues_mes = Encomenda.objects.filter(status='entregue', entregue_em__gte=inicio_mes)

        encomendas_hoje_valor = entregues_hoje.aggregate(total=Sum('valor'))['total'] or 0
        encomendas_mes_valor = entregues_mes.aggregate(total=Sum('valor'))['total'] or 0
        encomendas_hoje_qtd = entregues_hoje.count()
        encomendas_mes_qtd = entregues_mes.count()

        total_hoje += encomendas_hoje_valor
        total_mes += encomendas_mes_valor

        pedidos_mes = vendas_mes + encomendas_mes_qtd
        ticket_medio = (total_mes / pedidos_mes) if pedidos_mes else 0

        clientes_ativos = Cliente.objects.filter(ativo=True).count()

        produtos_baixo = Produto.objects.filter(
            quantidade_estoque__lte=F('estoque_minimo')
        ).order_by('quantidade_estoque')

        ultimas_vendas = Venda.objects.select_related(
            'cliente'
        ).prefetch_related(
            'itens'
        ).order_by('-data_venda')[:6]

        return render(request, self.template_name, {
            'saudacao': self.saudacao(agora),
            'hoje': agora,
            'total_hoje': total_hoje,
            'vendas_hoje': vendas_hoje,
            'total_mes': total_mes,
            'vendas_mes': vendas_mes,
            'encomendas_hoje_qtd': encomendas_hoje_qtd,
            'encomendas_mes_qtd': encomendas_mes_qtd,
            'ticket_medio': ticket_medio,
            'clientes_ativos': clientes_ativos,
            'qtd_estoque_baixo': produtos_baixo.count(),
            'produtos_baixo': produtos_baixo[:6],
            'ultimas_vendas': ultimas_vendas,
            'mais_vendidos': self.mais_vendidos(inicio_mes),
        })

    def mais_vendidos(self, inicio_mes):
        """Top 5 produtos mais vendidos no mês (com % para a barra)."""
        mais_vendidos = list(
            ItemVenda.objects.filter(
                venda__data_venda__gte=inicio_mes
            ).values(
                'produto__nome'
            ).annotate(
                qtd=Sum('quantidade')
            ).order_by('-qtd')[:5]
        )
        maior = mais_vendidos[0]['qtd'] if mais_vendidos else 0
        for item in mais_vendidos:
            item['pct'] = round(item['qtd'] * 100 / maior) if maior else 0
        return mais_vendidos

    @staticmethod
    def saudacao(agora):
        if agora.hour < 12:
            return 'Bom dia'
        if agora.hour < 18:
            return 'Boa tarde'
        return 'Boa noite'


# =========================================================
# PRODUTOS
# =========================================================

class ProdutosView(View):
    """Lista (com pesquisa e filtro por categoria) e cadastra produtos."""

    template_name = 'floricultura/produtos.html'

    def get(self, request):
        pesquisa = request.GET.get('pesquisa', '').strip()
        categoria_selecionada = request.GET.get('categoria', '')

        lista_produtos = Produto.objects.select_related('fornecedor').order_by('nome')

        if pesquisa:
            lista_produtos = lista_produtos.filter(
                Q(nome__icontains=pesquisa) |
                Q(descricao__icontains=pesquisa)
            )

        if categoria_selecionada:
            lista_produtos = lista_produtos.filter(categoria=categoria_selecionada)

        fornecedores = Fornecedor.objects.all().order_by('nome')

        return render(request, self.template_name, {
            'produtos': lista_produtos,
            'pesquisa': pesquisa,
            'categoria_selecionada': categoria_selecionada,
            'fornecedores': fornecedores,
        })

    def post(self, request):
        try:
            nome = request.POST.get('nome', '').strip()
            categoria = request.POST.get('categoria', '')
            unidade = request.POST.get('unidade', 'unidade')

            if not nome:
                raise ErroValidacao('Informe o nome do produto.')

            if categoria not in dict(Produto.CATEGORIAS):
                raise ErroValidacao('Selecione uma categoria válida.')

            if unidade not in dict(Produto.UNIDADES):
                raise ErroValidacao('Selecione uma unidade de medida válida.')

            preco_custo = Decimal(request.POST.get('preco_custo', '0').replace(',', '.'))
            preco_venda = Decimal(request.POST.get('preco_venda', '0').replace(',', '.'))
            quantidade = int(request.POST.get('quantidade', 0))
            estoque_minimo = int(request.POST.get('estoque_minimo', 5))

            if preco_custo < 0 or preco_venda < 0:
                raise ErroValidacao('Os preços não podem ser negativos.')

            if quantidade < 0 or estoque_minimo < 0:
                raise ErroValidacao('Quantidade e estoque mínimo não podem ser negativos.')

            fornecedor_id = request.POST.get('fornecedor') or None

            if fornecedor_id and not Fornecedor.objects.filter(id=fornecedor_id).exists():
                raise ErroValidacao('Fornecedor não encontrado.')

            Produto.objects.create(
                nome=nome,
                categoria=categoria,
                descricao=request.POST.get('descricao', '').strip(),
                preco_custo=preco_custo,
                preco_venda=preco_venda,
                quantidade_estoque=quantidade,
                estoque_minimo=estoque_minimo,
                unidade_medida=unidade,
                foto=request.FILES.get('foto'),
                fornecedor_id=fornecedor_id,
            )

            messages.success(request, f'Produto "{nome}" cadastrado com sucesso!')

            if preco_venda < preco_custo:
                messages.warning(
                    request,
                    'Atenção: o preço de venda está menor que o preço de custo.'
                )

        except ErroValidacao as erro:
            messages.error(request, str(erro))

        except (ValueError, TypeError, InvalidOperation):
            messages.error(request, 'Confira os preços e quantidades informados.')

        return redirect('produtos')


class MovimentoEstoqueView(View):
    """Base para entrada e baixa de estoque. Só aceita POST (GET devolve 405)."""

    def post(self, request, id):
        quantidade = self.ler_quantidade(request)

        if quantidade is None:
            messages.error(request, 'Informe uma quantidade válida.')
            return redirect('produtos')

        return self.movimentar(request, id, quantidade)

    def movimentar(self, request, id, quantidade):
        raise NotImplementedError

    @staticmethod
    def ler_quantidade(request):
        """Lê a quantidade enviada pelo formulário. Retorna None se for inválida."""
        try:
            quantidade = int(request.POST.get('quantidade', 0))
        except (ValueError, TypeError):
            return None
        return quantidade if quantidade > 0 else None


class BaixarEstoqueView(MovimentoEstoqueView):

    def movimentar(self, request, id, quantidade):
        with transaction.atomic():
            # trava a linha para duas operações ao mesmo tempo não bagunçarem o estoque
            produto = get_object_or_404(Produto.objects.select_for_update(), id=id)

            if quantidade > produto.quantidade_estoque:
                messages.error(
                    request,
                    f'Estoque insuficiente para {produto.nome}. '
                    f'Disponível: {produto.quantidade_estoque}.'
                )
                return redirect('produtos')

            produto.quantidade_estoque -= quantidade
            produto.save(update_fields=['quantidade_estoque'])

        messages.success(
            request,
            f'Baixa de {quantidade} unidade(s) de {produto.nome}. '
            f'Estoque atual: {produto.quantidade_estoque}.'
        )
        return redirect('produtos')


class AdicionarEstoqueView(MovimentoEstoqueView):

    def movimentar(self, request, id, quantidade):
        with transaction.atomic():
            produto = get_object_or_404(Produto.objects.select_for_update(), id=id)
            produto.quantidade_estoque = F('quantidade_estoque') + quantidade
            produto.save(update_fields=['quantidade_estoque'])
            produto.refresh_from_db(fields=['quantidade_estoque'])

        messages.success(
            request,
            f'Entrada de {quantidade} unidade(s) de {produto.nome}. '
            f'Estoque atual: {produto.quantidade_estoque}.'
        )
        return redirect('produtos')


class ExcluirProdutoView(View):
    """Só aceita POST (GET devolve 405)."""

    def post(self, request, id):
        produto = get_object_or_404(Produto, id=id)
        nome = produto.nome

        try:
            produto.delete()
            messages.success(request, f'Produto "{nome}" excluído com sucesso!')

        except ProtectedError:
            # o produto já aparece em vendas (ItemVenda usa PROTECT)
            messages.error(
                request,
                f'Não é possível excluir "{nome}" porque ele já foi vendido. '
                f'O histórico de vendas precisa dele.'
            )

        return redirect('produtos')


class DetalhesProdutoView(View):

    template_name = 'floricultura/detalhes_produto.html'

    def get(self, request, id):
        produto = get_object_or_404(
            Produto.objects.select_related('fornecedor'),
            id=id
        )

        return render(request, self.template_name, {
            'produto': produto
        })


# =========================================================
# VENDAS
# =========================================================

class VendasView(View):
    """Lista os clientes ativos para consultar as compras."""

    template_name = 'floricultura/vendas.html'

    def get(self, request):
        clientes = Cliente.objects.filter(ativo=True).order_by('nome')

        return render(request, self.template_name, {
            'clientes': clientes
        })


class NovaVendaView(View):

    template_name = 'floricultura/nova_venda.html'

    def get(self, request):
        return self.mostrar_formulario(request)

    def post(self, request):
        cliente_id = request.POST.get('cliente')
        produtos_ids = request.POST.getlist('produto')
        quantidades = request.POST.getlist('quantidade')
        forma_pagamento = request.POST.get('forma_pagamento')
        observacoes = request.POST.get('observacoes', '').strip()

        try:
            if not cliente_id:
                raise ErroValidacao('Selecione um cliente.')

            if not produtos_ids or not any(produtos_ids):
                raise ErroValidacao('Adicione pelo menos um produto.')

            if len(produtos_ids) != len(quantidades):
                raise ErroValidacao('Confira os produtos e quantidades.')

            if forma_pagamento not in dict(Venda.FORMAS_PAGAMENTO):
                raise ErroValidacao('Selecione uma forma de pagamento válida.')

            itens = self.agrupar_itens(produtos_ids, quantidades)
            venda = self.registrar_venda(cliente_id, forma_pagamento, observacoes, itens)

            total = f'{venda.total:.2f}'.replace('.', ',')
            messages.success(
                request,
                f'Venda #{venda.id} registrada com sucesso! Total: R$ {total}'
            )
            return redirect('vendas')

        except ErroValidacao as erro:
            # transaction.atomic desfaz tudo; mostra o motivo exato
            messages.error(request, str(erro))

        except (ValueError, TypeError):
            messages.error(request, 'Confira os dados informados na venda.')

        return self.mostrar_formulario(request)

    def mostrar_formulario(self, request):
        clientes = Cliente.objects.filter(ativo=True).order_by('nome')
        produtos_disponiveis = Produto.objects.filter(
            quantidade_estoque__gt=0
        ).order_by('nome')

        return render(request, self.template_name, {
            'clientes': clientes,
            'produtos': produtos_disponiveis
        })

    @staticmethod
    def agrupar_itens(produtos_ids, quantidades):
        """Junta linhas repetidas do mesmo produto (ex.: Rosa 2 + Rosa 3 = Rosa 5)."""
        itens = {}
        for produto_id, quantidade_texto in zip(produtos_ids, quantidades):
            if not produto_id:
                raise ErroValidacao('Selecione o produto em todas as linhas.')

            quantidade = int(quantidade_texto)

            if quantidade <= 0:
                raise ErroValidacao('A quantidade deve ser maior que zero.')

            itens[int(produto_id)] = itens.get(int(produto_id), 0) + quantidade
        return itens

    @staticmethod
    @transaction.atomic
    def registrar_venda(cliente_id, forma_pagamento, observacoes, itens):
        try:
            cliente = Cliente.objects.get(id=cliente_id, ativo=True)
        except Cliente.DoesNotExist:
            raise ErroValidacao('Cliente não encontrado ou está desativado.')

        venda = Venda.objects.create(
            cliente=cliente,
            forma_pagamento=forma_pagamento,
            observacoes=observacoes
        )

        for produto_id, quantidade in itens.items():

            try:
                produto = Produto.objects.select_for_update().get(id=produto_id)
            except Produto.DoesNotExist:
                raise ErroValidacao('Um dos produtos selecionados não foi encontrado.')

            if quantidade > produto.quantidade_estoque:
                raise ErroValidacao(
                    f'Estoque insuficiente para {produto.nome}. '
                    f'Disponível: {produto.quantidade_estoque}.'
                )

            ItemVenda.objects.create(
                venda=venda,
                produto=produto,
                quantidade=quantidade,
                preco_unitario=produto.preco_venda
            )

            produto.quantidade_estoque -= quantidade
            produto.save(update_fields=['quantidade_estoque'])

        return venda


class HistoricoClienteView(View):

    template_name = 'floricultura/historico_cliente.html'

    def get(self, request, cliente_id):
        cliente = get_object_or_404(Cliente, id=cliente_id)

        compras = Venda.objects.filter(
            cliente=cliente
        ).prefetch_related(
            'itens__produto'
        ).order_by('-data_venda')

        total_gasto = sum((compra.total for compra in compras), Decimal('0.00'))

        return render(request, self.template_name, {
            'cliente': cliente,
            'compras': compras,
            'total_gasto': total_gasto,
        })


# =========================================================
# ENCOMENDAS
# =========================================================

class EncomendasView(View):

    template_name = 'floricultura/encomendas.html'

    def get(self, request):
        filtro = request.GET.get('status', 'ativas')
        pesquisa = request.GET.get('pesquisa', '').strip()

        lista = Encomenda.objects.select_related('cliente')

        if filtro == 'ativas':
            lista = lista.filter(status__in=['pendente', 'em_preparo', 'pronta'])
        elif filtro in dict(Encomenda.STATUS):
            lista = lista.filter(status=filtro)
        else:
            filtro = 'todas'

        if pesquisa:
            lista = lista.filter(
                Q(cliente__nome__icontains=pesquisa) |
                Q(descricao__icontains=pesquisa)
            )

        # finalizadas mostram as mais recentes primeiro
        if filtro in ('entregue', 'cancelada', 'todas'):
            lista = lista.order_by('-data_entrega', '-hora_entrega')

        hoje = timezone.localdate()
        abertas = Encomenda.objects.filter(status__in=['pendente', 'em_preparo', 'pronta'])

        return render(request, self.template_name, {
            'encomendas': lista,
            'filtro': filtro,
            'pesquisa': pesquisa,
            'abas': self.abas(),
            'hoje': hoje,
            'amanha': hoje + timedelta(days=1),
            'qtd_hoje': abertas.filter(data_entrega=hoje).count(),
            'qtd_atrasadas': abertas.filter(data_entrega__lt=hoje).count(),
            'qtd_semana': abertas.filter(data_entrega__gte=hoje, data_entrega__lte=hoje + timedelta(days=7)).count(),
        })

    @staticmethod
    def abas():
        contagem = {codigo: 0 for codigo, _ in Encomenda.STATUS}
        for codigo in contagem:
            contagem[codigo] = Encomenda.objects.filter(status=codigo).count()

        return [
            ('ativas', 'Em aberto', contagem['pendente'] + contagem['em_preparo'] + contagem['pronta']),
            ('pendente', 'Pendentes', contagem['pendente']),
            ('em_preparo', 'Em preparo', contagem['em_preparo']),
            ('pronta', 'Prontas', contagem['pronta']),
            ('entregue', 'Entregues', contagem['entregue']),
            ('cancelada', 'Canceladas', contagem['cancelada']),
            ('todas', 'Todas', sum(contagem.values())),
        ]


class EncomendaFormView(View):
    """Base da tela usada tanto para criar quanto para editar encomenda."""

    template_name = 'floricultura/encomenda_form.html'

    def get_encomenda(self, id=None):
        raise NotImplementedError

    def get(self, request, id=None):
        return self.mostrar_formulario(request, self.get_encomenda(id))

    def post(self, request, id=None):
        encomenda = self.get_encomenda(id)
        nova = encomenda.pk is None

        try:
            self.salvar_encomenda(request, encomenda)
            messages.success(
                request,
                'Encomenda cadastrada com sucesso!' if nova else 'Encomenda atualizada com sucesso!'
            )
            return redirect('detalhes_encomenda', id=encomenda.id)
        except ErroValidacao as erro:
            messages.error(request, str(erro))

        return self.mostrar_formulario(request, encomenda)

    def mostrar_formulario(self, request, encomenda):
        nova = encomenda.pk is None

        # valores dos campos: da encomenda (editar) ou do que foi digitado (se deu erro)
        valores = {
            'cliente': str(encomenda.cliente_id or ''),
            'descricao': encomenda.descricao or '',
            'valor': f'{encomenda.valor:.2f}' if encomenda.valor is not None else '',
            'sinal': f'{encomenda.sinal:.2f}' if encomenda.pk else '',
            'data_entrega': encomenda.data_entrega.isoformat() if encomenda.data_entrega else '',
            'hora_entrega': encomenda.hora_entrega.strftime('%H:%M') if encomenda.hora_entrega else '',
            'tipo_entrega': encomenda.tipo_entrega or 'retirada',
            'endereco_entrega': encomenda.endereco_entrega or '',
            'observacoes': encomenda.observacoes or '',
        }
        if request.method == 'POST':
            valores.update(request.POST.dict())
        elif nova:
            valores.update(self.valores_da_encomenda_rapida(request))

        return render(request, self.template_name, {
            'encomenda': encomenda,
            'nova': nova,
            'valores': valores,
            'clientes': Cliente.objects.filter(ativo=True).order_by('nome'),
            'tipos_entrega': Encomenda.TIPOS_ENTREGA,
            'hoje': timezone.localdate(),
        })

    @staticmethod
    def valores_da_encomenda_rapida(request):
        """Campos que chegam pela 'Encomenda rápida' da página inicial (?cliente=...&valor=...)."""
        valores = {}
        for chave in ('cliente', 'descricao', 'data_entrega', 'valor'):
            texto = request.GET.get(chave, '').strip()
            if texto:
                valores[chave] = texto
        if 'valor' in valores:
            valores['valor'] = valores['valor'].replace('R$', '').replace('.', '').replace(',', '.').strip()
        return valores

    @staticmethod
    def salvar_encomenda(request, encomenda):
        """Valida o POST e preenche/salva a encomenda. Lança ErroValidacao com a mensagem."""

        try:
            cliente = Cliente.objects.get(id=request.POST.get('cliente'), ativo=True)
        except (Cliente.DoesNotExist, ValueError, TypeError):
            raise ErroValidacao('Selecione um cliente ativo.')

        descricao = request.POST.get('descricao', '').strip()
        if not descricao:
            raise ErroValidacao('Descreva o que o cliente encomendou.')

        try:
            valor = Decimal(request.POST.get('valor', '').replace(',', '.'))
            sinal = Decimal((request.POST.get('sinal') or '0').replace(',', '.'))
        except InvalidOperation:
            raise ErroValidacao('Confira o valor e o sinal.')

        if valor <= 0:
            raise ErroValidacao('O valor da encomenda precisa ser maior que zero.')
        if sinal < 0 or sinal > valor:
            raise ErroValidacao('O sinal não pode ser negativo nem maior que o valor.')

        try:
            data_entrega = datetime.strptime(request.POST.get('data_entrega', ''), '%Y-%m-%d').date()
        except ValueError:
            raise ErroValidacao('Informe a data de entrega.')

        hora_texto = request.POST.get('hora_entrega', '').strip()
        try:
            hora_entrega = datetime.strptime(hora_texto, '%H:%M').time() if hora_texto else None
        except ValueError:
            raise ErroValidacao('Hora de entrega inválida.')

        tipo_entrega = request.POST.get('tipo_entrega', 'retirada')
        if tipo_entrega not in dict(Encomenda.TIPOS_ENTREGA):
            raise ErroValidacao('Selecione retirada ou entrega.')

        endereco = request.POST.get('endereco_entrega', '').strip()
        if tipo_entrega == 'entrega' and not endereco:
            raise ErroValidacao('Informe o endereço de entrega.')

        # só cobra data futura na criação (editar uma encomenda antiga não pode travar)
        if encomenda.pk is None and data_entrega < timezone.localdate():
            raise ErroValidacao('A data de entrega não pode ser no passado.')

        encomenda.cliente = cliente
        encomenda.descricao = descricao
        encomenda.valor = valor
        encomenda.sinal = sinal
        encomenda.data_entrega = data_entrega
        encomenda.hora_entrega = hora_entrega
        encomenda.tipo_entrega = tipo_entrega
        encomenda.endereco_entrega = endereco if tipo_entrega == 'entrega' else ''
        encomenda.observacoes = request.POST.get('observacoes', '').strip()
        encomenda.save()
        return encomenda


class NovaEncomendaView(EncomendaFormView):

    def get_encomenda(self, id=None):
        return Encomenda()


class EditarEncomendaView(EncomendaFormView):

    def dispatch(self, request, *args, **kwargs):
        encomenda = get_object_or_404(Encomenda, id=kwargs['id'])

        if encomenda.finalizada:
            messages.error(request, 'Encomendas entregues ou canceladas não podem ser editadas.')
            return redirect('detalhes_encomenda', id=encomenda.id)

        self.encomenda = encomenda
        return super().dispatch(request, *args, **kwargs)

    def get_encomenda(self, id=None):
        return self.encomenda


class DetalhesEncomendaView(View):

    template_name = 'floricultura/detalhes_encomenda.html'

    def get(self, request, id):
        encomenda = get_object_or_404(Encomenda.objects.select_related('cliente'), id=id)

        return render(request, self.template_name, {
            'encomenda': encomenda,
            'passos': self.passos(encomenda),
        })

    @staticmethod
    def passos(encomenda):
        passos = []
        atingido = encomenda.status in Encomenda.FLUXO
        indice = Encomenda.FLUXO.index(encomenda.status) if atingido else -1
        for i, codigo in enumerate(Encomenda.FLUXO):
            passos.append({
                'nome': dict(Encomenda.STATUS)[codigo],
                'feito': i <= indice,
                'atual': i == indice,
            })
        return passos


class MudarStatusEncomendaView(View):
    """Só aceita POST (GET devolve 405)."""

    def post(self, request, id):
        encomenda = get_object_or_404(Encomenda, id=id)
        novo = request.POST.get('status')

        if encomenda.finalizada:
            messages.error(request, 'Essa encomenda já foi finalizada.')
        elif novo == 'cancelada' or novo == encomenda.proximo_status:
            encomenda.status = novo
            campos = ['status']

            # marca a hora da entrega: é a partir dela que o valor entra no faturamento
            if novo == 'entregue':
                encomenda.entregue_em = timezone.now()
                campos.append('entregue_em')

            encomenda.save(update_fields=campos)
            messages.success(
                request,
                f'Encomenda #{encomenda.id} marcada como "{encomenda.get_status_display()}".'
            )
        else:
            messages.error(request, 'Mudança de status inválida.')

        destino = request.POST.get('voltar')
        if destino == 'lista':
            return redirect('encomendas')
        return redirect('detalhes_encomenda', id=encomenda.id)
