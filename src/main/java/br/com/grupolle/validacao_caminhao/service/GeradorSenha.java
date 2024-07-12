package br.com.grupolle.validacao_caminhao.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

@Component
public class GeradorSenha {

    public static String gerarSenha(int length) {
        String senhaGerada = RandomStringUtils.randomAlphanumeric(length);
        System.out.println("Sua senha é: " + senhaGerada);
        return senhaGerada;
    }
}