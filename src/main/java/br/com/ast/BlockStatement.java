package br.com.ast;

import br.com.ast.interfaces.Statement;
import br.com.ast.interfaces.Visitor;

import java.util.ArrayList;
import java.util.List;

public record BlockStatement(List<Statement> statements) implements Statement {

    @Override
    public List<String> getDeclaredVariables() {
        List<String> vars = new ArrayList<>();
        statements.forEach(s -> vars.addAll(s.getDeclaredVariables()));
        return vars;
    }

    @Override
    public List<String> getUsedVariables() {
        List<String> vars = new ArrayList<>();
        statements.forEach(s -> vars.addAll(s.getUsedVariables()));
        return vars;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitBlockStatement(this);
    }

    @Override
    public String toString() {
        return String.format("Block%s", statements);
    }
}