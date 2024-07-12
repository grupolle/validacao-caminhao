package br.com.grupolle.validacao_caminhao.service;

import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.grupolle.validacao_caminhao.mailTo.Email;
import br.com.grupolle.validacao_caminhao.repository.UsuarioRepository;

@Service
public class EnviarEmail {		
	Email mail;			
	@Autowired
	UsuarioRepository usuarioRepository; 
	@Autowired
	GeradorSenha geradorSenha; 

    @Value("${lle.ambiente}")
    private String lleAmbiente;
    
    @Value("${lle.api_agendamento_transportadora.host}")
    private String produtosNovosHost;
    
    public void configurarContaEmail() {
		String contaSMTP = "noreply@lleferragens.com.br";
		String senhaSMTP = "grupoLLE2019";
		String ambiente = "TESTE";
		mail.setRemetente(contaSMTP);
		mail.setSenha(senhaSMTP);
		mail.setHost("smtp.office365.com");
	}
    
	public void enviarEmailSenha(String emailUsuario, String senhaGerada) {
		mail =  new Email();
		configurarContaEmail();	
		mail.setAssunto("LLE - Redefinição de senha!");	
	    mail.setDestinatarios(emailUsuario);	
		mail.setTxtCorpo(obterCorpoEmailSenha(senhaGerada));
		
		try {
			 
			mail.enviarEmail(true, null);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}						
	}

	
	public String obterCorpoEmailSenha(String senhaGerada) {
		
	    StringBuilder sbTextHtml = new StringBuilder();

	    sbTextHtml.append("<b>Olá</b>! <br/><br/>");
	    sbTextHtml.append("Aqui está sua nova senha provisória de acesso: <b>" + senhaGerada + "</b><br/><br/>");
	  
	    return sbTextHtml.toString();
	}
}
