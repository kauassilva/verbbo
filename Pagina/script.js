const sourceInput = document.getElementById('sourceCode');
const outputDiv = document.getElementById('tokenOutput');

const keywordMap = {
    'crie': 'VERBO_CRIAR', 'criar': 'VERBO_CRIAR', 'faça': 'VERBO_CRIAR',
    'exiba': 'VERBO_MOSTRAR', 'mostre': 'VERBO_MOSTRAR', 'imprima': 'VERBO_MOSTRAR',
    'valendo': 'VERBO_ATRIBUIR', 'vale': 'VERBO_ATRIBUIR', 'seja': 'VERBO_ATRIBUIR',
    'receba': 'VERBO_ATRIBUIR', 'como': 'VERBO_ATRIBUIR',
    'somar': 'VERBO_SOMAR', 'some': 'VERBO_SOMAR', 'mais': 'VERBO_SOMAR',
    'subtrair': 'VERBO_SUBTRAIR', 'menos': 'VERBO_SUBTRAIR',

    'numero': 'TIPO_NUMERICO', 'número': 'TIPO_NUMERICO',
    'texto': 'TIPO_TEXTO', 'string': 'TIPO_TEXTO',
    'booleano': 'TIPO_BOOLEANO',

    'e': 'CONECTOR_E',
    'ou': 'CONECTOR_OU',
    'para': 'CONECTOR_PARA',

    'se': 'CONDICIONAL_SE', 'caso': 'CONDICIONAL_SE',
    'senao': 'CONDICIONAL_SENAO', 'entao': 'CONDICIONAL_ENTAO',

    // Comparadores
    'maior': 'COMPARADOR_MAIOR',
    'menor': 'COMPARADOR_MENOR',
    'igual': 'COMPARADOR_IGUAL',
    'diferente': 'COMPARADOR_DIFERENTE'
};

function analisar() {
    const text = sourceInput.value;
    outputDiv.innerHTML = '';

    if(!text.trim()) {
        outputDiv.innerHTML = '<div class="text-gray-500 italic text-center mt-20">Aguardando entrada...</div>';
        return;
    }

    let i = 0;
    let count = 1;

    while (i < text.length) {
        let char = text[i];

        // Ignorar espaços em branco
        if (/\s/.test(char)) {
            i++;
            continue;
        }

        let tokenType = '';
        let tokenValue = '';

        if (/[0-9]/.test(char)) {
            tokenType = 'LITERAL_NUMERICO';
            while (i < text.length && /[0-9.]/.test(text[i])) {
                tokenValue += text[i];
                i++;
            }
        }
        else if (char === '"') {
            tokenType = 'LITERAL_TEXTO';
            tokenValue += '"';
            i++;
            while (i < text.length && text[i] !== '"') {
                tokenValue += text[i];
                i++;
            }
            if(i < text.length) {
                tokenValue += '"';
                i++;
            }
        }
        else if (char === '$') {
            tokenType = 'DECLARACAO_VARIAVEL';
            tokenValue += '$';
            i++;
            while (i < text.length && /[a-zA-Z0-9_]/.test(text[i])) {
                tokenValue += text[i];
                i++;
            }
        }
        else if (/[a-zA-Záàâãéêíóôõúç]/.test(char)) {
            while (i < text.length && /[a-zA-Záàâãéêíóôõúç]/.test(text[i])) {
                tokenValue += text[i];
                i++;
            }

            const lower = tokenValue.toLowerCase();

            if (keywordMap[lower]) {
                tokenType = keywordMap[lower];
            } else if (['o', 'a', 'um', 'uma', 'do', 'da', 'de'].includes(lower)) {
                tokenType = 'CONECTOR_RUIDO';
            } else {
                tokenType = 'IDENTIFICADOR';
            }
        }
        else if ('+-*/=<>!;:(){}'.includes(char)) {
            if ((char === '<' || char === '>' || char === '!') && text[i+1] === '=') {
                tokenValue = char + '=';
                tokenType = getSymbolType(tokenValue);
                i += 2;
            } else {
                tokenValue = char;
                tokenType = getSymbolType(char);
                i++;
            }
        }
        else {
            tokenValue = char;
            tokenType = 'ERRO_LEXICO';
            i++;
        }

        criarElementoToken(count, tokenType, tokenValue);
        count++;
    }
}

function getSymbolType(sym) {
    if(sym === '+') return 'VERBO_SOMAR';
    if(sym === '-') return 'VERBO_SUBTRAIR';
    if(sym === '*') return 'VERBO_MULTIPLICAR';
    if(sym === '/') return 'VERBO_DIVIDIR';
    if(sym === ';') return 'PONTUACAO_PONTO_E_VIRGULA';
    if(sym === '(') return 'PONTUACAO_ABRE_PARENTESES';
    if(sym === ')') return 'PONTUACAO_FECHA_PARENTESES';
    return 'SIMBOLO';
}

function getColorForType(type) {
    if (type.includes('VERBO')) return 'text-purple-400';
    if (type.includes('TIPO')) return 'text-cyan-400';
    if (type.includes('LITERAL')) return 'text-green-400';
    if (type.includes('VARIAVEL')) return 'text-yellow-400';
    if (type.includes('ERRO')) return 'text-red-500';
    if (type.includes('RUIDO')) return 'text-gray-500 opacity-50';
    if (type.includes('CONDICIONAL')) return 'text-pink-400';
    return 'text-gray-300';
}

function criarElementoToken(index, type, value) {
    const colorClass = getColorForType(type);
    const tokenEl = document.createElement('div');

    tokenEl.className = `token-entry flex items-center gap-4 p-2 rounded bg-white/5 border border-white/5 hover:border-white/20 transition-colors cursor-default group`;

    tokenEl.innerHTML = `
        <span class="text-gray-600 w-6 text-right text-xs font-mono select-none">${index}.</span>
        <span class="${colorClass} font-bold text-xs md:text-sm tracking-wide">${type}</span>
        <div class="flex-1 h-[1px] bg-white/5 group-hover:bg-white/10 transition-colors"></div>
        <span class="text-gray-200 bg-black/30 px-2 py-1 rounded font-mono text-sm border border-white/5">'${value}'</span>
    `;

    outputDiv.appendChild(tokenEl);
    outputDiv.scrollTop = outputDiv.scrollHeight;
}

function limpar() {
    sourceInput.value = '';
    analisar();
    sourceInput.focus();
}

sourceInput.addEventListener('input', analisar);
window.addEventListener('load', analisar);