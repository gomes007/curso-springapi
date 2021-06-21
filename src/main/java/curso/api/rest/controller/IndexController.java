package curso.api.rest.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import curso.api.rest.model.Usuario;
import curso.api.rest.repository.TelefoneRepository;
import curso.api.rest.repository.UsuarioRepository;
import curso.api.rest.service.ImplementacaoUserDetailsService;



@CrossOrigin(origins = "*")
@RestController
@RequestMapping(value = "/usuario")
public class IndexController {
	
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	@Autowired
	private ImplementacaoUserDetailsService implementacaoUserDetailsService;
	
	
	
	@GetMapping(value = "/{id}", produces = "application/json")
	public ResponseEntity<Usuario> init(@PathVariable (value = "id") Long id) {
		
		Optional<Usuario> usuario = usuarioRepository.findById(id);
	
		return new ResponseEntity<Usuario>(usuario.get(), HttpStatus.OK);
		
	}
	
	@GetMapping(value = "v1/{id}", produces = "application/json")
	@Cacheable("cacheuser")
	public ResponseEntity<Usuario> buscarPorIdV1(@PathVariable(value = "id") Long id) {
		Usuario usuario = usuarioRepository.findById(id).get();
		System.out.println("Buscar por id V1");
		return ResponseEntity.ok(usuario);
	}

	@GetMapping(value = "v2/{id}", produces = "application/json")
	public ResponseEntity<Usuario> buscarPorIdV2(@PathVariable(value = "id") Long id) {
		Usuario usuario = usuarioRepository.findById(id).get();
		System.out.println("Buscar por id V2");
		return ResponseEntity.ok(usuario);
	}
	
	
	@GetMapping(value = "/v2", produces = "application/json", headers = "X-API-Version=v2")
	public ResponseEntity<List<Usuario>> buscarTodosV2() {
		List<Usuario> usuarios = (List<Usuario>) usuarioRepository.findAll();
		System.out.println("Buscar Todos V2");
		return new ResponseEntity<List<Usuario>>(usuarios, HttpStatus.OK);
	}	
	
	
	
	
	@GetMapping(value = "/", produces = "application/json")
	public ResponseEntity<Page<Usuario>> usuario () throws InterruptedException{
		
		PageRequest page = PageRequest.of(0, 5, Sort.by("nome"));
		
		Page<Usuario> list = usuarioRepository.findAll(page);
		
		return new ResponseEntity<Page<Usuario>>(list, HttpStatus.OK);
		
	}
	
	
	
