package br.com.utils;

import java.util.Set;

public class TabelaCaracteresValidos {
    private final static Set<Character> CARACTERES_VALIDOS = Set.of(
            'á', 'à', 'â', 'ã',
            'é', 'ê',
            'í',
            'ó', 'ô', 'õ',
            'ú',
            'ç',
            '_'
    );

    public static boolean contem(char letra) {
        return CARACTERES_VALIDOS.contains(letra);
    }

}
