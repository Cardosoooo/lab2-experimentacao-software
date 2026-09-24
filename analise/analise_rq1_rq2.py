"""
Passo 4 do enunciado, parte de tempo e defeitos. Issue #25.

RQ1  O uso de assistente de IA reduz o tempo necessario para resolver a tarefa?
RQ2  O uso de assistente de IA reduz a quantidade de defeitos?
RQ4  O assistente antecipa o primeiro teste verde ou apenas o ultimo?

Rodar a partir da raiz do repositorio:
    python analise/analise_rq1_rq2.py

Escreve as tabelas em data/analise/ para o dashboard consumir, e imprime um relatorio
legivel no terminal.
"""

import pandas as pd

import dados
import estatistica


def revisar_outliers(df: pd.DataFrame) -> pd.DataFrame:
    """
    Revisao de outliers exigida pelo Passo 4.

    Aplica a regra do intervalo interquartil dentro de cada tratamento, mas marca a
    parte os trials censurados. Um trial censurado esta no limite do time-box por
    definicao do protocolo, entao ele nao e um valor anomalo a descartar: e um dado
    valido que diz que a tarefa nao foi concluida no tempo.
    """
    linhas = []
    for tratamento, grupo in df.groupby("tratamento"):
        serie = grupo["tempo_segundos"].dropna()
        q1, q3 = serie.quantile(0.25), serie.quantile(0.75)
        iqr = q3 - q1
        limite_baixo, limite_alto = q1 - 1.5 * iqr, q3 + 1.5 * iqr
        for _, trial in grupo.iterrows():
            valor = trial["tempo_segundos"]
            fora = bool(valor < limite_baixo or valor > limite_alto)
            linhas.append(
                {
                    "trial_id": trial["trial_id"],
                    "tratamento": tratamento,
                    "tempo_segundos": valor,
                    "censurado": bool(trial["censurado"]),
                    "fora_do_intervalo_iqr": fora,
                    "decisao": (
                        "mantido: censura e limite do protocolo, nao anomalia"
                        if trial["censurado"]
                        else ("mantido: revisado e plausivel" if fora else "normal")
                    ),
                }
            )
    return pd.DataFrame(linhas)


def analisar_rq4(df: pd.DataFrame) -> pd.DataFrame:
    """
    RQ4 fica descritiva.

    O tempo ate o primeiro teste verde nao foi medido nos quatro trials do Guilherme,
    entao nenhuma kata tem os dois lados. Sem par nao ha teste. Ver
    docs/desvios-sprint2.md, secao 1.
    """
    medidos = df[df["tempo_primeiro_verde_s"].notna()].copy()
    medidos["proporcao_do_total"] = (
        medidos["tempo_primeiro_verde_s"] / medidos["tempo_segundos"]
    ).round(3)
    return medidos[
        [
            "trial_id",
            "tratamento",
            "tempo_primeiro_verde_s",
            "tempo_segundos",
            "proporcao_do_total",
        ]
    ].sort_values("tratamento")


def imprimir_descritiva(titulo: str, tabela: pd.DataFrame, unidade: str) -> None:
    print(f"\n{titulo}")
    for _, linha in tabela.iterrows():
        if linha["n"] == 0:
            print(f"  {linha['tratamento']}: sem observacoes")
            continue
        print(
            f"  {linha['tratamento']}: mediana {linha['mediana']:.2f} {unidade}"
            f" | IQR {linha['iqr']:.2f}"
            f" | faixa {linha['minimo']:.2f} a {linha['maximo']:.2f}"
            f" | n={int(linha['n'])}"
        )


def imprimir_teste(rotulo: str, resultado: estatistica.Resultado) -> None:
    print(f"\n{rotulo}")
    print(f"  pares completos: {resultado.n_pares}, nao empatados: {resultado.n_pares_uteis}")
    if resultado.mediana_diferenca is not None:
        print(f"  mediana da diferenca (COM_IA menos SEM_IA): {resultado.mediana_diferenca:.2f}")
    if resultado.p_valor is not None:
        print(f"  estatistica W: {resultado.estatistica:.1f} | p-valor: {resultado.p_valor:.4f}")
    if resultado.p_minimo_possivel is not None:
        print(f"  menor p-valor possivel com esses pares: {resultado.p_minimo_possivel:.4f}")
    if resultado.tamanho_efeito is not None:
        rotulo_efeito = estatistica.descrever_efeito(resultado.tamanho_efeito)
        print(f"  tamanho de efeito: {resultado.tamanho_efeito:+.3f} ({rotulo_efeito})")
    print(f"  leitura: {resultado.observacao}")


