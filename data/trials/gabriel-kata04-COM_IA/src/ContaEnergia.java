import java.math.BigDecimal;
import java.math.RoundingMode;

public class ContaEnergia {

    private static final BigDecimal LIMITE_FAIXA_1 = new BigDecimal("100");
    private static final BigDecimal LIMITE_FAIXA_2 = new BigDecimal("200");
    private static final BigDecimal LIMITE_DESCONTO = new BigDecimal("300");

    private static final BigDecimal PRECO_FAIXA_1 = new BigDecimal("0.40");
    private static final BigDecimal PRECO_FAIXA_2 = new BigDecimal("0.55");
    private static final BigDecimal PRECO_FAIXA_3 = new BigDecimal("0.75");

    private static final BigDecimal FATOR_DESCONTO = new BigDecimal("0.95");

    public static String calcular(String consumoKwh, String bandeira) {
        if (consumoKwh == null || !consumoKwh.matches("\\d+")) {
            return "ERRO";
        }

        BigDecimal sobretaxaPorKwh = sobretaxaBandeira(bandeira);
        if (sobretaxaPorKwh == null) {
            return "ERRO";
        }

        BigDecimal consumo = new BigDecimal(consumoKwh);

        BigDecimal energia = calcularEnergia(consumo);
        BigDecimal sobretaxa = sobretaxaPorKwh.multiply(consumo);

        BigDecimal total = energia.add(sobretaxa);

        if (consumo.compareTo(LIMITE_DESCONTO) > 0) {
            total = total.multiply(FATOR_DESCONTO);
        }

        return arredondar(total).toPlainString();
    }

    private static BigDecimal calcularEnergia(BigDecimal consumo) {
        BigDecimal restante = consumo;
        BigDecimal total = BigDecimal.ZERO;

        BigDecimal faixa1 = min(restante, LIMITE_FAIXA_1);
        total = total.add(faixa1.multiply(PRECO_FAIXA_1));
        restante = restante.subtract(faixa1);

        if (restante.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal capacidadeFaixa2 = LIMITE_FAIXA_2.subtract(LIMITE_FAIXA_1);
            BigDecimal faixa2 = min(restante, capacidadeFaixa2);
            total = total.add(faixa2.multiply(PRECO_FAIXA_2));
            restante = restante.subtract(faixa2);
        }

        if (restante.compareTo(BigDecimal.ZERO) > 0) {
            total = total.add(restante.multiply(PRECO_FAIXA_3));
        }

        return total;
    }

    private static BigDecimal min(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) <= 0 ? a : b;
    }

    private static BigDecimal sobretaxaBandeira(String bandeira) {
        if (bandeira == null) {
            return null;
        }
        switch (bandeira) {
            case "VERDE":
                return new BigDecimal("0.00");
            case "AMARELA":
                return new BigDecimal("0.02");
            case "VERMELHA1":
                return new BigDecimal("0.04");
            case "VERMELHA2":
                return new BigDecimal("0.06");
            default:
                return null;
        }
    }

    private static BigDecimal arredondar(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}