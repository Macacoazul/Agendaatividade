package com.example.agenda.controller;

import android.content.Context;
import android.util.Patterns;

import com.example.agenda.model.Contato;
import com.example.agenda.model.ContatoDAO;
import com.example.agenda.util.FotoUtil;

import java.util.List;

/**
 * Controller (MVC): intermediário entre a View (Activities) e o Model (ContatoDAO).
 * Contém as regras de negócio; o SQL fica no Model.
 */
public class ContatoController {

    // Resultados possíveis da validação
    public static final int VALIDO = 0;
    public static final int ERRO_NOME = 1;
    public static final int ERRO_TELEFONE = 2;
    public static final int ERRO_EMAIL = 3;

    private final ContatoDAO contatoDAO;

    public ContatoController(Context context) {
        contatoDAO = new ContatoDAO(context);
        contatoDAO.open();
    }

    // Regra de negócio: nome e telefone são obrigatórios; o e-mail, se informado, precisa ser válido
    public int validarContato(String nome, String email, String telefone) {
        if (nome == null || nome.trim().isEmpty()) return ERRO_NOME;
        if (telefone == null || telefone.trim().isEmpty()) return ERRO_TELEFONE;
        if (email != null && !email.trim().isEmpty()
                && !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) return ERRO_EMAIL;
        return VALIDO;
    }

    // ações do CRUD - lembrar que o SQL está no model
    // CREATE
    public long adicionarContato(String nome, String email, String telefone, String foto) {
        Contato contato = new Contato(nome, email, telefone, foto);
        return contatoDAO.adicionarContato(contato);
    }

    // READ
    public List<Contato> listarContatos() {
        return contatoDAO.listarContatos();
    }

    public Contato buscarContatoPorId(int id) {
        return contatoDAO.buscaContatoPorId(id);
    }

    // UPDATE
    public int atualizarContato(int id, String nome, String email, String telefone, String foto) {
        Contato anterior = contatoDAO.buscaContatoPorId(id);
        Contato contato = new Contato(id, nome, email, telefone, foto);
        int linhas = contatoDAO.atualizarContato(contato);

        // Regra: se a foto foi trocada ou removida, apaga o arquivo antigo do armazenamento
        if (linhas > 0 && anterior != null && anterior.temFoto() && !anterior.getFoto().equals(foto)) {
            FotoUtil.apagar(anterior.getFoto());
        }
        return linhas;
    }

    // DELETE
    public void apagarContato(int id) {
        Contato contato = contatoDAO.buscaContatoPorId(id);
        contatoDAO.apagarContato(id);
        // Regra: ao excluir o contato, a foto dele também é apagada
        if (contato != null && contato.temFoto()) {
            FotoUtil.apagar(contato.getFoto());
        }
    }

    public void close() {
        contatoDAO.close();
    }
}
