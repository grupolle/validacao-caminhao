package br.com.grupolle.validacao_caminhao.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import br.com.grupolle.validacao_caminhao.mailTo.Email;
import br.com.grupolle.validacao_caminhao.model.Pedido;
import br.com.grupolle.validacao_caminhao.model.Usuarios;
import br.com.grupolle.validacao_caminhao.model.ValidarCaminhaoRequest;
import br.com.grupolle.validacao_caminhao.repository.UsuarioRepository;
import br.com.grupolle.validacao_caminhao.service.AltSenha;
import br.com.grupolle.validacao_caminhao.service.DadosPrimeiroAcesso;
import br.com.grupolle.validacao_caminhao.service.EnviarEmail;
import br.com.grupolle.validacao_caminhao.service.GeradorSenha;
import br.com.grupolle.validacao_caminhao.service.ImplementacaoUserDetailsService;
import okhttp3.*;


@Controller
@RequestMapping(value = "UsuarioController")
public class UsuarioController {
	
	@Autowired
	private ImplementacaoUserDetailsService UserDetailsService;
	@Autowired
    private UsuarioRepository usuarioRepository;
	@Autowired
    private EnviarEmail EnviarEmail;
	
	@PutMapping(value = "/primeiroacesso")
	@ResponseBody
	public ResponseEntity<String> primeiroacesso(@RequestBody DadosPrimeiroAcesso dados) {
		if (dados.getLogin() == null ) {
			return new ResponseEntity<>("Login inválido", HttpStatus.BAD_REQUEST);
		}

		Usuarios usuario = usuarioRepository.findByLogin(dados.getLogin());

		if (usuario == null) {
			return new ResponseEntity<>("Fornecedor não encontrado", HttpStatus.NOT_FOUND);
		}

		if (!usuario.getEmail().equals(dados.getEmail()) || !usuario.getLogin().equals(dados.getLogin())) {
			return new ResponseEntity<>("Não possui cadastro", HttpStatus.NOT_FOUND);
		}

		if (usuario.getSenha() == null) {
			BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
			String senhaCriptografada = passwordEncoder.encode(dados.getSenha());
			usuario.setSenha(senhaCriptografada);
			usuarioRepository.save(usuario);
			return new ResponseEntity<>("Senha cadastrada com sucesso", HttpStatus.OK);
		} else {
			return new ResponseEntity<>("CNPJ já possui senha cadastrada", HttpStatus.CONFLICT);
		}
	}
	
	@GetMapping("/chamar-validacoes-oc/ordemcarga/{ordemcarga}")
	public ResponseEntity<List<Pedido>> chamarValidacoesByOrdemCarga(@PathVariable Long ordemcarga) {
	    Long codemp = UserDetailsService.getCodempFromLoggedInUser();
	    if (codemp == null) {
	        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	    }

	    OkHttpClient client = new OkHttpClient();
	    String apiKey = "a5F8jL2sG7dP9qZ1wX4cV6bN3mK0oH2iY8rT9eU5";
	    String url = "http://192.168.2.58:8080/api-sankhya-lle/view/volume-entrada/validacoes-oc/" + ordemcarga + "?codemp=" + codemp;

	    Request request = new Request.Builder()
	            .url(url)
	            .get()
	            .addHeader("accept", "application/json")
	            .addHeader("X-API-Key", apiKey)
	            .build();

	    Response response = null;
	    List<Pedido> validacoes = new ArrayList<>();

	    try {
	        response = client.newCall(request).execute();
	        if (response.isSuccessful()) {
	            String responseBody = response.body().string();
	            JSONArray jsonArray = new JSONArray(responseBody);

	            for (int i = 0; i < jsonArray.length(); i++) {
	                JSONObject jsonObject = jsonArray.getJSONObject(i);

	                Pedido pedido = new Pedido();
	                pedido.setIdrev(jsonObject.optLong("idrev"));
	                pedido.setOrdemCarga(jsonObject.optLong("ordemcarga"));
	                pedido.setNumnota(jsonObject.optLong("numnota"));	               	      	                
	                pedido.setEntroucaminhao(jsonObject.optString("entrou_caminhao"));
	                pedido.setSequencia(jsonObject.optLong("sequencia"));
	                pedido.setExigeconf(jsonObject.optString("exigeconf"));
	                pedido.setVolumoso(jsonObject.optString("volumoso"));
	                validacoes.add(pedido);
	            }

	            return new ResponseEntity<>(validacoes, HttpStatus.OK);
	        } else {
	            System.out.println("Erro na requisição: " + response.code() + " - " + response.message());
	            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	        }
	    } catch (IOException | JSONException e) {
	        e.printStackTrace();
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    } finally {
	        if (response != null) {
	            response.close();
	        }
	    }
	}
		
