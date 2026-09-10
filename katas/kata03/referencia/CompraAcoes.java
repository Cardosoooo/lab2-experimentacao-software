import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Kata 03 - Compra Fracionada de Acoes. Solucao de referencia.
 *
 * Usada apenas para calibrar a dificuldade da kata antes do experimento.
 * Nao deve ser consultada durante um trial.
 */
public class CompraAcoes {

    /** Compra valida: total pago pela ordem (quantidade x preco). */
    private static final class Ordem {
        final BigDecimal total;

        Ordem(BigDecimal total) {
            this.total = total;
        }
    }

    public static String consolidar(List<String> linhas) {
        Map<String, List<Ordem>> compras = new LinkedHashMap<>();
        int invalidas = 0;
        int canceladas = 0;

        for (String bruto : linhas) {
            String linha = bruto == null ? "" : bruto.trim();
            String[] campos = linha.split(";", -1);

            if (campos.length == 4 && "C".equals(campos[0])) {
                BigDecimal totalCompra = parseTotal(campos[1], campos[2], campos[3]);
                if (totalCompra != null) {
                    compras.computeIfAbsent(campos[1], chave -> new ArrayList<>())
                            .add(new Ordem(totalCompra));
                } else {
                    invalidas++;
                }
            } else if (campos.length == 2 && "X".equals(campos[0])) {
                if (compras.containsKey(campos[1])) {
                    canceladas += compras.remove(campos[1]).size();
                } else {
                    invalidas++;
                }
            } else {
                invalidas++;
            }
        }

        BigDecimal totalInvestido = BigDecimal.ZERO;
        BigDecimal totalCorretagem = BigDecimal.ZERO;
        int ordens = 0;

        for (List<Ordem> lista : compras.values()) {
            for (Ordem ordem : lista) {
                totalInvestido = totalInvestido.add(ordem.total);
                totalCorretagem = totalCorretagem.add(corretagem(ordem.total));
                ordens++;
            }
        }

        BigDecimal totalGasto = totalInvestido.add(totalCorretagem);

        return "total_investido=" + moeda(totalInvestido)
                + ";corretagem=" + moeda(totalCorretagem)
                + ";total_gasto=" + moeda(totalGasto)
                + ";ordens=" + ordens
                + ";canceladas=" + canceladas
                + ";invalidas=" + invalidas;
    }

    /** Total da compra, ou null quando ticket, quantidade ou preco sao invalidos. */
    private static BigDecimal parseTotal(String ticket, String textoQuantidade, String textoPreco) {
        if (!ticket.matches("[A-Z0-9]{1,6}")) {
            return null;
        }
        Long quantidade;
        try {
            quantidade = Long.parseLong(textoQuantidade);
        } catch (NumberFormatException excecao) {
            return null;
        }
        if (quantidade <= 0 || quantidade % 10 != 0 || !textoPreco.matches("\\d+\\.\\d{2}")) {
            return null;
        }
        BigDecimal preco = new BigDecimal(textoPreco);
        if (preco.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return preco.multiply(BigDecimal.valueOf(quantidade));
    }

    private static BigDecimal corretagem(BigDecimal total) {
        if (total.compareTo(new BigDecimal("1000.00")) <= 0) {
            return new BigDecimal("5.00");
        }
        if (total.compareTo(new BigDecimal("10000.00")) <= 0) {
            return new BigDecimal("10.00");
        }
        return total.multiply(new BigDecimal("0.01")).setScale(2, RoundingMode.HALF_UP);
    }

    private static String moeda(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}