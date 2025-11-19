package br.com.utils;

import br.com.token.TokenType;

import java.util.HashMap;
import java.util.Map;

public class PalavrasReservadas {
    public static final Map<String, TokenType> MAP = new HashMap<>();

    static {
        add(new String[]{"crie", "criar", "faça", "monte"}, TokenType.VERBO_CRIAR);
        add(new String[]{"exibir", "exiba", "mostrar", "mostre", "imprimir", "imprima"}, TokenType.VERBO_MOSTRAR);
        add(new String[]{"atribuir", "atribua", "definir", "defina", "seja", "valer", "vale", "valendo",
                "ter", "tenha", "tera", "obtenha", "receber", "receba", "como"}, TokenType.VERBO_ATRIBUIR);

        add(new String[]{"numero", "número", "numerico", "numérico", "inteiro", "decimal"}, TokenType.TIPO_NUMERICO);
        add(new String[]{"texto", "string", "frase", "palavra", "palávra"}, TokenType.TIPO_TEXTO);
        add(new String[]{"booleano", "lógico", "verdadeiro", "falso", "true", "false"}, TokenType.TIPO_BOOLEANO);

        add(new String[]{"+", "somar", "some", "adicionar", "adicione",
                "juntar", "junte", "acrescentar", "acrescente", "mais", "com"}, TokenType.VERBO_SOMAR);
        add(new String[]{"-","subtração", "subtrair", "subtraia", "tirar", "tire", "retirar", "retire",
                "diminuir", "diminua", "reduza", "reduzir", "menos"}, TokenType.VERBO_SUBTRAIR);
        add(new String[]{"*","multiplicar", "multiplique", "multiplicação", "vezes"}, TokenType.VERBO_MULTIPLICAR);
        add(new String[]{"/","divisão", "dividir", "divida"}, TokenType.VERBO_DIVIDIR);

        add(new String[]{"se", "caso", "contanto"}, TokenType.CONDICIONAL_SE);
        add(new String[]{"senão", "exceto", "mas", "do contrário", "caso contrário"}, TokenType.CONDICIONAL_SENAO);

        add(new String[]{"<", "menor", "menor que"}, TokenType.COMPARADOR_MENOR);
        add(new String[]{">" ,"maior", "maior que"}, TokenType.COMPARADOR_MAIOR);
        add(new String[]{"=" ,"igual", "igual a", "é igual", "for igual"}, TokenType.COMPARADOR_IGUAL);
        add(new String[]{"<=" ,"menor ou igual", "menor igual"}, TokenType.COMPARADOR_MENOR_IGUAL);
        add(new String[]{">=" ,"maior ou igual", "maior igual"}, TokenType.COMPARADOR_MAIOR_IGUAL);
        add(new String[]{"!=" ,"diferente", "diferente de", "não igual"}, TokenType.COMPARADOR_DIFERENTE);

        add(new String[]{"a", "à", "ao", "o", "de", "do", "da", "um"}, TokenType.CONECTOR_RUIDO);
        add(new String[]{"e"}, TokenType.CONECTOR_E);
        add(new String[]{"ou"}, TokenType.CONECTOR_OU);
        add(new String[]{"para"}, TokenType.CONECTOR_PARA);

        add(new String[]{";"}, TokenType.PONTUACAO_PONTO_E_VIRGULA);
        add(new String[]{"."}, TokenType.PONTUACAO_PONTO);
        add(new String[]{":"}, TokenType.PONTUACAO_DOIS_PONTOS);
        add(new String[]{"("}, TokenType.PONTUACAO_ABRE_PARENTESES);
        add(new String[]{")"}, TokenType.PONTUACAO_FECHA_PARENTESES);
        add(new String[]{"{"}, TokenType.PONTUACAO_ABRE_CHAVES);
        add(new String[]{"}"}, TokenType.PONTUACAO_FECHA_CHAVES);
    }

    private static void add(String[] palavras, TokenType tipo) {
        for (String palavra : palavras) {
            MAP.put(palavra.toLowerCase(), tipo);
        }
    }

}
