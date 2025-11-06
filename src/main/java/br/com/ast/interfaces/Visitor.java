package br.com.ast.interfaces;

import br.com.ast.*;

/**
 * Interface para visitantes da AST(Arvore Sintatica Abstrat)
 * A padrão Visitor para percorrer/processar a árvore
 * Para a anlaise sintatico isso aqui não ta fazendo absolutamente nada ;)
 */
public interface Visitor<R> {
    R visitProgram(Program program);
    R visitDeclaration(Declaration declaration);
    R visitPrintStatement(PrintStatement printStatement);
    R visitLiteral(Literal literal);
    R visitVariable(Variable variable);
}
