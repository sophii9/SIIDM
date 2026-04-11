<?php
// ============================================================
//  labs/list.php
//  GET /siidm/api/labs/list.php
//
//  Header requerido: Authorization: Bearer <token>
//
//  Devuelve la lista de laboratorios registrados.
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

$stmt = $pdo->query("SELECT id, nombre, descripcion, activo FROM laboratorios ORDER BY id");
$labs = $stmt->fetchAll();

$result = array_map(fn($l) => [
    'id'          => (int)  $l['id'],
    'nombre'      =>        $l['nombre'],
    'descripcion' =>        $l['descripcion'],
    'activo'      => (bool) $l['activo'],
], $labs);

// Devolver directamente el array (Retrofit espera List<Laboratorio>)
http_response_code(200);
echo json_encode($result);
exit;