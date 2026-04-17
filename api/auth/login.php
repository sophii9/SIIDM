<?php
// ============================================================
//  auth/login.php
//  POST /siidm/api/auth/login.php
//
//  Body JSON esperado:
//  { "username": "admin", "password": "admin123" }
//
//  Respuesta exitosa:
//  { "success": true, "message": "Login exitoso",
//    "user": { "id": 1, "username": "admin",
//              "email": "...", "rol": "admin", "token": "..." } }
// ============================================================

require_once '../config/headers.php';
require_once '../config/db.php';

// Solo aceptar POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    jsonError('Método no permitido', 405);
}

// Leer y validar body
$body = getJsonBody();
requireFields($body, ['username', 'password']);

$username = trim($body['username']);
$password = $body['password'];

// ── Validaciones del lado del servidor ──────────────────────

// Longitud razonable para evitar ataques de longitud
if (strlen($username) > 50) {
    jsonError('Usuario inválido');
}
if (strlen($password) < 6 || strlen($password) > 72) {
    jsonError('Contraseña inválida');
}

// Solo caracteres alfanuméricos y guión bajo en el username
if (!preg_match('/^[a-zA-Z0-9_]+$/', $username)) {
    jsonError('El usuario solo puede contener letras, números y guión bajo');
}

// ── Buscar usuario en la BD ──────────────────────────────────

$pdo  = getConnection();
$stmt = $pdo->prepare("SELECT id, username, email, rol, password FROM usuarios WHERE username = ?");
$stmt->execute([$username]);
$user = $stmt->fetch();

// Verificar existencia y contraseña con password_verify (seguro contra timing attacks)
if (!$user || !password_verify($password, $user['password'])) {
    jsonError('Usuario o contraseña incorrectos', 401);
}

// ── Generar token de sesión ──────────────────────────────────

$token = bin2hex(random_bytes(32));   // 64 caracteres hexadecimales

$stmt = $pdo->prepare("UPDATE usuarios SET token = ? WHERE id = ?");
$stmt->execute([$token, $user['id']]);

// ── Responder con datos del usuario ─────────────────────────

jsonSuccess([
    'message' => 'Login exitoso',
    'user'    => [
        'id'       => (int) $user['id'],
        'username' => $user['username'],
        'email'    => $user['email'],
        'rol'      => $user['rol'],
        'token'    => $token,
    ]
]);