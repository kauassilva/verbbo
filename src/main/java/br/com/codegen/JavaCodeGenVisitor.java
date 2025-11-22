package br.com.codegen;

import br.com.ast.*;
import br.com.ast.interfaces.Expression;
import br.com.ast.interfaces.Statement;
import br.com.ast.interfaces.Visitor;
import br.com.token.Token;
import br.com.token.TokenType;
import br.com.utils.Simbolo;
import br.com.utils.TabelaSimbolos;

public class JavaCodeGenVisitor implements Visitor<Void> {
    private final CodeWriter w;
    private final TabelaSimbolos tabela;

    public JavaCodeGenVisitor(CodeWriter writer, TabelaSimbolos tabela) {
        this.w = writer;
        this.tabela = tabela;
    }

    @Override
    public Void visitProgram(Program program) {
        // emit statements inside main
        for (Statement s : program.statements()) {
            s.accept(this);
        }
        return null;
    }

    @Override
    public Void visitDeclaration(Declaration declaration) {
        String nome = declaration.nome();
        TokenType tipo = declaration.tipo();

        Simbolo simbolo = tabela.lookup(nome);

        String javaType = mapType(simbolo != null ? simbolo.getTipoVariavel() : tipo);
        if (declaration.inicializador() != null) {
            String expr = emitExpression(declaration.inicializador());
            w.writeLine(javaType + " " + nome + " = " + expr + ";");
        } else {
            w.writeLine(javaType + " " + nome + " = " + defaultValueFor(javaType) + ";");
        }
        return null;
    }

    @Override
    public Void visitPrintStatement(PrintStatement printStatement) {
        String expr = emitExpression(printStatement.expression());
        w.writeLine("System.out.println(" + expr + ");");
        return null;
    }

    @Override
    public Void visitLiteral(Literal literal) {
        // not directly used; literals are emitted via emitExpression
        return null;
    }

    @Override
    public Void visitVariable(Variable variable) {
        // variables are emitted by emitExpression
        return null;
    }

    @Override
    public Void visitBinaryExpression(BinaryExpression binaryExpression) {
        // handled by emitExpression
        return null;
    }

    @Override
    public Void visitUnaryExpression(UnaryExpression unaryExpression) {
        // handled by emitExpression
        return null;
    }

    @Override
    public Void visitIfStatement(IfStatement ifStatement) {
        String cond = emitExpression(ifStatement.condition());
        w.writeLine("if (" + cond + ") {");
        w.indent();
        if (ifStatement.thenCondition() != null) {
            ifStatement.thenCondition().accept(this);
        }
        w.unindent();
        if (ifStatement.elseCondition() != null) {
            w.writeLine("} else {");
            w.indent();
            ifStatement.elseCondition().accept(this);
            w.unindent();
        }
        w.writeLine("}");
        return null;
    }

    @Override
    public Void visitSequenceStatement(SequenceStatement sequenceStatement) {
        for (Statement s : sequenceStatement.statements()) {
            s.accept(this);
        }
        return null;
    }

    // helper methods
    private String emitExpression(Expression expr) {
        if (expr instanceof Literal) {
            Object v = ((Literal) expr).getValue();
            if (v instanceof String) return quoteString((String) v);
            if (v instanceof Integer) return v.toString();
            if (v instanceof Double) {
                String s = v.toString();
                if (!s.contains(".")) s = s + ".0";
                return s;
            }
            if (v instanceof Boolean) return v.toString();
            return "null";
        }
        if (expr instanceof Variable) {
            return ((Variable) expr).name();
        }
        if (expr instanceof BinaryExpression) {
            BinaryExpression b = (BinaryExpression) expr;
            String left = emitExpression(b.left());
            String right = emitExpression(b.right());
            Token op = b.operator();
            String javaOp = mapOperator(op);
            return "(" + left + " " + javaOp + " " + right + ")";
        }
        if (expr instanceof UnaryExpression) {
            UnaryExpression u = (UnaryExpression) expr;
            String right = emitExpression(u.right());
            String op = mapUnaryOperator(u.operator());
            return "(" + op + right + ")";
        }
        return "/*unsupported_expr*/null";
    }

    private String mapType(TokenType tipo) {
        if (tipo == null) return "Object";
        return switch (tipo) {
            case TIPO_NUMERICO, LITERAL_NUMERICO -> "double"; // use primitive double
            case TIPO_TEXTO, LITERAL_TEXTO -> "String";
            case TIPO_BOOLEANO, LITERAL_BOOLEANO -> "boolean";
            default -> "Object";
        };
    }

    private String mapOperator(Token op) {
        if (op == null) return "/*?*/";
        TokenType t = op.getTipo();
        return switch (t) {
            case VERBO_SOMAR -> "+";
            case VERBO_SUBTRAIR -> "-";
            case VERBO_MULTIPLICAR -> "*";
            case VERBO_DIVIDIR -> "/";
            case COMPARADOR_MAIOR -> ">";
            case COMPARADOR_MENOR -> "<";
            case COMPARADOR_MAIOR_IGUAL -> ">=";
            case COMPARADOR_MENOR_IGUAL -> "<=";
            case COMPARADOR_IGUAL -> "==";
            case COMPARADOR_DIFERENTE -> "!=";
            case CONECTOR_E -> "&&";
            case CONECTOR_OU -> "||";
            default -> op.getValor();
        };
    }

    private String mapUnaryOperator(Token op) {
        if (op == null) return "";
        TokenType t = op.getTipo();
        return switch (t) {
            case VERBO_SUBTRAIR -> "-";
            default -> op.getValor();
        };
    }

    private String quoteString(String s) {
        if (s == null) return "null";
        String escaped = s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
        return "\"" + escaped + "\"";
    }

    private String defaultValueFor(String javaType) {
        return switch (javaType) {
            case "double" -> "0.0";
            case "String" -> "\"\"";
            case "boolean" -> "false";
            default -> "null";
        };
    }
}
