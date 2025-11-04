package br.com.analisador;

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

    public AnalisadorLexico(String texto) {
        this.texto = texto;
        this.posicao = 0;
        this.caractereAtual = !texto.isEmpty() ? texto.charAt(0) : '\0';
        this.tokensDescartados = 0;
    }

    public List<Token> analisar() {
        List<Token> tokens = new ArrayList<>();
        tokensDescartados = 0;

        while (caractereAtual != '\0') {
            pularEspacos();

            if (caractereAtual == '\0')
                break;

            Token token;

            // Numero
            if (Character.isDigit(caractereAtual)) {
                token = lerNumero();
            }
            // Texto entre aspas
            else if (caractereAtual == '"') {
                token = lerTexto();
            }
            // Palavras-chaves ou identificadores
            else if (Character.isLetter(caractereAtual) || "áàâãéêíóôõúç".indexOf(caractereAtual) >= 0 || caractereAtual == '$') {
                token = lerPalavra();
            }
            // Operadores e simbolos
            else if (";<>:()=!+-*/".indexOf(caractereAtual) >= 0) {
                token = lerOperadorOuSimbolo();
            } else {
                avancar();
                continue;
            }

            // Adicionar o token se não for do tipo DESCARTE
            if (token.getTipo() != TokenType.DESCARTE)
                tokens.add(token);
            else
                tokensDescartados++;
        }

        return tokens;
    }

    public void exibirTokens(List<Token> tokens) {
        System.out.println("\n=== ANÁLISE LÉXICA ===");
        System.out.println("Total de tokens encontrados: " + tokens.size());

        if (tokensDescartados > 0) {
            System.out.println("Tokens descartados (artigos/preposições): " + tokensDescartados);
        }

        System.out.println("\nTokens identificados:");
        System.out.println("-".repeat(60));

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            System.out.printf("%2d. %-30s -> '%s'%n", i+1, token.getTipo(), token.getValor());
        }

        System.out.println("-".repeat(60));
    }

    private void pularEspacos() {
        while (caractereAtual != '\0' && Character.isWhitespace(caractereAtual))
            avancar();
    }

    private void avancar() {
        posicao++;

        if (posicao < texto.length())
            caractereAtual = texto.charAt(posicao);
        else
            caractereAtual = '\0';
    }

    private Token lerNumero() {
        StringBuilder numero = new StringBuilder();

        while (caractereAtual != '\0' && (Character.isDigit(caractereAtual) || caractereAtual == '.')) {
            numero.append(caractereAtual);
            avancar();
        }

        return new Token(TokenType.LITERAL_NUMERICO, numero.toString());
    }

    private Token lerTexto() {
        StringBuilder texto = new StringBuilder();
        avancar(); // pula a primeira aspas

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

        boolean isVariavel = false;
        if (caractereAtual == '$') {
            isVariavel = true;
            avancar();
        }

        // Lê letras, números e acentos
        while (caractereAtual != '\0' && (Character.isLetterOrDigit(caractereAtual) ||
                caractereAtual == 'á' || caractereAtual == 'à' || caractereAtual == 'â' || caractereAtual == 'ã' ||
                caractereAtual == 'é' || caractereAtual == 'ê' ||
                caractereAtual == 'í' ||
                caractereAtual == 'ó' || caractereAtual == 'ô' || caractereAtual == 'õ' ||
                caractereAtual == 'ú' ||
                caractereAtual == 'ç' ||
                caractereAtual == '_') || caractereAtual == '$') {
            palavra.append(caractereAtual);
            avancar();
        }

        String palavraString = palavra.toString();

        // Se começar com $ é identificador
        if (isVariavel) {
            return new Token(TokenType.IDENTIFICADOR, palavraString);
        }

        // Verifica se é uma palavra-chave
        TokenType tipo = PalavrasReservadas.MAP.get(palavraString.toLowerCase());

        if (tipo != null)
            return new Token(tipo, palavraString);

        // Se não for reconhecido é descarte
        return new Token(TokenType.DESCARTE, palavraString);
    }

    private Token lerOperadorOuSimbolo() {
        char simbolo = caractereAtual;

        // Verificar simbolos compostos
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

        // Simbolos simples
        String simboloString = String.valueOf(simbolo);
        TokenType tipo = PalavrasReservadas.MAP.get(simboloString);
        avancar();

        if (tipo != null)
            return new Token(tipo, simboloString);

        // Se não reconhecer, descarta
        return new Token(TokenType.DESCARTE, simboloString);
    }

    private char espiar() {
        int proximaPosicao = posicao + 1;

        if (proximaPosicao < texto.length())
            return texto.charAt(proximaPosicao);

        return '\0';
    }
}
