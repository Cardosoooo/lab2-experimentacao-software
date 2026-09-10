# Kata 04 — Conta de Energia Elétrica

Implemente o cálculo do valor de uma conta de energia a partir do consumo mensal e
da bandeira tarifária vigente no mês.

## Assinatura

```java
public class ContaEnergia {
    public static String calcular(String consumoKwh, String bandeira)
}
```

## Formato de entrada

`consumoKwh` é o consumo do mês em quilowatts-hora, como texto contendo apenas
dígitos (inteiro não negativo). Exemplo: `"350"`.

`bandeira` é uma das quatro bandeiras tarifárias, sempre em letras maiúsculas:
`VERDE`, `AMARELA`, `VERMELHA1` ou `VERMELHA2`.

## Regras

1. Se `consumoKwh` não for um texto de apenas dígitos, o resultado é `ERRO`.
2. Se `bandeira` não for exatamente uma das quatro previstas, o resultado é `ERRO`.
3. O valor da energia é cobrado por faixas progressivas de consumo, de forma
   **marginal**: os primeiros `100` kWh ao preço da primeira faixa, o que passar de
   `100` até `200` kWh ao preço da segunda faixa e o excedente ao preço da terceira.

   | Faixa de consumo (kWh) | Preço por kWh |
   |------------------------|---------------|
   | de `0` a `100`, inclusive | `0.40` |
   | acima de `100` até `200`, inclusive | `0.55` |
   | acima de `200` | `0.75` |

4. A bandeira tarifária acrescenta uma sobretaxa fixa multiplicada pelo consumo
   total, em todas as faixas:

   | Bandeira | Sobretaxa por kWh |
   |----------|-------------------|
   | `VERDE` | `0.00` |
   | `AMARELA` | `0.02` |
   | `VERMELHA1` | `0.04` |
   | `VERMELHA2` | `0.06` |

5. Consumo acima de `300` kWh recebe `5%` de desconto sobre o valor já composto
   (energia mais bandeira), antes do arredondamento final.
6. O valor final é arredondado para duas casas decimais, com empate para cima.
7. O resultado é o texto do valor em duas casas decimais com ponto como separador,
   ou o texto `ERRO`.

## Formato de saída

Texto com o valor em duas casas decimais, por exemplo `132.50`, ou o texto `ERRO`.

## Exemplos

| Consumo | Bandeira | Resultado | Motivo |
|---------|----------|-----------|--------|
| `"50"` | `VERDE` | `20.00` | 50 kWh na primeira faixa |
| `"101"` | `VERDE` | `40.55` | 100 kWh a 0.40 mais 1 kWh a 0.55 |
| `"250"` | `AMARELA` | `137.50` | energia 132.50 mais sobretaxa de 5.00 |
| `"350"` | `VERMELHA2` | `217.08` | energia 207.50, sobretaxa 21.00, desconto de 5% |
| `"-10"` | `VERDE` | `ERRO` | consumo com caractere não numérico |
| `"100"` | `AZUL` | `ERRO` | bandeira desconhecida |

## Como rodar os testes

```
javac -d out katas/kata04/src/*.java katas/kata04/test/*.java
java -cp out TestesKata04
```