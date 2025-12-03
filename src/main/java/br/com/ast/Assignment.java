package br.com.ast;

import br.com.ast.interfaces.Expression;
import br.com.ast.interfaces.Statement;
import br.com.ast.interfaces.Visitor;

import java.util.ArrayList;
import java.util.List;

public record Assignment(String name, Expression value) implements Statement {
    @Override
    public List<String> getDeclaredVariables() {
        return List.of();
    }

    @Override
    public List<String> getUsedVariables() {
        List<String> usadas = new ArrayList<>();
        usadas.add(name);
        if (value instanceof Variable) {
            usadas.add(((Variable) value).name());
        }
        return usadas;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitAssignment(this);
    }
}