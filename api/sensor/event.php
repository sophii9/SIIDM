<?php
// ============================================================
//  sensor/event.php
//  POST /siidm/api/sensor/event.php?lab_id=1&tipo=motion_detected
//
//  Tipos válidos: motion_detected | lights_on | lights_off
//
//  Este endpoint es llamado por la app cuando el Arduino
//  notifica un evento via Bluetooth.
//  También puede llamarse directamente desde el Arduino si
//  en el futuro se agrega un módulo WiFi (ESP8266/ESP32).
// ============================================================

require_once '../config/headers.php';
require_once '../config/db.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    jsonError('Método no permitido', 405);
}

$token = requireAuth();
$pdo   = getConnection();

$stmt = $pdo->prepare("SELECT id FROM usuarios WHERE token = ?");
$stmt->execute([$token]);
if (!$stmt->fetch()) {
    jsonError('Token inválido o sesión expirada', 401);
}

// Parámetros GET
$labId = filter_input(INPUT_GET, 'lab_id', FILTER_VALIDATE_INT);
$tipo  = filter_input(INPUT_GET, 'tipo',   FILTER_SANITIZE_SPECIAL_CHARS);

if (!$labId || $labId <= 0) {
    jsonError('Parámetro lab_id inválido');
}

$tiposValidos = ['motion_detected', 'lights_on', 'lights_off'];
if (!in_array($tipo, $tiposValidos, true)) {
    jsonError('Tipo de evento inválido. Use: ' . implode(', ', $tiposValidos));
}

// Timestamp opcional del body; si no viene, usar NOW()
$body     = getJsonBody();
$timestamp = isset($body['timestamp'])
    ? date('Y-m-d H:i:s', strtotime($body['timestamp']))
    : date('Y-m-d H:i:s');

// Registrar evento
$stmt = $pdo->prepare(
    "INSERT INTO eventos_sensor (lab_id, tipo, timestamp) VALUES (?, ?, ?)"
);
$stmt->execute([$labId, $tipo, $timestamp]);

jsonSuccess([
    'message'   => 'Evento registrado',
    'event_id'  => (int) $pdo->lastInsertId(),
    'lab_id'    => $labId,
    'tipo'      => $tipo,
    'timestamp' => $timestamp,
]);