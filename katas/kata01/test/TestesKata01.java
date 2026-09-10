import java.util.List;

/**
 * Suite de testes de aceitacao da Kata 01, sem JUnit.
 *
 * Imprime uma linha por teste e uma linha de resumo, no formato definido em
 * katas/README.md, e sai com codigo 1 quando algum teste falha.
 */
public class TestesKata01 {

    private static int total = 0;
    private static int passando = 0;
    private static int falhando = 0;

    public static void main(String[] args) {

        verifica("lista_vazia",
                "bruto=0.00;liquido=0.00;taxas=0.00;vendas=0;estornadas=0;invalidas=0",
                FechamentoCaixa.fechar(List.of()));

        verifica("dinheiro_nao_tem_taxa",
                "bruto=100.00;liquido=100.00;taxas=0.00;vendas=1;estornadas=0;invalidas=0",
                FechamentoCaixa.fechar(List.of("V;A1;100.00;DINHEIRO")));

        verifica("pix_nao_tem_taxa",
                "bruto=50.00;liquido=50.00;taxas=0.00;vendas=1;estornadas=0;invalidas=0",
                FechamentoCaixa.fechar(List.of("V;A1;50.00;PIX")));

        verifica("credito_desconta_tres_por_cento",
                "bruto=100.00;liquido=97.00;taxas=3.00;vendas=1;estornadas=0;invalidas=0",
                FechamentoCaixa.fechar(List.of("V;A1;100.00;CREDITO")));

        verifica("debito_desconta_dois_por_cento",
                "bruto=10.00;liquido=9.80;taxas=0.20;vendas=1;estornadas=0;invalidas=0",
                FechamentoCaixa.fechar(List.of("V;A1;10.00;DEBITO")));

        verifica("taxa_arredonda_para_duas_casas",
                "bruto=33.33;liquido=32.33;taxas=1.00;vendas=1;estornadas=0;invalidas=0",
                FechamentoCaixa.fechar(List.of("V;A1;33.33;CREDITO")));

        verifica("estorno_cancela_a_venda",
                "bruto=0.00;liquido=0.00;taxas=0.00;vendas=0;estornadas=1;invalidas=0",
                FechamentoCaixa.fechar(List.of("V;A1;100.00;PIX", "E;A1")));

        verifica("estorno_antes_da_venda_tambem_vale",
                "bruto=0.00;liquido=0.00;taxas=0.00;vendas=0;estornadas=1;invalidas=0",
                FechamentoCaixa.fechar(List.of("E;A1", "V;A1;100.00;PIX")));

        verifica("estorno_de_venda_inexistente_e_invalido",
                "bruto=0.00;liquido=0.00;taxas=0.00;vendas=0;estornadas=0;invalidas=1",
                FechamentoCaixa.fechar(List.of("E;X9")));

        verifica("estorno_repetido_e_invalido",
                "bruto=0.00;liquido=0.00;taxas=0.00;vendas=0;estornadas=1;invalidas=1",
                FechamentoCaixa.fechar(List.of("V;A1;10.00;PIX", "E;A1", "E;A1")));

        verifica("venda_com_id_duplicado_e_invalida",
                "bruto=10.00;liquido=10.00;taxas=0.00;vendas=1;estornadas=0;invalidas=1",
                FechamentoCaixa.fechar(List.of("V;A1;10.00;PIX", "V;A1;20.00;PIX")));

        verifica("linhas_malformadas_sao_invalidas",
                "bruto=0.00;liquido=0.00;taxas=0.00;vendas=0;estornadas=0;invalidas=5",
                FechamentoCaixa.fechar(List.of(
                        "V;A1;abc;PIX",
                        "V;A2;10.00;BOLETO",
                        "X;A3;10.00;PIX",
                        "V;A4;10.00",
                        "V;A5;0.00;PIX")));

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
