# Katas

Quatro katas autorais em Java, usadas como objetos experimentais. Cada kata tem
enunciado próprio, um esqueleto que compila e falha em todos os testes, uma suíte de
testes de aceitação sem JUnit e uma solução de referência.

| Kata | Tema | Autor |
|------|------|-------|
| `kata01` | Fechamento de caixa | Gabriel |
| `kata02` | Cobrança de estacionamento | Gabriel |
| `kata03` | Compra fracionada de ações | Guilherme |
| `kata04` | Conta de energia elétrica | Guilherme |

## Regra de uso durante o experimento

Quem resolve a kata trabalha apenas em `src`. A pasta `referencia` contém a solução
usada para calibrar a dificuldade e **não é aberta durante o trial**. A suíte em
`test` é fixa e idêntica nos dois tratamentos.

## Contrato de saída da suíte de testes

Toda suíte imprime uma linha por teste e uma linha de resumo, no formato abaixo. Esse
formato é o contrato consumido pelo runner de testes e pelo cronômetro, para que a
contagem de aprovados seja lida sem depender de cada kata.

```
TESTE;<nome do teste>;PASSOU
TESTE;<nome do teste>;FALHOU;esperado=<valor>;obtido=<valor>
RESUMO;total=<N>;passando=<M>;falhando=<K>
```

Código de saída: `0` quando todos os testes passam, `1` quando algum falha.

## Como compilar e rodar

Esqueleto, o que o sujeito executa durante o trial:

```
javac -d out katas/kata01/src/*.java katas/kata01/test/*.java
java -cp out TestesKata01
```

Solução de referência, usada só na calibração:

```
javac -d out-ref katas/kata01/referencia/*.java katas/kata01/test/*.java
java -cp out-ref TestesKata01
```

## Calibração de dificuldade

A dificuldade das katas foi verificada por indicadores estruturais da solução de
referência, e não por cronometragem. Cronometrar exigiria que um dos integrantes
resolvesse a kata do zero antes do experimento, o que anteciparia justamente o
trabalho que a Sprint 2 vai medir. A limitação está registrada em
[`docs/ameacas-validade.md`](../docs/ameacas-validade.md).

| Indicador | `kata01` | `kata02` | `kata03` | `kata04` |
|-----------|---------|---------|---------|---------|
| Regras numeradas no enunciado | 9 | 8 | 10 | 7 |
| Testes de aceitação | 12 | 14 | 15 | 16 |
| Linhas de código da referência | 99 | 61 | 88 | 62 |
| Métodos da referência | 4 | 5 | 4 | 4 |
| Pontos de decisão da referência | 23 | 11 | 19 | 14 |

**Critério de contagem de pontos de decisão.** No arquivo da solução de
referência, com comentários desconsiderados: cada `if`, `for`, `while`, `switch`,
`case`, `default` e `catch` conta 1; cada `&&` e `||` conta 1; um ternário conta 2,
porque tem dois ramos; `else` não conta, porque o ramo faz parte do `if` já
contado. Aplicada sobre a `kata01` e a `kata02` este critério reproduz
exatamente os valores 23 e 11 já publicados.

As duas primeiras katas exigem tratamento de entrada inválida, cabem em um único
arquivo e não pedem biblioteca externa nem estrutura de dados elaborada. A `kata01`
é a mais pesada das duas: concentra validação, deduplicação e agregação, e isso
aparece nos pontos de decisão. A `kata02` troca volume de validação por aritmética
de tempo e ordem de aplicação de regras.

As katas do Guilherme seguem o mesmo perfil. A `kata03` é a mais próxima da
`kata01`: validação de linhas, cancelamento por chave e três faixas de corretagem,
com porte e complexidade parecidos (88 LOC, 4 métodos, 19 pontos de decisão). A
`kata04` é a mais próxima da `kata02`: validação de entrada, faixas progressivas de
preço e um desconto condicional, com porte quase idêntico (62 LOC, 4 métodos, 14
pontos de decisão). A `kata04` tem mais testes de aceitação (16) porque cada
bandeira tarifária cobre uma combinação diferente de sobretaxa.

Essa diferença de porte entre as katas não compromete a comparação, porque o desenho
bloqueia por kata: cada uma é resolvida uma vez com IA e uma vez sem IA. A dificuldade
da kata incide igualmente nos dois tratamentos.

Se durante a Sprint 2 alguma kata censurar os dois trials, isso é registrado como
evidência de calibração insuficiente e entra na discussão do relatório final.

## Linha de base medida pelas ferramentas oficiais

A tabela acima é contagem manual sobre o arquivo. Como controle, a solução de
referência das quatro katas passou pelo mesmo pipeline CK e PMD que vai medir os
trials. Isso dá um piso de comparação para o código que sair da Sprint 2. A saída
bruta está em `data/metricas/kataNN-ref/`.

| Métrica (CK e PMD) | `kata01` | `kata02` | `kata03` | `kata04` |
|--------------------|---------|---------|---------|---------|
| Linhas de código | 93 | 54 | 87 | 60 |
| Métodos | 5 | 5 | 5 | 4 |
| Complexidade ciclomática total | 20 | 6 | 17 | 15 |
| Complexidade média por método | 4 | 1.2 | 3.4 | 3.75 |
| Linhas duplicadas | 0 | 0 | 0 | 0 |
| Tamanho mínimo de trecho no CPD | 50 | 50 | 50 | 50 |

Os valores de linhas de código diferem um pouco da contagem manual porque o CK aplica
o próprio critério de linha útil. As duas medidas convivem: a manual descreve o
enunciado, a do CK é a que entra na análise da RQ3 e da RQ5.

Duplicação zero nas quatro é o esperado. Cada referência é um arquivo único e curto,
sem trecho repetido acima do limite de 50 tokens do detector.
