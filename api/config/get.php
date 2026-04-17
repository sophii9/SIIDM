<?php
// ============================================================
//  config/get.php
//  GET /siidm/api/config/get.php?lab_id=1
//
//  Header requerido: Authorization: Bearer <token>
//
//  Respuesta exitosa:
//  { "success": true, "config": {
//      "id": 1, "lab_id": 1, "sensibilidad": 2,
//      "tiempo_espera": 10, "hora_inicio": "07:00",
//      "hora_fin": "21:00", "dias_activos": [1,2,3,4,5] } }
// ============================================================

require_once '../config/headers.php';
require_once '../config/db.php';

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    jsonError('Método no permitido', 405);
}

// Validar autenticación
$token = requireAuth();
$pdo   = getConnection();

// Verificar que el token exista en la BD
$stmt = $pdo->prepare("SELECT id FROM usuarios WHERE token = ?");
$stmt->execute([$token]);
if (!$stmt->fetch()) {
    jsonError('Token inválido o sesión expirada', 401);
}

// Validar parámetro lab_id
$labId = filter_input(INPUT_GET, 'lab_id', FILTER_VALIDATE_INT);
if (!$labId || $labId <= 0) {
    jsonError('Parámetro lab_id inválido');
}

// Obtener configuración
$stmt = $pdo->prepare("SELECT * FROM config_sensor WHERE lab_id = ?");
$stmt->execute([$labId]);
$config = $stmt->fetch();

if (!$config) {
    jsonError('No se encontró configuración para este laboratorio', 404);
}

// Convertir dias_activos de string "1,2,3,4,5" a array [1,2,3,4,5]
$diasArray = array_map('intval', explode(',', $config['dias_activos']));

// Formatear horas sin segundos (HH:MM)
$horaInicio = substr($config['hora_inicio'], 0, 5);
$horaFin    = substr($config['hora_fin'],    0, 5);

jsonSuccess([
    'config' => [
        'id'           => (int) $config['id'],
        'lab_id'       => (int) $config['lab_id'],
        'sensibilidad' => (int) $config['sensibilidad'],
        'tiempo_espera'=> (int) $config['tiempo_espera'],
        'hora_inicio'  => $horaInicio,
        'hora_fin'     => $horaFin,
        'dias_activos' => $diasArray,
    ]
]);