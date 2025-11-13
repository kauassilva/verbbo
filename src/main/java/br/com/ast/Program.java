package br.com.ast;

import br.com.ast.interfaces.Node;
import br.com.ast.interfaces.Statement;
import br.com.ast.interfaces.Visitor;

import java.util.List;
import java.util.ArrayList;

public record Program(List<Statement> statements) implements Node {

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitProgram(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Program[\n");
        for (Statement stmt : statements) {
            sb.append("  ").append(stmt.toString().replace("\n", "\n  ")).append("\n");
        }
        return sb.append("]").toString();
    }

    //Retorna todas as variáveis declaradas no programa.
    public List<String> getAllDeclaredVariables() {
        List<String> declared = new ArrayList<>();
        for (Statement stmt : statements) {
            declared.addAll(stmt.getDeclaredVariables());
        }
        return declared;
    }

    //Retorna todas as variáveis usadas no programa.
    public List<String> getAllUsedVariables() {
        List<String> used = new ArrayList<>();
        for (Statement stmt : statements) {
            used.addAll(stmt.getUsedVariables());
        }
        return used;
    }
}
