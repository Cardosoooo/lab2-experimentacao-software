import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Kata 02 - Cobranca de Estacionamento. Solucao de referencia.
 *
 * Usada apenas para calibrar a dificuldade da kata antes do experimento.
 * Nao deve ser consultada durante um trial.
 */
public class Estacionamento {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final BigDecimal PRIMEIRA_HORA = new BigDecimal("10.00");
    private static final BigDecimal HORA_ADICIONAL = new BigDecimal("5.00");
    private static final BigDecimal TETO_POR_PERIODO = new BigDecimal("40.00");
    private static final BigDecimal FATOR_NOTURNO = new BigDecimal("0.70");

    private static final int TOLERANCIA_MINUTOS = 15;
    private static final int MINUTOS_POR_PERIODO = 24 * 60;
    private static final int LIMITE_NOTURNO_MINUTOS = 8 * 60;

    public static String cobrar(String entrada, String saida, boolean mensalista) {
        LocalDateTime inicio = parse(entrada);
        LocalDateTime fim = parse(saida);

        if (inicio == null || fim == null || fim.isBefore(inicio)) {
            return "ERRO";
        }
        if (mensalista) {
            return "0.00";
        }

        long minutos = Duration.between(inicio, fim).toMinutes();
        if (minutos <= TOLERANCIA_MINUTOS) {
            return "0.00";
        }

        BigDecimal valor = valorPorHoras(minutos).min(teto(minutos));

        if (permanenciaNoturna(inicio, fim, minutos)) {
            valor = valor.multiply(FATOR_NOTURNO);
        }

        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static LocalDateTime parse(String texto) {
        if (texto == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(texto, FORMATO);
        } catch (DateTimeParseException excecao) {
            return null;
        }
    }

    /** Primeira hora cheia mais 5.00 por hora adicional ou fracao. */
    private static BigDecimal valorPorHoras(long minutos) {
        long horas = (minutos + 59) / 60;
        return PRIMEIRA_HORA.add(HORA_ADICIONAL.multiply(BigDecimal.valueOf(horas - 1)));
    }

    /** Teto de 40.00 por periodo de 24 horas iniciado. */
    private static BigDecimal teto(long minutos) {
        long periodos = (minutos + MINUTOS_POR_PERIODO - 1) / MINUTOS_POR_PERIODO;
        return TETO_POR_PERIODO.multiply(BigDecimal.valueOf(periodos));
    }

    private static boolean permanenciaNoturna(LocalDateTime inicio, LocalDateTime fim, long minutos) {
        int horaEntrada = inicio.getHour();
        boolean entrouDeNoite = horaEntrada >= 22 || horaEntrada < 6;
        boolean saiuAteSeis = !fim.toLocalTime().isAfter(LocalTime.of(6, 0));
        return entrouDeNoite && saiuAteSeis && minutos <= LIMITE_NOTURNO_MINUTOS;
    }
}
