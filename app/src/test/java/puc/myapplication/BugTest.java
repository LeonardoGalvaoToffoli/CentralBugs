package puc.myapplication;

import org.junit.Test;

import static org.junit.Assert.*;

public class BugTest {
    @Test
    public void codigosSaoSequenciaisMesmoQuandoUmBugEhDescartado() {
        Bug primeiro = new Bug("Falha", "Login", "Erro ao entrar", 4, 1);
        Bug descartado = new Bug("Falha", "Login", "Erro ao entrar", 1, 1);
        Bug seguinte = new Bug("Falha", "Login", "Erro ao entrar", 3, 1);
        int numero = Integer.parseInt(primeiro.getCodigo().substring(4));
        assertEquals(numero + 1, Integer.parseInt(descartado.getCodigo().substring(4)));
        assertEquals(numero + 2, Integer.parseInt(seguinte.getCodigo().substring(4)));
        assertTrue(primeiro.getCodigo().matches("BUG-\\d{3,}"));
        assertFalse(primeiro.isCorrigido());
    }

    @Test
    public void gravidadeInvalidaNaoConsomeCodigo() {
        Bug antes = new Bug("Falha", "Login", "Erro ao entrar", 1, 1);
        for (int gravidade : new int[]{0, 6}) {
            assertThrows(IllegalArgumentException.class,
                    () -> new Bug("Falha", "Login", "Erro ao entrar", gravidade, 1));
        }
        Bug depois = new Bug("Falha", "Login", "Erro ao entrar", 5, 1);
        assertEquals(Integer.parseInt(antes.getCodigo().substring(4)) + 1,
                Integer.parseInt(depois.getCodigo().substring(4)));
    }
}
