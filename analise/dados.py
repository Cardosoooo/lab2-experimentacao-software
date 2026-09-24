"""
Carregamento e pareamento dos dados do experimento.

Modulo compartilhado pelos scripts de analise da Sprint 3. Nao imprime nada e nao
escreve arquivos: so entrega os dados ja no formato que as analises precisam.

O pareamento primario do desenho e por kata: cada kata tem exatamente uma observacao
COM_IA e uma SEM_IA, o que neutraliza a dificuldade da kata, que e a variavel de
perturbacao dominante. Ver docs/desenho-experimento.md, secao 11.
"""

from pathlib import Path

import pandas as pd

RAIZ = Path(__file__).resolve().parents[1]
TRIALS_CSV = RAIZ / "data" / "trials.csv"
METRICAS_DIR = RAIZ / "data" / "metricas"
SAIDA_DIR = RAIZ / "data" / "analise"

TIME_BOX_SEGUNDOS = 2100


def carregar_trials() -> pd.DataFrame:
    """Le data/trials.csv com os tipos corretos, sem inferencia solta."""
    df = pd.read_csv(TRIALS_CSV)
    df["censurado"] = df["censurado"].astype(str).str.lower() == "true"
    for coluna in [
        "tempo_segundos",
        "tempo_primeiro_verde_s",
        "testes_total",
        "testes_passando",
        "taxa_sucesso",
        "n_prompts",
    ]:
        df[coluna] = pd.to_numeric(df[coluna], errors="coerce")
    return df


def carregar_metricas() -> pd.DataFrame:
    """
    Junta os metricas.csv de cada trial em uma tabela unica.

    As pastas terminadas em '-ref' sao a linha de base das solucoes de referencia e
    ficam de fora: elas nao sao trials e nao entram em nenhuma comparacao entre
    tratamentos.
    """
    linhas = []
    for pasta in sorted(METRICAS_DIR.iterdir()):
        if not pasta.is_dir() or pasta.name.endswith("-ref"):
            continue
        arquivo = pasta / "metricas.csv"
        if arquivo.exists():
            linhas.append(pd.read_csv(arquivo))
    if not linhas:
        raise FileNotFoundError(f"nenhum metricas.csv encontrado em {METRICAS_DIR}")

    tabela = pd.concat(linhas, ignore_index=True)

    # Rede de seguranca: se um metricas.csv trouxer mais de uma coleta do mesmo trial,
    # vale a mais recente pelo collected_at. Sem isso uma linha antiga sobrevivente de
    # uma recoleta entraria silenciosamente na analise.
    duplicados = tabela["trial_id"].duplicated(keep=False)
    if duplicados.any():
        tabela = (
            tabela.sort_values("collected_at")
            .drop_duplicates(subset="trial_id", keep="last")
            .reset_index(drop=True)
        )
    return tabela


def carregar_completo() -> pd.DataFrame:
    """Trials e metricas na mesma tabela, ligados pelo trial_id."""
    trials = carregar_trials()
    metricas = carregar_metricas().rename(columns={"trial_id": "trial_id"})
    return trials.merge(metricas, on="trial_id", how="left", suffixes=("", "_metrica"))


def montar_pares(df: pd.DataFrame, coluna: str) -> pd.DataFrame:
    """
    Monta os pares por kata para uma variavel dependente.

    Devolve uma linha por kata com o valor COM_IA, o valor SEM_IA e a diferenca
    (COM_IA menos SEM_IA). Katas em que qualquer um dos lados esta ausente ficam na
    tabela com valor nulo, para que a lacuna apareca no relatorio em vez de sumir.
    """
    tabela = df.pivot_table(
        index="kata_id", columns="tratamento", values=coluna, aggfunc="first"
    ).reset_index()
    for tratamento in ("COM_IA", "SEM_IA"):
        if tratamento not in tabela.columns:
            tabela[tratamento] = pd.NA
    tabela["diferenca"] = tabela["COM_IA"] - tabela["SEM_IA"]
    tabela["par_completo"] = tabela["COM_IA"].notna() & tabela["SEM_IA"].notna()
    return tabela[["kata_id", "COM_IA", "SEM_IA", "diferenca", "par_completo"]]


def descritiva(df: pd.DataFrame, coluna: str) -> pd.DataFrame:
    """
    Mediana e intervalo interquartil por tratamento.

    O enunciado pede mediana e IQR em vez de media e desvio-padrao, porque com quatro
    observacoes por tratamento e censura no limite superior a media e dominada por
    valores extremos.
    """
    linhas = []
    for tratamento in ("COM_IA", "SEM_IA"):
        serie = df.loc[df["tratamento"] == tratamento, coluna].dropna()
        if serie.empty:
            linhas.append(
                {
                    "tratamento": tratamento,
                    "n": 0,
                    "mediana": None,
                    "q1": None,
                    "q3": None,
                    "iqr": None,
                    "minimo": None,
                    "maximo": None,
                }
            )
            continue
        q1 = float(serie.quantile(0.25))
        q3 = float(serie.quantile(0.75))
        linhas.append(
            {
                "tratamento": tratamento,
                "n": int(serie.size),
                "mediana": float(serie.median()),
                "q1": q1,
                "q3": q3,
                "iqr": q3 - q1,
                "minimo": float(serie.min()),
                "maximo": float(serie.max()),
            }
        )
    return pd.DataFrame(linhas)


def garantir_saida() -> Path:
    """Cria data/analise/ se ainda nao existir e devolve o caminho."""
    SAIDA_DIR.mkdir(parents=True, exist_ok=True)
    return SAIDA_DIR
