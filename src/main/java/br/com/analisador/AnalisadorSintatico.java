package br.com.analisador;

import br.com.ast.*;
import br.com.ast.interfaces.Expression;
import br.com.ast.interfaces.Statement;
import br.com.token.Token;
import br.com.token.TokenType;
import br.com.excecoes.ParseException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AnalisadorSintatico {
    private final List<Token> tokens;
    private int pos = 0;
    private final Set<String> declaredVars = new HashSet<>();

    public AnalisadorSintatico(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Program parse() {
        List<Statement> statements = new ArrayList<>();

        while (!estaNoFinal()) {
            pularRuidos();
            // parsea uma sequência de statements separados por 'e'
            List<Statement> seq = new ArrayList<>();
            seq.add(parseStatement());
            while (match(TokenType.CONECTOR_E)) {
                pularRuidos();
                // se o próximo é um verbo (novo comando), parseia-o
                if (!estaNoFinal()) {
                    seq.add(parseStatement());
                }
            }
            if (seq.size() == 1) statements.add(seq.get(0));
            else statements.add(new SequenceStatement(seq));
        }

        return new Program(statements);
    }

    private Statement parseStatement() {
        if (match(TokenType.VERBO_CRIAR)) {
            return parseCreateStatements();
        }
        if (match(TokenType.VERBO_MOSTRAR)) {
            return parsePrintStatement();
        }
        if (match(TokenType.CONDICIONAL_SE)) {
            return parseIfStatement();
        }

        throw error(espiar(), "Esperado um comando (criar, mostrar, se, etc)");
    }

    // trata uma sequência de declarações iniciada por um único 'crie'
    private Statement parseCreateStatements() {
        List<Statement> decls = new ArrayList<>();
        decls.add(parseSingleDeclaration());

        while (match(TokenType.CONECTOR_E)) {
            pularRuidos();
            // se o próximo token sugere o início de outra declaração (tipo ou ruído seguido de tipo), parseia
            if (isStartOfDeclaration()) {
                decls.add(parseSingleDeclaration());
            } else {
                pos--;
                break;
            }
        }

        if (decls.size() == 1) return decls.get(0);
        return new SequenceStatement(decls);
    }

    private boolean isStartOfDeclaration() {
        // considera início de declaração se houver um tipo (numero/texto/booleano)
        int idx = pos;
        if (idx < tokens.size() && tokens.get(idx).getTipo() == TokenType.CONECTOR_RUIDO) {
            idx++;
        }
        if (idx >= tokens.size()) return false;
        TokenType t = tokens.get(idx).getTipo();
        return t == TokenType.TIPO_NUMERICO || t == TokenType.TIPO_TEXTO || t == TokenType.TIPO_BOOLEANO;
    }

    private Declaration parseSingleDeclaration() {
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

        // consumir opcionalmente 'para'
        match(TokenType.CONECTOR_PARA);

        pularRuidos();
        Token name = consome(TokenType.DECLARACAO_VARIAVEL, "Esperado declaração de variável (começando com $)");

        String varName = name.getValor();
        if (varName.startsWith("$")) {
            varName = varName.substring(1);
        }
        // registra a variável declarada antes de parsear o valor para permitir usos contextuais
        declaredVars.add(varName);

        pularRuidos();
        Expression value;
        pularRuidos();
        if (match(TokenType.VERBO_ATRIBUIR)) {
            pularRuidos();
            value = parseExpression();
        } else if (espiar().getTipo() == TokenType.LITERAL_NUMERICO || espiar().getTipo() == TokenType.LITERAL_TEXTO || espiar().getTipo() == TokenType.DECLARACAO_VARIAVEL || espiar().getTipo() == TokenType.IDENTIFICADOR || espiar().getTipo() == TokenType.PONTUACAO_ABRE_PARENTESES) {
            value = parseExpression();
        } else {
            throw error(espiar(), "Esperado 'valendo, vale, seja, etc' após nome da variável");
        }

        return new Declaration(tipo, varName, value);
    }

    private Statement parsePrintStatement() {
        pularRuidos();

        List<Statement> prints = new ArrayList<>();

        //primeiro alvo: aceita qualquer expressão
        Expression expr = parseExpression();
        prints.add(new PrintStatement(expr));

        while (checar(TokenType.CONECTOR_E) && espiaExpressaoInicial()) {
            // consome o 'e' que separa alvos
            avancar();
            pularRuidos();
            // aceita mais expressões
            Expression e = parseExpression();
            prints.add(new PrintStatement(e));
        }

        if (prints.size() == 1) return prints.get(0);
        return new SequenceStatement(prints);
    }

    private IfStatement parseIfStatement() {
        pularRuidos();
        Expression condition = parseExpression();
        pularRuidos();
        // consome opcionalmente 'entao'
        match(TokenType.CONDICIONAL_ENTAO);
        pularRuidos();
        Statement thenCondition = parseStatement();
        Statement elseCondition = null;
        if (match(TokenType.CONDICIONAL_SENAO)) {
            pularRuidos();
            elseCondition = parseStatement();
        }
        return new IfStatement(condition, thenCondition, elseCondition);
    }

    private Expression parseExpression() {
        return parseLogicalOr();
    }

    private Expression parseLogicalOr() {
        Expression expr = parseLogicalAnd();
        while (checar(TokenType.CONECTOR_OU) && nextIsStartOfExpression()) {
            // consume operador
            avancar();
            Token operator = anterior();
            Expression right = parseLogicalAnd();
            expr = new BinaryExpression(expr, operator, right);
        }
        return expr;
    }

    private Expression parseLogicalAnd() {
        Expression expr = parseEquality();
        while (checar(TokenType.CONECTOR_E) && nextIsStartOfExpression()) {
            // consume operador
            avancar();
            Token operator = anterior();
            Expression right = parseEquality();
            expr = new BinaryExpression(expr, operator, right);
        }
        return expr;
    }

    private boolean nextIsStartOfExpression() {
        int idx = pos + 1;
        if (idx >= tokens.size()) return false;
        TokenType t = tokens.get(idx).getTipo();
        return t == TokenType.LITERAL_NUMERICO || t == TokenType.LITERAL_TEXTO || t == TokenType.DECLARACAO_VARIAVEL || t == TokenType.IDENTIFICADOR || t == TokenType.PONTUACAO_ABRE_PARENTESES || t == TokenType.VERBO_SUBTRAIR;
    }

    private Expression parseEquality() {
        Expression expr = parseComparison();
        while (match(TokenType.COMPARADOR_IGUAL, TokenType.COMPARADOR_DIFERENTE)) {
            Token operator = anterior();
            Expression right = parseComparison();
            expr = new BinaryExpression(expr, operator, right);
        }
        return expr;
    }

    private Expression parseComparison() {
        Expression expr = parseTerm();
        while (match(TokenType.COMPARADOR_MAIOR, TokenType.COMPARADOR_MENOR, TokenType.COMPARADOR_MAIOR_IGUAL, TokenType.COMPARADOR_MENOR_IGUAL)) {
            Token operator = anterior();
            Expression right = parseTerm();
            expr = new BinaryExpression(expr, operator, right);
        }
        return expr;
    }

    private Expression parseTerm() {
        Expression expr = parseFactor();
        while (match(TokenType.VERBO_SOMAR, TokenType.VERBO_SUBTRAIR)) {
            Token operator = anterior();
            Expression right = parseFactor();
            expr = new BinaryExpression(expr, operator, right);
        }
        return expr;
    }

    private Expression parseFactor() {
        Expression expr = parseUnary();
        while (match(TokenType.VERBO_MULTIPLICAR, TokenType.VERBO_DIVIDIR)) {
            Token operator = anterior();
            Expression right = parseUnary();
            expr = new BinaryExpression(expr, operator, right);
        }
        return expr;
    }

    private Expression parseUnary() {
        if (match(TokenType.VERBO_SUBTRAIR)) {
            Token operator = anterior();
            Expression right = parseUnary();
            return new UnaryExpression(operator, right);
        }
        return parsePrimary();
    }

    private Expression parsePrimary() {
        pularRuidos();
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
        //se o token é CONECTOR_RUIDO mas o valor corresponde a uma variável declarada como variável (aceita usos sem $ quando já foi declarada)
        if (checar(TokenType.CONECTOR_RUIDO) && declaredVars.contains(espiar().getValor())) {
            Token t = avancar();
            String varName = t.getValor();
            return new Variable(varName);
        }
        if (match(TokenType.DECLARACAO_VARIAVEL) || match(TokenType.IDENTIFICADOR)) {
            String varName = anterior().getValor();
            if (varName.startsWith("$")) varName = varName.substring(1);
            return new Variable(varName);
        }

        // mais uma condição para caso a pessoa coloque parênteses
        if (match(TokenType.PONTUACAO_ABRE_PARENTESES)) {
            Expression expr = parseExpression();
            consome(TokenType.PONTUACAO_FECHA_PARENTESES, "Esperado ')' após expressão");
            return expr;
        }

        throw error(espiar(), "Esperado uma expressão (literal, variável ou parênteses)");
    }

    private void pularRuidos() {
        while (!estaNoFinal() && espiar().getTipo() == TokenType.CONECTOR_RUIDO) {
            // Se o ruído é o nome de uma variável declarada ele não vai pular
            String val = espiar().getValor();
            if (declaredVars.contains(val)) break;
            avancar();
        }
    }

    private boolean match(TokenType... types) {
        pularRuidos();
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
        pularRuidos();
        if (checar(type)) return avancar();
        throw error(espiar(), message);
    }

    private ParseException error(Token token, String message) {
        String encontrado = token.getValor();

        if (token.getTipo() == TokenType.ERRO_LEXICO && encontrado.startsWith("$")) {
            encontrado = encontrado.substring(1);
        }

        return new ParseException(
            String.format("Erro: %s (encontrado '%s')", message, encontrado)
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

    private boolean espiaExpressaoInicial() {
        int idx = pos + 1;
        if (idx >= tokens.size()) return false;
        TokenType t = tokens.get(idx).getTipo();
        return t == TokenType.LITERAL_NUMERICO || t == TokenType.LITERAL_TEXTO || t == TokenType.DECLARACAO_VARIAVEL || t == TokenType.IDENTIFICADOR || t == TokenType.PONTUACAO_ABRE_PARENTESES || t == TokenType.VERBO_SUBTRAIR || (t == TokenType.CONECTOR_RUIDO && declaredVars.contains(tokens.get(idx).getValor()));
    }
}
