package puc.myapplication;

import java.util.Locale;

public class Bug {
    private static int proximoCodigo = 1;
    private final String codigo;
    private final String titulo;
    private final String modulo;
    private final String descricao;
    private final int gravidade;
    private final int imagemResId;
    private boolean corrigido;

    // Criar somente depois da validação do formulário para não consumir códigos.
    public Bug(String titulo, String modulo, String descricao, int gravidade, int imagemResId) {
        if (gravidade < 1 || gravidade > 5) {
            throw new IllegalArgumentException("A gravidade deve estar entre 1 e 5.");
        }
        this.codigo = String.format(Locale.ROOT, "BUG-%03d", proximoCodigo++);
        this.titulo = titulo;
        this.modulo = modulo;
        this.descricao = descricao;
        this.gravidade = gravidade;
        this.imagemResId = imagemResId;
        this.corrigido = false;
    }

    public String getCodigo() { return codigo; }
    public String getTitulo() { return titulo; }
    public String getModulo() { return modulo; }
    public String getDescricao() { return descricao; }
    public int getGravidade() { return gravidade; }
    public int getImagemResId() { return imagemResId; }
    public boolean isCorrigido() { return corrigido; }
    public void setCorrigido(boolean corrigido) { this.corrigido = corrigido; }
}
