package br.com;

import br.com.analisador.AnalisadorLexico;
import br.com.token.Token;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

// TODO - as variáveis terão $ na frente.
// TODO - Ajustar a implementação dos tokens de descarte

public class Main {
    public static void main(String[] args) {
        Path caminhoScript = Paths.get("src/main/resources/scripts-teste.txt");
        Path caminhoScriptErrors = Paths.get("src/main/resources/script-errors.txt");

        Scanner scanner = new Scanner(System.in);

        try {
            List<String> linhasScript = Files.readAllLines(caminhoScriptErrors);
//            linhasScript.forEach(System.out::println);

            for (String linha : linhasScript) {
                AnalisadorLexico analisadorLexico = new AnalisadorLexico(linha);
                List<Token> tokens = analisadorLexico.analisar();
                analisadorLexico.exibirTokens(tokens);

                scanner.nextLine();
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
        }
    }
}