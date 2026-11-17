package com.example.agenda;

import android.content.ActivityNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.agenda.controller.ContatoController;
import com.example.agenda.model.Contato;
import com.example.agenda.util.FotoUtil;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * View (MVC): formulário de contato.
 * - Insert: o formulário inicia vazio;
 * - Update: recebe um Contato pelo Intent e inicia preenchido.
 */
public class AdicionarContatoActivity extends AppCompatActivity {

    public static final String EXTRA_CONTATO = "contato";
    private static final String ESTADO_FOTO = "foto";
    private static final String ESTADO_FOTO_TEMP = "foto_temporaria";

    private MaterialToolbar toolbar2;
    private TextInputLayout layoutNome, layoutEmail, layoutTelefone;
    private TextInputEditText editNome, editEmail, editTelefone;
    private Button btnSalvar, btnSelecionarFoto;
    private ShapeableImageView imageViewFoto;

    private ContatoController contatoController;
    private Contato contatoEdicao;   // null = novo contato
    private String caminhoFoto;      // foto atual do formulário
    private String fotoTemporaria;   // foto escolhida agora e ainda não salva no banco
    private boolean salvo = false;

    // Seletor de fotos do Android (galeria) - não exige permissão de armazenamento
    private final ActivityResultLauncher<PickVisualMediaRequest> seletorGaleria =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) definirNovaFoto(FotoUtil.salvarFoto(this, uri));
            });

    // Intent implícita para o app de câmera (comunicação entre apps)
    private final ActivityResultLauncher<Void> camera =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) definirNovaFoto(FotoUtil.salvarFoto(this, bitmap));
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT));
        setContentView(R.layout.activity_adicionar_contato);

        contatoController = new ContatoController(this);
        inicializarViews();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(barras.left, 0, barras.right, barras.bottom);
            toolbar2.setPadding(0, barras.top, 0, 0);
            return insets;
        });

        verificarEdicao();

        if (savedInstanceState != null) { // ex.: a tela foi girada
            caminhoFoto = savedInstanceState.getString(ESTADO_FOTO);
            fotoTemporaria = savedInstanceState.getString(ESTADO_FOTO_TEMP);
        }
        mostrarFoto();
    }

    public void inicializarViews() {
        toolbar2 = findViewById(R.id.toolbar2);
        layoutNome = findViewById(R.id.layoutNome);
        layoutEmail = findViewById(R.id.layoutEmail);
        layoutTelefone = findViewById(R.id.layoutTelefone);
        editNome = findViewById(R.id.editTextNome);
        editEmail = findViewById(R.id.editTextEmail);
        editTelefone = findViewById(R.id.editTextTelefone);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnSelecionarFoto = findViewById(R.id.btnSelecionarFoto);
        imageViewFoto = findViewById(R.id.imageView);

        // formata o telefone enquanto o usuário digita, no padrão brasileiro
        editTelefone.addTextChangedListener(new PhoneNumberFormattingTextWatcher("BR"));

        toolbar2.setNavigationOnClickListener(v -> finish());
        btnSalvar.setOnClickListener(v -> salvarContato());
        btnSelecionarFoto.setOnClickListener(v -> escolherOrigemFoto());
        imageViewFoto.setOnClickListener(v -> escolherOrigemFoto());
    }

    /** Se a tela recebeu um Contato pelo Intent, é uma alteração: preenche o formulário. */
    private void verificarEdicao() {
        contatoEdicao = IntentCompat.getSerializableExtra(getIntent(), EXTRA_CONTATO, Contato.class);
        if (contatoEdicao != null) {
            toolbar2.setTitle(R.string.titulo_editar);
            editNome.setText(contatoEdicao.getNome());
            editEmail.setText(contatoEdicao.getEmail());
            editTelefone.setText(contatoEdicao.getTelefone());
            caminhoFoto = contatoEdicao.getFoto();
            btnSalvar.setText(R.string.salvar_alteracoes);
        }
    }

    private void escolherOrigemFoto() {
        List<String> opcoes = new ArrayList<>();
        opcoes.add(getString(R.string.foto_galeria));
        opcoes.add(getString(R.string.foto_camera));
        boolean temFoto = caminhoFoto != null && !caminhoFoto.isEmpty();
        if (temFoto) opcoes.add(getString(R.string.foto_remover));

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.selecionar_foto)
                .setItems(opcoes.toArray(new String[0]), (dialog, which) -> {
                    if (which == 0) {
                        seletorGaleria.launch(new PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                .build());
                    } else if (which == 1) {
                        try {
                            camera.launch(null);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(this, R.string.sem_camera, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        trocarFoto(null); // remover foto
                    }
                })
                .show();
    }

    /** Usa a foto recém-copiada para a pasta do app (null = falhou ao ler a imagem). */
    private void definirNovaFoto(String caminho) {
        if (caminho == null) {
            Toast.makeText(this, R.string.erro_foto, Toast.LENGTH_SHORT).show();
            return;
        }
        trocarFoto(caminho);
    }

    /** Troca a foto do formulário (null = sem foto), descartando uma foto temporária anterior. */
    private void trocarFoto(String caminho) {
        if (fotoTemporaria != null) FotoUtil.apagar(fotoTemporaria);
        fotoTemporaria = caminho;
        caminhoFoto = caminho;
        mostrarFoto();
    }

    private void mostrarFoto() {
        Bitmap foto = FotoUtil.carregar(caminhoFoto);
        if (foto != null) {
            imageViewFoto.setImageBitmap(foto);
            btnSelecionarFoto.setText(R.string.trocar_foto);
        } else {
            imageViewFoto.setImageResource(R.drawable.foto_padrao);
            btnSelecionarFoto.setText(R.string.selecionar_foto);
        }
    }

    public void salvarContato() {
        // recuperar os valores dos campos
        String nome = texto(editNome);
        String email = texto(editEmail);
        String telefone = texto(editTelefone);

        // validação (regra de negócio no Controller)
        layoutNome.setError(null);
        layoutEmail.setError(null);
        layoutTelefone.setError(null);
        int resultado = contatoController.validarContato(nome, email, telefone);
        if (resultado != ContatoController.VALIDO) {
            if (resultado == ContatoController.ERRO_NOME) layoutNome.setError(getString(R.string.erro_nome));
            if (resultado == ContatoController.ERRO_TELEFONE) layoutTelefone.setError(getString(R.string.erro_telefone));
            if (resultado == ContatoController.ERRO_EMAIL) layoutEmail.setError(getString(R.string.erro_email));
            Toast.makeText(this, resultado == ContatoController.ERRO_EMAIL
                    ? R.string.erro_email : R.string.obrigatorios, Toast.LENGTH_SHORT).show();
            return;
        }

        // aciona o Controller: inserir (novo) ou atualizar (edição)
        boolean sucesso;
        if (contatoEdicao == null) {
            sucesso = contatoController.adicionarContato(nome, email, telefone, caminhoFoto) != -1;
            if (sucesso) Toast.makeText(this, R.string.contato_adicionado, Toast.LENGTH_SHORT).show();
        } else {
            sucesso = contatoController.atualizarContato(contatoEdicao.getId(), nome, email, telefone, caminhoFoto) > 0;
            if (sucesso) Toast.makeText(this, R.string.contato_atualizado, Toast.LENGTH_SHORT).show();
        }

        if (!sucesso) {
            Toast.makeText(this, R.string.erro_salvar, Toast.LENGTH_SHORT).show();
            return;
        }
        salvo = true;
        finish();
    }

    private String texto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ESTADO_FOTO, caminhoFoto);
        outState.putString(ESTADO_FOTO_TEMP, fotoTemporaria);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // saiu sem salvar: descarta a foto escolhida nesta tela
        if (!salvo && !isChangingConfigurations() && fotoTemporaria != null) {
            FotoUtil.apagar(fotoTemporaria);
        }
        contatoController.close();
    }
}
