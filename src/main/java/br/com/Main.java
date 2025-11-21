package br.com;

import br.com.analisador.AnalisadorLexico;
import br.com.analisador.AnalisadorSemantico;
import br.com.analisador.AnalisadorSintatico;
import br.com.ast.Program;
import br.com.codegen.JavaCodeGenerator;
import br.com.token.Token;
import br.com.utils.TabelaSimbolos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Path caminhoScript = Paths.get("src/main/resources/scripts-teste.txt");
        Path caminhoScriptErrors = Paths.get("src/main/resources/script-errors.txt");
        Path scriptSemantico = Paths.get("src/main/resources/script-semantico.txt");

        Scanner scanner = new Scanner(System.in);

        try {
            List<String> linhasScript = Files.readAllLines(scriptSemantico);

            for (String linha : linhasScript) {
                try {
                    // Análise léxica
                    AnalisadorLexico analisadorLexico = new AnalisadorLexico(linha);
                    List<Token> tokens = analisadorLexico.analisar();
                    analisadorLexico.exibirTokens(tokens);

                    // Análise sintática
                    AnalisadorSintatico analisadorSintatico = new AnalisadorSintatico(tokens);
                    Program programa = analisadorSintatico.parse();
                    analisadorSintatico.exibirPrograma(programa);

                    // Análise semântica
                    AnalisadorSemantico analisadorSemantico = new AnalisadorSemantico();
                    analisadorSemantico.analisar(programa);
                    TabelaSimbolos tabelaSimbolos = analisadorSemantico.getTabelaSimbolos();

                    String className = "Main";
                    String pathToClass = "target/generated/";

                    JavaCodeGenerator gen = new JavaCodeGenerator(className, pathToClass);
                    String source = gen.generateToString(programa, tabelaSimbolos);
                    gen.exibirCodigo(source);
                    gen.writeJavaFile(programa, tabelaSimbolos);
                } catch (Exception e) {
                    System.out.println("\nErro de sintaxe: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
        }
    }
}