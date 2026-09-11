#Requires -Version 5.1
<#
.SYNOPSIS
  Pipeline de metricas estaticas (RQ3): roda CK e PMD CPD sobre o codigo final de um
  trial e normaliza a saida em CSV.

.DESCRIPTION
  - CK (versao pinada 0.7.0) extrai class.csv e method.csv por trial.
  - PMD CPD (versao pinada 7.27.0) extrai cpd.csv (duplicacao de codigo).
  - O script grava a saida bruta em data/metricas/<trial>/ck e /pmd e gera
    data/metricas/<trial>/metricas.csv com as metricas consolidadas.

  O codigo analisado e, por padrao, data/trials/<trial>/src (o codigo final copiado
  ao fim de cada trial). Use -Diretorio para analisar outra pasta, por exemplo a
  solucao de referencia de uma kata na calibracao.

.PARAMETER Repositorio
  Raiz do repositorio (padrao: pasta pai de scripts/).

.PARAMETER Ferramentas
  Pasta onde estao ck/ e pmd/. Padrao: <repositorio>/tools (ferramentas fora do
  versionamento). Facilita apontar para uma copia local das ferramentas.

.PARAMETER Trial
  Identificador do trial, usado para nomear a pasta de saida e a linha do CSV
  consolidado. Exemplo: gabriel-kata01-COM_IA.

.PARAMETER Diretorio
  Pasta com o codigo Java a ser analisado. Padrao: data/trials/<Trial>/src.

.PARAMETER MinTokens
  Tamanho minimo de trecho (em tokens) para o CPD considerar duplicacao. Padrao 50.
  O valor usado e registrado em metricas.csv e deve constar no relatorio final.

.EXAMPLE
  .\scripts\coleta_metricas.ps1 -Trial gabriel-kata01-COM_IA
  .\scripts\coleta_metricas.ps1 -Trial kata01-ref -Diretorio katas\kata01\referencia
#>
[CmdletBinding()]
param(
    [string]$Repositorio,
    [string]$Ferramentas,
    [string]$Trial,
    [string]$Diretorio,
    [int]$MinTokens = 50
)

$ErrorActionPreference = 'Stop'
$soWindows = $env:OS -eq 'Windows_NT'

# ------------------------------------------------------------- raizes

if (-not $Repositorio) {
    $Repositorio = Split-Path -Parent $PSScriptRoot
}
if (-not $Ferramentas) {
    $toolsDir = Join-Path $Repositorio 'tools'
} else {
    $toolsDir = $Ferramentas
}

if (-not $Trial) {
    throw 'Informe -Trial <trial_id>.'
}

if (-not $Diretorio) {
    $Diretorio = Join-Path (Join-Path (Join-Path $Repositorio 'data') 'trials') ($Trial + '\src')
}
if (-not (Test-Path $Diretorio)) {
    throw "Codigo a analisar nao encontrado em $Diretorio"
}
$nArquivos = @(Get-ChildItem -Path $Diretorio -Recurse -Filter '*.java' -File).Count
if ($nArquivos -eq 0) {
    throw "Nenhum arquivo .java em $Diretorio"
}

$metricasBase = Join-Path (Join-Path (Join-Path $Repositorio 'data') 'metricas') $Trial
$ckBase = Join-Path $metricasBase 'ck'
$pmdBase = Join-Path $metricasBase 'pmd'
New-Item -ItemType Directory -Force -Path $ckBase, $pmdBase | Out-Null

# ------------------------------------------------------------- ferramentas

$javaCmd = (Get-Command java -ErrorAction SilentlyContinue).Source
if (-not $javaCmd) {
    throw 'java nao encontrado no PATH. Rode preparar_ambiente.ps1 antes.'
}
$ckJar = Join-Path (Join-Path $toolsDir 'ck') 'ck.jar'
if (-not (Test-Path $ckJar)) {
    throw "ck.jar ausente em $ckJar. Rode preparar_ambiente.ps1 antes."
}

