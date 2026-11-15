from django.urls import path
from . import views

urlpatterns = [
    path('', views.FornecedoresView.as_view(), name='fornecedores'),
]
