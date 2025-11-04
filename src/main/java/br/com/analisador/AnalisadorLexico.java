package br.com.analisador;

import br.com.excecoes.ErroLexicoException;
import br.com.palavras_reservadas.PalavrasReservadas;
import br.com.token.Token;
import br.com.token.TokenType;

import java.util.ArrayList;
import java.util.List;

public class AnalisadorLexico {
    private final String texto;
    private int posicao;
    private char caractereAtual;
    private int tokensDescartados;
    private int linha;
    private int coluna;

    public AnalisadorLexico(String texto) {
        this.texto = texto;
        this.posicao = 0;
        this.caractereAtual = !texto.isEmpty() ? texto.charAt(0) : '\0';
        this.tokensDescartados = 0;
        this.linha = 1;
        this.coluna = 1;
    }

    public List<Token> analisar() throws ErroLexicoException {
        List<Token> tokens = new ArrayList<>();
        tokensDescartados = 0;

        while (caractereAtual != '\0') {
            pularEspacos();

            if (caractereAtual == '\0')
                break;

            Token token;

            if (caractereAtual == '$') {
                token = lerVariavel();
            }

            else if (Character.isDigit(caractereAtual)) {
                token = lerNumero();
            }

            else if (caractereAtual == '"') {
                token = lerTexto();
            }

            else if (Character.isLetter(caractereAtual) || isCaractereAcentuado(caractereAtual)) {
                token = lerPalavra();
            }

            else if (";<>:()=!+-*/".indexOf(caractereAtual) >= 0) {
                token = lerOperadorOuSimbolo();
            } else {
                throw new ErroLexicoException(
                        "Caractere invalido encontrado",
                        posicao,
                        String.valueOf(caractereAtual)
                );
            }

            if (token.getTipo() == TokenType.ERRO) {
                throw new ErroLexicoException(
                        "Token invalido: " + token.getValor(),
                        posicao - token.getValor().length(),
                        token.getValor()
                );
            }


            if (token.getTipo() != TokenType.DESCARTE)
                tokens.add(token);
            else
                tokensDescartados++;
        }

        return tokens;
    }

    public void exibirTokens(List<Token> tokens) {
        System.out.println("\n=== ANALISE LEXICA ===");
        System.out.println("Total de tokens encontrados: " + tokens.size());

        if (tokensDescartados > 0) {
            System.out.println("Tokens descartados (artigos/preposicoes): " + tokensDescartados);
        }

        System.out.println("\nTokens identificados:");
        System.out.println("-".repeat(60));

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            System.out.printf("%2d. %-30s -> '%s'%n", i+1, token.getTipo(), token.getValor());
        }

        System.out.println("-".repeat(60));
    }

    private boolean isCaractereAcentuado(char c) {
        String acentuados = "áàâãéêíóôõúçÁÀÂÃÉÊÍÓÔÕÚÇ";
        return acentuados.indexOf(c) >= 0;
    }

    private void pularEspacos() {
        while (caractereAtual != '\0' && Character.isWhitespace(caractereAtual)) {
            if (caractereAtual == '\n') {
                linha++;
                coluna = 1;
            } else {
                coluna++;
            }
            avancar();
        }
    }

    private void avancar() {
        posicao++;
        coluna++;

        if (posicao < texto.length())
            caractereAtual = texto.charAt(posicao);
        else
            caractereAtual = '\0';
    }

    private Token lerVariavel() {
        StringBuilder variavel = new StringBuilder();
        variavel.append(caractereAtual);
        avancar();

        if (caractereAtual == '\0' || Character.isWhitespace(caractereAtual)) {
            return new Token(TokenType.ERRO, variavel.toString());
        }

        if (caractereAtual == '$') {
            variavel.append(caractereAtual);
            return new Token(TokenType.ERRO, variavel.toString());
        }

        if (!Character.isLetter(caractereAtual) && !isCaractereAcentuado(caractereAtual)) {
            variavel.append(caractereAtual);
            return new Token(TokenType.ERRO, variavel.toString());
        }

        while (caractereAtual != '\0' &&
                (Character.isLetterOrDigit(caractereAtual) ||
                        isCaractereAcentuado(caractereAtual) ||
                        caractereAtual == '_')) {
            variavel.append(caractereAtual);
            avancar();
        }

        return new Token(TokenType.IDENTIFICADOR, variavel.toString());
    }

    private Token lerNumero() {
        StringBuilder numero = new StringBuilder();
        boolean temPonto = false;

        while (caractereAtual != '\0' && (Character.isDigit(caractereAtual) || caractereAtual == '.')) {
            if (caractereAtual == '.') {
                if (temPonto) {
                    numero.append(caractereAtual);
                    avancar();
                    return new Token(TokenType.ERRO, numero.toString());
                }
                temPonto = true;
            }
            numero.append(caractereAtual);
            avancar();
        }

        return new Token(TokenType.LITERAL_NUMERICO, numero.toString());
    }

    private Token lerTexto() {
        StringBuilder texto = new StringBuilder();
        avancar();

        while (caractereAtual != '\0' && caractereAtual != '"') {
            texto.append(caractereAtual);
            avancar();
        }

        if (caractereAtual == '"')
            avancar();

        return new Token(TokenType.LITERAL_TEXTO, texto.toString());
    }

    private Token lerPalavra() {
        StringBuilder palavra = new StringBuilder();

        while (caractereAtual != '\0' &&
                (Character.isLetterOrDigit(caractereAtual) ||
                        isCaractereAcentuado(caractereAtual) ||
                        caractereAtual == '_')) {
            palavra.append(caractereAtual);
            avancar();
        }

        String palavraString = palavra.toString();

        TokenType tipo = PalavrasReservadas.MAP.get(palavraString.toLowerCase());

        if (tipo != null)
            return new Token(tipo, palavraString);

        return new Token(TokenType.IDENTIFICADOR, palavraString);
    }

    private Token lerOperadorOuSimbolo() {
        char simbolo = caractereAtual;

        if (simbolo == '<' && espiar() == '=') {
            avancar();
            avancar();
            return new Token(TokenType.COMPARADOR_MENOR_IGUAL, "<=");
        }
        if (simbolo == '>' && espiar() == '=') {
            avancar();
            avancar();
            return new Token(TokenType.COMPARADOR_MAIOR_IGUAL, ">=");
        }
        if (simbolo == '!' && espiar() == '=') {
            avancar();
            avancar();
            return new Token(TokenType.COMPARADOR_DIFERENTE, "!=");
        }

        String simboloString = String.valueOf(simbolo);
        TokenType tipo = PalavrasReservadas.MAP.get(simboloString);
        avancar();

        if (tipo != null)
            return new Token(tipo, simboloString);

        return new Token(TokenType.DESCARTE, simboloString);
    }

    private char espiar() {
        int proximaPosicao = posicao + 1;

        if (proximaPosicao < texto.length())
            return texto.charAt(proximaPosicao);

        return '\0';
    }

    public int getLinha() {
        return linha;
    }

    public int getColuna() {
        return coluna;
    }
}