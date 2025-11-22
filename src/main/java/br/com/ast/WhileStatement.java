package br.com.ast;

import br.com.ast.interfaces.Expression;
import br.com.ast.interfaces.Statement;
import br.com.ast.interfaces.Visitor;

import java.util.ArrayList;
import java.util.List;

public record WhileStatement(Expression condition, List<Statement> body) implements Statement {

    @Override
    public List<String> getDeclaredVariables() {
        List<String> vars = new ArrayList<>();
        body.forEach(s -> vars.addAll(s.getDeclaredVariables()));
        return vars;
    }

    @Override
    public List<String> getUsedVariables() {
        List<String> vars = new ArrayList<>();
        if (condition instanceof Variable) {
            vars.add(((Variable) condition).name());
        }
        body.forEach(s -> vars.addAll(s.getUsedVariables()));
        return vars;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitWhileStatement(this);
    }

    @Override
    public String toString() {
        return String.format("While[condition=%s, body=%s]", condition, body);
    }
}