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

A solução de referência de cada kata é cronometrada pelo autor. O alvo é entre 15 e
25 minutos, para que nenhuma kata seja resolvida trivialmente nem censure todos os
trials no time-box de 35 minutos.

| Kata | Tempo da referência | Testes |
|------|--------------------|--------|
| `kata01` | a preencher na calibração | 12 |
| `kata02` | a preencher na calibração | 14 |
