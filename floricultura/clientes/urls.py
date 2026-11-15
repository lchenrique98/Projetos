from django.urls import path
from . import views

urlpatterns = [
    path('', views.ClientesView.as_view(), name='clientes'),
    path('detalhes/<int:id>/', views.DetalhesClienteView.as_view(), name='detalhes_cliente'),
    path('editar/<int:id>/', views.EditarClienteView.as_view(), name='editar_cliente'),

    path('desativar/<int:id>/', views.DesativarClienteView.as_view(), name='desativar_cliente'),
    path('desativados/', views.ClientesDesativadosView.as_view(), name='clientes_desativados'),
    path('ativar/<int:id>/', views.AtivarClienteView.as_view(), name='ativar_cliente'),
]
