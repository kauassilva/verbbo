package br.com.codegen;

import br.com.ast.Program;
import br.com.utils.TabelaSimbolos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class JavaCodeGenerator {
    private final String className;
    private final String pathToClass;

    public JavaCodeGenerator() {
        this.className = "Main";
        this.pathToClass = "target/generated/";
    }

    public JavaCodeGenerator(String className, String pathToClass) {
        this.className = className;
        this.pathToClass = pathToClass;
    }

    public Path generate(Program program, TabelaSimbolos tabela, Path outputDir) throws GeneratorException {
        try {
            if (!Files.exists(outputDir)) Files.createDirectories(outputDir);

            CodeWriter w = new CodeWriter();

            w.writeLine("public class " + className + " {");
            w.indent();
            w.writeLine("public static void main(String[] args) {");
            w.indent();

            JavaCodeGenVisitor visitor = new JavaCodeGenVisitor(w, tabela);
            program.accept(visitor);

            w.unindent();
            w.writeLine("}");
            w.unindent();
            w.writeLine("}");

            String source = w.toString();
            Path out = outputDir.resolve(className + ".java");
            Files.writeString(out, source, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return out;
        } catch (IOException e) {
            throw new GeneratorException("Falha ao escrever arquivo gerado", e);
        } catch (Exception e) {
            throw new GeneratorException("Erro na geração: " + e.getMessage(), e);
        }
    }

    public String generateToString(Program program, TabelaSimbolos tabela) throws GeneratorException {
        try {
            CodeWriter w = new CodeWriter();
            w.writeLine("public class " + className + " {");
            w.indent();
            w.writeLine("public static void main(String[] args) {");
            w.indent();
            JavaCodeGenVisitor visitor = new JavaCodeGenVisitor(w, tabela);
            program.accept(visitor);
            w.unindent();
            w.writeLine("}");
            w.unindent();
            w.writeLine("}");
            return w.toString();
        } catch (Exception e) {
            throw new GeneratorException("Erro na geração: " + e.getMessage(), e);
        }
    }

    public void exibirCodigo(String source) {
        System.out.println("\n" + "=".repeat(60));
        System.out.printf("  %s%s.java  %n", pathToClass, className);
        System.out.println("=".repeat(60));
        System.out.println(source);
        System.out.println("=".repeat(60));
    }

    public void writeJavaFile(Program programa, TabelaSimbolos tabelaSimbolos) throws GeneratorException {
        Path outDir = Path.of(pathToClass);
        generate(programa, tabelaSimbolos, outDir);
        System.out.println("Arquivo Java escrito em: " + outDir.resolve(className + ".java"));
    }

}

