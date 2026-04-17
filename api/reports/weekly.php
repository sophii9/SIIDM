<?php
// ============================================================
//  reports/weekly.php
//  GET /siidm/api/reports/weekly.php?lab_id=1&fecha_inicio=2025-01-06
//
//  Header requerido: Authorization: Bearer <token>
//
//  Devuelve el consumo de los 7 días a partir de fecha_inicio.
// ============================================================

require_once '../config/headers.php';
require_once '../config/db.php';

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    jsonError('Método no permitido', 405);
}

$token = requireAuth();
$pdo   = getConnection();

$stmt = $pdo->prepare("SELECT id FROM usuarios WHERE token = ?");
$stmt->execute([$token]);
if (!$stmt->fetch()) {
    jsonError('Token inválido o sesión expirada', 401);
}

// Parámetros
$labId       = filter_input(INPUT_GET, 'lab_id',       FILTER_VALIDATE_INT);
$fechaInicio = filter_input(INPUT_GET, 'fecha_inicio', FILTER_SANITIZE_SPECIAL_CHARS);

if (!$labId || $labId <= 0) {
    jsonError('Parámetro lab_id inválido');
}
if (!$fechaInicio || !preg_match('/^\d{4}-\d{2}-\d{2}$/', $fechaInicio)) {
    jsonError('Parámetro fecha_inicio inválido. Use YYYY-MM-DD');
}

// Calcular fecha fin (7 días)
$fechaFin = date('Y-m-d', strtotime($fechaInicio . ' +6 days'));

// Obtener nombre del laboratorio
$stmt = $pdo->prepare("SELECT nombre FROM laboratorios WHERE id = ?");
$stmt->execute([$labId]);
$lab = $stmt->fetch();
if (!$lab) {
    jsonError('Laboratorio no encontrado', 404);
}

// Obtener consumo diario de la semana
$stmt = $pdo->prepare(
    "SELECT id, lab_id, fecha, kwh, horas_uso, costo
     FROM consumo_electrico
     WHERE lab_id = ? AND fecha BETWEEN ? AND ?
     ORDER BY fecha ASC"
);
$stmt->execute([$labId, $fechaInicio, $fechaFin]);
$registros = $stmt->fetchAll();

// Calcular totales
$totalKwh   = 0;
$totalHoras = 0;
$costoTotal = 0;

$datosDiarios = [];
foreach ($registros as $r) {
    $totalKwh   += (float) $r['kwh'];
    $totalHoras += (float) $r['horas_uso'];
    $costoTotal += (float) $r['costo'];

    $datosDiarios[] = [
        'id'        => (int)   $r['id'],
        'lab_id'    => (int)   $r['lab_id'],
        'fecha'     =>         $r['fecha'],
        'kwh'       => (float) $r['kwh'],
        'horas_uso' => (float) $r['horas_uso'],
        'costo'     => (float) $r['costo'],
    ];
}

jsonSuccess([
    'reporte' => [
        'lab_nombre'    => $lab['nombre'],
        'semana_inicio' => $fechaInicio,
        'semana_fin'    => $fechaFin,
        'total_kwh'     => round($totalKwh,   3),
        'total_horas'   => round($totalHoras, 2),
        'costo_total'   => round($costoTotal, 2),
        'datos_diarios' => $datosDiarios,
    ]
]);