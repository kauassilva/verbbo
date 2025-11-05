package br.com.analisador;

import br.com.palavras_reservadas.PalavrasReservadas;
import br.com.token.Token;
import br.com.token.TokenType;
import br.com.utils.TabelaCaracteresValidos;

import java.util.ArrayList;
import java.util.List;

public class AnalisadorLexico {
    private final String texto;
    private int posicao;
    private char caractereAtual;

    public AnalisadorLexico(String texto) {
        this.texto = texto;
        this.posicao = 0;
        this.caractereAtual = !texto.isEmpty() ? texto.charAt(0) : '\0';
    }

    public List<Token> analisar() {
        List<Token> tokens = new ArrayList<>();

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
                token = lerErroLexico();
            }

            // Adiciona o token na lista de tokens
            tokens.add(token);

        }

        return tokens;
    }

    public void exibirTokens(List<Token> tokens) {
        System.out.println("\n=== ANÁLISE LÉXICA ===");
        System.out.println("Script analisado: " + texto);
        System.out.println("Total de tokens encontrados: " + tokens.size());


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

        if (isVariavel && !(Character.isLetter(caractereAtual) || TabelaCaracteresValidos.contem(caractereAtual)) ) {
            return new Token(TokenType.ERRO_LEXICO, "$");
        }

        // Lê letras, números e acentos
        while (caractereAtual != '\0' && (Character.isLetterOrDigit(caractereAtual) || TabelaCaracteresValidos.contem(caractereAtual))) {
            palavra.append(caractereAtual);
            avancar();
        }

        String palavraString = palavra.toString();

        // Se começar com $ é identificador
        if (isVariavel) {
            return new Token(TokenType.DECLARACAO_VARIAVEL, palavraString);
        }

        // Verifica se é uma palavra-chave
        TokenType tipo = PalavrasReservadas.MAP.get(palavraString.toLowerCase());

        if (tipo != null)
            return new Token(tipo, palavraString);

        // Se não for reconhecido é um identificador
        return new Token(TokenType.IDENTIFICADOR, palavraString);
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

        // Se não reconhecer é tratado como erro
        return new Token(TokenType.ERRO_LEXICO, simboloString);
    }

    private Token lerErroLexico() {
        StringBuilder erro = new StringBuilder();

        // Continua consumindo caracteres enquanto eles forem "lixo"
        // Um caractere é "lixo" se ele NÃO for:
        // - Fim de arquivo
        // - Espaço em branco (que seria pulado)
        // - Dígito (que iniciaria lerNumero)
        // - Aspas (que iniciaria lerTexto)
        // - Letra/Acento/$ (que iniciaria lerPalavra)
        // - Operador conhecido (que iniciaria lerOperadorOuSimbolo)
        while (caractereAtual != '\0' &&
                !Character.isWhitespace(caractereAtual) &&
                !Character.isDigit(caractereAtual) &&
                caractereAtual != '"' &&
                !(Character.isLetter(caractereAtual) || "áàâãéêíóôõúç".indexOf(caractereAtual) >= 0 || caractereAtual == '$') &&
                !(";<>:()=!+-*/".indexOf(caractereAtual) >= 0)
        ) {
            erro.append(caractereAtual);
            avancar();
        }

        return new Token(TokenType.ERRO_LEXICO, erro.toString());
    }

    private char espiar() {
        int proximaPosicao = posicao + 1;

        if (proximaPosicao < texto.length())
            return texto.charAt(proximaPosicao);

        return '\0';
    }
}
