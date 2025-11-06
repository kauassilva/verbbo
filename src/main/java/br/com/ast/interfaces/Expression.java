package br.com.ast.interfaces;

import br.com.token.TokenType;

/**
 * Interface para nós que representam expressões (algo que produz um valor).
 * tipo: literais (5, "texto"), variáveis ($x), operações (2 + 3)
 */
public interface Expression extends Node {
    /**
     * Retorna o tipo desta expressão (número, texto, booleano).
     * Útil para verificação de tipos.
     */
    TokenType getType();
}
