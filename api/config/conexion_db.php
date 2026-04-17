<?php
// ============================================================
//  config/db.php
//  Configuración de conexión a MySQL
//  Coloca este archivo en: htdocs/siidm/api/config/db.php
// ============================================================

define('DB_HOST', 'localhost');
define('DB_NAME', 'siidm');
define('DB_USER', 'root');        // Cambia si tu usuario MySQL es diferente
define('DB_PASS', '');            // Cambia si tu MySQL tiene contraseña

function getConnection(): PDO {
    static $pdo = null;

    if ($pdo === null) {
        try {
            $dsn = "mysql:host=" . DB_HOST . ";dbname=" . DB_NAME . ";charset=utf8mb4";
            $pdo = new PDO($dsn, DB_USER, DB_PASS, [
                PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                PDO::ATTR_EMULATE_PREPARES   => false,
            ]);
        } catch (PDOException $e) {
            http_response_code(500);
            echo json_encode([
                'success' => false,
                'message' => 'Error de conexión a la base de datos'
            ]);
            exit;
        }
    }

    return $pdo;
}