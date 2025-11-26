package br.com;

import br.com.analisador.AnalisadorLexico;
import br.com.analisador.AnalisadorSemantico;
import br.com.analisador.AnalisadorSintatico;
import br.com.ast.Program;
import br.com.codegen.JavaCodeGenerator;
import br.com.token.Token;
import br.com.utils.TabelaSimbolos;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final String DEFAULT_INPUT_DIR = "input";
    private static final String DEFAULT_OUTPUT_DIR = "output";

    public static void main(String[] args) {
        try {
            File arquivoFonte;
            if (args.length > 0) {
                arquivoFonte = new File(args[0]);
            } else {
                arquivoFonte = buscarPrimeiroArquivoNaPasta(DEFAULT_INPUT_DIR);
            }

            if (arquivoFonte == null || !arquivoFonte.exists()) {
                System.err.println("Erro: Arquivo de entrada não encontrado na pasta '" + DEFAULT_INPUT_DIR + "'.");
                return;
            }

            Scanner console = new Scanner(System.in);
            System.out.println("------------------------------------------------");
            System.out.print(">>> Digite o nome do arquivo/classe que será gerado (ex: MinhaClasse): ");
            String nomeClasseUsuario = console.nextLine().trim();

            if (!nomeClasseUsuario.isEmpty()) {
                nomeClasseUsuario = nomeClasseUsuario.substring(0, 1).toUpperCase() + nomeClasseUsuario.substring(1);
            } else {
                nomeClasseUsuario = "AppGenerated"; // Fallback
            }

            System.out.println(">>> Processando arquivo fonte: " + arquivoFonte.getName());

            boolean sucesso = processarCompilacao(arquivoFonte, nomeClasseUsuario);

            if (sucesso) {
                executarELimpar(nomeClasseUsuario);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean processarCompilacao(File arquivo, String className) throws IOException {
        String codigoFonte = Files.readString(arquivo.toPath());

        try {
            AnalisadorLexico lexico = new AnalisadorLexico(codigoFonte);
            List<Token> tokens = lexico.analisar();

            AnalisadorSintatico sintatico = new AnalisadorSintatico(tokens);
            Program programa = sintatico.parse();

            AnalisadorSemantico semantico = new AnalisadorSemantico();
            semantico.analisar(programa);
            TabelaSimbolos tabela = semantico.getTabelaSimbolos();

            JavaCodeGenerator generator = new JavaCodeGenerator(className, DEFAULT_OUTPUT_DIR);
            generator.writeJavaFile(programa, tabela);

            Path javaPath = Paths.get(DEFAULT_OUTPUT_DIR, className + ".java");
            String codigoGerado = Files.readString(javaPath);

            System.out.println("\n================================================");
            System.out.println("            CÓDIGO JAVA GERADO");
            System.out.println("================================================\n");
            System.out.println(codigoGerado);
            System.out.println("================================================\n");

            Path txtPath = Paths.get(DEFAULT_OUTPUT_DIR, className + ".txt");
            Files.writeString(txtPath, codigoGerado);

            return true;

        } catch (Exception e) {
            System.err.println("ERRO NA ANÁLISE: " + e.getMessage());
            return false;
        }
    }

    private static void executarELimpar(String className) {
        try {
            System.out.println(">>> Compilando e Executando...");

            ProcessBuilder compileProcess = new ProcessBuilder(
                    "javac",
                    "-d", DEFAULT_OUTPUT_DIR,
                    DEFAULT_OUTPUT_DIR + "/" + className + ".java"
            );
            compileProcess.inheritIO();
            if (compileProcess.start().waitFor() != 0) {
                System.err.println(">>> Erro de compilação Java.");
                return;
            }

            System.out.println("\n>>> RESULTADO DA EXECUÇÃO:");
            System.out.println("------------------------------------------------");

            ProcessBuilder runProcess = new ProcessBuilder(
                    "java",
                    "-cp", DEFAULT_OUTPUT_DIR,
                    className
            );
            runProcess.inheritIO();
            runProcess.start().waitFor();

            System.out.println("------------------------------------------------");
            System.out.println(">>> Fim da execução");

            Files.deleteIfExists(Paths.get(DEFAULT_OUTPUT_DIR, className + ".java"));
            Files.deleteIfExists(Paths.get(DEFAULT_OUTPUT_DIR, className + ".class"));

            System.out.println(">>> Código salvo para consulta em: output/" + className + ".txt");

        } catch (IOException | InterruptedException e) {
            System.err.println("Erro na execução: " + e.getMessage());
        }
    }

    private static File buscarPrimeiroArquivoNaPasta(String pastaPath) {
        File pasta = new File(pastaPath);
        if (!pasta.exists()) pasta.mkdir();
        File[] arquivos = pasta.listFiles((dir, name) -> name.endsWith(".txt"));
        return (arquivos != null && arquivos.length > 0) ? arquivos[0] : null;
    }
}