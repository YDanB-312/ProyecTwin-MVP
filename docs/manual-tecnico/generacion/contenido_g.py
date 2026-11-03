# -*- coding: utf-8 -*-
"""Anexo G — Aplicación de las normas ISO al desarrollo de ProyecTwin.

Se puede emitir como documento independiente (`escribir`) o como anexo
integrado al final del Manual Técnico (`escribir_integrado`). No reescribe los
capítulos 1–8 ni los anexos A–F: actualiza el apartado 7.15 y deja trazabilidad
técnica verificable, con límites explícitos.
"""
from docx.shared import Pt, Cm
from docx.enum.text import WD_ALIGN_PARAGRAPH


def _portada(M, P):
    # ================================================================ PORTADA
    for _ in range(2):
        M.p("", espacio_despues=0)

    par = M.p("SERVICIO NACIONAL DE APRENDIZAJE – SENA", negrita=True, centrado=True, espacio_despues=2)
    for r in par.runs:
        r.font.size = Pt(14)
    M.p("República de Colombia", centrado=True, espacio_despues=18)

    par = M.doc.add_paragraph()
    par.alignment = WD_ALIGN_PARAGRAPH.CENTER
    par.add_run().add_picture(P["logo"], width=Cm(6.2))

    M.p("", espacio_despues=10)
    p = M.p("ANEXO G", negrita=True, centrado=True, espacio_despues=4)
    for r in p.runs:
        r.font.size = Pt(18)
    p = M.p("Aplicación de las normas ISO al desarrollo de ProyecTwin", negrita=True, centrado=True, espacio_despues=4)
    for r in p.runs:
        r.font.size = Pt(14)
    M.p("Documento complementario del Manual Técnico PT-MT-001 · Integrado también en la versión 1.1", centrado=True, cursiva=True, espacio_despues=18)
    M.p("Versión 1.0 · 2 de noviembre de 2026", centrado=True, negrita=True, espacio_despues=4)
    M.p("Popayán, Cauca — Colombia", centrado=True, espacio_despues=24)
    M.p("Este anexo actualiza el apartado 7.15 del Manual Técnico v1.0 (16-oct-2026). "
        "No certifica conformidad, no sustituye una auditoría y no declara un sistema de gestión implantado.",
        centrado=True, cursiva=True, espacio_despues=0)


def _seccion(M, integrado, texto_standalone, texto_integrado):
    if integrado:
        M.h2(texto_integrado)
    else:
        M.h1(texto_standalone)


