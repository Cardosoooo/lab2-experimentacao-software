"""
Gera o arquivo unico de dados para um dashboard de apresentacao.

Consolida todas as tabelas de data/ e data/analise/ em um JSON autocontido que pode
ser enviado inteiro a uma IA externa, sem acesso ao repositorio. Os numeros vêm dos
mesmos CSVs que o relatorio final usa, nao sao recalculados aqui.

Rodar a partir da raiz do repositorio:
    python analise/exportar_dashboard.py

Escreve data/analise/dados_dashboard.json. Os valores flutuantes sao arredondados a
4 casas para evitar vieses de representacao binaria (ex.: -0.01880000000000001).
"""

import json
import math
from pathlib import Path

import pandas as pd

import dados

SAIDA = dados.SAIDA_DIR / "dados_dashboard.json"

PARES_DESCRITIVAS = [
    ("tempo", "tempo_segundos"),
    ("taxa_sucesso", "taxa_sucesso"),
    ("cc_media_por_metodo", "cc_media_por_metodo"),
    ("pct_duplicacao_cpd", "pct_duplicacao_cpd"),
    ("loc_total", "loc_total"),
    ("cc_por_loc", "cc_por_loc"),
]

META = {
    "titulo": "Assistentes de IA vs. Codificacao Manual",
    "laboratorio": "Laboratorio 02 de Engenharia de Software - PUC Minas",
    "integrantes": ["Gabriel Cardoso", "Guilherme Brina Ferreira"],
    "repositorio": "Cardosoooo/lab2-experimentacao-software",
    "desenho": "crossover within-subject, bloqueio por kata, time-box",
    "tratamentos": ["COM_IA", "SEM_IA"],
    "sujeitos": 2,
    "katas": 4,
    "trials": 8,
    "trials_por_tratamento": 4,
    "alpha": 0.05,
    "time_box_segundos": dados.TIME_BOX_SEGUNDOS,
    "assistente": "Claude, versao gratuita, por conversa (somente COM_IA)",
    "aviso_poder": (
        "Com 4 pares o menor p-valor alcancavel no Wilcoxon e 0,0625 (unilateral) e "
        "0,125 (bilateral); portanto nenhum resultado deste experimento e significante "
        "a 0,05 por construcao do desenho, nao por caracteristica dos dados. "
        "Os graficos devem transmitir tamanho de efeito e direcao."
    ),
}

QUESTOES = [
    {"id": "RQ1", "pergunta": "O uso de assistente de IA reduz o tempo necessario para resolver uma tarefa de programacao?"},
    {"id": "RQ2", "pergunta": "O uso de assistente de IA reduz a quantidade de defeitos no codigo produzido?"},
    {"id": "RQ3", "pergunta": "O uso de assistente de IA altera a complexidade ciclomatica ou a duplicacao do codigo produzido?"},
    {"id": "RQ4", "pergunta": "O assistente antecipa o primeiro teste verde ou apenas o ultimo?"},
    {"id": "RQ5", "pergunta": "A diferenca de complexidade se mantem apos normalizar por linhas de codigo?"},
]

NOTAS_VISUALIZACAO = [
    "Dois trials sao censurados: tempo_segundos = 2100 com censurado = true. Marque com hachura ou anotacao, senao parecem tempos reais.",
    "A RQ2 nao tem teste: 7 dos 8 trials terminaram com todos os testes passando. Um grafico da taxa de sucesso vai parecer vazio, e e esse o resultado: efeito de teto.",
    "A duplicacao e zero nos oito trials. Nao rende grafico comparativo; vale uma nota de texto em vez de um eixo achatado.",
    "A RQ4 tem so quatro pontos, todos do mesma sujeito (Gabriel). Nao da para comparar tratamentos pareados, apenas mostrar os quatro valores.",
    "A escala do tempo e muito desigual, de 57 a 2100 segundos. Escala logaritmica ou eixo quebrado ajuda a nao esmagar o grupo com IA.",
    "Nenhum resultado e conclusivo a 0,05 (veja meta.aviso_poder). Mostre tamanho de efeito e direcao, nao um carimbo de significante ou nao significante.",
    "Tempo ate 1o teste verde so foi medido em 4 trials; nos 4 do outro sujeito a medida foi perdida.",
    "O numero de prompts (n_prompts) e variavel exploratoria: 1, 2, 1 e 1 nos trials COM_IA; nao entra em teste de hipotese.",
]


