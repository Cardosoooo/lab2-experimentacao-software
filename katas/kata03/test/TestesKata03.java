import java.util.List;

/**
 * Suite de testes de aceitacao da Kata 03, sem JUnit.
 *
 * Imprime uma linha por teste e uma linha de resumo, no formato definido em
 * katas/README.md, e sai com codigo 1 quando algum teste falha.
 */
public class TestesKata03 {

    private static int total = 0;
    private static int passando = 0;
    private static int falhando = 0;

    private static final String TUDO_ZERO =
            "total_investido=0.00;corretagem=0.00;total_gasto=0.00;ordens=0;canceladas=0;invalidas=0";

    public static void main(String[] args) {

        verifica("lista_vazia", TUDO_ZERO, CompraAcoes.consolidar(List.of()));

        verifica("compra_simples_cobra_corretagem_minima",
                "total_investido=1000.00;corretagem=5.00;total_gasto=1005.00;"
                        + "ordens=1;canceladas=0;invalidas=0",
                CompraAcoes.consolidar(List.of("C;PETR4;100;10.00")));

        verifica("total_entre_1000_e_10000_cobra_dez",
                "total_investido=2000.00;corretagem=10.00;total_gasto=2010.00;"
                        + "ordens=1;canceladas=0;invalidas=0",
                CompraAcoes.consolidar(List.of("C;VALE5;200;10.00")));

        verifica("total_exatamente_10000_cobra_dez",
                "total_investido=10000.00;corretagem=10.00;total_gasto=10010.00;"
                        + "ordens=1;canceladas=0;invalidas=0",
                CompraAcoes.consolidar(List.of("C;B3SA3;250;40.00")));

        verifica("total_acima_de_10000_cobra_um_por_cento",
                "total_investido=15000.00;corretagem=150.00;total_gasto=15150.00;"
                        + "ordens=1;canceladas=0;invalidas=0",
                CompraAcoes.consolidar(List.of("C;MGLU3;300;50.00")));

        verifica("varias_compras_sao_somadas",
                "total_investido=2500.00;corretagem=15.00;total_gasto=2515.00;"
                        + "ordens=2;canceladas=0;invalidas=0",
                CompraAcoes.consolidar(List.of(
                        "C;PETR4;100;10.00",
                        "C;PETR4;150;10.00")));

        verifica("cancelamento_anula_todas_as_compras_do_ticket",
                "total_investido=0.00;corretagem=0.00;total_gasto=0.00;"
                        + "ordens=0;canceladas=2;invalidas=0",
                CompraAcoes.consolidar(List.of(
                        "C;PETR4;100;10.00",
                        "C;PETR4;50;20.00",
                        "X;PETR4")));

        verifica("cancelamento_nao_afeta_outros_tickets",
                "total_investido=1000.00;corretagem=5.00;total_gasto=1005.00;"
                        + "ordens=1;canceladas=1;invalidas=0",
                CompraAcoes.consolidar(List.of(
                        "C;PETR4;100;10.00",
                        "C;VALE5;100;10.00",
                        "X;PETR4")));

        verifica("cancelamento_sem_compra_anterior_e_invalido",
                "total_investido=0.00;corretagem=0.00;total_gasto=0.00;"
                        + "ordens=0;canceladas=0;invalidas=1",
                CompraAcoes.consolidar(List.of("X;PETR4")));

        verifica("cancelamento_repetido_e_invalido",
                "total_investido=0.00;corretagem=0.00;total_gasto=0.00;"
                        + "ordens=0;canceladas=1;invalidas=1",
                CompraAcoes.consolidar(List.of(
                        "C;PETR4;100;10.00",
                        "X;PETR4",
                        "X;PETR4")));

        verifica("quantidade_fora_do_lote_e_invalida",
                "total_investido=0.00;corretagem=0.00;total_gasto=0.00;"
                        + "ordens=0;canceladas=0;invalidas=1",
                CompraAcoes.consolidar(List.of("C;PETR4;15;10.00")));

        verifica("quantidade_zero_e_invalida",
                "total_investido=0.00;corretagem=0.00;total_gasto=0.00;"
                        + "ordens=0;canceladas=0;invalidas=1",
                CompraAcoes.consolidar(List.of("C;PETR4;0;10.00")));

        verifica("preco_sem_duas_casas_e_invalido",
                "total_investido=0.00;corretagem=0.00;total_gasto=0.00;"
                        + "ordens=0;canceladas=0;invalidas=3",
                CompraAcoes.consolidar(List.of(
                        "C;PETR4;100;10",
                        "C;PETR4;100;10.000",
                        "C;PETR4;100;0.00")));

        verifica("ticket_invalido_por_caracteres_ou_tamanho",
                "total_investido=0.00;corretagem=0.00;total_gasto=0.00;"
                        + "ordens=0;canceladas=0;invalidas=3",
                CompraAcoes.consolidar(List.of(
                        "C;petr4;100;10.00",
                        "C;A$B;100;10.00",
                        "C;ABCDEFGH;100;10.00")));

        verifica("linhas_malformadas_sao_invalidas",
                "total_investido=0.00;corretagem=0.00;total_gasto=0.00;"
                        + "ordens=0;canceladas=0;invalidas=4",
                CompraAcoes.consolidar(List.of(
                        "V;PETR4;100;10.00",
                        "C;PETR4;100",
                        "C;PETR4",
                        "X")));

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