	@PutMapping("/validar-caminhao/idrev/{idrev}")
	public ResponseEntity<String> validarCaminhao(@PathVariable Long idrev) {
	    String login = UserDetailsService.getLoginFromLoggedInUser();
	    if (login == null) {
	        return new ResponseEntity<>("Usuário não autenticado", HttpStatus.UNAUTHORIZED);
	    }

	    String url = "http://192.168.2.58:8080/api-sankhya-lle/input/volume-entrada/validar-caminhao/" + idrev;
	    OkHttpClient client = new OkHttpClient();
	    String apiKey = "a5F8jL2sG7dP9qZ1wX4cV6bN3mK0oH2iY8rT9eU5";

        System.out.println("Login enviado: " + login);

	    okhttp3.RequestBody body = okhttp3.RequestBody.create(login, okhttp3.MediaType.parse("text/plain; charset=utf-8"));

	    Request request = new Request.Builder()
	            .url(url)
	            .put(body)
	            .addHeader("accept", "application/json")
	            .addHeader("X-API-Key", apiKey)
	            .build();

	    try (Response response = client.newCall(request).execute()) {
	        if (!response.isSuccessful()) {
	            return new ResponseEntity<>("Erro: " + response, HttpStatus.INTERNAL_SERVER_ERROR);
	        }
	        return new ResponseEntity<>("Sucesso", HttpStatus.OK);
	    } catch (IOException e) {
	        e.printStackTrace();
	        return new ResponseEntity<>("Erro: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	@PutMapping("/trocarSenha")
	public ResponseEntity<String> trocarSenha(@RequestBody AltSenha altSenha) {ResponseEntity retorno;		
	try {
		
		Usuarios usuario =  usuarioRepository.findByLogin(altSenha.getLogin());
		String novaSenha=new BCryptPasswordEncoder().encode(altSenha.getNovaSenha());		
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		
		if (passwordEncoder.matches(altSenha.getSenha() , usuario.getSenha())) {
			usuario.setSenha(novaSenha);
			usuarioRepository.save(usuario);
			retorno = new ResponseEntity<>("Senha alterada!", HttpStatus.OK);
		}else {
			retorno =  new ResponseEntity<>("Alteração não realizada!",HttpStatus.NOT_FOUND);
		}	
	}catch (Exception e) {
		retorno =  new ResponseEntity<>("Alteração não realizada!",HttpStatus.NOT_FOUND);
	}
	return retorno;        
	}
    @GetMapping("/usuarios/id")
    public ResponseEntity<Long> obterIdUsuario(@RequestParam String login) {
        Usuarios usuario = usuarioRepository.findByLogin(login);
        if (usuario != null) {
            return ResponseEntity.ok(usuario.getId());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @PostMapping(value = "enviaremailsenha")
	@ResponseBody
	public ResponseEntity<String> enviarEmailSenha(@RequestParam String emailUsuario) {
		try {
			Usuarios usuario = usuarioRepository.findByEmail(emailUsuario);

			if (!(usuario == null)) {
				try {

					String senhaGerada = GeradorSenha.gerarSenha(8);
					String senhaCrypto = new BCryptPasswordEncoder().encode(senhaGerada);
					usuario.setSenha(senhaCrypto);
					usuarioRepository.save(usuario);
					EnviarEmail.enviarEmailSenha(emailUsuario, senhaGerada);

					return new ResponseEntity<>("Email Enviado!", HttpStatus.OK);
				} catch (Exception e) {
					return new ResponseEntity<>("Erro ao processar os dados", HttpStatus.BAD_REQUEST);
				}
			} else {
				return new ResponseEntity<>("E-mail não encontrado", HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			return new ResponseEntity<>("Erro: " + e.getMessage() , HttpStatus.NOT_FOUND);
		}
	}
 
   
}
