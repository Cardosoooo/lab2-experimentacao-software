#Requires -Version 5.1
<#
.SYNOPSIS
  Prepara o ambiente de execucao do experimento: valida o JDK e baixa as ferramentas
  de metrica estatica (CK e PMD CPD) para a pasta tools/, fora do versionamento.

.DESCRIPTION
  - Valida que java/javac existem no PATH e imprime a versao (JDK 17 ou superior).
  - Baixa CK (jar com dependencias) e PMD (distribuicao binaria) na versao pinada
    e extrai para tools/.
  - Com -SkipDownload, apenas valida o que ja existe, sem baixar nada.

.EXAMPLE
  .\scripts\preparar_ambiente.ps1
  .\scripts\preparar_ambiente.ps1 -SkipDownload
#>
[CmdletBinding()]
param(
    [switch]$SkipDownload
)

$ErrorActionPreference = 'Stop'

$repo    = Split-Path -Parent $PSScriptRoot
$tools   = Join-Path $repo 'tools'
$ckDir   = Join-Path $tools 'ck'
$pmdDir  = Join-Path $tools 'pmd'

# Versoes pinadas. Altere estas linhas (e o registro de versoes do protocolo) juntas.
$ckVersion   = '0.7.0'
$pmdVersion  = '7.27.0'
$ckUrl       = "https://repo1.maven.org/maven2/com/github/mauricioaniche/ck/$ckVersion/ck-$ckVersion-jar-with-dependencies.jar"
$pmdZipUrl   = "https://github.com/pmd/pmd/releases/download/pmd_releases/$pmdVersion/pmd-dist-$pmdVersion-bin.zip"

# ---------------------------------------------------------------- JDK

function Test-Jdk {
    $java = Get-Command java -ErrorAction SilentlyContinue
    $javac = Get-Command javac -ErrorAction SilentlyContinue
    if (-not $java -or -not $javac) {
        throw 'java ou javac nao encontrado no PATH. Instale um JDK 17 ou superior (Temurin 25 recomendado).'
    }

    $saida = (& java -version 2>&1 | Select-Object -First 1)
    $versao = [string]$saida

    $match = [regex]::Match($versao, 'version "(\d+)')
    if (-not $match.Success) {
        throw "Nao foi possivel ler a versao do java. Saida: $versao"
    }
    $major = [int]$match.Groups[1].Value
    if ($major -lt 17) {
        throw "JDK encontrado e $major, mas o experimento exige 17 ou superior. Saida: $versao"
    }

    Write-Host 'JDK  OK  ' -NoNewline -ForegroundColor Green
    Write-Host $versao
    return $true
}

# ---------------------------------------------------------------- download

function Get-Arquivo {
    param([string]$Url, [string]$Destino, [string]$Rotulo)

    if (Test-Path $Destino) {
        Write-Host "$Rotulo  OK  ja presente em $Destino" -ForegroundColor Green
        return
    }

    $arquivo = Join-Path (Split-Path -Parent $Destino) (Split-Path -Leaf $Url)
    Write-Host "baixando ${Rotulo}: $Url"

    $curl = Get-Command curl.exe -ErrorAction SilentlyContinue
    if ($curl) {
        & $curl.Source -L --fail --silent --show-error -o $arquivo $Url
        if ($LASTEXITCODE -ne 0) {
            throw "Falha ao baixar $Rotulo com curl (exit $LASTEXITCODE)."
        }
    } else {
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -Uri $Url -OutFile $arquivo -UseBasicParsing
    }

    if (-not (Test-Path $arquivo)) {
        throw "Arquivo de $Rotulo nao foi criado."
    }
    Move-Item -Force $arquivo $Destino
    Write-Host "$Rotulo  OK  $Destino" -ForegroundColor Green
}

function Baixa-CK {
    New-Item -ItemType Directory -Force -Path $ckDir | Out-Null
    $jar = Join-Path $ckDir 'ck.jar'
    Get-Arquivo -Url $ckUrl -Destino $jar -Rotulo 'CK'
    if ((Get-Item $jar).Length -lt 1MB) {
        throw "ck.jar parece incompleto (tamanho invalido). Apague $jar e rode novamente."
    }
}

function Baixa-PMD {
    New-Item -ItemType Directory -Force -Path $pmdDir | Out-Null
    $zip = Join-Path $pmdDir "pmd-dist-$pmdVersion-bin.zip"
    Get-Arquivo -Url $pmdZipUrl -Destino $zip -Rotulo 'PMD'

    $extraido = Join-Path $pmdDir "pmd-bin-$pmdVersion"
    if (-not (Test-Path (Join-Path $extraido 'bin'))) {
        Write-Host "extraindo PMD..."
        Expand-Archive -Path $zip -DestinationPath $pmdDir -Force
    }

    $pmdBat = Join-Path $extraido 'bin\pmd.bat'
    if (-not (Test-Path $pmdBat)) {
        throw "Estrutura inesperada apos extrair o PMD. Esperado em $pmdBat."
    }
    Write-Host "PMD OK  $pmdBat" -ForegroundColor Green
}

# ---------------------------------------------------------------- validacao

function Valida-Tools {
    $ckJar = Join-Path $ckDir 'ck.jar'
    $pmdBat = Join-Path $pmdDir "pmd-bin-$pmdVersion\bin\pmd.bat"

    if (-not (Test-Path $ckJar)) {
        throw "ck.jar ausente em $ckJar. Rode preparar_ambiente.ps1 sem -SkipDownload."
    }
    if (-not (Test-Path $pmdBat)) {
        throw "pmd.bat ausente em $pmdBat. Rode preparar_ambiente.ps1 sem -SkipDownload."
    }

    Write-Host "CK  OK  $ckJar" -ForegroundColor Green
    Write-Host "PMD OK  $pmdBat" -ForegroundColor Green
}

# ---------------------------------------------------------------- main

Write-Host "== Preparacao do ambiente - Laboratorio 02 ==`n"

Test-Jdk | Out-Null

if ($SkipDownload) {
    Valida-Tools
} else {
    Baixa-CK
    Baixa-PMD
}

Write-Host "`nVersoes registradas no protocolo:" -ForegroundColor Cyan
Write-Host "  CK  $ckVersion"
Write-Host "  PMD $pmdVersion"
Write-Host "`nAmbiente pronto para a Sprint 2."