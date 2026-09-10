/**
 * Suite de testes de aceitacao da Kata 02, sem JUnit.
 *
 * Imprime uma linha por teste e uma linha de resumo, no formato definido em
 * katas/README.md, e sai com codigo 1 quando algum teste falha.
 */
public class TestesKata02 {

    private static int total = 0;
    private static int passando = 0;
    private static int falhando = 0;

    public static void main(String[] args) {

        verifica("mensalista_nao_paga", "0.00",
                Estacionamento.cobrar("2026-03-10 08:00", "2026-03-10 12:00", true));

        verifica("dentro_da_tolerancia", "0.00",
                Estacionamento.cobrar("2026-03-10 08:00", "2026-03-10 08:10", false));

        verifica("tolerancia_no_limite_exato", "0.00",
                Estacionamento.cobrar("2026-03-10 08:00", "2026-03-10 08:15", false));

        verifica("um_minuto_apos_a_tolerancia_cobra_primeira_hora", "10.00",
                Estacionamento.cobrar("2026-03-10 08:00", "2026-03-10 08:16", false));

        verifica("primeira_hora_cheia", "10.00",
                Estacionamento.cobrar("2026-03-10 08:00", "2026-03-10 09:00", false));

        verifica("fracao_da_segunda_hora_cobra_hora_inteira", "15.00",
                Estacionamento.cobrar("2026-03-10 08:00", "2026-03-10 09:01", false));

        verifica("tres_horas", "20.00",
                Estacionamento.cobrar("2026-03-10 08:00", "2026-03-10 11:00", false));

        verifica("teto_diario_limita_o_valor", "40.00",
                Estacionamento.cobrar("2026-03-10 08:00", "2026-03-10 18:00", false));

        verifica("dois_periodos_de_24h_dobram_o_teto", "80.00",
                Estacionamento.cobrar("2026-03-10 08:00", "2026-03-11 14:00", false));

        verifica("noturno_atravessando_a_meia_noite", "10.50",
                Estacionamento.cobrar("2026-03-10 23:00", "2026-03-11 01:00", false));

        verifica("noturno_de_madrugada", "14.00",
                Estacionamento.cobrar("2026-03-10 02:00", "2026-03-10 05:00", false));

        verifica("saida_apos_as_seis_perde_o_desconto_noturno", "40.00",
                Estacionamento.cobrar("2026-03-10 23:00", "2026-03-11 07:00", false));

        verifica("saida_anterior_a_entrada", "ERRO",
                Estacionamento.cobrar("2026-03-10 12:00", "2026-03-10 10:00", false));

        verifica("formato_de_data_invalido", "ERRO",
                Estacionamento.cobrar("10/03/2026 08:00", "2026-03-10 10:00", false));

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
