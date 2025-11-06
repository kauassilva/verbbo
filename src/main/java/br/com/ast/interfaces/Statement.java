package br.com.ast.interfaces;

import java.util.List;

/**
 * Interface para nós que representam comandos completos
 * Por exemplo: declarações (crie), atribuições (valendo), comandos de impressão (mostre)
 */
public interface Statement extends Node {
    /**
     * Retorna as variáveis declaradas por este statement, se houver.
     * Útil para análise de escopo.
     */
    List<String> getDeclaredVariables();

    /**
     * Retorna as variáveis usadas/referenciadas por este statement.
     * Útil para análise de uso de variáveis.
     */
    List<String> getUsedVariables();
}
