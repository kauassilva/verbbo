package br.com.analisador;

import br.com.ast.*;
import br.com.ast.interfaces.Expression;
import br.com.ast.interfaces.Statement;
import br.com.excecoes.SemanticException;
import br.com.token.TokenType;
import br.com.utils.Simbolo;
import br.com.utils.TabelaSimbolos;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AnalisadorSemantico {
    private final TabelaSimbolos tabelaSimbolos;
    private final List<String> erros;
    private final Set<String> variaveisUsadasAntesDeclaracao;

    public AnalisadorSemantico() {
        this.tabelaSimbolos = new TabelaSimbolos();
        this.erros = new java.util.ArrayList<>();
        this.variaveisUsadasAntesDeclaracao = new HashSet<>();
    }

    public void analisar(Program program) throws SemanticException {
        System.out.println("\n=== ANÁLISE SEMÂNTICA ===");

        // Primeira passagem: coleta declarações
        Set<String> variaveisDeclaradas = coletarDeclaracoes(program.statements());

        // Segunda passagem: valida statements
        for (Statement stmt : program.statements()) {
            analisarStatement(stmt, variaveisDeclaradas);
        }

        tabelaSimbolos.printScopes();

        if (!erros.isEmpty()) {
            System.err.println("\n" + "=".repeat(60));
            System.err.println("ERROS SEMÂNTICOS ENCONTRADOS: " + erros.size());
            System.err.println("=".repeat(60));
            for (int i = 0; i < erros.size(); i++) {
                System.err.printf("%2d. %s%n", i + 1, erros.get(i));
            }
            System.err.println("=".repeat(60));
            throw new SemanticException("Análise semântica falhou com " + erros.size() + " erro(s)");
        }

        System.out.println("Análise semântica concluída com sucesso!");
        System.out.println(" - Variáveis declaradas: " + variaveisDeclaradas.size());
        System.out.println(" - Sem erros detectados");
    }

    private Set<String> coletarDeclaracoes(List<Statement> statements) {
        Set<String> vars = new HashSet<>();
        for (Statement stmt : statements) {
            if (stmt instanceof Declaration decl) {
                vars.add(decl.nome());
            } else if (stmt instanceof SequenceStatement seq) {
                vars.addAll(coletarDeclaracoes(seq.statements()));
            } else if (stmt instanceof IfStatement ifStmt) {
                vars.addAll(coletarDeclaracoes(List.of(ifStmt.thenCondition())));
                if (ifStmt.elseCondition() != null) {
                    vars.addAll(coletarDeclaracoes(List.of(ifStmt.elseCondition())));
                }
            } else if (stmt instanceof WhileStatement whileStmt) {
                vars.addAll(coletarDeclaracoes(whileStmt.body()));
            } else if (stmt instanceof BlockStatement block) {
                vars.addAll(coletarDeclaracoes(block.statements()));
            }
        }
        return vars;
    }

    private void analisarStatement(Statement stmt, Set<String> todasVariaveis) {
        if (stmt instanceof Declaration decl) {
            analisarDeclaration(decl);
        } else if (stmt instanceof PrintStatement print) {
            analisarPrintStatement(print, todasVariaveis);
        } else if (stmt instanceof Assignment assign) {
            analisarAssignment(assign);
        } else if (stmt instanceof IfStatement ifStmt) {
            analisarIfStatement(ifStmt, todasVariaveis);
        } else if (stmt instanceof WhileStatement whileStmt) {
            analisarWhileStatement(whileStmt, todasVariaveis);
        } else if (stmt instanceof BlockStatement block) {
            analisarBlockStatement(block, todasVariaveis);
        } else if (stmt instanceof SequenceStatement seq) {
            analisarSequenceStatement(seq, todasVariaveis);
        }
    }

    private void analisarDeclaration(Declaration decl) {
        String nomeVar = decl.nome();
        TokenType tipoVar = decl.tipo();

        if (tabelaSimbolos.existsInCurrentScope(nomeVar)) {
            erros.add(String.format(
                    "Redeclaração de variável: '%s' já foi declarada neste escopo", nomeVar
            ));
            return;
        }

        TokenType tipoExpressao = inferirTipo(decl.inicializador());

        if (tipoExpressao == TokenType.ERRO_LEXICO) {
            return;
        }

        if (!tiposCompativeis(tipoVar, tipoExpressao)) {
            erros.add(String.format(
                    "Incompatibilidade de tipos: variável '%s' é do tipo '%s' mas recebeu valor do tipo '%s'",
                    nomeVar, tipoParaString(tipoVar), tipoParaString(tipoExpressao)
            ));
            return;
        }

        // Verifica se é um literal direto ou expressão computada
        boolean isLiteralDireto = decl.inicializador() instanceof Literal;
        Object valorInicial = isLiteralDireto ? extrairValor(decl.inicializador()) : null;

        Simbolo simbolo;
        if (isLiteralDireto) {
            // Literal direto: valor conhecido e inicializada=true
            simbolo = new Simbolo(nomeVar, tipoVar, valorInicial);
        } else {
            // Expressão computada: valor null e inicializada=false
            simbolo = new Simbolo(nomeVar, tipoVar);
        }

        tabelaSimbolos.declare(nomeVar, simbolo);

        System.out.printf("Declarada: %-15s : %-10s = %s%s%n",
                nomeVar,
                tipoParaString(tipoVar),
                formatarValor(valorInicial),
                isLiteralDireto ? "" : " (expressão computada)"
        );
    }

    private void analisarAssignment(Assignment assign) {
        String nomeVar = assign.name();

        if (!tabelaSimbolos.exists(nomeVar)) {
            erros.add(String.format("Variável '%s' não foi declarada", nomeVar));
            return;
        }

        Simbolo simbolo = tabelaSimbolos.lookup(nomeVar);
        TokenType tipoVar = simbolo.getTipoVariavel();
        TokenType tipoValor = inferirTipo(assign.value());

        if (!tiposCompativeis(tipoVar, tipoValor)) {
            erros.add(String.format(
                    "Incompatibilidade na atribuição: '%s' é do tipo '%s' mas recebeu '%s'",
                    nomeVar, tipoParaString(tipoVar), tipoParaString(tipoValor)
            ));
            return;
        }

        System.out.printf("Atribuição válida: %s = ... (tipo: %s)%n",
                nomeVar, tipoParaString(tipoVar)
        );
    }

    private void analisarIfStatement(IfStatement ifStmt, Set<String> todasVariaveis) {
        TokenType tipoCondicao = inferirTipo(ifStmt.condition());

        if (tipoCondicao != TokenType.TIPO_BOOLEANO && tipoCondicao != TokenType.ERRO_LEXICO) {
            erros.add(String.format(
                    "Condição do 'se' deve ser booleana, mas é do tipo '%s'",
                    tipoParaString(tipoCondicao)
            ));
        }

        System.out.println("Analisando bloco 'se'");

        tabelaSimbolos.pushScope();
        analisarStatement(ifStmt.thenCondition(), todasVariaveis);
        tabelaSimbolos.popScope();

        if (ifStmt.elseCondition() != null) {
            System.out.println("Analisando bloco 'senão'");
            tabelaSimbolos.pushScope();
            analisarStatement(ifStmt.elseCondition(), todasVariaveis);
            tabelaSimbolos.popScope();
        }
    }

    private void analisarWhileStatement(WhileStatement whileStmt, Set<String> todasVariaveis) {
        TokenType tipoCondicao = inferirTipo(whileStmt.condition());

        if (tipoCondicao != TokenType.TIPO_BOOLEANO && tipoCondicao != TokenType.ERRO_LEXICO) {
            erros.add(String.format(
                    "Condição do 'enquanto' deve ser booleana, mas é do tipo '%s'",
                    tipoParaString(tipoCondicao)
            ));
        }

        System.out.println("Analisando bloco 'enquanto'");

        tabelaSimbolos.pushScope();
        for (Statement stmt : whileStmt.body()) {
            analisarStatement(stmt, todasVariaveis);
        }
        tabelaSimbolos.popScope();
    }

    private void analisarBlockStatement(BlockStatement block, Set<String> todasVariaveis) {
        tabelaSimbolos.pushScope();
        for (Statement stmt : block.statements()) {
            analisarStatement(stmt, todasVariaveis);
        }
        tabelaSimbolos.popScope();
    }

    private void analisarSequenceStatement(SequenceStatement seq, Set<String> todasVariaveis) {
        for (Statement stmt : seq.statements()) {
            analisarStatement(stmt, todasVariaveis);
        }
    }

    private void analisarPrintStatement(PrintStatement print, Set<String> todasVariaveis) {
        Expression expr = print.expression();

        if (expr instanceof Variable var) {
            String name = var.name();

            if (!tabelaSimbolos.exists(name)) {
                if (todasVariaveis.contains(name) && !variaveisUsadasAntesDeclaracao.contains(name)) {
                    erros.add(String.format(
                            "Uso antes da declaração: variável '%s' é usada antes de ser declarada", name
                    ));
                    variaveisUsadasAntesDeclaracao.add(name);
                } else if (!todasVariaveis.contains(name)) {
                    erros.add(String.format(
                            "Variável não declarada: '%s' não foi declarada em nenhum lugar", name
                    ));
                }
                return;
            }

            Simbolo simbolo = tabelaSimbolos.lookup(name);
            System.out.printf("Uso válido: %-15s (tipo: %s)%n",
                    name, tipoParaString(simbolo.getTipoVariavel())
            );
        } else {
            // Se não for variável, apenas verifica o tipo da expressão
            inferirTipo(expr);
        }
    }

    private TokenType inferirTipo(Expression expr) {
        if (expr instanceof Literal literal) {
            Object value = literal.getValue();

            if (value instanceof Integer || value instanceof Double) {
                return TokenType.TIPO_NUMERICO;
            } else if (value instanceof String) {
                return TokenType.TIPO_TEXTO;
            } else if (value instanceof Boolean) {
                return TokenType.TIPO_BOOLEANO;
            }
        } else if (expr instanceof Variable var) {
            Simbolo simbolo = tabelaSimbolos.lookup(var.name());

            if (simbolo == null) {
                erros.add(String.format(
                        "Variável não declarada: '%s' usada na expressão não existe", var.name()
                ));
                return TokenType.ERRO_LEXICO;
            }

            return simbolo.getTipoVariavel();
        } else if (expr instanceof BinaryExpression binExpr) {
            return analisarBinaryExpression(binExpr);
        } else if (expr instanceof UnaryExpression unExpr) {
            return analisarUnaryExpression(unExpr);
        }

        return TokenType.ERRO_LEXICO;
    }

    private TokenType analisarBinaryExpression(BinaryExpression binExpr) {
        TokenType tipoEsq = inferirTipo(binExpr.left());
        TokenType tipoDir = inferirTipo(binExpr.right());
        TokenType operador = binExpr.operator().getTipo();

        // Operações aritméticas: +, -, *, /
        if (operador == TokenType.VERBO_SOMAR || operador == TokenType.VERBO_SUBTRAIR ||
                operador == TokenType.VERBO_MULTIPLICAR || operador == TokenType.VERBO_DIVIDIR) {

            if (tipoEsq != TokenType.TIPO_NUMERICO) {
                erros.add(String.format(
                        "Operação aritmética requer números: lado esquerdo é '%s'",
                        tipoParaString(tipoEsq)
                ));
                return TokenType.ERRO_LEXICO;
            }

            if (tipoDir != TokenType.TIPO_NUMERICO) {
                erros.add(String.format(
                        "Operação aritmética requer números: lado direito é '%s'",
                        tipoParaString(tipoDir)
                ));
                return TokenType.ERRO_LEXICO;
            }

            System.out.printf("Operação aritmética válida: %s%n",
                    operadorParaString(operador));
            return TokenType.TIPO_NUMERICO;
        }

        // Comparações: <, >, <=, >=, ==, !=
        if (operador == TokenType.COMPARADOR_MENOR || operador == TokenType.COMPARADOR_MAIOR ||
                operador == TokenType.COMPARADOR_MENOR_IGUAL || operador == TokenType.COMPARADOR_MAIOR_IGUAL ||
                operador == TokenType.COMPARADOR_IGUAL || operador == TokenType.COMPARADOR_DIFERENTE) {

            if (tipoEsq != tipoDir) {
                erros.add(String.format(
                        "Comparação entre tipos diferentes: '%s' e '%s'",
                        tipoParaString(tipoEsq), tipoParaString(tipoDir)
                ));
                return TokenType.ERRO_LEXICO;
            }

            System.out.printf("Comparação válida: %s entre %s%n",
                    operadorParaString(operador), tipoParaString(tipoEsq));
            return TokenType.TIPO_BOOLEANO;
        }

        // Operadores lógicos: e, ou
        if (operador == TokenType.CONECTOR_E || operador == TokenType.CONECTOR_OU) {
            if (tipoEsq != TokenType.TIPO_BOOLEANO || tipoDir != TokenType.TIPO_BOOLEANO) {
                erros.add(String.format(
                        "Operadores lógicos requerem booleanos: recebeu '%s' e '%s'",
                        tipoParaString(tipoEsq), tipoParaString(tipoDir)
                ));
                return TokenType.ERRO_LEXICO;
            }

            System.out.printf("Operação lógica válida: %s%n",
                    operadorParaString(operador));
            return TokenType.TIPO_BOOLEANO;
        }

        return TokenType.ERRO_LEXICO;
    }

    private TokenType analisarUnaryExpression(UnaryExpression unExpr) {
        TokenType tipoOperando = inferirTipo(unExpr.right());

        if (unExpr.operator().getTipo() == TokenType.VERBO_SUBTRAIR) {
            if (tipoOperando != TokenType.TIPO_NUMERICO) {
                erros.add("Operador '-' unário requer número");
                return TokenType.ERRO_LEXICO;
            }
            return TokenType.TIPO_NUMERICO;
        }

        return tipoOperando;
    }

    private Object extrairValor(Expression expr) {
        if (expr instanceof Literal literal) {
            return literal.getValue();
        } else if (expr instanceof Variable var) {
            Simbolo simbolo = tabelaSimbolos.lookup(var.name());
            if (simbolo != null) {
                return simbolo.getValor();
            }
        }
        return null;
    }

    private String formatarValor(Object valor) {
        if (valor == null) return "null";
        if (valor instanceof String) return "\"" + valor + "\"";
        return valor.toString();
    }

    private boolean tiposCompativeis(TokenType tipoEsperado, TokenType tipoRecebido) {
        if (tipoRecebido == TokenType.ERRO_LEXICO) return false;
        return tipoEsperado == tipoRecebido;
    }

    private String tipoParaString(TokenType tipo) {
        return switch (tipo) {
            case TIPO_NUMERICO -> "número";
            case TIPO_TEXTO -> "texto";
            case TIPO_BOOLEANO -> "booleano";
            default -> "desconhecido";
        };
    }

    private String operadorParaString(TokenType op) {
        return switch (op) {
            case VERBO_SOMAR -> "soma (+)";
            case VERBO_SUBTRAIR -> "subtração (-)";
            case VERBO_MULTIPLICAR -> "multiplicação (*)";
            case VERBO_DIVIDIR -> "divisão (/)";
            case COMPARADOR_MENOR -> "menor que (<)";
            case COMPARADOR_MAIOR -> "maior que (>)";
            case COMPARADOR_MENOR_IGUAL -> "menor ou igual (<=)";
            case COMPARADOR_MAIOR_IGUAL -> "maior ou igual (>=)";
            case COMPARADOR_IGUAL -> "igual (==)";
            case COMPARADOR_DIFERENTE -> "diferente (!=)";
            case CONECTOR_E -> "E lógico (&&)";
            case CONECTOR_OU -> "OU lógico (||)";
            default -> op.toString();
        };
    }

    public TabelaSimbolos getTabelaSimbolos() {
        return tabelaSimbolos;
    }

    public List<String> getErros() {
        return erros;
    }

    public boolean temErros() {
        return !erros.isEmpty();
    }
}