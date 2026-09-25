"""
Passo 6 do enunciado: dashboard de visualizacao. Issue #29.

Gera as figuras em data/dashboard/ a partir das tabelas de data/analise/ e do
data/trials.csv. Nao recalcula estatistica: o que e mostrado aqui vem dos scripts
analise_rq1_rq2.py e analise_rq3_rq5.py.

Rodar a partir da raiz do repositorio:
    python analise/dashboard.py

Regras de honestidade visual adotadas, vindas do guia de graficos da disciplina e das
limitacoes declaradas no desenho:

1. Distribuicao antes de resumo. Com quatro observacoes por tratamento, todo grafico
   de grupo mostra os pontos individuais por cima do boxplot. Media nao aparece como
   estatistica principal em lugar nenhum.
2. O n fica visivel em cada painel, para que ninguem leia uma caixa de quatro pontos
   como se fosse uma amostra grande.
3. Trials censurados no time-box sao desenhados com marcador proprio. Eles nao sao
   tempos de conclusao e nao podem parecer tempos de conclusao.
4. Nenhuma marca de significancia. Com quatro pares o menor p-valor alcancavel e
   0,0625, entao asterisco de significancia seria enganoso por construcao.
5. Escala logaritmica no tempo, porque os valores vao de 57 a 2100 segundos e a escala
   linear esmagaria o grupo com IA contra o eixo.
"""

from pathlib import Path

import matplotlib
matplotlib.use("Agg")

import matplotlib.pyplot as plt
import pandas as pd
import seaborn as sns
from matplotlib.lines import Line2D

import dados

SAIDA = dados.RAIZ / "data" / "dashboard"

COR = {"COM_IA": "#2E6F95", "SEM_IA": "#C1562E"}
ROTULO = {"COM_IA": "Com IA", "SEM_IA": "Sem IA"}
TIME_BOX = dados.TIME_BOX_SEGUNDOS

sns.set_theme(style="whitegrid", context="talk")
plt.rcParams.update(
    {
        "figure.dpi": 160,
        "savefig.dpi": 160,
        "savefig.bbox": "tight",
        "font.family": "DejaVu Sans",
        "axes.titlesize": 15,
        "axes.titleweight": "semibold",
        "axes.labelsize": 12,
        "xtick.labelsize": 11,
        "ytick.labelsize": 11,
        "legend.fontsize": 10,
    }
)


def _titulo(ax, titulo: str, subtitulo: str) -> None:
    """Titulo em duas alturas: a pergunta em cima, a leitura logo abaixo."""
    ax.set_title(titulo, loc="left", pad=30)
    ax.text(
        0.0,
        1.015,
        subtitulo,
        transform=ax.transAxes,
        fontsize=10.5,
        color="#4A4A4A",
        va="bottom",
    )


def _salvar(fig, nome: str) -> Path:
    caminho = SAIDA / nome
    fig.savefig(caminho, facecolor="white")
    plt.close(fig)
    print(f"  {nome}")
    return caminho


def _caixa_com_pontos(ax, df: pd.DataFrame, coluna: str, marcar_censura: bool = False):
    """
    Boxplot por tratamento com os pontos individuais por cima.

    Com n=4 a caixa sozinha sugere uma distribuicao que nao foi observada. Os pontos
    deixam claro que cada caixa resume quatro medidas.
    """
    grupos = ["COM_IA", "SEM_IA"]
    valores = [df.loc[df["tratamento"] == g, coluna].dropna().values for g in grupos]

    caixas = ax.boxplot(
        valores,
        positions=[0, 1],
        widths=0.45,
        patch_artist=True,
        medianprops={"color": "#222222", "linewidth": 2.2},
        whiskerprops={"color": "#777777"},
        capprops={"color": "#777777"},
        showfliers=False,
    )
    for caixa, grupo in zip(caixas["boxes"], grupos):
        caixa.set_facecolor(COR[grupo])
        caixa.set_alpha(0.22)
        caixa.set_edgecolor(COR[grupo])
        caixa.set_linewidth(1.6)

    rng = pd.Series(range(100))  # deslocamento deterministico, sem aleatoriedade
    for posicao, grupo in enumerate(grupos):
        sub = df[df["tratamento"] == grupo].dropna(subset=[coluna]).reset_index(drop=True)
        for i, linha in sub.iterrows():
            desloc = (i - (len(sub) - 1) / 2) * 0.085
            censurado = bool(linha.get("censurado", False)) if marcar_censura else False
            ax.scatter(
                posicao + desloc,
                linha[coluna],
                s=95 if not censurado else 130,
                marker="X" if censurado else "o",
                color=COR[grupo],
                edgecolor="white",
                linewidth=1.3,
                zorder=3,
            )

    ax.set_xticks([0, 1])
    ax.set_xticklabels([f"{ROTULO[g]}\n(n={len(v)})" for g, v in zip(grupos, valores)])
    ax.set_xlim(-0.6, 1.6)
    return caixas


