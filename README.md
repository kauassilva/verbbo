<h1>Verbbo<img src="docs/img/icon.png" alt="Verbbo icon" width="52" style="vertical-align:middle; margin-left:8px;"></h1>

**Verbbo** é um mini-compilador escrito em Java cujo objetivo é servir como um projeto didático para estudar as fases clássicas de compilação: análise léxica, análise sintática, análise semântica e geração de código.

> Página do projeto: https://kauassilva.github.io/verbbo/
> Slide do projeto: https://www.canva.com/design/DAG6aGqfaD0/nqcAOfEICv9KRJ_SLuzoyg/
# 🏁 QR CODE PARA O SITE
![qrcode.png](docs/img/qrcode.png)

### Resumo rápido
- Linguagem de origem: uma linguagem natural-like (português) com verbos como "crie", "mostre", comparadores em palavras, variáveis prefixadas com `$` para declaração.
- Saída: código Java gerado a partir do script de entrada.
- Estado: projeto em evolução — funcionalidades básicas (lex/parse/semantic/codegen) implementadas, várias melhorias planejadas.
---
### Principais conceitos
- Analisador Léxico (lexer): converte texto em tokens (p.ex. TIPO_NUMERICO, DECLARACAO_VARIAVEL, VERBO_CRIAR, CONECTOR_RUIDO etc.).
- Analisador Sintático (parser): constrói a árvore sintática abstrata (AST) a partir dos tokens.
- Analisador Semântico: verifica tipos, declarações e prepara uma tabela de símbolos.
- Geração de Código: converte a AST validada em código Java, que é compilado e executado.
---
### Regras de linguagem (resumo)
- Declaração de variável numérica: `Crie o numero $x valendo 10` ou `Crie numero $x 10`.
- Print: `mostre x` ou qualquer outra palavra reservada do token VERBO_MOSTRAR (quando o identificador já existe como variável).
- Condicional simples: `se <expressão> então <comando> [senao <comando>]`.
---
### Execução do projeto
- No `Main`, informe o nome da classe e `y/n` caso queira, ou não, ativar o modo debug e visualizar todo o processo (Analise Lexica, Sintatica, Semantica e Gerador de Codigo).
---
### Exemplos de scripts (dentro de `input/*.txt`)
- Declaração e print simples:
```
Crie o numero $a valendo 2 e o numero $b valendo 3 e mostre a e b
```
- Uso de texto:
```
Crie a mensagem $msg valendo "Olá Mundo" e mostre msg
```
- Condicional:
```
Crie o numero $idade valendo 25 se idade maior ou igual 18 mostre idade
```

- Aceita em blocos também:
```
Crie o numero $idade valendo 25
se idade maior ou igual 18 então
    mostre idade
senao
    mostre "Menor de idade"
```

- While:
```
Crie o numero $contador valendo 1
enquanto contador menor ou igual 5 execute {
    mostre contador
    contador somando 1
}
```
```
Notas: As palavras `execute` e `repita` são opcionais após a condição do while.
