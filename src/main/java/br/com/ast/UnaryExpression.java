package br.com.ast;

import br.com.ast.interfaces.Expression;
import br.com.ast.interfaces.Visitor;
import br.com.token.Token;
import br.com.token.TokenType;

public record UnaryExpression(Token operator, Expression right) implements Expression {

    @Override
    public TokenType getType() {
        return operator != null ? operator.getTipo() : TokenType.IDENTIFICADOR;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitUnaryExpression(this);
    }

    @Override
    public String toString() {
        return String.format("Unary[op=%s, right=%s]", operator != null ? operator.getValor() : "?", right);
    }
}