def _pares(ax, pares: pd.DataFrame, unidade: str = "", formato: str = "{:.0f}"):
    """
    Grafico pareado: uma linha por kata ligando o valor sem IA ao valor com IA.

    E o desenho do experimento desenhado. O pareamento por kata e o que neutraliza a
    dificuldade da kata, entao mostrar o par e mais informativo que mostrar dois grupos
    soltos.
    """
    completos = pares[pares["par_completo"]].reset_index(drop=True)
    for i, linha in completos.iterrows():
        y = len(completos) - 1 - i
        ax.plot(
            [linha["SEM_IA"], linha["COM_IA"]],
            [y, y],
            color="#B0B0B0",
            linewidth=2.2,
            zorder=1,
            solid_capstyle="round",
        )
        ax.scatter(linha["SEM_IA"], y, s=150, color=COR["SEM_IA"], zorder=3,
                   edgecolor="white", linewidth=1.5)
        ax.scatter(linha["COM_IA"], y, s=150, color=COR["COM_IA"], zorder=3,
                   edgecolor="white", linewidth=1.5)

    ax.set_yticks(range(len(completos)))
    ax.set_yticklabels(list(completos["kata_id"])[::-1])
    ax.set_ylim(-0.7, len(completos) - 0.3)
    ax.grid(axis="y", visible=False)
    return completos


def _legenda_tratamentos(ax, extras=None, loc="best"):
    itens = [
        Line2D([], [], marker="o", linestyle="", color=COR["COM_IA"],
               markersize=10, label=ROTULO["COM_IA"]),
        Line2D([], [], marker="o", linestyle="", color=COR["SEM_IA"],
               markersize=10, label=ROTULO["SEM_IA"]),
    ]
    if extras:
        itens.extend(extras)
    ax.legend(handles=itens, loc=loc, frameon=True, framealpha=0.95)


# --------------------------------------------------------------------- figuras


def figura_tempo_distribuicao(trials: pd.DataFrame) -> None:
    fig, ax = plt.subplots(figsize=(8.2, 6.0))
    _caixa_com_pontos(ax, trials, "tempo_segundos", marcar_censura=True)

    ax.axhline(TIME_BOX, color="#8A8A8A", linestyle="--", linewidth=1.4, zorder=0)
    ax.text(1.58, TIME_BOX * 1.04, "time-box 2100 s", ha="right", fontsize=10,
            color="#6A6A6A")

    ax.set_yscale("log")
    ax.set_ylabel("Tempo até todos os testes passarem (s, escala log)")
    _titulo(
        ax,
        "RQ1 · Tempo de resolução por tratamento",
        "Mediana de 126,5 s com IA contra 1997 s sem IA. O X marca trial censurado no time-box.",
    )
    _legenda_tratamentos(
        ax,
        extras=[Line2D([], [], marker="X", linestyle="", color="#555555",
                       markersize=11, label="Censurado no time-box")],
        loc="center right",
    )
    _salvar(fig, "fig01_tempo_distribuicao.png")


