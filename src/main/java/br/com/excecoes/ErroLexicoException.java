package br.com.excecoes;

public class ErroLexicoException extends Exception {
    private final int posicao;
    private final String tokenInvalido;

    public ErroLexicoException(String mensagem, int posicao, String tokenInvalido) {
        super(mensagem);
        this.posicao = posicao;
        this.tokenInvalido = tokenInvalido;
    }

    public int getPosicao() {
        return posicao;
    }

    public String getTokenInvalido() {
        return tokenInvalido;
    }

    @Override
    public String getMessage() {
        return String.format("%s (Posição: %d, Token: '%s')",
                super.getMessage(), posicao, tokenInvalido);
    }
}