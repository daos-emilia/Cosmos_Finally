package com.consultorio.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class EmailService {

    @Value("${RESEND_API_KEY}")
    private String resendApiKey;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.email.from:Consultorio Cosmos <noreply@consultoriocosmos.com>}")
    private String fromEmail;

    public void enviarEmailBienvenidaYValidacion(String toEmail, String nombre, String apellido, String tokenValidacion) {
        enviarEmailBienvenidaYValidacion(toEmail, nombre, apellido, tokenValidacion, null);
    }

    public void enviarEmailBienvenidaYValidacion(String toEmail, String nombre, String apellido, String tokenValidacion, String passwordInicial) {
        String validacionUrl = baseUrl + "/api/usuarios/validar-email?token=" + tokenValidacion;
        String logoUrl = baseUrl + "/images/logo.png";

        System.out.println("📧 ===== ENVIANDO EMAIL CON RESEND API =====");
        System.out.println("👉 Para: " + nombre + " " + apellido);
        System.out.println("📨 Email: " + toEmail);
        System.out.println("🔗 URL: " + validacionUrl);

        // Construir mensaje de contraseña si se proporciona
        String passwordInfo = "";
        if (passwordInicial != null && !passwordInicial.isEmpty()) {
            passwordInfo = String.format(
                "<div style='background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0;'>" +
                "<p style='margin: 0 0 10px 0;'><strong>📌 Información de acceso:</strong></p>" +
                "<p style='margin: 0;'>Tu contraseña temporal es: <strong style='color: #2E86AB; font-size: 18px;'>%s</strong></p>" +
                "<p style='margin: 10px 0 0 0; font-size: 14px; color: #666;'><em>Por seguridad, te recomendamos cambiarla en tu primer inicio de sesión.</em></p>" +
                "</div>", passwordInicial
            );
        }

        // Usar el email configurado en las variables de entorno
        String jsonBody = String.format("""
            {
                "from": "%s",
                "to": ["%s"],
                "subject": "Consultorio Cosmos - Valida tu email",
                "html": "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'><div style='text-align: center; margin-bottom: 20px;'><img src='%s' alt='Consultorio Cosmos' style='width: 120px; height: 120px; border-radius: 50%%;'></div><h2 style='color: #2E86AB; text-align: center;'>¡Bienvenido/a al Consultorio Cosmos!</h2><p>Hola <strong>%s %s</strong>,</p><p>Para activar tu cuenta en nuestro sistema, por favor valida tu email haciendo clic en el siguiente botón:</p><p style='text-align: center;'><a href='%s' style='background-color: #4CAF50; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block; font-weight: bold;'>Validar Mi Email</a></p><p>O copia y pega este enlace en tu navegador:<br><code style='background: #f4f4f4; padding: 8px; border-radius: 3px;'>%s</code></p>%s<p><em>Este enlace expirará en 24 horas.</em></p><hr style='border: none; border-top: 1px solid #eee;'><p style='color: #666;'>Si no te registraste en Consultorio Cosmos, por favor ignora este mensaje.</p><p><strong>Atentamente,</strong><br>Equipo del Consultorio Cosmos<br><span style='color: #2E86AB;'>consultorio.cosmos@gmail.com</span></p></div>"
            }
            """, fromEmail, toEmail, logoUrl, nombre, apellido, validacionUrl, validacionUrl, passwordInfo);

        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + resendApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("✅ RESEND API RESPONSE: " + response.statusCode());

            if (response.statusCode() == 200) {
                System.out.println("🎉 EMAIL ENVIADO EXITOSAMENTE");
                System.out.println("📨 Destino: " + toEmail);
            } else {
                System.out.println("❌ ERROR RESEND: " + response.body());
            }

        } catch (Exception e) {
            System.err.println("💥 ERROR ENVIANDO EMAIL: " + e.getMessage());
        }
    }

    public void enviarRecordatorioPassword(String toEmail, String nombre, String apellido, String password) {
        String logoUrl = baseUrl + "/images/logo.png";

        System.out.println("📧 ===== ENVIANDO RECORDATORIO DE CONTRASEÑA =====");
        System.out.println("👉 Para: " + nombre + " " + apellido);
        System.out.println("📨 Email: " + toEmail);

        String jsonBody = String.format("""
            {
                "from": "%s",
                "to": ["%s"],
                "subject": "Consultorio Cosmos - Recordatorio de Contraseña",
                "html": "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'><div style='text-align: center; margin-bottom: 20px;'><img src='%s' alt='Consultorio Cosmos' style='width: 120px; height: 120px; border-radius: 50%%;'></div><h2 style='color: #2E86AB; text-align: center;'>Recordatorio de Contraseña</h2><p>Hola <strong>%s %s</strong>,</p><p>Has solicitado un recordatorio de tu contraseña para acceder al sistema del Consultorio Cosmos.</p><div style='background-color: #f4f4f4; padding: 20px; border-radius: 5px; margin: 20px 0;'><p style='margin: 0;'><strong>Tu contraseña es:</strong></p><p style='font-size: 24px; color: #2E86AB; font-weight: bold; margin: 10px 0;'>%s</p></div><hr style='border: none; border-top: 1px solid #eee; margin-top: 30px;'><p style='color: #666;'>Si no solicitaste este recordatorio, por favor ignora este mensaje o contacta con el administrador.</p><p><strong>Atentamente,</strong><br>Equipo del Consultorio Cosmos<br><span style='color: #2E86AB;'>consultorio.cosmos@gmail.com</span></p></div>"
            }
            """, fromEmail, toEmail, logoUrl, nombre, apellido, password);

        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + resendApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("✅ RESEND API RESPONSE: " + response.statusCode());

            if (response.statusCode() == 200) {
                System.out.println("🎉 RECORDATORIO ENVIADO EXITOSAMENTE");
                System.out.println("📨 Destino: " + toEmail);
            } else {
                System.out.println("❌ ERROR RESEND: " + response.body());
            }

        } catch (Exception e) {
            System.err.println("💥 ERROR ENVIANDO RECORDATORIO: " + e.getMessage());
        }
    }

    public void enviarConfirmacionTurno(String toEmail, String nombrePaciente, String apellidoPaciente,
                                        String fecha, String horaInicio, String horaFin) {
        String logoUrl = baseUrl + "/images/logo.png";

        System.out.println("📧 ===== ENVIANDO CONFIRMACIÓN DE TURNO =====");
        System.out.println("👉 Para: " + nombrePaciente + " " + apellidoPaciente);
        System.out.println("📨 Email: " + toEmail);
        System.out.println("📅 Fecha: " + fecha + " " + horaInicio + " - " + horaFin);

        String jsonBody = String.format("""
            {
                "from": "%s",
                "to": ["%s"],
                "subject": "Consultorio Cosmos - Confirmación de Turno",
                "html": "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'><div style='text-align: center; margin-bottom: 20px;'><img src='%s' alt='Consultorio Cosmos' style='width: 120px; height: 120px; border-radius: 50%%;'></div><h2 style='color: #2E86AB; text-align: center;'>✅ Turno Confirmado</h2><p>Hola <strong>%s %s</strong>,</p><p>Tu turno ha sido confirmado exitosamente.</p><div style='background-color: #e8f5e9; padding: 20px; border-left: 4px solid #4CAF50; border-radius: 5px; margin: 20px 0;'><h3 style='margin-top: 0; color: #2E86AB;'>📋 Detalles del Turno</h3><p style='margin: 8px 0;'><strong>📅 Fecha:</strong> %s</p><p style='margin: 8px 0;'><strong>🕐 Horario:</strong> %s - %s</p><p style='margin: 8px 0;'><strong>📍 Lugar:</strong> Consultorio Cosmos</p></div><p><strong>⚠️ Importante:</strong></p><ul><li>Por favor, llega 5 minutos antes de tu turno</li><li>Si necesitas cancelar o reprogramar, contacta con anticipación</li></ul><p style='text-align: center; margin-top: 30px;'><a href='%s' style='background-color: #2E86AB; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block; font-weight: bold;'>Ver Mis Turnos</a></p><hr style='border: none; border-top: 1px solid #eee; margin-top: 30px;'><p><strong>Atentamente,</strong><br>Equipo del Consultorio Cosmos<br><span style='color: #2E86AB;'>consultorio.cosmos@gmail.com</span></p></div>"
            }
            """, fromEmail, toEmail, logoUrl, nombrePaciente, apellidoPaciente, fecha, horaInicio, horaFin, baseUrl);

        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + resendApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("✅ RESEND API RESPONSE: " + response.statusCode());

            if (response.statusCode() == 200) {
                System.out.println("🎉 CONFIRMACIÓN DE TURNO ENVIADA EXITOSAMENTE");
                System.out.println("📨 Destino: " + toEmail);
            } else {
                System.out.println("❌ ERROR RESEND: " + response.body());
            }

        } catch (Exception e) {
            System.err.println("💥 ERROR ENVIANDO CONFIRMACIÓN: " + e.getMessage());
        }
    }

    public void enviarRecordatorioTurno(String toEmail, String nombrePaciente, String apellidoPaciente,
                                        String fecha, String horaInicio, String horaFin) {
        String logoUrl = baseUrl + "/images/logo.png";

        System.out.println("📧 ===== ENVIANDO RECORDATORIO DE TURNO =====");
        System.out.println("👉 Para: " + nombrePaciente + " " + apellidoPaciente);
        System.out.println("📨 Email: " + toEmail);
        System.out.println("📅 Fecha: " + fecha + " " + horaInicio + " - " + horaFin);

        String jsonBody = String.format("""
            {
                "from": "%s",
                "to": ["%s"],
                "subject": "Consultorio Cosmos - Recordatorio de Turno",
                "html": "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'><div style='text-align: center; margin-bottom: 20px;'><img src='%s' alt='Consultorio Cosmos' style='width: 120px; height: 120px; border-radius: 50%%;'></div><h2 style='color: #FF9800; text-align: center;'>🔔 Recordatorio de Turno</h2><p>Hola <strong>%s %s</strong>,</p><p>Te recordamos que tienes un turno próximo en el Consultorio Cosmos.</p><div style='background-color: #fff3e0; padding: 20px; border-left: 4px solid #FF9800; border-radius: 5px; margin: 20px 0;'><h3 style='margin-top: 0; color: #2E86AB;'>📋 Detalles del Turno</h3><p style='margin: 8px 0;'><strong>📅 Fecha:</strong> %s</p><p style='margin: 8px 0;'><strong>🕐 Horario:</strong> %s - %s</p><p style='margin: 8px 0;'><strong>📍 Lugar:</strong> Consultorio Cosmos</p></div><p><strong>⚠️ Recordatorios:</strong></p><ul><li>Por favor, llega 5 minutos antes de tu turno</li><li>Si necesitas cancelar o reprogramar, contacta con anticipación</li><li>Trae tu DNI y cualquier documentación relevante</li></ul><p style='text-align: center; margin-top: 30px;'><a href='%s' style='background-color: #FF9800; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block; font-weight: bold;'>Ver Mis Turnos</a></p><hr style='border: none; border-top: 1px solid #eee; margin-top: 30px;'><p><strong>Atentamente,</strong><br>Equipo del Consultorio Cosmos<br><span style='color: #2E86AB;'>consultorio.cosmos@gmail.com</span></p></div>"
            }
            """, fromEmail, toEmail, logoUrl, nombrePaciente, apellidoPaciente, fecha, horaInicio, horaFin, baseUrl);

        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + resendApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("✅ RESEND API RESPONSE: " + response.statusCode());

            if (response.statusCode() == 200) {
                System.out.println("🎉 RECORDATORIO DE TURNO ENVIADO EXITOSAMENTE");
                System.out.println("📨 Destino: " + toEmail);
            } else {
                System.out.println("❌ ERROR RESEND: " + response.body());
            }

        } catch (Exception e) {
            System.err.println("💥 ERROR ENVIANDO RECORDATORIO: " + e.getMessage());
        }
    }
}
