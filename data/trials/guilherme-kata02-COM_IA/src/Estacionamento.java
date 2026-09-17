import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class Estacionamento {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter
            .ofPattern("uuuu-MM-dd HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);

    private static final int TOLERANCIA_MINUTOS = 15;
    private static final BigDecimal VALOR_PRIMEIRA_HORA = new BigDecimal("10.00");
    private static final BigDecimal VALOR_HORA_ADICIONAL = new BigDecimal("5.00");
    private static final BigDecimal TETO_DIARIO = new BigDecimal("40.00");
    private static final BigDecimal DESCONTO_NOTURNO = new BigDecimal("0.30");
    private static final int MINUTOS_POR_HORA = 60;
    private static final int MINUTOS_POR_DIA = 24 * 60;
    private static final int LIMITE_NOTURNO_MINUTOS = 8 * 60;
    private static final LocalTime INICIO_NOITE = LocalTime.of(22, 0);
    private static final LocalTime FIM_NOITE = LocalTime.of(6, 0);

    public static String cobrar(String entrada, String saida, boolean mensalista) {
        LocalDateTime dataEntrada;
        LocalDateTime dataSaida;
        try {
            dataEntrada = LocalDateTime.parse(entrada, FORMATO);
            dataSaida = LocalDateTime.parse(saida, FORMATO);
        } catch (DateTimeParseException | NullPointerException e) {
            return "ERRO";
        }

        if (dataSaida.isBefore(dataEntrada)) {
            return "ERRO";
        }

        if (mensalista) {
            return "0.00";
        }

        long minutos = Duration.between(dataEntrada, dataSaida).toMinutes();

        if (minutos <= TOLERANCIA_MINUTOS) {
            return "0.00";
        }

        BigDecimal valor = calcularValorBase(minutos);
        BigDecimal teto = calcularTeto(minutos);
        if (valor.compareTo(teto) > 0) {
            valor = teto;
        }

        if (ehNoturna(dataEntrada, dataSaida, minutos)) {
            BigDecimal fator = BigDecimal.ONE.subtract(DESCONTO_NOTURNO);
            valor = valor.multiply(fator);
        }

        return valor.setScale(2, RoundingMode.HALF_UP).toString();
    }

    private static BigDecimal calcularValorBase(long minutos) {
        long horasCobradas = (minutos + MINUTOS_POR_HORA - 1) / MINUTOS_POR_HORA;
        long horasAdicionais = Math.max(0, horasCobradas - 1);
        return VALOR_PRIMEIRA_HORA.add(VALOR_HORA_ADICIONAL.multiply(BigDecimal.valueOf(horasAdicionais)));
    }

    private static BigDecimal calcularTeto(long minutos) {
        long periodos = (minutos + MINUTOS_POR_DIA - 1) / MINUTOS_POR_DIA;
        return TETO_DIARIO.multiply(BigDecimal.valueOf(periodos));
    }

    private static boolean ehNoturna(LocalDateTime entrada, LocalDateTime saida, long minutos) {
        if (minutos > LIMITE_NOTURNO_MINUTOS) {
            return false;
        }
        LocalTime horaEntrada = entrada.toLocalTime();
        LocalTime horaSaida = saida.toLocalTime();

        boolean entradaNoturna = !horaEntrada.isBefore(INICIO_NOITE) || horaEntrada.isBefore(FIM_NOITE);
        boolean saidaNoturna = !horaSaida.isAfter(FIM_NOITE);

        return entradaNoturna && saidaNoturna;
    }
}