package com.example.sistema_controle_de_acessos.service;

import com.example.sistema_controle_de_acessos.exception.AcessoNegadoException;
import com.example.sistema_controle_de_acessos.exception.LoginInvalidoException;
import com.example.sistema_controle_de_acessos.exception.UsuarioBloqueadoException;
import com.example.sistema_controle_de_acessos.model.StatusUsuario;
import com.example.sistema_controle_de_acessos.model.TipoUsuario;
import com.example.sistema_controle_de_acessos.model.Usuario;
import com.example.sistema_controle_de_acessos.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
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

        if (senha == null)
            throw new AcessoNegadoException("\nSenha inválida. Tente Novamente");

        if (nome == null)
            throw new AcessoNegadoException("\nNome inválido. Tente Novamente");

        if (usuario == null)
            throw new AcessoNegadoException("\nUsuário inválido. Tente Novamente");

        if (email == null)
            throw new AcessoNegadoException("\nEmail não pode ser vazio");

        Optional<Usuario> findByEmail = usuarioRepository.findByEmail(email);

        if (findByEmail.isPresent())
            throw new AcessoNegadoException("\nEsse email já está cadastrado. Tente Novamente");

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
            throw new LoginInvalidoException("\nE-mail ou senha incorretos");

        return usuario;

    }

    public void alternarStatusUsuario(Long idUsuarioDestino, Usuario executor){

        if (idUsuarioDestino == null)
            throw new AcessoNegadoException("\nId inválido");

        if (executor.getTipoUsuario() != TipoUsuario.ADMIN)
            throw new AcessoNegadoException("\nApenas administradores podem gerenciar usuários");

        Optional<Usuario> findById = usuarioRepository.findById(idUsuarioDestino);

        if (findById.isEmpty())
            throw new RuntimeException("\nUsuário não encontrado");

        Usuario usuario = findById.get();

        if (executor.getId().equals(idUsuarioDestino))
            throw new RuntimeException("\nVocê não pode bloquear a si mesmo");

        if (usuario.getStatusUsuario() == StatusUsuario.ATIVO) {

            usuario.setStatusUsuario(StatusUsuario.BLOQUEADO);

        } else {

            usuario.setStatusUsuario(StatusUsuario.ATIVO);

        }

        usuarioRepository.save(usuario);

    }

    public String listarTodosUsuarios(Usuario executor){

        if (executor.getTipoUsuario() != TipoUsuario.ADMIN)
            throw new RuntimeException("\nSomemte ADMINs podem realizar essa ação");

        List<Usuario> usuarioList = usuarioRepository.findAll();

        if (usuarioList.isEmpty())
            throw new RuntimeException("\nA lista está vazia");

        StringBuilder resultUserList = new StringBuilder();

        for (Usuario listaUsuario : usuarioList){

            resultUserList.append("\nId: ").append(listaUsuario.getId());
            resultUserList.append("\nNome: ").append(listaUsuario.getNome());
            resultUserList.append("\nEmail: ").append(listaUsuario.getEmail());
            resultUserList.append("\nStatus: ").append(listaUsuario.getStatusUsuario());
            resultUserList.append("\nTipo: ").append(listaUsuario.getTipoUsuario());

        }

        return resultUserList.toString();

    }

    public void trocaDeSenha(Usuario logado, String senhaAntiga, String novaSenha){

        if (!passwordEncoder.matches(senhaAntiga, logado.getSenha()))
            throw new RuntimeException("Senha incorreta. Tente Novamente");

        if (novaSenha.isBlank() || novaSenha.length() < 4)
            throw new RuntimeException("A nova senha deve ter pelo menos 4 caracteres.");

        String senhaNovaCriptografada = passwordEncoder.encode(novaSenha);
        logado.setSenha(senhaNovaCriptografada);

        usuarioRepository.save(logado);

    }

    public void deletarUser(Usuario user){

        if (user.getTipoUsuario() == TipoUsuario.ADMIN)
            throw new RuntimeException("ADMs não podem ser deletados");

        usuarioRepository.delete(user);

    }



}
