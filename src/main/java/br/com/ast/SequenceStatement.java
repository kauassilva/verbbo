package br.com.ast;

import br.com.ast.interfaces.Statement;
import br.com.ast.interfaces.Visitor;

import java.util.ArrayList;
import java.util.List;

public record SequenceStatement(List<Statement> statements) implements Statement {

    @Override
    public List<String> getDeclaredVariables() {
        List<String> declared = new ArrayList<>();
        for (Statement s : statements) declared.addAll(s.getDeclaredVariables());
        return declared;
    }

    @Override
    public List<String> getUsedVariables() {
        List<String> used = new ArrayList<>();
        for (Statement s : statements) used.addAll(s.getUsedVariables());
        return used;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitSequenceStatement(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Sequence[\n");
        for (Statement s : statements) {
            sb.append("  ").append(s).append("\n");
        }
        sb.append("]");
        return sb.toString();
    }
}

