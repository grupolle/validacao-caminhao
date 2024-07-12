package br.com.grupolle.validacao_caminhao.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ValidarCaminhaoRequest {
    @JsonProperty("login")
    private String login;

    // Construtor padrão
    public ValidarCaminhaoRequest() {}

    // Construtor com parâmetros
    public ValidarCaminhaoRequest(String login) {
        this.login = login;
    }

    // Getter e setter
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}
