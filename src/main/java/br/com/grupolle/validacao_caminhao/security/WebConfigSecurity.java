package br.com.grupolle.validacao_caminhao.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import br.com.grupolle.validacao_caminhao.service.ImplementacaoUserDetailsService;

/*Mapeaia URL, enderecos, autoriza ou bloqueia acessoa a URL*/
@Configuration
@EnableWebSecurity
public class WebConfigSecurity extends WebSecurityConfigurerAdapter {	
	@Autowired
	private ImplementacaoUserDetailsService implementacaoUserDetailsSercice; 
//	@Autowired
//    private RequestLoggingFilter requestLoggingFilter;// chat gpt pra exibir o json
	
	/*Configura as solicitações de acesso por Http*/
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		/*Ativando a proteção contra usuário que não estão validados por TOKEN*/
		http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())			
			.disable()
			.authorizeRequests()
				.antMatchers("/").permitAll()
				.antMatchers("/login").permitAll()
				.antMatchers("/primeiroacesso").permitAll()
				.antMatchers("/UsuarioController/empresa").permitAll()			
				.antMatchers("/telavalidacao.html").permitAll()
				.antMatchers("/js/**").permitAll()
				.antMatchers("/styles/**").permitAll()
				.antMatchers("/primeiroacessoval").permitAll()
				.antMatchers("/UsuarioController/chamar-validacoes-oc/**").permitAll()
				.antMatchers("/UsuarioController/validar-caminhao/**").permitAll()
				.antMatchers("/tela.html").permitAll()//authenticated() 		
					.antMatchers("/cadastro.html").permitAll()
					.antMatchers("/trocarsenha.html").permitAll()	
					.antMatchers("/UsuarioController/enviaremailsenha/**").permitAll()
					.antMatchers("/trocarSenha").permitAll()	
					.antMatchers("/esquecisenha.html").permitAll()					
					.antMatchers("/esquecisenhaemail.html").permitAll()
					.antMatchers("/primeiroacessovalidacao.html").permitAll()
					  .antMatchers("/validacao-caminhao/**").permitAll()
                    .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()						
				.anyRequest().authenticated()
			.and()
			.logout()
				.logoutSuccessUrl("/login") /*URL de Logout - Redireciona após o user deslogar do sistema*/				
				.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))/*Maperia URL de Logout e invalida o usuário*/				
			.and().addFilterBefore(new JWTLoginFilter("/login", authenticationManager()), 
																	UsernamePasswordAuthenticationFilter.class)					
			.addFilterBefore(new JwtApiAutenticacaoFilter(), UsernamePasswordAuthenticationFilter.class);/*Filtra demais requisições paa verificar a presenção do TOKEN JWT no HEADER HTTP*/	
	}
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {

	/*Service que irá consultar o usuário no banco de dados*/	
	auth.userDetailsService(implementacaoUserDetailsSercice)
	
	/*Padrão de codigição de senha*/
	.passwordEncoder(new BCryptPasswordEncoder());
	
	}    
}
