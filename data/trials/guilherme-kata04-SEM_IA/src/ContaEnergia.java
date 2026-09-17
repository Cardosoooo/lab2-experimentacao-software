import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.regex.Pattern;

public class ContaEnergia {

    private static final Pattern CONSUMO_PATTERN = Pattern.compile("^\\d+$");

    private static final Map<String, BigDecimal> BANDEIRAS = Map.of(
            "VERDE", BigDecimal.ZERO,
            "AMARELA", new BigDecimal("0.02"),
            "VERMELHA1", new BigDecimal("0.04"),
            "VERMELHA2", new BigDecimal("0.06")
    );

    private static final BigDecimal PRECO_FAIXA_1 = new BigDecimal("0.40");
    private static final BigDecimal PRECO_FAIXA_2 = new BigDecimal("0.55");
    private static final BigDecimal PRECO_FAIXA_3 = new BigDecimal("0.75");
    
    private static final BigDecimal FAIXA_100 = new BigDecimal("100");
    private static final BigDecimal FAIXA_200 = new BigDecimal("200");
    
    private static final BigDecimal LIMITE_DESCONTO = new BigDecimal("300");
    private static final BigDecimal FATOR_DESCONTO = new BigDecimal("0.95");

    public static String calcular(String consumoKwh, String bandeira) {
        if (consumoKwh == null || !CONSUMO_PATTERN.matcher(consumoKwh).matches()) {
            return "ERRO";
        }
        if (bandeira == null || !BANDEIRAS.containsKey(bandeira)) {
            return "ERRO";
        }

        BigDecimal consumo = new BigDecimal(consumoKwh);
        BigDecimal taxaBandeira = BANDEIRAS.get(bandeira);

        BigDecimal energia = calcularEnergia(consumo);
        BigDecimal sobretaxa = consumo.multiply(taxaBandeira);
        
        BigDecimal total = energia.add(sobretaxa);

        total = aplicarDesconto(total, consumo);

        return total.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static BigDecimal calcularEnergia(BigDecimal consumo) {
        BigDecimal total = BigDecimal.ZERO;

        if (consumo.compareTo(FAIXA_200) > 0) {
            BigDecimal excedente = consumo.subtract(FAIXA_200);
            total = total.add(excedente.multiply(PRECO_FAIXA_3));
            total = total.add(FAIXA_100.multiply(PRECO_FAIXA_2)); 
            total = total.add(FAIXA_100.multiply(PRECO_FAIXA_1)); 
        } 
        else if (consumo.compareTo(FAIXA_100) > 0) {
            BigDecimal excedente = consumo.subtract(FAIXA_100);
            total = total.add(excedente.multiply(PRECO_FAIXA_2));
            total = total.add(FAIXA_100.multiply(PRECO_FAIXA_1));
        } 
        else {
            total = total.add(consumo.multiply(PRECO_FAIXA_1));
        }

        return total;
    }

    private static BigDecimal aplicarDesconto(BigDecimal valorTotal, BigDecimal consumo) {
        if (consumo.compareTo(LIMITE_DESCONTO) > 0) {
            return valorTotal.multiply(FATOR_DESCONTO);
        }
        return valorTotal;
    }
}