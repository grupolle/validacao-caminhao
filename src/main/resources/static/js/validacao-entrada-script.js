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
    const btnIniciarValidacaoManual = $('#btnIniciarValidacaoManual'); // Adicionei a variável para o botão de validação manual

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
        }, 1000);
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

    inputIdRev.on('input', function () {
        const idRev = $(this).val().trim();
        clearTimeout(this.timer);
        this.timer = setTimeout(function () {
            if (idRev !== '') {
                validarIdRev(idRev);
            }
        }, 500);
    });

    // Função para abrir modal de validação normal com foco no campo de inputIdRev
    $('#modalValidar').on('shown.bs.modal', function () {
        $('#inputIdRev').focus();
    });

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

    // Função para escapar HTML, prevenindo XSS
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

    // Função global para validar o idRev da modal
    window.validarVolumeManual = function() {
        var idRev = $('#idRevManual').val();  // Captura o idRev armazenado na modal
        validarIdRev(idRev);  // Chama a função de validação com o idRev
        validarManualModal.modal('hide');  // Fecha a modal
    };

    // Função para validar o IDREV
    function validarIdRev(idRev) {
        var token = localStorage.getItem("token");

        // Verificar se o IDREV já foi validado
        const row = tabelaordemcarga.find('tr').filter(function() {
            return $(this).find('.idrev').text() === idRev;
        });

        if (row.length && row.find('.entrou').find('i').hasClass('fa-check')) {
            showAlert('ETIQUETA JÁ VALIDADA', 'danger');
             $('#inputIdRev').val('');
            return;
        }

        var idRevPresente = row.length > 0;

        if (!idRevPresente) {
            showAlert("Esse volume não pertence à ordem de carga informada.", 'danger');
            $('#inputIdRev').val('');
            return;
        }

        $.ajax({
            url: 'CaminhaoController/validar-caminhao/idrev/' + idRev,
            type: 'PUT',
            dataType: 'text',
            headers: {
                Authorization: 'Bearer ' + token
            },
            success: function(data) {
                if (data === "Sucesso") { // Supondo que a resposta seja o texto "Sucesso"
                    showAlert("Volume validado com sucesso.", 'success');
                     $('#inputIdRev').val('');
                    atualizarIcone(idRev);
                    moverParaFim(idRev);
                } else {
                    showAlert("Falha na validação do volume.", 'danger');
                }
            },
            error: function(xhr, status, error) {
                console.error('Erro ao validar volume:', error);
                console.log('Status:', status);
                console.log('Response Text:', xhr.responseText);
                showAlert("Erro ao validar volume.", 'danger');
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
                tabelaordemcarga.empty(); // Limpar a tabela antes de adicionar novos dados

                let total = data.length;
                let validados = [];
                let naoValidados = [];

                data.forEach(entrada => {
                    const entroucaminhao = entrada['entroucaminhao'];
                    const entrouIcone = (entroucaminhao === 'S') ? '<i class="fas fa-check text-success entrou-icone"></i>' : '<i class="fas fa-times text-danger entrou-icone"></i>';
                    const confCell = (entrada['exigeconf'] === 'N') ? '<i class="fas fa-eye icon-eye" data-volumoso="' + entrada['volumoso'] + '"></i>' : '';

                    const row = `
                        <tr>
                            <td class="idrev">${entrada['idrev']}</td>
                            <td class="numnota">${entrada['numnota']}</td>
                            <td class="entrou">${entrouIcone}</td>
                            <td class="sequencia">${entrada['sequencia']}</td>
                            <td class="conf-cell">${confCell}</td>
                        </tr>`;
                    if (entroucaminhao === 'S') {
                        validados.push(row);
                    } else {
                        naoValidados.push(row);
                    }
                });

                tabelaordemcarga.append(naoValidados.join(''));
                tabelaordemcarga.append(validados.join(''));

                totalRegistros.text(`FORAM ENCONTRADOS ${total} REGISTROS`); // Atualiza o texto de totalRegistros

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
    
});
