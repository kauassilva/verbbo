package br.com.analisador;

import br.com.utils.PalavrasReservadas;
import br.com.token.Token;
import br.com.token.TokenType;
import br.com.utils.TabelaCaracteresValidos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
            else if (";<>:(){}=!+-*/".indexOf(caractereAtual) >= 0) {
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
            palavra.append(caractereAtual);
            isVariavel = true;
            avancar();
        }

        if (isVariavel && !(Character.isLetter(caractereAtual) || TabelaCaracteresValidos.contem(caractereAtual))) {
            StringBuilder invalidos = new StringBuilder();
            while (caractereAtual != '\0' &&
                    !Character.isWhitespace(caractereAtual) &&
                    !(Character.isLetterOrDigit(caractereAtual) || TabelaCaracteresValidos.contem(caractereAtual)) &&
                    !(";<>:(){}=!+-*/\"".indexOf(caractereAtual) >= 0)
            ) {
                invalidos.append(caractereAtual);
                avancar();
            }
            palavra.append(invalidos);
            return new Token(TokenType.ERRO_LEXICO, palavra.toString());
        }

        // Lê letras, números e acentos
        while (caractereAtual != '\0' && (Character.isLetterOrDigit(caractereAtual) || TabelaCaracteresValidos.contem(caractereAtual))) {
            palavra.append(caractereAtual);
            avancar();
        }

        String palavraString = palavra.toString();

        if (isVariavel) {
            return new Token(TokenType.DECLARACAO_VARIAVEL, palavraString);
        }

        //tenta combinar palavras compostas (ex: "maior ou igual", "para o", etc)
        Token tokenComposto = tentarCombinarPalavrasCompostas(palavraString);
        if (tokenComposto != null) {
            return tokenComposto;
        }

        //se não for composta, busca palavra simples no mapa
        TokenType tipo = PalavrasReservadas.MAP.get(palavraString.toLowerCase());

        return new Token(Objects.requireNonNullElse(tipo, TokenType.IDENTIFICADOR), palavraString);

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

        return new Token(Objects.requireNonNullElse(tipo, TokenType.ERRO_LEXICO), simboloString);

        // Se não reconhecer é tratado como erro
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
                !(";<>:(){}=!+-*/".indexOf(caractereAtual) >= 0)
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

    private Token tentarCombinarPalavrasCompostas(String palavraInicial) {
        String lower = palavraInicial.toLowerCase();
        if (lower.equals("maior") || lower.equals("menor")) {
            Token tokenComposto = tentarCombinarComparador(palavraInicial);
            if (tokenComposto != null) return tokenComposto;
        }

        return tentarCombinarAte2Palavras(palavraInicial);
    }


    private Token tentarCombinarComparador(String palavraInicial) {
        int tempPos = posicao;

        String palavra1 = lerProximaPalavra(tempPos);
        if (palavra1 == null || !palavra1.equalsIgnoreCase("ou")) return null;

        tempPos = proximaPosicaoAposLeitura(tempPos, palavra1);
        String palavra2 = lerProximaPalavra(tempPos);
        if (palavra2 == null) return null;

        tempPos = proximaPosicaoAposLeitura(tempPos, palavra2);
        String combined = palavraInicial + " " + palavra1 + " " + palavra2;
        TokenType tipo = PalavrasReservadas.MAP.get(combined.toLowerCase());

        if (tipo != null) {
            while (posicao < tempPos) avancar();
            return new Token(tipo, combined);
        }

        return null;
    }


    private Token tentarCombinarAte2Palavras(String palavraInicial) {
        StringBuilder combined = new StringBuilder(palavraInicial);
        int tempPos = posicao;

        for (int i = 0; i < 2; i++) {
            String proximaPalavra = lerProximaPalavra(tempPos);
            if (proximaPalavra == null) break;

            tempPos = proximaPosicaoAposLeitura(tempPos, proximaPalavra);
            combined.append(" ").append(proximaPalavra);

            TokenType tipo = PalavrasReservadas.MAP.get(combined.toString().toLowerCase());
            if (tipo != null) {
                while (posicao < tempPos) avancar();
                return new Token(tipo, combined.toString());
            }
        }

        return null;
    }

    private String lerProximaPalavra(int startPos) {
        int tempPos = startPos;

        while (tempPos < texto.length() && Character.isWhitespace(texto.charAt(tempPos))) {
            tempPos++;
        }

        if (tempPos >= texto.length()) return null;

        char tempChar = texto.charAt(tempPos);
        if (!(Character.isLetter(tempChar) || "áàâãéêíóôõúç".indexOf(tempChar) >= 0)) {
            return null;
        }

        StringBuilder palavra = new StringBuilder();
        while (tempPos < texto.length()) {
            tempChar = texto.charAt(tempPos);
            if (Character.isLetterOrDigit(tempChar) || TabelaCaracteresValidos.contem(tempChar)) {
                palavra.append(tempChar);
                tempPos++;
            } else {
                break;
            }
        }

        return palavra.isEmpty() ? null : palavra.toString();
    }

    private int proximaPosicaoAposLeitura(int startPos, String palavra) {
        int tempPos = startPos;

        while (tempPos < texto.length() && Character.isWhitespace(texto.charAt(tempPos))) {
            tempPos++;
        }

        tempPos += palavra.length();

        return tempPos;
    }
}
