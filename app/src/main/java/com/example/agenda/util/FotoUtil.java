package com.example.agenda.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Salva e carrega as fotos dos contatos no armazenamento específico do app
 * (App-specific storage: pasta interna "files/fotos", acessível só por este app).
 * No banco de dados fica guardado apenas o caminho do arquivo.
 */
public final class FotoUtil {

    private static final int TAMANHO_MAXIMO = 512; // pixels (lado maior)

    private FotoUtil() {
    }

    /** Copia a imagem escolhida na galeria (Uri) para a pasta do app. Devolve o caminho ou null. */
    public static String salvarFoto(Context context, Uri uri) {
        try {
            // 1ª leitura: só as dimensões, para calcular a redução
            BitmapFactory.Options opcoes = new BitmapFactory.Options();
            opcoes.inJustDecodeBounds = true;
            try (InputStream entrada = context.getContentResolver().openInputStream(uri)) {
                BitmapFactory.decodeStream(entrada, null, opcoes);
            }
            int reducao = 1;
            while (Math.max(opcoes.outWidth, opcoes.outHeight) / (reducao * 2) >= TAMANHO_MAXIMO) {
                reducao *= 2;
            }

            // 2ª leitura: a imagem já reduzida
            BitmapFactory.Options opcoesReduzidas = new BitmapFactory.Options();
            opcoesReduzidas.inSampleSize = reducao;
            Bitmap bitmap;
            try (InputStream entrada = context.getContentResolver().openInputStream(uri)) {
                bitmap = BitmapFactory.decodeStream(entrada, null, opcoesReduzidas);
            }
            return bitmap == null ? null : salvarFoto(context, bitmap);
        } catch (IOException | SecurityException e) {
            return null;
        }
    }

    /** Grava um Bitmap (ex.: foto da câmera) como JPEG na pasta do app. Devolve o caminho ou null. */
    public static String salvarFoto(Context context, Bitmap bitmap) {
        Bitmap reduzido = reduzir(bitmap);
        File pasta = new File(context.getFilesDir(), "fotos");
        if (!pasta.exists() && !pasta.mkdirs()) return null;

        File arquivo = new File(pasta, "contato_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream saida = new FileOutputStream(arquivo)) {
            reduzido.compress(Bitmap.CompressFormat.JPEG, 85, saida);
            return arquivo.getAbsolutePath();
        } catch (IOException e) {
            return null;
        }
    }

    /** Lê a foto salva. Devolve null se o arquivo não existir mais. */
    public static Bitmap carregar(String caminho) {
        if (caminho == null || caminho.isEmpty()) return null;
        return BitmapFactory.decodeFile(caminho);
    }

    /** Apaga o arquivo da foto, se existir. */
    public static void apagar(String caminho) {
        if (caminho == null || caminho.isEmpty()) return;
        File arquivo = new File(caminho);
        if (arquivo.exists()) {
            //noinspection ResultOfMethodCallIgnored
            arquivo.delete();
        }
    }

    private static Bitmap reduzir(Bitmap bitmap) {
        int maior = Math.max(bitmap.getWidth(), bitmap.getHeight());
        if (maior <= TAMANHO_MAXIMO) return bitmap;
        float escala = (float) TAMANHO_MAXIMO / maior;
        return Bitmap.createScaledBitmap(bitmap,
                Math.round(bitmap.getWidth() * escala), Math.round(bitmap.getHeight() * escala), true);
    }
}
