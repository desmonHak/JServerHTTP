<?php
// Read POST data from stdin
$postData = file_get_contents("php://stdin");

// Print raw data for debugging
echo "Datos crudos recibidos: " . $postData . "\n\n";

// Parse the POST data
parse_str($postData, $params);

// Generate HTML response
echo "<html><body><h1>Datos recibidos en PHP:</h1><ul>";
foreach ($params as $key => $value) {
    echo "<li>" . htmlspecialchars($key) . ": " . htmlspecialchars($value) . "</li>";
}
echo "</ul></body></html>";
?>