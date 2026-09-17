import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class FechamentoCaixa {

    private static final Set<String> FORMAS = Set.of("DINHEIRO", "DEBITO", "CREDITO", "PIX");

    public static String fechar(List<String> movimentos) {
        Map<String, Venda> vendasValidas = new LinkedHashMap<>();
        int invalidas = 0;

        // Passo 1: processa vendas (linhas do tipo V)
        for (String linhaRaw : movimentos) {
            String linha = linhaRaw.trim();
            String[] partes = linha.split(";", -1);
            String tipo = partes.length > 0 ? partes[0] : "";

            if (tipo.equals("V")) {
                if (partes.length != 4) {
                    invalidas++;
                    continue;
                }
                String id = partes[1];
                String valorStr = partes[2];
                String forma = partes[3];

                if (id.isEmpty()) {
                    invalidas++;
                    continue;
                }
                if (!valorStr.matches("\\d+\\.\\d{2}")) {
                    invalidas++;
                    continue;
                }
                BigDecimal valor = new BigDecimal(valorStr);
                if (valor.compareTo(BigDecimal.ZERO) <= 0) {
                    invalidas++;
                    continue;
                }
                if (!FORMAS.contains(forma)) {
                    invalidas++;
                    continue;
                }
                if (vendasValidas.containsKey(id)) {
                    invalidas++;
                    continue;
                }
                vendasValidas.put(id, new Venda(valor, forma));
            } else if (tipo.equals("E")) {
                // tratado no passo 2
            } else {
                invalidas++;
            }
        }

        // Passo 2: processa estornos (linhas do tipo E)
        Set<String> estornados = new HashSet<>();
        int estornadasCount = 0;
        for (String linhaRaw : movimentos) {
            String linha = linhaRaw.trim();
            String[] partes = linha.split(";", -1);
            String tipo = partes.length > 0 ? partes[0] : "";

            if (!tipo.equals("E")) {
                continue;
            }
            if (partes.length != 2) {
                invalidas++;
                continue;
            }
            String id = partes[1];
            if (id.isEmpty()) {
                invalidas++;
                continue;
            }
            if (!vendasValidas.containsKey(id)) {
                invalidas++;
                continue;
            }
            if (estornados.contains(id)) {
                invalidas++;
                continue;
            }
            estornados.add(id);
            estornadasCount++;
        }

        // Totais
        BigDecimal bruto = BigDecimal.ZERO;
        BigDecimal taxas = BigDecimal.ZERO;
        int vendasCount = 0;

        for (Map.Entry<String, Venda> entry : vendasValidas.entrySet()) {
            if (estornados.contains(entry.getKey())) {
                continue;
            }
            Venda v = entry.getValue();
            bruto = bruto.add(v.valor);
            BigDecimal taxaPercentual = taxaPara(v.forma);
            BigDecimal taxa = v.valor.multiply(taxaPercentual).setScale(2, RoundingMode.HALF_UP);
            taxas = taxas.add(taxa);
            vendasCount++;
        }

        BigDecimal liquido = bruto.subtract(taxas);

        return String.format(Locale.ROOT,
                "bruto=%s;liquido=%s;taxas=%s;vendas=%d;estornadas=%d;invalidas=%d",
                fmt(bruto), fmt(liquido), fmt(taxas), vendasCount, estornadasCount, invalidas);
    }

    private static String fmt(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP).toString();
    }

    private static BigDecimal taxaPara(String forma) {
        switch (forma) {
            case "DEBITO":
                return new BigDecimal("0.02");
            case "CREDITO":
                return new BigDecimal("0.03");
            default:
                return BigDecimal.ZERO;
        }
    }

    private static class Venda {
        final BigDecimal valor;
        final String forma;

        Venda(BigDecimal valor, String forma) {
            this.valor = valor;
            this.forma = forma;
        }
    }
}