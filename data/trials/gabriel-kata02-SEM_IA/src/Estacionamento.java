import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

public class Estacionamento {

    public static String cobrar(String entrada, String saida, boolean mensalista) {

        if (entrada == null || saida == null) {
            return "ERRO";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm")
                .withResolverStyle(ResolverStyle.STRICT);

        LocalDateTime dtEntrada;
        LocalDateTime dtSaida;

        try {
            dtEntrada = LocalDateTime.parse(entrada, formatter);
            dtSaida = LocalDateTime.parse(saida, formatter);
        } catch (DateTimeParseException e) {
            return "ERRO";
        }

        if (dtSaida.isBefore(dtEntrada)) {
            return "ERRO";
        }

        if (mensalista) {
            return "0.00";
        }

        long minutosTotais = Duration.between(dtEntrada, dtSaida).toMinutes();

        if (minutosTotais <= 15) {
            return "0.00";
        }

        long periodosDe24h = minutosTotais / 1440;
        long minutosRestantes = minutosTotais % 1440;

        double valorTotal = periodosDe24h * 40.0;

        
        if (minutosRestantes > 0) {
            double valorRestante = 10.0; 
            
            if (minutosRestantes > 60) {
                long horasExtras = (minutosRestantes - 60 + 59) / 60;
                valorRestante += horasExtras * 5.0;
            }
            
            if (valorRestante > 40.0) {
                valorRestante = 40.0;
            }
            
            valorTotal += valorRestante;
        }

        int horaEntrada = dtEntrada.getHour();
        int horaSaida = dtSaida.getHour();
        int minutoSaida = dtSaida.getMinute();

        boolean entradaNoturna = (horaEntrada >= 22 || horaEntrada < 6);
        boolean saidaNoturna = (horaSaida < 6 || (horaSaida == 6 && minutoSaida == 0));
        boolean duracaoPermitida = (minutosTotais <= 480);

        if (entradaNoturna && saidaNoturna && duracaoPermitida) {
            valorTotal *= 0.7;
        }

        BigDecimal bdFinal = BigDecimal.valueOf(valorTotal).setScale(2, RoundingMode.HALF_UP);
        
        return String.format(Locale.US, "%.2f", bdFinal);
    }
}