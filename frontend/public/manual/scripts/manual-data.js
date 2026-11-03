/* Datos del Manual de usuario del frontend de ProyecTWIN.
   Fuente única para el sitio HTML, la búsqueda y el PDF. */

const fig = (src, alt, caption, pins = []) => ({ src, alt, caption, pins });

window.MANUAL_PARTS = [
  { id: 'parte-0', title: 'Antes de empezar', eyebrow: 'Parte 0 de 6', desc: 'Control documental, navegación, glosario y permisos.' },
  { id: 'parte-1', title: 'Parte I. Acceso y primeros pasos', eyebrow: 'Parte 1 de 6', desc: 'Portada, inicio de sesión, recuperación, activación y navegación general.' },
  { id: 'parte-2', title: 'Parte II. Aprendiz', eyebrow: 'Parte 2 de 6', desc: 'Ficha, propuestas, análisis, similitudes, alertas, soporte y perfil.' },
  { id: 'parte-3', title: 'Parte III. Instructor', eyebrow: 'Parte 3 de 6', desc: 'Revisión de propuestas, fichas, directorio, similitudes, alertas y perfil.' },
  { id: 'parte-4', title: 'Parte IV. Administrador', eyebrow: 'Parte 4 de 6', desc: 'Usuarios, fichas, propuestas, similitudes, soporte, bitácora, redes y motor.' },
  { id: 'parte-5', title: 'Parte V. Comportamientos comunes', eyebrow: 'Parte 5 de 6', desc: 'Tablas, filtros, formularios, errores, preguntas frecuentes y checklist.' },
  { id: 'parte-6', title: 'Parte VI. Calidad y normas', eyebrow: 'Parte 6 de 6', desc: 'Aplicación de las normas ISO en el desarrollo, la calidad y la documentación del proyecto.' },
  { id: 'anexos', title: 'Anexos', eyebrow: 'Material de consulta', desc: 'Mapa de rutas, permisos, campos, funcionalidades y figuras.' }
];

window.MANUAL_GLOSSARY = [
  { term: 'Propuesta', text: 'Idea de proyecto formativo que registra un aprendiz. Tiene título, descripción, objetivos, área, palabras clave, equipo, ficha y estado.' },
  { term: 'Ficha', text: 'Grupo de formación identificado por un código y un número. Relaciona aprendices, instructor, programa y propuestas.' },
  { term: 'Similitud', text: 'Porcentaje de coincidencia detectado por el motor entre dos propuestas del mismo programa. No es un veredicto de plagio.' },
  { term: 'Umbral', text: 'Porcentaje mínimo configurado para que una coincidencia se considere relevante.' },
  { term: 'Corpus', text: 'Conjunto de propuestas aprobadas y recientes que el motor usa como referencia para comparar.' },
  { term: 'Observación', text: 'Comentario que un instructor o administrador deja sobre una propuesta. El aprendiz puede responder.' },
  { term: 'Estado', text: 'Situación de una propuesta: Borrador, En revisión, Aprobado o Rechazado.' },
  { term: 'Rol', text: 'Permisos generales de una cuenta: aprendiz, instructor o administrador.' },
  { term: 'Dashboard', text: 'Panel inicial de cada rol con indicadores, accesos rápidos y actividad reciente.' },
  { term: 'Soporte', text: 'Formulario para reportar un error, conflicto con una ficha, problema de datos o duda.' },
  { term: 'Bitácora', text: 'Registro de acciones sensibles: quién hizo qué, cuándo y sobre qué entidad.' },
  { term: 'Red de conocimiento', text: 'Agrupación institucional que contiene programas de formación. Cada ficha pertenece a un programa de una red.' }
];

const commonErrors = [
  'Si aparece “No se pudo conectar con el servidor”, revisa tu conexión y vuelve a intentarlo.',
  'Si la sesión expiró, vuelve a iniciar sesión. No pierdas el texto que escribiste: cópialo antes de recargar.',
  'Si no ves un botón o una sección, probablemente tu rol no tiene permiso. Consulta la matriz de permisos.',
  'Si el problema continúa, usa Soporte y describe la pantalla, la acción y el mensaje exacto.'
];

