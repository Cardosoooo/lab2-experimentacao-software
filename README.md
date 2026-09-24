# Laboratório 02 — Assistentes de IA vs. codificação manual

Experimento controlado que mede quantitativamente o efeito do uso de um assistente
de IA generativa na resolução de tarefas de programação.

- **Disciplina:** Laboratório de Experimentação de Software — Engenharia de Software, PUC Minas
- **Turma:** Noite — 6º período
- **Professor:** Danilo Maia
- **Integrantes:** Gabriel Cardoso ([@Cardosoooo](https://github.com/Cardosoooo)) e Guilherme Brina Ferreira ([@Gmbferreira](https://github.com/Gmbferreira))

## Questões de pesquisa

| ID | Pergunta |
|----|----------|
| RQ1 | O uso de assistente de IA reduz o tempo necessário para resolver uma tarefa de programação? |
| RQ2 | O uso de assistente de IA reduz a quantidade de defeitos (testes que falham) no código produzido? |
| RQ3 | O uso de assistente de IA altera a complexidade ciclomática ou a duplicação do código produzido? |
| RQ4 | O assistente antecipa o primeiro teste verde ou apenas o último? *(questão própria do grupo)* |
| RQ5 | A diferença de complexidade se mantém após normalizar por linhas de código? *(questão própria do grupo)* |

O detalhamento de hipóteses, variáveis, métricas e ameaças à validade está em
[`docs/desenho-experimento.md`](docs/desenho-experimento.md).

A RQ4 ficou sem par para teste estatístico, porque o tempo até o primeiro teste verde
não foi medido em quatro dos oito trials. O motivo está em
[`docs/desvios-sprint2.md`](docs/desvios-sprint2.md) e a questão entra no relatório
como análise descritiva.

**Quadro do grupo:** https://github.com/users/Cardosoooo/projects/4

## Desenho em uma tabela

Crossover within-subject contrabalanceado, com time-box fixo de 35 minutos por trial.

| Sujeito | kata01 | kata02 | kata03 | kata04 |
|---------|--------|--------|--------|--------|
| Guilherme | **com IA** | **com IA** | sem IA | sem IA |
| Gabriel | sem IA | sem IA | **com IA** | **com IA** |

Cada kata aparece uma vez em cada tratamento e cada sujeito passa duas vezes por cada
tratamento. Total de 8 trials, 4 por tratamento. Os desvios de protocolo observados na
execução estão em [`docs/desvios-sprint2.md`](docs/desvios-sprint2.md).

**Assistente fixado para todos os trials do tratamento com IA:** Claude, versão
gratuita, usado por conversa em janela separada da IDE.

**IDE fixada para os dois sujeitos:** Visual Studio Code 1.110.0 com o Extension Pack
for Java. Extensões de IA integradas à IDE ficam desligadas nos dois tratamentos.

## Estrutura do repositório

```
docs/          Desenho do experimento, protocolo de execução e desvios observados
katas/         As 4 katas autorais: enunciado, esqueleto, testes de aceitação e solução de referência
ferramentas/   Código Java de apoio: cronômetro, runner de testes, utilitários
scripts/       Scripts de preparação de ambiente, execução de trial e coleta de métricas estáticas
analise/       Análise estatística e dashboard, em Python
data/          Dados brutos e derivados do experimento
  trials/        Código final de cada trial, uma pasta por trial
  metricas/      Saída bruta de CK e PMD por trial
  snapshots/     Snapshots semanais do GitHub Projects
  trials.csv     Registro consolidado dos trials
relatorio/     Anotações dos trials e, na Sprint 3, o relatório final
tools/         CK e PMD baixados localmente (fora do versionamento)
```

## Pré-requisitos

| Ferramenta | Versão usada | Para quê |
|------------|--------------|----------|
| JDK | 17 ou superior (validado com Temurin 25) | Katas, testes de aceitação e ferramentas |
| Python | 3.10 ou superior | Análise estatística e dashboard (Sprint 3) |
| CK | 0.7.0 | Complexidade ciclomática e LOC (RQ3 e RQ5) |
| PMD CPD | 7.27.0 | Duplicação de código (RQ3) |
| IDE | VS Code 1.110.0 com Extension Pack for Java | ambiente fixado dos trials |

Nenhuma biblioteca de terceiros é usada no código Java. As dependências de análise
estão em `scripts/requirements.txt`.

## Convenções

- **Commits:** prefixo de tipo seguido do número da Issue, no formato
  `tipo: #N descrição`. Exemplo: `feat: #5 implementa cronometro de trial`.
  Commit sem referência a Issue não é considerado na correção.
- **Board:** GitHub Projects v2 com as colunas Backlog, To Do, Doing, Review e Done,
  limite de 2 cartões em Doing por integrante.
- **Segredos:** o token do GitHub é lido da variável de ambiente `GITHUB_TOKEN` e
  nunca é commitado.
- **Datas:** toda análise usa o campo `collected_at` gravado na coleta, nunca a data
  em que o script roda.

## Como registrar um trial

Sempre a partir da raiz do repositório.

```
javac -d out ferramentas/Cronometro.java ferramentas/ExecutorTestes.java
java -cp out Cronometro iniciar <sujeito> <kata> <tratamento> <ordem>
java -cp out ExecutorTestes <kata>
java -cp out Cronometro verde <trial_id>
java -cp out Cronometro status <trial_id>
java -cp out Cronometro finalizar <trial_id> <total> <passando> <prompts> <commit>
```

O `ExecutorTestes` roda a suíte e mostra quantos testes passam. O comando `verde` é
chamado uma única vez, na primeira vez que a suíte reporta algum teste passando, e
alimenta a RQ4. O `finalizar` recebe o total de testes, quantos passaram, o número de
prompts usados e o hash do commit do código final.

O trial é registrado como **censurado em 2100 segundos** quando estoura o time-box ou
quando termina sem todos os testes passando. Nesses casos a linha continua no CSV,
como o enunciado exige, e o tempo não é usado como tempo de resolução.

O roteiro completo, com as regras por tratamento e o arquivamento do código, está em
[`docs/protocolo-execucao.md`](docs/protocolo-execucao.md).

## Dados coletados

Os oito trials foram executados e estão registrados.

| Arquivo | Conteúdo |
|---------|----------|
| [`data/trials.csv`](data/trials.csv) | uma linha por trial, com tempo, censura, testes e prompts |
| `data/trials/<trial_id>/src` | código final de cada trial |
| `data/metricas/<trial_id>` | saída de CK e PMD por trial, mais as quatro referências |
| [`relatorio/Relatorio.md`](relatorio/Relatorio.md) | leitura rápida dos oito trials |
| [`docs/desvios-sprint2.md`](docs/desvios-sprint2.md) | desvios de protocolo e lacunas de medição |

## Análise e dashboard

| Caminho | Conteúdo |
|---------|----------|
| [`analise/`](analise) | scripts de estatística e de geração das figuras |
| [`data/analise/`](data/analise) | tabelas de resultado, com a legenda das colunas no README de lá |
| [`data/dashboard/`](data/dashboard) | as oito figuras em PNG |
| [`relatorio/dashboard.html`](relatorio/dashboard.html) | **o dashboard**, abrir no navegador |

Para regerar tudo, a partir da raiz:

```
pip install -r scripts/requirements.txt
python analise/analise_rq1_rq2.py
python analise/analise_rq3_rq5.py
python analise/dashboard.py
```

Dois dos oito trials foram censurados no time-box, o que é resultado válido e previsto
no desenho.

## Status

| Sprint | Entregável | Situação |
|--------|-----------|----------|
| Lab02S01 | Desenho do experimento e preparação | concluída |
| Lab02S02 | Execução dos 8 trials e coleta | concluída |
| Lab02S03 | Análise estatística e dashboard | não iniciada |
| Relatório Final | Documento consolidado | não iniciado |
