package br.com.ast;

import br.com.ast.interfaces.Statement;
import br.com.ast.interfaces.Visitor;
import br.com.ast.interfaces.Expression;

import java.util.ArrayList;
import java.util.List;

public record IfStatement(Expression condition, Statement thenCondition, Statement elseCondition) implements Statement {

    @Override
    public List<String> getDeclaredVariables() {
        List<String> declared = new ArrayList<>();
        if (thenCondition != null) declared.addAll(thenCondition.getDeclaredVariables());
        if (elseCondition != null) declared.addAll(elseCondition.getDeclaredVariables());
        return declared;
    }

    @Override
    public List<String> getUsedVariables() {
        List<String> used = new ArrayList<>();
        if (condition instanceof Variable) used.add(((Variable) condition).name());
        if (thenCondition != null) used.addAll(thenCondition.getUsedVariables());
        if (elseCondition != null) used.addAll(elseCondition.getUsedVariables());
        return used;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitIfStatement(this);
    }

    @Override
    public String toString() {
        return String.format("If[cond=%s, then=%s, else=%s]", condition, thenCondition, elseCondition);
    }
}

