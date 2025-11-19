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

    /**
     * Analisa o programa completo
     */
    public void analisar(Program program) throws SemanticException {
        System.out.println("\n=== ANÁLISE SEMÂNTICA ===");

        // Primeira passagem: coleta todas as declarações
        Set<String> variaveisDeclaradas = new HashSet<>();
        for (Statement stmt : program.statements()) {
            if (stmt instanceof Declaration) {
                variaveisDeclaradas.add(((Declaration) stmt).nome());
            }
        }

        // Segunda passagem: valida statements
        for (Statement stmt : program.statements()) {
            analisarStatement(stmt, variaveisDeclaradas);
        }

        // Exibe a tabela de símbolos
        tabelaSimbolos.printScopes();

        // Relatório de erros
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
        System.out.println("   • Variáveis declaradas: " + variaveisDeclaradas.size());
        System.out.println("   • Sem erros detectados");
    }

    /**
     * Analisa um statement
     */
    private void analisarStatement(Statement stmt, Set<String> todasVariaveis) {
        if (stmt instanceof Declaration) {
            analisarDeclaration((Declaration) stmt);
        } else if (stmt instanceof PrintStatement) {
            analisarPrintStatement((PrintStatement) stmt, todasVariaveis);
        }
        // Adicione outros tipos de statement conforme necessário
    }

    /**
     * Analisa declaração de variável
     */
    private void analisarDeclaration(Declaration decl) {
        String nomeVar = decl.nome();
        TokenType tipoVar = decl.tipo();

        // 1. Verifica se variável já foi declarada no escopo atual
        if (tabelaSimbolos.existsInCurrentScope(nomeVar)) {
            erros.add(String.format(
                    "Redeclaração de variável: '%s' já foi declarada neste escopo",
                    nomeVar
            ));
            return;
        }

        // 2. Analisa a expressão de inicialização
        TokenType tipoExpressao = inferirTipo(decl.inicializador());

        // 3. Verifica se houve erro na inferência
        if (tipoExpressao == TokenType.ERRO_LEXICO) {
            // Erro já foi adicionado em inferirTipo()
            return;
        }

        // 4. Verifica compatibilidade de tipos
        if (!tiposCompativeis(tipoVar, tipoExpressao)) {
            erros.add(String.format(
                    "Incompatibilidade de tipos: variável '%s' é do tipo '%s' mas recebeu valor do tipo '%s'",
                    nomeVar,
                    tipoParaString(tipoVar),
                    tipoParaString(tipoExpressao)
            ));
            return;
        }

        // 5. Extrai o valor do inicializador
        Object valorInicial = extrairValor(decl.inicializador());

        // 6. Adiciona variável na tabela de símbolos com valor
        Simbolo simbolo = new Simbolo(nomeVar, tipoVar, valorInicial);
        tabelaSimbolos.declare(nomeVar, simbolo);

        System.out.printf("Declarada: %-15s : %-10s = %s%n",
                nomeVar,
                tipoParaString(tipoVar),
                formatarValor(valorInicial)
        );
    }

    /**
     * Analisa statement de impressão
     */
    private void analisarPrintStatement(PrintStatement print, Set<String> todasVariaveis) {
        Expression expr = print.expression();

        if (expr instanceof Variable(String name)) {

            // Verifica se variável foi declarada
            if (!tabelaSimbolos.exists(name)) {
                // Verifica se será declarada depois
                if (todasVariaveis.contains(name) && !variaveisUsadasAntesDeclaracao.contains(name)) {
                    erros.add(String.format(
                            "Uso antes da declaração: variável '%s' é usada antes de ser declarada",
                            name
                    ));
                    variaveisUsadasAntesDeclaracao.add(name);
                } else if (!todasVariaveis.contains(name)) {
                    erros.add(String.format(
                            "Variável não declarada: '%s' não foi declarada em nenhum lugar",
                            name
                    ));
                }
                return;
            }

            Simbolo simbolo = tabelaSimbolos.lookup(name);
            System.out.printf("Uso válido: %-15s (tipo: %s)%n",
                    name,
                    tipoParaString(simbolo.getTipoVariavel())
            );
        }
    }

    /**
     * Infere o tipo de uma expressão
     */
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
        } else if (expr instanceof Variable(String name)) {
            Simbolo simbolo = tabelaSimbolos.lookup(name);

            if (simbolo == null) {
                erros.add(String.format(
                        "Variável não declarada: '%s' usada na inicialização não existe",
                        name
                ));
                return TokenType.ERRO_LEXICO;
            }

            return simbolo.getTipoVariavel();
        }

        return TokenType.ERRO_LEXICO;
    }

    /**
     * Extrai o valor de uma expressão
     */
    private Object extrairValor(Expression expr) {
        if (expr instanceof Literal) {
            return ((Literal) expr).getValue();
        } else if (expr instanceof Variable(String name)) {
            Simbolo simbolo = tabelaSimbolos.lookup(name);
            if (simbolo != null) {
                return simbolo.getValor();
            }
        }
        return null;
    }

    /**
     * Formata um valor para exibição
     */
    private String formatarValor(Object valor) {
        if (valor == null) {
            return "null";
        }
        if (valor instanceof String) {
            return "\"" + valor + "\"";
        }
        return valor.toString();
    }

    /**
     * Verifica compatibilidade entre tipos
     */
    private boolean tiposCompativeis(TokenType tipoEsperado, TokenType tipoRecebido) {
        if (tipoRecebido == TokenType.ERRO_LEXICO) {
            return false;
        }
        return tipoEsperado == tipoRecebido;
    }

    /**
     * Converte TokenType para string legível
     */
    private String tipoParaString(TokenType tipo) {
        return switch (tipo) {
            case TIPO_NUMERICO -> "número";
            case TIPO_TEXTO -> "texto";
            case TIPO_BOOLEANO -> "booleano";
            default -> "desconhecido";
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