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

| Campo | Situação |
|-------|----------|
| `tempo_segundos` | recuperado da anotação manual, com precisão de minuto e segundo |
| `inicio_iso` e `fim_iso` | perdidos, não foram registrados |
| `tempo_primeiro_verde_s` | **perdido nos quatro trials**, não foi medido |
| `testes_total` e `testes_passando` | recuperados da anotação |
| `n_prompts` | recuperado nos dois trials com IA |

**Impacto na análise.** A RQ4, que compara o tempo até o primeiro teste verde, fica
sem metade das observações. Com apenas os quatro trials do Gabriel, ela deixa de ter
par para o teste de Wilcoxon e passa a ser descritiva. Isso precisa constar no
relatório como resultado não obtido, e não como resultado nulo.

**Recuperação feita.** O código final de cada trial foi extraído do histórico do Git e
arquivado em `data/trials/<trial_id>/src`, e o pipeline de métricas foi executado
sobre os quatro. As métricas de RQ3 e RQ5 desses trials, portanto, estão completas.

## 2. O trial `guilherme-kata03-SEM_IA` ultrapassou o time-box

**O que aconteceu.** A anotação diz "Tempo: 35+" e "Taxa de sucesso: 100%". Pelo
protocolo, ao atingir 2100 segundos o trial é encerrado e vale o número de testes que
estavam passando naquele instante.

**Como foi registrado.** `tempo_segundos` igual a 2100 e `censurado` igual a `true`,
que é o tratamento correto para tempo censurado. Os campos `testes_passando` e
`taxa_sucesso` ficaram **em branco**, porque não se sabe quantos testes passavam aos
35 minutos. Preencher com 15 de 15 seria registrar um sucesso obtido fora do tempo
limite, o que distorce a RQ2 a favor do tratamento sem IA.

**Impacto na análise.** A RQ2 fica com três observações completas do Guilherme em vez
de quatro.

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

## 4. A ordem dos tratamentos ficou em blocos, não alternada

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
