package com.example.agenda.model;

import java.io.Serializable;

/**
 * Entidade (Bean) que representa um contato. Cada propriedade corresponde a uma
 * coluna da tabela "contato" no banco SQLite. Implementa Serializable para poder
 * ser enviada de uma Activity para outra dentro de um Intent (putExtra).
 */
public class Contato implements Serializable {

    // propriedades - correspondem aos atributos da tabela no DB
    private int id;
    private String nome;
    private String email;
    private String telefone;
    private String foto; // caminho do arquivo da foto no armazenamento do app

    // construtor vazio
    public Contato() {
    }

    // construtor para um contato novo (ainda sem id)
    public Contato(String nome, String email, String telefone, String foto) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.foto = foto;
    }

    // construtor para um contato já existente (com id) - usado no update
    public Contato(int id, String nome, String email, String telefone, String foto) {
        this(nome, email, telefone, foto);
        this.id = id;
    }

    // métodos getter e setter - para cada propriedade
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    /** Indica se o contato possui uma foto salva. */
    public boolean temFoto() {
        return foto != null && !foto.isEmpty();
    }

    /** Iniciais do nome (ex.: "Ana Souza" -> "AS"), usadas quando não há foto. */
    public String getIniciais() {
        if (nome == null || nome.trim().isEmpty()) return "?";
        String[] partes = nome.trim().split("\\s+");
        String iniciais = partes[0].substring(0, 1);
        if (partes.length > 1) {
            iniciais += partes[partes.length - 1].substring(0, 1);
        }
        return iniciais.toUpperCase();
    }

    @Override
    public String toString() {
        return nome;
    }
}
