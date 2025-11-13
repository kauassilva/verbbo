package br.com.ast.interfaces;

/**
 * Interface base para todos os nós da Árvore Sintática Abstrata (AST).
 */
public interface Node {
    /**
     * Aceita um visitor para processar este nó.
     * Implementa o padrão Visitor, útil para percorrer/processar a árvore.
     * Por enquanto não ta sendo usado nada disso, talvez no semantico
     */
    <R> R accept(Visitor<R> visitor);

    /**
     * Retorna uma representação em string deste nó.
     */
    String toString();
}
