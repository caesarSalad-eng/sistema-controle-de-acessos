package com.example.sistema_controle_de_acessos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Getter
    @Setter
    @Column(nullable = false)
    private String nome;

    @Getter
    @Setter
    @Column(unique = true, nullable = false)
    private String email;

    @Getter
    @Setter
    @Column(nullable = false)
    private String senha;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    StatusUsuario statusUsuario;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    TipoUsuario tipoUsuario;

}
