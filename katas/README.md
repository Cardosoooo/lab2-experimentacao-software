# Katas

Quatro katas autorais em Java, usadas como objetos experimentais. Cada kata tem
enunciado próprio, um esqueleto que compila e falha em todos os testes, uma suíte de
testes de aceitação sem JUnit e uma solução de referência.

| Kata | Tema | Autor |
|------|------|-------|
| `kata01` | Fechamento de caixa | Gabriel |
| `kata02` | Cobrança de estacionamento | Gabriel |
| `kata03` | a definir | Guilherme |
| `kata04` | a definir | Guilherme |

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

| Indicador | `kata01` | `kata02` |
|-----------|---------|---------|
| Regras numeradas no enunciado | 9 | 8 |
| Testes de aceitação | 12 | 14 |
| Linhas de código da referência | 99 | 61 |
| Métodos da referência | 4 | 5 |
| Pontos de decisão da referência | 23 | 11 |

As duas katas exigem tratamento de entrada inválida, cabem em um único arquivo e não
pedem biblioteca externa nem estrutura de dados elaborada. A `kata01` é a mais pesada
das duas: concentra validação, deduplicação e agregação, e isso aparece nos pontos de
decisão. A `kata02` troca volume de validação por aritmética de tempo e ordem de
aplicação de regras.

Essa diferença de porte entre as katas não compromete a comparação, porque o desenho
bloqueia por kata: cada uma é resolvida uma vez com IA e uma vez sem IA. A dificuldade
da kata incide igualmente nos dois tratamentos.

Se durante a Sprint 2 alguma kata censurar os dois trials, isso é registrado como
evidência de calibração insuficiente e entra na discussão do relatório final.
