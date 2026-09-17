import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompraAcoes {

    private static final BigDecimal LIMITE_FAIXA_1 = new BigDecimal("1000.00");
    private static final BigDecimal LIMITE_FAIXA_2 = new BigDecimal("10000.00");
    private static final BigDecimal CORRETAGEM_FAIXA_1 = new BigDecimal("5.00");
    private static final BigDecimal CORRETAGEM_FAIXA_2 = new BigDecimal("10.00");
    private static final BigDecimal PERCENTUAL_FAIXA_3 = new BigDecimal("0.01");

    private static class Compra {
        final BigDecimal total;
        final BigDecimal corretagem;

        Compra(BigDecimal total, BigDecimal corretagem) {
            this.total = total;
            this.corretagem = corretagem;
        }
    }

    public static String consolidar(List<String> linhas) {
        BigDecimal totalInvestido = BigDecimal.ZERO;
        BigDecimal totalCorretagem = BigDecimal.ZERO;
        int ordens = 0;
        int canceladas = 0;
        int invalidas = 0;

        Map<String, List<Compra>> compraisAtivasPorTicket = new HashMap<>();

        for (String linhaOriginal : linhas) {
            String linha = linhaOriginal.trim();
            String[] campos = linha.split(";", -1);
            String tipo = campos[0];

            if (tipo.equals("C")) {
                if (campos.length != 4) {
                    invalidas++;
                    continue;
                }

                String ticket = campos[1];
                Long quantidade = parseQuantidade(campos[2]);
                BigDecimal preco = parsePreco(campos[3]);

                if (!ticketValido(ticket) || quantidade == null || preco == null) {
                    invalidas++;
                    continue;
                }

                BigDecimal total = arredondar(preco.multiply(BigDecimal.valueOf(quantidade)));
                BigDecimal corretagem = calcularCorretagem(total);

                ordens++;
                totalInvestido = totalInvestido.add(total);
                totalCorretagem = totalCorretagem.add(corretagem);

                compraisAtivasPorTicket
                        .computeIfAbsent(ticket, k -> new ArrayList<>())
                        .add(new Compra(total, corretagem));

            } else if (tipo.equals("X")) {
                if (campos.length != 2) {
                    invalidas++;
                    continue;
                }

                String ticket = campos[1];
                List<Compra> ativas = compraisAtivasPorTicket.get(ticket);

                if (!ticketValido(ticket) || ativas == null || ativas.isEmpty()) {
                    invalidas++;
                    continue;
                }

                for (Compra compra : ativas) {
                    totalInvestido = totalInvestido.subtract(compra.total);
                    totalCorretagem = totalCorretagem.subtract(compra.corretagem);
                    ordens--;
                    canceladas++;
                }
                ativas.clear();

            } else {
                invalidas++;
            }
        }

        BigDecimal totalGasto = totalInvestido.add(totalCorretagem);

        return "total_investido=" + formatar(totalInvestido)
                + ";corretagem=" + formatar(totalCorretagem)
                + ";total_gasto=" + formatar(totalGasto)
                + ";ordens=" + ordens
                + ";canceladas=" + canceladas
                + ";invalidas=" + invalidas;
    }

    private static boolean ticketValido(String ticket) {
        return ticket != null && ticket.matches("[A-Z0-9]{1,6}");
    }

    private static Long parseQuantidade(String texto) {
        if (texto == null || !texto.matches("-?\\d+")) {
            return null;
        }
        long valor;
        try {
            valor = Long.parseLong(texto);
        } catch (NumberFormatException e) {
            return null;
        }
        if (valor <= 0 || valor % 10 != 0) {
            return null;
        }
        return valor;
    }

    private static BigDecimal parsePreco(String texto) {
        if (texto == null || !texto.matches("\\d+\\.\\d{2}")) {
            return null;
        }
        BigDecimal valor = new BigDecimal(texto);
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return valor;
    }

    private static BigDecimal calcularCorretagem(BigDecimal total) {
        if (total.compareTo(LIMITE_FAIXA_1) <= 0) {
            return CORRETAGEM_FAIXA_1;
        }
        if (total.compareTo(LIMITE_FAIXA_2) <= 0) {
            return CORRETAGEM_FAIXA_2;
        }
        return arredondar(total.multiply(PERCENTUAL_FAIXA_3));
    }

    private static BigDecimal arredondar(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private static String formatar(BigDecimal valor) {
        return arredondar(valor).toPlainString();
    }
}