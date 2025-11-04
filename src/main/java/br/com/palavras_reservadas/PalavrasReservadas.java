package br.com.palavras_reservadas;

import br.com.token.TokenType;

import java.util.HashMap;
import java.util.Map;

public class PalavrasReservadas {
    public static final Map<String, TokenType> MAP = new HashMap<>();

    static {
        add(new String[]{"crie", "criar", "faca", "monte"}, TokenType.VERBO_CRIAR);
        add(new String[]{"exibir", "exiba", "mostrar", "mostre", "imprimir", "imprima"}, TokenType.VERBO_MOSTRAR);
        add(new String[]{"atribuir", "atribua", "definir", "defina", "seja", "valer", "vale", "valendo",
                "ter", "tenha", "tera", "obtenha", "receber", "receba"}, TokenType.VERBO_ATRIBUIR);

        add(new String[]{"numero", "numerico", "inteiro", "decimal"}, TokenType.TIPO_NUMERICO);
        add(new String[]{"texto", "string", "frase", "palavra"}, TokenType.TIPO_TEXTO);
        add(new String[]{"booleano", "logico", "verdadeiro ou falso"}, TokenType.TIPO_BOOLEANO);

        add(new String[]{"somar", "some", "adicionar", "adicione",
                "juntar", "junte", "acrescentar", "acrescente", "mais"}, TokenType.VERBO_SOMAR);
        add(new String[]{"subtracao", "subtrair", "subtraia", "tirar", "tire", "retirar", "retire",
                "diminuir", "diminua", "reduza", "reduzir", "menos"}, TokenType.VERBO_SUBTRAIR);
        add(new String[]{"multiplicar", "multiplique", "multiplicacao", "vezes"}, TokenType.VERBO_MULTIPLICAR);
        add(new String[]{"divisao", "dividir", "divida"}, TokenType.VERBO_DIVIDIR);

        add(new String[]{"se", "caso", "contanto"}, TokenType.CONDICIONAL_SE);
        add(new String[]{"senao", "exceto", "mas", "do contrario", "caso contrario"}, TokenType.CONDICIONAL_SENAO);

        add(new String[]{"<", "menor", "menor que"}, TokenType.COMPARADOR_MENOR);
        add(new String[]{">" ,"maior", "maior que"}, TokenType.COMPARADOR_MAIOR);
        add(new String[]{"=" ,"igual", "igual a", "e igual", "for igual"}, TokenType.COMPARADOR_IGUAL);
        add(new String[]{"<=" ,"menor ou igual", "menor igual"}, TokenType.COMPARADOR_MENOR_IGUAL);
        add(new String[]{">=" ,"maior ou igual", "maior igual"}, TokenType.COMPARADOR_MAIOR_IGUAL);
        add(new String[]{"!=" ,"diferente", "diferente de", "nao igual"}, TokenType.COMPARADOR_DIFERENTE);

        add(new String[]{"a"}, TokenType.CONECTOR_A);
        add(new String[]{"e"}, TokenType.CONECTOR_E);
        add(new String[]{"ou"}, TokenType.CONECTOR_OU);
        add(new String[]{"como"}, TokenType.CONECTOR_COMO);
        add(new String[]{"com"}, TokenType.CONECTOR_COM);
        add(new String[]{"de", "do", "da", "dos", "das"}, TokenType.CONECTOR_DE);

        add(new String[]{";"}, TokenType.PONTUACAO_PONTO_E_VIRGULA);
        add(new String[]{"."}, TokenType.PONTUACAO_PONTO);
        add(new String[]{":"}, TokenType.PONTUACAO_DOIS_PONTOS);
        add(new String[]{"(", ")"}, TokenType.PONTUACAO_PARENTESES);

        add(new String[]{"o", "os", "um", "uns", "uma", "umas"}, TokenType.DESCARTE);
        add(new String[]{"ao", "aos", "no", "nos", "na", "nas"}, TokenType.DESCARTE);
        add(new String[]{"pelo", "pelos", "pela", "pelas"}, TokenType.DESCARTE);
        add(new String[]{"para", "por"}, TokenType.DESCARTE);
    }

    private static void add(String[] palavras, TokenType tipo) {
        for (String palavra : palavras) {
            MAP.put(palavra.toLowerCase(), tipo);
        }
    }

}