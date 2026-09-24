"""
Teste de Wilcoxon pareado e tamanho de efeito.

Separado dos scripts de analise porque RQ1, RQ2, RQ3 e RQ5 usam exatamente o mesmo
procedimento, mudando so a variavel dependente.

Com amostra desta dimensao o p-valor sozinho diz pouco, entao toda execucao devolve
tambem o tamanho de efeito e o menor p-valor que aquele numero de pares permite
alcancar. Isso evita ler um resultado como nulo quando na verdade ele era inalcancavel.
"""

from dataclasses import dataclass, asdict

import numpy as np
from scipy import stats


@dataclass
class Resultado:
    variavel: str
    alternativa: str
    n_pares: int
    n_pares_uteis: int
    n_empates: int
    estatistica: float | None
    p_valor: float | None
    p_minimo_possivel: float | None
    tamanho_efeito: float | None
    mediana_diferenca: float | None
    conclusivo: bool
    observacao: str

    def como_dicionario(self) -> dict:
        return asdict(self)


def _p_minimo(n_uteis: int, alternativa: str) -> float | None:
    """
    Menor p-valor alcancavel no teste exato com n pares nao empatados.

    Com todos os sinais na mesma direcao, so existe 1 das 2**n configuracoes possiveis
    tao extrema quanto a observada. No teste bilateral, duas.
    """
    if n_uteis < 1:
        return None
    caudas = 1 if alternativa != "two-sided" else 2
    return caudas / (2**n_uteis)


def _rank_biserial(diferencas: np.ndarray) -> float | None:
    """
    Correlacao rank-biserial para amostras pareadas.

    Vai de -1 a +1 e diz o quanto da soma dos postos esta de um lado. Preferida ao
    r = Z/sqrt(N) porque nao depende da aproximacao normal, que nao vale com 4 pares.
    """
    nao_nulas = diferencas[diferencas != 0]
    if nao_nulas.size == 0:
        return None
    postos = stats.rankdata(np.abs(nao_nulas))
    soma_positiva = float(postos[nao_nulas > 0].sum())
    soma_negativa = float(postos[nao_nulas < 0].sum())
    total = soma_positiva + soma_negativa
    if total == 0:
        return None
    return (soma_positiva - soma_negativa) / total


def wilcoxon_pareado(
    diferencas, variavel: str, alternativa: str = "two-sided"
) -> Resultado:
    """
    Executa o teste de Wilcoxon sobre as diferencas COM_IA menos SEM_IA.

    `alternativa` segue a convencao do SciPy: 'less' quando a hipotese alternativa diz
    que o valor com IA e menor, 'greater' quando diz que e maior, 'two-sided' quando a
    pergunta e apenas se ha diferenca.
    """
    valores = np.asarray([d for d in diferencas if d is not None], dtype=float)
    valores = valores[~np.isnan(valores)]

    n_pares = int(valores.size)
    empates = int((valores == 0).sum())
    uteis = int(n_pares - empates)

    base = {
        "variavel": variavel,
        "alternativa": alternativa,
        "n_pares": n_pares,
        "n_pares_uteis": uteis,
        "n_empates": empates,
        "mediana_diferenca": float(np.median(valores)) if n_pares else None,
        "tamanho_efeito": _rank_biserial(valores),
        "p_minimo_possivel": _p_minimo(uteis, alternativa),
    }

    if n_pares < 2:
        return Resultado(
            **base,
            estatistica=None,
            p_valor=None,
            conclusivo=False,
            observacao=f"pares insuficientes para qualquer teste (n={n_pares})",
        )

    if uteis == 0:
        return Resultado(
            **base,
            estatistica=None,
            p_valor=None,
            conclusivo=False,
            observacao="todas as diferencas sao zero; nao ha o que testar",
        )

    if uteis < 2:
        return Resultado(
            **base,
            estatistica=None,
            p_valor=None,
            conclusivo=False,
            observacao=(
                f"apenas {uteis} par nao empatado; o teste exige ao menos 2 "
                "diferencas diferentes de zero"
            ),
        )

    teste = stats.wilcoxon(valores, alternative=alternativa, zero_method="wilcox")
    p_valor = float(teste.pvalue)
    p_minimo = base["p_minimo_possivel"]
    alcancavel = p_minimo is not None and p_minimo <= 0.05

    if not alcancavel:
        observacao = (
            f"com {uteis} pares uteis o menor p-valor possivel e {p_minimo:.4f}; "
            "significancia a 0,05 e inalcancavel por construcao do desenho"
        )
    elif p_valor <= 0.05:
        observacao = "diferenca significativa a 0,05"
    else:
        observacao = "sem evidencia de diferenca a 0,05"

    return Resultado(
        **base,
        estatistica=float(teste.statistic),
        p_valor=p_valor,
        conclusivo=alcancavel,
        observacao=observacao,
    )


def descrever_efeito(tamanho: float | None) -> str:
    """Traduz o tamanho de efeito para uma palavra, pela convencao usual."""
    if tamanho is None:
        return "indefinido"
    absoluto = abs(tamanho)
    if absoluto >= 0.5:
        return "grande"
    if absoluto >= 0.3:
        return "moderado"
    if absoluto >= 0.1:
        return "pequeno"
    return "desprezivel"
