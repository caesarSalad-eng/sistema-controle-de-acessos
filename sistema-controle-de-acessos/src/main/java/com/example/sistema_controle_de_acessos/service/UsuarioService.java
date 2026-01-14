package com.example.sistema_controle_de_acessos.service;

import com.example.sistema_controle_de_acessos.exception.AcessoNegadoException;
import com.example.sistema_controle_de_acessos.exception.LoginInvalidoException;
import com.example.sistema_controle_de_acessos.exception.UsuarioBloqueadoException;
import com.example.sistema_controle_de_acessos.model.StatusUsuario;
import com.example.sistema_controle_de_acessos.model.Usuario;
import com.example.sistema_controle_de_acessos.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private final UsuarioRepository usuarioRepository;

    @Autowired
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, BCryptPasswordEncoder passwordEncoder ){

        this.usuarioRepository = usuarioRepository;

        this.passwordEncoder = passwordEncoder;

    }

    public void cadastrarUsuario(String senha, String nome, String email, Usuario usuario){

        if (senha == null) throw new AcessoNegadoException("\nSenha inválida. Tente Novamente");

        if (nome == null) throw new AcessoNegadoException("\nNome inválido. Tente Novamente");

        if (usuario == null) throw new AcessoNegadoException("\nUsuário inválido. Tente Novamente");

        if (email == null) throw new AcessoNegadoException("\nEmail não pode ser vazio");

        Optional<Usuario> findByEmail = usuarioRepository.findByEmail(email);

        if (findByEmail.isPresent()) throw new AcessoNegadoException("\nEsse email já está cadastrado. Tente Novamente");

        usuario.setNome(nome.toUpperCase().trim());
        usuario.setEmail(email.toLowerCase().trim());
        usuario.setSenha(senha);
        usuario.setStatusUsuario(StatusUsuario.ATIVO);

        usuarioRepository.save(usuario);

    }

    public Usuario loginUsuario(String email, String senha){

        if(email.isBlank() || senha.isBlank())
            throw new LoginInvalidoException("\nEmail ou Senha inválidos. Tente Novamente");

        Optional<Usuario> findByEmail = usuarioRepository.findByEmail(email);

        if (findByEmail.isEmpty())
            throw new LoginInvalidoException("\nEmail ou Senha inválidos. Tente Novamente");

        Usuario usuario = findByEmail.get();

        if (usuario.getStatusUsuario() == StatusUsuario.BLOQUEADO)
            throw new UsuarioBloqueadoException("\nConta bloqueada. Procure um administrador");

        if (!passwordEncoder.matches(senha, usuario.getSenha()))
            throw new LoginInvalidoException("E-mail ou senha incorretos");

        return usuario;

    }




}
