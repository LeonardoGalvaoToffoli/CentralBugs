package puc.myapplication;

import android.graphics.Paint;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

import puc.myapplication.databinding.ActivityMainBinding;
import puc.myapplication.databinding.ItemBugBinding;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class BugFlowTest {
    private ActivityScenario<MainActivity> scenario;

    @Before
    public void abrirTela() {
        scenario = ActivityScenario.launch(MainActivity.class);
    }

    @After
    public void fecharTela() {
        scenario.close();
    }

    private void naTela(Consumer<ActivityMainBinding> acao) {
        onView(withId(R.id.main)).perform(new ViewAction() {
            @Override public Matcher<View> getConstraints() {
                return isAssignableFrom(ConstraintLayout.class);
            }
            @Override public String getDescription() { return "Verificar fluxo da CentralBugs"; }
            @Override public void perform(UiController controller, View view) {
                acao.accept(ActivityMainBinding.bind(view));
                controller.loopMainThreadUntilIdle();
            }
        });
    }

    private void cadastrar(String titulo, int gravidade) {
        naTela(tela -> {
            tela.inputBugTitle.setText(titulo);
            tela.inputBugModule.setText("Login");
            tela.inputBugDescription.setText("Erro ao acessar o sistema.");
            tela.seekGravidade.setProgress(gravidade);
        });
        onView(withId(R.id.buttonRegisterBug)).perform(scrollTo(), click());
    }

    private void acionarCartao(int posicao, boolean longo) {
        naTela(tela -> tela.recyclerBugs.scrollToPosition(posicao));
        naTela(tela -> {
            RecyclerView.ViewHolder holder = tela.recyclerBugs.findViewHolderForAdapterPosition(posicao);
            assertNotNull(holder);
            if (longo) assertTrue(holder.itemView.performLongClick());
            else assertTrue(holder.itemView.performClick());
        });
    }

    @Test
    public void validaFormularioENaoInsereRegistrosInvalidos() {
        naTela(tela -> {
            assertEquals(View.VISIBLE, tela.textEmptyBugs.getVisibility());
            assertTrue(tela.recyclerBugs.getHeight() > 0);
            tela.inputBugTitle.setText("   ");
            tela.inputBugModule.setText("   ");
            tela.inputBugDescription.setText("curta");
        });
        onView(withId(R.id.buttonRegisterBug)).perform(scrollTo(), click());
        naTela(tela -> {
            assertNotNull(tela.inputBugTitle.getError());
            assertNotNull(tela.inputBugModule.getError());
            assertNotNull(tela.inputBugDescription.getError());
            assertEquals(0, tela.recyclerBugs.getAdapter().getItemCount());
        });
        cadastrar("  Falha ao entrar  ", 4);
        naTela(tela -> {
            assertEquals(1, tela.recyclerBugs.getAdapter().getItemCount());
            assertEquals(View.GONE, tela.textEmptyBugs.getVisibility());
            assertEquals("", tela.inputBugTitle.getText().toString());
            assertEquals("", tela.inputBugModule.getText().toString());
            assertEquals("", tela.inputBugDescription.getText().toString());
            assertEquals(1, tela.seekGravidade.getProgress());
            assertEquals(tela.getRoot().getContext().getString(R.string.gravidade_valor, 1),
                    tela.textGravidade.getText().toString());
        });
    }

    @Test
    public void atualizaSomenteItemAlteradoEPontuacaoAposRemocaoNoMeio() {
        ArrayList<String> eventos = new ArrayList<>();
        naTela(tela -> tela.recyclerBugs.getAdapter().registerAdapterDataObserver(
                new RecyclerView.AdapterDataObserver() {
                    @Override public void onChanged() { fail("Atualização completa indevida"); }
                    @Override public void onItemRangeInserted(int inicio, int quantidade) {
                        eventos.add("I:" + inicio + ":" + quantidade);
                    }
                    @Override public void onItemRangeChanged(int inicio, int quantidade) {
                        eventos.add("C:" + inicio + ":" + quantidade);
                    }
                    @Override public void onItemRangeRemoved(int inicio, int quantidade) {
                        eventos.add("R:" + inicio + ":" + quantidade);
                    }
                }));
        cadastrar("Falha A", 4);
        cadastrar("Falha B", 2);
        cadastrar("Falha C", 5);
        acionarCartao(0, false);
        naTela(tela -> assertEquals("40", tela.textHunterScore.getText().toString()));
        acionarCartao(0, false);
        naTela(tela -> assertEquals("0", tela.textHunterScore.getText().toString()));
        acionarCartao(1, true);
        // O antigo terceiro cartão agora está na posição 1.
        acionarCartao(1, false);
        naTela(tela -> {
            assertEquals("2", tela.textTotalBugs.getText().toString());
            assertEquals("1", tela.textOpenBugs.getText().toString());
            assertEquals("1", tela.textResolvedBugs.getText().toString());
            assertEquals("50", tela.textHunterScore.getText().toString());
        });
        acionarCartao(1, true);
        naTela(tela -> assertEquals("0", tela.textHunterScore.getText().toString()));
        acionarCartao(0, true);
        naTela(tela -> {
            assertEquals(0, tela.recyclerBugs.getAdapter().getItemCount());
            assertEquals(View.VISIBLE, tela.textEmptyBugs.getVisibility());
            assertEquals("0", tela.textTotalBugs.getText().toString());
        });
        assertEquals(Arrays.asList("I:0:1", "I:1:1", "I:2:1", "C:0:1", "C:0:1",
                "R:1:1", "C:1:1", "R:1:1", "R:0:1"), eventos);
    }

    @Test
    public void formularioContinuaAcessivelComTecladoAberto() {
        onView(withId(R.id.inputBugTitle)).perform(scrollTo(), click(), replaceText("Falha com teclado"));
        onView(withId(R.id.inputBugModule)).perform(scrollTo(), click(), replaceText("Login"));
        onView(withId(R.id.inputBugDescription)).perform(scrollTo(), click(), replaceText("Erro ao acessar o sistema."));
        onView(withId(R.id.buttonRegisterBug)).perform(scrollTo(), click(), closeSoftKeyboard());
        naTela(tela -> {
            assertEquals(1, tela.recyclerBugs.getAdapter().getItemCount());
            assertEquals("1", tela.textTotalBugs.getText().toString());
            assertTrue(tela.recyclerBugs.getHeight() > 0);
        });
    }

    @Test
    public void rolagemMantemDadosEStatusDeUmaListaMaior() {
        for (int i = 0; i < 10; i++) cadastrar("Falha " + i, i % 5 + 1);
        acionarCartao(9, false);
        acionarCartao(0, false);
        naTela(tela -> {
            assertEquals("60", tela.textHunterScore.getText().toString());
            ItemBugBinding item = ItemBugBinding.bind(
                    tela.recyclerBugs.findViewHolderForAdapterPosition(0).itemView);
            assertEquals("Falha 0", item.textBugTitle.getText().toString());
            assertEquals(tela.getRoot().getContext().getString(R.string.status_resolved_text),
                    item.textBugStatus.getText().toString());
        });
        naTela(tela -> tela.recyclerBugs.scrollToPosition(9));
        naTela(tela -> {
            ItemBugBinding item = ItemBugBinding.bind(
                    tela.recyclerBugs.findViewHolderForAdapterPosition(9).itemView);
            assertEquals("Falha 9", item.textBugTitle.getText().toString());
            assertEquals(tela.getRoot().getContext().getString(R.string.status_resolved_text),
                    item.textBugStatus.getText().toString());
        });
    }

    @Test
    public void reutilizaViewHolderSemManterEstadoDoBugAnterior() {
        naTela(tela -> {
            ArrayList<Bug> bugs = new ArrayList<>();
            Bug baixo = new Bug("Baixo", "Cadastro", "Primeira descrição", 1, 0);
            baixo.setCorrigido(true);
            Bug medio = new Bug("Médio", "Pagamento", "Segunda descrição", 3, R.drawable.ic_bug);
            Bug critico = new Bug("Crítico", "Login", "Terceira descrição", 5, R.drawable.ic_bug);
            bugs.add(baixo);
            bugs.add(medio);
            bugs.add(critico);
            ArrayList<Bug> cliques = new ArrayList<>();
            ArrayList<Bug> longos = new ArrayList<>();
            BugAdapter adapter = new BugAdapter(bugs, cliques::add, longos::add);
            BugViewHolder holder = adapter.onCreateViewHolder(tela.recyclerBugs, 0);
            ItemBugBinding item = ItemBugBinding.bind(holder.itemView);
            for (int i = 0; i < bugs.size(); i++) {
                adapter.onBindViewHolder(holder, i);
                Bug bug = bugs.get(i);
                assertEquals(bug.getTitulo(), item.textBugTitle.getText().toString());
                assertEquals(bug.getCodigo(), item.textBugCode.getText().toString());
                assertEquals(bug.getDescricao(), item.textBugDescription.getText().toString());
                int esperado = ContextCompat.getColor(tela.getRoot().getContext(), i == 0
                        ? R.color.severity_low : i == 1 ? R.color.severity_medium : R.color.severity_high);
                assertEquals(esperado, item.textBugSeverity.getCurrentTextColor());
                assertEquals(i == 0, (item.textBugTitle.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) != 0);
                assertEquals(tela.getRoot().getContext().getString(i == 0
                        ? R.string.status_resolved_text : R.string.status_open_text),
                        item.textBugStatus.getText().toString());
                assertEquals(ContextCompat.getColor(tela.getRoot().getContext(), i == 0
                        ? R.color.resolved_background : R.color.surface_background),
                        item.getRoot().getCardBackgroundColor().getDefaultColor());
                if (i == 0) assertNull(item.imageBug.getDrawable());
                else assertNotNull(item.imageBug.getDrawable());
                holder.itemView.performClick();
                assertTrue(holder.itemView.performLongClick());
                assertSame(bug, cliques.get(i));
                assertSame(bug, longos.get(i));
            }
        });
    }
}
