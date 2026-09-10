import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Kata 01 - Fechamento de Caixa. Solucao de referencia.
 *
 * Usada apenas para calibrar a dificuldade da kata antes do experimento.
 * Nao deve ser consultada durante um trial.
 */
public class FechamentoCaixa {

    private static final Set<String> FORMAS = new LinkedHashSet<>(
            List.of("DINHEIRO", "DEBITO", "CREDITO", "PIX"));

    /** Venda valida ja normalizada. */
    private static final class Venda {
        final BigDecimal valor;
        final String forma;

        Venda(BigDecimal valor, String forma) {
            this.valor = valor;
            this.forma = forma;
        }
    }

    public static String fechar(List<String> movimentos) {
        Map<String, Venda> vendas = new LinkedHashMap<>();
        Set<String> estornados = new LinkedHashSet<>();
        List<String> estornos = new ArrayList<>();
        int invalidas = 0;

        // Primeira passagem: registra vendas validas e guarda os estornos para depois,
        // porque o estorno pode aparecer antes da venda correspondente.
        for (String bruto : movimentos) {
            String linha = bruto == null ? "" : bruto.trim();
            String[] campos = linha.split(";", -1);

            if (campos.length == 4 && "V".equals(campos[0])) {
                String id = campos[1];
                BigDecimal valor = parseValor(campos[2]);
                String forma = campos[3];
                boolean valida = !id.isEmpty()
                        && valor != null
                        && valor.compareTo(BigDecimal.ZERO) > 0
                        && FORMAS.contains(forma)
                        && !vendas.containsKey(id);
                if (valida) {
                    vendas.put(id, new Venda(valor, forma));
                } else {
                    invalidas++;
                }
            } else if (campos.length == 2 && "E".equals(campos[0])) {
                estornos.add(campos[1]);
            } else {
                invalidas++;
            }
        }

        // Segunda passagem: aplica os estornos sobre as vendas ja conhecidas.
        for (String id : estornos) {
            if (id.isEmpty() || !vendas.containsKey(id) || estornados.contains(id)) {
                invalidas++;
            } else {
                estornados.add(id);
            }
        }

        BigDecimal totalBruto = BigDecimal.ZERO;
        BigDecimal totalTaxas = BigDecimal.ZERO;
        int quantidadeVendas = 0;

        for (Map.Entry<String, Venda> entrada : vendas.entrySet()) {
            if (estornados.contains(entrada.getKey())) {
                continue;
            }
            Venda venda = entrada.getValue();
            totalBruto = totalBruto.add(venda.valor);
            totalTaxas = totalTaxas.add(taxa(venda));
            quantidadeVendas++;
        }

        BigDecimal liquido = totalBruto.subtract(totalTaxas);

        return "bruto=" + moeda(totalBruto)
                + ";liquido=" + moeda(liquido)
                + ";taxas=" + moeda(totalTaxas)
                + ";vendas=" + quantidadeVendas
                + ";estornadas=" + estornados.size()
                + ";invalidas=" + invalidas;
    }

    /** Aceita apenas decimal com exatamente duas casas, sem sinal. */
    private static BigDecimal parseValor(String texto) {
        if (!texto.matches("\\d+\\.\\d{2}")) {
            return null;
        }
        return new BigDecimal(texto);
    }

    private static BigDecimal taxa(Venda venda) {
        BigDecimal percentual;
        switch (venda.forma) {
            case "CREDITO":
                percentual = new BigDecimal("0.03");
                break;
            case "DEBITO":
                percentual = new BigDecimal("0.02");
                break;
            default:
                percentual = BigDecimal.ZERO;
                break;
        }
        return venda.valor.multiply(percentual).setScale(2, RoundingMode.HALF_UP);
    }

    private static String moeda(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
