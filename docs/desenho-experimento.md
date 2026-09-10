# Desenho do Experimento — Laboratório 02

Documento correspondente ao Passo 1 do enunciado. As ameaças à validade e as
questões de pesquisa próprias do grupo estão em
[`ameacas-validade.md`](ameacas-validade.md).

---

## 1. Objetivo (GQM)

Analisar o **uso de assistentes de IA generativa** na resolução de tarefas de
programação, com o propósito de **comparar** seu efeito frente à codificação manual,
com respeito a **tempo de resolução, qualidade funcional (defeitos) e qualidade
estrutural do código produzido**, do ponto de vista do **grupo pesquisado**, no
contexto de **katas de dificuldade equivalente resolvidos por estudantes de
graduação sob condições controladas**, em desenho crossover within-subject com
time-box.

## 2. Questões de pesquisa

| ID | Pergunta | Origem |
|----|----------|--------|
| RQ1 | O uso de assistente de IA reduz o tempo necessário para resolver uma tarefa de programação? | enunciado |
| RQ2 | O uso de assistente de IA reduz a quantidade de defeitos no código produzido? | enunciado |
| RQ3 | O uso de assistente de IA altera a complexidade ciclomática ou a duplicação do código produzido? | enunciado |
| RQ4 | O assistente antecipa o primeiro teste verde ou apenas o último? | grupo |
| RQ5 | A diferença de complexidade se mantém após normalizar por linhas de código? | grupo |

## 3. Hipóteses

Nível de significância adotado: alfa igual a 0,05.

| RQ | Hipótese nula (H0) | Hipótese alternativa (H1) | Direção |
|----|--------------------|---------------------------|---------|
| RQ1 | A mediana do tempo até todos os testes passarem é igual nos dois tratamentos | A mediana do tempo é menor no tratamento com IA | unilateral |
| RQ2 | A mediana da taxa de sucesso dos testes é igual nos dois tratamentos | A mediana da taxa de sucesso é maior no tratamento com IA | unilateral |
| RQ3a | A mediana da complexidade ciclomática por método é igual nos dois tratamentos | As medianas diferem | bilateral |
| RQ3b | A mediana do percentual de linhas duplicadas é igual nos dois tratamentos | As medianas diferem | bilateral |
| RQ4 | A mediana do tempo até o primeiro teste passar é igual nos dois tratamentos | A mediana é menor no tratamento com IA | unilateral |
| RQ5 | A mediana da complexidade ciclomática por linha de código é igual nos dois tratamentos | As medianas diferem | bilateral |

RQ1, RQ2 e RQ4 são unilaterais porque a pergunta do enunciado é direcional: pergunta
se a IA **reduz** tempo e defeitos. RQ3 e RQ5 são bilaterais porque a pergunta é se a
IA **altera** a estrutura, sem direção esperada.

## 4. Variáveis

### 4.1 Variável independente

| Variável | Tipo | Níveis |
|----------|------|--------|
| Uso de assistente de IA | categórica, nominal | `COM_IA`, `SEM_IA` |

### 4.2 Variáveis dependentes

| Variável | Unidade | RQ | Instrumento |
|----------|---------|----|-------------|
| Tempo até todos os testes de aceitação passarem | segundos, censurado em 2100 | RQ1 | `Cronometro.java` |
| Tempo até o primeiro teste de aceitação passar | segundos | RQ4 | `Cronometro.java` |
| Testes passando ao final do time-box | contagem | RQ2 | `ExecutorTestes.java` |
| Taxa de sucesso dos testes | percentual | RQ2 | `ExecutorTestes.java` |
| Complexidade ciclomática média por método | número | RQ3a | CK |
| Percentual de linhas duplicadas | percentual | RQ3b | PMD CPD |
| Linhas de código | contagem | controle, RQ5 | CK |
| Complexidade ciclomática por linha de código | razão | RQ5 | derivada |

### 4.3 Variáveis controladas (mantidas fixas)

