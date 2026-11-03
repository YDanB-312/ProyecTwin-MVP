# -*- coding: utf-8 -*-
"""validar_anexo_g.py — Validación de contenido y PDF del Anexo G."""
import os
import sys

from docx import Document
from pypdf import PdfReader

AQUI = os.path.dirname(os.path.abspath(__file__))
RAIZ = os.path.abspath(os.path.join(AQUI, ".."))
DOCX = os.path.join(RAIZ, "Manual_Tecnico_ProyecTwin_Anexo_G.docx")
PDF = os.path.join(RAIZ, "Manual_Tecnico_ProyecTwin_Anexo_G_verificacion.pdf")

ok, avisos = [], []

if not os.path.exists(DOCX):
    print("FALLA: no existe " + DOCX)
    sys.exit(1)

doc = Document(DOCX)
texto = "\n".join(p.text for p in doc.paragraphs)
for tabla in doc.tables:
    for fila in tabla.rows:
        for celda in fila.cells:
            texto += "\n" + celda.text

print(f"DOCX: {DOCX}")
print(f"Párrafos: {len(doc.paragraphs)} | Tablas: {len(doc.tables)} | Secciones: {len(doc.sections)}")

normas = [
    "ISO/IEC 27001", "ISO 14001", "ISO/IEC 25010", "ISO/IEC 9126-1",
    "ISO 37001", "ISO 10005", "ISO 9001", "ISO/IEC 33000",
]
for norma in normas:
    if norma in texto:
        ok.append(f"Referencia presente: {norma}")
    else:
        avisos.append(f"Falta la referencia: {norma}")

claves = {
    "corte 99806b5": "99806b5",
    "CI verificada": "41572290608",
    "backend 312": "312",
    "E2E 25/25": "25/25",
    "Android 196": "196",
    "Cap. 57": "Cap. 57",
    "no certificación": "no certifica",
}
for etiqueta, aguja in claves.items():
    if aguja.lower() in texto.lower():
        ok.append(f"Clave presente: {etiqueta}")
    else:
        avisos.append(f"Clave ausente: {etiqueta}")

if "actualice el campo" in texto.lower():
    avisos.append("Quedó un marcador de campo sin actualizar")
else:
    ok.append("Sin marcadores de campo pendientes")

if "244 pruebas superadas" in texto and "Afirmación en el Manual v1.0" in texto:
    ok.append("Conteo histórico 244 ubicado en la tabla de correcciones")
elif "244 pruebas superadas" in texto:
    avisos.append("Conteo histórico 244 fuera de contexto")

if not os.path.exists(PDF):
    avisos.append("No existe el PDF de verificación")
else:
    lector = PdfReader(PDF)
    paginas = len(lector.pages)
    textos = [(p.extract_text() or "").strip() for p in lector.pages]
    vacias = [i + 1 for i, t in enumerate(textos) if len(t) < 50]
    print(f"PDF: {PDF}")
    print(f"Páginas: {paginas} | Vacías: {vacias or 'ninguna'}")
    if paginas >= 8:
        ok.append(f"PDF con {paginas} páginas")
    else:
        avisos.append(f"PDF con solo {paginas} páginas")
    if not vacias:
        ok.append("PDF sin páginas vacías")
    else:
        avisos.append(f"PDF con páginas vacías: {vacias}")
    pdf_texto = "\n".join(textos)
    for aguja in ("ISO 37001", "41572290608", "25/25", "no certifica"):
        if aguja.lower() in pdf_texto.lower():
            ok.append(f"PDF contiene: {aguja}")
        else:
            avisos.append(f"PDF no contiene: {aguja}")

print("\n--- RESULTADOS ---")
for item in ok:
    print("OK  ", item)
for item in avisos:
    print("AVISO", item)
print(f"\nTotal OK: {len(ok)} | Avisos: {len(avisos)}")
sys.exit(1 if avisos else 0)
