export const SIMILITUD_ESTADO_LABEL = {
  pendiente: 'Resultado pendiente de actualización',
  en_proceso: 'Recalculando similitudes',
  actualizado: 'Resultado actualizado',
  sin_referencias: 'Sin propuestas aprobadas de referencia',
  error: 'No se pudo actualizar el resultado',
}

export const SIMILITUD_ESTADO_MENSAJE = {
  pendiente: 'Otra propuesta fue aprobada. El resultado se está actualizando; espera antes de aprobar.',
  en_proceso: 'El motor está recalculando las coincidencias de esta propuesta. Espera antes de aprobar.',
  sin_referencias: 'Todavía no existen propuestas aprobadas de la misma red y programa. No hay un porcentaje calculado que mostrar.',
  error: 'No se pudo recalcular la similitud. Reintenta o contacta al administrador antes de aprobar.',
}

export function esSimilitudPendiente(estado) {
  return estado === 'pendiente' || estado === 'en_proceso'
}

export function puedeAprobarConSimilitud(proyecto) {
  const estado = proyecto?.similitud_estado
  return !estado || estado === 'actualizado' || estado === 'sin_referencias'
}
