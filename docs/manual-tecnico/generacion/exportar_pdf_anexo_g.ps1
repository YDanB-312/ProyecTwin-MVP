# exportar_pdf_anexo_g.ps1
# Exporta el Anexo G a PDF para verificación visual.
$ErrorActionPreference = "Stop"
$ruta = Join-Path (Split-Path -Parent $PSScriptRoot) "Manual_Tecnico_ProyecTwin_Anexo_G.docx"
$pdf = Join-Path (Split-Path -Parent $PSScriptRoot) "Manual_Tecnico_ProyecTwin_Anexo_G_verificacion.pdf"

if (-not (Test-Path $ruta)) {
    Write-Output "ERROR: no existe $ruta"
    exit 1
}
if (Test-Path $pdf) { Remove-Item $pdf -Force }

$word = New-Object -ComObject Word.Application
$word.Visible = $false
$word.DisplayAlerts = 0
try {
    $doc = $word.Documents.Open($ruta, $false, $true)
    $doc.SaveAs2([string]$pdf, [int]17)
    $doc.Close($false)
    if (Test-Path $pdf) {
        Write-Output ("PDF exportado: {0} ({1:N1} MB)" -f $pdf, ((Get-Item $pdf).Length / 1MB))
    } else {
        Write-Output "ERROR: el PDF no se creó"
    }
} finally {
    try { $word.Quit() } catch {}
}
