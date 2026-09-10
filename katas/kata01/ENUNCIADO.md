# Kata 01 — Fechamento de Caixa

Implemente o fechamento do caixa de uma loja a partir da lista de movimentos do dia.

## Assinatura

```java
public class FechamentoCaixa {
    public static String fechar(List<String> movimentos)
}
```

## Formato de entrada

Cada elemento da lista é uma linha de texto com campos separados por ponto e vírgula.
Existem dois tipos de movimento.

**Venda**, com quatro campos:

```
V;<id>;<valor>;<forma>
```

- `id`: identificador da venda, texto não vazio
- `valor`: decimal com exatamente duas casas e ponto como separador, por exemplo `59.90`
- `forma`: uma entre `DINHEIRO`, `DEBITO`, `CREDITO`, `PIX`

**Estorno**, com dois campos:

```
E;<id>
```

O estorno cancela a venda de mesmo identificador.

## Regras

1. Espaços em branco no início e no fim da linha são ignorados.
2. Uma venda estornada não entra em nenhum total e é contada em `estornadas`.
3. O estorno pode aparecer antes ou depois da venda correspondente na lista.
4. Uma linha é **inválida** quando:
   - o tipo não é `V` nem `E`;
   - o número de campos não corresponde ao tipo;
   - o `id` é vazio;
   - o `valor` não está no formato de duas casas decimais, ou é menor ou igual a zero;
   - a `forma` não é uma das quatro previstas;
   - é uma venda com `id` já usado por outra venda válida anterior;
   - é um estorno de um `id` que não corresponde a nenhuma venda válida;
   - é um estorno repetido para um `id` já estornado.
5. Cada forma de pagamento tem uma taxa sobre o valor da venda:

   | Forma | Taxa |
   |-------|------|
   | `DINHEIRO` | 0% |
   | `PIX` | 0% |
   | `DEBITO` | 2% |
   | `CREDITO` | 3% |

6. A taxa de cada venda é arredondada para duas casas decimais, sempre para cima no
   caso de empate, ou seja, meio para cima.
7. `bruto` é a soma dos valores das vendas não estornadas.
8. `taxas` é a soma das taxas dessas mesmas vendas.
9. `liquido` é `bruto` menos `taxas`.

## Formato de saída

Uma única linha, com os valores monetários em duas casas decimais e ponto como
separador:

```
bruto=<valor>;liquido=<valor>;taxas=<valor>;vendas=<n>;estornadas=<n>;invalidas=<n>
```

`vendas` conta apenas as vendas válidas que não foram estornadas.

## Exemplos

Lista vazia:

```
bruto=0.00;liquido=0.00;taxas=0.00;vendas=0;estornadas=0;invalidas=0
```

Entrada `["V;A1;100.00;CREDITO"]`:

```
bruto=100.00;liquido=97.00;taxas=3.00;vendas=1;estornadas=0;invalidas=0
```

Entrada `["V;A1;100.00;PIX", "E;A1"]`:

```
bruto=0.00;liquido=0.00;taxas=0.00;vendas=0;estornadas=1;invalidas=0
```

## Como rodar os testes

```
javac -d out katas/kata01/src/*.java katas/kata01/test/*.java
java -cp out TestesKata01
```