def figura_tempo_pares(pares: pd.DataFrame) -> None:
    fig, ax = plt.subplots(figsize=(9.0, 5.2))
    completos = _pares(ax, pares)

    for i, linha in completos.iterrows():
        y = len(completos) - 1 - i
        ax.annotate(
            f"{linha['diferenca']:+.0f} s",
            xy=(max(linha["SEM_IA"], linha["COM_IA"]) * 1.12, y),
            va="center", fontsize=10.5, color="#4A4A4A",
        )

    ax.set_xscale("log")
    ax.set_xlim(35, 6000)
    ax.set_xlabel("Tempo (s, escala log)")
    _titulo(
        ax,
        "RQ1 · O mesmo problema, com e sem assistente",
        "As quatro katas foram resolvidas mais rápido com IA. Nenhuma exceção entre os 4 pares.",
    )
    _legenda_tratamentos(ax, loc="lower right")
    _salvar(fig, "fig02_tempo_pares.png")


def figura_taxa_sucesso(pares: pd.DataFrame) -> None:
    fig, ax = plt.subplots(figsize=(8.8, 5.4))
    katas = list(pares["kata_id"])
    x = range(len(katas))
    largura = 0.36

    for deslocamento, grupo in ((-largura / 2, "COM_IA"), (largura / 2, "SEM_IA")):
        alturas, posicoes = [], []
        for i, (_, linha) in enumerate(pares.iterrows()):
            valor = linha[grupo]
            posicoes.append(i + deslocamento)
            alturas.append(0 if pd.isna(valor) else valor)
        barras = ax.bar(posicoes, alturas, largura, color=COR[grupo],
                        edgecolor="white", linewidth=1.2, label=ROTULO[grupo])
        for barra, (_, linha) in zip(barras, pares.iterrows()):
            valor = linha[grupo]
            if pd.isna(valor):
                ax.text(barra.get_x() + barra.get_width() / 2, 48,
                        "não medido\n(trial censurado,\nsem contagem aos 35 min)",
                        ha="center", va="center", fontsize=9.5, color="#8A2E2E",
                        fontweight="bold", linespacing=1.5)
            else:
                ax.text(barra.get_x() + barra.get_width() / 2, valor + 1.5,
                        f"{valor:.0f}%", ha="center", fontsize=10, color="#3A3A3A")

    ax.set_xticks(list(x))
    ax.set_xticklabels(katas)
    ax.set_ylim(0, 112)
    ax.set_ylabel("Testes de aceitação aprovados (%)")
    _titulo(
        ax,
        "RQ2 · Taxa de sucesso por kata",
        "Efeito de teto: 6 dos 8 trials terminaram com 100%. Os dois abaixo disso são trials sem IA.",
    )
    # A legenda vai abaixo do eixo: acima ela colide com o subtitulo, que e longo e
    # ocupa toda a largura do painel.
    ax.legend(loc="upper center", bbox_to_anchor=(0.5, -0.10), ncols=2,
              frameon=False)
    _salvar(fig, "fig03_taxa_sucesso.png")


def figura_complexidade(trials: pd.DataFrame, pares: pd.DataFrame) -> None:
    fig, eixos = plt.subplots(1, 2, figsize=(13.4, 5.6))

    _caixa_com_pontos(eixos[0], trials, "cc_media_por_metodo")
    eixos[0].set_ylabel("Complexidade ciclomática média por método")
    _titulo(
        eixos[0],
        "RQ3 · Distribuição",
        "Mediana 3,64 com IA contra 7,33 sem IA.",
    )

    completos = _pares(eixos[1], pares)
    for i, linha in completos.iterrows():
        y = len(completos) - 1 - i
        eixos[1].annotate(f"{linha['diferenca']:+.2f}",
                          xy=(max(linha["SEM_IA"], linha["COM_IA"]) + 0.9, y),
                          va="center", fontsize=10.5, color="#4A4A4A")
    eixos[1].set_xlim(0, 23)
    eixos[1].set_xlabel("Complexidade média por método")
    _titulo(
        eixos[1],
        "RQ3 · Pares por kata",
        "Direção mista: duas katas para cada lado, efeito moderado.",
    )
    _legenda_tratamentos(eixos[1], loc="lower right")
    _salvar(fig, "fig04_complexidade.png")