| Variável | Valor fixado |
|----------|--------------|
| Linguagem | Java |
| JDK | Temurin 25, o mesmo em todos os trials |
| Assistente de IA | Claude, versão gratuita, em todos os trials do tratamento com IA |
| Time-box | 35 minutos, ou 2100 segundos, por trial |
| Suíte de testes | fixa por kata, idêntica nos dois tratamentos |
| Ferramentas de métrica | CK e PMD, mesma versão para todos os trials |
| Acesso à internet no tratamento sem IA | permitido para documentação, proibido para qualquer assistente generativo |

### 4.4 Variáveis bloqueadas

| Variável | Como é tratada |
|----------|----------------|
| Kata | bloco experimental: cada kata aparece uma vez em cada tratamento |
| Sujeito | bloco experimental: cada sujeito passa pelos dois tratamentos |
| Posição na sequência | contrabalanceada: cada posição aparece uma vez em cada tratamento |

### 4.5 Variável exploratória

Número de prompts ou interações com o assistente no tratamento com IA. Não entra em
teste de hipótese, é usada apenas na discussão qualitativa, conforme o enunciado
permite.

## 5. Tratamentos

| Tratamento | Descrição |
|------------|-----------|
| `COM_IA` | O sujeito resolve a kata com o assistente disponível durante todo o trial, sem restrição de uso |
| `SEM_IA` | O sujeito resolve a kata sem qualquer assistente generativo, usando apenas IDE, documentação oficial da linguagem e conhecimento próprio |

O autocompletar padrão da IDE, sem componente generativo, é permitido nos dois
tratamentos, para que a diferença medida seja o assistente de IA e não a ausência de
ferramentas básicas de edição.

## 6. Objetos experimentais

Quatro katas autorais em Java, identificadas como `kata01` a `kata04`. Cada kata tem
enunciado escrito, esqueleto que compila e falha em todos os testes, e uma suíte de
testes de aceitação determinística e automatizada, sem JUnit.

Critério de dificuldade equivalente: a solução de referência de cada kata é
cronometrada pelo autor e precisa ficar entre 15 e 25 minutos. Katas fora dessa faixa
são recalibradas antes da execução, para que nenhuma kata censure todos os trials nem
seja resolvida trivialmente por qualquer tratamento.

As katas são autorais porque o enunciado alerta que exercícios clássicos e muito
indexados podem ser reproduzidos de memória pelo assistente, o que descaracterizaria
o efeito medido.

## 7. Tipo de projeto experimental

Crossover **within-subject** com dois tratamentos, **contrabalanceado** e replicado
em dois sujeitos, com bloqueio por kata.

| Sujeito | Trial 1 | Trial 2 | Trial 3 | Trial 4 |
|---------|---------|---------|---------|---------|
| Gabriel | kata01 `COM_IA` | kata02 `SEM_IA` | kata03 `COM_IA` | kata04 `SEM_IA` |
| Guilherme | kata01 `SEM_IA` | kata02 `COM_IA` | kata03 `SEM_IA` | kata04 `COM_IA` |

Propriedades do arranjo:

- Cada kata é resolvida uma vez com IA e uma vez sem IA, então a dificuldade da kata
  não fica confundida com o tratamento.
- Cada sujeito passa duas vezes por cada tratamento, então habilidade individual não
  fica confundida com o tratamento.
- Cada posição da sequência aparece uma vez em cada tratamento, então o efeito de
  aprendizado e de fadiga ao longo da sessão fica distribuído entre os dois braços.
- Cada autor de kata resolve uma kata própria com IA e outra sem IA, então a vantagem
  de conhecer o próprio enunciado incide igualmente sobre os dois tratamentos.

## 8. Quantidade de medições

| Item | Quantidade |
|------|-----------|
| Sujeitos | 2 |
| Katas | 4 |
| Trials por sujeito | 4 |
| Trials totais | 8 |
| Trials por tratamento | 4 |
| Pares para o teste de Wilcoxon | 4 |
| Variáveis dependentes por trial | 8 |

## 9. Métricas escolhidas e justificativa

O enunciado apresenta métricas candidatas por questão e exige que a escolha seja
justificada. As escolhas do grupo estão abaixo.

### RQ1 — Tempo

**Escolhida:** tempo até passar em todos os testes de aceitação, o time-to-green.

Trial que atinge o time-box sem sucesso é registrado como **censurado em 2100
segundos** e permanece na análise. Descartar esses trials distorceria a comparação a
favor do tratamento com mais falhas, exatamente como o enunciado alerta.

