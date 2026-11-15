from django.shortcuts import redirect, render
from django.contrib import messages
from django.views import View

from .models import Fornecedor


class FornecedoresView(View):
    """Lista os fornecedores e cadastra novos."""

    template_name = 'fornecedores/fornecedores.html'

    def get(self, request):
        lista_fornecedores = Fornecedor.objects.all().order_by('nome')

        return render(request, self.template_name, {
            'empresas': lista_fornecedores
        })

    def post(self, request):
        cnpj = request.POST.get('cnpj')

        if Fornecedor.objects.filter(cnpj=cnpj).exists():
            messages.error(request, 'Já existe um fornecedor cadastrado com esse CNPJ.')

        else:
            Fornecedor.objects.create(
                nome=request.POST.get('nome'),
                endereco=request.POST.get('endereco'),
                telefone=request.POST.get('telefone'),
                email=request.POST.get('email'),
                cnpj=cnpj
            )

            messages.success(request, 'Fornecedor cadastrado com sucesso!')

        return redirect('fornecedores')
