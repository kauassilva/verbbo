package br.com;

import br.com.analisador.AnalisadorLexico;
import br.com.token.Token;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

// TODO - as variáveis terão $ na frente.
// TODO - Ajustar a implementação dos tokens de descarte

public class Main {
    public static void main(String[] args) {
        Path caminhoScript = Paths.get("src/main/resources/scripts-teste.txt");

        try {
            List<String> linhasScript = Files.readAllLines(caminhoScript);
            linhasScript.forEach(System.out::println);

            for (String linha : linhasScript) {
                System.out.println("Analisando linha: " + linha);
                AnalisadorLexico analisadorLexico = new AnalisadorLexico(linha);
                List<Token> tokens = analisadorLexico.analisar();
                analisadorLexico.exibirTokens(tokens);
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
        }
    }
}