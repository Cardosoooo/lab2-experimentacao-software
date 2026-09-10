# Protocolo de Execução e Ambiente — Laboratório 02

Este documento fixa o **ambiente de execução** (versões pinadas das ferramentas) e
o **roteiro passo a passo de um trial**, para que os 8 trials decorram de forma
idêntica, independentemente do sujeito. A fundamentação metodológica está em
[`desenho-experimento.md`](desenho-experimento.md); as ameaças à validade, em
[`ameacas-validade.md`](ameacas-validade.md).

---

## 1. Registro de versões do ambiente

As versões abaixo ficam fixas durante toda a Sprint 2 e são replicadas no relatório
final (Sprint 3). O campo `collected_at` de `data/trials.csv` e `data/metricas/*`
registra o instante real de cada coleta.

| Componente | Versão fixada | Obtido por |
|------------|---------------|------------|
| JDK | Temurin 25 (exige JDK 17+) | instalação do sujeito, `scripts/preparar_ambiente.ps1` valida |
| CK | 0.7.0 | `scripts/preparar_ambiente.ps1` (Maven Central) |
| PMD (CPD) | 7.27.0 | `scripts/preparar_ambiente.ps1` (GitHub Releases) |
| Assistente de IA | Claude, versão gratuita | conta do sujeito (somente tratamento `COM_IA`) |
| Time-box | 35 minutos = 2100 segundos | `ferramentas/Cronometro.java` |
| Suíte de testes | por kata, sem JUnit, idêntica nos dois tratamentos | `katas/<kata>/test/` |
| Linguagem | Java | — |

Qualquer mudança nesses componentes invalida a comparabilidade dos trials e deve ser
registrada no relatório como ameaça.

## 2. Preparação do ambiente

Script: `scripts/preparar_ambiente.ps1`.

1. Instalar o JDK Temurin 25 e garantir `java` e `javac` no `PATH`.
2. Executar, na raiz do repositório:

   ```
   .\scripts\preparar_ambiente.ps1
   ```

   O script valida o JDK (17 ou superior) e baixa CK e PMD nas versões pinadas para a
   pasta `tools/`, que é ignorada pelo versionamento.
3. Revalidar a qualquer momento, sem baixar novamente:

   ```
   .\scripts\preparar_ambiente.ps1 -SkipDownload
   ```

4. Compilar as ferramentas Java uma única vez (a partir da raiz do repositório):

   ```
   javac -d out ferramentas/Cronometro.java ferramentas/ExecutorTestes.java
   ```

## 3. Roteiro de execução de um trial

Pré-condição: as 4 katas estão no repositório e a solução de referência de cada uma
passa em toda a suíte (`java -cp out ExecutorTestes <kata> --ref` retorna exit 0).

### 3.1 Identificação do trial

- `trial_id` no padrão `<sujeito>-<kata_id>-<tratamento>`, por exemplo
  `guilherme-kata01-SEM_IA`.
- `ordem` = posição do trial na sequência do sujeito (1 a 4).
- Escala de trials dos sujeitos (definida em `desenho-experimento.md`, seção 7):
  Gabriel: 1 kata01 `COM_IA`, 2 kata02 `SEM_IA`, 3 kata03 `COM_IA`, 4 kata04 `SEM_IA`;
  Guilherme: 1 kata01 `SEM_IA`, 2 kata02 `COM_IA`, 3 kata03 `SEM_IA`, 4 kata04 `COM_IA`.
  Cada sujeito resolve as 4 katas no papel de autor de 2 delas: a vantagem de conhecer
  o próprio enunciado incide nos dois tratamentos (seção 7 do desenho). Como cada
  autor fez uma tratativa COM_IA e outra SEM_IA, o pareamento interno do sujeito
  mistura autorias de forma balanceada.

### 3.2 Sequência passo a passo

1. **Reset da kata-alvo.** Restaurar o esqueleto para o estado inicial, descartando
   qualquer edição anterior:

   ```
   git restore katas/<kata_id>/src/   (via linha de comando ou GUI)
   ```

   Confira que o esqueleto volta a falhar em toda a suíte:
   `java -cp out ExecutorTestes <kata_id>` retorna exit 1.

2. **Iniciar o trial.** Exatamente ao sinal de início:

   ```
   java -cp out Cronometro iniciar <sujeito> <kata_id> <tratamento> <ordem>
   ```

   O cronômetro cria `data/trials/<trial_id>/` e grava `inicio_iso`. O código final do
   trial será copiado para `data/trials/<trial_id>/src/` ao fim.

3. **Resolver a kata.** O sujeito edita os fontes em `katas/<kata_id>/src/` e usa o
   runner para obter feedback a qualquer momento:

   ```
   java -cp out ExecutorTestes <kata_id>
   ```

   Saída legível na linha `RESUMO;...`: `total`, `passando` e `falhando` (contrato em
   `katas/README.md`).