def main() -> None:
    saida = dados.garantir_saida()
    df = dados.carregar_trials()

    print("=" * 74)
    print("ANALISE RQ1, RQ2 e RQ4 - tempo e defeitos")
    print("=" * 74)
    print(f"\ntrials carregados: {len(df)} ({(df['tratamento'] == 'COM_IA').sum()} com IA,"
          f" {(df['tratamento'] == 'SEM_IA').sum()} sem IA)")
    print(f"trials censurados no time-box: {int(df['censurado'].sum())}")

    # ---------------------------------------------------------------- outliers
    outliers = revisar_outliers(df)
    outliers.to_csv(saida / "revisao_outliers.csv", index=False)
    print("\nRevisao de outliers (tempo)")
    for _, linha in outliers.iterrows():
        marca = "!" if linha["fora_do_intervalo_iqr"] else " "
        print(f" {marca} {linha['trial_id']:<26} {linha['tempo_segundos']:>6.0f}s  {linha['decisao']}")
    print("  Nenhum trial foi removido: o desenho exige manter os censurados.")

    # ------------------------------------------------------------------- RQ1
    print("\n" + "-" * 74)
    print("RQ1 - tempo ate todos os testes passarem")
    print("-" * 74)

    desc_tempo = dados.descritiva(df, "tempo_segundos")
    desc_tempo.to_csv(saida / "descritiva_tempo.csv", index=False)
    imprimir_descritiva("Descritiva por tratamento", desc_tempo, "s")

    pares_tempo = dados.montar_pares(df, "tempo_segundos")
    pares_tempo.to_csv(saida / "pares_tempo.csv", index=False)
    print("\nPares por kata (COM_IA menos SEM_IA)")
    for _, linha in pares_tempo.iterrows():
        print(f"  {linha['kata_id']}: {linha['COM_IA']:>6.0f}s  vs {linha['SEM_IA']:>6.0f}s"
              f"   diferenca {linha['diferenca']:>+7.0f}s")

    rq1 = estatistica.wilcoxon_pareado(
        pares_tempo.loc[pares_tempo["par_completo"], "diferenca"],
        variavel="tempo_segundos",
        alternativa="less",
    )
    imprimir_teste("Wilcoxon pareado, H1: tempo menor com IA", rq1)

    # ------------------------------------------------------------------- RQ2
    print("\n" + "-" * 74)
    print("RQ2 - defeitos, medidos pela taxa de sucesso dos testes")
    print("-" * 74)

    desc_taxa = dados.descritiva(df, "taxa_sucesso")
    desc_taxa.to_csv(saida / "descritiva_taxa_sucesso.csv", index=False)
    imprimir_descritiva("Descritiva por tratamento", desc_taxa, "%")

    pares_taxa = dados.montar_pares(df, "taxa_sucesso")
    pares_taxa.to_csv(saida / "pares_taxa_sucesso.csv", index=False)
    print("\nPares por kata (COM_IA menos SEM_IA)")
    for _, linha in pares_taxa.iterrows():
        if not linha["par_completo"]:
            print(f"  {linha['kata_id']}: par incompleto, um dos lados nao foi medido")
            continue
        print(f"  {linha['kata_id']}: {linha['COM_IA']:>6.2f}% vs {linha['SEM_IA']:>6.2f}%"
              f"   diferenca {linha['diferenca']:>+6.2f}")

    rq2 = estatistica.wilcoxon_pareado(
        pares_taxa.loc[pares_taxa["par_completo"], "diferenca"],
        variavel="taxa_sucesso",
        alternativa="greater",
    )
    imprimir_teste("Wilcoxon pareado, H1: taxa de sucesso maior com IA", rq2)

    # ------------------------------------------------------------------- RQ4
    print("\n" + "-" * 74)
    print("RQ4 - tempo ate o primeiro teste verde (questao propria do grupo)")
    print("-" * 74)

    rq4 = analisar_rq4(df)
    rq4.to_csv(saida / "rq4_primeiro_verde.csv", index=False)
    print(f"\ntrials com a medida: {len(rq4)} de {len(df)}")
    for _, linha in rq4.iterrows():
        print(f"  {linha['trial_id']:<26} {linha['tratamento']:<7}"
              f" primeiro verde {linha['tempo_primeiro_verde_s']:>6.0f}s"
              f" de {linha['tempo_segundos']:>6.0f}s totais"
              f"  ({linha['proporcao_do_total']:.0%} do trial)")
    print("\n  Nenhuma kata tem os dois tratamentos medidos, entao nao existe par e o")
    print("  teste de Wilcoxon nao se aplica. A RQ4 entra no relatorio como descritiva.")

    # ---------------------------------------------------------------- arquivos
    resumo = pd.DataFrame([rq1.como_dicionario(), rq2.como_dicionario()])
    resumo.to_csv(saida / "testes_rq1_rq2.csv", index=False)

    print("\n" + "=" * 74)
    print(f"tabelas gravadas em {saida.relative_to(dados.RAIZ)}")
    print("=" * 74)


if __name__ == "__main__":
    main()
