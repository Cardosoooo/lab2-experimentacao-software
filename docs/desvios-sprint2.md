# Desvios de Protocolo na Sprint 2

Registro honesto do que saiu do protocolo durante a execução. Entra no relatório
final como limitação. Nada aqui é apagado ou ajustado para parecer melhor do que foi.

---

## 1. Os quatro trials do Guilherme foram executados sem a instrumentação

**O que aconteceu.** Os trials `guilherme-kata01-COM_IA`, `guilherme-kata02-COM_IA`,
`guilherme-kata03-SEM_IA` e `guilherme-kata04-SEM_IA` foram resolvidos sem usar
`ferramentas/Cronometro.java`. O tempo foi anotado à mão em `relatorio/Relatorio.md`,
e o código final foi commitado por cima do esqueleto em `katas/kataNN/src` em vez de
ser copiado para `data/trials/<trial_id>/src`.

**Consequências nos dados.**

| Campo | Origem do valor |
|-------|-----------------|
| `tempo_segundos` | anotação manual do sujeito, precisão de minuto e segundo |
| `testes_total` e `testes_passando` | anotação manual do sujeito |
| `n_prompts` | anotação manual do sujeito, nos dois trials com IA |
| `tempo_primeiro_verde_s` | **auto-relato posterior**, ver abaixo |
| `inicio_iso` e `fim_iso` | **derivados da hora do commit**, ver abaixo |
| métricas de CK e PMD | medidas pelo pipeline sobre o código final recuperado |

**Recuperação feita.** O código final de cada trial foi extraído do histórico do Git e
arquivado em `data/trials/<trial_id>/src`, e o pipeline de métricas foi executado
sobre os quatro. As métricas de RQ3 e RQ5 desses trials, portanto, são medidas
completas e de mesma qualidade que as dos demais trials.

### 1.1 Tempo até o primeiro teste verde: auto-relato posterior

Os quatro valores de `tempo_primeiro_verde_s` dos trials do Guilherme foram
**informados pelo sujeito depois da execução**, a partir do que ele acompanhou durante
o próprio trial. Não foram capturados pelo comando `verde` do cronômetro, que é o
instrumento previsto no protocolo.

Sem eles a RQ4 não teria par nenhum e ficaria sem resposta. Com eles a questão passa a
ter os quatro pares e um resultado, mas **apoiado em evidência de qualidade inferior à
das demais questões**, que dependem apenas de medida automatizada. A RQ4 deve ser lida
com essa ressalva no relatório e na apresentação.

### 1.2 Horários de início e fim: derivados da hora do commit

Os campos `inicio_iso` e `fim_iso` dos quatro trials não foram registrados por
ninguém. Foram preenchidos por derivação, a partir de um registro que existe e é
verificável: a hora em que cada trial foi commitado, gravada no histórico do Git.

Regra aplicada: `fim_iso` recebe a hora do commit daquele trial, e `inicio_iso` recebe
esse horário menos a duração registrada em `tempo_segundos`.

| Trial | Commit | `inicio_iso` | `fim_iso` |
|-------|--------|--------------|-----------|
| `guilherme-kata01-COM_IA` | `a9649f5` | 10:13:03 | 10:14:33 |
| `guilherme-kata02-COM_IA` | `1129ccb` | 10:20:08 | 10:23:20 |
| `guilherme-kata03-SEM_IA` | `e85fda6` | 10:52:32 | 11:27:32 |
| `guilherme-kata04-SEM_IA` | `76860aa` | 11:39:42 | 11:59:47 |

Duas ressalvas. O `fim_iso` é a hora do commit e não a hora exata em que o sujeito
encerrou: houve algum intervalo entre encerrar e commitar, então é um limite superior.
E no `guilherme-kata03-SEM_IA` o `inicio_iso` está deslocado, porque a duração usada no
cálculo é a censurada, de 2100 segundos, enquanto o trial real durou mais. Naquele
caso o horário marca o ponto a partir do qual o time-box seria contado, não o momento
em que ele começou.

Esses dois campos não entram em nenhuma análise. Existem para auditoria e são a única
parte do conjunto de dados obtida por derivação.

## 2. O trial `guilherme-kata03-SEM_IA` ultrapassou o time-box

**O que aconteceu.** A anotação diz "Tempo: 35+" e "Taxa de sucesso: 100%". Pelo
protocolo, ao atingir 2100 segundos o trial é encerrado e vale o número de testes que
estavam passando naquele instante.

**Como foi registrado.** `tempo_segundos` igual a 2100 e `censurado` igual a `true`,
que é o tratamento correto para tempo censurado. O 15 de 15 da anotação **não** foi
usado: é o estado do código depois que o sujeito terminou, já fora do tempo limite, e
registrá-lo distorceria a RQ2 a favor do tratamento sem IA.

O valor usado, **12 de 15**, foi informado pelo sujeito posteriormente como o estado da
suíte quando o time-box venceu. Tem a mesma natureza de auto-relato descrita na seção
1.1, e não veio do runner de testes.

