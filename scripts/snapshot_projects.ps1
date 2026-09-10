#Requires -Version 5.1
<#
.SYNOPSIS
  Registra um snapshot do quadro do GitHub Projects (itens do projeto) em data/snapshots/.

.DESCRIPTION
  - Roda `gh project item-list <Projeto> --owner <Dono> --format json`.
  - Valida a saida como JSON e grava em data/snapshots/<aaaaMMdd-HHmmss>.json,
    permitindo reconstruir a evolucao dos cartoes entre os trials da sprint.
  - Autenticacao: usa o gh autenticado (`gh auth`) ou a variavel GITHUB_TOKEN/GH_TOKEN.

.PARAMETER Projeto
  Numero do projeto (padrao: 4 - quadro do Lab 2).

.PARAMETER Dono
  Dono/organizacao do projeto (padrao: Cardosoooo).

.PARAMETER Saida
  Caminho alternativo para o arquivo de snapshot.

.EXAMPLE
  .\scripts\snapshot_projects.ps1
  .\scripts\snapshot_projects.ps1 -Projeto 4 -Dono Cardosoooo
#>
[CmdletBinding()]
param(
    [int]$Projeto = 4,
    [string]$Dono = 'Cardosoooo',
    [string]$Saida
)

$ErrorActionPreference = 'Stop'

$repo = Split-Path -Parent $PSScriptRoot
$snapshots = Join-Path (Join-Path $repo 'data') 'snapshots'
New-Item -ItemType Directory -Force -Path $snapshots | Out-Null

if (-not $Saida) {
    $carimbo = Get-Date -Format 'yyyyMMdd-HHmmss'
    $Saida = Join-Path $snapshots ($carimbo + '.json')
}

$ghCmd = (Get-Command gh -ErrorAction SilentlyContinue).Source
if (-not $ghCmd) {
    throw 'gh (GitHub CLI) nao encontrado no PATH.'
}

$outTmp = [IO.Path]::GetTempFileName()
$errTmp = [IO.Path]::GetTempFileName()
try {
    $p = Start-Process -FilePath $ghCmd `
        -ArgumentList @('project', 'item-list', "$Projeto", '--owner', $Dono, '--format', 'json') `
        -RedirectStandardOutput $outTmp -RedirectStandardError $errTmp -Wait -PassThru
    if ($p.ExitCode -ne 0) {
        $erro = Get-Content -Path $errTmp -Raw
        throw "gh project item-list $Projeto falhou (exit $($p.ExitCode)). Verifique ''gh auth status'' e GITHUB_TOKEN. Detalhe: $erro"
    }

    $json = Get-Content -Path $outTmp -Raw
    $null = $json | ConvertFrom-Json   # valida o JSON antes de gravar

    [IO.File]::WriteAllText($Saida, $json, (New-Object System.Text.UTF8Encoding($false)))
    Write-Host "Snapshot OK -> $Saida" -ForegroundColor Green
}
finally {
    Remove-Item -Force $outTmp, $errTmp -ErrorAction SilentlyContinue
}