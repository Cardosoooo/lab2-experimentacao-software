import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Kata 04 - Conta de Energia Eletrica. Solucao de referencia.
 *
 * Usada apenas para calibrar a dificuldade da kata antes do experimento.
 * Nao deve ser consultada durante um trial.
 */
public class ContaEnergia {

    private static final BigDecimal PRECO_FAIXA1 = new BigDecimal("0.40");
    private static final BigDecimal PRECO_FAIXA2 = new BigDecimal("0.55");
    private static final BigDecimal PRECO_FAIXA3 = new BigDecimal("0.75");

    private static final BigDecimal AMARELA = new BigDecimal("0.02");
    private static final BigDecimal VERMELHA1 = new BigDecimal("0.04");
    private static final BigDecimal VERMELHA2 = new BigDecimal("0.06");

    private static final long LIMITE_FAIXA1 = 100;
    private static final long LIMITE_FAIXA2 = 200;
    private static final long LIMITE_DESCONTO = 300;
    private static final BigDecimal FATOR_DESCONTO = new BigDecimal("0.95");

    public static String calcular(String consumoKwh, String bandeira) {
        Long consumo = parseConsumo(consumoKwh);
        BigDecimal sobretaxa = sobretaxaDaBandeira(bandeira);

        if (consumo == null || sobretaxa == null) {
            return "ERRO";
        }

        BigDecimal energia = valorPorFaixas(consumo);
        BigDecimal parcial = energia.add(sobretaxa.multiply(BigDecimal.valueOf(consumo)));

        if (consumo > LIMITE_DESCONTO) {
            parcial = parcial.multiply(FATOR_DESCONTO);
        }

        return parcial.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /** Aceita apenas texto composto por digitos, sem sinal nem separador. */
    private static Long parseConsumo(String texto) {
        if (texto == null || !texto.matches("\\d+")) {
            return null;
        }
        return Long.parseLong(texto);
    }

    /** Sobretaxa por kWh da bandeira, ou null quando a bandeira e desconhecida. */
    private static BigDecimal sobretaxaDaBandeira(String bandeira) {
        if (bandeira == null) {
            return null;
        }
        switch (bandeira) {
            case "VERDE":
                return BigDecimal.ZERO;
            case "AMARELA":
                return AMARELA;
            case "VERMELHA1":
                return VERMELHA1;
            case "VERMELHA2":
                return VERMELHA2;
            default:
                return null;
        }
    }

    /** Valor da energia por faixas marginais de consumo. */
    private static BigDecimal valorPorFaixas(long consumo) {
        BigDecimal valor = BigDecimal.ZERO;
        if (consumo > LIMITE_FAIXA2) {
            valor = valor.add(PRECO_FAIXA3.multiply(BigDecimal.valueOf(consumo - LIMITE_FAIXA2)));
            consumo = LIMITE_FAIXA2;
        }
        if (consumo > LIMITE_FAIXA1) {
            valor = valor.add(PRECO_FAIXA2.multiply(BigDecimal.valueOf(consumo - LIMITE_FAIXA1)));
            consumo = LIMITE_FAIXA1;
        }
        return valor.add(PRECO_FAIXA1.multiply(BigDecimal.valueOf(consumo)));
    }
}