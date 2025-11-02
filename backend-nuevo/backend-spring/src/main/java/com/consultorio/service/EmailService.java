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

    public void enviarEmailBienvenidaYValidacion(String toEmail, String nombre, String apellido, String tokenValidacion) {
        String validacionUrl = baseUrl + "/api/usuarios/validar-email?token=" + tokenValidacion;

        System.out.println("📧 ===== ENVIANDO EMAIL CON RESEND API =====");
        System.out.println("👉 Para: " + nombre + " " + apellido);
        System.out.println("📨 Email: " + toEmail);
        System.out.println("🔗 URL: " + validacionUrl);

        // SOLUCIÓN 2: Usar dominio temporal de Resend
        String jsonBody = String.format("""
            {
                "from": "Consultorio Cosmos <onboarding@resend.dev>",
                "to": ["%s"],
                "subject": "Consultorio Cosmos - Valida tu email",
                "html": "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'><h2 style='color: #2E86AB;'>¡Bienvenido/a al Consultorio Cosmos!</h2><p>Hola <strong>%s %s</strong>,</p><p>Para activar tu cuenta en nuestro sistema, por favor valida tu email haciendo clic en el siguiente botón:</p><p style='text-align: center;'><a href='%s' style='background-color: #4CAF50; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block; font-weight: bold;'>Validar Mi Email</a></p><p>O copia y pega este enlace en tu navegador:<br><code style='background: #f4f4f4; padding: 8px; border-radius: 3px;'>%s</code></p><p><em>Este enlace expirará en 24 horas.</em></p><hr style='border: none; border-top: 1px solid #eee;'><p style='color: #666;'>Si no te registraste en Consultorio Cosmos, por favor ignora este mensaje.</p><p><strong>Atentamente,</strong><br>Equipo del Consultorio Cosmos<br><span style='color: #2E86AB;'>consultorio.cosmos@gmail.com</span></p></div>"
            }
            """, toEmail, nombre, apellido, validacionUrl, validacionUrl);

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
        System.out.println("📧 Recordatorio enviado a: " + toEmail);
    }
}
