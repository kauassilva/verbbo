package br.com.ast;

import br.com.ast.interfaces.Expression;
import br.com.ast.interfaces.Statement;
import br.com.ast.interfaces.Visitor;

import java.util.ArrayList;
import java.util.List;

public record PrintStatement(Expression expression) implements Statement {

    @Override
    public List<String> getDeclaredVariables() {
        return List.of(); // Comando de impressão não declara variáveis
    }

    @Override
    public List<String> getUsedVariables() {
        List<String> usadas = new ArrayList<>();
        if (expression instanceof Variable) {
            usadas.add(((Variable) expression).name());
        }
        return usadas;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitPrintStatement(this);
    }

    @Override
    public String toString() {
        return String.format("Print[valor=%s]", expression);
    }
}