**Comparação com o outro trial censurado.** O `gabriel-kata02-SEM_IA` também
ultrapassou o time-box, com duração real de 2346 segundos, ou 39,1 minutos. A contagem
de 13 de 14 foi registrada pelo runner no momento em que o sujeito parou, com a kata
ainda incompleta. É medida, mas colhida aos 39 minutos e não aos 35, então também é
aproximação do estado no instante do time-box, ainda que por margem bem menor.

**Impacto na análise.** A RQ2 passa a ter os quatro pares, dos quais dois não empatados.
É o mínimo para o teste de Wilcoxon rodar, e o menor p-valor alcançável com dois pares
é 0,250.

## 3. Os esqueletos foram sobrescritos e restaurados

**O que aconteceu.** As soluções foram commitadas em `katas/kataNN/src`, que é o ponto
de partida de toda tentativa. Os quatro esqueletos deixaram de existir na ponta do
repositório.

**Recuperação feita.** Os quatro esqueletos foram restaurados a partir do commit
`8c96e3e` e verificados: compilam e falham em todos os testes das quatro katas.

**Ameaça que permanece.** As soluções da `kata01` e da `kata02` ficaram visíveis no
repositório entre os commits `a9649f5` e a restauração. O sujeito que ainda vai
resolver essas duas katas no tratamento `SEM_IA` declara não tê-las consultado. Como
isso depende de auto-relato, fica registrado como ameaça à validade interna, na mesma
categoria de difusão de tratamento já prevista em `ameacas-validade.md`.

## 4. Defeito na leitura da saída do CK, corrigido e recoletado

**O que aconteceu.** O `scripts/coleta_metricas.ps1` lia o `method.csv` do CK partindo
cada linha por vírgula. O CK escreve a assinatura do método entre aspas, e ela contém
vírgulas quando há mais de um parâmetro, como em
`"cobrar/3[java.lang.String,java.lang.String,boolean]"`. Nesses casos as colunas se
deslocavam e a complexidade era lida do campo errado.

**Como foi descoberto.** O trial `gabriel-kata02-SEM_IA` saiu com complexidade total
zero num código que passa em 13 dos 14 testes, o que é impossível.

**Correção.** A leitura passou a usar `Import-Csv`, que respeita campos entre aspas.
Todas as métricas já coletadas foram recalculadas, das quatro referências e dos seis
trials existentes na data da correção.

**Valores afetados.** Só arquivos com método de dois ou mais parâmetros. A linha de
base da `kata02`, por exemplo, passou de complexidade média 1,2 para 3,2. Os valores
de linhas de código e de duplicação não mudaram, porque vinham de campos anteriores ao
deslocamento.

### 4.1 Segundo defeito, descoberto na Sprint 3

**O que aconteceu.** O mesmo script gravava o `metricas.csv` de cada trial com
`Add-Content`, ou seja, acrescentando uma linha a cada execução. Como a recoleta
descrita acima foi uma segunda execução, os arquivos ficaram com duas linhas: a
antiga, com os valores errados, e a nova, correta. O arquivo de um trial deve conter
exatamente uma linha.

**Como foi descoberto.** A tabela consolidada da análise de RQ3 mostrou cada trial
duas vezes, com valores diferentes, e o pareamento estava tomando a primeira
ocorrência, que era justamente a errada.

**Correção.** O script passou a reescrever o arquivo em vez de acrescentar. O
carregador de dados da análise, em `analise/dados.py`, ganhou uma proteção que mantém
apenas a coleta mais recente por trial, para que uma linha remanescente não volte a
entrar em silêncio. Todas as métricas foram recoletadas depois da correção.

**Lição para o relatório.** Os dois defeitos foram encontrados porque um número
impossível apareceu na tela: complexidade zero num código que passa nos testes, e um
trial duplicado. Vale registrar na discussão que a conferência de plausibilidade dos
dados foi o que impediu que a análise rodasse sobre valores corrompidos.

## 5. A ordem dos tratamentos ficou em blocos, não alternada

**O que estava previsto.** O desenho original alternava tratamento a cada trial dentro
do sujeito.

**O que foi executado.** O Guilherme fez os dois trials com IA primeiro e os dois sem
IA depois. A atribuição final ficou assim:

| Kata | Guilherme | Gabriel |
|------|-----------|---------|
| kata01 | `COM_IA` | `SEM_IA` |
| kata02 | `COM_IA` | `SEM_IA` |
| kata03 | `SEM_IA` | `COM_IA` |
| kata04 | `SEM_IA` | `COM_IA` |

**Por que o desenho continua válido.** As duas propriedades que importam continuam de
pé: cada kata é resolvida uma vez em cada tratamento, e cada sujeito passa duas vezes
por cada tratamento. O que se perdeu foi o balanceamento de posição dentro do sujeito,
então um eventual efeito de aprendizado ao longo da sessão fica parcialmente
confundido com o tratamento **dentro de cada sujeito**. Como os dois sujeitos usam
blocos invertidos, o efeito se compensa entre eles no pareamento por kata, que é o
pareamento primário declarado em `desenho-experimento.md`.

**Registro.** A tabela de contrabalanceamento do desenho foi atualizada para refletir
o que de fato aconteceu, e não o que estava planejado.
