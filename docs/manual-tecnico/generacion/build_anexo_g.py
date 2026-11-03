# -*- coding: utf-8 -*-
"""
build_anexo_g.py — Genera el Anexo G del Manual Técnico de ProyecTwin.

Uso:
    python build_anexo_g.py

Genera:
    ../Manual_Tecnico_ProyecTwin_Anexo_G.docx
"""
import os
import re
import sys
import zipfile

AQUI = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, AQUI)

from manual_lib import Manual
import contenido_g

RAIZ_MANUAL = os.path.abspath(os.path.join(AQUI, ".."))
RAIZ_REPO = os.path.abspath(os.path.join(RAIZ_MANUAL, "..", ".."))
SALIDA = os.path.join(RAIZ_MANUAL, "Manual_Tecnico_ProyecTwin_Anexo_G.docx")

P = {
    "logo": os.path.join(RAIZ_REPO, "frontend", "public", "images", "Logo-ProyecTwin.png"),
}


def forzar_tema_arial(ruta_docx):
    """Sustituye las fuentes del tema por Arial para heredar la tipografía institucional."""
    temp = ruta_docx + ".tmp"
    with zipfile.ZipFile(ruta_docx, "r") as zin, zipfile.ZipFile(temp, "w", zipfile.ZIP_DEFLATED) as zout:
        for item in zin.infolist():
            datos = zin.read(item.filename)
            if item.filename == "word/theme/theme1.xml":
                texto = datos.decode("utf-8")
                texto = re.sub(r'(<a:majorFont>.*?<a:latin typeface=")[^"]*(")', r"\1Arial\2", texto, count=1, flags=re.S)
                texto = re.sub(r'(<a:minorFont>.*?<a:latin typeface=")[^"]*(")', r"\1Arial\2", texto, count=1, flags=re.S)
                datos = texto.encode("utf-8")
            zout.writestr(item, datos)
    os.replace(temp, ruta_docx)


def main():
    if not os.path.exists(P["logo"]):
        raise SystemExit(f"No existe el logo: {P['logo']}")

    M = Manual(RAIZ_MANUAL)
    contenido_g.escribir(M, P)
    ruta = M.guardar(SALIDA)
    forzar_tema_arial(ruta)
    print(f"DOCX generado: {ruta}")
    print(f"Figuras: {M.n_figuras} | Tablas: {M.n_tablas} | Fragmentos de código: {M.n_codigos}")


if __name__ == "__main__":
    main()
