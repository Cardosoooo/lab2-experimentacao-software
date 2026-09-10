/**
 * Suite de testes de aceitacao da Kata 04, sem JUnit.
 *
 * Imprime uma linha por teste e uma linha de resumo, no formato definido em
 * katas/README.md, e sai com codigo 1 quando algum teste falha.
 */
public class TestesKata04 {

    private static int total = 0;
    private static int passando = 0;
    private static int falhando = 0;

    public static void main(String[] args) {

        verifica("consumo_50_verde_fica_na_primeira_faixa", "20.00",
                ContaEnergia.calcular("50", "VERDE"));

        verifica("consumo_100_verde_fim_da_primeira_faixa", "40.00",
                ContaEnergia.calcular("100", "VERDE"));

        verifica("consumo_101_verde_inicia_a_segunda_faixa", "40.55",
                ContaEnergia.calcular("101", "VERDE"));

        verifica("consumo_200_verde_fim_da_segunda_faixa", "95.00",
                ContaEnergia.calcular("200", "VERDE"));

        verifica("consumo_201_verde_inicia_a_terceira_faixa", "95.75",
                ContaEnergia.calcular("201", "VERDE"));

        verifica("consumo_250_verde_mistura_as_tres_faixas", "132.50",
                ContaEnergia.calcular("250", "VERDE"));

        verifica("consumo_zero_verde_nao_cobra_energia", "0.00",
                ContaEnergia.calcular("0", "VERDE"));

        verifica("consumo_301_verde_aplica_desconto", "162.21",
                ContaEnergia.calcular("301", "VERDE"));

        verifica("amarela_soma_dois_centavos_por_kwh", "137.50",
                ContaEnergia.calcular("250", "AMARELA"));

        verifica("vermelha1_soma_quatro_centavos_por_kwh", "142.50",
                ContaEnergia.calcular("250", "VERMELHA1"));

        verifica("vermelha2_soma_seis_centavos_por_kwh", "147.50",
                ContaEnergia.calcular("250", "VERMELHA2"));

        verifica("consumo_350_vermelha2_aplica_desconto", "217.08",
                ContaEnergia.calcular("350", "VERMELHA2"));

        verifica("bandeira_desconhecida_retorna_erro", "ERRO",
                ContaEnergia.calcular("100", "AZUL"));

        verifica("bandeira_em_minusculas_e_invalida", "ERRO",
                ContaEnergia.calcular("100", "verde"));

        verifica("consumo_negativo_retorna_erro", "ERRO",
                ContaEnergia.calcular("-10", "VERDE"));

        verifica("consumo_nao_numerico_retorna_erro", "ERRO",
                ContaEnergia.calcular("12,5", "VERDE"));

        System.out.println("RESUMO;total=" + total + ";passando=" + passando + ";falhando=" + falhando);
        if (falhando > 0) {
            System.exit(1);
        }
    }

    private static void verifica(String nome, String esperado, String obtido) {
        total++;
        if (esperado.equals(obtido)) {
            passando++;
            System.out.println("TESTE;" + nome + ";PASSOU");
        } else {
            falhando++;
            System.out.println("TESTE;" + nome + ";FALHOU;esperado=" + esperado
                    + ";obtido=" + String.valueOf(obtido));
        }
    }
}