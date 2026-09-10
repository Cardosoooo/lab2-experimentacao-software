# Kata 03 — Compra Fracionada de Ações

Implemente a consolidação de ordens de compra de ações, com corretagem por faixa de
valor e cancelamento por lote.

## Assinatura

```java
public class CompraAcoes {
    public static String consolidar(List<String> linhas)
}
```

## Formato de entrada

Cada elemento da lista é uma linha de texto com campos separados por ponto e vírgula.
Existem dois tipos de linha.

**Compra**, com quatro campos:

```
C;<ticket>;<quantidade>;<preco_unitario>
```

- `ticket`: código do ativo, de 1 a 6 caracteres, apenas letras maiúsculas ou dígitos
- `quantidade`: inteiro positivo, múltiplo do lote padrão de 10
- `preco_unitario`: decimal com exatamente duas casas e ponto como separador,
  por exemplo `10.50`, maior que zero

**Cancelamento**, com dois campos:

```
X;<ticket>
```

O cancelamento anula as compras válidas do `ticket` registradas até aquele momento.

## Regras

1. Espaços em branco no início e no fim da linha são ignorados.
2. Uma linha com tipo diferente de `C` ou `X`, ou com o número de campos
   incompatível com o tipo, é inválida.
3. A compra é inválida quando:
   - o `ticket` não tem 1 a 6 caracteres, ou contém algo além de letras maiúsculas e dígitos;
   - a `quantidade` não é um inteiro, é menor ou igual a zero, ou não é múltiplo de 10;
   - o `preco_unitario` não está no formato de duas casas decimais, ou é menor ou igual a zero.
4. Um cancelamento é inválido quando o `ticket` é malformado ou não corresponde a
   nenhuma compra válida registrada antes dele.
5. O cancelamento anula **todas** as compras válidas do `ticket` existentes até o
   momento em que aparece. Compras dos outros tickets não são afetadas.
6. A corretagem é calculada sobre o total de cada compra válida não cancelada
   (`quantidade` vezes `preco_unitario`), por faixa:

   | Total da compra | Corretagem |
   |-----------------|------------|
   | até `1000.00`, inclusive | `5.00` fixa |
   | acima de `1000.00` até `10000.00`, inclusive | `10.00` fixa |
   | acima de `10000.00` | 1% do total da compra |

7. Valores monetários são arredondados para duas casas decimais, empate para cima.
8. Cada compra válida e não cancelada conta uma vez em `ordens` e acrescenta o total
   em `total_investido` e a corretagem em `corretagem`.
9. `canceladas` conta as compras válidas que foram anuladas por um cancelamento.
10. `invalidas` conta as linhas inválidas, incluindo compras inválidas e cancelamentos inválidos.

## Formato de saída

Uma única linha, com valores monetários em duas casas decimais:

```
total_investido=<valor>;corretagem=<valor>;total_gasto=<valor>;ordens=<n>;canceladas=<n>;invalidas=<n>
```

`total_gasto` é `total_investido` mais `corretagem`.

## Exemplos

Lista vazia:

```
total_investido=0.00;corretagem=0.00;total_gasto=0.00;ordens=0;canceladas=0;invalidas=0
```

Entrada `["C;PETR4;100;10.00"]` (total de `1000.00`, corretagem mínima):

```
total_investido=1000.00;corretagem=5.00;total_gasto=1005.00;ordens=1;canceladas=0;invalidas=0
```

Entrada `["C;PETR4;100;10.00", "X;PETR4"]`:

```
total_investido=0.00;corretagem=0.00;total_gasto=0.00;ordens=0;canceladas=1;invalidas=0
```

## Como rodar os testes

```
javac -d out katas/kata03/src/*.java katas/kata03/test/*.java
java -cp out TestesKata03
```