# Relatório Final — Assistentes de IA vs. Codificação Manual

**Laboratório 02 · Engenharia de Software · PUC Minas**
Turma Noite — 6º período · Professor Danilo Maia
**Integrantes:** Gabriel Cardoso ([@Cardosoooo](https://github.com/Cardosoooo)) e Guilherme Brina Ferreira ([@Gmbferreira](https://github.com/Gmbferreira))

**Repositório:** `Cardosoooo/lab2-experimentacao-software`

Este documento consolida o experimento planejado na Sprint 1 (`docs/desenho-experimento.md`,
`docs/ameacas-validade.md`, `docs/protocolo-execucao.md`), executado na Sprint 2 (`data/trials.csv`,
`relatorio/Relatorio.md`, `docs/desvios-sprint2.md`) e analisado na Sprint 3 (`analise/`, `data/analise/`).
Os valores numéricos foram verificados por re-execução dos scripts de análise e conferidos com as tabelas
de `data/analise/`.

---

## 1. Resumo executivo

O experimento comparou o uso de um assistente de IA generativa (Claude, versão gratuita, por conversa)
contra a codificação manual na resolução de quatro katas autorais em Java, em desenho crossover
within-subject com time-box de 35 minutos. Foram executados 8 trials, 4 por tratamento.

**Achados principais:**

- O tratamento `COM_IA` foi consistentemente mais rápido: mediana de **126,5 s** contra **1997 s** sem IA,
  com o tempo com IA menor que o sem IA **nas quatro katas** (diferenças de −1148 s a −1937 s).
- Na **qualidade funcional**, houve efeito de teto: todos os trials com IA terminaram com 100% dos testes
  passando (4/4); sem IA, dois trials não foram concluídos no time-box (censurados em 2100 s), um deles
  com um teste falhando. A taxa de sucesso não produziu comparação estatística útil.
- Na **estrutura do código**, a complexidade ciclomática média por método foi menor com IA
  (mediana 3,64 contra 7,33 sem IA), mas a diferença está concentrada em duas katas; o número de linhas e
  a complexidade por linha são praticamente iguais entre os tratamentos. A duplicação foi nula nos oito
  trials.
- **Nenhum teste produziu p-valor concluível ao nível de 0,05** — o desenho com 4 pares limita o menor
  p-valor alcançável a 0,0625 (unilateral) e 0,125 (bilateral). A leitura dos resultados se apoia em
  tamanho de efeito e consistência de direção, não em rejeição formal de hipótese nula.

### 1.1 As katas individuais e o resultado em cada tratamento

As quatro katas são autorais — duas de cada integrante — calibradas para porte e complexidade próximos
(indicadores estruturais em `katas/README.md`, Apêndice B). Tempo censurado (2100 s) significa que o trial
não foi concluído dentro do time-box; nesses casos o valor é um piso do tempo real.

| Kata | Tema | Autor | Regras | Testes | Ref. (LOC/Mét./Decisões) | COM_IA | SEM_IA |
|------|------|-------|--------|--------|---------------------------|--------|--------|
| kata01 | Fechamento de caixa | Gabriel | 9 | 12 | 99 / 4 / 23 | 90 s · 12/12 | 1894 s · 12/12 |
| kata02 | Cobrança de estacionamento | Gabriel | 8 | 14 | 61 / 5 / 11 | 192 s · 14/14 | censurado (2100 s) · 13/14 |
| kata03 | Compra fracionada de ações | Guilherme | 10 | 15 | 88 / 4 / 19 | 163 s · 15/15 | censurado (2100 s) · não medido |
| kata04 | Conta de energia elétrica | Guilherme | 7 | 16 | 62 / 4 / 14 | 57 s · 16/16 | 1205 s · 16/16 |

- **kata01 — Fechamento de caixa.** A kata com maior carga de validação: combina conferência de entradas
  inválidas, deduplicação e agregação (23 pontos de decisão, o maior das quatro). Resolvida em **90 s** com
  IA (12/12) contra ~32 min sem IA (12/12) — o par de maior razão de tempo do experimento.
- **kata02 — Cobrança de estacionamento.** Troca volume de validação por aritmética de tempo e aplicação
  ordenada de regras de cobrança (11 pontos de decisão, o menor). Com IA, **192 s** e 14/14; sem IA,
  censurou no time-box com **13/14** — o único defeito de todo o experimento (75,00 em vez de 80,00).
- **kata03 — Compra fracionada de ações.** Validação de linhas, cancelamento por chave e três faixas de
  corretagem (19 pontos de decisão, a mais complexa ao lado da kata01). Com IA, **163 s** e 15/15 (1º verde
  aos 150 s); sem IA, ultrapassou o time-box com contagem de testes não registrada.
- **kata04 — Conta de energia elétrica.** Faixas progressivas de preço, desconto condicional e bandeiras
  tarifárias (16 testes de aceitação, a suíte maior). Com IA foi o trial mais rápido do experimento:
  **57 s**, 16/16 (1º verde aos 54 s); sem IA, 16/16 em **1205 s**.

**Conclusão geral:** nos limites deste desenho e amostra, os dados indicam que a IA reduziu o tempo de
resolução com efeito grande e consistente, sem prejuízo funcional e sem alterar a complexidade por linha;
a complexidade média por método aparentou menor com IA, mas com direção inconsistente entre katas.
O resultado vale apenas para katas pequenas, dois sujeitos, Java e Claude na versão gratuita.

## 2. Motivação e contexto

O trabalho mede quantitativamente o efeito do uso de um assistente de IA generativa na resolução de
tarefas de programação. A pergunta é relevante porque a adoção dessas ferramentas cresceu rapidamente e
grande parte da evidência disponível é anedótica. Um experimento controlado permite isolar o efeito da
ferramenta de fatores como dificuldade da tarefa e habilidade do participante.

O contexto experimental é restrito e deliberado: katas autorais pequenas e bem especificadas, resolvidas
por estudantes de graduação, com time-box fixo. Essa escolha limita a generalização (ver ameaças à
validade externa), mas permite uma medição precisa e replicável das variáveis de interesse.

## 3. Questões de pesquisa e hipóteses

### 3.1 Questões

| ID | Pergunta | Origem |
|----|----------|--------|
| RQ1 | O uso de assistente de IA reduz o tempo necessário para resolver uma tarefa de programação? 
| RQ2 | O uso de assistente de IA reduz a quantidade de defeitos no código produzido? 
| RQ3 | O uso de assistente de IA altera a complexidade ciclomática ou a duplicação do código produzido? 
| RQ4 | O assistente antecipa o primeiro teste verde ou apenas o último? | 
| RQ5 | A diferença de complexidade se mantém após normalizar por linhas de código? | 

### 3.2 Hipóteses e direção

Nível de significância adotado: α = 0,05.

| RQ | Hipótese nula (H₀) | Hipótese alternativa (H₁) | Direção |
|----|--------------------|---------------------------|---------|
| RQ1 | A mediana do tempo até todos os testes passarem é igual nos dois tratamentos | A mediana do tempo é **menor** com IA | unilateral |
| RQ2 | A mediana da taxa de sucesso dos testes é igual nos dois tratamentos | A mediana é **maior** com IA | unilateral |
| RQ3a | A mediana da complexidade ciclomática por método é igual | As medianas **diferem** | bilateral |
| RQ3b | A mediana do percentual de linhas duplicadas é igual | As medianas **diferem** | bilateral |
| RQ4 | A mediana do tempo até o primeiro teste verde é igual | A mediana é **menor** com IA | unilateral |
| RQ5 | A mediana da complexidade por linha de código é igual | As medianas **diferem** | bilateral |

RQ1, RQ2 e RQ4 são unilaterais porque a pergunta é direcional (a IA **reduz** tempo e defeitos); RQ3 e
RQ5 são bilaterais porque a pergunta é se a IA **altera** a estrutura, sem direção esperada.

## 4. Desenho experimental

### 4.1 Objetivo (GQM)

Analisar o **uso de assistentes de IA generativa** na resolução de tarefas de programação, com o propósito
de **comparar** seu efeito frente à codificação manual, com respeito a **tempo de resolução, qualidade
funcional (defeitos) e qualidade estrutural do código produzido**, do ponto de vista do **grupo
pesquisado**, no contexto de **katas de dificuldade equivalente resolvidas por estudantes de graduação
sob condições controladas**, em desenho crossover within-subject com time-box.

### 4.2 Variáveis

**Variável independente:**

| Variável | Tipo | Níveis |
|----------|------|--------|
| Uso de assistente de IA | categórica, nominal | `COM_IA`, `SEM_IA` |

**Variáveis dependentes:**

| Variável | Unidade | RQ | Instrumento |
|----------|---------|----|-------------|
| Tempo até todos os testes passarem | segundos, censurado em 2100 | RQ1 | `Cronometro.java` |
| Tempo até o primeiro teste verde | segundos | RQ4 | `Cronometro.java` |
| Testes passando ao final do time-box | contagem | RQ2 | `ExecutorTestes.java` |
| Taxa de sucesso dos testes | percentual | RQ2 | `ExecutorTestes.java` |
| Complexidade ciclomática média por método | número | RQ3a | CK |
| Percentual de linhas duplicadas | percentual | RQ3b | PMD CPD |
| Linhas de código | contagem (controle e RQ5) | RQ3b/RQ5 | CK |
| Complexidade ciclomática por linha | razão | RQ5 | derivada do CK |

**Variáveis controladas (fixas):** linguagem Java; JDK Temurin 25; IDE VS Code 1.110.0 com Extension Pack
for Java; extensões de IA da IDE desligadas nos dois tratamentos; assistente fixado (Claude gratuito, por
conversa, somente `COM_IA`); time-box de 2100 s; suíte de testes fixa por kata; CK 0.7.0 e PMD 7.27.0;
internet liberada no `SEM_IA` apenas para documentação oficial.

**Variáveis bloqueadas:** kata (cada kata aparece uma vez em cada tratamento), sujeito (cada sujeito passa
pelos dois tratamentos), posição na sequência (contrabalanceada).

**Variável exploratória:** número de prompts enviados ao assistente no `COM_IA` (não entra em teste de
hipótese; usada na discussão qualitativa).

### 4.3 Objetos experimentais

Quatro katas autorais em Java, cada uma com enunciado, esqueleto que compila e falha em todos os testes e
suíte de aceitação determinística sem JUnit. As katas são autorais para que o assistente não reproduza
solução memorizada de exercícios indexados. A dificuldade foi verificada por indicadores estruturais da
solução de referência (não por cronometragem, que contaminaria o sujeito) — detalhamento no Apêndice B.

### 4.4 Tipo de projeto experimental

Crossover **within-subject** com dois tratamentos, contrabalanceado e replicado em dois sujeitos, com
bloqueio por kata. A atribuição efetivamente executada na Sprint 2:

| Sujeito | kata01 | kata02 | kata03 | kata04 |
|---------|--------|--------|--------|--------|
| Guilherme | `COM_IA` | `COM_IA` | `SEM_IA` | `SEM_IA` |
| Gabriel | `SEM_IA` | `SEM_IA` | `COM_IA` | `COM_IA` |

O desenho original previa alternância de tratamento a cada trial dentro do sujeito; a execução ficou em
blocos, com os dois trials de um tratamento seguidos (Guilherme: dois `COM_IA` depois dois `SEM_IA`;
Gabriel: blocos invertidos). O desvio está detalhado em `docs/desvios-sprint2.md` e resumido na seção 6.2.

Propriedades mantidas: cada kata é resolvida uma vez em cada tratamento (a dificuldade da kata não fica
confundida com o tratamento); cada sujeito passa duas vezes por cada tratamento (a habilidade individual
não fica confundida); os blocos invertidos compensam entre os sujeitos o efeito de aprendizado no
pareamento por kata. Cada autor resolveu uma kata própria com IA e outra sem, de modo que a vantagem de
conhecer o próprio enunciado incide igualmente sobre os dois braços.

### 4.5 Quantidade de medições

| Item | Quantidade |
|------|-----------|
| Sujeitos | 2 |
| Katas | 4 |
| Trials totais | 8 (4 por tratamento) |
| Pares para o Wilcoxon | 4 (por kata) |
| Variáveis dependentes por trial | 8 |

### 4.6 Limitação de poder estatístico (declarada no desenho)

Com 4 pares, o menor p-valor alcançável no Wilcoxon é **0,0625** em teste unilateral e **0,125** em
bilateral. Portanto nenhum resultado deste experimento pode ser declarado significativo a α = 0,05 — por
construção do desenho, não por característica dos dados. A leitura dos resultados se apoia em tamanho de
efeito e consistência de direção. Essa limitação está registrada em `docs/ameacas-validade.md`.
## 5. Instrumentação

### 5.1 Ambiente de execução (versões pinadas)

| Componente | Versão fixada |
|------------|---------------|
| JDK | Temurin 25 (mínimo exigido: 17+) |
| IDE | Visual Studio Code 1.110.0 |
| Suporte a Java na IDE | Extension Pack for Java |
| Extensões de IA na IDE | desligadas nos dois tratamentos |
| CK | 0.7.0 |
| PMD (CPD) | 7.27.0 |
| Assistente de IA | Claude, versão gratuita, por conversa (somente `COM_IA`) |
| Time-box | 35 minutos = 2100 segundos |
| Linguagem | Java |

Qualquer mudança nesses componentes invalidaria a comparabilidade dos trials.

### 5.2 Métricas e instrumentos por RQ

| Métrica | Escolha e justificativa | Instrumento |
|---------|-------------------------|-------------|
| Tempo até todos os testes passarem (time-to-green) | Mede resolução funcional dentro do time-box; trial não concluído é censurado em 2100 s e permanece na análise | `Cronometro.java` |
| Taxa de sucesso dos testes | Percentual normaliza katas com números diferentes de testes; a contagem absoluta falhando é reportada complementarmente | `ExecutorTestes.java` |
| Complexidade ciclomática média por método | Campo `wmc` do CK normalizado pelo número de métodos | CK |
| Percentual de linhas duplicadas | Campo do PMD CPD com tamanho mínimo de trecho fixado em 50 tokens | PMD CPD |
| Linhas de código | Controle obrigatório e insumo da RQ5 (código de IA tende a ser mais verboso) | CK |
| Complexidade por linha de código | Razão `cc_total / loc_total`, separa "mais complexo" de "apenas maior" | derivada do CK |
| Tempo até o 1º teste verde | Registrado no instante da primeira suíte com ao menos um teste passando | `Cronometro.java` (`verde`) |

Foram descartadas, com justificativa em `docs/desenho-experimento.md`: número de prompts como métrica
primária (fica como variável exploratória), densidade de defeitos por KLOC (katas de porte equivalente) e
Índice de Manutenibilidade (implementação de referência é do Radon, de Python, e as katas são em Java).

## 6. Execução e coleta

### 6.1 Trials executados

Os campos com valor ausente (—) correspondem a medições perdidas, explicadas em `docs/desvios-sprint2.md`.
O tempo `2100 s` com `censurado = sim` significa trial não concluído no time-box.

| Trial | Sujeito | Kata | Tratamento | Tempo | Censurado | 1º verde | Testes | Taxa | Prompts | Commit |
|-------|---------|------|------------|-------|-----------|----------|--------|------|---------|--------|
| `guilherme-kata01-COM_IA` | Guilherme | 01 | COM_IA | 90 s | não | — | 12/12 | 100% | 1 | `a9649f5` |
| `guilherme-kata02-COM_IA` | Guilherme | 02 | COM_IA | 192 s | não | — | 14/14 | 100% | 2 | `1129ccb` |
| `guilherme-kata03-SEM_IA` | Guilherme | 03 | SEM_IA | 2100 s | **sim** | — | — | — | 0 | `e85fda6` |
| `guilherme-kata04-SEM_IA` | Guilherme | 04 | SEM_IA | 1205 s | não | — | 16/16 | 100% | 0 | `76860aa` |
| `gabriel-kata01-SEM_IA` | Gabriel | 01 | SEM_IA | 1894 s | não | 1423 s | 12/12 | 100% | 0 | `9f5f6f6` |
| `gabriel-kata02-SEM_IA` | Gabriel | 02 | SEM_IA | 2100 s | **sim** | 1769 s | 13/14 | 92,86% | 0 | `ece7523` |
| `gabriel-kata03-COM_IA` | Gabriel | 03 | COM_IA | 163 s | não | 150 s | 15/15 | 100% | 1 | `11c2fec` |
| `gabriel-kata04-COM_IA` | Gabriel | 04 | COM_IA | 57 s | não | 54 s | 16/16 | 100% | 1 | `0c1fbb3` |

### 6.2 Desvios de protocolo observados

| # | Desvio | Consequência nos dados |
|---|--------|------------------------|
| 1 | Os 4 trials do Guilherme foram executados sem o `Cronometro` (tempo anotado à mão; código commitado por cima do esqueleto em `katas/kataNN/src`) | `inicio_iso`/`fim_iso` e **1º verde perdidos** nos 4 trials → RQ4 fica sem par e passa a ser descritiva; tempo e contagens recuperados da anotação; código final recuperado do histórico e arquivado em `data/trials/<id>/src` |
| 2 | `guilherme-kata03-SEM_IA` ultrapassou o time-box com anotação "Tempo: 35+" | Registrado como censurado em 2100 s; `testes_passando`/`taxa_sucesso` ficam **em branco** (não se sabe o estado aos 35 min) → RQ2 fica com 3 observações sem IA |
| 3 | Esqueletos sobrescritos pelas soluções e depois restaurados do commit `8c96e3e` | Soluções da kata01/02 ficaram visíveis no repo entre commits → ameaça de difusão de tratamento registrada, dependente de auto-relato |
| 4 | Dois defeitos no pipeline de métricas (leitura do `method.csv` com assinaturas entre aspas; `Add-Content` duplicando a linha a cada recoleta) | Detectados por valores impossíveis; corrigidos e **todas as métricas recoletadas** |
| 5 | Ordem dos tratamentos em blocos, não alternada | Balanceamento de posição dentro do sujeito perdido; compensado entre sujeitos no pareamento por kata |

Cada desvio está detalhado em `docs/desvios-sprint2.md`, com a recuperação feita.

### 6.3 Observações qualitativas de execução

- **Teste que faltou no único trial com defeito:** em `gabriel-kata02-SEM_IA`, o único teste não atendido
  foi `dois_periodos_de_24h_dobram_o_teto`: a implementação devolveu `75.00` onde o esperado era `80.00`.
  O trial foi encerrado no time-box nesse estado, conforme o protocolo.
- **Prompts:** nos trials com IA foram usados 1, 2, 1 e 1 prompts (média 1,25). A contagem baixa sugere
  que a resolução com IA foi dirigida por poucas interações.
- **Conferência de plausibilidade:** os dois defeitos do pipeline foram encontrados porque saíram números
  impossíveis (complexidade zero num código que passa em 13/14 testes; trial duplicado na tabela). Isso
  evitou que a análise rodasse sobre valores corrompidos e é uma lição registrada para a discussão.

## 7. Análise estatística

### 7.1 Método

- **Descritiva:** mediana e intervalo interquartil por tratamento, conforme prescrito no enunciado. Com 4
  observações por tratamento e censura no limite superior, a média é dominada por extremos.
- **Inferência:** teste de Wilcoxon para amostras pareadas, coerente com o desenho within-subject.
- **Pareamento primário:** por kata, cada kata dá exatamente uma observação com e uma sem IA,
  neutralizando a dificuldade da kata (variável de perturbação dominante). Resultam 4 pares.
- **Tamanho de efeito:** correlação rank-biserial, reportada junto de cada teste, porque com amostra
  pequena a magnitude da diferença informa mais que o p-valor isolado.
- **Revisão de outliers:** regra do IQR dentro de cada tratamento, mantendo os trials censurados (a censura
  é resultado previsto do protocolo, não anomalia).

### 7.2 Estatística descritiva por tratamento

**Tempo até todos os testes passarem, segundos, RQ1**

| Tratamento | n | Mediana | Q1 | Q3 | IQR | Mínimo | Máximo |
|------------|---|---------|----|----|-----|--------|--------|
| COM_IA | 4 | **126,50** | 81,75 | 170,25 | 88,50 | 57,0 | 192,0 |
| SEM_IA | 4 | **1997,00** | 1721,75 | 2100,00 | 378,25 | 1205,0 | 2100,0 |

**Taxa de sucesso, % - RQ2**

| Tratamento | n | Mediana | Q1 | Q3 | IQR | Mínimo | Máximo |
|------------|---|---------|----|----|-----|--------|--------|
| COM_IA | 4 | 100,00 | 100,00 | 100,00 | 0,00 | 100,0 | 100,0 |
| SEM_IA | 3 | 100,00 | 96,43 | 100,00 | 3,57 | 92,86 | 100,0 |

**Complexidade ciclomática média por método - RQ3a**

| Tratamento | n | Mediana | Q1 | Q3 | IQR | Mínimo | Máximo |
|------------|---|---------|----|----|-----|--------|--------|
| COM_IA | 4 | **3,638** | 3,238 | 4,469 | 1,231 | 2,750 | 6,250 |
| SEM_IA | 4 | **7,333** | 3,583 | 13,000 | 9,416 | 3,333 | 19,000 |

**Percentual de linhas duplicadas, % - RQ3b**

| Tratamento | n | Mediana | Q1 | Q3 | IQR | Mínimo | Máximo |
|------------|---|---------|----|----|-----|--------|--------|
| COM_IA | 4 | 0,00 | 0,00 | 0,00 | 0,00 | 0,0 | 0,0 |
| SEM_IA | 4 | 0,00 | 0,00 | 0,00 | 0,00 | 0,0 | 0,0 |

**Linhas de código (controle)**

| Tratamento | n | Mediana | Q1 | Q3 | IQR | Mínimo | Máximo |
|------------|---|---------|----|----|-----|--------|--------|
| COM_IA | 4 | 94,00 | 65,25 | 122,25 | 57,00 | 63,0 | 123,0 |
| SEM_IA | 4 | 91,50 | 51,50 | 132,25 | 80,75 | 50,0 | 136,0 |

**Complexidade ciclomática por linha - RQ5**

| Tratamento | n | Mediana | Q1 | Q3 | IQR | Mínimo | Máximo |
|------------|---|---------|----|----|-----|--------|--------|
| COM_IA | 4 | 0,228 | 0,197 | 0,253 | 0,056 | 0,175 | 0,258 |
| SEM_IA | 4 | 0,247 | 0,232 | 0,280 | 0,048 | 0,200 | 0,365 |
### 7.3 Pares por kata (COM_IA − SEM_IA)

**Tempo (s)** - o valor com IA foi menor que o sem IA nas quatro katas:

| Kata | COM_IA | SEM_IA | Diferença |
|------|--------|--------|-----------|
| kata01 | 90 | 1894 | −1804 |
| kata02 | 192 | 2100 | −1908 |
| kata03 | 163 | 2100 | −1937 |
| kata04 | 57 | 1205 | −1148 |

**Taxa de sucesso (%)** - par incompleto na kata03 (sem IA não medido):

| Kata | COM_IA | SEM_IA | Diferença |
|------|--------|--------|-----------|
| kata01 | 100,00 | 100,00 | 0,00 |
| kata02 | 100,00 | 92,86 | +7,14 |
| kata03 | 100,00 | - | incompleto |
| kata04 | 100,00 | 100,00 | 0,00 |

**Complexidade ciclomática média por método:**

| Kata | COM_IA | SEM_IA | Diferença |
|------|--------|--------|-----------|
| kata01 | 6,250 | 11,000 | −4,750 |
| kata02 | 2,750 | 19,000 | −16,250 |
| kata03 | 3,875 | 3,667 | +0,208 |
| kata04 | 3,400 | 3,333 | +0,067 |

**Percentual de linhas duplicadas (%):**

| Kata | COM_IA | SEM_IA | Diferença |
|------|--------|--------|-----------|
| kata01 | 0 | 0 | 0 |
| kata02 | 0 | 0 | 0 |
| kata03 | 0 | 0 | 0 |
| kata04 | 0 | 0 | 0 |

**Linhas de código:**

| Kata | COM_IA | SEM_IA | Diferença |
|------|--------|--------|-----------|
| kata01 | 122 | 136 | −14 |
| kata02 | 63 | 52 | +11 |
| kata03 | 123 | 131 | −8 |
| kata04 | 66 | 50 | +16 |

**Complexidade ciclomática por linha:**

| Kata | COM_IA | SEM_IA | Diferença |
|------|--------|--------|-----------|
| kata01 | 0,205 | 0,243 | −0,038 |
| kata02 | 0,175 | 0,365 | −0,191 |
| kata03 | 0,252 | 0,252 | ≈ 0,000 |
| kata04 | 0,258 | 0,200 | +0,058 |

### 7.4 Revisão de outliers (tempo)

Nenhum trial ficou fora do intervalo 1,5 × IQR. Os dois trials censurados foram mantidos porque a censura
é o limite do protocolo, não um valor anomalo a descartar.

| Trial | Tratamento | Tempo | Censurado | Decisão |
|-------|------------|-------|-----------|---------|
| `guilherme-kata01-COM_IA` | COM_IA | 90 | não | normal |
| `guilherme-kata02-COM_IA` | COM_IA | 192 | não | normal |
| `gabriel-kata03-COM_IA` | COM_IA | 163 | não | normal |
| `gabriel-kata04-COM_IA` | COM_IA | 57 | não | normal |
| `guilherme-kata03-SEM_IA` | SEM_IA | 2100 | sim | mantido: censura e limite do protocolo, não anomalia |
| `guilherme-kata04-SEM_IA` | SEM_IA | 1205 | não | normal |
| `gabriel-kata01-SEM_IA` | SEM_IA | 1894 | não | normal |
| `gabriel-kata02-SEM_IA` | SEM_IA | 2100 | sim | mantido: censura e limite do protocolo, não anomalia |

### 7.5 Resultados dos testes de Wilcoxon

**RQ1 e RQ2 (`data/analise/testes_rq1_rq2.csv`):**

| Variável | Direção | Pares | Úteis | Empates | W | p | p mínimo possível | Efeito | Δ mediana | Conclusivo |
|----------|---------|-------|-------|---------|---|---|--------------------|--------|-----------|------------|
| `tempo_segundos` | menor com IA | 4 | 4 | 0 | 0,0 | 0,0625 | 0,0625 | −1,000 (grande) | −1856,0 | não |
| `taxa_sucesso` | maior com IA | 3 | 1 | 2 | - | - | 0,5 | +1,000 (grande) | 0,0 | não |

**RQ3 e RQ5 (`data/analise/testes_rq3_rq5.csv`):**

| Variável | Direção | Pares | Úteis | Empates | W | p | p mínimo possível | Efeito | Δ mediana | Conclusivo |
|----------|---------|-------|-------|---------|---|---|--------------------|--------|-----------|------------|
| `cc_media_por_metodo` | bilateral | 4 | 4 | 0 | 3,0 | 0,625 | 0,125 | −0,400 (moderado) | −2,342 | não |
| `pct_duplicacao_cpd` | bilateral | 4 | 0 | 4 | - | - | - | - | 0,0 | não |
| `loc_total` | bilateral | 4 | 4 | 0 | 4,0 | 0,875 | 0,125 | +0,200 (pequeno) | +1,5 | não |
| `cc_por_loc` | bilateral | 4 | 4 | 0 | 4,0 | 0,875 | 0,125 | −0,200 (pequeno) | −0,019 | não |

`Conclusivo = não` quer dizer que a significância a 0,05 era inalcançável por construção do desenho (o
p mínimo com 4 pares é maior que 0,05), e não que o efeito esteja ausente. `W` é a estatística do teste;
o tamanho de efeito é a correlação rank-biserial, de −1 a +1.

### 7.6 RQ4 — tempo até o primeiro teste verde (descritiva)

A medida só existe nos quatro trials do Gabriel (perdida nos trials do Guilherme). Como nenhuma kata tem os
dois tratamentos medidos, não há par e o teste de Wilcoxon não se aplica. Segue descritiva:

| Trial | Tratamento | 1º verde | Tempo total | Proporção do total |
|-------|------------|----------|-------------|--------------------|
| `gabriel-kata03-COM_IA` | COM_IA | 150 s | 163 s | 92% |
| `gabriel-kata04-COM_IA` | COM_IA | 54 s | 57 s | 95% |
| `gabriel-kata01-SEM_IA` | SEM_IA | 1423 s | 1894 s | 75% |
| `gabriel-kata02-SEM_IA` | SEM_IA | 1769 s | 2100 s | 84% |
## 8. Resultados por questão de pesquisa

### 8.1 RQ1 — A IA reduz o tempo necessário para resolver a tarefa?

**Descritiva.** Mediana com IA 126,50 s (IQR 88,5; faixa 57–192) contra 1997,00 s sem IA (IQR 378,25; faixa
1205–2100). Os quatro pares vão todos na mesma direção: IA mais rápida nas quatro katas, diferenças de
−1148 s a −1937 s (mediana da diferença −1856 s).

**Teste.** Wilcoxon pareado por kata, unilateral (`less`), 4 pares úteis: W = 0,0; p = 0,0625 (igual ao p
mínimo possível); tamanho de efeito −1,000 (grande).

**Leitura.** Não é possível rejeitar formalmente H₀ a 0,05 - o p mínimo com 4 pares é 0,0625 unilateral. O
efeito, porém, é **grande e perfeitamente consistente**: todos os pares na mesma direção. Em termos
práticos, com IA a kata foi resolvida em 1 a 3 minutos; sem IA, em 20 a 35 minutos. A razão entre as
medianas é de ~16×.

**Conclusão (indicativa):** forte evidência de redução de tempo com IA, coerente com H₁, sem significância
formal pelo limite de poder do desenho.

### 8.2 RQ2 — A IA reduz a quantidade de defeitos?

**Descritiva.** Taxa de sucesso com IA: 100% nos 4 trials. Sem IA: mediana 100%, faixa 92,86–100 (n = 3; o
4º trial teve a contagem em branco por censura - desvio 2 da seção 6.2).

**Teste.** Wilcoxon pareado, unilateral (`greater`), 3 pares (kata03 sem o lado sem IA), mas com **apenas 1
par não empatado** (kata02: 100% vs 92,86%; kata01 e kata04 empataram em 100%). O teste exige ao menos 2
diferenças diferentes de zero, portanto **não se aplica**.

**Leitura.** Ocorreu **efeito de teto**: sete dos oito trials terminaram com 100% dos testes passando, então
a taxa de sucesso quase não varia. O único defeito de todo o experimento (13/14 na kata02) ocorreu no
braço sem IA, mas uma observação não sustenta comparação estatística. Efeito reportado +1,000, com n
efetivo de 1 par — não interpretável.

**Conclusão (indicativa):** não há diferença mensurável em defeitos; a RQ2 é o resultado menos informativo,
como já era esperado para katas curtas com suítes bem especificadas.
### 8.3 RQ3 — A IA altera a complexidade ciclomática ou a duplicação?

**RQ3a — complexidade média por método (descritiva).** Mediana 3,638 com IA (faixa 2,75–6,25) contra 7,333
sem IA (faixa 3,333–19). Nas katas 01 e 02 o valor com IA foi bem menor (−4,75 e −16,25); nas katas 03 e 04
as diferenças são mínimas (+0,21 e +0,07).

**RQ3a — teste.** Wilcoxon bilateral, 4 pares úteis: W = 3,0; p = 0,625 (p mínimo 0,125); efeito −0,400
(moderado); mediana da diferença −2,342.

**RQ3a — leitura.** Não significativo a 0,05 e a **direção não é uniforme** entre katas (2 a 2). O efeito
agregado é moderado e aponta para complexidade média menor com IA, mas a dispersão sem IA (até 19 em um só
método) domina a comparação. A leitura segura é que a IA não aumentou a complexidade média por método.

**RQ3b — duplicação.** Percentual de linhas duplicadas = 0 nos 8 trials (CPD fixado em 50 tokens; soluções
de arquivo único). Todas as diferenças são zero; **não há o que testar**. A duplicação deixou de ser uma
variável discriminativa neste desenho.

**Conclusão (indicativa):** a IA não aumentou a complexidade; há tendência de redução na média por método,
sem diferença conclusiva e com direção mista. Duplicação nula nos dois tratamentos.

### 8.4 RQ4 — O assistente antecipa o primeiro teste verde ou só o último?

**Descritiva.** A medida existe apenas nos 4 trials do Gabriel (perdida nos 4 do Guilherme — desvio 1 da
seção 6.2). Com IA, o primeiro verde chegou a 92–95% do tempo total (54 s de 57 s; 150 s de 163 s); sem IA,
a 75–84% (1423 s de 1894 s; 1769 s de 2100 s).

**Teste.** Não aplicável: nenhuma kata tem os dois tratamentos medidos, logo não há pares.

**Leitura (descritiva).** Nos 4 pontos disponíveis, o primeiro verde chega **proporcionalmente mais tarde**
com IA (razão 0,92–0,95) do que sem IA (0,75–0,84). Isso é **contrário à hipótese de que a IA vence a página
em branco** e sugere ganho concentrado na etapa final. Com 4 pontos, todos de um mesmo sujeito e sem
pareamento, é **sugestão qualitativa**, não evidência: a RQ4 entra como **resultado não obtido** para
inferência e **achado descritivo** na discussão.

### 8.5 RQ5 — A diferença de complexidade se mantém após normalizar por LOC?

**Descritiva.** Complexidade por linha: mediana 0,228 com IA (faixa 0,175–0,258) contra 0,247 sem IA (faixa
0,200–0,365). LOC: 94,0 contra 91,5 linhas (medianas praticamente iguais).

**Teste.** Wilcoxon bilateral, 4 pares úteis: W = 4,0; p = 0,875 (p mínimo 0,125); efeito −0,200 (pequeno);
mediana da diferença −0,019.

**Leitura.** Ao normalizar por tamanho, a diferença bruta de complexidade média por método **quase
desaparece** (efeito pequeno, p = 0,875), e o volume de código é comparável (LOC: W = 4,0, p = 0,875, efeito
+0,2). **Não se sustenta a ideia de que o código com IA ficou maior ou mais complexo por linha.** A diferença
bruta da RQ3a, quando existe, parece ser mais de distribuição da lógica entre métodos do que um efeito
estrutural da IA.

**Conclusão (indicativa):** após normalização, não há diferença relevante de complexidade por linha entre os
tratamentos; a hipótese de verbosidade inflando a complexidade não se confirma nesta amostra.
## 9. Discussão

- **Tempo.** É o resultado mais forte: 4 de 4 pares na mesma direção, efeito rank-biserial −1,0 e razão de
  medianas de ~16×. Três dos quatro trials com IA terminaram com tudo verde em menos de 3,5 minutos; sem IA,
  dois trials nem concluíram em 35 minutos. Mesmo sem significância formal (p = 0,0625 contra α = 0,05), a
  consistência da direção e a magnitude tornam a evidência prática clara.
- **Defeitos.** Efeito de teto: 7/8 trials com 100%. As suítes são curtas (12–16 testes) e bem
  especificadas; dificilmente qualquer tratamento produziria muitos defeitos. A RQ2 não é conclusiva e isso
  é um resultado sobre o instrumento (métrica pouco variável), não sobre a ferramenta.
- **Estrutura.** A IA não tornou o código mais verboso (LOC equivalente) nem mais complexo por linha (RQ5
  ≈ zero). A complexidade média por método foi menor com IA na mediana, mas com direção mista 2 a 2 — a
  leitura é de ausência de piora estrutural, não de melhora estatisticamente demonstrada.
- **RQ4 (achado descritivo).** A proporção 1º-verde/total foi mais alta com IA, ou seja, o primeiro verde
  apareceu **mais tarde** dentro do trial. Isso contraria a expectativa de que a IA vence a página em
  branco; um mecanismo possível é que, com IA, o sujeito delega a solução completa e só executa a suíte
  tardiamente, enquanto sem IA o primeiro verde vem por exploração incremental. Testar isso exige medir o
  1º verde em todos os trials (experimento futuro).
- **Prompts.** Contagem muito baixa (1, 2, 1, 1). A maior parte do tempo com IA pareceu ser de leitura,
  edição e conferência da resposta, não de conversa longa, consistente com o tempo total curto.
- **Censura.** Os dois trials censurados estão no braço sem IA. Mantê-los (como o protocolo exige) preserva
  a comparação: descartá-los favoreceria artificialmente o sem IA. Na RQ1 eles operam como piso, não como
  valor exato (o tempo verdadeiro era ≥ 2100 s).
- **Qualidade do pipeline.** Os dois defeitos das métricas (leitura do CSV do CK e gravação duplicada) foram
  pegos por plausibilidade: complexidade zero num código que passa 13/14 testes é impossível. A conferência
  de sanidade dos dados impediu que a análise rodasse sobre valores corrompidos.
- **Comparabilidade com a calibração.** Os trials produziram código de porte similar ao da referência (50–136
  LOC contra 60–93 da referência), Apêndice B. Nenhuma kata censurou os dois lados, o que teria indicado
  calibração insuficiente.

## 10. Ameaças à validade

### 10.1 Interna

| Ameaça | Mitigação / situação |
|--------|----------------------|
| Efeito de aprendizado e fadiga | Contrabalanceamento e limite de 2 trials por sessão previstos; **parcialmente perdidos** porque a ordem ficou em blocos (desvio 5), compensados entre sujeitos no pareamento por kata |
| Autoria da kata pelo sujeito | Cada autor resolveu uma própria com IA e outra sem |
| Difusão de tratamento / vazamento de solução | Soluções da kata01/02 ficaram visíveis entre commits; o cumprimento depende de auto-relato → **limitação declarada** |
| Instrumentação | Mesmo cronômetro, runner, JDK e versões de CK/PMD; mas **4 trials do Guilherme sem o cronômetro** (desvio 1) → 1º verde perdido |
| Memorização de exercícios conhecidos | Katas autorais, não publicadas |

### 10.2 Externa

Dois sujeitos, mesma turma e nível; katas pequenas com time-box de 35 min; um único assistente (Claude
gratuito) usado por conversa; linguagem única (Java); familiaridade prévia com o assistente pode diferir
entre sujeitos. Os resultados descrevem este grupo e este setup.

### 10.3 Construto

Tempo até o verde não é qualidade de manutenção; a taxa de sucesso depende da suíte escrita pelo grupo; CC
é proxy de clareza; a duplicação depende do tamanho mínimo do CPD (50 tokens, fixado); a calibração das
katas foi por indicadores estruturais e não por cronometragem.

### 10.4 Conclusão

| Ameaça | Tratamento |
|--------|------------|
| Poder estatístico | p mínimo 0,0625 (unilateral) e 0,125 (bilateral) com 4 pares — **nenhum resultado é significativo a 0,05 por construção**; leitura por efeito e direção |
| Número de sujeitos | 2, sem análise por perfil |
| Comparações múltiplas | 6 hipóteses sobre os mesmos 8 trials, sem correção — estudo exploratório, p-valores indicativos |
| Empates por censura | 2 trials censurados no SEM_IA reduzem ainda mais o poder (RQ2) |
| Dependência entre observações | O mesmo sujeito contribui com 4 trials; o pareamento absorve parcialmente |
## 11. Reprodutibilidade

Todo o material do experimento está versionado em `Cardosoooo/lab2-experimentacao-software`.

| Artefato | Caminho |
|----------|---------|
| Registro bruto dos trials | `data/trials.csv` |
| Código final de cada trial | `data/trials/<trial_id>/src/` |
| Métricas CK/PMD brutas | `data/metricas/<trial_id>/` (+ `kataNN-ref/` de referência) |
| Tabelas da análise | `data/analise/` (descritivas, pares, testes, outliers, RQ4) |
| Scripts de análise | `analise/dados.py`, `analise/estatistica.py`, `analise/analise_rq1_rq2.py`, `analise/analise_rq3_rq5.py` |
| Anotações de execução | `relatorio/Relatorio.md` |
| Desenho, protocolo e desvios | `docs/desenho-experimento.md`, `docs/protocolo-execucao.md`, `docs/desvios-sprint2.md`, `docs/ameacas-validade.md` |

Reproduzir a análise, a partir da raiz do repositório:

```
pip install -r scripts/requirements.txt
python analise/analise_rq1_rq2.py
python analise/analise_rq3_rq5.py
```

Os scripts reescrevem as tabelas de `data/analise/` e usam o campo `collected_at` gravado na coleta, nunca a
data de execução. Ambiente de verificação deste relatório: Python 3.14, pandas 3.0, scipy 1.18.

## 12. Conclusões

Respostas às questões de pesquisa, na ordem:

| RQ | Resposta aos dados |
|----|--------------------|
| RQ1 — a IA reduz o tempo? | **Sim, indicativamente.** Mediana 126,5 s vs 1997 s; 4/4 pares na direção esperada (Δ −1148 a −1937 s); efeito −1,0; p = 0,0625, inalcançável a 0,05 pelo desenho |
| RQ2 — a IA reduz defeitos? | **Não mensurável (efeito de teto).** 7/8 trials com 100%; teste inviável (1 par não empatado); único defeito ocorreu no `SEM_IA` |
| RQ3a — altera a complexidade média por método? | **Tendência a menor, sem conclusão.** Mediana 3,64 vs 7,33; efeito moderado (−0,4); direção mista 2 a 2; p = 0,625 |
| RQ3b — altera a duplicação? | **Não.** Duplicação nula nos 8 trials; nada a testar |
| RQ4 — antecipa o 1º teste verde? | **Resultado não obtido** (medida perdida em 4 trials); descritivamente, o 1º verde chegou proporcionalmente mais tarde com IA (92–95% vs 75–84% do tempo) |
| RQ5 — a complexidade se mantém após normalizar por LOC? | **Sim, a diferença desaparece.** CC/LOC 0,228 vs 0,247; efeito pequeno (−0,2); p = 0,875; LOC equivalentes (94 vs 91,5) |

**Síntese.** No contexto deste experimento, katas autorais pequenas em Java, dois estudantes, Claude
gratuito por conversa, desenho com 4 pares, a IA reduziu substancialmente o tempo de resolução com efeito
grande e consistente, sem piorar a qualidade funcional e sem aumentar a complexidade estrutural do código.
Nenhum resultado é estatisticamente significativo ao nível 0,05, porque o desenho não tem poder para isso
(p mínimo 0,0625 unilateral / 0,125 bilateral); as conclusões se apoiam em tamanho de efeito, direção e
magnitude. Um experimento com mais sujeitos, ordem alternada e medição completa do primeiro verde seria o
próximo passo para confirmar as tendências observadas, especialmente as referentes a defeitos e ao momento
do primeiro teste verde.
## 13. Apêndices

### A. Anotações de execução originais

Reproduz o conteúdo de `relatorio/Relatorio.md`, mantido à parte por ser a fonte das anotações da Sprint 2.

| Trial | Tempo | Censurado | 1º verde | Testes | Taxa | Prompts |
|-------|-------|-----------|----------|--------|------|---------|
| kata01 com IA, Guilherme | 90 s | não | não medido | 12/12 | 100% | 1 |
| kata01 sem IA, Gabriel | 1894 s | não | 1423 s | 12/12 | 100% | 0 |
| kata02 com IA, Guilherme | 192 s | não | não medido | 14/14 | 100% | 2 |
| kata02 sem IA, Gabriel | 2100 s | sim | 1769 s | 13/14 | 92,86% | 0 |
| kata03 com IA, Gabriel | 163 s | não | 150 s | 15/15 | 100% | 1 |
| kata03 sem IA, Guilherme | 2100 s | sim | não medido | não medido | não medido | 0 |
| kata04 com IA, Gabriel | 57 s | não | 54 s | 16/16 | 100% | 1 |
| kata04 sem IA, Guilherme | 1205 s | não | não medido | 16/16 | 100% | 0 |

Métricas estáticas do código final (idênticas à consolidada da seção 6.1/7.3):

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

Observação registrada: no trial `gabriel-kata02-SEM_IA`, o único teste não atendido foi
`dois_periodos_de_24h_dobram_o_teto` (esperado 80,00, obtido 75,00); o trial foi encerrado no time-box.

### B. Calibração das katas (`katas/README.md`)

Dificuldade verificada por indicadores estruturais da solução de referência (não por cronometragem):

| Indicador | kata01 | kata02 | kata03 | kata04 |
|-----------|--------|--------|--------|--------|
| Regras numeradas no enunciado | 9 | 8 | 10 | 7 |
| Testes de aceitação | 12 | 14 | 15 | 16 |
| Linhas de código da referência (manual) | 99 | 61 | 88 | 62 |
| Métodos da referência | 4 | 5 | 4 | 4 |
| Pontos de decisão da referência | 23 | 11 | 19 | 14 |

Linha de base medida pelas ferramentas oficiais (CK e PMD), piso de comparação:

| Métrica | kata01 | kata02 | kata03 | kata04 |
|---------|--------|--------|--------|--------|
| Linhas de código (CK) | 93 | 54 | 87 | 60 |
| Métodos | 5 | 5 | 5 | 4 |
| Complexidade ciclomática total | 21 | 16 | 23 | 16 |
| Complexidade média por método | 4,2 | 3,2 | 4,6 | 4 |
| Linhas duplicadas | 0 | 0 | 0 | 0 |
| Tamanho mínimo de trecho no CPD | 50 | 50 | 50 | 50 |

Os valores de LOC diferem entre a contagem manual e o CK porque o CK aplica o próprio critério de linha
útil; o valor do CK é o que entra na análise de RQ3/RQ5.

### C. Estrutura do repositório

```
docs/          Desenho do experimento, protocolo de execução e desvios observados
katas/         As 4 katas autorais: enunciado, esqueleto, testes de aceitação e referência
ferramentas/   Código Java de apoio: cronômetro, runner de testes
scripts/       Preparação de ambiente, execução de trial e coleta de métricas estáticas
analise/       Análise estatística em Python (Sprint 3)
data/          Dados brutos e derivados (trials, métricas, snapshots, análises)
relatorio/     Anotações dos trials (Relatorio.md) e relatório final (este documento)
tools/         CK e PMD baixados localmente (fora do versionamento)
```

**Convenções seguintes:** commits no padrão `tipo: #N descrição`; análise usa o campo `collected_at`, nunca a
data de execução; o token do GitHub vem da variável de ambiente `GITHUB_TOKEN` e nunca é commitado; quadro
do grupo em https://github.com/users/Cardosoooo/projects/4.
