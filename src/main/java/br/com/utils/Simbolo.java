package br.com.utils;

import br.com.token.TokenType;

public class Simbolo {
    private final TokenType tipoVariavel; // numero, texto, booleano
    private final String nome;
    private Object valor; // valor atual da variável
    private boolean inicializada;

    public Simbolo(String nome, TokenType tipoVariavel) {
        this.nome = nome;
        this.tipoVariavel = tipoVariavel;
        this.valor = null;
        this.inicializada = false;
    }

    public Simbolo(String nome, TokenType tipoVariavel, Object valor) {
        this.nome = nome;
        this.tipoVariavel = tipoVariavel;
        this.valor = valor;
        this.inicializada = true;
    }

    public TokenType getTipoVariavel() {
        return tipoVariavel;
    }

    public String getNome() {
        return nome;
    }

    public Object getValor() {
        return valor;
    }

    public void setValor(Object valor) {
        this.valor = valor;
        this.inicializada = true;
    }

    public boolean isInicializada() {
        return inicializada;
    }

    @Override
    public String toString() {
        String valorStr = valor != null
                ? (valor instanceof String ? "\"" + valor + "\"" : valor.toString())
                : "null";

        return String.format("Simbolo{nome='%s', tipo=%s, valor=%s, inicializada=%s}",
                nome, tipoParaString(tipoVariavel), valorStr, inicializada);
    }

    private String tipoParaString(TokenType tipo) {
        return switch (tipo) {
            case TIPO_NUMERICO -> "NUMERO";
            case TIPO_TEXTO -> "TEXTO";
            case TIPO_BOOLEANO -> "BOOLEANO";
            default -> tipo.toString();
        };
    }
}