def figura_loc_controle(trials: pd.DataFrame) -> None:
    fig, ax = plt.subplots(figsize=(8.4, 6.0))

    for grupo in ("COM_IA", "SEM_IA"):
        sub = trials[trials["tratamento"] == grupo]
        ax.scatter(sub["loc_total"], sub["cc_total"], s=170, color=COR[grupo],
                   edgecolor="white", linewidth=1.5, zorder=3, label=ROTULO[grupo])
        for _, linha in sub.iterrows():
            ax.annotate(linha["kata_id"].replace("kata", "k"),
                        xy=(linha["loc_total"], linha["cc_total"]),
                        xytext=(7, -4), textcoords="offset points",
                        fontsize=9.5, color="#5A5A5A")

    ax.set_xlabel("Linhas de código (CK)")
    ax.set_ylabel("Complexidade ciclomática total")
    _titulo(
        ax,
        "Controle · Tamanho contra complexidade",
        "LOC entra como controle obrigatório: sem ele, código maior parece código mais complexo.",
    )
    ax.legend(loc="upper left", frameon=True, framealpha=0.95)
    _salvar(fig, "fig05_loc_controle.png")


def figura_rq5(trials: pd.DataFrame, pares: pd.DataFrame) -> None:
    fig, eixos = plt.subplots(1, 2, figsize=(13.4, 5.6))

    _caixa_com_pontos(eixos[0], trials, "cc_por_loc")
    eixos[0].set_ylabel("Complexidade por linha de código")
    _titulo(
        eixos[0],
        "RQ5 · Distribuição normalizada",
        "Mediana 0,228 com IA contra 0,247 sem IA.",
    )

    completos = _pares(eixos[1], pares)
    for i, linha in completos.iterrows():
        y = len(completos) - 1 - i
        eixos[1].annotate(f"{linha['diferenca']:+.3f}",
                          xy=(max(linha["SEM_IA"], linha["COM_IA"]) + 0.012, y),
                          va="center", fontsize=10.5, color="#4A4A4A")
    eixos[1].set_xlim(0, 0.44)
    eixos[1].set_xlabel("Complexidade por linha")
    _titulo(
        eixos[1],
        "RQ5 · Pares por kata",
        "A diferença da RQ3 encolhe ao normalizar por tamanho.",
    )
    _legenda_tratamentos(eixos[1], loc="lower right")
    _salvar(fig, "fig06_rq5_complexidade_normalizada.png")


def figura_rq4(rq4: pd.DataFrame) -> None:
    fig, ax = plt.subplots(figsize=(9.6, 5.0))

    rq4 = rq4.sort_values("tratamento").reset_index(drop=True)
    for i, linha in rq4.iterrows():
        y = len(rq4) - 1 - i
        grupo = linha["tratamento"]
        ax.barh(y, linha["tempo_segundos"], height=0.5, color="#E3E3E3",
                edgecolor="white", zorder=1)
        ax.barh(y, linha["tempo_primeiro_verde_s"], height=0.5, color=COR[grupo],
                edgecolor="white", zorder=2)
        ax.text(linha["tempo_segundos"] + 45, y,
                f"{linha['proporcao_do_total']:.0%} do trial",
                va="center", fontsize=10.5, color="#4A4A4A")

    ax.set_yticks(range(len(rq4)))
    ax.set_yticklabels(
        [f"{l['kata_id']} · {l['sujeito'].capitalize()}" for _, l in rq4.iterrows()][::-1]
    )
    ax.set_xlim(0, 2600)
    ax.set_xlabel("Tempo (s)")
    ax.grid(axis="y", visible=False)
    _titulo(
        ax,
        "RQ4 · Quando chega o primeiro teste verde",
        "Barra cheia é o trial inteiro; a parte colorida vai até o primeiro teste passar.",
    )
    _legenda_tratamentos(ax, loc="upper right")
    _salvar(fig, "fig07_rq4_primeiro_verde.png")