window.MANUAL_CHAPTERS = [
  /* ------------------------------------------------------------------ PARTE 0 */
  {
    id: 'control-documental', num: 'Introducción', part: 'parte-0', roles: ['todos'], title: 'Control documental',
    purpose: 'Este manual explica el uso del frontend web de ProyecTWIN para personas sin experiencia técnica. No documenta el backend, la base de datos, la app Android ni la infraestructura.',
    route: 'No aplica. Es la ficha de control del documento.',
    whatYouSee: ['Título, versión, fecha y alcance del manual.', 'Público objetivo y límites del documento.', 'Fuente de verdad: la aplicación real y el código del frontend.'],
    steps: ['Lee esta ficha para saber qué encontrarás.', 'Usa la barra lateral o el buscador para ir a tu rol.', 'Si algo no coincide con la aplicación, prevalece la aplicación real.'],
    after: ['Al terminar el manual podrás localizar una funcionalidad, entender qué campos completar y comprobar si la operación quedó registrada.'],
    tip: 'No necesitas leerlo todo de principio a fin. Si ya conoces tu rol, usa el filtro lateral y ve directo al capítulo que necesitas.',
    errors: ['Si una pantalla cambió después de generar el manual, repórtalo para actualizar las capturas. No inventes pasos nuevos.'],
    related: [{ id: 'normas-iso', label: 'Aplicación de las normas ISO' }],
    extra: [{ title: 'Datos del documento', html: '<div class="table-wrap"><table><thead><tr><th>Dato</th><th>Valor</th></tr></thead><tbody><tr><td>Título</td><td>Manual de usuario del frontend de ProyecTWIN</td></tr><tr><td>Versión</td><td>1.0</td></tr><tr><td>Fecha</td><td>Noviembre de 2026</td></tr><tr><td>Audiencia</td><td>Público, aprendices, instructores y administradores</td></tr><tr><td>Alcance</td><td>Interfaz web React: zona pública y paneles por rol</td></tr><tr><td>Fuera de alcance</td><td>Backend, base de datos, API, app Android, Docker y producción</td></tr><tr><td>Datos de las capturas</td><td>Datos demo sintéticos del entorno de pruebas</td></tr></tbody></table></div>' }]
  },
  {
    id: 'como-usar-este-manual', num: 'Introducción', part: 'parte-0', roles: ['todos'], title: 'Cómo usar este manual',
    purpose: 'Explica las tres formas de lectura: completa, por rol o como consulta puntual. También describe los iconos y las capturas anotadas.',
    route: 'No aplica.',
    whatYouSee: ['Filtros por rol en la barra lateral.', 'Buscador con atajo / y resultados por capítulo.', 'Barra de progreso y botón “Marcar como leída”.', 'Pines numerados sobre las capturas y botón “Recorrer esta pantalla”.', 'Botón PDF y cambio de tema claro/oscuro.'],
    steps: ['Elige tu rol en la barra lateral o deja “Todos”.', 'Escribe una palabra en el buscador si necesitas algo concreto.', 'Haz clic en un pin para leer qué hace ese elemento.', 'Pulsa “Recorrer esta pantalla” para abrir el recorrido guiado paso a paso.', 'Marca los capítulos leídos para ver tu progreso.', 'Descarga el PDF si necesitas imprimir o entregar.'],
    after: ['El manual recuerda tu progreso en este navegador y mantiene tu tema claro u oscuro.'],
    tip: 'Atajos: / enfoca el buscador, T cambia el tema y Esc cierra ventanas o recorridos.',
    errors: ['Si el buscador no encuentra una palabra, prueba con un término más corto: “ficha”, “propuesta”, “usuario”, “similitud”.'],
    extra: [{ title: 'Cómo leer las instrucciones', html: '<div class="table-wrap"><table><thead><tr><th>Elemento</th><th>Significado</th></tr></thead><tbody><tr><td><strong>Negrita</strong></td><td>Nombre exacto de un botón, campo, menú o columna.</td></tr><tr><td><code>Ruta</code></td><td>Dirección web de la pantalla.</td></tr><tr><td>1, 2, 3</td><td>Pasos que se hacen en orden.</td></tr><tr><td>💡 Consejo</td><td>Recomendación para trabajar mejor.</td></tr><tr><td>⛔ Error</td><td>Qué hacer cuando algo falla.</td></tr><tr><td>Figura</td><td>Captura real de la aplicación con datos demo.</td></tr></tbody></table></div>' }]
  },
  {
    id: 'proyectwin-en-dos-minutos', num: 'Introducción', part: 'parte-0', roles: ['todos'], title: 'ProyecTWIN en dos minutos',
    purpose: 'Resume qué es ProyecTWIN, qué experiencias tiene y cómo se relacionan sus roles.',
    route: 'No aplica.',
    whatYouSee: ['ProyecTWIN organiza propuestas de proyecto formativo y detecta similitudes.', 'La zona pública presenta la plataforma y permite iniciar sesión.', 'El aprendiz registra propuestas y consulta similitudes.', 'El instructor revisa propuestas y gestiona fichas.', 'El administrador supervisa usuarios, catálogos, propuestas, similitudes, soporte y motor.'],
    steps: ['Identifica tu rol.', 'Consulta el capítulo de acceso si es tu primera vez.', 'Ve a la parte de tu rol.', 'Usa los flujos completos para entender una operación de principio a fin.'],
    after: ['Cada rol ve solo las secciones y datos que le corresponden.'],
    extra: [{ title: 'Mapa de experiencias', html: '<div class="table-wrap"><table><thead><tr><th>Rol</th><th>Qué hace</th><th>Entrada</th></tr></thead><tbody><tr><td><strong>Público</strong></td><td>Conoce ProyecTWIN, prueba la demo y entra al login.</td><td><code>/</code></td></tr><tr><td><strong>Aprendiz</strong></td><td>Se une a una ficha, crea propuestas, las envía y revisa similitudes.</td><td><code>/aprendiz/dashboard</code></td></tr><tr><td><strong>Instructor</strong></td><td>Revisa propuestas, aprueba o rechaza y gestiona sus fichas.</td><td><code>/instructor/dashboard</code></td></tr><tr><td><strong>Administrador</strong></td><td>Supervisa toda la plataforma y configura el motor.</td><td><code>/admin/dashboard</code></td></tr></tbody></table></div>' }]
  },
  {
    id: 'glosario', num: 'Introducción', part: 'parte-0', roles: ['todos'], title: 'Glosario',
    purpose: 'Reúne los términos que aparecen en la interfaz para que no tengas que adivinarlos.',
    route: 'No aplica.',
    whatYouSee: ['Términos de negocio: propuesta, ficha, similitud, umbral, corpus.', 'Estados y permisos.', 'Términos de soporte y auditoría.'],
    steps: ['Busca el término en esta página o usa el botón “Glosario” de la barra lateral.'],
    after: ['Si un término no aparece, usa el buscador del manual o consulta al administrador.'],
    extra: [{ title: 'Términos principales', html: '<div class="table-wrap"><table><thead><tr><th>Término</th><th>Qué significa</th></tr></thead><tbody><tr><td><strong>Propuesta</strong></td><td>Idea de proyecto formativo registrada por un aprendiz.</td></tr><tr><td><strong>Ficha</strong></td><td>Grupo de formación con código, número, programa, instructor y aprendices.</td></tr><tr><td><strong>Similitud</strong></td><td>Coincidencia detectada entre dos propuestas del mismo programa.</td></tr><tr><td><strong>Umbral</strong></td><td>Porcentaje mínimo para considerar relevante una coincidencia.</td></tr><tr><td><strong>Corpus</strong></td><td>Propuestas aprobadas y recientes que sirven de comparación.</td></tr><tr><td><strong>Observación</strong></td><td>Comentario del instructor o admin sobre una propuesta.</td></tr><tr><td><strong>Bitácora</strong></td><td>Registro de acciones sensibles de la plataforma.</td></tr><tr><td><strong>Red de conocimiento</strong></td><td>Agrupación que contiene programas y fichas.</td></tr></tbody></table></div>' }]
  },
  {
    id: 'perfiles-permisos', num: 'Introducción', part: 'parte-0', roles: ['todos'], title: 'Perfiles y permisos',
    purpose: 'Explica qué puede hacer cada rol y por qué algunas pantallas no aparecen para todos.',
    route: 'No aplica. La cuenta se crea desde el panel de administración.',
    whatYouSee: ['El rol se muestra en la barra superior junto al nombre.', 'El menú lateral cambia según el rol.', 'Si intentas abrir una ruta prohibida, aparece “403 · Acceso denegado”.'],
    steps: ['Identifica tu rol en la barra superior.', 'Usa solo las secciones de tu menú.', 'Si necesitas otro permiso, solicita al administrador que ajuste tu cuenta.'],
    after: ['Los permisos se validan en el frontend y en el backend; no basta con escribir una URL.'],
    errors: ['Si aparece “403”, vuelve a tu panel. No intentes cambiar el rol por tu cuenta.', 'Si tu cuenta está suspendida, el sistema no permitirá iniciar sesión.'],
    extra: [{ title: 'Resumen de permisos', html: '<div class="table-wrap"><table><thead><tr><th>Acción</th><th>Aprendiz</th><th>Instructor</th><th>Admin</th></tr></thead><tbody><tr><td>Crear propuesta</td><td>Sí, en su ficha</td><td>No</td><td>No desde el frontend</td></tr><tr><td>Revisar propuesta</td><td>No</td><td>Sí, las de sus fichas</td><td>Sí, todas</td></tr><tr><td>Crear ficha</td><td>No</td><td>Sí, asignada a sí mismo</td><td>Sí, elige instructor</td></tr><tr><td>Gestionar usuarios</td><td>No</td><td>No</td><td>Sí</td></tr><tr><td>Configurar motor</td><td>No</td><td>No</td><td>Sí</td></tr><tr><td>Ver bitácora</td><td>No</td><td>No</td><td>Sí</td></tr></tbody></table></div>' }]
  },

  /* ------------------------------------------------------------------ PARTE I */
  {
    id: 'home', num: 'Cap. 1', part: 'parte-1', roles: ['publico', 'todos'], title: 'La portada pública',
    purpose: 'Presenta ProyecTWIN, muestra el estado del motor y permite probar una comparación de idea sin iniciar sesión.',
    route: 'Abre <code>/</code> o pulsa el logo de ProyecTWIN.',
    whatYouSee: ['Barra institucional con el estado del motor y el botón de tema.', 'Título “¿Tu propuesta es original?” y botón “Iniciar sesión”.', 'Chips de resumen de propuestas y programas.', 'Terminal “proyectwin · demo en vivo” para escribir una idea.', 'Secciones de beneficios y pasos para empezar.'],
    figures: [fig('assets/capturas/01-home-publica.jpg', 'Portada pública de ProyecTWIN.', 'Portada pública en escritorio.', [{ n: 1, left: 55, top: 2, title: 'Barra institucional', text: 'Muestra el estado del motor y permite cambiar el tema.' }, { n: 2, left: 30, top: 24, title: 'Mensaje principal', text: 'Explica qué hace ProyecTWIN y ofrece el acceso al login.' }, { n: 3, left: 70, top: 26, title: 'Demo en vivo', text: 'Escribe una idea de al menos 3 caracteres para compararla con la base.' }, { n: 4, left: 25, top: 60, title: 'Beneficios', text: 'Resume detección, fichas y reportes.' }]), fig('assets/capturas/02-home-movil.jpg', 'Portada pública en móvil.', 'La misma portada en un teléfono.')],
    steps: ['Lee el mensaje principal y los beneficios.', 'Escribe una idea en el campo de la demo si quieres probar la comparación.', 'Observa el resultado: esperando, analizando o número de coincidencias.', 'Pulsa “Iniciar sesión” cuando tengas credenciales.'],
    after: ['La demo no guarda tu idea ni crea una propuesta. Solo compara el texto con la base pública.'],
    tip: 'La demo necesita al menos 3 caracteres y espera unos milisegundos antes de comparar.',
    errors: ['Si el motor muestra “NO DISPONIBLE”, la portada sigue funcionando, pero la demo puede no responder.', 'Si no aparece el resumen de propuestas o programas, la página muestra “—”; no es un error de tu cuenta.'],
    related: [{ id: 'login', label: 'Iniciar sesión' }, { id: 'errores-acceso', label: 'Errores de acceso' }]
  },
  {
    id: 'login', num: 'Cap. 2', part: 'parte-1', roles: ['publico', 'todos'], title: 'Iniciar sesión',
    purpose: 'Autentica tu cuenta y te lleva al panel correspondiente a tu rol.',
    route: 'Pulsa “Iniciar sesión” en la portada o abre <code>/login</code>.',
    whatYouSee: ['Título “Bienvenido de nuevo”.', 'Campo “Correo electrónico” para el correo registrado.', 'Campo “Contraseña” con botón para mostrarla u ocultarla.', 'Checkbox “Recordarme”.', 'Enlace “¿Olvidaste tu contraseña?”.'],
    figures: [fig('assets/capturas/03-login.jpg', 'Formulario de inicio de sesión.', 'Pantalla de inicio de sesión.', [{ n: 1, left: 50, top: 47, title: 'Correo electrónico', text: 'Escribe el correo registrado en tu cuenta.' }, { n: 2, left: 50, top: 61, title: 'Contraseña', text: 'Escribe tu contraseña. El ojo permite mostrarla.' }, { n: 3, left: 40, top: 66, title: 'Recordarme', text: 'Mantiene la sesión en este equipo según la configuración.' }, { n: 4, left: 50, top: 71, title: 'Entrar', text: 'Valida las credenciales y abre tu dashboard.' }])],
    fields: { head: ['Campo', 'Obligatorio', 'Qué escribir'], rows: [['Correo electrónico', 'Sí', 'Tu correo registrado.'], ['Contraseña', 'Sí', 'Tu contraseña personal.'], ['Recordarme', 'No', 'Márcalo solo en un equipo de confianza.']] },
    steps: ['Escribe tu correo electrónico.', 'Escribe tu contraseña.', 'Marca “Recordarme” si corresponde.', 'Pulsa “Iniciar Sesión”.', 'Espera el mensaje y la redirección.'],
    after: ['Aprendiz → dashboard de aprendiz; instructor → dashboard de instructor; admin → dashboard de administrador.', 'Si ya tenías sesión, el sistema te redirige automáticamente.'],
    tip: 'Si no recuerdas tu contraseña, usa el enlace de recuperación. No pidas a otra persona que la escriba por ti.',
    errors: ['“Ingresa tu correo y contraseña”: falta un campo.', '“Ingresa un correo electrónico válido”: el correo no tiene un formato válido.', 'Credenciales incorrectas o cuenta suspendida: revisa el mensaje y contacta al administrador.'],
    related: [{ id: 'recuperar-contrasena', label: 'Recuperar contraseña' }, { id: 'activar-cuenta', label: 'Activar cuenta' }]
  },
  {
    id: 'recuperar-contrasena', num: 'Cap. 3', part: 'parte-1', roles: ['publico', 'todos'], title: 'Recuperar contraseña',
    purpose: 'Solicita un enlace por correo para crear una nueva contraseña.',
    route: 'Abre <code>/recuperar-contrasena</code> o usa el enlace del login.',
    whatYouSee: ['Panel lateral con los tres pasos de recuperación.', 'Campo “Correo electrónico”.', 'Botón “Enviar enlace”.'],
    figures: [fig('assets/capturas/04-recuperar-contrasena.jpg', 'Formulario de recuperación de contraseña.', 'Solicitud de recuperación.')],
    fields: { head: ['Campo', 'Obligatorio', 'Validación'], rows: [['Correo electrónico', 'Sí', 'Debe tener formato de correo. Se envía en minúsculas y sin espacios.']] },
    steps: ['Escribe el correo con el que te registraron.', 'Pulsa “Enviar enlace”.', 'Revisa la bandeja de entrada y la carpeta de spam.', 'Abre el enlace del correo.'],
    after: ['Verás “Revisa tu correo”. Por seguridad, el mensaje no confirma si el correo existe.', 'El enlace lleva a la pantalla de nueva contraseña.'],
    tip: 'Si el correo no llega, espera unos minutos y revisa spam. No solicites muchos enlaces seguidos.',
    errors: ['Correo inválido: corrige el formato.', 'El enlace no llega: verifica que sea el correo registrado o pide soporte al administrador.'],
    related: [{ id: 'restablecer-contrasena', label: 'Restablecer contraseña' }, { id: 'login', label: 'Volver al login' }]
  },
  {
    id: 'restablecer-contrasena', num: 'Cap. 4', part: 'parte-1', roles: ['publico', 'todos'], title: 'Restablecer contraseña',
    purpose: 'Define una nueva contraseña desde el enlace de recuperación.',
    route: 'Abre el enlace del correo. La ruta es <code>/restablecer-contrasena</code> con token y correo.',
    whatYouSee: ['Correo electrónico precargado y de solo lectura.', 'Nueva contraseña y confirmación.', 'Botón “Restablecer contraseña”.'],
    figures: [fig('assets/capturas/05-restablecer-contrasena.jpg', 'Formulario para definir una nueva contraseña.', 'Formulario de nueva contraseña.')],
    fields: { head: ['Campo', 'Obligatorio', 'Validación'], rows: [['Correo electrónico', 'Sí', 'Viene en el enlace y no se puede editar.'], ['Nueva contraseña', 'Sí', 'Mínimo 12 caracteres; puedes usar espacios.'], ['Confirmar nueva contraseña', 'Sí', 'Debe coincidir exactamente.']] },
    steps: ['Abre el enlace del correo.', 'Escribe la nueva contraseña.', 'Repítela en la confirmación.', 'Pulsa “Restablecer contraseña”.', 'Cuando termine, pulsa “Ir al login” e inicia sesión.'],
    after: ['La contraseña anterior deja de funcionar. El token se retira de la URL por seguridad.'],
    tip: 'Usa una contraseña larga y distinta a la de otros servicios. No la compartas.',
    errors: ['“Enlace inválido”: solicita uno nuevo desde “Solicitar un nuevo enlace”.', '“Las contraseñas no coinciden”: vuelve a escribir la confirmación.', 'El enlace venció: solicita otro.'],
    related: [{ id: 'recuperar-contrasena', label: 'Solicitar otro enlace' }, { id: 'login', label: 'Ir al login' }]
  },
  {
    id: 'activar-cuenta', num: 'Cap. 5', part: 'parte-1', roles: ['publico', 'todos'], title: 'Activar cuenta',
    purpose: 'Valida el enlace de activación y permite que una cuenta nueva defina su contraseña inicial.',
    route: 'Abre el enlace de activación del correo. La ruta es <code>/activar-cuenta</code>.',
    whatYouSee: ['Estado “Validando enlace…”.', 'Si el token es válido: correo de solo lectura, nueva contraseña y confirmación.', 'Si el token no sirve: “Enlace inválido o vencido” con acciones.', 'Si la validación falla por red: botón “Reintentar”.'],
    figures: [fig('assets/capturas/06-activar-cuenta-invalida.jpg', 'Pantalla de activación con enlace inválido.', 'Estado de enlace inválido o vencido.')],
    fields: { head: ['Campo', 'Obligatorio', 'Validación'], rows: [['Correo electrónico', 'Sí', 'Viene de la validación del token; no se edita.'], ['Nueva contraseña', 'Sí', 'Mínimo 12 caracteres.'], ['Confirmar nueva contraseña', 'Sí', 'Debe coincidir.']] },
    steps: ['Abre el enlace del correo de activación.', 'Espera la validación.', 'Si el formulario aparece, escribe y confirma la contraseña.', 'Pulsa “Activar mi cuenta”.', 'Pulsa “Ir al login” y entra.'],
    after: ['La cuenta queda activa y ya puede iniciar sesión.'],
    tip: 'Si el enlace venció, no lo reenvíes manualmente: usa “Ir a recuperación” o pide al administrador un nuevo enlace.',
    errors: ['“Enlace inválido o vencido”: solicita uno nuevo.', '“No pudimos validar el enlace”: pulsa “Reintentar” o vuelve al login.', 'La contraseña no cumple: usa al menos 12 caracteres.'],
    related: [{ id: 'login', label: 'Iniciar sesión' }, { id: 'recuperar-contrasena', label: 'Recuperar acceso' }]
  },
  {
    id: 'navegacion-layout', num: 'Cap. 6', part: 'parte-1', roles: ['todos'], title: 'Navegación general y layout',
    purpose: 'Explica la barra superior, el menú lateral, el cambio de tema, las notificaciones y el comportamiento en móvil.',
    route: 'Aparece en todos los paneles privados después de iniciar sesión.',
    whatYouSee: ['Barra institucional con el estado del motor.', 'Barra superior con menú, logo, título, botón +, campana, avatar y “Salir”.', 'Menú lateral con las secciones de tu rol.', 'Botón de tema claro/oscuro.', 'Footer con Soporte y datos de contacto.'],
    figures: [fig('assets/capturas/08-aprendiz-dashboard.jpg', 'Panel de aprendiz con barra superior y menú lateral.', 'Estructura general de un panel privado.', [{ n: 1, left: 8, top: 30, title: 'Menú lateral', text: 'Cambia según el rol. La sección activa queda resaltada.' }, { n: 2, left: 50, top: 5, title: 'Barra superior', text: 'Muestra el título, el botón de creación rápida, la campana, el avatar y Salir.' }, { n: 3, left: 82, top: 5, title: 'Perfil y salida', text: 'El avatar lleva al perfil; “Salir” cierra la sesión.' }, { n: 4, left: 50, top: 25, title: 'Contenido', text: 'Cada pantalla muestra su título, acciones y datos.' }]), fig('assets/capturas/09-aprendiz-dashboard-movil.jpg', 'Panel de aprendiz en móvil.', 'El menú se convierte en un cajón lateral.')],
    steps: ['Usa el menú lateral para cambiar de sección.', 'Pulsa la campana para ir a Notificaciones.', 'Pulsa el avatar para abrir tu perfil.', 'Pulsa “Salir” para cerrar la sesión.', 'En móvil, abre el menú con el botón ☰.'],
    after: ['La sección activa se resalta y el título de la barra superior cambia.', 'El tema elegido se recuerda en el navegador.'],
    tip: 'El botón + crea el elemento principal de tu rol: propuesta, ficha o usuario.',
    errors: ['Si el menú no se cierra en móvil, toca fuera de él o pulsa de nuevo ☰.', 'Si una sección no aparece, revisa tu rol.'],
    related: [{ id: 'errores-acceso', label: 'Acceso denegado' }, { id: 'perfiles-permisos', label: 'Perfiles y permisos' }]
  },
  {
    id: 'errores-acceso', num: 'Cap. 7', part: 'parte-1', roles: ['todos'], title: 'Errores de acceso: 403 y 404',
    purpose: 'Explica qué hacer cuando una ruta no existe o cuando tu rol no tiene permiso.',
    route: 'Aparecen al escribir una URL incorrecta o al intentar abrir una sección de otro rol.',
    whatYouSee: ['403 · Acceso denegado: estás autenticado, pero no tienes permiso.', 'Botón “Volver a mi panel”.', '404 · Página no encontrada: la URL no existe o fue movida.', 'Botones “Volver al inicio” e “Iniciar sesión”.'],
    figures: [fig('assets/capturas/23-acceso-denegado.jpg', 'Pantalla 403 de acceso denegado.', 'Acceso denegado por rol.'), fig('assets/capturas/07-pagina-no-encontrada.jpg', 'Pantalla 404 de página no encontrada.', 'Página no encontrada.')],
    steps: ['Si ves 403, pulsa “Volver a mi panel”.', 'Si ves 404, revisa la dirección o vuelve al inicio.', 'No compartas una URL privada con otra persona: su rol puede no tener acceso.'],
    after: ['El sistema no muestra datos del recurso protegido.'],
    tip: 'Escribe las rutas desde el menú, no a mano, para evitar errores de dedo.',
    errors: ['Si necesitas acceso a otra sección, pide al administrador que revise tu rol.', 'Si la URL era correcta y ahora da 404, puede haber cambiado la ruta.'],
    related: [{ id: 'navegacion-layout', label: 'Navegación general' }, { id: 'perfiles-permisos', label: 'Permisos' }]
  },

  /* ------------------------------------------------------------------ PARTE II */
  {
    id: 'aprendiz-dashboard', num: 'Cap. 8', part: 'parte-2', roles: ['aprendiz'], title: 'Dashboard del aprendiz',
    purpose: 'Muestra el resumen de tus propuestas, similitudes y notificaciones, y ofrece accesos rápidos.',
    route: 'Inicia sesión como aprendiz o abre <code>/aprendiz/dashboard</code>.',
    whatYouSee: ['Saludo con tu nombre y fecha.', 'Tarjetas: propuestas registradas, aprobadas, similitudes y notificaciones sin leer.', 'Gráficos de estado y similitud máxima.', 'Acciones rápidas: nueva propuesta y marcar alertas.', 'Propuestas recientes y observaciones.'],
    figures: [fig('assets/capturas/08-aprendiz-dashboard.jpg', 'Dashboard del aprendiz.', 'Vista general del aprendiz.', [{ n: 1, left: 8, top: 32, title: 'Menú del aprendiz', text: 'Acceso a dashboard, propuestas, similitudes, ficha, notificaciones y perfil.' }, { n: 2, left: 35, top: 22, title: 'Métricas', text: 'Resumen de propuestas y alertas. Cada tarjeta es navegable.' }, { n: 3, left: 55, top: 55, title: 'Gráficos', text: 'Estado de propuestas y similitud máxima por propuesta.' }, { n: 4, left: 55, top: 78, title: 'Propuestas recientes', text: 'Abre el detalle de una propuesta.' }]), fig('assets/capturas/09-aprendiz-dashboard-movil.jpg', 'Dashboard del aprendiz en móvil.', 'Vista móvil del dashboard.'), fig('assets/capturas/24-aprendiz-dashboard-oscuro.jpg', 'Dashboard del aprendiz en tema oscuro.', 'El tema oscuro se conserva en el navegador.')],
    steps: ['Revisa las tarjetas superiores.', 'Usa “Nueva propuesta” si necesitas crear una.', 'Pulsa una propuesta reciente para abrirla.', 'Pulsa “Marcar alertas leídas” si tienes notificaciones pendientes.'],
    after: ['Las tarjetas y accesos te llevan a la sección correspondiente.', 'Las alertas marcadas dejan de contar como pendientes.'],
    tip: 'Si no tienes propuestas, el panel muestra un estado vacío con el botón “Crear propuesta”.',
    errors: ['Si los gráficos no aparecen, puede que aún no haya datos suficientes.', 'Si una tarjeta muestra 0, revisa si tienes propuestas o alertas reales.'],
    related: [{ id: 'aprendiz-propuestas', label: 'Mis propuestas' }, { id: 'aprendiz-alertas', label: 'Notificaciones' }]
  },
  {
    id: 'aprendiz-ficha', num: 'Cap. 9', part: 'parte-2', roles: ['aprendiz'], title: 'Mi Ficha: unirse o consultar',
    purpose: 'Permite unirte a una ficha con el código del instructor o consultar la ficha a la que ya perteneces.',
    route: 'Abre <code>/aprendiz/ficha</code> desde el menú lateral.',
    whatYouSee: ['Sin ficha: campo “Código de la ficha” y botón “Buscar ficha”.', 'Previsualización con nombre, código, estado, programa e instructor.', 'Con ficha: datos de la ficha, integrantes, buscador y botón “Salir de la ficha”.'],
    figures: [fig('assets/capturas/10-aprendiz-mi-ficha.jpg', 'Ficha actual del aprendiz.', 'Vista de una ficha con sus integrantes.', [{ n: 1, left: 35, top: 36, title: 'Datos de la ficha', text: 'Nombre, código, número, programa, instructor, aprendices y propuestas.' }, { n: 2, left: 93, top: 55, title: 'Salir de la ficha', text: 'Puedes salir cuando quieras; tus propuestas se conservan.' }, { n: 3, left: 30, top: 70, title: 'Integrantes', text: 'Busca por nombre o correo y abre el perfil de un compañero.' }]), fig('assets/capturas/20-aprendiz-detalle-ficha.jpg', 'Detalle de una ficha desde la vista del aprendiz.', 'Detalle de la ficha con sus propuestas.')],
    fields: { head: ['Campo', 'Obligatorio', 'Validación'], rows: [['Código de la ficha', 'Sí', 'Máximo 40 caracteres. No valida formato; busca el código exacto.'], ['Buscar integrante', 'No', 'Filtra por nombre o correo.']] },
    steps: ['Si no tienes ficha, escribe el código que te dio tu instructor.', 'Pulsa “Buscar ficha”.', 'Revisa la previsualización y pulsa “Unirme a esta ficha”.', 'Si ya tienes ficha, consulta los integrantes o pulsa “Salir de la ficha”.', 'Confirma la salida en el modal.'],
    after: ['Al unirte, la ficha aparece en tu perfil y puedes crear propuestas.', 'Al salir, tus propuestas anteriores se conservan en la ficha donde se crearon.', 'El botón “Ir a mis propuestas” te lleva a tu lista.'],
    tip: 'El código suele tener el formato xkp-mqwr. Cópialo exactamente como te lo compartió el instructor.',
    errors: ['“Ingresa el código de la ficha”: el campo está vacío.', '“No encontramos una ficha con ese código”: revisa mayúsculas, guiones y espacios.', 'La ficha no está activa: pide un código vigente.', 'No puedes unirte dos veces a la misma ficha.'],
    related: [{ id: 'aprendiz-propuestas', label: 'Crear una propuesta' }, { id: 'aprendiz-companeros', label: 'Perfiles de compañeros' }]
  },
  {
    id: 'aprendiz-propuestas', num: 'Cap. 10', part: 'parte-2', roles: ['aprendiz'], title: 'Mis Propuestas: listado y filtros',
    purpose: 'Consulta todas tus propuestas, filtra por estado y abre el detalle o envía un borrador.',
    route: 'Abre <code>/aprendiz/propuestas</code> desde el menú lateral.',
    whatYouSee: ['Título “Mis Propuestas” y botón “Nueva propuesta”.', 'Filtros de búsqueda y estado.', 'Tarjetas con título, estado, resumen, fecha, ficha, programa y similitud.', 'Botón “Enviar” en borradores o rechazadas de la ficha actual.', 'Paginación de 6 tarjetas.'],
    figures: [fig('assets/capturas/11-aprendiz-propuestas.jpg', 'Listado de propuestas del aprendiz.', 'Listado con filtros y tarjetas.', [{ n: 1, left: 30, top: 28, title: 'Filtros', text: 'Busca por título, resumen o palabras clave y filtra por estado.' }, { n: 2, left: 30, top: 50, title: 'Tarjeta de propuesta', text: 'Muestra estado, resumen, fecha, ficha y similitud.' }, { n: 3, left: 93, top: 15, title: 'Nueva propuesta', text: 'Abre el formulario si perteneces a una ficha.' }, { n: 4, left: 91, top: 92, title: 'Paginación', text: 'Cambia de página sin perder los filtros.' }])],
    fields: { head: ['Filtro', 'Qué hace'], rows: [['Buscar', 'Filtra por título, resumen y palabras clave; ignora mayúsculas y tildes.'], ['Estado', 'Todos, Borrador, En revisión, Aprobado o Rechazado.']] },
    steps: ['Escribe una palabra en Buscar si quieres acotar.', 'Elige un estado.', 'Pulsa una tarjeta para abrir el detalle.', 'Pulsa “Enviar” si la propuesta es un borrador o fue rechazada y pertenece a tu ficha activa.', 'Usa la paginación para ver más propuestas.'],
    after: ['Al enviar, el sistema te lleva a la pantalla de análisis.', 'Si la propuesta es de una ficha anterior, se conserva y se indica con “ficha anterior”.'],
    tip: 'El botón “Nueva propuesta” te lleva a Mi Ficha si aún no perteneces a una ficha.',
    errors: ['“Sin resultados”: limpia la búsqueda o cambia el estado.', 'No aparece “Enviar”: la propuesta no es editable o la ficha no está activa.', 'Error de envío: revisa el mensaje y vuelve a intentarlo.'],
    related: [{ id: 'aprendiz-crear-propuesta', label: 'Crear propuesta' }, { id: 'aprendiz-detalle-propuesta', label: 'Detalle de propuesta' }]
  },
  {
    id: 'aprendiz-crear-propuesta', num: 'Cap. 11', part: 'parte-2', roles: ['aprendiz'], title: 'Crear una propuesta',
    purpose: 'Registra una idea de proyecto, guarda un borrador o envíala al motor de similitud y al instructor.',
    route: 'Abre <code>/aprendiz/propuestas?crear=1</code> o pulsa “Nueva propuesta”.',
    whatYouSee: ['Formulario “Nueva Propuesta”.', 'Nombre, descripción, objetivo general, objetivos específicos.', 'Integrantes del equipo de tu ficha.', 'Área de aplicación, ficha informativa y palabras clave.', 'Botones “Enviar propuesta y analizar”, “Guardar borrador” y “Cancelar”.'],
    figures: [fig('assets/capturas/12-aprendiz-crear-propuesta.jpg', 'Formulario de creación de propuesta.', 'Formulario completo de propuesta.', [{ n: 1, left: 35, top: 31, title: 'Nombre', text: 'Entre 5 y 120 caracteres.' }, { n: 2, left: 30, top: 39, title: 'Descripción', text: 'Entre 20 y 600 caracteres. Explica problema, solución y beneficiarios.' }, { n: 3, left: 30, top: 62, title: 'Objetivos', text: 'Objetivo general y al menos 2 objetivos específicos, uno por línea.' }, { n: 4, left: 25, top: 75, title: 'Equipo', text: 'Selecciona compañeros de tu ficha. Es opcional.' }, { n: 5, left: 30, top: 86, title: 'Área y palabras clave', text: 'El área es obligatoria; las palabras clave son opcionales.' }, { n: 6, left: 45, top: 95, title: 'Acciones', text: 'Enviar y analizar, guardar borrador o cancelar.' }])],
    fields: { head: ['Campo', 'Obligatorio', 'Regla'], rows: [['Nombre de la propuesta', 'Sí', 'Mínimo 5 caracteres; máximo 120.'], ['Descripción del problema y la solución', 'Sí', 'Mínimo 20 caracteres; máximo 600; contador visible.'], ['Objetivo general', 'Sí', 'Mínimo 15 caracteres; máximo 300.'], ['Objetivos específicos', 'Sí', 'Mínimo 2 líneas no vacías; cada una con 8 caracteres o más.'], ['Integrantes del equipo', 'No', 'Solo compañeros de tu ficha; se excluye tu propia cuenta.'], ['Área de aplicación', 'Sí', 'Desarrollo Web, IA, Ciencia de Datos, Ciberseguridad u Otro.'], ['Palabras clave', 'No', 'Opcional; se separan por comas.'], ['Ficha de formación', 'Informativo', 'Se toma de tu perfil y no se edita.']] },
    steps: ['Escribe un nombre claro.', 'Describe el problema y cómo lo resuelve tu propuesta.', 'Escribe el objetivo general.', 'Escribe al menos dos objetivos específicos, uno por línea.', 'Selecciona compañeros si aplica.', 'Elige el área de aplicación.', 'Agrega palabras clave si quieres.', 'Pulsa “Guardar borrador” o “Enviar propuesta y analizar”.'],
    after: ['Guardar borrador no envía la propuesta ni ejecuta el análisis.', 'Enviar crea o actualiza el borrador, agrega el equipo, ejecuta el análisis y abre la pantalla de progreso.', 'Si el envío falla, el borrador se conserva para reintentar.'],
    tip: 'La descripción más útil explica tres cosas: qué problema existe, qué hará el software y quién se beneficia.',
    errors: ['“El nombre debe tener al menos 5 caracteres”.', '“La descripción debe tener al menos 20 caracteres”.', '“Escribe al menos 2 objetivos específicos”.', '“Cada objetivo específico debe tener al menos 8 caracteres”.', '“Selecciona un área de aplicación”.', 'Si no tienes ficha, el botón de creación te redirige a Mi Ficha.'],
    related: [{ id: 'aprendiz-detalle-propuesta', label: 'Detalle y envío' }, { id: 'aprendiz-analisis', label: 'Análisis de la propuesta' }]
  },
  {
    id: 'aprendiz-detalle-propuesta', num: 'Cap. 12', part: 'parte-2', roles: ['aprendiz'], title: 'Detalle de una propuesta',
    purpose: 'Consulta toda la información, edita mientras sea posible, envía, elimina si eres creador, comenta y revisa similitudes e historial.',
    route: 'Pulsa una tarjeta en <code>/aprendiz/propuestas</code>. La ruta es <code>/aprendiz/detalle-proyecto/:id</code>.',
    whatYouSee: ['Título, fecha, autor, ficha y estado.', 'Información del proyecto: descripción, objetivos, palabras clave e integrantes.', 'Botones Editar, Enviar y Eliminar según permisos.', 'Similitudes detectadas e historial.', 'Observaciones del instructor con opción de responder.'],
    figures: [fig('assets/capturas/13-aprendiz-detalle-propuesta.jpg', 'Detalle de una propuesta del aprendiz.', 'Detalle de propuesta con información, similitudes e historial.')],
    steps: ['Revisa el estado y los datos generales.', 'Pulsa “Editar” si la propuesta sigue siendo editable.', 'Pulsa “Enviar propuesta” si está en borrador o rechazada.', 'Lee las similitudes y el historial.', 'Responde una observación si necesitas aclarar algo.', 'Pulsa “Eliminar” solo si eres creador y estás seguro.'],
    after: ['Al editar, los cambios se guardan y se recarga el detalle.', 'Al enviar, la propuesta pasa a revisión y se ejecuta el análisis.', 'Al eliminar, la propuesta desaparece si el backend lo permite.'],
    tip: 'Antes de enviar, revisa que los objetivos específicos estén completos y que el equipo sea el correcto.',
    errors: ['Si no aparece “Editar”, la propuesta ya no es editable.', 'Si no aparece “Enviar”, no está en borrador/rechazada o la ficha no está activa.', 'Si una observación no se puede responder, recarga la página.'],
    related: [{ id: 'aprendiz-crear-propuesta', label: 'Crear propuesta' }, { id: 'aprendiz-similitudes', label: 'Similitudes' }]
  },
  {
    id: 'aprendiz-analisis', num: 'Cap. 13', part: 'parte-2', roles: ['aprendiz'], title: 'Análisis y resultado',
    purpose: 'Muestra el progreso del análisis y el resultado de similitud de una propuesta enviada.',
    route: 'Después de enviar una propuesta, o abre <code>/aprendiz/resultado-analisis?projectId=ID</code>.',
    whatYouSee: ['Pantalla “Analizando propuesta” con pasos y consejos.', 'Resultado con puntaje, nivel A/B/C y similitudes encontradas.', 'Enlaces a cada coincidencia y a la propuesta.'],
    figures: [fig('assets/capturas/14-aprendiz-resultado-analisis.jpg', 'Resultado del análisis de similitud.', 'Resultado del análisis con puntaje y coincidencias.')],
    steps: ['Espera a que termine el análisis.', 'Lee el porcentaje y el nivel.', 'Abre una coincidencia para comparar.', 'Si no hay coincidencias, el resultado indica que no se detectaron pares relevantes.'],
    after: ['El resultado queda asociado a la propuesta y puedes consultarlo después.', 'Las coincidencias se pueden abrir desde el detalle o desde Similitudes.'],
    tip: 'Un porcentaje alto no es una acusación automática: es una señal para revisar y citar fuentes.',
    errors: ['Si el análisis falla, pulsa “Reintentar”.', 'Si no hay projectId, el sistema te devuelve a Mis Propuestas.', 'Si no hay datos, revisa que la propuesta se haya enviado correctamente.'],
    related: [{ id: 'aprendiz-similitudes', label: 'Similitudes' }, { id: 'aprendiz-detalle-propuesta', label: 'Volver al detalle' }]
  },
  {
    id: 'aprendiz-similitudes', num: 'Cap. 14', part: 'parte-2', roles: ['aprendiz'], title: 'Similitudes del aprendiz',
    purpose: 'Agrupa las coincidencias de tus propuestas y permite filtrarlas por título y porcentaje mínimo.',
    route: 'Abre <code>/aprendiz/similitudes</code>.',
    whatYouSee: ['Título “Similitudes” con el total de coincidencias.', 'Buscador por título y campo “% mínimo”.', 'Ranking agrupado por tu propuesta.', 'Cada grupo se expande y muestra las coincidencias con su porcentaje.'],
    figures: [fig('assets/capturas/15-aprendiz-similitudes.jpg', 'Listado de similitudes del aprendiz.', 'Coincidencias agrupadas por propuesta.')],
    steps: ['Escribe el título de una propuesta si quieres filtrar.', 'Escribe un porcentaje mínimo, por ejemplo 40.', 'Expande un grupo.', 'Pulsa una coincidencia para abrir el detalle.'],
    after: ['Los filtros cambian el conteo y la lista sin modificar los datos.', 'Si no hay coincidencias, se muestra un mensaje tranquilizador.'],
    tip: 'Ordena mentalmente por porcentaje: las coincidencias más altas suelen requerir revisión primero.',
    errors: ['“Sin coincidencias con esos filtros”: limpia los filtros.', 'Si no hay propuestas, primero crea y envía una.'],
    related: [{ id: 'aprendiz-detalle-similitud', label: 'Detalle de similitud' }, { id: 'aprendiz-propuestas', label: 'Mis propuestas' }]
  },
  {
    id: 'aprendiz-detalle-similitud', num: 'Cap. 15', part: 'parte-2', roles: ['aprendiz'], title: 'Detalle de una similitud',
    purpose: 'Compara dos propuestas y revisa el porcentaje, las métricas, los términos y los fragmentos coincidentes.',
    route: 'Pulsa una coincidencia en <code>/aprendiz/similitudes</code>. La ruta es <code>/aprendiz/detalle-similitud/:id</code>.',
    whatYouSee: ['Umbral configurado y comparación A/B.', 'Tarjetas de las dos propuestas.', 'Métricas: palabras, caracteres, tema y cobertura.', 'Términos compartidos y fragmentos coincidentes.'],
    figures: [fig('assets/capturas/16-aprendiz-detalle-similitud.jpg', 'Detalle de comparación de similitud.', 'Comparación visual entre dos propuestas.')],
    steps: ['Lee el porcentaje y la nota de interpretación.', 'Revisa los datos de las dos propuestas.', 'Lee los términos y fragmentos compartidos.', 'Abre una propuesta si necesitas más contexto.'],
    after: ['La pantalla es de solo lectura; no cambia el estado de la propuesta.'],
    tip: 'La similitud es una medida técnica. No es una prueba de plagio ni un veredicto definitivo.',
    errors: ['“Similitud no encontrada”: el par pudo eliminarse o recalcularse.', '“Similitud no autorizada”: no pertenece a tu ámbito.'],
    related: [{ id: 'aprendiz-similitudes', label: 'Volver a similitudes' }, { id: 'aprendiz-detalle-propuesta', label: 'Abrir propuesta' }]
  },
  {
    id: 'aprendiz-alertas', num: 'Cap. 16', part: 'parte-2', roles: ['aprendiz'], title: 'Notificaciones del aprendiz',
    purpose: 'Consulta las novedades de tus propuestas, similitudes y revisiones, y márcalas como leídas.',
    route: 'Abre <code>/aprendiz/alertas</code> o pulsa la campana.',
    whatYouSee: ['Título “Notificaciones” y botón “Marcar todas como leídas”.', 'Resumen de no leídas.', 'Lista con tipo, fecha, título y marca “Nueva”.', 'Estado vacío con acceso a Mis Propuestas.'],
    figures: [fig('assets/capturas/17-aprendiz-alertas.jpg', 'Notificaciones del aprendiz.', 'Bandeja de notificaciones.')],
    steps: ['Revisa la lista de arriba hacia abajo.', 'Pulsa una notificación para abrir su destino.', 'Si no tiene destino, solo se marca como leída.', 'Usa “Marcar todas como leídas” para limpiar el contador.'],
    after: ['La campana deja de mostrar el punto cuando no quedan pendientes.', 'Las notificaciones de reporte no navegan para el aprendiz; solo se marcan.'],
    tip: 'Las notificaciones nuevas aparecen primero. Si algo no tiene enlace, el título suele explicar el cambio.',
    errors: ['Si no hay notificaciones, el estado vacío te lleva a Mis Propuestas.', 'Si una notificación no abre nada, puede ser un aviso sin enlace.'],
    related: [{ id: 'aprendiz-propuestas', label: 'Mis propuestas' }, { id: 'aprendiz-similitudes', label: 'Similitudes' }]
  },
  {
    id: 'aprendiz-soporte', num: 'Cap. 17', part: 'parte-2', roles: ['aprendiz'], title: 'Soporte del aprendiz',
    purpose: 'Envía una solicitud al administrador por errores, conflictos con fichas, datos o dudas.',
    route: 'Pulsa “Soporte” en el footer o abre <code>/aprendiz/reportar-falla</code>.',
    whatYouSee: ['Título “Soporte”.', 'Campos: número de ficha, motivo, título, descripción y tipo.', 'Botón “Enviar solicitud”.', 'Panel de confirmación “Solicitud enviada”.'],
    figures: [fig('assets/capturas/18-aprendiz-soporte.jpg', 'Formulario de soporte del aprendiz.', 'Formulario de reporte de falla.')],
    fields: { head: ['Campo', 'Obligatorio', 'Regla'], rows: [['Número de ficha', 'No', 'Máximo 30 caracteres; se puede precargar desde otra pantalla.'], ['Motivo', 'No', 'Máximo 120 caracteres.'], ['Título de la solicitud', 'No', 'Máximo 255; si se deja vacío y hay número de ficha, se genera.'], ['Descripción', 'Sí', 'Explica qué pasó, en qué pantalla y qué esperabas.'], ['Tipo de solicitud', 'No', 'Interfaz, datos, rendimiento, seguridad u otro.']] },
    steps: ['Escribe el número de ficha si el caso lo requiere.', 'Escribe un motivo corto.', 'Escribe un título o déjalo vacío.', 'Describe el problema con detalle.', 'Elige el tipo.', 'Pulsa “Enviar solicitud”.'],
    after: ['Se muestra “¡Gracias por escribirnos!”. El administrador revisará la solicitud.', 'No hay historial de solicitudes en esta pantalla.'],
    tip: 'Incluye la ruta, el botón que pulsaste y el mensaje exacto. Eso acelera la solución.',
    errors: ['Si falta la descripción, el navegador bloquea el envío.', 'Si falla, revisa la conexión y vuelve a intentarlo.'],
    related: [{ id: 'admin-reportes', label: 'Bandeja de soporte del admin' }]
  },
  {
    id: 'aprendiz-perfil', num: 'Cap. 18', part: 'parte-2', roles: ['aprendiz'], title: 'Mi Perfil del aprendiz',
    purpose: 'Consulta y actualiza tus datos, foto, correo y contraseña.',
    route: 'Pulsa tu avatar o abre <code>/aprendiz/perfil</code>.',
    whatYouSee: ['Tarjetas de proyectos, notificaciones y programa.', 'Información personal, rol, ficha y documento.', 'Botón “Editar perfil”.', 'Panel de Seguridad con cambio de correo y contraseña.'],
    figures: [fig('assets/capturas/19-aprendiz-perfil.jpg', 'Perfil del aprendiz.', 'Perfil con información personal y seguridad.')],
    fields: { head: ['Acción', 'Campos', 'Regla'], rows: [['Editar perfil', 'Nombres, apellidos', 'Obligatorios.'], ['Cambiar foto', 'Archivo de imagen', 'Se recorta y optimiza; máximo 5 MB.'], ['Cambiar correo', 'Nuevo correo, contraseña actual', 'Correo válido y contraseña actual obligatoria.'], ['Cambiar contraseña', 'Actual, nueva, confirmación', 'Nueva de mínimo 12 caracteres; debe coincidir.']] },
    steps: ['Pulsa “Editar perfil” para cambiar nombres o apellidos.', 'Pulsa la foto para cambiarla o quitarla.', 'Usa “Cambiar correo” y confirma con tu contraseña.', 'Usa “Cambiar contraseña” y guarda.', 'Pulsa “Guardar cambios” si editaste datos.'],
    after: ['Los cambios se reflejan en la barra superior y en tu perfil.', 'El correo y la contraseña se actualizan de inmediato.'],
    tip: 'El documento y el rol no se editan desde el perfil. Si están mal, pide al administrador que los corrija.',
    errors: ['“Ingresa tus nombres/apellidos”: campo vacío.', '“Ese correo ya está registrado”: usa otro.', 'La contraseña no coincide: revisa la confirmación.'],
    related: [{ id: 'aprendiz-ficha', label: 'Mi ficha' }, { id: 'login', label: 'Cerrar sesión' }]
  },
  {
    id: 'aprendiz-companeros', num: 'Cap. 19', part: 'parte-2', roles: ['aprendiz'], title: 'Compañeros e instructor',
    purpose: 'Consulta el perfil de tus compañeros de ficha y el de tu instructor.',
    route: 'Desde Mi Ficha, pulsa un integrante o el nombre del instructor.',
    whatYouSee: ['Perfil de solo lectura con nombre, correo y rol.', 'Ficha y programa si es aprendiz.', 'Propuestas del compañero si es aprendiz.', 'Información del instructor y sus fichas.'],
    figures: [fig('assets/capturas/21-aprendiz-perfil-companero.jpg', 'Perfil de un compañero de ficha.', 'Perfil de solo lectura de un compañero.'), fig('assets/capturas/22-aprendiz-perfil-instructor.jpg', 'Perfil del instructor desde la vista del aprendiz.', 'Perfil del instructor.')],
    steps: ['Abre Mi Ficha.', 'Pulsa un compañero para ver su perfil.', 'Pulsa el nombre del instructor para ver el suyo.', 'Usa las propuestas del compañero para abrir un detalle si está permitido.'],
    after: ['No puedes editar el perfil de otra persona.', 'Los datos visibles dependen de tu rol y de la relación con la ficha.'],
    tip: 'Si necesitas contactar a un compañero, usa el correo visible en su perfil.',
    errors: ['“Usuario no encontrado”: la cuenta pudo eliminarse.', 'Si no ves propuestas, el compañero aún no ha registrado ninguna.'],
    related: [{ id: 'aprendiz-ficha', label: 'Mi ficha' }, { id: 'aprendiz-propuestas', label: 'Mis propuestas' }]
  },
  {
    id: 'aprendiz-flujo-completo', num: 'Cap. 20', part: 'parte-2', roles: ['aprendiz'], title: 'Flujo completo del aprendiz',
    purpose: 'Reúne el recorrido completo: unirse a una ficha, crear, enviar, analizar y consultar una propuesta.',
    route: 'Empieza en <code>/aprendiz/ficha</code>.',
    whatYouSee: ['Secuencia de pantallas y acciones.', 'Puntos de control para saber si cada paso quedó bien.'],
    steps: ['Únete a una ficha con el código del instructor.', 'Abre Mis Propuestas y pulsa “Nueva propuesta”.', 'Completa todos los campos obligatorios.', 'Guarda un borrador o envía y analiza.', 'Espera el resultado y revisa las similitudes.', 'Consulta las notificaciones cuando el instructor responda.', 'Si algo falla, usa Soporte con la ruta y el mensaje.'],
    after: ['La propuesta aparece en tu lista con su estado actualizado.', 'El instructor la verá en su cola de revisión.'],
    tip: 'Haz una captura o anota el número de la propuesta si necesitas soporte.',
    errors: commonErrors,
    related: [{ id: 'aprendiz-ficha', label: 'Mi ficha' }, { id: 'aprendiz-crear-propuesta', label: 'Crear propuesta' }, { id: 'aprendiz-analisis', label: 'Análisis' }]
  },

  /* ------------------------------------------------------------------ PARTE III */
  {
    id: 'instructor-dashboard', num: 'Cap. 21', part: 'parte-3', roles: ['instructor'], title: 'Dashboard del instructor',
    purpose: 'Muestra las propuestas pendientes, tus fichas, similitudes y notificaciones.',
    route: 'Inicia sesión como instructor o abre <code>/instructor/dashboard</code>.',
    whatYouSee: ['Saludo y resumen de pendientes.', 'Chips de por revisar, fichas, similitudes y notificaciones.', 'Métricas: fichas, aprendices, propuestas y estados.', '“Hoy en tu turno” con propuestas recientes.', '“Tus cohortes” con tus fichas.'],
    figures: [fig('assets/capturas/25-instructor-dashboard.jpg', 'Dashboard del instructor.', 'Panel general del instructor.')],
    steps: ['Revisa cuántas propuestas tienes pendientes.', 'Pulsa “Revisar propuestas” si hay trabajo.', 'Abre una ficha en “Tus cohortes” para ver su detalle.', 'Usa la campana para revisar notificaciones.'],
    after: ['Los indicadores se actualizan según tus fichas y propuestas.', 'Si no hay pendientes, el panel muestra un estado vacío.'],
    tip: 'Empieza por las propuestas pendientes más antiguas o con mayor similitud.',
    errors: ['Si no aparecen tus fichas, revisa que el administrador te haya asignado como instructor.', 'Si una métrica falla, el resto del panel sigue funcionando.'],
    related: [{ id: 'instructor-revision', label: 'Revisión de propuestas' }, { id: 'instructor-fichas', label: 'Mis fichas' }]
  },
  {
    id: 'instructor-revision', num: 'Cap. 22', part: 'parte-3', roles: ['instructor'], title: 'Revisión de propuestas',
    purpose: 'Permite revisar la cola de propuestas enviadas por tus aprendices y aprobarlas o rechazarlas.',
    route: 'Abre <code>/instructor/revision-propuestas</code>.',
    whatYouSee: ['Filtros por texto, estado y ficha.', 'Cola de propuestas con autor, fecha y similitud.', 'Vista previa con resumen, similitud y acciones.', 'Botones Aprobar, Rechazar y Ver detalle.'],
    figures: [fig('assets/capturas/26-instructor-revision.jpg', 'Cola de revisión de propuestas.', 'Revisión con cola y vista previa.', [{ n: 1, left: 30, top: 27, title: 'Filtros', text: 'Busca por título, aprendiz o ficha y filtra por estado.' }, { n: 2, left: 22, top: 45, title: 'Cola', text: 'Selecciona una propuesta para verla a la derecha.' }, { n: 3, left: 65, top: 45, title: 'Vista previa', text: 'Revisa resumen, similitud y estado.' }, { n: 4, left: 65, top: 56, title: 'Decisión', text: 'Aprobar o rechazar solo si está pendiente.' }])],
    steps: ['Filtra por estado o ficha si lo necesitas.', 'Pulsa una propuesta de la cola.', 'Lee el resumen y la similitud.', 'Pulsa “Aprobar” o “Rechazar”.', 'Confirma en el modal.', 'Si rechazas, puedes escribir una observación opcional.'],
    after: ['La propuesta cambia de estado y el aprendiz recibe una notificación.', 'La cola se recarga y la propuesta sale de pendientes.'],
    tip: 'Si la similitud es alta, abre “Ver detalle” antes de decidir.',
    errors: ['No hay botones si la propuesta ya no está pendiente.', 'Error de actualización: revisa la conexión y reintenta.', 'Sin propuestas: el estado vacío explica que aún no has recibido envíos.'],
    related: [{ id: 'instructor-detalle-propuesta', label: 'Detalle de propuesta' }, { id: 'instructor-similitudes', label: 'Similitudes' }]
  },
  {
    id: 'instructor-detalle-propuesta', num: 'Cap. 23', part: 'parte-3', roles: ['instructor'], title: 'Detalle de propuesta del instructor',
    purpose: 'Revisa en profundidad una propuesta, su historial, similitudes y observaciones, y decide.',
    route: 'Pulsa “Ver detalle” o una propuesta. La ruta es <code>/instructor/detalle-proyecto/:id</code>.',
    whatYouSee: ['Título, estado, fecha, autor y ficha.', 'Descripción, objetivos y palabras clave.', 'Historial de cambios.', 'Información del aprendiz, similitudes y observaciones.'],
    figures: [fig('assets/capturas/27-instructor-detalle-propuesta.jpg', 'Detalle de propuesta para instructor.', 'Detalle con información, historial y panel lateral.')],
    steps: ['Lee la información del proyecto.', 'Revisa el historial y las similitudes.', 'Escribe una observación si necesitas aclarar algo.', 'Pulsa “Aprobar” o “Rechazar” si está pendiente.', 'Confirma la decisión.'],
    after: ['La decisión se registra y el aprendiz recibe el aviso.', 'Las observaciones quedan en el hilo y el aprendiz puede responder.'],
    tip: 'Usa las observaciones para explicar una decisión, no solo para rechazar.',
    errors: ['Si la propuesta es de otra ficha, se muestra en modo lectura.', 'Si no aparecen acciones, no está pendiente o no está a tu cargo.'],
    related: [{ id: 'instructor-revision', label: 'Volver a revisión' }, { id: 'instructor-similitudes', label: 'Similitudes' }]
  },
  {
    id: 'instructor-fichas', num: 'Cap. 24', part: 'parte-3', roles: ['instructor'], title: 'Gestión de fichas del instructor',
    purpose: 'Consulta tus fichas, copia su código, abre el detalle o crea una nueva.',
    route: 'Abre <code>/instructor/fichas</code>.',
    whatYouSee: ['Título “Gestionar Fichas” y botón “Crear Ficha”.', 'Filtros por campo, búsqueda, estado y red.', 'Tarjetas con código, estado, nombre, número, programa, avance y conteos.', 'Botones Ver, Eliminar y Copiar.'],
    figures: [fig('assets/capturas/28-instructor-fichas.jpg', 'Listado de fichas del instructor.', 'Fichas asignadas al instructor.', [{ n: 1, left: 30, top: 30, title: 'Filtros', text: 'Busca por código, nombre o número; filtra por estado y red.' }, { n: 2, left: 25, top: 52, title: 'Tarjeta de ficha', text: 'Muestra código, estado, aprendices, propuestas y avance.' }, { n: 3, left: 30, top: 49, title: 'Copiar código', text: 'Copia el código para compartirlo con los aprendices.' }, { n: 4, left: 94, top: 15, title: 'Crear ficha', text: 'Abre el formulario de creación.' }])],
    steps: ['Busca una ficha si tienes varias.', 'Copia el código y compártelo.', 'Pulsa “Ver” para abrir el detalle.', 'Pulsa “Crear Ficha” para una nueva.'],
    after: ['La ficha creada queda asignada a ti como instructor.', 'El código generado permite que los aprendices se unan.'],
    tip: 'Comparte el código solo con los aprendices de la ficha; no lo publiques en canales abiertos.',
    errors: ['“Ninguna ficha coincide”: limpia los filtros.', 'Si no tienes fichas, crea la primera.', 'Eliminar una ficha con historial la anula; no borra aprendices ni propuestas.'],
    related: [{ id: 'instructor-crear-ficha', label: 'Crear ficha' }, { id: 'instructor-detalle-ficha', label: 'Detalle de ficha' }]
  },
  {
    id: 'instructor-crear-ficha', num: 'Cap. 25', part: 'parte-3', roles: ['instructor'], title: 'Crear una ficha',
    purpose: 'Registra una ficha de formación y queda asignada a ti como instructor.',
    route: 'Pulsa “Crear Ficha” o abre <code>/instructor/fichas?crear=1</code>.',
    whatYouSee: ['Red de conocimiento, programa, nombre y número.', 'Botones “Crear ficha” y “Cancelar”.', 'Aviso si el número ya existe, con enlace a Soporte.'],
    figures: [fig('assets/capturas/29-instructor-crear-ficha.jpg', 'Formulario de creación de ficha del instructor.', 'Formulario de nueva ficha.')],
    fields: { head: ['Campo', 'Obligatorio', 'Regla'], rows: [['Red de conocimiento', 'Sí', 'Determina los programas disponibles.'], ['Programa de formación', 'Sí', 'Se habilita después de elegir la red.'], ['Nombre de la ficha', 'Sí', 'Máximo 80 caracteres.'], ['Número de ficha', 'Sí', 'Solo dígitos, entre 4 y 8 caracteres.']] },
    steps: ['Elige la red.', 'Elige el programa.', 'Escribe el nombre.', 'Escribe el número.', 'Pulsa “Crear ficha”.'],
    after: ['La ficha aparece en tu listado y queda activa.', 'El sistema genera un código de unión.'],
    tip: 'El número de ficha debe ser único. Si ya existe, usa Soporte para revisar el caso.',
    errors: ['“Solo dígitos (4 a 8 caracteres)”.', '“El número ya está registrado en otra ficha”.', 'Si no existe tu perfil de instructor, contacta al administrador.'],
    related: [{ id: 'instructor-fichas', label: 'Volver a fichas' }, { id: 'instructor-detalle-ficha', label: 'Detalle de ficha' }]
  },
  {
    id: 'instructor-detalle-ficha', num: 'Cap. 26', part: 'parte-3', roles: ['instructor'], title: 'Detalle de una ficha',
    purpose: 'Gestiona una ficha propia: edita datos, consulta aprendices y propuestas, abre el directorio o elimina.',
    route: 'Pulsa “Ver” en una ficha. La ruta es <code>/instructor/detalle-ficha/:id</code>.',
    whatYouSee: ['Información de la ficha con código, número, red, programa, instructor y estado.', 'Botones Editar, Eliminar y Ver directorio.', 'Lista de aprendices y propuestas.'],
    figures: [fig('assets/capturas/30-instructor-detalle-ficha.jpg', 'Detalle de ficha del instructor.', 'Información, aprendices y propuestas de la ficha.', [{ n: 1, left: 25, top: 30, title: 'Información', text: 'Datos generales de la ficha.' }, { n: 2, left: 25, top: 52, title: 'Aprendices', text: 'Abre el perfil de cada aprendiz.' }, { n: 3, left: 25, top: 76, title: 'Propuestas', text: 'Abre el detalle de cada propuesta.' }, { n: 4, left: 87, top: 18.5, title: 'Acciones', text: 'Editar o eliminar la ficha.' }])],
    steps: ['Revisa los datos generales.', 'Pulsa “Editar” para cambiar nombre, número o estado.', 'Guarda los cambios.', 'Abre el directorio para ver aprendices.', 'Pulsa una propuesta para revisarla.'],
    after: ['Los cambios se guardan y se recargan.', 'Eliminar una ficha con historial la anula; conserva los datos.'],
    tip: 'Finalizar una ficha bloquea nuevas uniones, pero conserva el historial.',
    errors: ['“Ficha no encontrada”.', '“Esta ficha no está a tu cargo”: solo puedes gestionar las tuyas.', 'Número duplicado al editar: corrige el campo.'],
    related: [{ id: 'instructor-directorio', label: 'Directorio' }, { id: 'instructor-fichas', label: 'Volver a fichas' }]
  },
  {
    id: 'instructor-directorio', num: 'Cap. 27', part: 'parte-3', roles: ['instructor'], title: 'Directorio de la ficha',
    purpose: 'Explora los aprendices de una ficha y abre sus perfiles.',
    route: 'Desde el detalle de la ficha, pulsa “Ver directorio”. La ruta es <code>/instructor/directorio-ficha/:id</code>.',
    whatYouSee: ['Título “Directorio · nombre de la ficha”.', 'Buscador por nombre o correo.', 'Selector de orden A–Z o Z–A.', 'Tarjetas con avatar, nombre, correo, programa y “Ver perfil”.'],
    figures: [fig('assets/capturas/31-instructor-directorio-ficha.jpg', 'Directorio de aprendices de una ficha.', 'Directorio con búsqueda y tarjetas.')],
    steps: ['Escribe un nombre o correo.', 'Cambia el orden si lo necesitas.', 'Pulsa “Ver perfil” para abrir un aprendiz.', 'Usa la paginación si hay muchos.'],
    after: ['El perfil es de solo lectura.', 'No puedes agregar o retirar aprendices desde el directorio.'],
    tip: 'El buscador ignora mayúsculas y tildes.',
    errors: ['“Sin resultados”: prueba con otro nombre.', '“Sin aprendices”: aún nadie se ha unido a la ficha.'],
    related: [{ id: 'instructor-detalle-ficha', label: 'Volver a la ficha' }, { id: 'aprendiz-companeros', label: 'Perfil de aprendiz' }]
  },
  {
    id: 'instructor-similitudes', num: 'Cap. 28', part: 'parte-3', roles: ['instructor'], title: 'Similitudes del instructor',
    purpose: 'Analiza los pares de propuestas similares que involucran tus propuestas.',
    route: 'Abre <code>/instructor/similitudes</code>.',
    whatYouSee: ['Filtros por título, aprendiz, estado, ficha y porcentaje mínimo.', 'Grupos por ficha.', 'Tabla con propuesta A, propuesta B, similitud, estados y fecha.', 'Botón “Ver”.'],
    figures: [fig('assets/capturas/32-instructor-similitudes.jpg', 'Similitudes detectadas para el instructor.', 'Pares agrupados por ficha.')],
    steps: ['Filtra por ficha o porcentaje.', 'Expande un grupo.', 'Pulsa “Ver” para abrir la comparación.'],
    after: ['La pantalla es de solo lectura; no elimina similitudes.', 'Los pares se actualizan cuando cambia el motor o se recalcula.'],
    tip: 'Revisa primero los porcentajes más altos y las propuestas pendientes.',
    errors: ['“Ninguna de tus propuestas alcanza el umbral”: aún no hay coincidencias.', '“No hay similitudes con el estado seleccionado”: cambia el filtro.'],
    related: [{ id: 'instructor-detalle-similitud', label: 'Detalle de similitud' }, { id: 'instructor-revision', label: 'Revisión' }]
  },
  {
    id: 'instructor-detalle-similitud', num: 'Cap. 29', part: 'parte-3', roles: ['instructor'], title: 'Detalle de similitud del instructor',
    purpose: 'Compara dos propuestas y revisa la evidencia antes de decidir.',
    route: 'Pulsa “Ver” en una similitud. La ruta es <code>/instructor/detalle-similitud/:id</code>.',
    whatYouSee: ['Umbral y comparación A/B.', 'Tarjetas de ambas propuestas.', 'Métricas, términos compartidos y fragmentos.', 'Otras coincidencias relacionadas.'],
    figures: [fig('assets/capturas/33-instructor-detalle-similitud.jpg', 'Detalle de similitud para instructor.', 'Comparación visual y evidencia.')],
    steps: ['Lee el porcentaje y la nota.', 'Revisa las tarjetas A/B.', 'Lee los términos y fragmentos.', 'Abre una propuesta si necesitas más contexto.'],
    after: ['No cambia estados ni elimina similitudes.'],
    tip: 'La similitud no es un veredicto. Úsala como evidencia para revisar, no para acusar.',
    errors: ['“Similitud no encontrada”.', '“Similitud no autorizada”: no pertenece a tu ámbito.'],
    related: [{ id: 'instructor-similitudes', label: 'Volver a similitudes' }, { id: 'instructor-detalle-propuesta', label: 'Abrir propuesta' }]
  },
  {
    id: 'instructor-alertas', num: 'Cap. 30', part: 'parte-3', roles: ['instructor'], title: 'Notificaciones del instructor',
    purpose: 'Consulta revisiones, similitudes y actividad del sistema.',
    route: 'Abre <code>/instructor/alertas</code> o pulsa la campana.',
    whatYouSee: ['Lista con tipo, fecha, título y “Nueva”.', 'Botón “Marcar todas como leídas”.', 'Estado vacío con acceso a revisión.'],
    figures: [fig('assets/capturas/34-instructor-alertas.jpg', 'Notificaciones del instructor.', 'Bandeja de notificaciones.')],
    steps: ['Pulsa una notificación para abrir su destino.', 'Usa “Marcar todas como leídas”.', 'Si no hay enlace, solo se marca.'],
    after: ['La campana deja de mostrar el punto cuando no hay pendientes.'],
    tip: 'Las notificaciones de reporte no navegan para el instructor; solo se marcan.',
    errors: ['Si no hay notificaciones, el estado vacío te lleva a revisión.'],
    related: [{ id: 'instructor-revision', label: 'Revisión de propuestas' }]
  },
  {
    id: 'instructor-soporte-perfil', num: 'Cap. 31', part: 'parte-3', roles: ['instructor'], title: 'Soporte y perfil del instructor',
    purpose: 'Envía solicitudes de soporte y actualiza tus datos, foto, correo y contraseña.',
    route: 'Soporte: <code>/instructor/reportar-falla</code>. Perfil: <code>/instructor/perfil</code>.',
    whatYouSee: ['Formulario de soporte con número de ficha, motivo, título, descripción y tipo.', 'Perfil con datos, ficha, seguridad y foto.'],
    figures: [fig('assets/capturas/35-instructor-soporte.jpg', 'Formulario de soporte del instructor.', 'Soporte para instructor.'), fig('assets/capturas/36-instructor-perfil.jpg', 'Perfil del instructor.', 'Perfil y seguridad del instructor.')],
    fields: { head: ['Formulario', 'Campos', 'Regla'], rows: [['Soporte', 'Número de ficha, motivo, título, descripción, tipo', 'La descripción es obligatoria; los demás son opcionales.'], ['Perfil', 'Nombres, apellidos, foto', 'Nombres y apellidos obligatorios.'], ['Correo', 'Nuevo correo, contraseña actual', 'Correo válido y contraseña actual.'], ['Contraseña', 'Actual, nueva, confirmación', 'Mínimo 12 caracteres; debe coincidir.']] },
    steps: ['Para soporte: describe el caso y pulsa “Enviar solicitud”.', 'Para el perfil: pulsa “Editar perfil”, cambia tus datos y guarda.', 'Para la foto: pulsa el avatar y elige una imagen.', 'Para seguridad: usa “Cambiar correo” o “Cambiar contraseña”.'],
    after: ['La solicitud queda pendiente para el administrador.', 'Los cambios de perfil se reflejan en la barra superior.'],
    tip: 'En soporte, incluye el número de ficha y el mensaje exacto del error.',
    errors: ['Si falta la descripción, el navegador bloquea el envío.', 'Si el correo ya existe, usa otro.', 'Si la contraseña no coincide, revisa la confirmación.'],
    related: [{ id: 'instructor-alertas', label: 'Notificaciones' }, { id: 'admin-reportes', label: 'Soporte del admin' }]
  },
  {
    id: 'instructor-flujo-revision', num: 'Cap. 32', part: 'parte-3', roles: ['instructor'], title: 'Flujo completo de revisión',
    purpose: 'Resume el recorrido desde que el aprendiz envía una propuesta hasta que recibe la decisión.',
    route: 'Empieza en <code>/instructor/dashboard</code>.',
    whatYouSee: ['Secuencia de revisión y puntos de control.'],
    steps: ['Entra al dashboard y revisa pendientes.', 'Abre Revisión de Propuestas.', 'Selecciona una propuesta.', 'Revisa el detalle y las similitudes.', 'Aprueba o rechaza, con observación si aplica.', 'Confirma y verifica que la propuesta salió de pendientes.', 'El aprendiz recibe la notificación.'],
    after: ['La propuesta cambia de estado y queda en el historial.', 'El admin puede supervisar el mismo cambio desde su panel.'],
    tip: 'Si necesitas más contexto, abre el perfil del aprendiz o el detalle de la similitud antes de decidir.',
    errors: commonErrors,
    related: [{ id: 'instructor-revision', label: 'Revisión' }, { id: 'instructor-detalle-propuesta', label: 'Detalle' }]
  },

  /* ------------------------------------------------------------------ PARTE IV */
  {
    id: 'admin-dashboard', num: 'Cap. 33', part: 'parte-4', roles: ['admin'], title: 'Dashboard del administrador',
    purpose: 'Monitorea usuarios, propuestas, similitudes, reportes y estado del motor.',
    route: 'Inicia sesión como admin o abre <code>/admin/dashboard</code>.',
    whatYouSee: ['Saludo y resumen general.', 'Métricas de usuarios, aprendices, instructores y fichas.', 'Gráficos de propuestas y cuentas.', 'Salud del motor.', 'Colas por atender y notificaciones recientes.'],
    figures: [fig('assets/capturas/37-admin-dashboard.jpg', 'Dashboard del administrador.', 'Panel general de la plataforma.')],
    steps: ['Revisa las métricas generales.', 'Pulsa una cola para atenderla.', 'Ajusta el motor si es necesario.', 'Revisa las notificaciones recientes.'],
    after: ['Cada tarjeta y enlace te lleva a la sección correspondiente.'],
    tip: 'Usa “Colas por atender” para priorizar propuestas pendientes, reportes abiertos y cuentas suspendidas.',
    errors: ['Si una métrica falla, el resto del panel sigue funcionando.', 'Si no hay notificaciones, el panel muestra “Sin notificaciones”.'],
    related: [{ id: 'admin-proyectos', label: 'Propuestas' }, { id: 'admin-reportes', label: 'Soporte' }, { id: 'admin-motor', label: 'Motor' }]
  },
  {
    id: 'admin-usuarios', num: 'Cap. 34', part: 'parte-4', roles: ['admin'], title: 'Gestión de usuarios',
    purpose: 'Administra cuentas de aprendices e instructores, crea usuarios y controla su estado.',
    route: 'Abre <code>/admin/usuarios</code>.',
    whatYouSee: ['Título “Gestión de Usuarios” y botón “Nuevo Usuario”.', 'Filtros por texto, rol, estado, ficha y programa.', 'Tabla con nombre completo, documento, usuario institucional, correo, rol, ficha, acceso, estado y acciones.', 'Paginación del servidor.'],
    figures: [fig('assets/capturas/38-admin-usuarios.jpg', 'Listado de usuarios del administrador.', 'Tabla de usuarios con filtros y acciones.', [{ n: 1, left: 30, top: 22, title: 'Filtros', text: 'Busca por nombre, documento, usuario institucional o correo.' }, { n: 2, left: 50, top: 48, title: 'Tabla', text: 'Muestra rol, ficha, acceso y estado.' }, { n: 3, left: 92, top: 48, title: 'Acciones', text: 'Ver, reenviar activación, activar o suspender.' }, { n: 4, left: 94, top: 11, title: 'Nuevo usuario', text: 'Abre el formulario de alta.' }])],
    steps: ['Filtra si buscas una cuenta.', 'Pulsa un nombre para abrir el detalle.', 'Usa “Reenviar activación” si la cuenta está pendiente.', 'Suspende o activa según corresponda.'],
    after: ['Los cambios de estado se reflejan de inmediato.'],
    tip: 'La activación llega al correo personal; no se generan contraseñas temporales ni se entregan por PDF.',
    errors: ['“No hay usuarios que coincidan”: limpia los filtros.', 'No puedes suspender tu propia cuenta.', 'Si el reenvío falla, revisa el correo y la configuración de envío.'],
    related: [{ id: 'admin-crear-usuario', label: 'Crear usuario' }, { id: 'admin-detalle-usuario', label: 'Detalle de usuario' }]
  },
  {
    id: 'admin-crear-usuario', num: 'Cap. 35', part: 'parte-4', roles: ['admin'], title: 'Crear un usuario',
    purpose: 'Crea una cuenta de aprendiz o instructor y envía el enlace de activación.',
    route: 'Pulsa “Nuevo Usuario” o abre <code>/admin/usuarios?crear=1</code>.',
    whatYouSee: ['Formulario con nombres, apellidos, tipo y número de documento, correo y rol.', 'Botones “Crear usuario” y “Cancelar”.'],
    figures: [fig('assets/capturas/40-admin-crear-usuario.jpg', 'Formulario de creación de usuario.', 'Alta de usuario.')],
    fields: { head: ['Campo', 'Obligatorio', 'Regla'], rows: [['Nombres', 'Sí', 'Máximo 80 caracteres.'], ['Apellidos', 'Sí', 'Máximo 80 caracteres.'], ['Tipo de documento', 'Sí', 'CC, TI, CE o PPT.'], ['Número de documento', 'Sí', 'Único; máximo 40 caracteres.'], ['Correo personal', 'Sí', 'Formato de correo; recibe la activación.'], ['Rol', 'Sí', 'Aprendiz o instructor.']] },
    steps: ['Escribe nombres y apellidos.', 'Elige el tipo de documento.', 'Escribe el número.', 'Escribe el correo personal.', 'Elige el rol.', 'Pulsa “Crear usuario”.'],
    after: ['La cuenta queda pendiente de activación.', 'Si el correo se envía, verás la confirmación; si falla, podrás reenviar.'],
    tip: 'Revisa el correo antes de guardar: allí llegará el enlace de activación.',
    errors: ['Correo duplicado o documento duplicado: usa otro.', 'Correo inválido: corrige el formato.', 'Si no se envía el correo, usa “Reenviar activación” en el detalle.'],
    related: [{ id: 'admin-usuarios', label: 'Volver a usuarios' }, { id: 'admin-detalle-usuario', label: 'Detalle de usuario' }]
  },
  {
    id: 'admin-detalle-usuario', num: 'Cap. 36', part: 'parte-4', roles: ['admin'], title: 'Detalle de usuario',
    purpose: 'Consulta y administra una cuenta: edita datos, rol, ficha, foto, estado y eliminación.',
    route: 'Pulsa un usuario en el listado. La ruta es <code>/admin/detalle-usuario/:id</code>.',
    whatYouSee: ['Perfil con rol, estado, ficha, programa y acceso.', 'Acciones Editar, Activar/Suspender, Reenviar activación o Enviar enlace, Eliminar.', 'Propuestas del aprendiz.', 'Modales de confirmación y eliminación definitiva.'],
    figures: [fig('assets/capturas/39-admin-detalle-usuario.jpg', 'Detalle de un usuario.', 'Cuenta de aprendiz con acciones y propuestas.', [{ n: 1, left: 25, top: 30, title: 'Datos de la cuenta', text: 'Rol, estado, ficha, programa y acceso.' }, { n: 2, left: 25, top: 55, title: 'Acciones', text: 'Editar, suspender, enviar enlace o eliminar.' }, { n: 3, left: 25, top: 70, title: 'Propuestas', text: 'Consulta las propuestas del aprendiz.' }])],
    fields: { head: ['Acción', 'Campos', 'Regla'], rows: [['Editar', 'Nombres, apellidos, documento, correo, rol, ficha', 'Nombres, apellidos, documento y correo obligatorios; correo válido.'], ['Cambiar foto', 'Imagen', 'Se recorta y optimiza; máximo 5 MB.'], ['Suspender/Activar', 'Confirmación', 'No puedes suspender tu propia cuenta.'], ['Eliminar', 'Responsabilidad y texto ELIMINAR', 'Si hay historial, ofrece desactivar o eliminar definitivamente.']] },
    steps: ['Revisa los datos de la cuenta.', 'Pulsa “Editar” para cambiar datos o rol.', 'Guarda los cambios.', 'Usa “Suspender” o “Activar” según corresponda.', 'Usa “Reenviar activación” o “Enviar enlace de contraseña” si la persona no puede entrar.', 'Pulsa “Eliminar” solo si estás seguro y sigue la confirmación.'],
    after: ['Los cambios se guardan y se recargan.', 'La eliminación definitiva requiere una segunda confirmación y puede borrar datos relacionados.'],
    tip: 'Si el usuario tiene historial, prefiere “Desactivar cuenta” antes que eliminarla definitivamente.',
    errors: ['“Ya existe un usuario con este correo”.', '“Ya existe un usuario con este número de documento”.', 'No puedes quitarte tu propio rol de administrador.', 'Si la cuenta está suspendida, el usuario no podrá iniciar sesión.'],
    related: [{ id: 'admin-usuarios', label: 'Volver a usuarios' }, { id: 'admin-bitacora', label: 'Ver en bitácora' }]
  },
  {
    id: 'admin-fichas', num: 'Cap. 37', part: 'parte-4', roles: ['admin'], title: 'Gestión de fichas',
    purpose: 'Consulta todas las fichas y crea nuevas.',
    route: 'Abre <code>/admin/fichas</code>.',
    whatYouSee: ['Título “Fichas de formación” y botón “Crear Ficha”.', 'Filtros por campo, texto, estado, instructor, programa, red y fechas.', 'Tabla con código, ficha, instructor, aprendices, propuestas, estado, fecha y acciones.', 'Acciones Ver y Eliminar.', 'En tablet, filtros apilados, menú compacto y tabla adaptada.'],
    figures: [fig('assets/capturas/41-admin-fichas.jpg', 'Listado de fichas del administrador.', 'Tabla de fichas con filtros y acciones.', [{ n: 1, left: 30, top: 22, title: 'Filtros', text: 'Busca por código, nombre o número y filtra por estado, instructor, programa, red y fechas.' }, { n: 2, left: 50, top: 48, title: 'Tabla', text: 'Muestra instructor, aprendices, propuestas y estado.' }, { n: 3, left: 92, top: 48, title: 'Acciones', text: 'Ver o eliminar.' }, { n: 4, left: 94, top: 11, title: 'Crear ficha', text: 'Abre el formulario de creación.' }]), fig('assets/capturas/55-admin-fichas-tablet.jpg', 'Vista de fichas de formación del administrador en una tablet.', 'Vista en tablet: menú compacto, filtros apilados, acción Crear ficha y tabla adaptada.')],
    steps: ['Filtra si buscas una ficha.', 'Pulsa “Ver” para abrir el detalle.', 'Pulsa “Crear Ficha” para una nueva.', 'Elimina solo si estás seguro.'],
    after: ['La tabla se recarga con los cambios.', 'Eliminar una ficha con historial la anula y libera el número.'],
    tip: 'El roster se gestiona desde “Editar ficha”; los cambios se reflejan en el detalle.',
    errors: ['“Ninguna ficha coincide”: limpia los filtros.', 'Si no hay fichas, crea la primera.', 'Error al cargar: revisa la conexión.'],
    related: [{ id: 'admin-crear-ficha', label: 'Crear ficha' }, { id: 'admin-detalle-ficha', label: 'Detalle de ficha' }]
  },
  {
    id: 'admin-crear-ficha', num: 'Cap. 38', part: 'parte-4', roles: ['admin'], title: 'Crear una ficha',
    purpose: 'Registra una ficha, asigna instructor y aprende cuáles son los campos obligatorios.',
    route: 'Pulsa “Crear Ficha” o abre <code>/admin/fichas?crear=1</code>.',
    whatYouSee: ['Red, programa, instructor, aprendices, nombre y número.', 'Buscador de aprendices con checkboxes.', 'Botones “Crear ficha” y “Cancelar”.'],
    figures: [fig('assets/capturas/43-admin-crear-ficha.jpg', 'Formulario de creación de ficha del administrador.', 'Alta de ficha con instructor y aprendices.', [{ n: 1, left: 35, top: 31, title: 'Red y programa', text: 'La red filtra los programas disponibles.' }, { n: 2, left: 35, top: 39, title: 'Instructor', text: 'Elige un instructor activo.' }, { n: 3, left: 35, top: 50, title: 'Aprendices', text: 'Marca los aprendices que pertenecen a la ficha.' }, { n: 4, left: 35, top: 80, title: 'Nombre y número', text: 'El número debe tener 4 a 8 dígitos y ser único.' }, { n: 5, left: 24, top: 91, title: 'Crear', text: 'Guarda la ficha y muestra la confirmación.' }])],
    fields: { head: ['Campo', 'Obligatorio', 'Regla'], rows: [['Red de conocimiento', 'Sí', 'Determina los programas.'], ['Programa de formación', 'Sí', 'Se habilita al elegir red.'], ['Instructor a cargo', 'Sí', 'Solo instructores activos.'], ['Aprendices de la ficha', 'No', 'Buscador y checkboxes; máximo 60 coincidencias visibles.'], ['Nombre de la ficha', 'Sí', 'Máximo 80 caracteres.'], ['Número de ficha', 'Sí', 'Solo dígitos, 4–8 caracteres, único.']] },
    steps: ['Elige red y programa.', 'Elige instructor.', 'Marca aprendices si ya existen.', 'Escribe el nombre.', 'Escribe el número.', 'Pulsa “Crear ficha”.'],
    after: ['La ficha aparece en la tabla y queda activa.', 'Los aprendices marcados quedan asociados.'],
    tip: 'Si el número ya existe, la pantalla te ofrece “Revisar soporte”.',
    errors: ['“Solo dígitos (4 a 8 caracteres)”.', '“El número ya está registrado en otra ficha”.', 'Si no hay instructores, crea uno primero.'],
    related: [{ id: 'admin-fichas', label: 'Volver a fichas' }, { id: 'admin-detalle-ficha', label: 'Detalle de ficha' }]
  },
  {
    id: 'admin-detalle-ficha', num: 'Cap. 39', part: 'parte-4', roles: ['admin'], title: 'Detalle de ficha del administrador',
    purpose: 'Edita todos los datos de una ficha, agrega o crea aprendices, revisa propuestas y elimina.',
    route: 'Pulsa “Ver” en una ficha. La ruta es <code>/admin/detalle-ficha/:id</code>.',
    whatYouSee: ['Información de la ficha con acciones Editar y Eliminar.', 'Aprendices con buscador, alta y asociación.', 'Propuestas con filtro por estado.', 'Modales de eliminación.'],
    figures: [fig('assets/capturas/42-admin-detalle-ficha.jpg', 'Detalle de ficha del administrador.', 'Ficha con aprendices y propuestas.', [{ n: 1, left: 25, top: 28, title: 'Información', text: 'Código, número, red, programa, instructor y estado.' }, { n: 2, left: 25, top: 65, title: 'Aprendices', text: 'Busca, agrega o crea aprendices.' }, { n: 3, left: 25, top: 80, title: 'Propuestas', text: 'Filtra por estado y abre el detalle.' }, { n: 4, left: 87, top: 26, title: 'Acciones', text: 'Editar o eliminar la ficha.' }])],
    fields: { head: ['Acción', 'Campos', 'Regla'], rows: [['Editar ficha', 'Nombre, número, red, programa, instructor, estado', 'Nombre, número, programa e instructor obligatorios; número de 4–8 dígitos.'], ['Agregar aprendiz', 'Búsqueda por nombre, correo, usuario institucional o documento', 'Solo usuarios disponibles.'], ['Crear aprendiz', 'Nombres, apellidos, documento, correo', 'Crea cuenta, asocia y envía activación.']] },
    steps: ['Pulsa “Editar” para cambiar los datos.', 'Guarda o cancela.', 'Busca un aprendiz para agregarlo.', 'Si no existe, pulsa “Crear aprendiz” y completa el formulario.', 'Revisa las propuestas de la ficha.', 'Elimina la ficha solo si es necesario.'],
    after: ['Los cambios se recargan.', 'Agregar un aprendiz existente no envía un nuevo enlace; crear uno nuevo sí.'],
    tip: 'Finalizar una ficha impide nuevas uniones, pero conserva aprendices y propuestas.',
    errors: ['“Ficha no encontrada”.', 'Número duplicado o fuera de rango.', 'Si el correo de activación falla, reenvía desde Usuarios.'],
    related: [{ id: 'admin-fichas', label: 'Volver a fichas' }, { id: 'admin-usuarios', label: 'Usuarios' }]
  },
  {
    id: 'admin-proyectos', num: 'Cap. 40', part: 'parte-4', roles: ['admin'], title: 'Supervisión de propuestas',
    purpose: 'Consulta y filtra todas las propuestas registradas en la plataforma.',
    route: 'Abre <code>/admin/proyectos</code>.',
    whatYouSee: ['Filtros por texto, estado, programa, ficha, instructor, área, porcentaje y fechas.', 'Tabla con propuesta, aprendiz, fecha, similitud, programa, estado y acción Ver.', 'Paginación de 8.'],
    figures: [fig('assets/capturas/44-admin-proyectos.jpg', 'Listado de propuestas del administrador.', 'Supervisión de propuestas.')],
    steps: ['Filtra por estado o programa.', 'Pulsa “Ver” para abrir el detalle.', 'Usa la paginación para recorrer la lista.'],
    after: ['La tabla muestra los datos actuales; no edita desde el listado.'],
    tip: 'Filtra por “En revisión” para ver el trabajo pendiente de los instructores.',
    errors: ['“Ninguna propuesta coincide”: limpia los filtros.', 'Si no hay propuestas, aún no se ha registrado ninguna.'],
    related: [{ id: 'admin-detalle-proyecto', label: 'Detalle de propuesta' }, { id: 'admin-similitudes', label: 'Similitudes' }]
  },
  {
    id: 'admin-detalle-proyecto', num: 'Cap. 41', part: 'parte-4', roles: ['admin'], title: 'Detalle y edición de propuesta',
    purpose: 'Revisa, cambia el estado, edita contenido, elimina y agrega observaciones a una propuesta.',
    route: 'Pulsa “Ver” en una propuesta. La ruta es <code>/admin/detalle-proyecto/:id</code>.',
    whatYouSee: ['Título, autor, estado y ficha.', 'Selector de estado y botón Guardar.', 'Botones Editar, Eliminar y Ver usuario.', 'Historial, similitudes y observaciones.'],
    figures: [fig('assets/capturas/45-admin-detalle-proyecto.jpg', 'Detalle de propuesta del administrador.', 'Supervisión y edición de una propuesta.')],
    fields: { head: ['Acción', 'Campos', 'Regla'], rows: [['Cambiar estado', 'En revisión, Aprobado, Rechazado', 'Guarda con versión para evitar conflictos.'], ['Editar contenido', 'Título, descripción, palabras clave', 'Título mínimo 5; descripción mínimo 20.'], ['Observación', 'Texto', 'Se agrega al hilo; se puede responder o eliminar.'], ['Eliminar', 'Confirmación', 'Solo si no tiene similitudes compartidas.']] },
    steps: ['Revisa la información y el historial.', 'Cambia el estado si corresponde y pulsa “Guardar”.', 'Pulsa “Editar” para modificar título, descripción o palabras clave.', 'Guarda el contenido.', 'Agrega una observación si necesitas comunicar algo.', 'Elimina solo si estás seguro.'],
    after: ['Si editas una propuesta aprobada, vuelve a “En revisión” por intervención administrativa.', 'Los cambios quedan en el historial y la bitácora.'],
    tip: 'Si hay conflicto de versión, recarga e inténtalo de nuevo; otra sesión pudo haber cambiado la propuesta.',
    errors: ['“La propuesta fue modificada por otra sesión”.', 'No se puede eliminar si tiene similitudes compartidas.', 'Título o descripción fuera de rango.'],
    related: [{ id: 'admin-proyectos', label: 'Volver a propuestas' }, { id: 'admin-similitudes', label: 'Similitudes' }]
  },
  {
    id: 'admin-similitudes', num: 'Cap. 42', part: 'parte-4', roles: ['admin'], title: 'Supervisión de similitudes',
    purpose: 'Supervisa todos los pares de similitud vigentes, con independencia de la ficha o instructor.',
    route: 'Abre <code>/admin/similitudes</code>.',
    whatYouSee: ['Resumen de pares, sobre umbral, programas y umbral.', 'Filtros por texto, estado, ficha, programa, instructor, porcentaje y fechas.', 'Tabla con propuestas A/B, similitud, estados, fecha y acciones Ver/Eliminar.'],
    figures: [fig('assets/capturas/46-admin-similitudes.jpg', 'Supervisión de similitudes del administrador.', 'Todos los pares vigentes.')],
    steps: ['Filtra por programa o porcentaje.', 'Pulsa “Ver” para comparar.', 'Pulsa “Eliminar” solo si la coincidencia no debe conservarse.', 'Confirma la eliminación.'],
    after: ['La tabla se recarga.', 'El motor puede volver a detectar una similitud eliminada al recalcular.'],
    tip: 'El botón “Ajustar motor” te lleva a la configuración del umbral y la ventana.',
    errors: ['“Sin coincidencias con el umbral vigente”: baja el umbral o amplía la ventana y recalcula.', '“Sin similitudes” con filtros: limpia los filtros.'],
    related: [{ id: 'admin-detalle-similitud', label: 'Detalle de similitud' }, { id: 'admin-motor', label: 'Configurar motor' }]
  },
  {
    id: 'admin-detalle-similitud', num: 'Cap. 43', part: 'parte-4', roles: ['admin'], title: 'Detalle de similitud del administrador',
    purpose: 'Compara el par y agrega observaciones a cualquiera de las dos propuestas.',
    route: 'Pulsa “Ver” en una similitud. La ruta es <code>/admin/detalle-similitud/:id</code>.',
    whatYouSee: ['Umbral, comparación A/B y tarjetas de las propuestas.', 'Métricas, términos y fragmentos.', 'Panel “Observaciones” con selector A/B y textarea.'],
    figures: [fig('assets/capturas/47-admin-detalle-similitud.jpg', 'Detalle de similitud para administrador.', 'Comparación y observaciones administrativas.')],
    fields: { head: ['Campo', 'Obligatorio', 'Regla'], rows: [['Propuesta para la observación', 'Sí', 'A o B.'], ['Observación', 'Sí', 'El botón se deshabilita si está vacío o se está enviando.']] },
    steps: ['Revisa la comparación y las métricas.', 'Elige la propuesta A o B.', 'Escribe la observación.', 'Pulsa “Agregar observación”.', 'Revisa la lista combinada de observaciones.'],
    after: ['La observación se agrega a la propuesta seleccionada.', 'No hay eliminación de la similitud desde esta pantalla.'],
    tip: 'Usa observaciones para dejar contexto; no son una decisión de estado.',
    errors: ['“Similitud no encontrada”.', 'No se puede publicar una observación vacía.'],
    related: [{ id: 'admin-similitudes', label: 'Volver a similitudes' }, { id: 'admin-detalle-proyecto', label: 'Abrir propuesta' }]
  },
  {
    id: 'admin-reportes', num: 'Cap. 44', part: 'parte-4', roles: ['admin'], title: 'Bandeja de soporte',
    purpose: 'Atiende las solicitudes de soporte de aprendices e instructores.',
    route: 'Abre <code>/admin/reportes-fallas</code>.',
    whatYouSee: ['Filtros por texto, estado, tipo, prioridad y fechas.', 'Tabla con solicitud, reportante, tipo, prioridad, estado, fecha y acciones.', 'Botones Ver y Eliminar.'],
    figures: [fig('assets/capturas/48-admin-reportes.jpg', 'Bandeja de soporte del administrador.', 'Solicitudes de soporte registradas.')],
    steps: ['Filtra por estado pendiente o por número de ficha.', 'Pulsa “Ver” para abrir el detalle.', 'Atiende la solicitud.', 'Elimina solo si es un duplicado o no corresponde.'],
    after: ['La bandeja se recarga con el estado actualizado.', 'Las solicitudes cerradas conservan su respuesta.'],
    tip: 'El buscador acepta #id, título, número de ficha, descripción o reportante.',
    errors: ['“No hay solicitudes registradas”.', '“Ninguna solicitud coincide”: limpia los filtros.'],
    related: [{ id: 'admin-detalle-reporte', label: 'Detalle de reporte' }, { id: 'aprendiz-soporte', label: 'Soporte del aprendiz' }]
  },
  {
    id: 'admin-detalle-reporte', num: 'Cap. 45', part: 'parte-4', roles: ['admin'], title: 'Detalle y respuesta de soporte',
    purpose: 'Cambia el estado de una solicitud, responde al solicitante y revisa sus datos.',
    route: 'Pulsa “Ver” en una solicitud. La ruta es <code>/admin/detalle-reporte/:id</code>.',
    whatYouSee: ['Información de la solicitud y estado actual.', 'Datos: tipo, prioridad, ficha, motivo, fechas y respuesta.', 'Formulario de estado y respuesta.', 'Información del reportante y botón “Ver usuario”.'],
    figures: [fig('assets/capturas/49-admin-detalle-reporte.jpg', 'Detalle de una solicitud de soporte.', 'Atención de soporte.')],
    fields: { head: ['Campo', 'Obligatorio', 'Regla'], rows: [['Cambiar estado', 'Sí', 'Pendiente, En revisión, Resuelto, Cerrado o Rechazado.'], ['Respuesta al solicitante', 'No', 'Máximo 2000 caracteres; se envía al cerrar.'], ['Guardar', '—', 'Se habilita si cambió el estado o la respuesta.']] },
    steps: ['Lee la descripción y los datos.', 'Cambia el estado.', 'Escribe una respuesta clara.', 'Pulsa “Guardar”.', 'Verifica el mensaje de actualización.'],
    after: ['La solicitud se actualiza y el solicitante recibe el aviso cuando se cierra.', 'La acción queda en la bitácora.'],
    tip: 'Explica qué se hizo y qué debe hacer la persona si el problema continúa.',
    errors: ['“Solicitud no encontrada”.', '“No se pudo cargar el reporte”: reintenta.', 'El botón Guardar está deshabilitado si no hay cambios.'],
    related: [{ id: 'admin-reportes', label: 'Volver a soporte' }, { id: 'admin-detalle-usuario', label: 'Ver usuario' }]
  },
  {
    id: 'admin-notificaciones', num: 'Cap. 46', part: 'parte-4', roles: ['admin'], title: 'Notificaciones del administrador',
    purpose: 'Consulta novedades del sistema y elimina notificaciones.',
    route: 'Abre <code>/admin/notificaciones</code> o pulsa la campana.',
    whatYouSee: ['Lista con tipo, fecha, título y “Nueva”.', 'Botón “Marcar todas como leídas”.', 'Botón “Eliminar” por notificación.'],
    figures: [fig('assets/capturas/50-admin-notificaciones.jpg', 'Notificaciones del administrador.', 'Bandeja de notificaciones del admin.')],
    steps: ['Pulsa una notificación para abrir su destino.', 'Marca todas como leídas si corresponde.', 'Elimina una notificación con confirmación.'],
    after: ['La campana se actualiza.', 'Eliminar una notificación no elimina la entidad relacionada.'],
    tip: 'Las notificaciones de reporte abren el detalle de soporte; las de propuesta o ficha abren su detalle.',
    errors: ['Si no hay notificaciones, se muestra “Sin notificaciones por ahora”.'],
    related: [{ id: 'admin-reportes', label: 'Soporte' }, { id: 'admin-proyectos', label: 'Propuestas' }]
  },
  {
    id: 'admin-perfil', num: 'Cap. 47', part: 'parte-4', roles: ['admin'], title: 'Perfil del administrador',
    purpose: 'Actualiza tus datos, foto, correo y contraseña.',
    route: 'Abre <code>/admin/perfil</code>.',
    whatYouSee: ['Información personal, rol y documento.', 'Edición de nombres/apellidos y foto.', 'Seguridad: cambio de correo y contraseña.'],
    figures: [fig('assets/capturas/51-admin-perfil.jpg', 'Perfil del administrador.', 'Datos y seguridad del admin.')],
    steps: ['Pulsa “Editar perfil” para cambiar nombres o apellidos.', 'Pulsa la foto para cambiarla.', 'Usa “Cambiar correo” con tu contraseña actual.', 'Usa “Cambiar contraseña” si necesitas.'],
    after: ['Los cambios se reflejan en la barra superior.', 'El correo y la contraseña se actualizan de inmediato.'],
    tip: 'Como admin, protege tu cuenta: usa una contraseña larga y no compartas tu sesión.',
    errors: ['Correo duplicado o inválido.', 'Contraseña nueva menor de 12 caracteres.', 'La confirmación no coincide.'],
    related: [{ id: 'admin-usuarios', label: 'Usuarios' }, { id: 'admin-bitacora', label: 'Bitácora' }]
  },
  {
    id: 'admin-bitacora', num: 'Cap. 48', part: 'parte-4', roles: ['admin'], title: 'Bitácora de acciones',
    purpose: 'Audita quién hizo qué, cuándo y sobre qué entidad.',
    route: 'Abre <code>/admin/bitacora</code>.',
    whatYouSee: ['Filtros por texto, responsable, rol, acción, entidad, fechas, orden y tamaño de página.', 'Tabla con fecha, responsable, acción, entidad y detalle.', 'Botón Detalle para expandir una fila.'],
    figures: [fig('assets/capturas/52-admin-bitacora.jpg', 'Bitácora de acciones del administrador.', 'Registro de auditoría.')],
    steps: ['Filtra por acción o entidad.', 'Ajusta las fechas.', 'Pulsa “Detalle” para ver los datos completos.', 'Cambia el orden o el tamaño de página.'],
    after: ['La bitácora es de solo lectura; no se edita ni se elimina desde aquí.'],
    tip: 'Usa la bitácora para investigar cambios de usuarios, propuestas, fichas, motor o soporte.',
    errors: ['“Ningún registro coincide”: limpia los filtros.', '“Sin acciones registradas”: aún no hay actividad.'],
    related: [{ id: 'admin-usuarios', label: 'Usuarios' }, { id: 'admin-motor', label: 'Motor' }]
  },
  {
    id: 'admin-redes', num: 'Cap. 49', part: 'parte-4', roles: ['admin'], title: 'Redes y programas',
    purpose: 'Gestiona las redes de conocimiento y sus programas; cada ficha pertenece a un programa.',
    route: 'Abre <code>/admin/redes-conocimiento</code>.',
    whatYouSee: ['Título “Redes de Conocimiento” y botón “Nueva Red”.', 'Filtros por nombre y uso.', 'Tabla con red, programas, fichas y acciones.', 'Formulario de creación/edición con nombre y programas.'],
    figures: [fig('assets/capturas/53-admin-redes.jpg', 'Redes de conocimiento y programas.', 'Gestión de redes y programas.')],
    fields: { head: ['Campo', 'Obligatorio', 'Regla'], rows: [['Nombre de la red', 'Sí', 'Mínimo 3 caracteres; máximo 80; no se repite.'], ['Programas', 'Sí', 'Mínimo 1; máximo 60 por nombre; no se repiten dentro de la red ni en otra.']] },
    steps: ['Pulsa “Nueva Red” o “Editar”.', 'Escribe el nombre.', 'Agrega uno o más programas.', 'Quita los programas que ya no correspondan.', 'Pulsa “Crear red” o “Guardar cambios”.'],
    after: ['La red y sus programas se actualizan.', 'Quitar un programa elimina sus fichas; aprendices y propuestas se conservan sin ficha ni programa.'],
    tip: 'Antes de eliminar una red, revisa cuántas fichas están en uso.',
    errors: ['Nombre duplicado.', 'Programa duplicado.', 'Debe haber al menos un programa.'],
    related: [{ id: 'admin-fichas', label: 'Fichas' }, { id: 'admin-crear-ficha', label: 'Crear ficha' }]
  },
  {
    id: 'admin-motor', num: 'Cap. 50', part: 'parte-4', roles: ['admin'], title: 'Motor de similitudes',
    purpose: 'Ajusta el umbral y la ventana del motor, y recalcula la base existente.',
    route: 'Abre <code>/admin/config-similitud</code>.',
    whatYouSee: ['Tira de lectura: umbral, ventana y coincidencias.', 'Parámetros de detección con umbral y meses.', 'Botones Guardar parámetros y Recalcular base existente.', 'Explicación del corpus.'],
    figures: [fig('assets/capturas/54-admin-motor.jpg', 'Configuración del motor de similitudes.', 'Parámetros y recálculo del motor.', [{ n: 1, left: 30, top: 44, title: 'Umbral', text: 'Porcentaje mínimo entre 5 y 95.' }, { n: 2, left: 30, top: 58, title: 'Ventana', text: 'Antigüedad máxima del corpus entre 1 y 60 meses.' }, { n: 3, left: 30, top: 71, title: 'Guardar', text: 'Aplica a las próximas detecciones.' }, { n: 4, left: 40, top: 71, title: 'Recalcular', text: 'Recalcula la base existente con los parámetros actuales.' }])],
    fields: { head: ['Campo', 'Obligatorio', 'Regla'], rows: [['Porcentaje límite de coincidencia', 'Sí', 'Número entre 5 y 95.'], ['Antigüedad máxima del corpus', 'Sí', 'Número entre 1 y 60 meses.']] },
    steps: ['Ajusta el umbral.', 'Ajusta los meses.', 'Pulsa “Guardar parámetros”.', 'Si quieres aplicar a la base existente, pulsa “Recalcular base existente” y confirma.'],
    after: ['El nuevo umbral cambia la clasificación A/B/C en toda la aplicación.', 'El recálculo elimina y crea coincidencias según los parámetros.'],
    tip: 'Un umbral más alto reduce coincidencias; uno más bajo muestra más pares. Cambia con criterio y documenta el motivo.',
    errors: ['Umbral fuera de 5–95 o meses fuera de 1–60.', 'El recálculo puede tardar; espera el mensaje de resultado.', 'Si falla, revisa la conexión y vuelve a intentarlo.'],
    related: [{ id: 'admin-similitudes', label: 'Similitudes' }, { id: 'admin-dashboard', label: 'Dashboard' }]
  },
  {
    id: 'admin-flujo-gestion', num: 'Cap. 51', part: 'parte-4', roles: ['admin'], title: 'Flujo completo de administración',
    purpose: 'Resume la creación de usuarios, fichas y la supervisión de propuestas, similitudes y soporte.',
    route: 'Empieza en <code>/admin/dashboard</code>.',
    whatYouSee: ['Secuencia completa de administración.'],
    steps: ['Crea o revisa la red y sus programas.', 'Crea usuarios de aprendices e instructores.', 'Crea una ficha y asigna instructor y aprendices.', 'Verifica que el instructor vea la ficha.', 'Supervisa propuestas y similitudes.', 'Atiende reportes de soporte.', 'Revisa la bitácora y el estado del motor.'],
    after: ['Cada acción queda registrada y se refleja en los paneles de los roles.'],
    tip: 'Antes de crear una ficha, confirma que la red, el programa y el instructor existan.',
    errors: commonErrors,
    related: [{ id: 'admin-usuarios', label: 'Usuarios' }, { id: 'admin-fichas', label: 'Fichas' }, { id: 'admin-reportes', label: 'Soporte' }]
  },

  /* ------------------------------------------------------------------ PARTE V */
  {
    id: 'tablas-filtros', num: 'Cap. 52', part: 'parte-5', roles: ['todos'], title: 'Tablas, filtros y paginación',
    purpose: 'Explica cómo leer y usar los listados de toda la aplicación.',
    route: 'Aparece en usuarios, fichas, propuestas, similitudes, soporte y bitácora.',
    whatYouSee: ['Barra de filtros plegable.', 'Campo de búsqueda y selectores.', 'Conteo de resultados.', 'Tabla o tarjetas con paginación.', 'Acciones por fila.'],
    figures: [fig('assets/capturas/46-admin-similitudes.jpg', 'Ejemplo de tabla con filtros y acciones.', 'Patrón de tabla y filtros.')],
    steps: ['Abre o cierra los filtros con el encabezado.', 'Escribe una búsqueda.', 'Selecciona estado, ficha o programa.', 'Pulsa “Limpiar filtros” si no ves resultados.', 'Usa la paginación.'],
    after: ['Los filtros no modifican los datos; solo cambian lo que ves.'],
    tip: 'Si una búsqueda no devuelve nada, prueba con una palabra más corta o sin tildes.',
    errors: ['“Ningún resultado”: limpia los filtros.', 'Una fila no tiene acción porque tu rol no lo permite.'],
    related: [{ id: 'formularios-validaciones', label: 'Formularios' }, { id: 'errores-frecuentes', label: 'Errores' }]
  },
  {
    id: 'formularios-validaciones', num: 'Cap. 53', part: 'parte-5', roles: ['todos'], title: 'Formularios y validaciones',
    purpose: 'Explica los campos obligatorios, los mensajes de error y las reglas comunes.',
    route: 'Aparece en login, propuestas, fichas, usuarios, perfil y soporte.',
    whatYouSee: ['Etiquetas con asterisco * para obligatorios.', 'Texto de ayuda bajo el campo.', 'Mensajes de error junto al campo.', 'Botones Guardar, Cancelar y Enviar.'],
    figures: [fig('assets/capturas/12-aprendiz-crear-propuesta.jpg', 'Formulario con campos obligatorios y ayudas.', 'Patrón de formulario.')],
    steps: ['Lee la etiqueta y la ayuda.', 'Completa los campos con asterisco.', 'Corrige los mensajes en rojo.', 'Pulsa Guardar o Enviar una sola vez.', 'Espera la confirmación.'],
    after: ['Si el formulario se guarda, la pantalla se recarga o muestra un mensaje de éxito.'],
    tip: 'No cierres la pestaña mientras se está guardando. Si la sesión expira, copia el texto antes de recargar.',
    errors: ['“Campo requerido”: falta un obligatorio.', '“Formato inválido”: revisa correo, documento o contraseña.', 'Si el botón queda deshabilitado, espera o revisa los campos.'],
    related: [{ id: 'errores-frecuentes', label: 'Errores frecuentes' }, { id: 'preguntas-frecuentes', label: 'Preguntas frecuentes' }]
  },
  {
    id: 'errores-frecuentes', num: 'Cap. 54', part: 'parte-5', roles: ['todos'], title: 'Errores frecuentes y cómo resolverlos',
    purpose: 'Reúne los mensajes más comunes y su solución.',
    route: 'No aplica.',
    whatYouSee: ['Errores de conexión, sesión, permisos, validación y conflictos.'],
    steps: ['Lee el mensaje exacto.', 'Revisa esta tabla.', 'Si no se resuelve, usa Soporte con la ruta y el mensaje.'],
    after: ['La mayoría de errores se resuelven recargando, corrigiendo un campo o volviendo a iniciar sesión.'],
    extra: [{ title: 'Tabla de errores', html: '<div class="table-wrap"><table><thead><tr><th>Mensaje o código</th><th>Qué significa</th><th>Qué hacer</th></tr></thead><tbody><tr><td>401 / sesión expirada</td><td>Tu sesión ya no es válida.</td><td>Vuelve a iniciar sesión.</td></tr><tr><td>403 / acceso denegado</td><td>Tu rol no tiene permiso.</td><td>Vuelve a tu panel o pide permisos.</td></tr><tr><td>404 / no encontrado</td><td>La URL o el recurso no existe.</td><td>Revisa la ruta o vuelve al listado.</td></tr><tr><td>409 / conflicto</td><td>Otra sesión modificó el registro.</td><td>Recarga y vuelve a intentarlo.</td></tr><tr><td>422 / validación</td><td>Un campo no cumple una regla.</td><td>Corrige el campo indicado.</td></tr><tr><td>429 / demasiados intentos</td><td>Se superó el límite.</td><td>Espera unos minutos.</td></tr><tr><td>500 / error del servidor</td><td>Falla interna.</td><td>Reintenta o reporta soporte.</td></tr><tr><td>“No se pudo conectar”</td><td>Problema de red o servidor.</td><td>Revisa conexión y reintenta.</td></tr></tbody></table></div>' }],
    tip: 'Cuando reportes un error, incluye la ruta, el botón que pulsaste, el mensaje exacto y la hora.',
    errors: commonErrors,
    related: [{ id: 'formularios-validaciones', label: 'Formularios' }, { id: 'aprendiz-soporte', label: 'Soporte' }]
  },
  {
    id: 'preguntas-frecuentes', num: 'Cap. 55', part: 'parte-5', roles: ['todos'], title: 'Preguntas frecuentes',
    purpose: 'Responde dudas comunes sin necesidad de soporte.',
    route: 'No aplica.',
    whatYouSee: ['Preguntas sobre cuenta, ficha, propuestas, similitudes, permisos y soporte.'],
    steps: ['Busca tu duda en la lista.', 'Si no aparece, usa el buscador del manual.', 'Si sigue sin resolverse, usa Soporte.'],
    after: ['Las respuestas remiten a los capítulos detallados.'],
    extra: [{ title: 'Preguntas y respuestas', html: '<div class="table-wrap"><table><thead><tr><th>Pregunta</th><th>Respuesta breve</th></tr></thead><tbody><tr><td>¿Cómo obtengo una cuenta?</td><td>El administrador crea la cuenta y envía un enlace de activación.</td></tr><tr><td>¿Puedo registrarme solo?</td><td>No. El alta la realiza el administrador.</td></tr><tr><td>¿Cómo entro a una ficha?</td><td>Con el código que comparte el instructor.</td></tr><tr><td>¿Puedo cambiar de ficha?</td><td>Sí, sal de la actual y únete a otra. Tus propuestas se conservan.</td></tr><tr><td>¿Por qué no veo “Enviar” en una propuesta?</td><td>No está en borrador/rechazada o la ficha no está activa.</td></tr><tr><td>¿Qué significa una similitud alta?</td><td>Es una señal para revisar y citar fuentes; no es un veredicto.</td></tr><tr><td>¿Puedo eliminar una propuesta?</td><td>Solo el creador o el admin, según el estado y las similitudes.</td></tr><tr><td>¿Dónde veo mis notificaciones?</td><td>En la campana o en la sección Notificaciones.</td></tr><tr><td>¿Cómo reporto un error?</td><td>Usa Soporte en el footer o el menú.</td></tr><tr><td>¿Por qué no veo una sección?</td><td>Tu rol no tiene permiso.</td></tr></tbody></table></div>' }],
    related: [{ id: 'errores-frecuentes', label: 'Errores frecuentes' }, { id: 'aprendiz-soporte', label: 'Soporte' }]
  },
  {
    id: 'checklist', num: 'Cap. 56', part: 'parte-5', roles: ['todos'], title: 'Checklist y autoevaluación',
    purpose: 'Ayuda a comprobar que una operación quedó completa.',
    route: 'No aplica.',
    whatYouSee: ['Listas de verificación por rol.', 'Preguntas de autoevaluación.'],
    steps: ['Marca las comprobaciones que ya cumpliste.', 'Si algo falta, abre el capítulo relacionado.', 'Repite la operación si es necesario.'],
    after: ['Si todas las comprobaciones están bien, la operación quedó registrada.'],
    extra: [{ title: 'Aprendiz', html: '<ul><li>Pertenezco a una ficha y veo su nombre en Mi Ficha.</li><li>Mi propuesta tiene título, descripción, objetivo general y al menos 2 objetivos específicos.</li><li>El equipo y el área son correctos.</li><li>La propuesta aparece con el estado esperado.</li><li>Revisé mis notificaciones y similitudes.</li></ul>' }, { title: 'Instructor', html: '<ul><li>Veo mis fichas y sus códigos.</li><li>La cola de revisión muestra las propuestas pendientes.</li><li>Revisé la similitud antes de decidir.</li><li>La propuesta salió de pendientes.</li><li>El aprendiz recibió la notificación.</li></ul>' }, { title: 'Administrador', html: '<ul><li>El usuario quedó creado y activo.</li><li>La ficha tiene instructor y aprendices.</li><li>La propuesta y la similitud se ven en supervisión.</li><li>El reporte de soporte quedó cerrado con respuesta.</li><li>La acción aparece en la bitácora.</li></ul>' }],
    related: [{ id: 'aprendiz-flujo-completo', label: 'Flujo aprendiz' }, { id: 'instructor-flujo-revision', label: 'Flujo instructor' }, { id: 'admin-flujo-gestion', label: 'Flujo admin' }, { id: 'normas-iso', label: 'Normas ISO en el desarrollo' }]
  },
  {
    id: 'normas-iso', num: 'Cap. 57', part: 'parte-6', roles: ['todos'], title: 'Aplicación de las normas ISO en ProyecTWIN',
    purpose: 'Documenta cómo el equipo aplicó principios de ocho referencias normativas durante el desarrollo, la calidad, la seguridad y la documentación de ProyecTWIN. No es una auditoría, no declara certificación y no enseña al usuario a aplicar las normas.',
    route: 'No aplica. Es un capítulo de trazabilidad del desarrollo y de las decisiones del proyecto.',
    whatYouSee: ['Introducción y estados de evidencia.', 'Una ficha por cada norma.', 'Matriz de aplicación, evidencias y límites.', 'Conclusiones y referencias verificadas.'],
    steps: ['Lee la introducción y la leyenda de estados.', 'Revisa la ficha de cada norma.', 'Contrasta la evidencia con el estado declarado.', 'Usa la matriz para comparar alcance y límites.', 'No interpretes el capítulo como una certificación.'],
    after: ['El capítulo deja claro qué se aplicó, qué fue parcial, qué quedó como criterio de diseño y qué no tiene evidencia suficiente.'],
    tip: 'La evidencia manda: si una práctica no está en código, pruebas, Git o documentación, se marca como pendiente o sin evidencia.',
    related: [{ id: 'control-documental', label: 'Control documental' }, { id: 'checklist', label: 'Checklist' }, { id: 'admin-bitacora', label: 'Bitácora' }],
    extra: [
      {
        id: 'normas-iso-intro',
        title: 'Introducción y alcance',
        html: '<div class="callout note"><div class="callout-head"><span class="callout-icon" aria-hidden="true">ℹ</span><span>Qué documenta este capítulo</span></div><div class="callout-body"><p>Este capítulo explica cómo el equipo tomó principios de ocho referencias normativas para tomar decisiones de desarrollo, seguridad, calidad, documentación y procesos. No afirma que ProyecTWIN esté certificado ni que cumpla todos los requisitos. Tampoco es una guía para que el lector aplique las normas: el protagonista es el trabajo realizado en el proyecto.</p></div></div><p>La evidencia se buscó en el código, las pruebas, el historial Git, la documentación técnica y los procedimientos de revisión. Cuando no se encontró evidencia, se indica expresamente.</p><div class="table-wrap"><table class="tabla-iso"><thead><tr><th>Estado</th><th>Qué significa en este capítulo</th></tr></thead><tbody><tr><td><strong>Aplicado y comprobado</strong></td><td>La práctica está implementada y existe evidencia verificable, como pruebas o registro de auditoría.</td></tr><tr><td><strong>Aplicado parcialmente</strong></td><td>La práctica existe, pero no cubre todos los aspectos de la norma o no está evaluada formalmente.</td></tr><tr><td><strong>Considerado como criterio de diseño</strong></td><td>Influyó en decisiones técnicas o metodológicas, sin implementación completa.</td></tr><tr><td><strong>Pendiente / sin evidencia</strong></td><td>No se encontró evidencia suficiente; no se atribuye al proyecto.</td></tr><tr><td><strong>Referencia histórica</strong></td><td>Norma retirada o sustituida; se cita solo para explicar su relación con el modelo actual.</td></tr></tbody></table></div><p><strong>Contenido:</strong> <a class="xref" href="#normas-iso-27001">ISO/IEC 27001</a> · <a class="xref" href="#normas-iso-14001">ISO 14001</a> · <a class="xref" href="#normas-iso-25010">ISO/IEC 25010</a> · <a class="xref" href="#normas-iso-9126">ISO/IEC 9126-1</a> · <a class="xref" href="#normas-iso-37001">ISO 37001</a> · <a class="xref" href="#normas-iso-10005">ISO 10005</a> · <a class="xref" href="#normas-iso-9001">ISO 9001</a> · <a class="xref" href="#normas-iso-33000">ISO/IEC 33000</a> · <a class="xref" href="#normas-iso-matriz">Matriz</a> · <a class="xref" href="#normas-iso-conclusiones">Conclusiones</a></p><p><strong>Alias de búsqueda:</strong> ISO 27001, ISO 25010 e ISO 33000; las referencias oficiales son ISO/IEC 27001, ISO/IEC 25010 y la familia ISO/IEC 330xx.</p>'
      },
      {
        id: 'normas-iso-27001',
        title: 'ISO/IEC 27001:2022 — Seguridad de la información',
        html: '<p><strong>Propósito:</strong> aportar un marco para gestionar la seguridad de la información: confidencialidad, integridad, disponibilidad y responsabilidades.</p><p><strong>Cómo se aplicó en el desarrollo:</strong> el equipo adoptó controles concretos de autenticación, autorización y trazabilidad, aunque no implantó un sistema de gestión de seguridad de la información completo.</p><ul><li>El login es exclusivamente por correo; el <code>username</code> histórico se rechaza como credencial.</li><li>La activación y la recuperación de contraseña usan enlaces enviados al correo; no se generan contraseñas temporales visibles.</li><li>Los roles aprendiz, instructor y admin delimitan rutas y acciones; el backend vuelve a validar los permisos.</li><li>La web usa cookie httpOnly y la app Android usa token de Sanctum; el cierre de sesión y los flujos de restablecimiento revocan el acceso.</li><li>Los campos sensibles se ocultan por defecto y la bitácora registra acciones administrativas relevantes.</li></ul><p><strong>Decisiones y mecanismos:</strong> prohibición de autenticar por <code>username</code>, validación de permisos en servidor, límites de intentos, ocultamiento de datos personales y auditoría de acciones sensibles.</p><p><strong>Evidencia:</strong> pruebas de autenticación y autorización del backend, pruebas de contrato del login Android, configuración de sesiones y bitácora. <strong>Límite:</strong> no hay evidencia de un SGSI formal, análisis de riesgos, revisión periódica de accesos ni certificación; tampoco se implementó MFA, control recomendable según el riesgo pero no exigido literalmente por la norma. Estado: <strong>aplicado parcialmente y comprobado</strong>.</p>'
      },
      {
        id: 'normas-iso-14001',
        title: 'ISO 14001:2026 — Gestión ambiental',
        html: '<p><strong>Propósito:</strong> establecer un sistema de gestión ambiental con política, objetivos, aspectos ambientales, cumplimiento y mejora.</p><p><strong>Qué se encontró en ProyecTWIN:</strong> no se localizó una política ambiental, objetivos medibles, matriz de aspectos ambientales, medición de consumo, plan de residuos electrónicos ni auditoría ambiental. La digitalización del manual y de los flujos de propuestas, fichas y soporte reduce papel e impresiones, pero es una práctica operativa indirecta, no un sistema de gestión ambiental.</p><p><strong>Evidencia:</strong> manual HTML/PDF y flujos electrónicos; ausencia de documentación ambiental específica. <strong>Límite:</strong> no se atribuye a ProyecTWIN una aplicación de ISO 14001. Para aplicarla, la organización tendría que definir política, objetivos, aspectos, responsables y métricas. Estado: <strong>sin evidencia de aplicación formal; práctica indirecta</strong>.</p>'
      },
      {
        id: 'normas-iso-25010',
        title: 'ISO/IEC 25010:2023 — Calidad del producto software',
        html: '<p><strong>Propósito:</strong> ofrecer un modelo para especificar, medir y evaluar la calidad del producto software.</p><p><strong>Cómo se aplicó en el desarrollo:</strong> el equipo usó características de calidad como criterios de diseño y verificación, sin realizar una evaluación formal del modelo.</p><ul><li><strong>Adecuación funcional:</strong> el manual relaciona cada función con un resultado esperado y un flujo verificable.</li><li><strong>Capacidad de interacción (usabilidad en 25010:2011):</strong> navegación por roles, formularios con ayudas, mensajes de error y manual con capturas y recorridos.</li><li><strong>Fiabilidad:</strong> validaciones, manejo de sesión expirada, mensajes de conflicto y pruebas automatizadas.</li><li><strong>Seguridad:</strong> autenticación, permisos y trazabilidad, relacionados con ISO/IEC 27001.</li><li><strong>Compatibilidad y mantenibilidad:</strong> frontend React, API Laravel y app Android con pruebas de contrato en Android; separación de responsabilidades y pruebas por capa.</li></ul><p><strong>Evidencia:</strong> suites de pruebas del backend, frontend, Android y E2E; lint, build y pruebas puntuales de accesibilidad en componentes. <strong>Límite:</strong> no se definieron métricas formales de calidad ni una evaluación completa de 25010; la aplicación es parcial y como criterio de diseño. Estado: <strong>aplicado parcialmente</strong>.</p>'
      },
      {
        id: 'normas-iso-9126',
        title: 'ISO/IEC 9126-1:2001 — Referencia histórica',
        html: '<p><strong>Propósito histórico:</strong> ISO/IEC 9126-1:2001 organizó la calidad del software en características y subcaracterísticas y fue un antecedente de los modelos actuales.</p><p><strong>Relación con ProyecTWIN:</strong> no se encontró evidencia de que el equipo haya usado ISO/IEC 9126 como norma de trabajo. Se menciona únicamente para explicar su relación histórica con ISO/IEC 25010. La edición 9126-1:2001 está retirada y fue sustituida por ISO/IEC 25010:2011, que a su vez fue reemplazada por ISO/IEC 25010:2023.</p><p><strong>Estado:</strong> <strong>referencia histórica; no aplicado</strong>. No debe presentarse como norma vigente ni usarse para declarar conformidad.</p>'
      },
      {
        id: 'normas-iso-37001',
        title: 'ISO 37001:2025 — Prevención del soborno',
        html: '<p><strong>Propósito:</strong> establecer un sistema de gestión antisoborno con política, liderazgo, evaluación de riesgos, debida diligencia, controles, formación y canales de reporte.</p><p><strong>Qué se encontró en ProyecTWIN:</strong> no se localizó política antisoborno, evaluación de riesgos de soborno, debida diligencia de terceros, canal de denuncias ni formación específica. Existen roles, bitácora y un módulo de soporte que aportan transparencia y trazabilidad, pero no equivalen a un sistema antisoborno.</p><p><strong>Evidencia:</strong> bitácora de acciones y reportes de soporte; ausencia de documentación antisoborno. <strong>Límite:</strong> un canal ético, la debida diligencia y la declaración de conflictos de interés dependen de la organización. Estado: <strong>sin evidencia de aplicación formal; controles de transparencia no equivalentes</strong>.</p>'
      },
      {
        id: 'normas-iso-10005',
        title: 'ISO 10005:2018 — Planes de calidad',
        html: '<p><strong>Propósito:</strong> dar directrices para establecer, revisar, aceptar, aplicar y revisar planes de calidad. Son directrices, no requisitos certificables por sí mismas.</p><p><strong>Cómo se aplicó en el desarrollo:</strong> el equipo planificó la verificación con suites de pruebas, un gate E2E, lint, build y un checklist de operaciones. El manual y sus capturas se revisan y regeneran cuando cambia la interfaz.</p><ul><li>Pruebas backend con base desechable y guardas de aislamiento.</li><li>Pruebas frontend, lint y build de producción.</li><li>Gate E2E con 25 pruebas; revisión de accesibilidad en componentes seleccionados.</li><li>Checklist de operaciones y control documental del manual.</li></ul><p><strong>Evidencia:</strong> <code>CI.md</code>, workflow de GitHub Actions, <code>docs/E2E-GATE.md</code>, suites de pruebas y checklist del manual. <strong>Límite:</strong> no existe un plan de calidad formal por release con responsables y criterios de aceptación firmados; el workflow de CI se ejecutó en GitHub Actions (9 corridas exitosas al 2 de noviembre de 2026; la última sobre el commit <code>99806b5</code>), aunque no incluye E2E, Android ni auditoría de dependencias. Estado: <strong>aplicado parcialmente</strong>.</p>'
      },
      {
        id: 'normas-iso-9001',
        title: 'ISO 9001:2026 — Gestión de la calidad',
        html: '<p><strong>Propósito:</strong> establecer un sistema de gestión de la calidad con liderazgo, planificación, gestión de la información documentada, evaluación del desempeño y mejora continua.</p><p><strong>Cómo se aplicó en el desarrollo:</strong> el equipo mantuvo control documental del manual, historial Git con commits separados por área, pruebas automatizadas, reportes de soporte y auditorías internas del código y de la documentación.</p><ul><li>El manual tiene título, versión, fecha, alcance y fuente de verdad.</li><li>Los cambios se registran en Git y se separan por backend, frontend, Android y documentación.</li><li>Los errores se atienden mediante soporte y se corrigen con pruebas de regresión.</li><li>Las auditorías internas y los inventarios de pendientes alimentan la mejora.</li></ul><p><strong>Evidencia:</strong> historial Git, control documental, bitácora, reportes de soporte y suites de pruebas. <strong>Límite:</strong> no se declara certificación; no hay evidencia de política de calidad formal, revisión por la dirección ni auditoría interna según ISO 9001. Estado: <strong>aplicado parcialmente</strong>.</p>'
      },
      {
        id: 'normas-iso-33000',
        title: 'ISO/IEC 33000 — Evaluación de procesos',
        html: '<p><strong>Propósito:</strong> ISO/IEC 33000 no es una norma única; es la familia ISO/IEC 330xx para evaluar procesos. Define conceptos, requisitos de evaluación, marcos de medición, modelos y evaluación de capacidad.</p><p><strong>Cómo se usó en el proyecto:</strong> se tomó como referencia conceptual para ordenar procesos, actividades, roles, resultados y evidencias del desarrollo: alta de usuarios, activación, login, propuestas, revisión, soporte y cierre de release.</p><p><strong>Partes consideradas:</strong> ISO/IEC 33001:2015 (conceptos y terminología), 33002:2015 (requisitos para realizar evaluaciones), 33003:2015 (marcos de medición), 33004:2015 (modelos de referencia, evaluación y madurez) y 33020:2019 (capacidad de proceso).</p><p><strong>Evidencia:</strong> descripción de flujos y roles, checklist, pruebas y bitácora. <strong>Límite:</strong> no se realizó una evaluación formal de capacidad, no se definió un assessor competente ni se emitió una calificación. Estado: <strong>considerado como criterio de diseño; sin evaluación formal</strong>.</p>'
      },
      {
        id: 'normas-iso-matriz',
        title: 'Matriz de evidencias y estado',
        html: '<p>La matriz resume la aplicación real en el proyecto. No es una declaración de conformidad: cada fila indica qué se hizo, qué evidencia existe y qué límite se reconoce.</p><div class="table-wrap"><table class="tabla-iso tabla-iso-desarrollo"><thead><tr><th>Norma</th><th>Cómo se aplicó en el desarrollo</th><th>Evidencia</th><th>Estado</th><th>Límite u observación</th></tr></thead><tbody><tr><td><strong>ISO/IEC 27001</strong><br>2022</td><td>Autenticación por correo, roles, sesiones, activación/reset, validación backend y bitácora.</td><td>Pruebas de autenticación/autorización, configuración de sesiones y auditoría.</td><td>Aplicado parcialmente y comprobado</td><td>Sin SGSI formal ni análisis de riesgos; sin MFA como control adicional (medida recomendable según el riesgo) ni revisión periódica de accesos.</td></tr><tr><td><strong>ISO 14001</strong><br>2026</td><td>Digitalización del manual y de los flujos; reducción indirecta de impresiones.</td><td>Manual HTML/PDF y flujos electrónicos.</td><td>Sin evidencia formal; práctica indirecta</td><td>No hay política, objetivos, aspectos ambientales ni métricas. No se atribuye aplicación.</td></tr><tr><td><strong>ISO/IEC 25010</strong><br>2023</td><td>Calidad como criterio de diseño: capacidad de interacción, fiabilidad, seguridad, compatibilidad y mantenibilidad.</td><td>Suites backend/frontend/Android/E2E, lint, build y pruebas puntuales de accesibilidad.</td><td>Aplicado parcialmente</td><td>Sin evaluación formal ni métricas de calidad del modelo.</td></tr><tr><td><strong>ISO/IEC 9126-1</strong><br>2001</td><td>Antecedente histórico de los modelos de calidad; no se usó como norma de trabajo.</td><td>Referencia bibliográfica.</td><td>Referencia histórica; no aplicado</td><td>Retirada y sustituida por ISO/IEC 25010. No usar como norma vigente.</td></tr><tr><td><strong>ISO 37001</strong><br>2025</td><td>Transparencia mediante roles, bitácora y soporte; no es un sistema antisoborno.</td><td>Bitácora y reportes de soporte.</td><td>Sin evidencia formal; controles no equivalentes</td><td>Sin política, riesgos de soborno, debida diligencia ni canal de denuncias.</td></tr><tr><td><strong>ISO 10005</strong><br>2018</td><td>Planificación de pruebas, gate E2E, lint/build y checklist de operaciones.</td><td>CI, E2E, suites de pruebas y checklist del manual.</td><td>Aplicado parcialmente</td><td>Sin plan de calidad formal por release ni aceptación firmada.</td></tr><tr><td><strong>ISO 9001</strong><br>2026</td><td>Control documental, historial Git, pruebas, soporte, auditorías y mejora.</td><td>Git, manual, bitácora, soporte y pruebas.</td><td>Aplicado parcialmente</td><td>Sin certificación, política de calidad formal ni revisión por la dirección.</td></tr><tr><td><strong>ISO/IEC 33000</strong></td><td>Referencia para ordenar procesos, roles, actividades, resultados y evidencias.</td><td>Flujos documentados, checklist, pruebas y bitácora.</td><td>Criterio de diseño; sin evaluación formal</td><td>Sin assessment, assessor competente ni calificación de capacidad.</td></tr></tbody></table></div>'
      },
      {
        id: 'normas-iso-conclusiones',
        title: 'Conclusiones',
        html: '<ul><li><strong>Mayor evidencia:</strong> ISO/IEC 27001, ISO/IEC 25010, ISO 9001 e ISO 10005, aplicadas parcialmente mediante controles, pruebas, control documental y mejora continua.</li><li><strong>Aplicación indirecta:</strong> ISO 14001 solo aparece como digitalización y reducción de impresiones; no hay sistema de gestión ambiental.</li><li><strong>Sin evidencia formal:</strong> ISO 37001 no se aplicó como sistema antisoborno; la bitácora y el soporte no sustituyen política, riesgos, debida diligencia ni canal ético.</li><li><strong>Referencia histórica:</strong> ISO/IEC 9126-1:2001 no es vigente y no se usó como norma de trabajo; su legado se lee a través de ISO/IEC 25010.</li><li><strong>Procesos:</strong> ISO/IEC 33000 se usó como marco conceptual para ordenar procesos y evidencias, no como evaluación formal de capacidad.</li></ul><p><strong>Conclusión general:</strong> ProyecTWIN documenta prácticas reales de seguridad, calidad, control documental y procesos, pero no declara certificación ni cumplimiento integral de las ocho referencias normativas. Las brechas se registran como pendientes y no se ocultan.</p>'
      },
      {
        id: 'normas-iso-referencias',
        title: 'Referencias y estado de las normas',
        html: '<p>Ediciones y estado consultados en el catálogo oficial de ISO en noviembre de 2026. Las normas se revisan periódicamente; antes de una auditoría o certificación, confirma la edición vigente con el organismo correspondiente. Las descripciones de este capítulo son redacción propia y no reproducen el texto de las normas.</p><div class="table-wrap"><table class="tabla-iso tabla-iso-referencias"><thead><tr><th>Norma</th><th>Edición y estado</th><th>Uso en este capítulo</th><th>Fuente</th></tr></thead><tbody><tr><td><strong>ISO/IEC 27001</strong><br>2022</td><td>Publicada; edición 3 (2022). Amd 1:2024 publicada.</td><td>Seguridad de la información</td><td><code>iso.org/standard/27001</code></td></tr><tr><td><strong>ISO 14001</strong><br>2026</td><td>Publicada; edición 4 (2026). La edición 2015 fue retirada.</td><td>Gestión ambiental</td><td><code>iso.org/standard/14001</code></td></tr><tr><td><strong>ISO/IEC 25010</strong><br>2023</td><td>Publicada; edición 2 (2023). La edición 2011 fue retirada.</td><td>Calidad del producto</td><td><code>iso.org/standard/78176</code></td></tr><tr><td><strong>ISO/IEC 9126-1</strong><br>2001</td><td>Retirada; referencia histórica. Sustituida por ISO/IEC 25010.</td><td>Evolución del modelo de calidad</td><td><code>iso.org/standard/22749</code></td></tr><tr><td><strong>ISO 37001</strong><br>2025</td><td>Publicada; edición 2 (2025).</td><td>Prevención del soborno</td><td><code>iso.org/standard/37001</code></td></tr><tr><td><strong>ISO 10005</strong><br>2018</td><td>Publicada; edición 3 (2018), corregida en 2019 (solo versión en español) y confirmada en 2023.</td><td>Planes de calidad</td><td><code>iso.org/standard/70398</code></td></tr><tr><td><strong>ISO 9001</strong><br>2026</td><td>Publicada; edición 6 (2026). La edición 2015 fue retirada.</td><td>Gestión de la calidad</td><td><code>iso.org/standard/9001</code></td></tr><tr><td><strong>ISO/IEC 33001:2015</strong><br><strong>33002:2015 · 33003:2015</strong><br><strong>33004:2015 · 33020:2019</strong></td><td>33001/33002/33003 ed. 1 (2015); 33004 ed. 1 (2015, corregida en 2017); 33020 ed. 2 (2019). Confirmadas en 2026.</td><td>Evaluación de procesos</td><td><code>iso.org/standard/54175</code><br><code>54176 · 54177 · 54178 · 78526</code></td></tr></tbody></table></div><div class="callout note"><div class="callout-head"><span class="callout-icon" aria-hidden="true">ℹ</span><span>Nota</span></div><div class="callout-body"><p>ISO 14001:2015 e ISO 9001:2015 no son las ediciones vigentes. ISO/IEC 9126-1:2001 no es vigente. La familia ISO/IEC 33000 se cita por sus partes, no como una norma única.</p></div></div>'
      }
    ]
  },

  /* ------------------------------------------------------------------ ANEXOS */
  {
    id: 'mapa-rutas', num: 'Anexo A', part: 'anexos', roles: ['todos'], title: 'Mapa de rutas y cobertura',
    purpose: 'Relaciona las rutas del frontend con su capítulo y clasifica las rutas directas, los alias y las pantallas compartidas o transitorias.',
    route: 'No aplica.',
    whatYouSee: ['Tabla de rutas públicas y privadas.', 'Capítulo donde se documenta cada una.', 'Tabla complementaria de alias, redirecciones y pantallas compartidas o transitorias.'],
    steps: ['Busca la ruta que necesitas.', 'Si no está en la tabla principal, revisa la tabla de alias y pantallas compartidas.', 'Abre el capítulo indicado.'],
    after: ['La tabla principal documenta las rutas funcionales directas y el capítulo que las explica.', 'La segunda tabla clasifica los alias, las redirecciones y las pantallas compartidas o transitorias. No se afirma cobertura nominal de todas las rutas declaradas en App.jsx.'],
    extra: [{ title: 'Cobertura de rutas', html: '<div class="table-wrap"><table><thead><tr><th>Ruta</th><th>Rol</th><th>Capítulo</th></tr></thead><tbody><tr><td><code>/</code></td><td>Público</td><td>La portada pública</td></tr><tr><td><code>/login</code></td><td>Público</td><td>Iniciar sesión</td></tr><tr><td><code>/recuperar-contrasena</code></td><td>Público</td><td>Recuperar contraseña</td></tr><tr><td><code>/restablecer-contrasena</code></td><td>Público</td><td>Restablecer contraseña</td></tr><tr><td><code>/activar-cuenta</code></td><td>Público</td><td>Activar cuenta</td></tr><tr><td><code>/aprendiz/dashboard</code></td><td>Aprendiz</td><td>Dashboard del aprendiz</td></tr><tr><td><code>/aprendiz/ficha</code></td><td>Aprendiz</td><td>Mi Ficha</td></tr><tr><td><code>/aprendiz/propuestas</code></td><td>Aprendiz</td><td>Mis Propuestas</td></tr><tr><td><code>/aprendiz/propuestas?crear=1</code></td><td>Aprendiz</td><td>Crear una propuesta</td></tr><tr><td><code>/aprendiz/detalle-proyecto/:id</code></td><td>Aprendiz</td><td>Detalle de propuesta</td></tr><tr><td><code>/aprendiz/resultado-analisis</code></td><td>Aprendiz</td><td>Análisis y resultado</td></tr><tr><td><code>/aprendiz/similitudes</code></td><td>Aprendiz</td><td>Similitudes</td></tr><tr><td><code>/aprendiz/detalle-similitud/:id</code></td><td>Aprendiz</td><td>Detalle de similitud</td></tr><tr><td><code>/aprendiz/alertas</code></td><td>Aprendiz</td><td>Notificaciones</td></tr><tr><td><code>/aprendiz/reportar-falla</code></td><td>Aprendiz</td><td>Soporte</td></tr><tr><td><code>/aprendiz/perfil</code></td><td>Aprendiz</td><td>Mi Perfil</td></tr><tr><td><code>/aprendiz/detalle-ficha/:id</code></td><td>Aprendiz</td><td>Mi Ficha / detalle</td></tr><tr><td><code>/aprendiz/perfil-companero/:id</code></td><td>Aprendiz/Instructor</td><td>Compañeros</td></tr><tr><td><code>/aprendiz/perfil-instructor</code></td><td>Aprendiz</td><td>Compañeros e instructor</td></tr><tr><td><code>/instructor/dashboard</code></td><td>Instructor</td><td>Dashboard del instructor</td></tr><tr><td><code>/instructor/revision-propuestas</code></td><td>Instructor</td><td>Revisión de propuestas</td></tr><tr><td><code>/instructor/detalle-proyecto/:id</code></td><td>Instructor</td><td>Detalle de propuesta</td></tr><tr><td><code>/instructor/fichas</code></td><td>Instructor</td><td>Gestión de fichas</td></tr><tr><td><code>/instructor/fichas?crear=1</code></td><td>Instructor</td><td>Crear ficha</td></tr><tr><td><code>/instructor/detalle-ficha/:id</code></td><td>Instructor</td><td>Detalle de ficha</td></tr><tr><td><code>/instructor/directorio-ficha/:id</code></td><td>Instructor</td><td>Directorio</td></tr><tr><td><code>/instructor/similitudes</code></td><td>Instructor</td><td>Similitudes</td></tr><tr><td><code>/instructor/detalle-similitud/:id</code></td><td>Instructor</td><td>Detalle de similitud</td></tr><tr><td><code>/instructor/alertas</code></td><td>Instructor</td><td>Notificaciones</td></tr><tr><td><code>/instructor/reportar-falla</code></td><td>Instructor</td><td>Soporte</td></tr><tr><td><code>/instructor/perfil</code></td><td>Instructor</td><td>Perfil</td></tr><tr><td><code>/admin/dashboard</code></td><td>Admin</td><td>Dashboard del administrador</td></tr><tr><td><code>/admin/usuarios</code></td><td>Admin</td><td>Gestión de usuarios</td></tr><tr><td><code>/admin/usuarios?crear=1</code></td><td>Admin</td><td>Crear usuario</td></tr><tr><td><code>/admin/detalle-usuario/:id</code></td><td>Admin</td><td>Detalle de usuario</td></tr><tr><td><code>/admin/fichas</code></td><td>Admin</td><td>Gestión de fichas</td></tr><tr><td><code>/admin/fichas?crear=1</code></td><td>Admin</td><td>Crear ficha</td></tr><tr><td><code>/admin/detalle-ficha/:id</code></td><td>Admin</td><td>Detalle de ficha</td></tr><tr><td><code>/admin/proyectos</code></td><td>Admin</td><td>Supervisión de propuestas</td></tr><tr><td><code>/admin/detalle-proyecto/:id</code></td><td>Admin</td><td>Detalle de propuesta</td></tr><tr><td><code>/admin/similitudes</code></td><td>Admin</td><td>Supervisión de similitudes</td></tr><tr><td><code>/admin/detalle-similitud/:id</code></td><td>Admin</td><td>Detalle de similitud</td></tr><tr><td><code>/admin/reportes-fallas</code></td><td>Admin</td><td>Bandeja de soporte</td></tr><tr><td><code>/admin/detalle-reporte/:id</code></td><td>Admin</td><td>Detalle de soporte</td></tr><tr><td><code>/admin/notificaciones</code></td><td>Admin</td><td>Notificaciones</td></tr><tr><td><code>/admin/perfil</code></td><td>Admin</td><td>Perfil</td></tr><tr><td><code>/admin/bitacora</code></td><td>Admin</td><td>Bitácora</td></tr><tr><td><code>/admin/redes-conocimiento</code></td><td>Admin</td><td>Redes y programas</td></tr><tr><td><code>/admin/config-similitud</code></td><td>Admin</td><td>Motor</td></tr></tbody></table></div>' }, { title: 'Rutas alias y pantallas compartidas', html: '<div class="table-wrap"><table><thead><tr><th>Ruta</th><th>Tipo</th><th>Rol</th><th>Relación</th></tr></thead><tbody><tr><td><code>/aprendiz/mis-proyectos</code></td><td>Alias</td><td>Aprendiz</td><td>Redirige a <code>/aprendiz/propuestas</code>.</td></tr><tr><td><code>/aprendiz/nuevo-proyecto</code></td><td>Alias</td><td>Aprendiz</td><td>Redirige a <code>/aprendiz/propuestas?crear=1</code>.</td></tr><tr><td><code>/aprendiz/unirse-ficha</code></td><td>Alias</td><td>Aprendiz</td><td>Redirige a <code>/aprendiz/ficha</code>.</td></tr><tr><td><code>/instructor/gestionar-fichas</code></td><td>Alias</td><td>Instructor</td><td>Redirige a <code>/instructor/fichas</code>.</td></tr><tr><td><code>/instructor/crear-ficha</code></td><td>Alias</td><td>Instructor</td><td>Redirige a <code>/instructor/fichas?crear=1</code>.</td></tr><tr><td><code>/admin/gestion-usuarios</code></td><td>Alias</td><td>Admin</td><td>Redirige a <code>/admin/usuarios</code>.</td></tr><tr><td><code>/admin/nuevo-usuario</code></td><td>Alias</td><td>Admin</td><td>Redirige a <code>/admin/usuarios?crear=1</code>.</td></tr><tr><td><code>/instructor/perfil-companero/:id</code></td><td>Pantalla compartida</td><td>Instructor</td><td>Usa la misma vista de perfil de solo lectura que <code>/aprendiz/perfil-companero/:id</code>; se abre desde el directorio de la ficha.</td></tr><tr><td><code>/aprendiz/analizando-proyecto</code></td><td>Pantalla transitoria</td><td>Aprendiz</td><td>Muestra el progreso del análisis; continúa en <code>/aprendiz/resultado-analisis</code> y se documenta en el Cap. 13.</td></tr></tbody></table></div>' }]
  },
  {
    id: 'matriz-permisos', num: 'Anexo B', part: 'anexos', roles: ['todos'], title: 'Matriz de permisos',
    purpose: 'Compara qué puede hacer cada rol en las operaciones principales.',
    route: 'No aplica.',
    whatYouSee: ['Tabla de operaciones y roles.'],
    steps: ['Busca la operación.', 'Revisa si tu rol tiene Sí o No.', 'Si no tiene permiso, no intentes forzar la ruta.'],
    after: ['Los permisos se aplican en frontend y backend.'],
    extra: [{ title: 'Matriz', html: '<div class="table-wrap"><table><thead><tr><th>Operación</th><th>Aprendiz</th><th>Instructor</th><th>Admin</th></tr></thead><tbody><tr><td>Ver propuestas</td><td>Propias/equipo</td><td>Sus fichas/asignadas</td><td>Todas</td></tr><tr><td>Crear propuesta</td><td>Sí</td><td>No</td><td>No</td></tr><tr><td>Aprobar/rechazar</td><td>No</td><td>Sí, si está a cargo</td><td>Sí</td></tr><tr><td>Editar contenido</td><td>Sí, si es editable</td><td>No</td><td>Sí</td></tr><tr><td>Eliminar propuesta</td><td>Creador, si aplica</td><td>No</td><td>Sí, con restricciones</td></tr><tr><td>Ver similitudes</td><td>Propias</td><td>Sus fichas</td><td>Todas</td></tr><tr><td>Eliminar similitud</td><td>No</td><td>No</td><td>Sí</td></tr><tr><td>Crear ficha</td><td>No</td><td>Sí, propia</td><td>Sí, elige instructor</td></tr><tr><td>Agregar aprendices</td><td>Se une con código</td><td>No desde frontend</td><td>Sí</td></tr><tr><td>Gestionar usuarios</td><td>No</td><td>No</td><td>Sí</td></tr><tr><td>Configurar motor</td><td>No</td><td>No</td><td>Sí</td></tr><tr><td>Ver bitácora</td><td>No</td><td>No</td><td>Sí</td></tr></tbody></table></div>' }]
  },
  {
    id: 'campos-formularios', num: 'Anexo C', part: 'anexos', roles: ['todos'], title: 'Campos de los formularios',
    purpose: 'Resume los campos y reglas más importantes de los formularios del frontend.',
    route: 'No aplica.',
    whatYouSee: ['Tabla por formulario con campos, obligatoriedad y regla.'],
    steps: ['Busca el formulario.', 'Revisa los campos antes de llenarlo.'],
    after: ['Las reglas exactas pueden variar si el backend agrega validaciones; el mensaje de la pantalla manda.'],
    extra: [{ title: 'Resumen de campos', html: '<div class="table-wrap"><table><thead><tr><th>Formulario</th><th>Campos principales</th><th>Reglas clave</th></tr></thead><tbody><tr><td>Login</td><td>Correo electrónico, contraseña, recordarme</td><td>Correo válido y contraseña obligatorios.</td></tr><tr><td>Propuesta</td><td>Nombre, descripción, objetivo general, objetivos específicos, equipo, área, palabras clave</td><td>5/20/15 caracteres; 2 objetivos; área obligatoria.</td></tr><tr><td>Ficha</td><td>Red, programa, instructor, nombre, número</td><td>Número 4–8 dígitos y único.</td></tr><tr><td>Usuario</td><td>Nombres, apellidos, documento, correo, rol</td><td>Documento y correo únicos; rol aprendiz/instructor.</td></tr><tr><td>Soporte</td><td>Ficha, motivo, título, descripción, tipo</td><td>Descripción obligatoria.</td></tr><tr><td>Perfil</td><td>Nombres, apellidos, foto, correo, contraseña</td><td>Nombres/apellidos obligatorios; contraseña 12+.</td></tr><tr><td>Red</td><td>Nombre, programas</td><td>Mínimo 3 caracteres y 1 programa.</td></tr><tr><td>Motor</td><td>Umbral, meses</td><td>5–95 y 1–60.</td></tr></tbody></table></div>' }]
  },
  {
    id: 'indice-funcionalidades', num: 'Anexo D', part: 'anexos', roles: ['todos'], title: 'Índice de funcionalidades',
    purpose: 'Agrupa las funcionalidades principales y el capítulo donde se explican.',
    route: 'No aplica.',
    whatYouSee: ['Lista de funcionalidades por área.'],
    steps: ['Busca la funcionalidad.', 'Abre el capítulo indicado.'],
    after: ['Sirve como control de cobertura del manual.'],
    extra: [{ title: 'Funcionalidades', html: '<div class="table-wrap"><table><thead><tr><th>Funcionalidad</th><th>Capítulo</th></tr></thead><tbody><tr><td>Iniciar sesión y recuperar acceso</td><td>Iniciar sesión / Recuperar / Restablecer / Activar</td></tr><tr><td>Unirse a una ficha</td><td>Mi Ficha</td></tr><tr><td>Crear, guardar y enviar propuesta</td><td>Crear una propuesta / Detalle</td></tr><tr><td>Consultar análisis y similitudes</td><td>Análisis / Similitudes / Detalle</td></tr><tr><td>Gestionar notificaciones</td><td>Notificaciones del aprendiz / instructor / admin</td></tr><tr><td>Reportar soporte</td><td>Soporte del aprendiz / instructor / Bandeja admin</td></tr><tr><td>Editar perfil y seguridad</td><td>Perfil del aprendiz / instructor / admin</td></tr><tr><td>Revisar propuestas</td><td>Revisión / Detalle instructor</td></tr><tr><td>Crear y gestionar fichas</td><td>Fichas instructor / admin</td></tr><tr><td>Gestionar usuarios</td><td>Usuarios / Crear / Detalle</td></tr><tr><td>Supervisar propuestas y similitudes</td><td>Propuestas / Similitudes admin</td></tr><tr><td>Configurar motor</td><td>Motor de similitudes</td></tr><tr><td>Auditar acciones</td><td>Bitácora</td></tr><tr><td>Gestionar redes y programas</td><td>Redes y programas</td></tr></tbody></table></div>' }]
  },
  {
    id: 'indice-figuras', num: 'Anexo E', part: 'anexos', roles: ['todos'], title: 'Índice de figuras',
    purpose: 'Lista las capturas utilizadas en el manual.',
    route: 'No aplica.',
    whatYouSee: ['Relación de figuras y capítulo.'],
    steps: ['Usa el buscador del navegador para localizar una captura.'],
    after: ['Todas las capturas provienen del entorno demo/E2E con datos sintéticos.'],
    extra: [{ title: 'Capturas', html: '<div class="table-wrap"><table><thead><tr><th>Archivo</th><th>Contenido</th></tr></thead><tbody><tr><td>01–02</td><td>Portada pública en escritorio y móvil.</td></tr><tr><td>03–07</td><td>Login, recuperación, restablecimiento, activación inválida y 404.</td></tr><tr><td>08–24</td><td>Dashboard, ficha, propuestas, detalle, análisis, similitudes, alertas, soporte, perfil, compañeros, 403 y tema oscuro.</td></tr><tr><td>25–36</td><td>Dashboard, revisión, detalle, fichas, creación, directorio, similitudes, alertas, soporte y perfil del instructor.</td></tr><tr><td>37–55</td><td>Dashboard, usuarios, fichas, propuestas, similitudes, soporte, notificaciones, perfil, bitácora, redes, motor y tablet del administrador.</td></tr></tbody></table></div>' }]
  }
];
