package puc.myapplication;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;

import puc.myapplication.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private final ArrayList<Bug> bugs = new ArrayList<>();
    private BugAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        int paddingLeft = binding.getRoot().getPaddingLeft();
        int paddingTop = binding.getRoot().getPaddingTop();
        int paddingRight = binding.getRoot().getPaddingRight();
        int paddingBottom = binding.getRoot().getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets keyboard = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(paddingLeft + systemBars.left, paddingTop + systemBars.top,
                    paddingRight + systemBars.right,
                    paddingBottom + Math.max(systemBars.bottom, keyboard.bottom));
            return insets;
        });
        ViewCompat.requestApplyInsets(binding.getRoot());
        binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(this::ajustarAlturaFormulario);
        binding.recyclerBugs.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BugAdapter(bugs, this::alternarResolucao, this::excluirBug);
        binding.recyclerBugs.setAdapter(adapter);
        binding.buttonRegisterBug.setOnClickListener(view -> registrarBug());
        atualizarPainel();
        atualizarGravidade(binding.seekGravidade.getProgress());
        binding.seekGravidade.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                atualizarGravidade(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    private void atualizarGravidade(int gravidade) {
        binding.textGravidade.setText(getString(R.string.gravidade_valor, gravidade));
    }

    private void ajustarAlturaFormulario() {
        // Reserva espaço à lista; só o formulário pode rolar quando o teclado reduz a tela.
        int reservaLista = Math.round(96 * getResources().getDisplayMetrics().density);
        int minimoFormulario = Math.round(48 * getResources().getDisplayMetrics().density);
        ViewGroup.MarginLayoutParams painel = (ViewGroup.MarginLayoutParams) binding.panelCounters.getLayoutParams();
        ViewGroup.MarginLayoutParams titulo = (ViewGroup.MarginLayoutParams) binding.textBugsListTitle.getLayoutParams();
        ViewGroup.MarginLayoutParams lista = (ViewGroup.MarginLayoutParams) binding.recyclerBugs.getLayoutParams();
        int alturaDisponivel = binding.getRoot().getHeight() - binding.getRoot().getPaddingBottom()
                - binding.scrollBugForm.getTop() - painel.topMargin - binding.panelCounters.getHeight()
                - titulo.topMargin - binding.textBugsListTitle.getHeight() - lista.topMargin - reservaLista;
        int altura = Math.min(binding.formBug.getMeasuredHeight(), Math.max(minimoFormulario, alturaDisponivel));
        ViewGroup.LayoutParams parametros = binding.scrollBugForm.getLayoutParams();
        if (altura > 0 && parametros.height != altura) {
            parametros.height = altura;
            binding.scrollBugForm.setLayoutParams(parametros);
        }
    }

    private void registrarBug() {
        String titulo = binding.inputBugTitle.getText().toString().trim();
        String modulo = binding.inputBugModule.getText().toString().trim();
        String descricao = binding.inputBugDescription.getText().toString().trim();
        binding.inputBugTitle.setError(null);
        binding.inputBugModule.setError(null);
        binding.inputBugDescription.setError(null);
        View primeiroErro = null;
        if (titulo.isEmpty()) {
            binding.inputBugTitle.setError(getString(R.string.error_title_required));
            primeiroErro = binding.inputBugTitle;
        }
        if (modulo.isEmpty()) {
            binding.inputBugModule.setError(getString(R.string.error_module_required));
            if (primeiroErro == null) primeiroErro = binding.inputBugModule;
        }
        if (descricao.length() < 10) {
            binding.inputBugDescription.setError(getString(R.string.error_description_short));
            if (primeiroErro == null) primeiroErro = binding.inputBugDescription;
        }
        if (primeiroErro != null) {
            primeiroErro.requestFocus();
            return;
        }

        Bug bug = new Bug(titulo, modulo, descricao, binding.seekGravidade.getProgress(), R.drawable.ic_bug);
        int posicao = bugs.size();
        bugs.add(bug);
        adapter.notifyItemInserted(posicao);
        binding.recyclerBugs.scrollToPosition(posicao);
        binding.inputBugTitle.getText().clear();
        binding.inputBugModule.getText().clear();
        binding.inputBugDescription.getText().clear();
        binding.seekGravidade.setProgress(1);
        binding.inputBugTitle.clearFocus();
        binding.inputBugModule.clearFocus();
        binding.inputBugDescription.clearFocus();
        new WindowInsetsControllerCompat(getWindow(), binding.getRoot())
                .hide(WindowInsetsCompat.Type.ime());
        atualizarPainel();
    }

    private void alternarResolucao(Bug bug) {
        int posicao = bugs.indexOf(bug);
        if (posicao < 0) return;
        bug.setCorrigido(!bug.isCorrigido());
        adapter.notifyItemChanged(posicao);
        atualizarPainel();
    }

    private void excluirBug(Bug bug) {
        int posicao = bugs.indexOf(bug);
        if (posicao < 0) return;
        bugs.remove(posicao);
        adapter.notifyItemRemoved(posicao);
        atualizarPainel();
        Toast.makeText(this, getString(R.string.bug_removed, bug.getCodigo()), Toast.LENGTH_SHORT).show();
    }

    private void atualizarPainel() {
        int corrigidos = 0;
        int pontos = 0;
        for (Bug bug : bugs) {
            if (bug.isCorrigido()) {
                corrigidos++;
                pontos += bug.getGravidade() * 10;
            }
        }
        binding.textTotalBugs.setText(getString(R.string.counter_value, bugs.size()));
        binding.textOpenBugs.setText(getString(R.string.counter_value, bugs.size() - corrigidos));
        binding.textResolvedBugs.setText(getString(R.string.counter_value, corrigidos));
        binding.textHunterScore.setText(getString(R.string.counter_value, pontos));
        binding.textEmptyBugs.setVisibility(bugs.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
