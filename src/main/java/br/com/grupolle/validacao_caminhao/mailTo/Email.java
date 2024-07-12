package br.com.grupolle.validacao_caminhao.mailTo;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

public class Email {
	private String remetente;
	private String senha;
	private String destinatarios;	
	private String host;
	private String assunto;
	private String txtCorpo; 

	public void Email(String remetente, String senha, String destinatarios, String host) {
		this.remetente=remetente;
		this.senha=senha;
		this.destinatarios=destinatarios;
		this.host=host;
	}

	public void enviarEmail(boolean envioHTML, List<File>  anexos) throws Exception {
		//String remetente = "noreply2@lleferragens.com.br";
		//String senha ="grupolle@2020@";
		Properties propsTLS = new Properties();			
		propsTLS.put("mail.smtp.auth", "true");
		propsTLS.put("mail.smtp.starttls", "true"); 
		propsTLS.put("mail.smtp.starttls.enable", "true");
		propsTLS.put("mail.smtp.host", this.host);//"smtp.office365.com"
		propsTLS.put("mail.smtp.port", "587");

		Session session = Session.getInstance(propsTLS, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(remetente, senha);
			}
		});

		Address[] destinatarios = InternetAddress.parse(this.destinatarios);

		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(remetente)); /*Quem est� enviano*/
		message.setRecipients(Message.RecipientType.TO, destinatarios);/*Email de destino*/
		message.setSubject(this.assunto);/*Assunto do e-mail*/

		MimeBodyPart corpoEmail = new MimeBodyPart();

		if (envioHTML) {				
			corpoEmail.setContent(this.txtCorpo, "text/html; charset=utf-8");					
		}else{
			corpoEmail.setText(this.txtCorpo);
		}

		Multipart mp = new MimeMultipart();
		if (anexos != null && anexos.size()>0 ) {
			for (File arquivo : anexos) {
				MimeBodyPart anexoEmail = new MimeBodyPart();

				FileInputStream fip = new FileInputStream(arquivo);
				String nomeArquivo = arquivo.getPath().substring(arquivo.getPath().lastIndexOf("\\")+1, arquivo.getPath().length());

				anexoEmail.setDataHandler(new DataHandler(new ByteArrayDataSource(fip, "application/pdf")));
				anexoEmail.setFileName(nomeArquivo);

				mp.addBodyPart(anexoEmail);
			}

		}										
		mp.addBodyPart(corpoEmail);			
		message.setContent(mp);			

		Transport.send(message);
	}



	public String getRemetente() {
		return remetente;
	}


	public void setRemetente(String remetente) {
		this.remetente = remetente;
	}


	public String getSenha() {
		return senha;
	}


	public void setSenha(String senha) {
		this.senha = senha;
	}


	public String getDestinatarios() {
		return destinatarios;
	}


	public void setDestinatarios(String destinatarios) {
		this.destinatarios = destinatarios;
	}


	public String getHost() {
		return host;
	}


	public void setHost(String host) {
		this.host = host;
	}
	public String getAssunto() {
		return assunto;
	}

	public void setAssunto(String assunto) {
		this.assunto = assunto;
	}
	public String getTxtCorpo() {
		return txtCorpo;
	}

	public void setTxtCorpo(String txtCorpo) {
		this.txtCorpo = txtCorpo;
	}

}







