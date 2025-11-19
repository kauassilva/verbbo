package br.com.utils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class TabelaSimbolos {
    private final Deque<Map<String, Simbolo>> scopes = new ArrayDeque<>();

    public TabelaSimbolos() {
        // Inicializa com escopo global
        pushScope();
    }

    public void pushScope() {
        scopes.push(new HashMap<>());
    }

    public void popScope() {
        if (scopes.isEmpty() || scopes.size() == 1) {
            throw new IllegalStateException("Não pode remover o escopo global");
        }
        scopes.pop();
    }

    /**
     * Declara uma variável no escopo atual
     * @return true se declarou com sucesso, false se já existe
     */
    public boolean declare(String name, Simbolo symbol) {
        Map<String, Simbolo> currentScope = scopes.peek();

        assert currentScope != null;
        if (currentScope.containsKey(name)) {
            return false; // Variável já declarada no escopo atual
        }

        currentScope.put(name, symbol);
        return true;
    }

    /**
     * Busca uma variável em todos os escopos (do mais interno ao mais externo)
     * @return Simbolo encontrado ou null
     */
    public Simbolo lookup(String name) {
        for (Map<String, Simbolo> scope : scopes) {
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        return null;
    }

    /**
     * Verifica se variável existe apenas no escopo atual
     */
    public boolean existsInCurrentScope(String name) {
        if (scopes.isEmpty()) return false;
        return scopes.peek().containsKey(name);
    }

    /**
     * Verifica se variável existe em qualquer escopo
     */
    public boolean exists(String name) {
        return lookup(name) != null;
    }

    /**
     * Atualiza o valor de uma variável existente
     */
    public boolean update(String name, Object value) {
        Simbolo symbol = lookup(name);
        if (symbol == null) {
            return false;
        }
        symbol.setValor(value);
        return true;
    }

    public void printScopes() {
        System.out.println("\n========== TABELA DE SÍMBOLOS ==========");
        int nivel = scopes.size();
        for (Map<String, Simbolo> scope : scopes) {
            System.out.println("--- Escopo nível " + nivel + " ---");
            if (scope.isEmpty()) {
                System.out.println("  (vazio)");
            } else {
                scope.forEach((nome, simbolo) ->
                        System.out.println("  " + simbolo));
            }
            nivel--;
        }
        System.out.println("========================================\n");
    }
}