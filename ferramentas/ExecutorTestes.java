import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Runner de testes de aceitacao do Laboratorio 02.
 *
 * Compila o esqueleto (ou a solucao de referencia) de uma kata junto com a suite
 * de testes de aceitacao, executa a suite e conta os testes passando e falhando,
 * seguindo o contrato definido em katas/README.md.
 *
 * Java puro, sem dependencia externa. Rodar sempre a partir da raiz do repositorio:
 *
 *   javac -d out ferramentas/ExecutorTestes.java
 *   java -cp out ExecutorTestes kata03
 *   java -cp out ExecutorTestes kata03 --ref
 *
 * A saida do runner e a saida da propria suite (linhas TESTE; e RESUMO;), intacta.
 * O codigo de saida espelha o da suite: 0 quando todos os testes passam, 1 quando
 * algum falha, 2 quando o runner nao consegue compilar ou validar a execucao.
 */
public class ExecutorTestes {

    private static final Pattern TESTE_PASSOU = Pattern.compile("^TESTE;(.+);PASSOU$");
    private static final Pattern TESTE_FALHOU = Pattern.compile("^TESTE;(.+);FALHOU");
    private static final Pattern RESUMO = Pattern.compile(
            "^RESUMO;total=(\\d+);passando=(\\d+);falhando=(\\d+)$");

    public static void main(String[] args) throws IOException {
        if (args.length < 1 || !args[0].matches("kata\\d{2}")) {
            uso();
            System.exit(2);
        }

        String kata = args[0];
        boolean referencia = args.length > 1 && "--ref".equals(args[1]);

        Path dirSrc = Path.of("katas", kata, referencia ? "referencia" : "src");
        Path dirTest = Path.of("katas", kata, "test");
        Path dirOut = Path.of("out", "runner", kata + (referencia ? "-ref" : "-src"));

        if (!Files.isDirectory(dirSrc) || !Files.isDirectory(dirTest)) {
            erro("pasta incompleta em katas/" + kata);
        }

        List<Path> fontes = new ArrayList<>();
        listaJava(dirSrc, fontes);
        listaJava(dirTest, fontes);
        if (fontes.isEmpty()) {
            erro("nenhum arquivo .java encontrado em " + dirSrc + " e " + dirTest);
        }

        if (!compila(dirOut, fontes)) {
            erro("falha ao compilar " + kata + " em " + dirOut);
        }

        String nomeClasse = "TestesKata" + kata.substring(4);
        Saida saida = roda(dirOut, nomeClasse);
        System.out.print(saida.buffer);
        System.out.flush();

        if (saida.code == 2) {
            erro(kata + ": o processo java falhou por motivo de infraestrutura");
        }

        Validacao resultado = valida(saida.buffer);
        if (resultado.falha) {
            System.err.println("runner: inconsistencia na saida da suite " + kata + ": "
                    + resultado.mensagem);
            System.exit(2);
        }

        System.err.println("runner: " + kata + (referencia ? " (referencia)" : " (esqueleto)")
                + " total=" + resultado.total
                + " passando=" + resultado.passando
                + " falhando=" + resultado.falhando);

        System.exit(saida.code);
    }

    // ------------------------------------------------------------- execucao

    private static boolean compila(Path dirOut, List<Path> fontes) throws IOException {
        Files.createDirectories(dirOut);
        List<String> comando = new ArrayList<>();
        comando.add("javac");
        comando.add("-encoding");
        comando.add("UTF-8");
        comando.add("-d");
        comando.add(dirOut.toString());
        for (Path fonte : fontes) {
            comando.add(fonte.toString());
        }
        Process processo;
        try {
            processo = new ProcessBuilder(comando).inheritIO().start();
        } catch (IOException excecao) {
            erro("javac nao encontrado no PATH: " + excecao.getMessage());
            return false;
        }
        try {
            processo.waitFor();
        } catch (InterruptedException interrompido) {
            Thread.currentThread().interrupt();
            return false;
        }
        return processo.exitValue() == 0;
    }

    private static Saida roda(Path dirOut, String nomeClasse) {
        Process processo;
        StringBuilder buffer = new StringBuilder();
        try {
            processo = new ProcessBuilder("java", "-cp", dirOut.toString(), nomeClasse)
                    .redirectErrorStream(true)
                    .start();
            buffer.append(new String(processo.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
            processo.waitFor();
        } catch (IOException | InterruptedException excecao) {
            erro("falha ao executar " + nomeClasse + ": " + excecao.getMessage());
            return new Saida(2, buffer.toString());
        }
        return new Saida(processo.exitValue(), buffer.toString());
    }

    private static Validacao valida(String texto) {
        int contadosPassando = 0;
        int contadosFalhando = 0;
        int total = -1;
        int passando = -1;
        int falhando = -1;

        for (String linha : texto.split("\\R")) {
            if (TESTE_PASSOU.matcher(linha).matches()) {
                contadosPassando++;
            } else if (TESTE_FALHOU.matcher(linha).find()) {
                contadosFalhando++;
            } else if (RESUMO.matcher(linha).matches()) {
                Matcher m = RESUMO.matcher(linha);
                m.matches();
                total = Integer.parseInt(m.group(1));
                passando = Integer.parseInt(m.group(2));
                falhando = Integer.parseInt(m.group(3));
            }
        }

        if (total < 0) {
            return new Validacao(true, "linha RESUMO ausente ou fora do formato do contrato", 0, 0, 0);
        }
        if (contadosPassando + contadosFalhando != total) {
            return new Validacao(true, "testes contados (" + contadosPassando + "+" + contadosFalhando
                    + ") divergem do total da suite (" + total + ")", 0, 0, 0);
        }
        if (passando != contadosPassando || falhando != contadosFalhando) {
            return new Validacao(true, "RESUMO declara passando=" + passando + " falhando=" + falhando
                    + " mas as linhas TESTE somam " + contadosPassando + "+" + contadosFalhando, 0, 0, 0);
        }
        return new Validacao(false, "", total, passando, falhando);
    }

    // ----------------------------------------------------------------- apoio

    private static void listaJava(Path dir, List<Path> destino) throws IOException {
        try (Stream<Path> arquivos = Files.list(dir)) {
            arquivos.filter(arquivo -> arquivo.toString().endsWith(".java"))
                    .sorted()
                    .forEach(destino::add);
        }
    }

    private static void erro(String mensagem) {
        System.err.println("erro: " + mensagem);
        System.exit(2);
    }

    private static void uso() {
        System.out.println("Runner de testes de aceitacao - Laboratorio 02");
        System.out.println();
        System.out.println("  ExecutorTestes <kata_id> [--ref]");
        System.out.println();
        System.out.println("kata_id: kata01 a kata04");
        System.out.println("--ref:   roda contra a solucao de referencia em vez do esqueleto");
    }

    private static final class Saida {
        final int code;
        final String buffer;

        Saida(int code, String buffer) {
            this.code = code;
            this.buffer = buffer;
        }
    }

    private static final class Validacao {
        final boolean falha;
        final String mensagem;
        final int total;
        final int passando;
        final int falhando;

        Validacao(boolean falha, String mensagem, int total, int passando, int falhando) {
            this.falha = falha;
            this.mensagem = mensagem;
            this.total = total;
            this.passando = passando;
            this.falhando = falhando;
        }
    }
}