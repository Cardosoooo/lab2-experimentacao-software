import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FechamentoCaixa {
    private static class LinhaParseada {
        String tipo;
        String id;
        BigDecimal valor;
        String forma;
        boolean invalida = false;
    }

    public static String fechar(List<String> movimentos) {
        List<LinhaParseada> linhas = new ArrayList<>();

        for (String mov : movimentos) {
            String trimmed = mov.trim();
            LinhaParseada linha = new LinhaParseada();

            if (trimmed.isEmpty()) {
                linha.invalida = true;
                linhas.add(linha);
                continue;
            }

            String[] parts = trimmed.split(";", -1);
            linha.tipo = parts[0];

            if ("V".equals(linha.tipo)) {
                if (parts.length != 4) {
                    linha.invalida = true;
                } else {
                    linha.id = parts[1];
                    if (linha.id.isEmpty()) {
                        linha.invalida = true;
                    }
                    
                    String valorStr = parts[2];
    
                    if (!valorStr.matches("^\\d+\\.\\d{2}$")) {
                        linha.invalida = true;
                    } else {
                        try {
                            BigDecimal val = new BigDecimal(valorStr);
                            if (val.compareTo(BigDecimal.ZERO) <= 0) {
                                linha.invalida = true;
                            }
                            linha.valor = val;
                        } catch (NumberFormatException e) {
                            linha.invalida = true;
                        }
                    }

                    linha.forma = parts[3];
                    if (!"DINHEIRO".equals(linha.forma) && !"DEBITO".equals(linha.forma) &&
                        !"CREDITO".equals(linha.forma) && !"PIX".equals(linha.forma)) {
                        linha.invalida = true;
                    }
                }
            } else if ("E".equals(linha.tipo)) {
                if (parts.length != 2) {
                    linha.invalida = true;
                } else {
                    linha.id = parts[1];
                    if (linha.id.isEmpty()) {
                        linha.invalida = true;
                    }
                }
            } else {
                linha.invalida = true;
            }

            linhas.add(linha);
        }

        Map<String, LinhaParseada> vendasValidas = new HashMap<>();
        for (LinhaParseada linha : linhas) {
            if (linha.invalida) continue;

            if ("V".equals(linha.tipo)) {
                if (vendasValidas.containsKey(linha.id)) {
                    linha.invalida = true;
                } else {
                    vendasValidas.put(linha.id, linha);
                }
            }
        }

        Set<String> estornadas = new HashSet<>();
        for (LinhaParseada linha : linhas) {
            if (linha.invalida) continue;

            if ("E".equals(linha.tipo)) {
                if (!vendasValidas.containsKey(linha.id)) {
                    linha.invalida = true; 
                } else if (estornadas.contains(linha.id)) {
                    linha.invalida = true;
                } else {
                    estornadas.add(linha.id);
                }
            }
        }

        int vendasCount = 0;
        int invalidasCount = 0;
        BigDecimal bruto = BigDecimal.ZERO;
        BigDecimal taxas = BigDecimal.ZERO;

        for (LinhaParseada linha : linhas) {
            if (linha.invalida) {
                invalidasCount++;
            }
        }

        for (Map.Entry<String, LinhaParseada> entry : vendasValidas.entrySet()) {
            String id = entry.getKey();
            LinhaParseada venda = entry.getValue();

            if (!estornadas.contains(id)) {
                vendasCount++;
                bruto = bruto.add(venda.valor);
                taxas = taxas.add(calcularTaxa(venda.valor, venda.forma));
            }
        }

        BigDecimal liquido = bruto.subtract(taxas);

        return "bruto=" + formatarBD(bruto) +
               ";liquido=" + formatarBD(liquido) +
               ";taxas=" + formatarBD(taxas) +
               ";vendas=" + vendasCount +
               ";estornadas=" + estornadas.size() +
               ";invalidas=" + invalidasCount;
    }

    private static BigDecimal calcularTaxa(BigDecimal valor, String forma) {
        BigDecimal taxa = BigDecimal.ZERO;
        if ("DEBITO".equals(forma)) {
            taxa = new BigDecimal("0.02");
        } else if ("CREDITO".equals(forma)) {
            taxa = new BigDecimal("0.03");
        }
        return valor.multiply(taxa).setScale(2, RoundingMode.HALF_UP);
    }

    private static String formatarBD(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}