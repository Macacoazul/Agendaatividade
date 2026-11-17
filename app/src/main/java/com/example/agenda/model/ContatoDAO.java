package com.example.agenda.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import static com.example.agenda.model.DatabaseHelper.COLUNA_EMAIL;
import static com.example.agenda.model.DatabaseHelper.COLUNA_FOTO;
import static com.example.agenda.model.DatabaseHelper.COLUNA_ID;
import static com.example.agenda.model.DatabaseHelper.COLUNA_NOME;
import static com.example.agenda.model.DatabaseHelper.COLUNA_TELEFONE;
import static com.example.agenda.model.DatabaseHelper.TABELA_CONTATO;

/**
 * DAO (Data Access Object): concentra as operações de banco de dados (CRUD)
 * da entidade Contato, separando o acesso a dados da lógica de negócio.
 */
public class ContatoDAO {

    private SQLiteDatabase database;
    private final DatabaseHelper databaseHelper; // nossa classe helper SQLite

    // construtor - instancia o helper
    public ContatoDAO(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    // abre o database para escrita
    public void open() {
        database = databaseHelper.getWritableDatabase();
    }

    // fecha o database
    public void close() {
        databaseHelper.close();
    }

    // CREATE - insere um novo contato e devolve o id gerado (-1 em caso de erro)
    public long adicionarContato(Contato contato) {
        ContentValues values = paraValores(contato);
        return database.insert(TABELA_CONTATO, null, values);
    }

    // READ - SELECT de todos os contatos, em ordem alfabética
    public List<Contato> listarContatos() {
        List<Contato> contatos = new ArrayList<>();
        Cursor cursor = database.query(TABELA_CONTATO, null, null, null, null, null,
                COLUNA_NOME + " COLLATE NOCASE ASC");
        try {
            if (cursor.moveToFirst()) {
                do {
                    contatos.add(lerContato(cursor)); // cria um Contato com os valores da linha
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return contatos;
    }

    // READ - SELECT de um contato pelo id
    public Contato buscaContatoPorId(int id) {
        Cursor cursor = database.query(TABELA_CONTATO, null, COLUNA_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return lerContato(cursor);
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    // UPDATE - atualiza os dados de um contato (devolve o número de linhas alteradas)
    public int atualizarContato(Contato contato) {
        ContentValues values = paraValores(contato);
        // SQL parametrizado ("id=?") evita SQL injection
        return database.update(TABELA_CONTATO, values, COLUNA_ID + "=?",
                new String[]{String.valueOf(contato.getId())});
    }

    // DELETE - apaga um contato pelo id
    public void apagarContato(int id) {
        database.delete(TABELA_CONTATO, COLUNA_ID + "=?", new String[]{String.valueOf(id)});
    }

    // Converte um Contato em ContentValues (pares coluna -> valor)
    private ContentValues paraValores(Contato contato) {
        ContentValues values = new ContentValues();
        values.put(COLUNA_NOME, contato.getNome());
        values.put(COLUNA_EMAIL, contato.getEmail());
        values.put(COLUNA_TELEFONE, contato.getTelefone());
        values.put(COLUNA_FOTO, contato.getFoto());
        return values;
    }

    // Converte a linha atual do cursor em um objeto Contato
    private Contato lerContato(Cursor cursor) {
        Contato contato = new Contato();
        contato.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUNA_ID)));
        contato.setNome(cursor.getString(cursor.getColumnIndexOrThrow(COLUNA_NOME)));
        contato.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUNA_EMAIL)));
        contato.setTelefone(cursor.getString(cursor.getColumnIndexOrThrow(COLUNA_TELEFONE)));
        contato.setFoto(cursor.getString(cursor.getColumnIndexOrThrow(COLUNA_FOTO)));
        return contato;
    }
}
