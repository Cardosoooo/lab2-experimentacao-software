"""
Passo 4 do enunciado, parte de estrutura do codigo. Issue #26.

RQ3  O uso de assistente de IA altera a complexidade ciclomatica ou a duplicacao?
RQ5  A diferenca de complexidade se mantem apos normalizar por linhas de codigo?

Rodar a partir da raiz do repositorio:
    python analise/analise_rq3_rq5.py

As duas questoes sao bilaterais: o enunciado pergunta se a IA *altera* a estrutura,
sem direcao esperada. LOC entra como variavel de controle obrigatoria, porque codigo
gerado por IA tende a ser mais verboso e complexidade sem normalizar por tamanho pode
indicar apenas que o arquivo e maior.
"""

import pandas as pd

import dados
import estatistica

# Variaveis analisadas: rotulo legivel, coluna e unidade.
VARIAVEIS = [
    ("Complexidade ciclomatica media por metodo", "cc_media_por_metodo", ""),
    ("Percentual de linhas duplicadas", "pct_duplicacao_cpd", "%"),
    ("Linhas de codigo (controle)", "loc_total", " linhas"),
    ("Complexidade por linha de codigo (RQ5)", "cc_por_loc", ""),
]


def montar_tabela() -> pd.DataFrame:
    """
    Junta trials e metricas e deriva a razao usada na RQ5.

    A complexidade por linha e a complexidade total do trial dividida pelas linhas de
    codigo do mesmo trial, ambas vindas do CK. E essa razao que separa 'o codigo ficou
    mais complexo' de 'o codigo apenas ficou maior'.
    """
    df = dados.carregar_completo()
    df["cc_por_loc"] = (df["cc_total"] / df["loc_total"]).round(4)
    return df


def imprimir_variavel(rotulo: str, coluna: str, unidade: str, df: pd.DataFrame, saida) -> dict:
    print("\n" + "-" * 74)
    print(rotulo)
    print("-" * 74)

    desc = dados.descritiva(df, coluna)
    desc.to_csv(saida / f"descritiva_{coluna}.csv", index=False)
    print("\nDescritiva por tratamento")
    for _, linha in desc.iterrows():
        if linha["n"] == 0:
            print(f"  {linha['tratamento']}: sem observacoes")
            continue
        print(
            f"  {linha['tratamento']}: mediana {linha['mediana']:.3f}{unidade}"
            f" | IQR {linha['iqr']:.3f}"
            f" | faixa {linha['minimo']:.3f} a {linha['maximo']:.3f}"
            f" | n={int(linha['n'])}"
        )

    pares = dados.montar_pares(df, coluna)
    pares.to_csv(saida / f"pares_{coluna}.csv", index=False)
    print("\nPares por kata (COM_IA menos SEM_IA)")
    for _, linha in pares.iterrows():
        if not linha["par_completo"]:
            print(f"  {linha['kata_id']}: par incompleto")
            continue
        print(
            f"  {linha['kata_id']}: {linha['COM_IA']:>8.3f} vs {linha['SEM_IA']:>8.3f}"
            f"   diferenca {linha['diferenca']:>+9.3f}"
        )

    resultado = estatistica.wilcoxon_pareado(
        pares.loc[pares["par_completo"], "diferenca"],
        variavel=coluna,
        alternativa="two-sided",
    )

    print("\nWilcoxon pareado bilateral")
    print(f"  pares completos: {resultado.n_pares}, nao empatados: {resultado.n_pares_uteis}")
    if resultado.mediana_diferenca is not None:
        print(f"  mediana da diferenca: {resultado.mediana_diferenca:+.3f}")
    if resultado.p_valor is not None:
        print(f"  estatistica W: {resultado.estatistica:.1f} | p-valor: {resultado.p_valor:.4f}")
    if resultado.p_minimo_possivel is not None:
        print(f"  menor p-valor possivel: {resultado.p_minimo_possivel:.4f}")
    if resultado.tamanho_efeito is not None:
        print(
            f"  tamanho de efeito: {resultado.tamanho_efeito:+.3f}"
            f" ({estatistica.descrever_efeito(resultado.tamanho_efeito)})"
        )
    print(f"  leitura: {resultado.observacao}")

    return resultado.como_dicionario()


def main() -> None:
    saida = dados.garantir_saida()
    df = montar_tabela()

    print("=" * 74)
    print("ANALISE RQ3 e RQ5 - estrutura do codigo produzido")
    print("=" * 74)

    consolidado = df[
        [
            "trial_id",
            "sujeito",
            "kata_id",
            "tratamento",
            "loc_total",
            "n_metodos",
            "cc_total",
            "cc_media_por_metodo",
            "cc_por_loc",
            "linhas_duplicadas_cpd",
            "pct_duplicacao_cpd",
            "min_tokens_cpd",
        ]
    ].sort_values(["kata_id", "tratamento"])
    consolidado.to_csv(saida / "metricas_consolidadas.csv", index=False)

    print("\nMetricas consolidadas dos 8 trials")
    print(consolidado.to_string(index=False))

    resultados = []
    for rotulo, coluna, unidade in VARIAVEIS:
        resultados.append(imprimir_variavel(rotulo, coluna, unidade, df, saida))

    pd.DataFrame(resultados).to_csv(saida / "testes_rq3_rq5.csv", index=False)

    print("\n" + "=" * 74)
    print(f"tabelas gravadas em {saida.relative_to(dados.RAIZ)}")
    print("=" * 74)


if __name__ == "__main__":
    main()
