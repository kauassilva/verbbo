package br.com.analisador;

import br.com.ast.*;
import br.com.ast.interfaces.Expression;
import br.com.ast.interfaces.Statement;
import br.com.token.Token;
import br.com.token.TokenType;
import br.com.exception.ParseException;

import java.util.ArrayList;
import java.util.List;

public class AnalisadorSintatico {
    private final List<Token> tokens;
    private int pos = 0;

    public AnalisadorSintatico(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Program parse() {
        List<Statement> statements = new ArrayList<>();

        while (!estaNoFinal()) {
            pularRuidos();
            statements.add(parseStatement());

            // Se houver um CONECTOR_E, consome ele e continue parseando
            match(TokenType.CONECTOR_E);
        }

        return new Program(statements);
    }

    private Statement parseStatement() {
        if (match(TokenType.VERBO_CRIAR)) {
            return parseDeclaration();
        }
        if (match(TokenType.VERBO_MOSTRAR)) {
            return parsePrintStatement();
        }

        throw error(espiar(), "Esperado um comando (criar, mostrar, etc)");
    }

    private Declaration parseDeclaration() {
        pularRuidos();
        TokenType tipo;
        if (match(TokenType.TIPO_NUMERICO)) {
            tipo = TokenType.TIPO_NUMERICO;
        } else if (match(TokenType.TIPO_TEXTO)) {
            tipo = TokenType.TIPO_TEXTO;
        } else if (match(TokenType.TIPO_BOOLEANO)) {
            tipo = TokenType.TIPO_BOOLEANO;
        } else {
            throw error(espiar(), "Esperado um tipo (numero, texto, etc)");
        }

        pularRuidos();

        match(TokenType.CONECTOR_PARA);

        pularRuidos();
        Token name = consome(TokenType.DECLARACAO_VARIAVEL, "Esperado declaração de variável (começando com $)");

        pularRuidos();
        consome(TokenType.VERBO_ATRIBUIR, "Esperado 'valendo, vale, seja, etc' após nome da variável");

        pularRuidos();
        Expression value = parseExpression();

        // Remove o $ do início do nome da variável
        String varName = name.getValor();
        if (varName.startsWith("$")) {
            varName = varName.substring(1);
        }

        return new Declaration(tipo, varName, value);
    }

    private PrintStatement parsePrintStatement() {
        pularRuidos(); // Pula ruídos antes da variável
        Token varName = consome(TokenType.IDENTIFICADOR, "Esperado nome de variável existente após mostrar/exibir");

        // Remove o $ do início do nome da variável
        String name = varName.getValor();
        if (name.startsWith("$")) {
            name = name.substring(1);
        }
        return new PrintStatement(new Variable(name));
    }

    private Expression parseExpression() {
        pularRuidos(); // Pula ruídos antes da expressão
        if (match(TokenType.LITERAL_NUMERICO)) {
            String valor = anterior().getValor();
            if (valor.contains(".")) {
                return new Literal(Double.parseDouble(valor));
            } else {
                return new Literal(Integer.parseInt(valor));
            }
        }
        if (match(TokenType.LITERAL_TEXTO)) {
            return new Literal(anterior().getValor());
        }
        if (match(TokenType.IDENTIFICADOR)) {
            String varName = anterior().getValor();
            if (varName.startsWith("$")) {
                varName = varName.substring(1);
            }
            return new Variable(varName);
        }

        throw error(espiar(), "Esperado uma expressão (número, texto ou variável existente)");
    }

    private void pularRuidos() {
        while (!estaNoFinal() && espiar().getTipo() == TokenType.CONECTOR_RUIDO) {
            avancar();
        }
    }

    private boolean match(TokenType... types) {
        pularRuidos(); // Pula ruídos antes do match
        for (TokenType type : types) {
            if (checar(type)) {
                avancar();
                return true;
            }
        }
        return false;
    }

    private boolean checar(TokenType type) {
        if (estaNoFinal()) return false;
        return espiar().getTipo() == type;
    }

    private Token avancar() {
        if (!estaNoFinal()) pos++;
        return anterior();
    }

    private boolean estaNoFinal() {
        return pos >= tokens.size();
    }

    private Token espiar() {
        if (estaNoFinal()) {
            return new Token(TokenType.ERRO_LEXICO, "<fim>");
        }
        return tokens.get(pos);
    }

    private Token anterior() {
        return tokens.get(pos - 1);
    }

    private Token consome(TokenType type, String message) {
        pularRuidos(); // Pula ruídos antes de consumir
        if (checar(type)) return avancar();
        throw error(espiar(), message);
    }

    private ParseException error(Token token, String message) {
        return new ParseException(
            String.format("Erro: %s (encontrado '%s')", message, token.getValor())
        );
    }

    public void exibirPrograma(Program program) {
        System.out.println("\n=== ANÁLISE SINTÁTICA ===");
        System.out.println("Statements encontrados: " + program.statements().size());

        // Lista variáveis declaradas e usadas
        System.out.println("\nVariáveis declaradas: " + program.getAllDeclaredVariables());
        System.out.println("Variáveis usadas: " + program.getAllUsedVariables());

        System.out.println("\nÁrvore sintática:");
        System.out.println("-".repeat(60));
        // formata a "árvore", é mais a estrutura de como vai ser exibido
        System.out.println(program);
        System.out.println("-".repeat(60));
    }
}
