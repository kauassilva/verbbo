package br.com.ast;

import br.com.ast.interfaces.Expression;
import br.com.ast.interfaces.Visitor;
import br.com.token.TokenType;

public class Literal implements Expression {
    private final Object value;
    private final TokenType type;

    public Literal(Object value) {
        this.value = value;
        // Infere o tipo baseado no valor
        if (value instanceof Integer || value instanceof Double) {
            this.type = TokenType.LITERAL_NUMERICO;
        } else if (value instanceof String) {
            this.type = TokenType.LITERAL_TEXTO;
        } else if (value instanceof Boolean) {
            this.type = TokenType.LITERAL_BOOLEANO;
        } else {
            throw new IllegalArgumentException("Tipo de literal não suportado: " + value.getClass());
        }
    }

    @Override
    public TokenType getType() {
        return type;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitLiteral(this);
    }

    @Override
    public String toString() {
        if (value instanceof String) {
            return String.format("Literal[\"%s\"]", value);
        }
        return String.format("Literal[%s]", value);
    }

    public Object getValue() {
        return value;
    }
}
