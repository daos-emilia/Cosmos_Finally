package com.consultorio.desktop.utils;

import com.consultorio.desktop.models.Turno;
import com.consultorio.desktop.models.Paciente;
import com.consultorio.desktop.models.InformePaciente;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpClientUtil {

    //private static final String BASE_URL = "http://localhost:8080/api";
    private static final String BASE_URL = "https://consultorio-cosmos-backend.onrender.com/api";

    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error encoding URL", e);
        }
    }

    public static List<Turno> getProximosTurnos() {
        try {
            String url = BASE_URL + "/turnos/proximos";
            System.out.println("🔄 Cargando próximos turnos del backend...");
            System.out.println("🔗 URL: " + url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("🔍 RESPONSE CODE: " + response.statusCode());
            System.out.println("🔍 RESPONSE BODY: " + response.body());

            if (response.statusCode() == 200) {
                List<Turno> turnos = parseTurnosFromJson(response.body());
                System.out.println("📊 Turnos recibidos: " + turnos.size());
                if (!turnos.isEmpty()) {
                    System.out.println("🎯 Primer turno: " + turnos.get(0));
                }
                return turnos;
            } else {
                System.out.println("❌ Error en respuesta: " + response.statusCode());
                return new ArrayList<>();
            }

        } catch (Exception e) {
            System.out.println("❌ Error obteniendo próximos turnos: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static List<Turno> parseTurnosFromJson(String json) {
        List<Turno> turnos = new ArrayList<>();

        try {
            System.out.println("📦 JSON RECIBIDO: " + json);

            json = json.trim();
            if (!json.startsWith("[")) return turnos;

            json = json.substring(1, json.length() - 1);

            List<String> objetos = splitJsonArray(json);

            for (int i = 0; i < objetos.size(); i++) {
                String obj = objetos.get(i);
                System.out.println("🔍 Parseando item " + i + ": " + obj);

                Turno turno = new Turno();
                turno.setId(parseLong(extractValue(obj, "id")));
                turno.setPacienteId(parseLong(extractValue(obj, "pacienteId")));
                turno.setFecha(LocalDate.parse(extractValue(obj, "fecha")));
                turno.setHoraInicio(LocalTime.parse(extractValue(obj, "horaInicio")));
                turno.setHoraFin(LocalTime.parse(extractValue(obj, "horaFin")));
                turno.setEstado(extractValue(obj, "estado"));
                turno.setTipo(extractValue(obj, "tipo"));
                turno.setMonto(parseDouble(extractValue(obj, "monto")));
                turno.setPagoConfirmado(parseBoolean(extractValue(obj, "pagoConfirmado")));
                turno.setNombrePaciente(extractValue(obj, "nombrePaciente"));
                turno.setApellidoPaciente(extractValue(obj, "apellidoPaciente"));
                turno.setDniPaciente(extractValue(obj, "dniPaciente"));

                System.out.println("✅ Turno parseado: " + turno);
                turnos.add(turno);
            }

        } catch (Exception e) {
            System.out.println("❌ Error parseando JSON: " + e.getMessage());
            e.printStackTrace();
        }

        return turnos;
    }

    private static List<String> splitJsonArray(String content) {
        List<String> objetos = new ArrayList<>();
        int nivel = 0;
        int inicio = 0;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') nivel++;
            else if (c == '}') {
                nivel--;
                if (nivel == 0) {
                    objetos.add(content.substring(inicio, i + 1));
                    inicio = i + 2;
                }
            }
        }

        return objetos;
    }

    public static List<String> getHorariosDisponibles(LocalDate fecha) {
        try {
            String url = BASE_URL + "/turnos/disponibilidad/" + fecha.toString();
            System.out.println("🔄 Cargando horarios disponibles para: " + fecha);
            System.out.println("🔗 URL horarios disponibles: " + url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                List<String> horarios = parseStringArrayFromJson(response.body());
                System.out.println("✅ " + horarios.size() + " horarios cargados para: " + fecha);
                return horarios;
            } else {
                System.out.println("❌ Error en respuesta: " + response.statusCode());
                System.out.println("📭 No hay horarios disponibles para: " + fecha);
                return new ArrayList<>();
            }

        } catch (Exception e) {
            System.out.println("❌ Error obteniendo horarios disponibles: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static List<String> parseStringArrayFromJson(String json) {
        List<String> strings = new ArrayList<>();
        try {
            json = json.trim();
            if (!json.startsWith("[")) return strings;

            json = json.substring(1, json.length() - 1);

            if (json.isEmpty()) return strings;

            String[] partes = json.split(",");
            for (String parte : partes) {
                parte = parte.trim();
                if (parte.startsWith("\"")) {
                    parte = parte.substring(1, parte.length() - 1);
                }
                strings.add(parte);
            }
        } catch (Exception e) {
            System.out.println("❌ Error parseando array de strings: " + e.getMessage());
        }
        return strings;
    }

    public static List<Map<String, Object>> getConfiguracionHoraria() {
        try {
            String url = BASE_URL + "/configuracion-horaria";
            System.out.println("🔄 Cargando configuración actual...");
            System.out.println("🔗 URL configuración horaria: " + url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("✅ Configuración cargada correctamente");
                return parseConfiguracionFromJson(response.body());
            } else {
                System.out.println("❌ Error en respuesta: " + response.statusCode());
                return new ArrayList<>();
            }

        } catch (Exception e) {
            System.out.println("❌ Error obteniendo configuración horaria: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static List<Map<String, Object>> parseConfiguracionFromJson(String json) {
        List<Map<String, Object>> configuraciones = new ArrayList<>();
        try {
            json = json.trim();
            if (!json.startsWith("[")) return configuraciones;

            json = json.substring(1, json.length() - 1);
            List<String> objetos = splitJsonArray(json);

            for (String obj : objetos) {
                Map<String, Object> config = new HashMap<>();

                // IMPORTANTE: Guardar el ID
                String idStr = extractValue(obj, "id");
                if (idStr != null && !idStr.equals("null")) {
                    config.put("id", parseLong(idStr));
                    System.out.println("   📌 ID encontrado: " + idStr + " para día: " + extractValue(obj, "diaSemana"));
                }

                config.put("diaSemana", extractValue(obj, "diaSemana"));
                config.put("activo", parseBoolean(extractValue(obj, "activo")));
                config.put("turnoManana", parseBoolean(extractValue(obj, "turnoManana")));
                config.put("turnoTarde", parseBoolean(extractValue(obj, "turnoTarde")));
                config.put("horaInicioManana", extractValue(obj, "inicioManana"));
                config.put("horaFinManana", extractValue(obj, "finManana"));
                config.put("horaInicioTarde", extractValue(obj, "inicioTarde"));
                config.put("horaFinTarde", extractValue(obj, "finTarde"));
                config.put("duracionSesion", parseInt(extractValue(obj, "duracionSesion")));
                config.put("tiempoDescanso", parseInt(extractValue(obj, "tiempoDescanso")));

                // Agregar campos de auditoría
                config.put("fechaActualizacion", extractValue(obj, "fechaActualizacion"));
                config.put("actualizadoPor", extractValue(obj, "actualizadoPor"));

                configuraciones.add(config);
            }
        } catch (Exception e) {
            System.out.println("❌ Error parseando configuración: " + e.getMessage());
            e.printStackTrace();
        }
        return configuraciones;
    }

    public static boolean actualizarConfiguracionHoraria(List<Map<String, Object>> configuraciones, String usuario) {
        try {
            System.out.println("\n💾 ===== INICIANDO GUARDADO DE CONFIGURACIÓN =====");

            // Obtener configuraciones actuales con sus IDs
            List<Map<String, Object>> configuracionesActuales = getConfiguracionHoraria();

            System.out.println("📋 Configuraciones actuales con IDs:");
            for (Map<String, Object> configActual : configuracionesActuales) {
                System.out.println("   ID: " + configActual.get("id") + " - Día: " + configActual.get("diaSemana"));
            }

            // Agregar IDs a las configuraciones nuevas
            for (Map<String, Object> configNueva : configuraciones) {
                String diaSemana = (String) configNueva.get("diaSemana");

                // Buscar el ID correspondiente
                for (Map<String, Object> configActual : configuracionesActuales) {
                    if (diaSemana.equals(configActual.get("diaSemana"))) {
                        configNueva.put("id", configActual.get("id"));
                        System.out.println("   🔗 Asignando ID " + configActual.get("id") + " a " + diaSemana);
                        break;
                    }
                }
            }

            String usuarioEncoded = encodeValue(usuario);
            String url = BASE_URL + "/configuracion-horaria/actualizar?usuario=" + usuarioEncoded;
            System.out.println("🔗 URL actualizar configuración: " + url);

            String jsonBody = createJsonFromConfiguraciones(configuraciones);
            System.out.println("📦 JSON enviado (primeros 300 chars): " + jsonBody.substring(0, Math.min(300, jsonBody.length())) + "...");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("✅ Configuración actualizada exitosamente");
                System.out.println("===== FIN GUARDADO DE CONFIGURACIÓN =====\n");
                return true;
            } else {
                System.out.println("❌ Error en respuesta: " + response.statusCode() + " - " + response.body());
                System.out.println("===== FIN GUARDADO DE CONFIGURACIÓN =====\n");
                return false;
            }

        } catch (Exception e) {
            System.out.println("❌ Error actualizando configuración horaria: " + e.getMessage());
            e.printStackTrace();
            System.out.println("===== FIN GUARDADO DE CONFIGURACIÓN =====\n");
            return false;
        }
    }

    private static String createJsonFromConfiguraciones(List<Map<String, Object>> configuraciones) {
        StringBuilder json = new StringBuilder("[");

        for (int i = 0; i < configuraciones.size(); i++) {
            if (i > 0) json.append(",");
            json.append(createJsonFromMap(configuraciones.get(i)));
        }

        json.append("]");
        return json.toString();
    }

    public static Map<String, Object> login(String dni, String password) throws Exception {
        String jsonBody = "{\"dni\":\"" + dni + "\",\"password\":\"" + password + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/usuarios/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return parseJsonResponse(response.body());
        } else {
            throw new RuntimeException("Error: " + response.statusCode() + " - " + response.body());
        }
    }

    public static Map<String, Object> getUsuarioByDni(String dni) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/usuarios/dni/" + dni))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return parseJsonResponse(response.body());
        } else if (response.statusCode() == 404) {
            return new HashMap<>();
        } else {
            throw new RuntimeException("Error: " + response.statusCode() + " - " + response.body());
        }
    }

    /**
     * Enviar recordatorio de contraseña por email
     * @param dni DNI del usuario
     * @return true si se envió correctamente, false en caso contrario
     */
    public static boolean enviarRecordatorioPassword(String dni) {
        try {
            System.out.println("📧 Enviando recordatorio de contraseña para DNI: " + dni);

            String jsonBody = "{\"dni\":\"" + dni + "\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/usuarios/recordatorio-password"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📨 Response status: " + response.statusCode());
            System.out.println("📨 Response body: " + response.body());

            if (response.statusCode() == 200) {
                System.out.println("✅ Recordatorio de contraseña enviado exitosamente");
                return true;
            } else {
                System.err.println("❌ Error enviando recordatorio: " + response.body());
                return false;
            }
        } catch (Exception e) {
            System.err.println("💥 Error en enviarRecordatorioPassword: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    public static Map<String, Object> sendPost(String endpoint, Object requestBody) throws Exception {
        String jsonBody = createJsonFromMap((Map<String, Object>) requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return parseJsonResponse(response.body());
        } else {
            throw new RuntimeException("Error: " + response.statusCode() + " - " + response.body());
        }
    }

    private static String createJsonFromMap(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":");

            Object value = entry.getValue();
            if (value == null) {
                json.append("null");
            } else if (value instanceof String) {
                json.append("\"").append(value).append("\"");
            } else if (value instanceof Boolean || value instanceof Number) {
                json.append(value);
            } else {
                json.append("\"").append(value).append("\"");
            }

            first = false;
        }
        json.append("}");
        return json.toString();
    }

    private static Map<String, Object> parseJsonResponse(String json) {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("id", extractValue(json, "id"));
            result.put("dni", extractValue(json, "dni"));
            result.put("nombre", extractValue(json, "nombre"));
            result.put("apellido", extractValue(json, "apellido"));
            result.put("tipo", extractValue(json, "tipo"));
            result.put("telefono", extractValue(json, "telefono"));
            result.put("email", extractValue(json, "email"));

            String emailValidadoStr = extractValue(json, "emailValidado");
            result.put("emailValidado", parseBoolean(emailValidadoStr));

        } catch (Exception e) {
            System.out.println("❌ Error en parseJsonResponse: " + e.getMessage());
        }
        return result;
    }

    private static String extractValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) return null;

        startIndex += searchKey.length();
        while (startIndex < json.length() && Character.isWhitespace(json.charAt(startIndex))) {
            startIndex++;
        }

        if (startIndex >= json.length()) return null;

        if (json.charAt(startIndex) == '"') {
            startIndex++;
            int endIndex = json.indexOf("\"", startIndex);
            if (endIndex == -1) return null;
            return json.substring(startIndex, endIndex);
        } else if (json.charAt(startIndex) == 'n' && json.startsWith("null", startIndex)) {
            return null;
        } else {
            int endIndex = startIndex;
            while (endIndex < json.length() &&
                    json.charAt(endIndex) != ',' &&
                    json.charAt(endIndex) != '}' &&
                    json.charAt(endIndex) != ']') {
                endIndex++;
            }
            return json.substring(startIndex, endIndex).trim();
        }
    }

    private static long parseLong(String value) {
        if (value == null || value.isEmpty() || value.equals("null")) return 0L;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private static int parseInt(String value) {
        if (value == null || value.isEmpty() || value.equals("null")) return 0;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static double parseDouble(String value) {
        if (value == null || value.isEmpty() || value.equals("null")) return 0.0;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private static boolean parseBoolean(String value) {
        if (value == null || value.isEmpty() || value.equals("null")) return false;
        return "true".equalsIgnoreCase(value);
    }

    // ==================== MÉTODOS PARA PACIENTES ====================

    /**
     * Obtener todos los pacientes (ordenados por fecha de creación, más nuevos primero)
     */
    public static List<Paciente> getAllPacientes() {
        try {
            String url = BASE_URL + "/pacientes";
            System.out.println("🔄 Obteniendo todos los pacientes...");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                List<Paciente> pacientes = parsePacientesFromJson(response.body());
                System.out.println("✅ Pacientes recibidos: " + pacientes.size());
                return pacientes;
            } else {
                System.out.println("❌ Error obteniendo pacientes: " + response.statusCode());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Obtener pacientes activos
     */
    public static List<Paciente> getPacientesActivos() {
        try {
            String url = BASE_URL + "/pacientes/activos";
            System.out.println("🔄 Obteniendo pacientes activos...");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parsePacientesFromJson(response.body());
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Buscar pacientes por nombre o apellido
     */
    public static List<Paciente> buscarPacientes(String termino) {
        try {
            String url = BASE_URL + "/pacientes/buscar?termino=" + encodeValue(termino);
            System.out.println("🔍 Buscando pacientes: " + termino);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parsePacientesFromJson(response.body());
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Obtener paciente por ID
     */
    public static Paciente getPacienteById(Long id) {
        try {
            String url = BASE_URL + "/pacientes/" + id;
            System.out.println("🔄 Obteniendo paciente ID: " + id);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parsePacienteFromJson(response.body());
            } else {
                return null;
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Crear nuevo paciente
     */
    public static Paciente crearPaciente(Paciente paciente) {
        return crearPaciente(paciente, null);
    }

    public static Paciente crearPaciente(Paciente paciente, String usuarioCreador) {
        try {
            String url = BASE_URL + "/pacientes";
            String jsonBody = pacienteToJson(paciente);
            System.out.println("📤 Creando paciente: " + jsonBody);
            System.out.println("👤 Usuario creador: " + (usuarioCreador != null ? usuarioCreador : "No especificado"));

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

            // Agregar header con usuario si está disponible
            if (usuarioCreador != null && !usuarioCreador.isEmpty()) {
                requestBuilder.header("X-Usuario", usuarioCreador);
            }

            HttpRequest request = requestBuilder.build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("✅ Paciente creado exitosamente");
                return parsePacienteFromJson(response.body());
            } else {
                System.out.println("❌ Error creando paciente: " + response.statusCode());
                System.out.println("Response: " + response.body());
                return null;
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Actualizar paciente
     */
    public static Paciente actualizarPaciente(Long id, Paciente paciente) {
        return actualizarPaciente(id, paciente, null);
    }

    public static Paciente actualizarPaciente(Long id, Paciente paciente, String usuarioModificador) {
        try {
            String url = BASE_URL + "/pacientes/" + id;
            String jsonBody = pacienteToJson(paciente);
            System.out.println("📤 Actualizando paciente ID " + id);
            System.out.println("👤 Usuario modificador: " + (usuarioModificador != null ? usuarioModificador : "No especificado"));

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody));

            // Agregar header con usuario si está disponible
            if (usuarioModificador != null && !usuarioModificador.isEmpty()) {
                requestBuilder.header("X-Usuario", usuarioModificador);
            }

            HttpRequest request = requestBuilder.build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("✅ Paciente actualizado exitosamente");
                return parsePacienteFromJson(response.body());
            } else {
                System.out.println("❌ Error actualizando paciente: " + response.statusCode());
                return null;
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Dar de baja paciente (borrado lógico)
     */
    public static boolean darDeBajaPaciente(Long id) {
        return darDeBajaPaciente(id, null);
    }

    public static boolean darDeBajaPaciente(Long id, String usuarioEliminador) {
        try {
            String url = BASE_URL + "/pacientes/" + id;
            System.out.println("📤 Dando de baja paciente ID: " + id);
            System.out.println("👤 Usuario eliminador: " + (usuarioEliminador != null ? usuarioEliminador : "No especificado"));

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .DELETE();

            // Agregar header con usuario si está disponible
            if (usuarioEliminador != null && !usuarioEliminador.isEmpty()) {
                requestBuilder.header("X-Usuario", usuarioEliminador);
            }

            HttpRequest request = requestBuilder.build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("✅ Paciente dado de baja exitosamente");
                return true;
            } else {
                System.out.println("❌ Error dando de baja paciente: " + response.statusCode());
                return false;
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            return false;
        }
    }

    // ==================== MÉTODOS PARA INFORMES ====================

    /**
     * Obtener todos los informes de un paciente
     */
    public static List<InformePaciente> getInformesByPaciente(Long pacienteId) {
        try {
            String url = BASE_URL + "/informes/paciente/" + pacienteId;
            System.out.println("🔄 Obteniendo informes del paciente ID: " + pacienteId);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                List<InformePaciente> informes = parseInformesFromJson(response.body());
                System.out.println("✅ Informes recibidos: " + informes.size());
                return informes;
            } else {
                System.out.println("❌ Error obteniendo informes: " + response.statusCode());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Obtener informe por ID
     */
    public static InformePaciente getInformeById(Long id) {
        try {
            String url = BASE_URL + "/informes/" + id;
            System.out.println("🔄 Obteniendo informe ID: " + id);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseInformeFromJson(response.body());
            } else {
                return null;
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Crear nuevo informe
     */
    public static InformePaciente crearInforme(InformePaciente informe) {
        try {
            String url = BASE_URL + "/informes";
            String jsonBody = informeToJson(informe);
            System.out.println("📤 Creando informe: " + jsonBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("✅ Informe creado exitosamente");
                return parseInformeFromJson(response.body());
            } else {
                System.out.println("❌ Error creando informe: " + response.statusCode());
                System.out.println("Response: " + response.body());
                return null;
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // ==================== MÉTODOS DE PARSEO PARA PACIENTES ====================

    private static List<Paciente> parsePacientesFromJson(String json) {
        List<Paciente> pacientes = new ArrayList<>();
        try {
            json = json.trim();
            if (!json.startsWith("[")) return pacientes;

            json = json.substring(1, json.length() - 1);
            List<String> objetos = splitJsonArray(json);

            for (String obj : objetos) {
                Paciente paciente = parsePacienteFromJson(obj);
                if (paciente != null) {
                    pacientes.add(paciente);
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error parseando pacientes: " + e.getMessage());
        }
        return pacientes;
    }

    private static Paciente parsePacienteFromJson(String json) {
        try {
            Paciente paciente = new Paciente();

            paciente.setId(parseLong(extractValue(json, "id")));
            paciente.setFechaNacimiento(extractValue(json, "fechaNacimiento"));
            paciente.setNivelEducativo(extractValue(json, "nivelEducativo"));
            paciente.setCondicion(extractValue(json, "condicion"));
            paciente.setAntecedentes(extractValue(json, "antecedentes"));
            paciente.setObservaciones(extractValue(json, "observaciones"));
            paciente.setFotoPath(extractValue(json, "fotoPath"));
            paciente.setFechaCreacion(extractValue(json, "fechaCreacion"));

            // Parsear usuario anidado
            String usuarioJson = extractNestedObject(json, "usuario");
            if (usuarioJson != null && !usuarioJson.isEmpty()) {
                paciente.setNombre(extractValue(usuarioJson, "nombre"));
                paciente.setApellido(extractValue(usuarioJson, "apellido"));
                paciente.setDni(extractValue(usuarioJson, "dni"));
                paciente.setTelefono(extractValue(usuarioJson, "telefono"));
                paciente.setEmail(extractValue(usuarioJson, "email"));
                paciente.setActivo(parseBoolean(extractValue(usuarioJson, "activo")));
                paciente.setUsuarioId(parseLong(extractValue(usuarioJson, "id")));
            }

            return paciente;
        } catch (Exception e) {
            System.out.println("❌ Error parseando paciente: " + e.getMessage());
            return null;
        }
    }

    private static String pacienteToJson(Paciente p) {
        StringBuilder json = new StringBuilder("{");

        // Usuario
        json.append("\"usuario\":{");
        json.append("\"dni\":\"").append(escapeJson(p.getDni())).append("\",");
        json.append("\"nombre\":\"").append(escapeJson(p.getNombre())).append("\",");
        json.append("\"apellido\":\"").append(escapeJson(p.getApellido())).append("\",");
        json.append("\"telefono\":\"").append(escapeJson(p.getTelefono())).append("\",");
        json.append("\"email\":\"").append(escapeJson(p.getEmail())).append("\",");
        json.append("\"tipo\":\"PACIENTE\"");
        json.append("},");

        // Datos del paciente
        json.append("\"fechaNacimiento\":\"").append(p.getFechaNacimiento()).append("\",");
        json.append("\"nivelEducativo\":\"").append(p.getNivelEducativo()).append("\",");
        json.append("\"condicion\":\"").append(escapeJson(p.getCondicion())).append("\"");

        if (p.getAntecedentes() != null && !p.getAntecedentes().isEmpty()) {
            json.append(",\"antecedentes\":\"").append(escapeJson(p.getAntecedentes())).append("\"");
        }

        if (p.getObservaciones() != null && !p.getObservaciones().isEmpty()) {
            json.append(",\"observaciones\":\"").append(escapeJson(p.getObservaciones())).append("\"");
        }

        json.append("}");
        return json.toString();
    }

    // ==================== MÉTODOS DE PARSEO PARA INFORMES ====================

    private static List<InformePaciente> parseInformesFromJson(String json) {
        List<InformePaciente> informes = new ArrayList<>();
        try {
            json = json.trim();
            if (!json.startsWith("[")) return informes;

            json = json.substring(1, json.length() - 1);
            List<String> objetos = splitJsonArray(json);

            for (String obj : objetos) {
                InformePaciente informe = parseInformeFromJson(obj);
                if (informe != null) {
                    informes.add(informe);
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error parseando informes: " + e.getMessage());
        }
        return informes;
    }

    private static InformePaciente parseInformeFromJson(String json) {
        try {
            InformePaciente informe = new InformePaciente();

            informe.setId(parseLong(extractValue(json, "id")));
            informe.setTitulo(extractValue(json, "titulo"));
            informe.setContenido(extractValue(json, "contenido"));
            informe.setFechaCreacion(extractValue(json, "fechaCreacion"));
            informe.setPacienteId(parseLong(extractValue(json, "pacienteId")));

            return informe;
        } catch (Exception e) {
            System.out.println("❌ Error parseando informe: " + e.getMessage());
            return null;
        }
    }

    private static String informeToJson(InformePaciente i) {
        StringBuilder json = new StringBuilder("{");

        json.append("\"paciente\":{\"id\":").append(i.getPacienteId()).append("},");
        json.append("\"titulo\":\"").append(escapeJson(i.getTitulo())).append("\",");
        json.append("\"contenido\":\"").append(escapeJson(i.getContenido())).append("\"");

        json.append("}");
        return json.toString();
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private static String extractNestedObject(String json, String key) {
        try {
            int keyIndex = json.indexOf("\"" + key + "\":");
            if (keyIndex == -1) return null;

            int startIndex = json.indexOf("{", keyIndex);
            if (startIndex == -1) return null;

            int braceCount = 1;
            int endIndex = startIndex + 1;

            while (endIndex < json.length() && braceCount > 0) {
                char c = json.charAt(endIndex);
                if (c == '{') braceCount++;
                else if (c == '}') braceCount--;
                endIndex++;
            }

            return json.substring(startIndex, endIndex);
        } catch (Exception e) {
            return null;
        }
    }

    private static String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}
