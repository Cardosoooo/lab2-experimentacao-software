import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class CompraAcoes {

    private static final Pattern TICKET_PATTERN = Pattern.compile("^[A-Z0-9]{1,6}$");
    private static final Pattern PRECO_PATTERN = Pattern.compile("^\\d+\\.\\d{2}$");
    
    private static final BigDecimal FAIXA_1000 = new BigDecimal("1000.00");
    private static final BigDecimal FAIXA_10000 = new BigDecimal("10000.00");
    private static final BigDecimal CORRETAGEM_FIXA_5 = new BigDecimal("5.00");
    private static final BigDecimal CORRETAGEM_FIXA_10 = new BigDecimal("10.00");
    private static final BigDecimal CORRETAGEM_PERCENTUAL = new BigDecimal("0.01");

    private static class Compra {
        String ticket;
        BigDecimal totalInvestido;
        BigDecimal corretagem;

        Compra(String ticket, BigDecimal totalInvestido, BigDecimal corretagem) {
            this.ticket = ticket;
            this.totalInvestido = totalInvestido;
            this.corretagem = corretagem;
        }
    }

    public static String consolidar(List<String> linhas) {
        int invalidas = 0;
        int canceladas = 0;
        List<Compra> comprasValidas = new ArrayList<>();

        for (String linhaBruta : linhas) {
            String linha = linhaBruta.trim();
            if (linha.isEmpty()) {
                invalidas++;
                continue;
            }

            String[] partes = linha.split(";", -1);
            String tipo = partes[0];

            if ("C".equals(tipo)) {
                if (!processarCompra(partes, comprasValidas)) {
                    invalidas++;
                }
            } else if ("X".equals(tipo)) {
                int canceladasAgora = processarCancelamento(partes, comprasValidas);
                if (canceladasAgora == -1) {
                    invalidas++;
                } else {
                    canceladas += canceladasAgora;
                }
            } else {
                invalidas++;
            }
        }

        return gerarResumo(comprasValidas, canceladas, invalidas);
    }

    private static boolean processarCompra(String[] partes, List<Compra> comprasValidas) {
        if (partes.length != 4) return false;

        String ticket = partes[1];
        String qtdStr = partes[2];
        String precoStr = partes[3];

        if (!isTicketValido(ticket) || !isQuantidadeValida(qtdStr) || !isPrecoValido(precoStr)) {
            return false;
        }

        int quantidade = Integer.parseInt(qtdStr);
        BigDecimal preco = new BigDecimal(precoStr);

        if (preco.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        BigDecimal totalInvestido = preco.multiply(BigDecimal.valueOf(quantidade)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal corretagem = calcularCorretagem(totalInvestido);

        comprasValidas.add(new Compra(ticket, totalInvestido, corretagem));
        return true;
    }

    private static int processarCancelamento(String[] partes, List<Compra> comprasValidas) {
        if (partes.length != 2) return -1; 

        String ticket = partes[1];
        if (!isTicketValido(ticket)) return -1;

        int comprasCanceladasAgora = 0;
        Iterator<Compra> iterator = comprasValidas.iterator();
        
        while (iterator.hasNext()) {
            if (iterator.next().ticket.equals(ticket)) {
                iterator.remove();
                comprasCanceladasAgora++;
            }
        }

        return comprasCanceladasAgora == 0 ? -1 : comprasCanceladasAgora; 
    }

    private static String gerarResumo(List<Compra> comprasValidas, int canceladas, int invalidas) {
        BigDecimal totalInvestidoGeral = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal corretagemGeral = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        for (Compra c : comprasValidas) {
            totalInvestidoGeral = totalInvestidoGeral.add(c.totalInvestido);
            corretagemGeral = corretagemGeral.add(c.corretagem);
        }

        BigDecimal totalGastoGeral = totalInvestidoGeral.add(corretagemGeral);
        int ordens = comprasValidas.size();

        return String.format(Locale.US,
                "total_investido=%.2f;corretagem=%.2f;total_gasto=%.2f;ordens=%d;canceladas=%d;invalidas=%d",
                totalInvestidoGeral, corretagemGeral, totalGastoGeral, ordens, canceladas, invalidas);
    }

    private static boolean isTicketValido(String ticket) {
        return TICKET_PATTERN.matcher(ticket).matches();
    }

    private static boolean isQuantidadeValida(String qtdStr) {
        try {
            int qtd = Integer.parseInt(qtdStr);
            return qtd > 0 && qtd % 10 == 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isPrecoValido(String precoStr) {
        if (!PRECO_PATTERN.matcher(precoStr).matches()) return false;
        try {
            new BigDecimal(precoStr); 
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static BigDecimal calcularCorretagem(BigDecimal totalCompra) {
        if (totalCompra.compareTo(FAIXA_1000) <= 0) return CORRETAGEM_FIXA_5;
        if (totalCompra.compareTo(FAIXA_10000) <= 0) return CORRETAGEM_FIXA_10;
        return totalCompra.multiply(CORRETAGEM_PERCENTUAL).setScale(2, RoundingMode.HALF_UP);
    }
}