$pmdBin = Get-ChildItem -Path (Join-Path $toolsDir 'pmd') -Directory |
    Where-Object { $_.Name -like 'pmd-bin-*' } |
    Select-Object -First 1
if (-not $pmdBin) {
    throw "Distribuicao do PMD ausente em $(Join-Path $toolsDir 'pmd'). Rode preparar_ambiente.ps1 antes."
}
if ($soWindows) {
    $pmdComando = 'cmd.exe'
    $pmdBat = Join-Path $pmdBin.FullName 'bin\pmd.bat'
    if (-not (Test-Path $pmdBat)) {
        throw "pmd.bat ausente em $pmdBat"
    }
} else {
    $pmdComando = Join-Path $pmdBin.FullName 'bin\pmd'
    if (-not (Test-Path $pmdComando)) {
        throw "pmd ausente em $pmdComando"
    }
}

# ------------------------------------------------------------- CK

Write-Host "== CK =="
$ckOut = $ckBase + [IO.Path]::DirectorySeparatorChar   # CK concatena 'class.csv' no final
$ckLog = Join-Path $ckBase 'ck.log'
$ckErr = Join-Path $ckBase 'ck.err'
$p = Start-Process -FilePath $javaCmd `
    -ArgumentList @('-jar', $ckJar, $Diretorio, 'true', '0', 'false', $ckOut) `
    -RedirectStandardOutput $ckLog -RedirectStandardError $ckErr `
    -Wait -PassThru
if ($p.ExitCode -ne 0) {
    throw "CK falhou (exit $($p.ExitCode)). Veja $ckErr"
}
$classCsv = Join-Path $ckBase 'class.csv'
$methodCsv = Join-Path $ckBase 'method.csv'
if (-not (Test-Path $classCsv) -or -not (Test-Path $methodCsv)) {
    throw "CK nao gerou class.csv/method.csv em $ckBase"
}
Write-Host "CK OK -> $ckBase" -ForegroundColor Green

# ------------------------------------------------------------- PMD CPD

Write-Host "== PMD CPD =="
$cpdCsv = Join-Path $pmdBase 'cpd.csv'
$cpdLog = Join-Path $pmdBase 'cpd.log'
$cpdErr = Join-Path $pmdBase 'cpd.err'
Remove-Item -Force $cpdCsv -ErrorAction SilentlyContinue

if ($soWindows) {
    $argumentos = '/c "' + $pmdBat + '" cpd --dir "' + $Diretorio +
        '" --minimum-tokens ' + $MinTokens +
        ' --language java --format csv --report-file "' + $cpdCsv +
        '" --no-fail-on-violation'
} else {
    $argumentos = @('cpd', '--dir', $Diretorio, '--minimum-tokens', "$MinTokens",
        '--language', 'java', '--format', 'csv', '--report-file', $cpdCsv,
        '--no-fail-on-violation')
}

$p = Start-Process -FilePath $pmdComando -ArgumentList $argumentos `
    -RedirectStandardOutput $cpdLog -RedirectStandardError $cpdErr `
    -Wait -PassThru
if ($p.ExitCode -ne 0) {
    Write-Host "PMD cpd retornou exit $($p.ExitCode) (esperado 0 com --no-fail-on-violation). Veja $cpdErr" -ForegroundColor Yellow
}
if (-not (Test-Path $cpdCsv)) {
    Write-Host 'CPD nao gerou cpd.csv (sem duplicacoes acima do limite); repetindo com saida vazia.' -ForegroundColor Yellow
    Set-Content -Path $cpdCsv -Value 'lines,tokens,occurrences' -Encoding utf8
}
Write-Host "PMD CPD OK -> $pmdBase" -ForegroundColor Green

# ------------------------------------------------------------- normalizacao

function Indice-Coluna {
    param([string]$Cabecalho, [string]$Nome)
    $colunas = $Cabecalho.Split(',')
    for ($i = 0; $i -lt $colunas.Count; $i++) {
        if ($colunas[$i] -eq $Nome) {
            return $i
        }
    }
    throw "Coluna '$Nome' nao encontrada no cabecalho: $Cabecalho"
}

# class.csv -> loc_total e numero de classes
$classLinhas = Get-Content -Path $classCsv | Where-Object { $_ }
$idxLocClass = Indice-Coluna $classLinhas[0] 'loc'
$locTotal = 0
$nClasses = 0
for ($i = 1; $i -lt $classLinhas.Count; $i++) {
    $campos = $classLinhas[$i].Split(',')
    $locTotal += [int]$campos[$idxLocClass]
    $nClasses++
}

# method.csv -> complexidade ciclomatica por metodo (coluna wmc)
$methodLinhas = Get-Content -Path $methodCsv | Where-Object { $_ }
$idxWmc = Indice-Coluna $methodLinhas[0] 'wmc'
$ccTotal = 0L
$nMetodos = 0
for ($i = 1; $i -lt $methodLinhas.Count; $i++) {
    $campos = $methodLinhas[$i].Split(',')
    $ccTotal += [long]$campos[$idxWmc]
    $nMetodos++
}
if ($nMetodos -gt 0) {
    $ccMedia = [math]::Round($ccTotal / $nMetodos, 3)
} else {
    $ccMedia = 0
}

# cpd.csv -> duplicacao de codigo
$cpdLinhas = Get-Content -Path $cpdCsv | Where-Object { $_ }
$duplicacoes = 0
$linhasDuplicadas = 0
for ($i = 1; $i -lt $cpdLinhas.Count; $i++) {
    $campos = $cpdLinhas[$i].Split(',')
    if ($campos.Count -lt 3) {
        continue
    }
    $linhas = [int]$campos[0]
    $ocorrencias = [int]$campos[2]
    $linhasDuplicadas += $linhas * $ocorrencias
    $duplicacoes++
}
if ($locTotal -gt 0) {
    $pctDuplicacao = [math]::Round(100 * $linhasDuplicadas / $locTotal, 2)
} else {
    $pctDuplicacao = 0
}

$collectedAt = (Get-Date).ToString('yyyy-MM-ddTHH:mm:sszzz')
$consolidado = Join-Path $metricasBase 'metricas.csv'
# Os campos decimais precisam ser formatados em cultura invariante. Com a cultura do
# sistema em portugues, o separador vira virgula, o valor 1.2 e escrito como "1,2" e
# quebra a linha do CSV em duas colunas, deslocando todas as metricas seguintes.
$invariante = [System.Globalization.CultureInfo]::InvariantCulture
$ccMediaTexto = ([double]$ccMedia).ToString($invariante)
$pctDuplicacaoTexto = ([double]$pctDuplicacao).ToString($invariante)

$linha = @(
    $Trial,
    $nArquivos,
    $nClasses,
    $locTotal,
    $nMetodos,
    $ccTotal,
    $ccMediaTexto,
    $duplicacoes,
    $linhasDuplicadas,
    $pctDuplicacaoTexto,
    $MinTokens,
    $collectedAt
) -join ','

$cabecalho = 'trial_id,n_arquivos,n_classes,loc_total,n_metodos,cc_total,cc_media_por_metodo,duplicacoes_cpd,linhas_duplicadas_cpd,pct_duplicacao_cpd,min_tokens_cpd,collected_at'
if (-not (Test-Path $consolidado)) {
    Set-Content -Path $consolidado -Value $cabecalho -Encoding utf8
}
Add-Content -Path $consolidado -Value $linha -Encoding utf8

Write-Host "`n== Resumo do trial $Trial ==" -ForegroundColor Cyan
Write-Host "  arquivos.: $nArquivos"
Write-Host "  classes..: $nClasses"
Write-Host "  loc total: $locTotal"
Write-Host "  cc total.: $ccTotal (metodos: $nMetodos)"
Write-Host "  cc media por metodo: $ccMedia"
Write-Host "  duplicacao CPD (min tokens $MinTokens): $linhasDuplicadas linhas ($pctDuplicacao%)"
Write-Host "Consolidado: $consolidado"