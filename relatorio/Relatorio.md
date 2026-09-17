# Anotações de execução dos trials

Resumo legível dos oito trials da Sprint 2. A fonte de verdade para a análise é
[`data/trials.csv`](../data/trials.csv), junto com as métricas em `data/metricas/`.
Esta página existe para leitura rápida e é derivada daqueles arquivos.

Os campos marcados como não medidos estão explicados em
[`docs/desvios-sprint2.md`](../docs/desvios-sprint2.md).

| Trial | Tempo | Censurado | 1º verde | Testes | Taxa | Prompts |
|-------|-------|-----------|----------|--------|------|---------|
| kata01 com IA, Guilherme | 90 s | não | não medido | 12/12 | 100% | 1 |
| kata01 sem IA, Gabriel | 1894 s | não | 1423 s | 12/12 | 100% | 0 |
| kata02 com IA, Guilherme | 192 s | não | não medido | 14/14 | 100% | 2 |
| kata02 sem IA, Gabriel | 2100 s | **sim** | 1769 s | 13/14 | 92,86% | 0 |
| kata03 com IA, Gabriel | 163 s | não | 150 s | 15/15 | 100% | 1 |
| kata03 sem IA, Guilherme | 2100 s | **sim** | não medido | não medido | não medido | 0 |
| kata04 com IA, Gabriel | 57 s | não | 54 s | 16/16 | 100% | 1 |
| kata04 sem IA, Guilherme | 1205 s | não | não medido | 16/16 | 100% | 0 |

## Métricas estáticas do código final

| Trial | LOC | Métodos | CC total | CC média | Duplicação |
|-------|-----|---------|----------|----------|------------|
| kata01 com IA, Guilherme | 122 | 4 | 25 | 6,25 | 0% |
| kata01 sem IA, Gabriel | 136 | 3 | 33 | 11 | 0% |
| kata02 com IA, Guilherme | 63 | 4 | 11 | 2,75 | 0% |
| kata02 sem IA, Gabriel | 52 | 1 | 19 | 19 | 0% |
| kata03 com IA, Gabriel | 123 | 8 | 31 | 3,875 | 0% |
| kata03 sem IA, Guilherme | 131 | 9 | 33 | 3,667 | 0% |
| kata04 com IA, Gabriel | 66 | 5 | 17 | 3,4 | 0% |
| kata04 sem IA, Guilherme | 50 | 3 | 10 | 3,333 | 0% |

Duplicação zero em todos os trials, com o limite de 50 tokens do detector. Cada
solução é um arquivo único e curto, sem trecho repetido acima desse tamanho.

## Observação sobre o teste que faltou

No trial `gabriel-kata02-SEM_IA`, o único teste não atendido foi
`dois_periodos_de_24h_dobram_o_teto`: a implementação devolveu `75.00` onde o
esperado era `80.00`. O trial foi encerrado no time-box com esse estado, conforme o
protocolo.

A análise estatística destes dados é a entrega da Sprint 3.
