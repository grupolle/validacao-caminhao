function deslogar() {
    localStorage.removeItem('token');
    localStorage.removeItem('cnpj');
    localStorage.removeItem('login');
    window.location.href = "loginvalidacao.html";
}

function showAlert(message, type) {
    const alertDiv = $(`<div class="alert alert-${type} alert-message">${message}</div>`);
    $('body').append(alertDiv);
    setTimeout(() => {
        alertDiv.fadeOut(2000, function() {
            $(this).remove();
        });
    }, 1000);
}

$(document).ready(function () {
    const tabelaordemcarga = $('#tabelaordemcarga tbody');
    const inputOC = $('#inputOC');
    const btnCarregar = $('#btnCarregar');
    const totalRegistros = $('#totalRegistros');
    const btnAbrirModalValidar = $('#btnAbrirModalValidar');
    const btnLimpar = $('#btnLimpar');
    const modalValidar = $('#modalValidar');
    const btnIniciarValidacao = $('#btnIniciarValidacao');
    const inputIdRev = $('#inputIdRev');

    // Focar automaticamente no campo inputOC ao abrir a página
    inputOC.focus();
    
    btnLimpar.addClass('d-none');
    // Monitorar mudanças no campo inputOC
     inputOC.on('input', function () {
    const ordemcarga = inputOC.val().trim();
    // Limpar o temporizador anterior, se houver
    clearTimeout(this.timer);

    // Configurar um novo temporizador para aguardar 1 segundo após a última entrada
    this.timer = setTimeout(function () {
        if (ordemcarga !== '') {
            carregarLista(ordemcarga);
        } else {
            limparLista();
        }
    }, 1000);
});

    function carregarLista(ordemcarga) {
        var token = localStorage.getItem("token");
        $.ajax({
            url: 'UsuarioController/chamar-validacoes-oc/ordemcarga/' + ordemcarga,
            type: 'GET',
            dataType: 'json',
            headers: {
                Authorization: 'Bearer ' + token
            },
            success: function (data) {
              
                tabelaordemcarga.empty(); // Limpar a tabela antes de adicionar novos dados

                let total = data.length;
                let validados = [];
                let naoValidados = [];

                for (let i = 0; i < data.length; i++) {
                    const entrada = data[i];
                    const entroucaminhao = entrada['entroucaminhao'];               
                    const entrouIcone = (entroucaminhao === 'S') ? '<i class="fas fa-check text-success entrou-icone"></i>' : '<i class="fas fa-times text-danger entrou-icone"></i>';               
                    const row = `
                        <tr>
                            <td class="idrev">${entrada['idrev']}</td>
                            <td class="numnota">${entrada['numnota']}</td>
                            <td class="entrou">${entrouIcone}</td>
                            <td class="sequencia">${entrada['sequencia']}</td>
                        </tr>`;
                    if (entroucaminhao === 'S') {
                        validados.push(row);
                    } else {
                        naoValidados.push(row);
                    }
                }

                tabelaordemcarga.append(naoValidados.join(''));
                tabelaordemcarga.append(validados.join(''));

                totalRegistros.text(`FORAM ENCONTRADOS ${total} REGISTROS`);
                if (total > 0) {
                    btnAbrirModalValidar.removeClass('d-none');
                    btnLimpar.removeClass('d-none');
                } else {
                    btnAbrirModalValidar.addClass('d-none');
                    btnLimpar.addClass('d-none');
                    inputOC.focus();
                }
            },
            error: function (error) {
                console.error('Erro ao carregar os dados:', error);
                showAlert('Erro ao carregar os dados.', 'danger');
            }
        });
    }
    
     function validarIdRev(idRev) {
        var token = localStorage.getItem("token");

        // Verificar se o idRev está presente na tabela
        let idRevPresente = false;
        const rows = tabelaordemcarga.find('tr');
        rows.each(function () {
            const row = $(this);
            const idRevAtual = row.find('.idrev').text();
            if (idRevAtual === idRev) {
                idRevPresente = true;
                return false; // Sair do loop each
            }
        });

        if (!idRevPresente) {
            showAlert("Esse volume não pertence à ordem de carga informada.", 'danger');
             $('#inputIdRev').val('');
            return;
        }

        let idRevJaValidado = false;
        rows.each(function () {
            const row = $(this);
            const idRevAtual = row.find('.idrev').text();
            if (idRevAtual === idRev) {
                const entrouIcone = row.find('.entrou').html();
                if (entrouIcone.includes('fa-check')) {
                    idRevJaValidado = true;
                    return false;
                }
            }
        });

        if (idRevJaValidado) {
            showAlert("Etiqueta já foi validada.", 'warning');
            $('#inputIdRev').val('');
            return;
        }

        $.ajax({
            type: "PUT",
            url: 'UsuarioController/validar-caminhao/idrev/' + idRev,
            headers: {
                Authorization: 'Bearer ' + token
            },
            contentType: "application/json",
            success: function (response) {
                console.log("Resposta do servidor:", response);
                if (response.success) {
                    showAlert("Etiqueta validada com sucesso.", 'success');
                } else {
                    showAlert("Etiqueta validada com sucesso.", 'success');
                }
                $('#inputIdRev').val('');
                atualizarIcone(idRev);
            },
            error: function (error) {
                console.error("Erro ao validar a etiqueta:", error);
                showAlert("Etiqueta validada com sucesso.", 'success');
                $('#inputIdRev').val('');
                atualizarIcone(idRev);
            }
        });
    }

    
    function limparLista() {
        tabelaordemcarga.empty();
        totalRegistros.text('');
        btnAbrirModalValidar.addClass('d-none');
        btnLimpar.addClass('d-none');
        inputOC.focus();
    }

    btnAbrirModalValidar.on('click', function () {
        if (tabelaordemcarga.find('tr').length === 0) {
            showAlert('Não há registros para validar.', 'warning');
            return;
        }
        modalValidar.modal('show');
    });
     btnLimpar.on('click', function () {
        if (tabelaordemcarga.find('tr').length === 0) {
            showAlert('Não há nada para limpar.', 'warning');
            return;
        }
         $('#inputOC').val('');
         tabelaordemcarga.empty();
         totalRegistros.text('');
         btnAbrirModalValidar.addClass('d-none');
         inputOC.focus();
         btnLimpar.addClass('d-none');
         
    });

    modalValidar.on('shown.bs.modal', function () {
        inputIdRev.focus();
    });

    btnIniciarValidacao.on('click', function () {
        const idRev = inputIdRev.val().trim();
        if (idRev === '') {
            showAlert('Por favor, informe o ID REV.', 'warning');
            return;
        }
        validarIdRev(idRev);
    });

   inputIdRev.on('input', function () {
    const idRev = $(this).val().trim();
    // Limpar o temporizador anterior, se houver
    clearTimeout(this.timer);

    // Configurar um novo temporizador para aguardar 1 segundo após a última entrada
    this.timer = setTimeout(function () {
        if (idRev !== '') {
            validarIdRev(idRev);
        }
    }, 500);
});

   
    function atualizarIcone(idRev) {
        const rows = tabelaordemcarga.find('tr');
        rows.each(function () {
            const row = $(this);
            const idRevAtual = row.find('.idrev').text();
            if (idRevAtual === idRev) {
                const entrouIcone = '<i class="fas fa-check text-success entrou-icone"></i>';
                row.find('.entrou').html(entrouIcone);
            }
        });

        // Reordenar as linhas validadas para o final
        const validados = [];
        const naoValidados = [];

        rows.each(function () {
            const row = $(this);
            const entrouIcone = row.find('.entrou').html();
            if (entrouIcone.includes('fa-check')) {
                validados.push(row);
            } else {
                naoValidados.push(row);
            }
        });

        tabelaordemcarga.append(naoValidados);
        tabelaordemcarga.append(validados);
    }
});
