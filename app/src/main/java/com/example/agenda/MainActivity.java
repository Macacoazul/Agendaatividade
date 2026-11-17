package com.example.agenda;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.agenda.controller.ContatoController;
import com.example.agenda.model.Contato;
import com.example.agenda.util.FotoUtil;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * View (MVC): apresenta a lista de contatos.
 * - Clique no botão flutuante: abre o formulário vazio (inclusão);
 * - Clique em um contato: abre o formulário preenchido (alteração);
 * - Clique longo em um contato: confirma e exclui.
 */
public class MainActivity extends AppCompatActivity {

    // Cores usadas no círculo das iniciais quando o contato não tem foto
    private static final int[] CORES_AVATAR = {
            0xFF6750A4, 0xFF00897B, 0xFFE8710A, 0xFF1E88E5, 0xFFD81B60, 0xFF43A047, 0xFF8E24AA
    };

    // propriedades referentes aos componentes View no layout
    private MaterialToolbar toolbar;
    private ListView listView;
    private FloatingActionButton fabAdicionar;
    private List<Contato> contatos;
    private ArrayAdapter<Contato> adapter;
    private ContatoController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Barra de status com ícones claros, combinando com a toolbar roxa
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT));
        setContentView(R.layout.activity_main);

        controller = new ContatoController(this);
        inicializarViews();
        configurarListeners();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            toolbar.setPadding(0, systemBars.top, 0, 0); // a toolbar ocupa a área da barra de status
            return insets;
        });
    }

    private void inicializarViews() {
        // inicializar os componentes
        toolbar = findViewById(R.id.toolbar);
        listView = findViewById(R.id.listView);
        fabAdicionar = findViewById(R.id.fabAdicionar);
        listView.setEmptyView(findViewById(R.id.layoutVazio));

        // Intent explícita: abre a tela de inclusão
        fabAdicionar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdicionarContatoActivity.class);
            startActivity(intent);
        });
    }

    private void carregarContatos() {
        // pede ao Controller a lista de contatos (o Controller consulta o Model)
        contatos = controller.listarContatos();

        // o Adapter "transforma" cada Contato em um item da ListView (list_item.xml)
        adapter = new ArrayAdapter<Contato>(this, R.layout.list_item, contatos) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.list_item, parent, false);
                }
                Contato contato = getItem(position);
                if (contato == null) return convertView;

                // escrever nos componentes do layout do item
                TextView textNome = convertView.findViewById(R.id.textNomeLista);
                TextView textTelefone = convertView.findViewById(R.id.textTelLista);
                TextView textEmail = convertView.findViewById(R.id.textEmailLista);
                TextView textIniciais = convertView.findViewById(R.id.textIniciais);
                ImageView imageFoto = convertView.findViewById(R.id.imageView2);
                ImageView imgLigar = convertView.findViewById(R.id.imgLigar);

                textNome.setText(contato.getNome());
                textTelefone.setText(contato.getTelefone());

                boolean temEmail = contato.getEmail() != null && !contato.getEmail().isEmpty();
                textEmail.setVisibility(temEmail ? View.VISIBLE : View.GONE);
                textEmail.setText(contato.getEmail());

                Bitmap foto = contato.temFoto() ? FotoUtil.carregar(contato.getFoto()) : null;
                if (foto != null) {
                    imageFoto.setImageBitmap(foto);
                    imageFoto.setVisibility(View.VISIBLE);
                    textIniciais.setVisibility(View.INVISIBLE);
                } else {
                    imageFoto.setVisibility(View.GONE);
                    textIniciais.setVisibility(View.VISIBLE);
                    textIniciais.setText(contato.getIniciais());
                    int cor = CORES_AVATAR[Math.abs(contato.getNome().hashCode()) % CORES_AVATAR.length];
                    textIniciais.setBackgroundTintList(ColorStateList.valueOf(cor));
                }

                imgLigar.setOnClickListener(v -> ligar(contato));
                return convertView;
            }
        };
        listView.setAdapter(adapter);

        toolbar.setSubtitle(getResources().getQuantityString(
                R.plurals.quantidade_contatos, contatos.size(), contatos.size()));
    }

    private void configurarListeners() {
        // clique curto em item do listView - alterar contato
        listView.setOnItemClickListener((parent, view, position, id) -> {
            // position dá a posição do item selecionado
            Contato contato = contatos.get(position);
            // abre a activity enviando o contato (Serializable) como extra do Intent
            Intent intent = new Intent(MainActivity.this, AdicionarContatoActivity.class);
            intent.putExtra(AdicionarContatoActivity.EXTRA_CONTATO, contato);
            startActivity(intent);
        });

        // clique longo em item do listView - excluir contato
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            Contato contato = contatos.get(position);
            // alerta de confirmação de exclusão
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.confirmar_exclusao)
                    .setMessage(getString(R.string.mensagem_exclusao, contato.getNome()))
                    .setPositiveButton(R.string.sim, (dialog, which) -> {
                        controller.apagarContato(contato.getId());
                        carregarContatos(); // atualizar a listView
                        Toast.makeText(this, R.string.contato_excluido, Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(R.string.nao, null)
                    .show();
            return true;
        });
    }

    /** Intent implícita: pede ao Android um app capaz de discar (comunicação entre apps). */
    private void ligar(Contato contato) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(contato.getTelefone())));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.sem_discador, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarContatos(); // ao voltar para esta activity, recarrega a lista
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        controller.close();
    }
}
