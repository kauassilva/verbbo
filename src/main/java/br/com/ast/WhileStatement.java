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
        collectVariablesFromExpression(condition, vars);
        body.forEach(s -> vars.addAll(s.getUsedVariables()));
        return vars;
    }

    private void collectVariablesFromExpression(Expression expr, List<String> vars) {
        if (expr instanceof Variable v) {
            vars.add(v.name());
        } else if (expr instanceof BinaryExpression b) {
            collectVariablesFromExpression(b.left(), vars);
            collectVariablesFromExpression(b.right(), vars);
        } else if (expr instanceof UnaryExpression u) {
            collectVariablesFromExpression(u.right(), vars);
        }
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