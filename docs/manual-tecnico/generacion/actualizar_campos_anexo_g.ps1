# actualizar_campos_anexo_g.ps1
# Actualiza los campos automáticos (PAGE, SEQ de tablas) del Anexo G.
# Uso: powershell -ExecutionPolicy Bypass -File actualizar_campos_anexo_g.ps1
$ErrorActionPreference = "Stop"
$ruta = Join-Path (Split-Path -Parent $PSScriptRoot) "Manual_Tecnico_ProyecTwin_Anexo_G.docx"

if (-not (Test-Path $ruta)) {
    Write-Output "ERROR: no existe $ruta"
    exit 1
}

$word = New-Object -ComObject Word.Application
$word.Visible = $false
$word.DisplayAlerts = 0
try {
    $doc = $word.Documents.Open($ruta, $false, $false)
    $doc.Fields.Update() | Out-Null
    foreach ($toc in $doc.TablesOfContents) { $toc.Update() | Out-Null }
    foreach ($tof in $doc.TablesOfFigures) { $tof.Update() | Out-Null }
    $doc.Repaginate()
    $paginas = $doc.ComputeStatistics(2)
    $palabras = $doc.ComputeStatistics(0)
    $doc.Save()
    $doc.Close()
    Write-Output "Campos actualizados. Páginas: $paginas | Palabras: $palabras"
} finally {
    try { $word.Quit() } catch {}
    try { [System.Runtime.InteropServices.Marshal]::ReleaseComObject($word) | Out-Null } catch {}
}