4. **Primeiro teste verde (RQ4).** No instante em que o primeiro teste da suíte passa:

   ```
   java -cp out Cronometro verde <trial_id>
   ```

5. **Controle de tempo (opcional):**

   ```
   java -cp out Cronometro status <trial_id>
   ```

   mostra decorrido e restante. Estourou o time-box, encerre com o passo 6 mesmo sem
   concluir a kata.

6. **Encerrar o trial.** Ao fim do time-box (35 min), ou quando todos os testes
   passarem:

   ```
   java -cp out ExecutorTestes <kata_id>        (anote total e passando)
   java -cp out Cronometro finalizar <trial_id> <total> <passando> [n_prompts] [commit_final]
   ```

   - `n_prompts`: número de prompts enviados ao assistente (só `COM_IA`, 0 em `SEM_IA`).
   - `commit_final`: hash curto do commit com a solução final do trial.
   - O cronômetro aplica a censura (tempo `2100` e `censurado=true`) quando a kata não
     foi concluída dentro do time-box, mantendo o trial na análise.

7. **Arquivar o código final.** Copiar a solução para o histórico do trial:

   ```
   New-Item -ItemType Directory -Force .\data\trials\<trial_id>\src | Out-Null
   Copy-Item .\katas\<kata_id>\src\*  .\data\trials\<trial_id>\src\
   ```

8. **Commit.** Commit único da solução do trial no padrão das issues, de forma que o
   esqueleto volte ao estado inicial no próximo trial (`git restore` no passo 1).

## 4. Regras por tratamento

| Regra | `COM_IA` | `SEM_IA` |
|-------|----------|----------|
| Assistente de IA generativo | disponível sem restrição durante todo o trial | proibido |
| Internet | liberada | liberada para documentação oficial, proibida para qualquer assistente generativo ou código pré-pronto |
| Autocompletar da IDE | permitido | permitido (sem componente generativo) |
| Consulta a outros sujeitos | proibida | proibida |
| Troca de código entre trials | proibida (esqueleto restaurado a cada trial) | idêntico |

O único fator que difere entre os braços é o acesso ao assistente; ferramentas de
edição e suítes de teste são idênticas, conforme `desenho-experimento.md`, seções 5 e
6.

## 5. Coleta de métricas estáticas (pós-trial)

Script: `scripts/coleta_metricas.ps1` (seção 9 de `desenho-experimento.md`, RQ3).

```
.\scripts\coleta_metricas.ps1 -Trial <trial_id>
```

O script analisa `data/trials/<trial_id>/src/` por padrão e gera por trial:

| Saída | Conteúdo |
|-------|----------|
| `data/metricas/<trial_id>/ck/class.csv` | métricas por classe (CK), inclusive `loc` e `wmc` |
| `data/metricas/<trial_id>/ck/method.csv` | métricas por método (CK), inclusive `wmc` (complexidade ciclomática) |
| `data/metricas/<trial_id>/pmd/cpd.csv` | duplicações detectadas pelo CPD |
| `data/metricas/<trial_id>/metricas.csv` | linha consolidada + cabeçalho |

Colunas consolidadas: `trial_id`, `n_arquivos`, `n_classes`, `loc_total`,
`n_metodos`, `cc_total`, `cc_media_por_metodo` (RQ3a), `duplicacoes_cpd`,
`linhas_duplicadas_cpd`, `pct_duplicacao_cpd` (RQ3b, sobre LOC total),
`min_tokens_cpd` (padrão 50) e `collected_at`.

Para validar o pipeline contra uma solução de referência (calibração), use
`-Diretorio`:

```
.\scripts\coleta_metricas.ps1 -Trial kata01-ref -Diretorio .\katas\kata01\referencia
```

## 6. Snapshot do quadro (pós-trial)

Script: `scripts/snapshot_projects.ps1` (registra a evolução dos cartões).

```
.\scripts\snapshot_projects.ps1 -Projeto 4 -Dono Cardosoooo
```

Grava `data/snapshots/<aaaammdd-hhmmss>.json` com os itens e status do GitHub
Projects, permitindo reconstruir o andamento da sprint entre trials. O script usa o
login do `gh` já autenticado ou a variável `GITHUB_TOKEN`. Recomenda-se um snapshot
por sessão de coleta.

## 7. Registro de dados gerados

- `data/trials.csv` — uma linha por trial (schema do `Cronometro`): trial_id, sujeito,
  kata_id, tratamento, ordem, inicio/fim ISO, tempo, censura, primeiro verde, total e
  passando, taxa de sucesso, n_prompts, commit e collected_at.
- `data/trials/<trial_id>/` — `trial.properties` (estado) e `src/` (código final).
- `data/metricas/<trial_id>/` — saídas brutas de CK e PMD CPD e o consolidado.
- `data/snapshots/<timestamp>.json` — evolução do quadro.