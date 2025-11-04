package br.com;

import br.com.analisador.AnalisadorLexico;
import br.com.token.Token;

import java.util.List;

// TODO - as variáveis terão $ na frente.
// TODO - Ajustar a implementação dos tokens de descarte

public class Main {
    public static void main(String[] args) {
        String script = "Crie o numero a valendo 5 e crie o numero b valendo 3";

        AnalisadorLexico analisadorLexico = new AnalisadorLexico(script);
        List<Token> tokens = analisadorLexico.analisar();
        analisadorLexico.exibirTokens(tokens);
    }
}