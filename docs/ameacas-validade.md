# Ameaças à Validade e Questões de Pesquisa Próprias

Complementa o [desenho do experimento](desenho-experimento.md). Cobre o item H do
Passo 1 do enunciado e registra a parcela de contribuição própria do grupo.

---

## 1. Ameaças à validade interna

| Ameaça | Por que é um risco | Mitigação adotada |
|--------|--------------------|-------------------|
| Efeito de aprendizado entre katas | O sujeito melhora ao longo da sessão, e o tratamento aplicado nos últimos trials herda esse ganho | Contrabalanceamento de posição: cada posição da sequência aparece uma vez em cada tratamento |
| Fadiga | Trials no fim da sessão saem piores por cansaço, não por tratamento | Máximo de dois trials por sessão, com intervalo mínimo de 15 minutos entre eles |
| Autoria da kata pelo próprio sujeito | Quem escreveu a kata conhece a solução e resolve mais rápido | O arranjo garante que cada autor resolva uma kata própria com IA e outra sem IA, então a vantagem incide igualmente nos dois braços |
| Vazamento de solução entre integrantes | Quem executa depois pode ter ouvido a abordagem do outro | Nenhuma kata é discutida entre os integrantes até o encerramento da Sprint 2. A pasta `referencia` de cada kata só é aberta pelo autor |
| Difusão de tratamento | Consultar assistente durante um trial `SEM_IA` invalida a comparação | Regra declarada no protocolo e registro do trial. Como o cumprimento depende de auto-relato, isso permanece como limitação declarada |
| Instrumentação | Medir com ferramentas diferentes entre trials introduz diferença artificial | Mesmo cronômetro, mesmo runner, mesmo JDK e mesmas versões de CK e PMD em todos os trials |
| Memorização de exercícios conhecidos | O assistente reproduz solução vista no treinamento em vez de resolver | Katas autorais, não publicadas antes da execução |

## 2. Ameaças à validade externa

| Ameaça | Alcance da limitação |
|--------|----------------------|
| Dois sujeitos, mesma turma e nível de formação semelhante | Os resultados descrevem este grupo. Não se estendem a desenvolvedores profissionais nem a outros níveis de experiência |
| Katas pequenas e com time-box de 35 minutos | O experimento mede tarefas curtas e bem especificadas. Não permite conclusão sobre manutenção de sistemas grandes ou requisitos ambíguos |
| Um único assistente e uma única versão | O resultado vale para Claude na versão gratuita. Não se transfere para assistentes integrados à IDE, que atuam por completação contínua e não por conversa |
| Linguagem única | Todas as katas são em Java. Efeitos podem diferir em linguagens mais concisas |
| Familiaridade prévia desigual com o assistente | Se um integrante usa assistentes com mais frequência que o outro, parte do efeito medido é habilidade de uso e não a ferramenta. O nível de familiaridade de cada um é registrado antes da execução |

## 3. Ameaças à validade de construto

| Ameaça | Detalhe |
|--------|---------|
| Tempo até o verde não é qualidade | A métrica captura resolução funcional dentro do time-box. Não diz nada sobre legibilidade ou facilidade de manutenção |
| A taxa de sucesso depende da suíte escrita pelo grupo | Uma suíte fraca infla a taxa de sucesso dos dois tratamentos. Cada suíte cobre caminho feliz, casos de borda e entradas inválidas, e é revisada pelo integrante que não escreveu a kata |
| Complexidade ciclomática é proxy | O valor agregado por classe do CK mede caminhos de decisão, não clareza do código |
| Duplicação depende de configuração | O resultado do CPD varia conforme o tamanho mínimo de trecho considerado. O valor usado é fixado no script de coleta e registrado no relatório |

## 4. Ameaças à validade de conclusão

| Ameaça | Detalhe e tratamento |
|--------|----------------------|
| Poder estatístico | Com 4 pares, o menor p-valor alcançável no Wilcoxon é 0,0625 unilateral e 0,125 bilateral. Nenhum resultado poderá ser declarado significativo a 0,05. A leitura se apoia em tamanho de efeito e consistência de direção |
| Número de sujeitos | Apenas dois sujeitos, o que limita a variabilidade individual observável e impede qualquer análise por perfil |
| Comparações múltiplas | São seis hipóteses testadas sobre os mesmos 8 trials. Nenhuma correção formal é aplicada, porque o estudo é exploratório e o poder já é o fator limitante. Os p-valores são reportados sem ajuste e lidos como indicativos |
| Empates por censura | Trials censurados em 2100 segundos geram valores idênticos, e empates reduzem ainda mais o poder do Wilcoxon. A quantidade de trials censurados é reportada junto de cada teste |
| Dependência entre observações | O mesmo sujeito contribui com quatro trials. O pareamento do teste absorve parte dessa dependência, mas não toda |

---

## 5. Questões de pesquisa próprias do grupo

O enunciado corresponde a 70% do que se espera do trabalho. Os 30% restantes são
propostos pelo grupo. As duas questões abaixo reaproveitam os mesmos 8 trials, sem
custo adicional de execução, e atacam pontos que as questões do enunciado deixam em
aberto.

### RQ4 — O assistente antecipa o primeiro teste verde ou apenas o último?

**Motivação.** A RQ1 mede apenas o instante em que a kata fica inteiramente
resolvida. Isso confunde dois ganhos diferentes: começar mais rápido e terminar mais
rápido. Se o assistente ajuda principalmente a vencer a página em branco, o efeito
aparece cedo no trial e some depois. Se ajuda a fechar casos de borda, aparece só no
fim.

**Hipóteses.**

- H0: a mediana do tempo até o primeiro teste de aceitação passar é igual nos dois tratamentos.
- H1: a mediana é menor no tratamento com IA.

**Métrica.** `tempo_primeiro_verde_s`, em segundos, contado do início do trial até a
primeira execução da suíte em que pelo menos um teste passa.

**Coleta.** O cronômetro registra o instante da primeira execução do runner que
retorna contagem de aprovados maior que zero. Não exige nenhuma ação extra do sujeito
durante o trial.

**Leitura esperada.** Comparar a razão entre tempo até o primeiro verde e tempo total
nos dois tratamentos. Razão menor com IA indica ganho concentrado no arranque.

### RQ5 — A diferença de complexidade se mantém após normalizar por linhas de código?

**Motivação.** O próprio enunciado alerta que código gerado com IA tende a ser mais
verboso, e que complexidade e duplicação sem normalizar por tamanho podem enganar. A
RQ3 trata LOC como variável de controle, mas não testa a hipótese normalizada. Sem
isso, um resultado de RQ3 pode significar apenas que o arquivo ficou maior.

**Hipóteses.**

- H0: a mediana da complexidade ciclomática por linha de código é igual nos dois tratamentos.
- H1: as medianas diferem.

**Métrica.** `cc_por_loc`, razão entre a soma da complexidade ciclomática de todas as
classes do trial e o total de linhas de código do mesmo trial, ambos extraídos do CK.

**Coleta.** Derivada da saída do CK já produzida para a RQ3, sem execução adicional.

**Leitura esperada.** Se a RQ3 acusar diferença de complexidade e a RQ5 não, a
conclusão é que a IA produz mais código e não código mais complexo por linha. Se
ambas acusarem, a diferença é estrutural e não apenas de volume.
