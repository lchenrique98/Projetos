# Copie este arquivo para "local_settings.py" (na mesma pasta) e preencha.
# O local_settings.py está no .gitignore e não deve ser enviado para o GitHub.

SECRET_KEY = 'troque-por-uma-chave-secreta'

DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.mysql',
        'NAME': 'GestaoFloricultura',
        'USER': 'root',
        'PASSWORD': 'sua-senha-do-mysql',
        'HOST': 'localhost',
        'PORT': '3306',
        'OPTIONS': {
            'charset': 'utf8mb4',
        },
    }
}
