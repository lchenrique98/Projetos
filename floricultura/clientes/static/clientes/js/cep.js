(function () {
    const campoCep = document.getElementById('cep');
    if (!campoCep) return;
 
    const campos = {
        rua: document.getElementById('rua'),
        bairro: document.getElementById('bairro'),
        cidade: document.getElementById('cidade'),
        estado: document.getElementById('estado'),
        numero: document.getElementById('numero_casa'),
    };
 
    // mensagem embaixo do campo CEP
    const aviso = document.createElement('small');
    aviso.className = 'cep-status';
    campoCep.insertAdjacentElement('afterend', aviso);
 
    function mostrarAviso(texto, tipo) {
        aviso.textContent = texto;
        aviso.className = 'cep-status ' + (tipo || '');
    }
 
    // máscara 00000-000
    function formatarCep(valor) {
        return valor.replace(/\D/g, '').slice(0, 8).replace(/^(\d{5})(\d)/, '$1-$2');
    }
 
    campoCep.value = formatarCep(campoCep.value);
    campoCep.setAttribute('maxlength', '9');
    campoCep.setAttribute('inputmode', 'numeric');
 
    let ultimoCepBuscado = '';
 
    async function buscarCep() {
        const cep = campoCep.value.replace(/\D/g, '');
 
        if (cep.length !== 8 || cep === ultimoCepBuscado) return;
        ultimoCepBuscado = cep;
 
        mostrarAviso('Buscando endereço...', 'carregando');
        campoCep.classList.add('buscando');
 
        try {
            const resposta = await fetch(`https://viacep.com.br/ws/${cep}/json/`);
            const dados = await resposta.json();
 
            if (dados.erro) {
                mostrarAviso('CEP não encontrado. Preencha o endereço manualmente.', 'erro');
                return;
            }
 
            // só substitui se a API trouxe o dado (CEPs de cidade pequena vêm sem rua/bairro)
            if (dados.logradouro) campos.rua.value = dados.logradouro;
            if (dados.bairro) campos.bairro.value = dados.bairro;
            campos.cidade.value = dados.localidade || '';
            campos.estado.value = dados.uf || '';
 
            [campos.rua, campos.bairro, campos.cidade, campos.estado].forEach(campo => {
                if (!campo) return;
                campo.classList.add('preenchido-auto');
                setTimeout(() => campo.classList.remove('preenchido-auto'), 1200);
            });
 
            mostrarAviso(`✓ ${dados.localidade} - ${dados.uf}`, 'ok');
 
            // pula para o número (ou para a rua, se ela veio vazia)
            (dados.logradouro ? campos.numero : campos.rua)?.focus();
 
        } catch (erro) {
            ultimoCepBuscado = '';
            mostrarAviso('Não foi possível buscar o CEP agora. Preencha manualmente.', 'erro');
        } finally {
            campoCep.classList.remove('buscando');
        }
    }
 
    campoCep.addEventListener('input', () => {
        campoCep.value = formatarCep(campoCep.value);
        if (campoCep.value.replace(/\D/g, '').length === 8) {
            buscarCep();
        } else {
            mostrarAviso('', '');
        }
    });
 
    campoCep.addEventListener('blur', buscarCep);
})();