**Agregação:** mediana e intervalo interquartil, não média e desvio-padrão. Com 4
trials por tratamento e censura no limite superior, a média é dominada por outliers.

**Descartada:** número de prompts como métrica primária. É informativa, mas mede
comportamento de uso e não tempo de resolução. Fica como variável exploratória.

### RQ2 — Defeitos

**Escolhida:** taxa de sucesso, o percentual de testes de aceitação passando ao final
do time-box.

O percentual normaliza katas com números diferentes de testes, o que a contagem bruta
não faz. Uma kata com 12 testes pesaria mais que uma com 5 se usássemos contagem
absoluta.

**Complementar:** número absoluto de testes falhando, reportado nas tabelas
descritivas por ser mais direto de ler.

**Descartada:** densidade de defeitos por KLOC. Faz sentido quando as katas têm
tamanhos muito diferentes, o que o critério de calibração da seção 6 já evita.

### RQ3 — Estrutura do código

**Escolhidas:**

| Métrica | Ferramenta | Papel |
|---------|-----------|-------|
| Complexidade ciclomática média por método | CK, campo `wmc` normalizado pelo número de métodos | RQ3a |
| Percentual de linhas duplicadas | PMD CPD | RQ3b |
| Linhas de código | CK, campo `loc` | controle obrigatório |

LOC entra como controle porque o enunciado exige: código gerado por IA tende a ser
mais verboso, e complexidade ou duplicação sem normalizar por tamanho pode indicar
apenas que o arquivo é maior, não mais complexo. Essa observação é justamente o que
motiva a RQ5.

**Descartada como métrica primária:** Índice de Manutenibilidade. É uma métrica
composta e a implementação de referência é do Radon, ferramenta de Python. Como as
katas são em Java, usar CK e PMD mantém a coerência com a ferramenta indicada pelo
enunciado para essa linguagem.

## 10. Instrumentação

| Instrumento | Issue | Papel |
|-------------|-------|-------|
| `ferramentas/Cronometro.java` | #5 | marca início e fim do trial, aplica a censura e grava em `data/trials.csv` |
| `ferramentas/ExecutorTestes.java` | Guilherme | roda a suíte de aceitação e conta testes passando e falhando |
| `scripts/coleta_metricas.ps1` | Guilherme | executa CK e PMD sobre o código final e normaliza a saída |
| `analise/` | Sprint 3 | Wilcoxon, estatística descritiva e dashboard |

Todo registro de tempo grava o campo `collected_at` no momento da coleta. A análise
usa esse campo e nunca a data em que o script de análise roda, para que o resultado
não mude conforme o dia da execução.

## 11. Procedimento de análise

1. **Descritiva:** mediana e intervalo interquartil por tratamento, para cada
   variável dependente. Média e desvio-padrão não são reportados como estatística
   principal, seguindo a orientação de robustez do enunciado.
2. **Inferência:** teste de Wilcoxon para amostras pareadas, coerente com o desenho
   within-subject.
3. **Pareamento primário:** por kata. Cada kata fornece exatamente uma observação com
   IA e uma sem IA, o que neutraliza a dificuldade da kata, que é a variável de
   perturbação dominante. Resultam 4 pares.
4. **Pareamento secundário, como análise de sensibilidade:** dentro do sujeito,
   comparando os dois trials com IA contra os dois sem IA do mesmo sujeito. Também
   resultam 4 pares. Se os dois pareamentos apontarem na mesma direção, a conclusão
   é mais confiável.
5. **Tamanho de efeito:** reportado junto de cada teste, porque com amostra pequena a
   magnitude da diferença informa mais que o p-valor isolado.

**Limitação de poder, declarada desde o desenho.** Com 4 pares, o menor p-valor
alcançável no teste de Wilcoxon é 0,0625 em teste unilateral e 0,125 em teste
bilateral. Nenhum resultado deste experimento poderá ser declarado significativo a
alfa igual a 0,05, por construção do desenho e não por característica dos dados. A
consequência prática é que a leitura dos resultados se apoia em tamanho de efeito e
direção consistente, e não em rejeição formal de hipótese nula. Essa limitação está
detalhada em [`ameacas-validade.md`](ameacas-validade.md).
