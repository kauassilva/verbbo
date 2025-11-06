package br.com.ast;

import br.com.ast.interfaces.Expression;
import br.com.ast.interfaces.Visitor;
import br.com.token.TokenType;

public record Variable(String name) implements Expression {

    @Override
    public TokenType getType() {
        // O tipo real da variável será determinado durante a análise semântica
        return TokenType.IDENTIFICADOR;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitVariable(this);
    }

    @Override
    public String toString() {
        return String.format("Variable[nome=%s]", name);
    }
}
