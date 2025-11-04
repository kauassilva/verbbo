package br.com;

import br.com.analisador.AnalisadorLexico;
import br.com.excecoes.ErroLexicoException;
import br.com.token.Token;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Teste 1: Script valido com variaveis
        testarScript("Crie o numero $a valendo 5 e crie o numero $b valendo 3");

        // Teste 2: Script com variavel invalida ($$)
        testarScript("Crie o numero $$ valendo 5");

        // Teste 3: Script com variavel invalida ($)
        testarScript("Crie o numero $ valendo 5");

        // Teste 4: Script com caractere invalido
        testarScript("Crie o numero $a valendo @ 5");

        // Teste 5: Script com numero invalido (multiplos pontos)
        testarScript("Crie o numero $valor valendo 3.14.15");

        // Teste 6: Script com variavel valida e acentos
        testarScript("Crie o numero $cao valendo 10");
    }

    private static void testarScript(String script) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("TESTANDO: " + script);
        System.out.println("=".repeat(70));

        try {
            AnalisadorLexico analisadorLexico = new AnalisadorLexico(script);
            List<Token> tokens = analisadorLexico.analisar();
            analisadorLexico.exibirTokens(tokens);
            System.out.println("✓ Analise concluida com sucesso!");
        } catch (ErroLexicoException e) {
            System.err.println("✗ ERRO LEXICO: " + e.getMessage());
        }
    }
}