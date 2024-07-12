package br.com.grupolle.validacao_caminhao.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import br.com.grupolle.validacao_caminhao.model.Usuarios;
import br.com.grupolle.validacao_caminhao.repository.UsuarioRepository;

@Service
public class ImplementacaoUserDetailsService implements UserDetailsService {

	@Autowired
	private UsuarioRepository usuarioRepository;
	
	  @Override
	    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
	        Usuarios usuario = usuarioRepository.findByLogin(login);
	        if (usuario == null) {
	            throw new UsernameNotFoundException("Usuário não encontrado com o login: " + login);
	            }
			return usuario;
	        }		
	

	public String getCnpj() {
		// TODO Auto-generated method stub
		return null;
	}
	 @Bean
	    public BCryptPasswordEncoder passwordEncoder() {
	        return new BCryptPasswordEncoder();
	    }
	
	
		 public Long getCodempFromLoggedInUser() {
			    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			    if (authentication != null && authentication.isAuthenticated()) {
			        Object principal = authentication.getPrincipal();
			        if (principal instanceof UserDetails) {
			            String login = ((UserDetails) principal).getUsername();
			            Usuarios usuario = usuarioRepository.findByLogin(login);
			            if (usuario != null) {
			                Long codemp = usuario.getcodemp();
			                System.out.println("Usuário encontrado: " + usuario.getLogin() + ", codemp: " + codemp);
			                return codemp;
			            } else {
			                System.out.println("Usuário não encontrado para o login: " + login);
			            }
			        } else if (principal instanceof String) {
			         
			            String login = (String) principal;
			            Usuarios usuario = usuarioRepository.findByLogin(login);
			            if (usuario != null) {
			                Long codemp = usuario.getcodemp();
			                System.out.println("Usuário encontrado (string principal): " + usuario.getLogin() + ", codemp: " + codemp);
			                return codemp;
			            } else {
			                System.out.println("Usuário não encontrado para o login (string principal): " + login);
			            }
			        } else {
			            System.out.println("Principal não é uma instância de UserDetails ou String: " + principal.getClass());
			        }
			    } else {
			        System.out.println("Nenhuma autenticação válida encontrada.");
			    }
			    return null;
			}
		 public String getLoginFromLoggedInUser() {
			    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			    if (authentication != null && authentication.isAuthenticated()) {
			        Object principal = authentication.getPrincipal();
			        if (principal instanceof UserDetails) {
			            String login = ((UserDetails) principal).getUsername();
			            System.out.println("Usuário logado (UserDetails): " + login);
			            return login;
			        } else if (principal instanceof String) {
			        
			            String login = (String) principal;
			            System.out.println("Usuário logado (string principal): " + login);
			            return login;
			        } else {
			            System.out.println("Principal não é uma instância de UserDetails ou String: " + principal.getClass());
			        }
			    } else {
			        System.out.println("Nenhuma autenticação válida encontrada.");
			    }
			    return null;
			}

	 }