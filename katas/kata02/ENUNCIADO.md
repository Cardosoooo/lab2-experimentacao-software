# Kata 02 — Cobrança de Estacionamento

Implemente o cálculo do valor a pagar na saída de um estacionamento.

## Assinatura

```java
public class Estacionamento {
    public static String cobrar(String entrada, String saida, boolean mensalista)
}
```

## Formato de entrada

`entrada` e `saida` são datas com hora, no formato `yyyy-MM-dd HH:mm`, com hora
sempre em dois dígitos. Exemplo: `2026-03-10 08:05`.

`mensalista` indica se o veículo pertence a um assinante mensal.

## Regras

Avalie na ordem abaixo.

1. Se qualquer uma das datas estiver fora do formato, ou se a saída for anterior à
   entrada, o resultado é `ERRO`.
2. Mensalista não paga: o resultado é `0.00`.
3. Permanência de até 15 minutos, inclusive, é gratuita.
4. A primeira hora, ou fração dela, custa `10.00`.
5. Cada hora adicional, ou fração dela, custa `5.00`.
6. Existe um teto de `40.00` por período de 24 horas iniciado. Uma permanência de 30
   horas ocupa dois períodos e tem teto de `80.00`.
7. Permanência inteiramente noturna recebe 30% de desconto sobre o valor já limitado
   pelo teto. É considerada noturna a permanência que atende às três condições:
   - a entrada ocorre às 22:00 ou depois, ou antes das 06:00;
   - a saída ocorre até as 06:00, inclusive;
   - a permanência não passa de 8 horas.
8. O valor final é arredondado para duas casas decimais, com empate para cima.

## Formato de saída

Texto com o valor em duas casas decimais e ponto como separador, por exemplo
`15.00`, ou o texto `ERRO`.

## Exemplos

| Entrada | Saída | Mensalista | Resultado | Motivo |
|---------|-------|-----------|-----------|--------|
| `2026-03-10 08:00` | `2026-03-10 08:10` | não | `0.00` | dentro da tolerância |
| `2026-03-10 08:00` | `2026-03-10 09:01` | não | `15.00` | primeira hora mais uma fração |
| `2026-03-10 08:00` | `2026-03-10 18:00` | não | `40.00` | teto do primeiro período |
| `2026-03-10 23:00` | `2026-03-11 01:00` | não | `10.50` | duas horas com desconto noturno |
| `2026-03-10 12:00` | `2026-03-10 10:00` | não | `ERRO` | saída anterior à entrada |

## Como rodar os testes

```
javac -d out katas/kata02/src/*.java katas/kata02/test/*.java
java -cp out TestesKata02
```
