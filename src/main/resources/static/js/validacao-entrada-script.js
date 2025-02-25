$(document).ready(function() {
    const tabelaordemcarga = $('#tabelaordemcarga tbody');
    const inputOC = $('#inputOC');
    const btnLimpar = $('#btnLimpar');
    const btnDeslogar = $('#btnDeslogar');
    const btnAbrirModalValidar = $('#btnAbrirModalValidar');
    const totalRegistros = $('#totalRegistros'); // Defina a variável corretamente
    const modalValidar = $('#modalValidar');
    const btnIniciarValidacao = $('#btnIniciarValidacao');
    const inputIdRev = $('#inputIdRev');
    const validarManualModal = $('#validarManualModal');
    const btnIniciarValidacaoManual = $('#btnIniciarValidacaoManual');
     const validarManualModalInfo = $('#validarManualModalInfo');
    const btnIniciarValidacaoManualInfo= $('#btnIniciarValidacaoManualInfo'); // Adicionei a variável para o botão de validação manual

    // Focar automaticamente no campo inputOC ao abrir a página
    inputOC.focus();

    btnLimpar.addClass('d-none');

    // Monitorar mudanças no campo inputOC
    inputOC.on('input', function () {
        const ordemcarga = inputOC.val().trim();
        clearTimeout(this.timer);
        this.timer = setTimeout(function () {
            if (ordemcarga !== '') {
                carregarLista(ordemcarga);
            } else {
                limparLista();
            }
        }, 1500);
    });

    // Evento para limpar a lista
    $('#btnLimpar').on('click', function () {
        limparLista();
    });

    // Função para limpar a lista
    function limparLista() {
        tabelaordemcarga.empty();
        totalRegistros.text(''); // Certifique-se de que totalRegistros é um objeto jQuery
        btnAbrirModalValidar.addClass('d-none');
        btnLimpar.addClass('d-none');
        inputOC.val('');
        inputOC.focus();
    }

    // Função para abrir modal de validação normal
    $('#btnAbrirModalValidar').on('click', function() {
        $('#modalValidar').modal('show');
    });

    btnIniciarValidacao.on('click', function () {
        const idRev = inputIdRev.val().trim();
        if (idRev === '') {
            showAlert('Por favor, informe o ID REV.', 'warning');
            return;
        }
        validarIdRev(idRev);
        inputIdRev.val('');
    });

$('#inputIdRev').on('input', function () {
    const inputOC = $('#inputOC').val().trim(); // Obtendo o valor do inputOC
    const self = this; // Salva referência ao input para uso no setTimeout

    clearTimeout(this.timer);
    this.timer = setTimeout(function () {
        const idRev = $(self).val().trim(); // Captura o valor dentro do setTimeout

        if (idRev !== '') {
            if (idRev === inputOC) {
                $('.alert-oc-warning').remove();
                $('body').append(`<div class="alert-message alert-oc-warning">VOCÊ ESTÁ INFORMANDO A OC: <strong>${idRev}</strong></div>`);
                $('#inputIdRev').val('');
                 tocarSom('sounds/incorreto.mp3');

                setTimeout(() => $('.alert-oc-warning').fadeOut(500, function() { $(this).remove(); }), 2000);
            } else {
                const linhaCorrespondente = $('#tabelaordemcarga tbody tr').filter(function () {
                    return $(this).find('td:first').text().trim() === idRev;
                });

                if (linhaCorrespondente.length === 0) {
                    $('.alert-etiqueta-nao-encontrada').remove();
                    $('body').append(`<div class="alert-message alert-etiqueta-nao-encontrada">A ETIQUETA <strong>${idRev}</strong> NÃO PERTENCE À ORDEM DE CARGA INFORMADA</div>`);
                    $('#inputIdRev').val('');
                     tocarSom('sounds/incorreto.mp3');

                    setTimeout(() => $('.alert-etiqueta-nao-encontrada').fadeOut(500, function() { $(this).remove(); }), 2000);
                } else {
                    const jaValidado = linhaCorrespondente.find('.entrou .fa-check').length > 0;

                    if (jaValidado) {
                        $('.alert-etiqueta-ja-validada').remove();
                        $('body').append(`<div class="alert-message alert-etiqueta-ja-validada">ETIQUETA <strong>${idRev}</strong> JÁ VALIDADA</div>`);
                        $('#inputIdRev').val('');
                         tocarSom('sounds/incorreto.mp3');

                        setTimeout(() => $('.alert-etiqueta-ja-validada').fadeOut(500, function() { $(this).remove(); }), 2000);
                    } else {
                        validarIdRev(idRev, 'E');
                    }
                }
            }
        }
    }, 1000);
});

    // Função para abrir modal de validação normal com foco no campo de inputIdRev
    $('#modalValidar').on('shown.bs.modal', function () {
        $('#inputIdRev').focus();
    });

   function escapeHtml(text) {
        var map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        };
        return text.replace(/[&<>"']/g, function(m) { return map[m]; });
    }

    // Função para mostrar alertas
    function showAlert(message, type) {
        const alertDiv = $(`<div class="alert alert-${type} alert-message">${message}</div>`);
        $('body').append(alertDiv);
        setTimeout(() => {
            alertDiv.fadeOut(2000, function() {
                $(this).remove();
            });
        }, 1000);
    }

  // Evento para abrir a modal e carregar os itens
    $(document).on('click', '.icon-eye', function() {
        var idRev = $(this).closest('tr').find('.idrev').text();
        var volumoso = $(this).data('volumoso');
        carregarItensModal(idRev, volumoso);
        validarManualModal.modal('show');
    });

window.validarVolumeManual = function() {
    var idRev = $('#idRevManual').val();  
    validarIdRev(idRev, 'M');  
    $('#validarManualModal').modal('hide');
};

    
function validarIdRev(idRev, tipoValidacao) {
    var token = localStorage.getItem("token");
    var login = localStorage.getItem("login");
    $.ajax({
        url: 'CaminhaoController/validar-caminhao/idrev/' + idRev + '?tipoValidacao=' + tipoValidacao,
        type: 'PUT',
          contentType: 'application/json',  // Defina o tipo como JSON
    data: JSON.stringify({
        login: login,  // Passe o login ou qualquer outra informação necessária
        tipoValidacao: tipoValidacao  // Passe o tipo de validação
    }),
        headers: {
            Authorization: 'Bearer ' + token
        },
        success: function(data) {
            if (data === "Sucesso") {
                showAlert("Volume validado com sucesso.", 'success');
                tocarSom('sounds/correto.mp3');
                $('#inputIdRev').val('');
                atualizarIcone(idRev);
                atualizarContadores();
                moverParaFim(idRev);
            } else {
                showAlert("Falha na validação do volume.", 'danger');
                tocarSom('sounds/incorreto.mp3');
            }
        },
        error: function(xhr, status, error) {
            console.error('Erro ao validar volume:', error);
            showAlert("Erro ao validar volume.", 'danger');
            tocarSom('sounds/incorreto.mp3');
        }
    });
}


    // Função para atualizar o ícone na tabela principal
    function atualizarIcone(idRev) {
        const row = tabelaordemcarga.find('tr').filter(function() {
            return $(this).find('.idrev').text() === idRev;
        });

        if (row.length) {
            row.find('.entrou').html('<i class="fas fa-check text-success entrou-icone"></i>'); // Atualiza ícone para validado
        }
    }

    // Função para mover a linha para o fim da tabela
    function moverParaFim(idRev) {
        const row = tabelaordemcarga.find('tr').filter(function() {
            return $(this).find('.idrev').text() === idRev;
        });

        if (row.length) {
            row.remove(); // Remove a linha da posição atual
            tabelaordemcarga.append(row); // Adiciona a linha ao fim da tabela
        }
    }
    var dadosOrdemCarga = [];  // Variável para armazenar os dados
    // Função para carregar a lista de ordens de carga
    function carregarLista(ordemcarga) {
        var token = localStorage.getItem("token");
        $.ajax({
            url: 'CaminhaoController/chamar-validacoes-oc/ordemcarga/' + ordemcarga,
            type: 'GET',
            dataType: 'json',
            headers: {
                Authorization: 'Bearer ' + token
            },
            success: function(data) {
				dadosOrdemCarga = data; 
                tabelaordemcarga.empty(); // Limpar a tabela antes de adicionar novos dados

                let total = data.length;
                let validados = [];
                let naoValidados = [];
               

                data.forEach(entrada => {
                    const entroucaminhao = entrada['entroucaminhao'];
                    const entrouIcone = (entroucaminhao === 'S') ? '<i class="fas fa-check text-success entrou-icone"></i>' : '<i class="fas fa-times text-danger entrou-icone"></i>';
                    const confCell = (entrada['exigeconf'] === 'N') ? '<i class="fas fa-eye icon-eye" data-volumoso="' + entrada['volumoso'] + '"></i>' : '';
                    const infoCell = '<i class="fas fa-info-circle icon-info" data-idrev="' + entrada['idrev'] + '"></i>'; // Botão de informações
                    const nomeparc = entrada['nomeparc'] || 'N/A';  // Verifica se 'nomeparc' existe
                    const area = entrada['area'] || 'N/A';  // Verifica se 'area' existe
                    
                    
                    const row = `
                        <tr>
                            <td class="idrev">${entrada['idrev']}</td>
                            <td class="numnota">${entrada['numnota']}</td>
                            <td class="entrou">${entrouIcone}</td>
                            <td class="sequencia">${entrada['sequencia']}</td>
                            <td class="conf-cell">${confCell}</td>
                             <td class="info-cell">${infoCell}</td> 
                        </tr>`;
                    if (entroucaminhao === 'S') {
                        validados.push(row);
                    } else {
                        naoValidados.push(row);
                    }
                });

                tabelaordemcarga.append(naoValidados.join(''));
                tabelaordemcarga.append(validados.join(''));
            
                atualizarContadores();


                if (total > 0) {
                    btnAbrirModalValidar.removeClass('d-none');
                    btnLimpar.removeClass('d-none');
                } else {
                    btnAbrirModalValidar.addClass('d-none');
                    btnLimpar.addClass('d-none');
                    inputOC.focus();
                }
            },
            error: function(error) {
                console.error('Erro ao carregar os dados:', error);
                showAlert('Erro ao carregar os dados.', 'danger');
            }
        });
        
 }
 function atualizarContadores() {
    let totalNaoValidados = $('#tabelaordemcarga tbody tr').filter(function() {
        return $(this).find('.entrou .fa-times').length > 0;
    }).length;

    let totalValidados = $('#tabelaordemcarga tbody tr').filter(function() {
        return $(this).find('.entrou .fa-check').length > 0;
    }).length;

    let totalGeral = $('#tabelaordemcarga tbody tr').length; // Contagem total de registros

    // Atualiza os valores no HTML
    $('#totalRegistros').html(`
      <span class="texto-verde">${totalValidados} VALIDADOS</span> <br> 
        <span class="texto-vermelho">${totalNaoValidados} PENDENTES</span>         
        <span class="texto-azul">DE ${totalGeral} VOLUMES</span>
    `);
}
 
  $('#btnDeslogar').on('click', function () {
        deslogar();
    });
 // Função para deslogar e redirecionar
function deslogar() {
    localStorage.removeItem('token');
    localStorage.removeItem('cnpj');
    localStorage.removeItem('login');
    window.location.href = "loginvalidacao.html";
}
function tocarSom(url) {
    var audio = new Audio(url);
    audio.play().catch(function(error) {
        console.error('Erro ao tocar o som:', error);
    });
    
}
    // Função para carregar os itens na modal
    function carregarItensModal(idRev, volumoso) {
        $('#idRevManual').val(idRev);  // Armazena o idRev na modal
        $('#listaVolumoso tbody').html(tabelaVolumoso(volumoso));
    }
 function tabelaVolumoso(volumoso) {
    var rowsHtml = '';
    var produtos = volumoso.split(';'); 
    
    produtos.forEach(function(produto) {
        var partes = produto.split('QTD');
        var descricao = escapeHtml(partes[0].trim()); 
        var quantidade = escapeHtml(partes.length > 1 ? partes[1].trim() : '');

        rowsHtml += `
            <tr>
                <td>${descricao}</td>
                <td>${quantidade}</td>
            </tr>
        `;
    });

    return rowsHtml;
}


// Função para criar as linhas da tabela com as informações recebidas
function tabelaInfo(info) {
    var area = info.area || 'N/A';  // Garante que 'area' tem um valor válido
    var nomeparc = info.nomeparc || 'N/A';  // Garante que 'nomeparc' tem um valor válido

    var rowsHtml = `
        <tr>
            <td>${area}</td>
            <td>${nomeparc}</td>
        </tr>
    `;

    return rowsHtml;  // Retorna as linhas para preencher o corpo da tabela
}

// Evento para abrir a modal de informações
$(document).on('click', '.icon-info', function() {
    var idRev = $(this).closest('tr').find('.idrev').text().trim(); // Captura o IDREV correto

    console.log('IDREV capturado:', idRev); // Log para verificar

    // Busca as informações nos dados carregados
    var info = dadosOrdemCarga.find(entrada => entrada.idrev == idRev);

    if (info) {
        abrirModalInfo(info);
    } else {
        console.error('Informações não encontradas para o idRev:', idRev);
    }
});

// Função para abrir a modal e carregar as informações
function abrirModalInfo(info) {
    $('#idRevManualInfo').val(info.idrev || 'N/A'); // Define o idRev corretamente
    console.log('IDREV definido na modal:', info.idrev); // Log para depuração

    // Preenche a tabela dentro da modal
    $('#ModalInfo tbody').html(tabelaInfo(info)); 
    $('#ModalInfo').modal('show'); 
}

// Definição da função de validação
window.validarVolumeManualInfo = function() {
    var idRev = $('#idRevManualInfo').val().trim(); // Pega o ID correto

    console.log('Validando idRev:', idRev); 

    if (!idRev || idRev === 'N/A') {
        console.error('IDREV não encontrado ou inválido!');
        return;
    }

    validarIdRev(idRev, 'M'); 
    $('#ModalInfo').modal('hide');
};



});