	@GetMapping(value = "/page/{pagina}", produces = "application/json")
	public ResponseEntity<Page<Usuario>> usuarioPagina (@PathVariable("pagina") int pagina) throws InterruptedException{
		
		PageRequest page = PageRequest.of(pagina, 5, Sort.by("nome"));
		
		Page<Usuario> list = usuarioRepository.findAll(page);
		
		return new ResponseEntity<Page<Usuario>>(list, HttpStatus.OK);
		
	}
	
	
	
	
	@GetMapping(value = "/{id}/codigovenda/{venda}", produces = "application/json")
	public ResponseEntity<Usuario> relatorio(@PathVariable (value = "id") Long id
											, @PathVariable (value = "venda") Long venda) {
		
		Optional<Usuario> usuario = usuarioRepository.findById(id);
	
		return new ResponseEntity<Usuario>(usuario.get(), HttpStatus.OK);		
	}
	
	
	
	
	@PostMapping(value = "/", produces = "application/json")
	public ResponseEntity<Usuario> cadastrar(@RequestBody Usuario usuario){
		
		
		// amarrar filhos aos pais: telefones ao usuario
		for (int pos = 0; pos < usuario.getTelefones().size(); pos++) {
			usuario.getTelefones().get(pos).setUsuario(usuario);
		}
		
		
		String senhacriptografada = new BCryptPasswordEncoder().encode(usuario.getSenha());
		usuario.setSenha(senhacriptografada);
		
		Usuario usuarioSalvo = usuarioRepository.save(usuario);
		implementacaoUserDetailsService.insereAcessoPadrao(usuarioSalvo.getId());
		
		
		return new ResponseEntity<Usuario>(usuarioSalvo, HttpStatus.OK);
		
	}
	

	
	@PutMapping(value = "/", produces = "application/json")
	public ResponseEntity<Usuario> atualizar(@RequestBody Usuario usuario){
		
		// amarrar filhos aos pais: telefones ao usuario
				for (int pos = 0; pos < usuario.getTelefones().size(); pos++) {
					usuario.getTelefones().get(pos).setUsuario(usuario);
				}
				
		
				
		Usuario userTemporario = usuarioRepository.findUserByLogin(usuario.getLogin());
		
		if (!userTemporario.getSenha().equals(usuario.getSenha())) {
			String senhacriptografada = new BCryptPasswordEncoder().encode(usuario.getSenha());
			usuario.setSenha(senhacriptografada);
		}
		
		
		
		Usuario usuarioSalvo = usuarioRepository.save(usuario);
		
		
		return new ResponseEntity<Usuario>(usuarioSalvo, HttpStatus.OK);
		
	}
	
	
	/*
	@PutMapping(value = "/", produces = "application/json")
	public ResponseEntity<Usuario> atualizar(@RequestBody Usuario usuario){
		
		// amarrar filhos aos pais: telefones ao usuario
				for (int pos = 0; pos < usuario.getTelefones().size(); pos++) {
					usuario.getTelefones().get(pos).setUsuario(usuario);
				}
		
		String senhacriptografada = new BCryptPasswordEncoder().encode(usuario.getSenha());
		usuario.setSenha(senhacriptografada);
		
		Usuario usuarioSalvo = usuarioRepository.save(usuario);
		
		return new ResponseEntity<Usuario>(usuarioSalvo, HttpStatus.OK);
		
	}*/
	
	
	
	
	
	
	@PutMapping(value = "/{id}", produces = "application/json")
	public ResponseEntity<Usuario> atualizarSetId(@PathVariable("id") Long id, @RequestBody Usuario usuario) {
		usuario.setId(id);
		Usuario usuarioSalvo = usuarioRepository.save(usuario);
		return new ResponseEntity<Usuario>(usuarioSalvo, HttpStatus.OK);
	}
	
	
	@DeleteMapping(value = "/{id}", produces = "application/text")
	public String delete(@PathVariable("id") Long id) {
		usuarioRepository.deleteById(id);
		return "ok";
	}
	
	
	/*
	//END-POINT 
	@GetMapping(value = "/usuarioPorNome/{nome}", produces = "application/json")
	public ResponseEntity<List<Usuario>> usuarioPorNome (@PathVariable("nome") String nome) throws InterruptedException{
			
		List<Usuario> list = (List<Usuario>) usuarioRepository.findUserByNome(nome);
		
		return new ResponseEntity<List<Usuario>>(list, HttpStatus.OK);
		
	} */
	
	
	//END-POINT 
	@GetMapping(value = "/usuarioPorNome/{nome}", produces = "application/json")
	public ResponseEntity<Page<Usuario>> usuarioPorNome (@PathVariable("nome") String nome) throws InterruptedException{
		
		PageRequest pageRequest = null;
		Page<Usuario> list = null;
		
		if (nome == null || (nome != null && nome.trim().isEmpty()) || nome.equalsIgnoreCase("undefined")) {
			pageRequest = PageRequest.of(0, 5, Sort.by("nome"));
			list = usuarioRepository.findAll(pageRequest);
		} else {
			pageRequest = PageRequest.of(0, 5, Sort.by("nome"));
			list = usuarioRepository.findUserByNamePage(nome, pageRequest);
		}
						
		return new ResponseEntity<Page<Usuario>>(list, HttpStatus.OK);
		
	}
	
	
	
	//END-POINT 
		@GetMapping(value = "/usuarioPorNome/{nome}/page/{page}", produces = "application/json")
		public ResponseEntity<Page<Usuario>> usuarioPorNomePage (@PathVariable("nome") String nome, @PathVariable("page") int page) throws InterruptedException{
			
			PageRequest pageRequest = null;
			Page<Usuario> list = null;
			
			if (nome == null || (nome != null && nome.trim().isEmpty()) || nome.equalsIgnoreCase("undefined")) {
				pageRequest = PageRequest.of(page, 5, Sort.by("nome"));
				list = usuarioRepository.findAll(pageRequest);
			} else {
				pageRequest = PageRequest.of(page, 5, Sort.by("nome"));
				list = usuarioRepository.findUserByNamePage(nome, pageRequest);
			}
							
			return new ResponseEntity<Page<Usuario>>(list, HttpStatus.OK);
			
		}
	
	
	
	
	@Autowired
	private TelefoneRepository telefoneRepository;
	
	@DeleteMapping(value = "/removerTelefone/{id}", produces = "application/text")
	public String deleteTelefone(@PathVariable("id") Long id) {
		
		telefoneRepository.deleteById(id);
		return "ok";
		
	}
	
	

}