def _jsonamigo(tabela: pd.DataFrame) -> list:
    """Converte um DataFrame em registros JSON puro (None, bool, int, float)."""
    registros = []
    for _, linha in tabela.iterrows():
        registro = {}
        for coluna, valor in linha.items():
            if valor is None:
                registro[coluna] = None
            elif isinstance(valor, bool):
                registro[coluna] = valor
            elif isinstance(valor, int):
                registro[coluna] = int(valor)
            elif isinstance(valor, float):
                if math.isnan(valor):
                    registro[coluna] = None
                else:
                    registro[coluna] = round(valor, 4)
            elif hasattr(valor, "item"):
                item = valor.item()
                if isinstance(item, bool):
                    registro[coluna] = item
                elif isinstance(item, int):
                    registro[coluna] = item
                else:
                    registro[coluna] = round(float(item), 4) if not math.isnan(item) else None
            elif isinstance(valor, str):
                registro[coluna] = valor
            else:
                registro[coluna] = str(valor)
        registros.append(registro)
    return registros


def montar_trials() -> list:
    colunas = [
        "trial_id",
        "sujeito",
        "kata_id",
        "tratamento",
        "ordem",
        "tempo_segundos",
        "censurado",
        "tempo_primeiro_verde_s",
        "testes_total",
        "testes_passando",
        "taxa_sucesso",
        "n_prompts",
        "commit_final",
    ]
    trials = dados.carregar_trials()
    return _jsonamigo(trials[colunas])


def montar_metricas() -> list:
    metricas = pd.read_csv(dados.SAIDA_DIR / "metricas_consolidadas.csv")
    return _jsonamigo(metricas)


def montar_leque(prefixo: str) -> dict:
    """Une as tabelas descritiva_<v>.csv ou pares_<v>.csv em um dict por variavel."""
    resultado = {}
    for sufixo, chave in PARES_DESCRITIVAS:
        arquivo = dados.SAIDA_DIR / f"{prefixo}{sufixo}.csv"
        tabela = pd.read_csv(arquivo)
        resultado[chave] = _jsonamigo(tabela)
    return resultado


def montar_testes() -> list:
    blocos = [
        ("tempo e defeitos", dados.SAIDA_DIR / "testes_rq1_rq2.csv"),
        ("estrutura e tamanho", dados.SAIDA_DIR / "testes_rq3_rq5.csv"),
    ]
    registros = []
    for grupo, caminho in blocos:
        for registro in _jsonamigo(pd.read_csv(caminho)):
            registro["grupo"] = grupo
            registros.append(registro)
    return registros


def main() -> None:
    dados.garantir_saida()

    payload = {
        "meta": META,
        "questoes_de_pesquisa": QUESTOES,
        "trials": montar_trials(),
        "metricas": montar_metricas(),
        "descritivas": montar_leque("descritiva_"),
        "pares": montar_leque("pares_"),
        "testes_wilcoxon": montar_testes(),
        "outliers": _jsonamigo(pd.read_csv(dados.SAIDA_DIR / "revisao_outliers.csv")),
        "rq4_primeiro_verde": _jsonamigo(pd.read_csv(dados.SAIDA_DIR / "rq4_primeiro_verde.csv")),
        "notas_para_visualizacao": NOTAS_VISUALIZACAO,
    }

    with SAIDA.open("w", encoding="utf-8") as arquivo:
        json.dump(payload, arquivo, ensure_ascii=False, indent=2)

    print(f"dados do dashboard gravados em {SAIDA.relative_to(dados.RAIZ)}")
    print(f"  trials: {len(payload['trials'])}")
    print(f"  metricas: {len(payload['metricas'])}")
    print(f"  descritivas: {sorted(payload['descritivas'])}")
    print(f"  pares: {sorted(payload['pares'])}")
    print(f"  testes wilcoxon: {len(payload['testes_wilcoxon'])}")


if __name__ == "__main__":
    main()