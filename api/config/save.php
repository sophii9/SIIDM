<?php
// ============================================================
//  config/save.php
//  POST /siidm/api/config/save.php
//
//  Header requerido: Authorization: Bearer <token>
//
//  Body JSON esperado:
//  { "lab_id": 1, "sensibilidad": 2, "tiempo_espera": 10,
//    "hora_inicio": "07:00", "hora_fin": "21:00",
//    "dias_activos": [1,2,3,4,5] }
//
//  Respuesta exitosa:
//  { "success": true, "message": "Configuración guardada",
//    "config": { ... } }
// ============================================================

require_once '../config/headers.php';
require_once '../config/db.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    jsonError('Método no permitido', 405);
}

// Validar autenticación
$token = requireAuth();
$pdo   = getConnection();

$stmt = $pdo->prepare("SELECT id FROM usuarios WHERE token = ?");
$stmt->execute([$token]);
if (!$stmt->fetch()) {
    jsonError('Token inválido o sesión expirada', 401);
}

// Leer body
$body = getJsonBody();
requireFields($body, ['lab_id', 'sensibilidad', 'tiempo_espera', 'hora_inicio', 'hora_fin']);

$labId       = (int) $body['lab_id'];
$sensibilidad = (int) $body['sensibilidad'];
$tiempoEspera = (int) $body['tiempo_espera'];
$horaInicio  = trim($body['hora_inicio']);
$horaFin     = trim($body['hora_fin']);
$diasActivos = $body['dias_activos'] ?? [1, 2, 3, 4, 5];

// ── Validaciones ─────────────────────────────────────────────

if ($labId <= 0) {
    jsonError('lab_id inválido');
}
if ($sensibilidad < 1 || $sensibilidad > 3) {
    jsonError('Sensibilidad debe ser 1, 2 o 3');
}
if ($tiempoEspera < 1 || $tiempoEspera > 60) {
    jsonError('Tiempo de espera debe ser entre 1 y 60 minutos');
}
// Validar formato HH:MM
if (!preg_match('/^\d{2}:\d{2}$/', $horaInicio) ||
    !preg_match('/^\d{2}:\d{2}$/', $horaFin)) {
    jsonError('Formato de hora inválido. Use HH:MM');
}
if (!is_array($diasActivos)) {
    jsonError('dias_activos debe ser un arreglo');
}

// Convertir array de días a string "1,2,3,4,5"
$diasStr = implode(',', array_map('intval', $diasActivos));

// ── Verificar que el laboratorio existe ──────────────────────

$stmt = $pdo->prepare("SELECT id FROM laboratorios WHERE id = ?");
$stmt->execute([$labId]);
if (!$stmt->fetch()) {
    jsonError('Laboratorio no encontrado', 404);
}

// ── INSERT o UPDATE (UPSERT) ─────────────────────────────────

$sql = "INSERT INTO config_sensor
            (lab_id, sensibilidad, tiempo_espera, hora_inicio, hora_fin, dias_activos)
        VALUES (?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE
            sensibilidad  = VALUES(sensibilidad),
            tiempo_espera = VALUES(tiempo_espera),
            hora_inicio   = VALUES(hora_inicio),
            hora_fin      = VALUES(hora_fin),
            dias_activos  = VALUES(dias_activos)";

$stmt = $pdo->prepare($sql);
$stmt->execute([
    $labId, $sensibilidad, $tiempoEspera,
    $horaInicio . ':00', $horaFin . ':00', $diasStr
]);

jsonSuccess([
    'message' => 'Configuración guardada correctamente',
    'config'  => [
        'lab_id'       => $labId,
        'sensibilidad' => $sensibilidad,
        'tiempo_espera'=> $tiempoEspera,
        'hora_inicio'  => $horaInicio,
        'hora_fin'     => $horaFin,
        'dias_activos' => $diasActivos,
    ]
]);