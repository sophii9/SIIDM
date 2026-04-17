<?php
// ============================================================
//  config/headers.php
//  Headers comunes para todas las respuestas de la API
// ============================================================

// Permitir peticiones desde la app Android en la red local
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Authorization");

// Responder preflight OPTIONS sin procesar lógica
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

// ── Funciones helper ────────────────────────────────────────

function jsonSuccess(array $data = [], int $code = 200): void {
    http_response_code($code);
    echo json_encode(array_merge(['success' => true], $data));
    exit;
}

function jsonError(string $message, int $code = 400): void {
    http_response_code($code);
    echo json_encode(['success' => false, 'message' => $message]);
    exit;
}

/**
 * Lee el cuerpo JSON de una petición POST.
 * Devuelve un array asociativo o termina con error si el JSON es inválido.
 */
function getJsonBody(): array {
    $raw = file_get_contents('php://input');
    $data = json_decode($raw, true);
    if (json_last_error() !== JSON_ERROR_NONE) {
        jsonError('Cuerpo de la petición inválido (no es JSON)');
    }
    return $data ?? [];
}

/**
 * Valida que los campos requeridos estén presentes y no vacíos.
 * $data  = array de datos a revisar
 * $fields = lista de nombres de campo obligatorios
 */
function requireFields(array $data, array $fields): void {
    foreach ($fields as $field) {
        if (!isset($data[$field]) || trim((string)$data[$field]) === '') {
            jsonError("El campo '$field' es obligatorio");
        }
    }
}

/**
 * Extrae y valida el token Bearer del encabezado Authorization.
 * Devuelve el token o termina con error 401.
 */
function requireAuth(): string {
    $header = $_SERVER['HTTP_AUTHORIZATION'] ?? '';
    if (!str_starts_with($header, 'Bearer ')) {
        jsonError('No autorizado. Token requerido.', 401);
    }
    $token = trim(substr($header, 7));
    if (empty($token)) {
        jsonError('Token inválido.', 401);
    }
    return $token;
}