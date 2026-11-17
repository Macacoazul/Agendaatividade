package com.example.agenda.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

/**
 * Classe auxiliar do SQLite: gerencia a criação e o versionamento do banco de dados.
 * Estende SQLiteOpenHelper, classe do Android para esse fim.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // propriedades como constantes - nome e versão do database
    private static final String DB_NAME = "contatos.db";
    private static final int DB_VERSION = 2;

    // nomes da tabela e das colunas (constantes evitam erros de digitação no DAO)
    public static final String TABELA_CONTATO = "contato";
    public static final String COLUNA_ID = "id";
    public static final String COLUNA_NOME = "nome";
    public static final String COLUNA_EMAIL = "email";
    public static final String COLUNA_TELEFONE = "telefone";
    public static final String COLUNA_FOTO = "foto";

    // construtor
    public DatabaseHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // onCreate - executado apenas quando o banco ainda não existe: cria a tabela
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABELA_CONTATO + " (" +
                COLUNA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUNA_NOME + " TEXT NOT NULL, " +
                COLUNA_EMAIL + " TEXT, " +
                COLUNA_TELEFONE + " TEXT NOT NULL, " +
                COLUNA_FOTO + " TEXT);");
    }

    // onUpgrade - executado quando o banco já existe e a versão (DB_VERSION) aumentou.
    // Cada "migration" aplica somente o que mudou, preservando os contatos já salvos.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // versão 2: adicionou a coluna foto
            db.execSQL("ALTER TABLE " + TABELA_CONTATO + " ADD COLUMN " + COLUNA_FOTO + " TEXT");
        }
    }
}
