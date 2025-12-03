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
    private boolean allowLogicalOperators = true; // controla se 'e'/'ou' são tratados como operadores lógicos

    public AnalisadorSintatico(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Program parse() {
        List<Statement> statements = new ArrayList<>();

        while (!estaNoFinal()) {
            pularRuidoEIdentificadores();

            if (estaNoFinal()) break;

            List<Statement> seq = new ArrayList<>();
            seq.add(parseStatement());

            while (match(TokenType.CONECTOR_E)) {
                pularRuidos();
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
        if (checar(TokenType.IDENTIFICADOR) || checar(TokenType.DECLARACAO_VARIAVEL)) {
            int savePos = pos;
            Token varToken = avancar();
            String varName = varToken.getValor();
            if (varName.startsWith("$")) varName = varName.substring(1);

            pularRuidos();

            if (checar(TokenType.VERBO_ATRIBUIR)) {
                avancar();
                pularRuidoEIdentificadores();
                Expression value = parseExpression();
                return new Assignment(varName, value);
            }
            else if (checar(TokenType.VERBO_SOMANDO) || checar(TokenType.VERBO_SUBTRAINDO)) {
                Token opToken = avancar();
                pularRuidoEIdentificadores();

                if (checar(TokenType.VERBO_SOMAR) || checar(TokenType.VERBO_SUBTRAIR)) {
                    avancar();
                }

                pularRuidoEIdentificadores();
                Expression right = parseExpression();

                Token operador;
                if (opToken.getTipo() == TokenType.VERBO_SOMANDO) {
                    operador = new Token(TokenType.VERBO_SOMAR, "+");
                } else {
                    operador = new Token(TokenType.VERBO_SUBTRAIR, "-");
                }

                Expression value = new BinaryExpression(new Variable(varName), operador, right);
                return new Assignment(varName, value);
            }
            else {
                pos = savePos;
            }
        }
        pularApenasRuidos();

        if (match(TokenType.VERBO_CRIAR)) {
            return parseCreateStatements();
        }
        if (match(TokenType.VERBO_MOSTRAR)) {
            return parsePrintStatement();
        }
        if (match(TokenType.CONDICIONAL_SE)) {
            return parseIfStatement();
        }
        if (match(TokenType.REPETICAO_ENQUANTO)) {
            return parseWhileStatement();
        }

        throw error(espiar(), "Esperado um comando (criar, mostrar, se, enquanto, etc)");
    }

    private Statement parseCreateStatements() {
        List<Statement> decls = new ArrayList<>();
        decls.add(parseSingleDeclaration());

        while (match(TokenType.CONECTOR_E)) {
            pularRuidoEIdentificadores();
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
        int idx = pos;

        // Pula ruídos e identificadores
        while (idx < tokens.size() &&
                (tokens.get(idx).getTipo() == TokenType.CONECTOR_RUIDO ||
                        tokens.get(idx).getTipo() == TokenType.IDENTIFICADOR)) {
            idx++;
        }

        if (idx >= tokens.size()) return false;
        TokenType t = tokens.get(idx).getTipo();
        return t == TokenType.TIPO_NUMERICO || t == TokenType.TIPO_TEXTO || t == TokenType.TIPO_BOOLEANO;
    }

    private Declaration parseSingleDeclaration() {
        pularRuidoEIdentificadores();

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

        pularRuidoEIdentificadores();
        match(TokenType.CONECTOR_PARA);

        pularRuidoEIdentificadores();
        Token name = consome(TokenType.DECLARACAO_VARIAVEL, "Esperado declaração de variável (começando com $)");

        String varName = name.getValor();
        if (varName.startsWith("$")) {
            varName = varName.substring(1);
        }
        declaredVars.add(varName);

        pularRuidoEIdentificadores();
        Expression value;

        while (!estaNoFinal() && (checar(TokenType.CONECTOR_E) || checar(TokenType.CONECTOR_RUIDO))) {
            avancar();
            pularRuidoEIdentificadores();
        }

        if (match(TokenType.VERBO_ATRIBUIR)) {
            pularRuidoEIdentificadores();
            value = parseExpression();
        } else if (espiar().getTipo() == TokenType.LITERAL_NUMERICO ||
                espiar().getTipo() == TokenType.LITERAL_TEXTO ||
                espiar().getTipo() == TokenType.DECLARACAO_VARIAVEL ||
                espiar().getTipo() == TokenType.IDENTIFICADOR ||
                espiar().getTipo() == TokenType.PONTUACAO_ABRE_PARENTESES) {
            value = parseExpression();
        } else {
            throw error(espiar(), "Esperado 'valendo, vale, seja, etc' após nome da variável");
        }

        return new Declaration(tipo, varName, value);
    }

    private Statement parsePrintStatement() {
        pularRuidoEIdentificadores();

        List<Statement> prints = new ArrayList<>();
        // Desabilita operadores lógicos dentro do parsing dos argumentos do 'mostre'
        boolean previousAllow = allowLogicalOperators;
        allowLogicalOperators = false;
        try {
            Expression expr = parseExpression();
            prints.add(new PrintStatement(expr));

            while (checar(TokenType.CONECTOR_E) && espiaExpressaoInicial()) {
                avancar();
                pularRuidoEIdentificadores();
                Expression e = parseExpression();
                prints.add(new PrintStatement(e));
            }
        } finally {
            allowLogicalOperators = previousAllow;
        }

        if (prints.size() == 1) return prints.get(0);
        return new SequenceStatement(prints);
    }

    private IfStatement parseIfStatement() {
        pularRuidoEIdentificadores();
        Expression condition = parseExpression();
        pularRuidoEIdentificadores();
        match(TokenType.CONDICIONAL_ENTAO);
        pularRuidoEIdentificadores();
        Statement thenCondition = parseStatement();
        Statement elseCondition = null;
        if (match(TokenType.CONDICIONAL_SENAO)) {
            pularRuidoEIdentificadores();
            elseCondition = parseStatement();
        }
        return new IfStatement(condition, thenCondition, elseCondition);
    }

    private WhileStatement parseWhileStatement() {
        pularRuidoEIdentificadores();

        Expression condition = parseExpression();

        pularRuidoEIdentificadores();

        match(TokenType.REPETICAO_REPITA);

        pularRuidoEIdentificadores();

        List<Statement> body = new ArrayList<>();

        if (match(TokenType.PONTUACAO_ABRE_CHAVES)) {
            pularApenasRuidos();

            while (!estaNoFinal() && !checar(TokenType.PONTUACAO_FECHA_CHAVES)) {
                body.add(parseStatement());
                pularApenasRuidos();

                while (match(TokenType.CONECTOR_E)) {
                    pularApenasRuidos();
                    if (!estaNoFinal() && !checar(TokenType.PONTUACAO_FECHA_CHAVES)) {
                        body.add(parseStatement());
                        pularApenasRuidos();
                    }
                }
            }

            consome(TokenType.PONTUACAO_FECHA_CHAVES, "Esperado '}' para fechar o bloco do enquanto");
        } else {
            body.add(parseStatement());
        }

        return new WhileStatement(condition, body);
    }

    private Expression parseExpression() {
        return parseLogicalOr();
    }

    private Expression parseLogicalOr() {
        Expression expr = parseLogicalAnd();
        while (allowLogicalOperators && checar(TokenType.CONECTOR_OU) && nextIsStartOfExpression()) {
            avancar();
            Token operator = anterior();
            Expression right = parseLogicalAnd();
            expr = new BinaryExpression(expr, operator, right);
        }
        return expr;
    }

    private Expression parseLogicalAnd() {
        Expression expr = parseEquality();
        while (allowLogicalOperators && checar(TokenType.CONECTOR_E) && nextIsStartOfExpression()) {
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
        return t == TokenType.LITERAL_NUMERICO || t == TokenType.LITERAL_TEXTO ||
                t == TokenType.DECLARACAO_VARIAVEL || t == TokenType.IDENTIFICADOR ||
                t == TokenType.PONTUACAO_ABRE_PARENTESES || t == TokenType.VERBO_SUBTRAIR;
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
        while (match(TokenType.COMPARADOR_MAIOR, TokenType.COMPARADOR_MENOR,
                TokenType.COMPARADOR_MAIOR_IGUAL, TokenType.COMPARADOR_MENOR_IGUAL)) {
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
        pularRuidoEIdentificadores();

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

        if (checar(TokenType.CONECTOR_RUIDO) && declaredVars.contains(espiar().getValor())) {
            Token t = avancar();
            return new Variable(t.getValor());
        }

        if (match(TokenType.DECLARACAO_VARIAVEL) || match(TokenType.IDENTIFICADOR)) {
            String varName = anterior().getValor();
            if (varName.startsWith("$")) varName = varName.substring(1);
            return new Variable(varName);
        }

        if (match(TokenType.PONTUACAO_ABRE_PARENTESES)) {
            Expression expr = parseExpression();
            consome(TokenType.PONTUACAO_FECHA_PARENTESES, "Esperado ')' após expressão");
            return expr;
        }

        throw error(espiar(), "Esperado uma expressão (literal, variável ou parênteses)");
    }

    /**
     * Pula tanto CONECTOR_RUIDO quanto IDENTIFICADORES soltos
     * Isso permite frases como "Mano crie uma..." ou "Cara mostre..."
     */
    private void pularRuidoEIdentificadores() {
        while (!estaNoFinal()) {
            TokenType tipo = espiar().getTipo();
            String valor = espiar().getValor();

            // Pula ruído
            if (tipo == TokenType.CONECTOR_RUIDO) {
                // Mas não pula se for uma variável declarada
                if (declaredVars.contains(valor)) break;
                avancar();
                continue;
            }

            // Pula identificadores soltos APENAS se o próximo for um verbo/comando
            if (tipo == TokenType.IDENTIFICADOR && !declaredVars.contains(valor)) {
                // Espia o próximo token
                if (proximoEhComando()) {
                    avancar(); // Pula o identificador
                    continue;
                }
            }

            break;
        }
    }

    private boolean proximoEhComando() {
        int idx = pos + 1;

        // Pula ruídos
        while (idx < tokens.size() && tokens.get(idx).getTipo() == TokenType.CONECTOR_RUIDO) {
            idx++;
        }

        if (idx >= tokens.size()) return false;

        TokenType t = tokens.get(idx).getTipo();
        return t == TokenType.VERBO_CRIAR ||
                t == TokenType.VERBO_MOSTRAR ||
                t == TokenType.CONDICIONAL_SE ||
                t == TokenType.REPETICAO_ENQUANTO ||
                t == TokenType.TIPO_NUMERICO ||
                t == TokenType.TIPO_TEXTO ||
                t == TokenType.TIPO_BOOLEANO;
    }

    private void pularRuidos() {
        while (!estaNoFinal() && espiar().getTipo() == TokenType.CONECTOR_RUIDO) {
            String val = espiar().getValor();
            if (declaredVars.contains(val)) break;
            avancar();
        }
    }

    private void pularApenasRuidos() {
        while (!estaNoFinal() && espiar().getTipo() == TokenType.CONECTOR_RUIDO) {
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
        System.out.println("\nVariáveis declaradas: " + program.getAllDeclaredVariables());
        System.out.println("Variáveis usadas: " + program.getAllUsedVariables());
        System.out.println("\nÁrvore sintática:");
        System.out.println("-".repeat(60));
        System.out.println(program);
        System.out.println("-".repeat(60));
    }

    private boolean espiaExpressaoInicial() {
        int idx = pos + 1;
        if (idx >= tokens.size()) return false;
        TokenType t = tokens.get(idx).getTipo();

        if (t == TokenType.IDENTIFICADOR || t == TokenType.DECLARACAO_VARIAVEL) {
            int nextIdx = idx + 1;
            while (nextIdx < tokens.size() && tokens.get(nextIdx).getTipo() == TokenType.CONECTOR_RUIDO) {
                nextIdx++;
            }
            if (nextIdx < tokens.size()) {
                TokenType nextType = tokens.get(nextIdx).getTipo();
                if (nextType == TokenType.VERBO_SOMANDO || nextType == TokenType.VERBO_SUBTRAINDO || nextType == TokenType.VERBO_ATRIBUIR) {
                    return false;
                }
            }
        }

        return t == TokenType.LITERAL_NUMERICO || t == TokenType.LITERAL_TEXTO ||
                t == TokenType.DECLARACAO_VARIAVEL || t == TokenType.IDENTIFICADOR ||
                t == TokenType.PONTUACAO_ABRE_PARENTESES || t == TokenType.VERBO_SUBTRAIR ||
                (t == TokenType.CONECTOR_RUIDO && declaredVars.contains(tokens.get(idx).getValor()));
    }
}