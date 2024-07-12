package br.com.grupolle.validacao_caminhao.security;

import java.io.IOException;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import br.com.grupolle.validacao_caminhao.ApplicationContextLoad;
import br.com.grupolle.validacao_caminhao.model.Usuarios;
import br.com.grupolle.validacao_caminhao.repository.UsuarioRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
@Component
public class JWTTokenAutenticacaoService {
	
	/*Tem de validade do Token 2 dias*/
	private static final long EXPIRATION_TIME = 972800000;
	
	/*Uma senha unica para compor a autenticacao e ajudar na segurança*/
	private static final String SECRET = "SenhaExtremamenteSecreta";
	
	/*Prefixo padrão de Token*/
	private static final String TOKEN_PREFIX = "Bearer";
	
	private static final String HEADER_STRING = "Authorization";
	
	 /*Gerando token de autenticado e adiconando ao cabeçalho e resposta Http*/
    public void addAuthentication(HttpServletResponse response, String username) throws IOException {
        
        /*Montagem do Token*/
        String JWT = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
        
        String token = TOKEN_PREFIX + " " + JWT;
        
        response.addHeader(HEADER_STRING, token);
        ApplicationContextLoad.getApplicationContext().getBean(UsuarioRepository.class)
                .atualizaTokenUser(JWT, username);
        
        libercaoCors(response);
        
        response.getWriter().write("{\"Authorization\": \"" + token + "\"}");
    }

    /*Retorna o usuário validado com token ou caso não seja valido retorna null*/
    public Authentication getAuthentication(HttpServletRequest request, HttpServletResponse response) {
        
        String token = request.getHeader(HEADER_STRING);
        try {
            if (token != null) {
                String tokenLimpo = token.replace(TOKEN_PREFIX, "").trim();
                String user = Jwts.parser()
                        .setSigningKey(SECRET)
                        .parseClaimsJws(tokenLimpo)
                        .getBody()
                        .getSubject();
                if (user != null) {
                    Usuarios usuario = ApplicationContextLoad.getApplicationContext()
                            .getBean(UsuarioRepository.class)
                            .findByLogin(user);
                    if (usuario != null) {
                        if (tokenLimpo.equalsIgnoreCase(usuario.getToken())) {
                            return new UsernamePasswordAuthenticationToken(usuario.getLogin(), usuario.getSenha(),
                                    usuario.getAuthorities());
                        }
                    }
                }
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            try {
                response.getOutputStream().println("TOKEN expirado!!!");
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

        libercaoCors(response);
        return null; /*Não autorizado*/
    }

    private void libercaoCors(HttpServletResponse response) {
        if (response.getHeader("Access-Control-Allow-Origin") == null) {
            response.addHeader("Access-Control-Allow-Origin", "*");
        }

        if (response.getHeader("Access-Control-Allow-Headers") == null) {
            response.addHeader("Access-Control-Allow-Headers", "*");
        }

        if (response.getHeader("Access-Control-Request-Headers") == null) {
            response.addHeader("Access-Control-Request-Headers", "*");
        }

        if (response.getHeader("Access-Control-Allow-Methods") == null) {
            response.addHeader("Access-Control-Allow-Methods", "*");
        }
    }
}