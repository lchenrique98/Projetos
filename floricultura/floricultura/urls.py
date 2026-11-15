from django.urls import path
from . import views

urlpatterns = [
    path('dashboard/', views.PainelView.as_view(), name='dashboard_floricultura'),

    # Produtos
    path('produtos/', views.ProdutosView.as_view(), name='produtos'),
    path('baixar_estoque/<int:id>/', views.BaixarEstoqueView.as_view(), name='baixar_estoque'),
    path('adicionar_estoque/<int:id>/', views.AdicionarEstoqueView.as_view(), name='adicionar_estoque'),
    path('detalhes_produto/<int:id>/', views.DetalhesProdutoView.as_view(), name='detalhes_produto'),
    path('produtos/excluir/<int:id>/', views.ExcluirProdutoView.as_view(), name='excluir_produto'),

    # Vendas
    path('vendas/', views.VendasView.as_view(), name='vendas'),
    path('vendas/nova/', views.NovaVendaView.as_view(), name='nova_venda'),
    path('vendas/historico/<int:cliente_id>/', views.HistoricoClienteView.as_view(), name='historico_cliente'),

    # Encomendas
    path('encomendas/', views.EncomendasView.as_view(), name='encomendas'),
    path('encomendas/nova/', views.NovaEncomendaView.as_view(), name='nova_encomenda'),
    path('encomendas/<int:id>/', views.DetalhesEncomendaView.as_view(), name='detalhes_encomenda'),
    path('encomendas/<int:id>/editar/', views.EditarEncomendaView.as_view(), name='editar_encomenda'),
    path('encomendas/<int:id>/status/', views.MudarStatusEncomendaView.as_view(), name='mudar_status_encomenda'),
]