def figura_efeitos(testes: pd.DataFrame) -> None:
    fig, ax = plt.subplots(figsize=(9.8, 5.6))

    rotulos = {
        "tempo_segundos": "Tempo até o verde (RQ1)",
        "taxa_sucesso": "Taxa de sucesso (RQ2)",
        "cc_media_por_metodo": "Complexidade por método (RQ3)",
        "pct_duplicacao_cpd": "Duplicação (RQ3)",
        "tempo_primeiro_verde_s": "1º verde em segundos (RQ4)",
        "proporcao_primeiro_verde": "1º verde como fração do trial (RQ4)",
        "loc_total": "Linhas de código (controle)",
        "cc_por_loc": "Complexidade por linha (RQ5)",
    }
    testes = testes[testes["tamanho_efeito"].notna()].copy()
    testes["rotulo"] = testes["variavel"].map(rotulos)

    for i, (_, linha) in enumerate(testes.iterrows()):
        y = len(testes) - 1 - i
        efeito = linha["tamanho_efeito"]
        testavel = pd.notna(linha["p_valor"])

        # Variavel sem teste valido nao pode competir visualmente com as testadas. O
        # efeito da RQ2 vem de um unico par nao empatado, entao o valor existe mas nao
        # significa nada. Cinza e marcador vazado dizem isso sem precisar de nota.
        cor = ("#9A9A9A" if not testavel
               else COR["COM_IA"] if efeito < 0 else COR["SEM_IA"])
        ax.plot([0, efeito], [y, y], color=cor, linewidth=3, zorder=2,
                linestyle="-" if testavel else (0, (4, 3)))
        ax.scatter(efeito, y, s=190, zorder=3, linewidth=1.8,
                   color=cor if testavel else "white",
                   edgecolor="white" if testavel else cor)

        # O texto fica curto de proposito: no efeito maximo, -1,0, uma anotacao longa
        # invade a area dos rotulos do eixo. O numero de pares so aparece quando foge
        # dos 4 habituais, que e o caso da RQ2.
        uteis = int(linha["n_pares_uteis"])
        if testavel:
            texto = f"p = {linha['p_valor']:.3f}"
            if uteis != 4:
                texto += f"  ·  {uteis} pares"
        else:
            texto = f"não testável · {uteis} par"
        ax.text(efeito + (0.05 if efeito >= 0 else -0.05), y, texto,
                va="center", ha="left" if efeito >= 0 else "right",
                fontsize=10, color="#4A4A4A" if testavel else "#8A2E2E")

    ax.axvline(0, color="#777777", linewidth=1.2)
    ax.set_yticks(range(len(testes)))
    ax.set_yticklabels(list(testes["rotulo"])[::-1])
    ax.set_xlim(-2.2, 2.2)
    ax.set_xlabel("Tamanho de efeito (rank-biserial)   ←  menor com IA        maior com IA  →")
    ax.grid(axis="y", visible=False)
    _titulo(
        ax,
        "Tamanho de efeito por variável",
        "Sem marca de significância: com 4 pares o menor p alcançável é 0,0625, então 0,05 é inatingível por construção.",
    )
    _salvar(fig, "fig08_tamanhos_de_efeito.png")


def main() -> None:
    SAIDA.mkdir(parents=True, exist_ok=True)
    analise = dados.SAIDA_DIR

    trials = dados.carregar_completo()
    trials["cc_por_loc"] = (trials["cc_total"] / trials["loc_total"]).round(4)

    rq4 = pd.read_csv(analise / "rq4_primeiro_verde.csv")
    testes = pd.concat(
        [
            pd.read_csv(analise / "testes_rq1_rq2.csv"),
            pd.read_csv(analise / "testes_rq3_rq5.csv"),
        ],
        ignore_index=True,
    )

    print("gerando figuras em data/dashboard/")
    figura_tempo_distribuicao(trials)
    figura_tempo_pares(pd.read_csv(analise / "pares_tempo.csv"))
    figura_taxa_sucesso(pd.read_csv(analise / "pares_taxa_sucesso.csv"))
    figura_complexidade(trials, pd.read_csv(analise / "pares_cc_media_por_metodo.csv"))
    figura_loc_controle(trials)
    figura_rq5(trials, pd.read_csv(analise / "pares_cc_por_loc.csv"))
    figura_rq4(rq4)
    figura_efeitos(testes)
    print(f"\n8 figuras gravadas em {SAIDA.relative_to(dados.RAIZ)}")


if __name__ == "__main__":
    main()