def escribir(M, P, integrado=False):
    if not integrado:
        _portada(M, P)
        sec = M.salto_seccion()
        M.encabezado_y_pie(sec, "Manual Técnico — ProyecTwin · Anexo G", numeracion="decimal", inicio=1)
    else:
        M.h1("ANEXO G. APLICACIÓN DE LAS NORMAS ISO AL DESARROLLO", salto=True)

    _seccion(M, integrado, "Ficha de identificación del anexo", "G.1. Ficha de identificación del Anexo G")
    M.tabla(
        "Ficha técnica del Anexo G",
        ["Campo", "Valor"],
        [
            ("Nombre del documento", "Anexo G — Aplicación de las normas ISO al desarrollo de ProyecTwin"),
            ("Identificador documental", "PT-MT-001-Anexo G"),
            ("Versión", "1.0"),
            ("Fecha de elaboración", "2 de noviembre de 2026"),
            ("Estado", "Anexo complementario para revisión; no certifica conformidad"),
            ("Documento base", "Manual Técnico PT-MT-001 v1.1 (este documento), apartado 7.15" if integrado else "Manual Técnico PT-MT-001 v1.0 (16-oct-2026), apartado 7.15; integrado en la v1.1 (2-nov-2026)"),
            ("Ubicación", "Anexo G, al final de este manual, después de los anexos A–F" if integrado else "Documento independiente, complementario del Manual Técnico"),
            ("Documento relacionado", "Manual de usuario del frontend v1.0 (nov-2026), PROYECTWIN-MU-FE-001, Parte VI, Cap. 57, pp. 174–180"),
            ("Corte de evidencia", "Commit 99806b5 (2-nov-2026); revisión estática y documental; consulta a la API de GitHub Actions"),
            ("Alcance", "Ocho referencias normativas aplicadas al desarrollo: evidencia, estado y brechas"),
            ("Fuera de alcance", "Auditoría formal, certificación, SGSI/SGA/SGAS/SGC, producción, dispositivo Android físico, Railway y Brevo"),
            ("Responsable", "Equipo ProyecTwin; generación documental asistida y revisión del equipo"),
        ],
        fuente="Elaboración propia a partir del repositorio y de los reportes fechados que se citan.",
        anchos=[4.4, 12.2],
    )

    if integrado:
        M.h2("G.2. Resumen ejecutivo")
    else:
        M.h2("Resumen ejecutivo")
    M.bullets([
        "**Prácticas reales, no certificación.** ProyecTwin aplica prácticas parciales de seguridad, calidad de producto, planificación de pruebas y control documental alineadas con ISO/IEC 27001, ISO/IEC 25010, ISO 10005 e ISO 9001. No existe un sistema de gestión certificado ni una conformidad integral.",
        "**Dos normas sin aplicación formal.** ISO 14001 e ISO 37001 no tienen política, objetivos, riesgos, métricas ni auditoría propia. La digitalización de manuales y la bitácora son prácticas indirectas o controles genéricos, no un SGA ni un SGAS.",
        "**Una referencia histórica y una familia conceptual.** ISO/IEC 9126-1:2001 está retirada y se cita solo como antecedente. La familia ISO/IEC 33000 se usó como vocabulario para ordenar procesos; no hubo evaluación de capacidad.",
        "**Corrección del registro histórico.** Este anexo corrige afirmaciones del Manual Técnico v1.0 sobre login por `username`, PDF de credenciales, CI no ejecutado, conteos de pruebas y ausencia de pruebas Android. El cuerpo histórico se conserva como snapshot; este anexo es la referencia vigente para ISO.",
    ])

    _seccion(M, integrado, "1. Alcance, método y declaración de no certificación", "G.3. Alcance, método y declaración de no certificación")
    M.p("Este anexo responde a la indicación académica de aplicar las normas ISO a los manuales técnicos y de usuario. "
        "La explicación narrativa, pensada para el lector del manual de usuario, está en la Parte VI, Cap. 57. "
        "Aquí se aporta la trazabilidad técnica: qué práctica existe, dónde está, qué evidencia la respalda, qué estado tiene y qué brecha queda.")
    if integrado:
        M.p("El cuerpo de este manual (capítulos 1–8 y anexos A–F) conserva el **snapshot histórico** del 16-oct-2026. Su apartado 7.15 era un análisis de pertinencia y contenía datos que hoy no corresponden al sistema: login por `username`, contraseñas temporales y PDF de credenciales, CI no ejecutado y conteos de pruebas anteriores. Este Anexo G, incorporado al final, **sustituye ese apartado** para efectos de la aplicación ISO. Los capítulos 1–8 y los anexos A–F se conservan sin reescritura para no convertir un documento histórico en una versión ficticia del estado actual.")
    else:
        M.p("El Manual Técnico v1.0 es un **snapshot histórico** del 16-oct-2026. Su apartado 7.15 era un análisis de pertinencia y contenía datos que hoy no corresponden al sistema: login por `username`, contraseñas temporales y PDF de credenciales, CI no ejecutado y conteos de pruebas anteriores. Este anexo **sustituye ese apartado** para efectos de la aplicación ISO. Los capítulos 1–8 y los anexos A–F se conservan sin reescritura para no convertir un documento histórico en una versión ficticia del estado actual.")
    M.p("**Método.** Revisión estática y documental del repositorio en el commit `99806b5` (2-nov-2026), contraste con los reportes de pruebas y con la API pública de GitHub Actions. No se reejecutaron las suites, no se abrió la base de datos, no se accedió a producción, a Railway, a Brevo ni a secretos reales. Por tanto, «comprobado» significa que existe evidencia versionada o publicada; no significa que este anexo la haya vuelto a ejecutar.")
    M.tabla(
        "Estados de evidencia usados en este anexo",
        ["Estado", "Significado"],
        [
            ("Aplicado y comprobado", "La práctica está implementada y existe evidencia verificable: prueba, registro, configuración o ejecución publicada."),
            ("Aplicado parcialmente", "La práctica existe, pero no cubre todos los aspectos de la referencia o no tiene evaluación formal."),
            ("Considerado como criterio de diseño", "Influyó en decisiones técnicas o metodológicas, sin implementación completa ni medición."),
            ("Sin evidencia suficiente", "No se encontró evidencia; no se atribuye la práctica al proyecto."),
            ("Referencia histórica", "Norma retirada o sustituida; se cita solo para explicar su relación con el modelo vigente."),
        ],
        fuente="Adaptado de la leyenda del Cap. 57 del manual de usuario.",
        anchos=[4.0, 12.6],
    )
    M.p("**Declaración expresa:** nada de este anexo permite afirmar que ProyecTwin «cumple», «es conforme» o «está certificado» frente a una norma ISO. Se documentan prácticas, evidencia y brechas. La certificación, si se busca, exige un sistema de gestión formal, auditoría independiente y decisión de la organización.")

    _seccion(M, integrado, "2. Correspondencia con el manual de usuario", "G.4. Correspondencia con el manual de usuario")
    M.p("El manual de usuario y este anexo no duplican contenido: se reparten el relato y la evidencia. El Cap. 57 explica qué se aplicó y con qué límites; el Anexo G permite a un evaluador técnico comprobar dónde está la práctica y qué falta.")
    M.tabla(
        "Correspondencia entre el Cap. 57 y el Anexo G",
        ["Documento", "Contenido", "Uso"],
        [
            ("Manual de usuario, Parte VI, Cap. 57 (pp. 174–180)", "Narrativa por norma, matriz resumida, conclusiones y referencias verificadas.", "Lectura funcional y académica; no requiere conocimientos de código."),
            ("Anexo G (este documento)", "Evidencia técnica, corrección del registro histórico, brechas y acciones.", "Evaluación técnica y trazabilidad; complementa el Cap. 57."),
            ("Manual Técnico v1.0, §7.15", "Análisis de pertinencia del 16-oct-2026.", "Se conserva como registro histórico; para ISO rige este Anexo G."),
        ],
        fuente="Elaboración propia.",
        anchos=[5.0, 6.8, 4.8],
    )
    M.p("**Nota de denominación.** El Cap. 57 trabaja con ocho referencias: siete normas y una familia (ISO/IEC 33000). ISO/IEC 27000 se usa solo como vocabulario de apoyo y no se cuenta como una de las ocho. En ISO/IEC 25010:2023 la característica que en 2011 se llamaba «usabilidad» se denomina **capacidad de interacción**; en ISO 9001:2026 la terminología vigente es **información documentada**, no «control documental»; y MFA no es un requisito universal de ISO/IEC 27001, sino una medida recomendable según el riesgo.")

    _seccion(M, integrado, "3. Matriz de aplicación de las ocho referencias normativas", "G.5. Matriz de aplicación de las ocho referencias normativas")
    M.p("La matriz siguiente es el núcleo del anexo. Cada fila indica la referencia y su edición, la práctica observada, la evidencia principal, el estado y la brecha. Las rutas son relativas a la raíz del repositorio.")
    M.tabla(
        "Matriz de aplicación, evidencia y límites",
        ["Referencia y edición", "Aplicación en el desarrollo", "Evidencia principal", "Estado", "Brecha / límite"],
        [
            (
                "**ISO/IEC 27001:2022** + Amd 1:2024",
                "Controles de autenticación por correo, autorización por roles y alcance, sesión web/token Android, límites de intentos, ocultamiento de PII, cabeceras y bitácora. No hay SGSI.",
                "`AuthController`, `Kernel`, `routes/api-v1.php`, `Support/Auditoria`, `Support/Pii`, pruebas de autenticación/autorización, E2E `acceso` y `correo`; CI 9/9.",
                "Aplicado parcialmente; controles con pruebas versionadas.",
                "Sin SGSI, análisis de riesgos, Declaración de Aplicabilidad, auditoría interna, revisión por la dirección, MFA (medida según riesgo, no requisito universal), revisión periódica de accesos, backup/DR, respuesta a incidentes, gestión continua de vulnerabilidades ni verificación del correo nuevo.",
            ),
            (
                "**ISO 14001:2026**",
                "Digitalización de manuales y flujos; reducción indirecta de papel. No hay sistema de gestión ambiental.",
                "Manual HTML/PDF y flujos electrónicos; ausencia de política, aspectos ambientales, objetivos y métricas.",
                "Sin evidencia formal; práctica indirecta.",
                "No se atribuye aplicación. Faltan política, aspectos/impactos, requisitos legales, objetivos, controles operacionales, medición, auditoría y revisión por la dirección.",
            ),
            (
                "**ISO/IEC 25010:2023**",
                "Criterios de calidad: adecuación funcional, capacidad de interacción, fiabilidad, seguridad, compatibilidad, mantenibilidad y flexibilidad. Sin evaluación formal del modelo.",
                "Suites backend/frontend/Android/E2E, lint, build, pruebas puntuales de accesibilidad y revisión de arquitectura.",
                "Aplicado parcialmente; criterio de diseño y verificación.",
                "Sin métricas del modelo, evaluación formal, auditoría WCAG completa, pruebas de carga, contratos OpenAPI ni cobertura. «Usabilidad» es terminología de 2011; en 2023 es «capacidad de interacción».",
            ),
            (
                "**ISO/IEC 9126-1:2001**",
                "Referencia histórica; no se usó como norma de trabajo.",
                "Bibliografía y comparación con ISO/IEC 25010.",
                "Referencia histórica; no aplicado.",
                "Retirada el 13-09-2012; sustituida por ISO/IEC 25010:2011 y luego por 25010:2023. No debe usarse para declarar conformidad.",
            ),
            (
                "**ISO 37001:2025**",
                "No hay sistema antisoborno. La bitácora y el soporte son controles genéricos de trazabilidad, no controles antisoborno.",
                "`audit_logs`, `bug_reports`, roles y permisos; ausencia de política, riesgos, terceros y canal ético.",
                "Sin evidencia formal; controles no equivalentes.",
                "Faltan política, liderazgo, riesgos de soborno, debida diligencia de terceros, regalos/hospitalidad, conflictos de interés, formación, canal de denuncias confidencial, investigación y protección contra represalias.",
            ),
            (
                "**ISO 10005:2018** (corregida en 2019 solo en la versión en español; confirmada en 2023)",
                "Planificación de la verificación: suites, gate E2E, lint/build, CI y checklists de operación.",
                "`CI.md`, `.github/workflows/ci.yml`, `docs/E2E-GATE.md`, READMEs, checklist de instalación.",
                "Aplicado parcialmente.",
                "Sin plan de calidad formal por release con alcance, responsables, criterios de aceptación, calendario, riesgos y firma. El CI no ejecuta E2E, Android ni auditoría de dependencias.",
            ),
            (
                "**ISO 9001:2026**",
                "Prácticas de control documental, historial Git, pruebas, soporte, auditorías internas y mejora continua.",
                "Git (138 commits), manuales, bitácora, soporte, suites y auditorías internas.",
                "Aplicado parcialmente.",
                "Sin SGC, política de calidad, objetivos medibles, revisión por la dirección, registro formal de no conformidades/CAPA, auditoría interna ISO 9001 ni certificación.",
            ),
            (
                "**Familia ISO/IEC 33000**: 33001:2015, 33002:2015, 33003:2015, 33004:2015 (corregida en 2017), 33020:2019",
                "Referencia conceptual para ordenar procesos, roles, actividades, resultados y evidencias.",
                "Flujos documentados, checklist, pruebas y bitácora; no hay PRM/PAM, métricas ni assessment.",
                "Criterio de diseño; sin evaluación formal.",
                "Sin dueño de proceso, objetivos, métricas, muestreo, assessor competente, perfil de capacidad ni calificación. 33002, 33003, 33004 y 33020 no se aplicaron formalmente.",
            ),
        ],
        fuente="Revisión estática del repositorio (commit 99806b5), reportes de pruebas y catálogo ISO consultado en noviembre de 2026.",
        anchos=[2.9, 4.4, 4.1, 2.2, 3.0],
        tamano=7.5,
    )

    _seccion(M, integrado, "4. Correcciones al registro histórico del Manual Técnico v1.0", "G.6. Correcciones al registro histórico del Manual Técnico v1.0")
    M.p("El Manual Técnico v1.0 se conserva como snapshot, pero varias afirmaciones ya no describen el sistema. Esta tabla evita que un lector las tome como estado vigente. No se reescribió el cuerpo histórico; se corrige el registro en este anexo.")
    M.tabla(
        "Afirmaciones superadas y estado actual",
        ["Afirmación en el Manual v1.0", "Estado actual", "Evidencia", "Límite"],
        [
            (
                "Login por `username` (§7.2, RF-01).",
                "El login es exclusivamente por correo; `username` se rechaza como credencial.",
                "`AuthController.php:37-43`; `frontend/src/pages/public/Login.jsx`; `AuthContractTest`; E2E `acceso`.",
                "`username` sigue existiendo como identificador interno y dato único, no como credencial de acceso.",
            ),
            (
                "Contraseña temporal y PDF de credenciales (§7.2, §7.6, RF-04).",
                "El alta genera un enlace de activación; la recuperación usa un enlace. No se generan contraseñas temporales ni PDF de credenciales.",
                "`routes/api-v1.php:33-36`; `Services/ActivacionCuenta`; `Services/RecuperacionPassword`; `docs/manual-tecnico/LEEME.md:6-7`.",
                "Los enlaces dependen del correo; producción y Brevo no se validaron en esta revisión.",
            ),
            (
                "CI no ejecutado (`CI.md`, R-04).",
                "El workflow se ejecutó 9 veces, todas con éxito; la última corrida es `41572290608`, sobre `99806b5` (2-nov-2026).",
                "API de GitHub Actions; `.github/workflows/ci.yml`.",
                "No ejecuta E2E, Android, cobertura ni auditoría de dependencias; no hay evidencia de gate de PR.",
            ),
            (
                "244 pruebas backend, 60 frontend y 54 E2E.",
                "312 pruebas backend / 1.698 aserciones; 110 pruebas frontend / 31 archivos; E2E 25/25; Android 196 unitarias.",
                "`backend/README.md:230`; `frontend/README.md:91,100`; `docs/E2E-GATE.md`; reporte XML de Android.",
                "No es una corrida global única; los conteos se reportan con fecha y no se reejecutaron aquí. El reporte Android es local.",
            ),
            (
                "La app Android no tiene pruebas.",
                "Tiene 196 pruebas unitarias y 7 instrumentadas; la evidencia instrumentada es parcial.",
                "`Movil/app/src/test/`; reporte XML local; `Movil/app/src/androidTest/`.",
                "Sin CI Android; el XML instrumentado histórico registra un fallo; no se validó en dispositivo físico.",
            ),
            (
                "§7.15 es un análisis de pertinencia vigente.",
                "§7.15 se conserva como registro histórico; para ISO rige este Anexo G.",
                "Este documento.",
                "El resto del Manual Técnico v1.0 sigue siendo un snapshot del 16-oct-2026.",
            ),
            (
                "MFA como requisito de ISO/IEC 27001.",
                "MFA no es un requisito universal; es una medida recomendable según el riesgo (control A.8.5).",
                "ISO/IEC 27001:2022, Anexo A; Cap. 57 del manual de usuario.",
                "No se implementó MFA en ProyecTwin.",
            ),
        ],
        fuente="Contraste entre el Manual Técnico v1.0, el código actual y los reportes de noviembre de 2026.",
        anchos=[3.2, 5.2, 5.2, 3.0],
        tamano=7.5,
    )

    _seccion(M, integrado, "5. Evidencia ejecutada, evidencia reportada y límites", "G.7. Evidencia ejecutada, evidencia reportada y límites")
    M.p("No toda la evidencia tiene el mismo grado de actualidad. Esta tabla separa lo ejecutado y publicado de lo reportado en documentos, y declara el límite de cada fuente.")
    M.tabla(
        "Evidencia disponible y grado de verificación",
        ["Evidencia", "Resultado", "Fuente y fecha", "Límite"],
        [
            ("Integración continua", "9 corridas, 9 exitosas; última `41572290608` sobre `99806b5`.", "API de GitHub Actions, 2-nov-2026.", "No cubre E2E, Android, cobertura ni auditoría de dependencias."),
            ("Gate E2E", "11 specs / 25 pruebas, 25/25, exit 0; base desechable.", "`docs/E2E-GATE.md`, 2-nov-2026.", "Gate manual; logs fuera del repositorio; no está en CI."),
            ("Backend", "312 pruebas / 1.698 aserciones en 49 archivos.", "`backend/README.md`, Fase 6, 2-nov-2026.", "Reportado; no reejecutado en esta revisión; sin reporte JUnit versionado."),
            ("Frontend", "110 pruebas en 31 archivos.", "`frontend/README.md`, Fase 6, 2-nov-2026.", "Reportado; no reejecutado; sin umbral de cobertura."),
            ("Android", "196 pruebas unitarias; 7 instrumentadas.", "Reporte XML local, 3-nov-2026; `androidTest`.", "Sin CI Android; evidencia instrumentada parcial; sin dispositivo físico."),
            ("Lint y build", "ESLint sin hallazgos; build de producción generado.", "CI y `frontend/package.json`.", "Sin PHPStan/Psalm; Pint no es obligatorio; Android sin minificación."),
            ("Dependencias", "`composer audit`: 42 avisos en 11 paquetes; `npm audit`: 3 altas (dev).", "Auditoría del 16-oct-2026 (§7.8).", "Datos históricos; no hay Dependabot ni auditoría continua en CI."),
            ("Accesibilidad", "Prueba axe de tres reglas en gráficos; revisión visual 5/5 documentada.", "`Charts.test.jsx`; `docs/E2E-GATE.md`.", "Sin auditoría WCAG completa ni axe en E2E."),
            ("Seguridad", "Pruebas de autenticación, autorización, sesiones, PII y bitácora.", "Suites del backend; E2E de acceso y correo.", "Sin pentest, SAST/DAST, backup/restore ni validación en producción."),
            ("Producción y despliegue", "Configuración documentada; sin ejecución verificada en esta revisión.", "`docs/deploy-railway.md`; `docs/release-android.md`.", "Railway, Brevo, APK y dispositivo físico no validados aquí."),
        ],
        fuente="Reportes y documentos del repositorio; consulta a GitHub Actions del 2-nov-2026.",
        anchos=[3.0, 5.4, 4.6, 3.6],
        tamano=7.5,
    )

    _seccion(M, integrado, "6. Brechas y acciones recomendadas", "G.8. Brechas y acciones recomendadas")
    M.p("Las acciones siguientes son propuestas de mejora, no actividades ejecutadas. Se ordenan por prioridad; la asignación de responsables queda pendiente de decisión del equipo y de la dirección del proyecto.")
    M.tabla(
        "Brechas, acciones y prioridad",
        ["Brecha", "Referencia", "Acción recomendada", "Prioridad", "Responsable"],
        [
            ("SGSI, análisis de riesgos, Declaración de Aplicabilidad y auditoría interna.", "ISO/IEC 27001", "Formalizar el SGSI o declarar explícitamente que queda fuera del alcance del MVP.", "P1", "Dirección / equipo"),
            ("MFA, revisión periódica de accesos, backup/restore, continuidad y respuesta a incidentes.", "ISO/IEC 27001", "Plan de endurecimiento y continuidad con responsables, pruebas y registro.", "P1–P2", "Equipo técnico"),
            ("Sistema de gestión ambiental.", "ISO 14001", "Decidir si aplica a la organización; no atribuirlo al producto software.", "P2", "Dirección"),
            ("Sistema antisoborno, riesgos, terceros y canal ético.", "ISO 37001", "Política, evaluación de riesgos, debida diligencia y canal confidencial.", "P2", "Dirección"),
            ("SGC, política, objetivos y revisión por la dirección.", "ISO 9001", "Definir política y objetivos medibles; programar revisión por la dirección y CAPA.", "P1", "Dirección"),
            ("Plan de calidad formal por release.", "ISO 10005", "Plantilla con alcance, roles, criterios de aceptación, riesgos, calendario y firma.", "P1", "Equipo"),
            ("Evaluación de procesos y capacidad.", "ISO/IEC 33000", "Definir dueño, métricas, PRM/PAM y, si se requiere, assessor competente.", "P2", "Dirección"),
            ("Métricas de calidad de producto, WCAG, carga, OpenAPI y cobertura.", "ISO/IEC 25010", "Plan de medición y auditoría; no declarar evaluación formal sin datos.", "P2", "Equipo"),
            ("CI incompleto: sin E2E, Android, cobertura ni auditoría de dependencias.", "ISO 10005 / 9001", "Ampliar el workflow y publicar el reporte de cada release.", "P1", "Equipo"),
            ("Documentación ISO sin versionar en Git.", "ISO 9001", "Commit y revisión del Cap. 57, PDF, README y este anexo; una sola fuente de verdad.", "P1", "Equipo"),
        ],
        fuente="Análisis propio a partir de las brechas identificadas en los informes de investigación.",
        anchos=[4.8, 2.4, 6.2, 1.4, 1.6],
        tamano=7.5,
    )

    _seccion(M, integrado, "7. Declaración de límites y vigencia", "G.9. Declaración de límites y vigencia")
    M.bullets([
        "**No certificación.** Este anexo no declara conformidad, certificación ni implantación de un sistema de gestión. Documenta prácticas, evidencia y brechas.",
        "**Corte temporal.** La evidencia principal corresponde al commit `99806b5` (2-nov-2026). El Manual Técnico v1.0 sigue siendo un snapshot del 16-oct-2026; sus datos no deben presentarse como estado actual.",
        "**Verificación limitada.** No se reejecutaron las suites, no se abrió la base de datos, no se accedió a producción, Railway, Brevo ni secretos, y no se probó un dispositivo Android físico. Los resultados de CI se consultaron en la API pública.",
        "**Uso previsto.** Este anexo forma parte del Manual Técnico v1.1 y acompaña al Cap. 57 del manual de usuario. Si la organización busca certificación, necesita auditoría independiente y sistemas de gestión formales." if integrado else "**Uso previsto.** Este anexo acompaña al Cap. 57 del manual de usuario y al Manual Técnico v1.0. Si la organización busca certificación, necesita auditoría independiente y sistemas de gestión formales.",
    ])

    _seccion(M, integrado, "Referencias", "G.10. Referencias")
    M.h3("Normas y catálogo ISO")
    M.bullets([
        "International Organization for Standardization. (2022). *ISO/IEC 27001:2022. Information security, cybersecurity and privacy protection — Information security management systems — Requirements* (Ed. 3; Amd 1:2024). https://www.iso.org/standard/27001",
        "International Organization for Standardization. (2026). *ISO 14001:2026. Environmental management systems — Requirements with guidance for use* (Ed. 4). https://www.iso.org/standard/14001",
        "International Organization for Standardization. (2023). *ISO/IEC 25010:2023. Systems and software engineering — Systems and software Quality Requirements and Evaluation (SQuaRE) — Product quality model* (Ed. 2). https://www.iso.org/standard/78176.html",
        "International Organization for Standardization. (2012). *ISO/IEC 9126-1:2001. Software engineering — Product quality — Part 1: Quality model* (retirada el 13-09-2012). https://www.iso.org/standard/22749.html",
        "International Organization for Standardization. (2025). *ISO 37001:2025. Anti-bribery management systems — Requirements with guidance for use* (Ed. 2). https://www.iso.org/standard/37001",
        "International Organization for Standardization. (2018). *ISO 10005:2018. Quality management — Guidelines for quality plans* (Ed. 3; corregida en 2019 solo en la versión en español; confirmada en 2023). https://www.iso.org/standard/70398.html",
        "International Organization for Standardization. (2026). *ISO 9001:2026. Quality management systems — Requirements* (Ed. 6). https://www.iso.org/standard/9001",
        "International Organization for Standardization. (2015). *ISO/IEC 33001:2015, 33002:2015, 33003:2015 y 33004:2015; ISO/IEC 33020:2019*. Familia de evaluación de procesos. https://www.iso.org/standard/54175.html",
    ])
    M.h3("Documentos internos y evidencia")
    M.bullets([
        "ProyecTwin. (2026). *Manual de usuario del frontend v1.0* [PROYECTWIN-MU-FE-001], Parte VI, Cap. 57, pp. 174–180. `frontend/public/manual/scripts/manual-data.js`.",
        "ProyecTwin. (2026). *Manual Técnico PT-MT-001 v1.0* [snapshot 16-oct-2026], §7.15. `docs/manual-tecnico/Manual_Tecnico_ProyecTwin.docx`.",
        "ProyecTwin. (2026). *Gate E2E — ejecuciones 6AN, 6AP y Fase 6* [2-nov-2026]. `docs/E2E-GATE.md`.",
        "ProyecTwin. (2026). *Entorno de pruebas seguro* [2-nov-2026]. `docs/entorno-pruebas-seguro.md`.",
        "ProyecTwin. (2026). *README del backend* [312 tests, 2-nov-2026]. `backend/README.md`.",
        "ProyecTwin. (2026). *README del frontend* [110 tests, 25 E2E, 2-nov-2026]. `frontend/README.md`.",
        "ProyecTwin. (2026). *Workflow de integración continua* [9 corridas exitosas; última 41572290608]. `.github/workflows/ci.yml`; https://github.com/YDanB-312/ProyecTwin-MVP/actions/runs/41572290608",
    ])
    M.p("**Nota final.** Las referencias y ediciones se contrastaron con el catálogo oficial de ISO en noviembre de 2026. Las descripciones de este anexo son redacción propia y no reproducen el texto normativo. Antes de una auditoría o certificación, confirma la edición vigente con el organismo correspondiente.")


def escribir_integrado(M, P):
    """Emite el Anexo G como última sección del Manual Técnico principal."""
    escribir(M, P, integrado=True)
