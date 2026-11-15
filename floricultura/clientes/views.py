import re
from decimal import Decimal

from django.shortcuts import render, redirect, get_object_or_404
from django.contrib import messages
from django.views import View

from .models import Cliente


class ClienteFormularioMixin:
    """Leitura e validação dos dados do formulário de cliente."""

    @staticmethod
    def somente_numeros(valor):
        """Remove pontos, traços, barras e espaços (ex.: 123.456.789-00 -> 12345678900)."""
        return re.sub(r'\D', '', valor or '')


class ClientesView(ClienteFormularioMixin, View):
    """Lista os clientes ativos e cadastra novos."""

    template_name = 'clientes/clientes.html'

    def get(self, request):
        lista_clientes = Cliente.objects.filter(
            ativo=True
        ).order_by('nome')

        return render(request, self.template_name, {
            'clientes': lista_clientes
        })

    def post(self, request):
        cpf = self.somente_numeros(request.POST.get('cpf'))

        if len(cpf) != 11:
            messages.error(request, 'O CPF precisa ter 11 números.')

        elif Cliente.objects.filter(cpf=cpf).exists():
            messages.error(request, 'Já existe um cliente cadastrado com esse CPF.')
        else:
            Cliente.objects.create(
                nome=request.POST.get('nome'),
                cpf=cpf,
                telefone=request.POST.get('telefone'),
                email=request.POST.get('email', ''),
                rua=request.POST.get('rua'),
                numero_casa=request.POST.get('numero_casa'),
                complemento=request.POST.get('complemento', ''),
                bairro=request.POST.get('bairro'),
                cep=self.somente_numeros(request.POST.get('cep')),
                cidade=request.POST.get('cidade'),
                estado=request.POST.get('estado', '').strip().upper(),
                ativo=True
            )

            messages.success(request, 'Cliente cadastrado com sucesso!')

        return redirect('clientes')


class DetalhesClienteView(View):

    template_name = 'clientes/detalhes_cliente.html'

    def get(self, request, id):
        cliente = get_object_or_404(Cliente, id=id)

        endereco = (
            f'{cliente.rua}, {cliente.numero_casa}, '
            f'{cliente.bairro}, {cliente.cidade}, '
            f'{cliente.estado}, CEP {cliente.cep}, Brasil'
        )

        if cliente.complemento:
            endereco += f', {cliente.complemento}'

        # Histórico de compras (Venda tem related_name='compras' no campo cliente)
        compras = list(
            cliente.compras.prefetch_related('itens__produto').order_by('-data_venda')
        )

        total_compras = len(compras)
        total_gasto = sum((compra.total for compra in compras), Decimal('0.00'))
        ticket_medio = (total_gasto / total_compras) if total_compras else Decimal('0.00')
        ultima_compra = compras[0] if compras else None

        return render(request, self.template_name, {
            'cliente': cliente,
            'endereco': endereco,
            'total_compras': total_compras,
            'total_gasto': total_gasto,
            'ticket_medio': ticket_medio,
            'ultima_compra': ultima_compra,
            'ultimas_compras': compras[:5],
        })


class EditarClienteView(ClienteFormularioMixin, View):

    template_name = 'clientes/editar_cliente.html'

    def get(self, request, id):
        cliente = get_object_or_404(Cliente, id=id)
        return self.mostrar_formulario(request, cliente)

    def post(self, request, id):
        cliente = get_object_or_404(Cliente, id=id)
        cpf = self.somente_numeros(request.POST.get('cpf'))

        if len(cpf) != 11:
            messages.error(request, 'O CPF precisa ter 11 números.')

        elif Cliente.objects.filter(cpf=cpf).exclude(id=cliente.id).exists():
            messages.error(request, 'Esse CPF já está cadastrado para outro cliente.')
        else:
            cliente.nome = request.POST.get('nome')
            cliente.cpf = cpf
            cliente.telefone = request.POST.get('telefone')
            cliente.email = request.POST.get('email', '')
            cliente.rua = request.POST.get('rua')
            cliente.numero_casa = request.POST.get('numero_casa')
            cliente.complemento = request.POST.get('complemento', '')
            cliente.bairro = request.POST.get('bairro')
            cliente.cep = self.somente_numeros(request.POST.get('cep'))
            cliente.cidade = request.POST.get('cidade')
            cliente.estado = request.POST.get('estado', '').strip().upper()

            cliente.save()

            messages.success(request, 'Cliente atualizado com sucesso!')
            return redirect('detalhes_cliente', id=cliente.id)

        return self.mostrar_formulario(request, cliente)

    def mostrar_formulario(self, request, cliente):
        return render(request, self.template_name, {
            'cliente': cliente
        })


class DesativarClienteView(View):
    """Só aceita POST (GET devolve 405, como o antigo @require_POST)."""

    def post(self, request, id):
        cliente = get_object_or_404(Cliente, id=id)
        cliente.ativo = False
        cliente.save()

        messages.success(request, 'Cliente desativado com sucesso!')
        return redirect('clientes')


class ClientesDesativadosView(View):

    template_name = 'clientes/clientes_desativados.html'

    def get(self, request):
        lista_clientes = Cliente.objects.filter(
            ativo=False
        ).order_by('nome')

        return render(request, self.template_name, {
            'clientes': lista_clientes
        })


class AtivarClienteView(View):
    """Só aceita POST (GET devolve 405, como o antigo @require_POST)."""

    def post(self, request, id):
        cliente = get_object_or_404(Cliente, id=id)
        cliente.ativo = True
        cliente.save()

        messages.success(request, 'Cliente reativado com sucesso!')
        return redirect('clientes_desativados')
