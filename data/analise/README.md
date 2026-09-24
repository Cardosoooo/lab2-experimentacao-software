# Resultados da análise

Saída dos scripts de `analise/`. Estes arquivos são o insumo do dashboard e do
relatório final: quem for montar os gráficos lê daqui, e não recalcula nada.

Para regerar tudo, a partir da raiz do repositório:

```
python analise/analise_rq1_rq2.py
python analise/analise_rq3_rq5.py
```

## Tabelas de resultado dos testes

| Arquivo | Conteúdo |
|---------|----------|
| `testes_rq1_rq2.csv` | resultado do Wilcoxon para tempo e taxa de sucesso |
| `testes_rq3_rq5.csv` | resultado do Wilcoxon para complexidade, duplicação, LOC e complexidade por linha |

Colunas dessas duas tabelas:

| Coluna | Significado |
|--------|-------------|
| `variavel` | variável dependente testada |
| `alternativa` | direção da hipótese: `less`, `greater` ou `two-sided` |
| `n_pares` | pares completos por kata |
| `n_pares_uteis` | pares com diferença diferente de zero, que é o que o teste usa |
| `n_empates` | pares com diferença exatamente zero |
| `estatistica` | estatística W do Wilcoxon, vazia quando o teste não se aplica |
| `p_valor` | p-valor, vazio quando o teste não se aplica |
| `p_minimo_possivel` | menor p-valor alcançável com esse número de pares |
| `tamanho_efeito` | correlação rank-biserial, de -1 a +1 |
| `mediana_diferenca` | mediana de COM_IA menos SEM_IA |
| `conclusivo` | `False` quando a significância a 0,05 era inalcançável por construção |
| `observacao` | leitura em texto do que o resultado permite afirmar |

**Atenção ao usar nos gráficos:** `conclusivo` é `False` em todas as linhas. Isso não
significa ausência de efeito, significa que o desenho com quatro pares não permite
atingir 0,05. O gráfico deve mostrar tamanho de efeito e direção, não um carimbo de
significante ou não significante.

## Tabelas por variável

Para cada variável dependente existem dois arquivos.

| Padrão | Conteúdo |
|--------|----------|
| `descritiva_<variavel>.csv` | por tratamento: n, mediana, Q1, Q3, IQR, mínimo e máximo |
| `pares_<variavel>.csv` | uma linha por kata, com o valor COM_IA, o SEM_IA, a diferença e se o par está completo |

Variáveis disponíveis: `tempo`, `taxa_sucesso`, `cc_media_por_metodo`,
`pct_duplicacao_cpd`, `loc_total` e `cc_por_loc`.

O enunciado pede mediana e intervalo interquartil em vez de média e desvio-padrão, e
os gráficos devem seguir a mesma escolha. Com quatro observações por tratamento e
censura no limite superior, a média engana.

## Tabelas de apoio

| Arquivo | Conteúdo |
|---------|----------|
| `metricas_consolidadas.csv` | as métricas de CK e PMD dos oito trials em uma tabela |
| `revisao_outliers.csv` | revisão de outliers do tempo, com a decisão tomada em cada trial |
| `rq4_primeiro_verde.csv` | os quatro trials que têm tempo até o primeiro teste verde |

## Cuidados para o dashboard

1. **Dois trials são censurados.** Eles têm `tempo_segundos` igual a 2100 e
   `censurado` igual a `true` no `data/trials.csv`. Marque isso no gráfico, com hachura
   ou anotação, senão parecem tempos reais.
2. **A RQ2 não tem teste.** Sete dos oito trials terminaram com todos os testes
   passando. Um gráfico de barras da taxa de sucesso vai parecer vazio, e é esse o
   resultado: efeito de teto.
3. **A duplicação é zero nos oito trials.** Não rende gráfico comparativo. Vale uma
   nota de texto em vez de um eixo achatado.
4. **A RQ4 tem só quatro pontos**, todos do Gabriel. Não dá para comparar tratamentos
   pareados, apenas mostrar os quatro valores.
5. **A escala do tempo é muito desigual**, de 57 a 2100 segundos. Escala logarítmica
   ou eixo quebrado ajuda a não esmagar o grupo com IA.

O porquê de cada lacuna está em [`../../docs/desvios-sprint2.md`](../../docs/desvios-sprint2.md).
