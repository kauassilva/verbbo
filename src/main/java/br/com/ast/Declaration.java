package br.com.ast;

import br.com.ast.interfaces.Expression;
import br.com.ast.interfaces.Statement;
import br.com.ast.interfaces.Visitor;
import br.com.token.TokenType;
import java.util.ArrayList;
import java.util.List;

public record Declaration(TokenType tipo, String nome, Expression inicializador) implements Statement {

    @Override
    public List<String> getDeclaredVariables() {
        return List.of(nome); //declara uma variável
    }

    @Override
    public List<String> getUsedVariables() {
        List<String> usadas = new ArrayList<>();
        if (inicializador instanceof Variable) {
            usadas.add(((Variable) inicializador).name());
        }
        return usadas;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitDeclaration(this);
    }

    @Override
    public String toString() {
        return String.format("Declaration[tipo=%s, nome=%s, valor=%s]",
                tipo, nome, inicializador);
    }
}
