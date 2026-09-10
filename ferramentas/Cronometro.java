import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Properties;

/**
 * Cronometro de trial do experimento do Laboratorio 02.
 *
 * Marca inicio e fim de cada trial, registra o instante do primeiro teste verde e
 * grava a linha consolidada em data/trials.csv, aplicando a censura no time-box.
 *
 * Java puro, sem dependencia externa. Rodar sempre a partir da raiz do repositorio:
 *
 *   javac -d out ferramentas/Cronometro.java
 *   java -cp out Cronometro iniciar gabriel kata01 COM_IA 1
 *   java -cp out Cronometro verde gabriel-kata01-COM_IA
 *   java -cp out Cronometro finalizar gabriel-kata01-COM_IA 12 12 7 abc1234
 */
public class Cronometro {

    /** Time-box fixo do enunciado: 35 minutos. */
    private static final long TIME_BOX_SEGUNDOS = 35 * 60;

    private static final Path RAIZ_TRIALS = Path.of("data", "trials");
    private static final Path ARQUIVO_CSV = Path.of("data", "trials.csv");

    private static final String CABECALHO = String.join(",",
            "trial_id", "sujeito", "kata_id", "tratamento", "ordem",
            "inicio_iso", "fim_iso", "tempo_segundos", "censurado",
            "tempo_primeiro_verde_s", "testes_total", "testes_passando",
            "taxa_sucesso", "n_prompts", "commit_final", "collected_at");

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            uso();
            System.exit(1);
        }

        switch (args[0]) {
            case "iniciar":
                iniciar(args);
                break;
            case "verde":
                verde(args);
                break;
            case "finalizar":
                finalizar(args);
                break;
            case "status":
                status(args);
                break;
            default:
                uso();
                System.exit(1);
        }
    }

    // ------------------------------------------------------------------ comandos

    /** iniciar <sujeito> <kata_id> <tratamento> <ordem> */
    private static void iniciar(String[] args) throws IOException {
        exigeArgumentos(args, 5, "iniciar <sujeito> <kata_id> <tratamento> <ordem>");

        String sujeito = args[1];
        String kata = args[2];
        String tratamento = args[3];
        String ordem = args[4];

        if (!tratamento.equals("COM_IA") && !tratamento.equals("SEM_IA")) {
            erro("tratamento deve ser COM_IA ou SEM_IA, recebido: " + tratamento);
        }

        String trialId = sujeito + "-" + kata + "-" + tratamento;
        Path pasta = RAIZ_TRIALS.resolve(trialId);
        Path estado = pasta.resolve("trial.properties");

        if (Files.exists(estado)) {
            erro("ja existe um trial iniciado com o id " + trialId
                    + ". Apague " + estado + " se precisar refazer.");
        }

        Files.createDirectories(pasta);

        Properties dados = new Properties();
        dados.setProperty("trial_id", trialId);
        dados.setProperty("sujeito", sujeito);
        dados.setProperty("kata_id", kata);
        dados.setProperty("tratamento", tratamento);
        dados.setProperty("ordem", ordem);
        dados.setProperty("inicio_iso", agora().toString());
        grava(estado, dados);

        System.out.println("trial iniciado: " + trialId);
        System.out.println("time-box: " + TIME_BOX_SEGUNDOS + " segundos");
        System.out.println("codigo do trial deve ficar em " + pasta);
    }

    /** verde <trial_id> */
    private static void verde(String[] args) throws IOException {
        exigeArgumentos(args, 2, "verde <trial_id>");

        Path estado = estadoDe(args[1]);
        Properties dados = le(estado);

        if (dados.containsKey("tempo_primeiro_verde_s")) {
            System.out.println("primeiro verde ja registrado em "
                    + dados.getProperty("tempo_primeiro_verde_s") + "s, nada a fazer");
            return;
        }

        long decorrido = decorridoSegundos(dados);
        dados.setProperty("tempo_primeiro_verde_s", String.valueOf(decorrido));
        grava(estado, dados);

        System.out.println("primeiro teste verde em " + decorrido + "s");
    }

    /** finalizar <trial_id> <testes_total> <testes_passando> [n_prompts] [commit_final] */
    private static void finalizar(String[] args) throws IOException {
        if (args.length < 4) {
            erro("uso: finalizar <trial_id> <testes_total> <testes_passando> [n_prompts] [commit_final]");
        }

        Path estado = estadoDe(args[1]);
        Properties dados = le(estado);

        int testesTotal = inteiro(args[2], "testes_total");
        int testesPassando = inteiro(args[3], "testes_passando");
        String prompts = args.length > 4 ? args[4] : "";
        String commit = args.length > 5 ? args[5] : "";

        if (testesTotal <= 0) {
            erro("testes_total precisa ser maior que zero");
        }
        if (testesPassando < 0 || testesPassando > testesTotal) {
            erro("testes_passando precisa estar entre 0 e testes_total");
        }

        OffsetDateTime fim = agora();
        long decorrido = decorridoSegundos(dados);
        boolean resolveu = testesPassando == testesTotal;

        // O tempo so vale como time-to-green quando a kata foi realmente concluida
        // dentro do time-box. Caso contrario o trial e censurado no limite e continua
        // na analise, como o enunciado exige.
        boolean censurado = !resolveu || decorrido > TIME_BOX_SEGUNDOS;
        long tempoRegistrado = censurado ? TIME_BOX_SEGUNDOS : decorrido;

        BigDecimal taxa = BigDecimal.valueOf(testesPassando)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(testesTotal), 2, RoundingMode.HALF_UP);

        String linha = String.join(",",
                dados.getProperty("trial_id"),
                dados.getProperty("sujeito"),
                dados.getProperty("kata_id"),
                dados.getProperty("tratamento"),
                dados.getProperty("ordem"),
                dados.getProperty("inicio_iso"),
                fim.toString(),
                String.valueOf(tempoRegistrado),
                String.valueOf(censurado),
                dados.getProperty("tempo_primeiro_verde_s", ""),
                String.valueOf(testesTotal),
                String.valueOf(testesPassando),
                taxa.toPlainString(),
                prompts,
                commit,
                agora().toString());

        garanteCabecalho();
        Files.writeString(ARQUIVO_CSV, linha + System.lineSeparator(),
                StandardCharsets.UTF_8, StandardOpenOption.APPEND);

        dados.setProperty("fim_iso", fim.toString());
        dados.setProperty("encerrado", "true");
        grava(estado, dados);

        System.out.println("trial encerrado: " + dados.getProperty("trial_id"));
        System.out.println("tempo decorrido: " + decorrido + "s");
        System.out.println("tempo registrado: " + tempoRegistrado + "s");
        System.out.println("censurado: " + censurado);
        System.out.println("taxa de sucesso: " + taxa.toPlainString() + "%");
        System.out.println("linha gravada em " + ARQUIVO_CSV);
    }

    /** status <trial_id> */
    private static void status(String[] args) throws IOException {
        exigeArgumentos(args, 2, "status <trial_id>");

        Properties dados = le(estadoDe(args[1]));
        long decorrido = decorridoSegundos(dados);
        long restante = TIME_BOX_SEGUNDOS - decorrido;

        System.out.println("trial: " + dados.getProperty("trial_id"));
        System.out.println("inicio: " + dados.getProperty("inicio_iso"));
        System.out.println("decorrido: " + decorrido + "s");
        System.out.println("restante: " + Math.max(restante, 0) + "s");
        if (restante <= 0) {
            System.out.println("ATENCAO: time-box estourado, encerre o trial");
        }
    }

    // ------------------------------------------------------------------- apoio

    /** Momento atual truncado no segundo, para o CSV nao carregar ruido de milissegundos. */
    private static OffsetDateTime agora() {
        return OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    private static long decorridoSegundos(Properties dados) {
        OffsetDateTime inicio = OffsetDateTime.parse(dados.getProperty("inicio_iso"));
        return Duration.between(inicio, agora()).getSeconds();
    }

    private static Path estadoDe(String trialId) {
        Path estado = RAIZ_TRIALS.resolve(trialId).resolve("trial.properties");
        if (!Files.exists(estado)) {
            erro("trial nao encontrado: " + trialId + " (esperado em " + estado + ")");
        }
        return estado;
    }

    private static Properties le(Path arquivo) throws IOException {
        Properties dados = new Properties();
        try (var entrada = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            dados.load(entrada);
        }
        return dados;
    }

    private static void grava(Path arquivo, Properties dados) throws IOException {
        try (var saida = Files.newBufferedWriter(arquivo, StandardCharsets.UTF_8)) {
            dados.store(saida, "estado do trial");
        }
    }

    /** Cria data/trials.csv com o cabecalho quando ele ainda nao existe. */
    private static void garanteCabecalho() throws IOException {
        if (!Files.exists(ARQUIVO_CSV)) {
            Files.createDirectories(ARQUIVO_CSV.getParent());
            Files.writeString(ARQUIVO_CSV, CABECALHO + System.lineSeparator(),
                    StandardCharsets.UTF_8);
            return;
        }
        List<String> linhas = Files.readAllLines(ARQUIVO_CSV, StandardCharsets.UTF_8);
        if (linhas.isEmpty()) {
            Files.writeString(ARQUIVO_CSV, CABECALHO + System.lineSeparator(),
                    StandardCharsets.UTF_8);
        }
    }

    private static int inteiro(String texto, String campo) {
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException excecao) {
            erro(campo + " precisa ser um numero inteiro, recebido: " + texto);
            return 0;
        }
    }

    private static void exigeArgumentos(String[] args, int quantidade, String formato) {
        if (args.length != quantidade) {
            erro("uso: " + formato);
        }
    }

    private static void erro(String mensagem) {
        System.err.println("erro: " + mensagem);
        System.exit(1);
    }

    private static void uso() {
        System.out.println("Cronometro de trial - Laboratorio 02");
        System.out.println();
        System.out.println("  iniciar   <sujeito> <kata_id> <tratamento> <ordem>");
        System.out.println("  verde     <trial_id>");
        System.out.println("  finalizar <trial_id> <testes_total> <testes_passando> [n_prompts] [commit_final]");
        System.out.println("  status    <trial_id>");
        System.out.println();
        System.out.println("tratamento: COM_IA ou SEM_IA");
        System.out.println("time-box:   " + TIME_BOX_SEGUNDOS + " segundos");
    }
}
