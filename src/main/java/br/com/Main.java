package br.com;

import br.com.analisador.AnalisadorLexico;
import br.com.token.Token;

import java.util.List;

// TODO - as variáveis terão $ na frente.
// TODO - Ajustar a implementação dos tokens de descarte

public class Main {
    public static void main(String[] args) {
//        ---- Identificou corretamente ----
//        String script = "Crie o numero x valendo 10 e mostre x";
//        String script = "Crie o texto nome valendo \"Maria\" e mostre nome";
//        String script = "Crie o numero preco valendo 29.99 e mostre preco";
//        String script = "Crie o texto msg valendo \"Olá! Você está aprendendo programação\" e exiba msg";

//        ---- Não identificou corretamente ----
        String script = "Crie o numero a valendo 5 e crie o numero b valendo 3";
//        String script = "Some 10 com 20 e mostre resultado";
//        String script = "Subtraia 5 de 15";
//        String script = "Multiplique 7 por 8";
//        String script = "Divida 100 por 4";
//        String script = "Se x maior que 10 mostre x";
//        String script = "Se idade maior ou igual 18 mostre \"Adulto\" senão mostre \"Menor\"";
//        String script = "Se x menor que 5 mostre x";
//        String script = "Crie um numero para o contador valendo 0";
//        String script = "Se x maior que 10 e x menor que 100 mostre x";
//        String script = "Crie o numero total valendo 0 e adicione 10 ao total e multiplique total por 2 e mostre total";

        AnalisadorLexico analisadorLexico = new AnalisadorLexico(script);
        List<Token> tokens = analisadorLexico.analisar();
        analisadorLexico.exibirTokens(tokens);
    }
}