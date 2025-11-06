package com.consultorio.desktop.controllers;

import com.consultorio.desktop.models.InformePaciente;
import com.consultorio.desktop.models.Paciente;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class VerInformeController {

    @FXML private Label lblPaciente;
    @FXML private Label lblFecha;
    @FXML private Label lblTitulo;
    @FXML private TextArea txtContenido;

    private InformePaciente informe;
    private Paciente paciente;

    public void cargarInforme(InformePaciente informe, Paciente paciente) {
        this.informe = informe;
        this.paciente = paciente;

        // Cargar datos en la interfaz
        lblPaciente.setText(paciente.getNombreCompleto());
        lblTitulo.setText(informe.getTitulo());
        lblFecha.setText(informe.getFechaCreacionFormateada());
        txtContenido.setText(informe.getContenido());
    }

    @FXML
    private void handleDescargarPDF() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Informe como PDF");
            DateTimeFormatter fileNameFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            fileChooser.setInitialFileName("Informe_" + paciente.getApellido() + "_" +
                fileNameFormatter.format(LocalDateTime.now()) + ".pdf");

            // Establecer directorio inicial en Descargas
            String userHome = System.getProperty("user.home");
            File downloadsDir = new File(userHome, "Downloads");
            if (downloadsDir.exists()) {
                fileChooser.setInitialDirectory(downloadsDir);
            }

            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivo PDF", "*.pdf")
            );

            Stage stage = (Stage) lblPaciente.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                guardarInformeComoPDF(file);
                mostrarExito("Informe Guardado", "El informe se ha guardado exitosamente en:\n" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            mostrarError("Error", "No se pudo guardar el informe: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void guardarInformeComoPDF(File file) throws IOException {
        PDDocument document = new PDDocument();

        try {
            // Configuración de fuentes y márgenes
            float margin = 50;
            float yStart = PDRectangle.A4.getHeight() - margin;
            float width = PDRectangle.A4.getWidth() - 2 * margin;
            float leading = 15f; // Espacio entre líneas

            // Crear primera página
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            float yPosition = yStart;

            // TÍTULO DEL DOCUMENTO (Times New Roman, tamaño 16)
            contentStream.setFont(PDType1Font.TIMES_BOLD, 16);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("INFORME DE EVOLUCION");
            contentStream.endText();

            // LOGO (arriba a la derecha, al mismo nivel del título)
            try {
                InputStream logoStream = getClass().getResourceAsStream("/com/consultorio/desktop/images/logo.png");
                if (logoStream != null) {
                    PDImageXObject logo = PDImageXObject.createFromByteArray(document, logoStream.readAllBytes(), "logo");
                    float logoWidth = 80;
                    float logoHeight = 80;
                    float logoX = PDRectangle.A4.getWidth() - margin - logoWidth;
                    // Punto medio entre la posición anterior y la muy arriba
                    float logoY = yPosition - 40; // Centrado verticalmente con el título
                    contentStream.drawImage(logo, logoX, logoY, logoWidth, logoHeight);
                    logoStream.close();
                }
            } catch (Exception e) {
                System.out.println("⚠️ No se pudo cargar el logo: " + e.getMessage());
            }

            yPosition -= 30;

            // LÍNEA SEPARADORA
            contentStream.setLineWidth(1f);
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(PDRectangle.A4.getWidth() - margin, yPosition);
            contentStream.stroke();
            yPosition -= 20;

            // INFORMACIÓN DEL PACIENTE (Times New Roman, tamaño 11)
            contentStream.setFont(PDType1Font.TIMES_BOLD, 11);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("PACIENTE: ");
            contentStream.endText();

            contentStream.setFont(PDType1Font.TIMES_ROMAN, 11);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin + 80, yPosition);
            contentStream.showText(paciente.getNombreCompleto());
            contentStream.endText();
            yPosition -= leading;

            contentStream.setFont(PDType1Font.TIMES_BOLD, 11);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("DNI: ");
            contentStream.endText();

            contentStream.setFont(PDType1Font.TIMES_ROMAN, 11);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin + 80, yPosition);
            contentStream.showText(paciente.getDni());
            contentStream.endText();
            yPosition -= leading;

            contentStream.setFont(PDType1Font.TIMES_BOLD, 11);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("FECHA: ");
            contentStream.endText();

            contentStream.setFont(PDType1Font.TIMES_ROMAN, 11);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin + 80, yPosition);
            contentStream.showText(lblFecha.getText());
            contentStream.endText();
            yPosition -= leading;

            contentStream.setFont(PDType1Font.TIMES_BOLD, 11);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("TITULO: ");
            contentStream.endText();

            contentStream.setFont(PDType1Font.TIMES_ROMAN, 11);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin + 80, yPosition);
            contentStream.showText(informe.getTitulo());
            contentStream.endText();
            yPosition -= 25;

            // LÍNEA SEPARADORA
            contentStream.setLineWidth(0.5f);
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(PDRectangle.A4.getWidth() - margin, yPosition);
            contentStream.stroke();
            yPosition -= 20;

            // CONTENIDO DEL INFORME (Times New Roman)
            contentStream.setFont(PDType1Font.TIMES_BOLD, 11);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("CONTENIDO:");
            contentStream.endText();
            yPosition -= 20;

            // Dividir el contenido en líneas que quepan en el ancho de la página
            contentStream.setFont(PDType1Font.TIMES_ROMAN, 11);
            List<String> lines = splitTextIntoLines(informe.getContenido(), PDType1Font.TIMES_ROMAN, 11, width);

            for (String line : lines) {
                // Si no hay espacio en la página, crear una nueva
                if (yPosition < margin + 20) {
                    // Cerrar el stream actual
                    contentStream.close();

                    // Crear nueva página
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    yPosition = yStart;

                    contentStream.setFont(PDType1Font.TIMES_ROMAN, 11);
                }

                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText(line);
                contentStream.endText();
                yPosition -= leading;
            }

            // PIE DE PÁGINA en la última página (centrado)
            yPosition = margin;
            contentStream.setFont(PDType1Font.TIMES_ITALIC, 9);
            String piePagina = "Lic. en Psicopedagogia Silvina Accinelli - Consultorio Cosmos";
            float textWidth = PDType1Font.TIMES_ITALIC.getStringWidth(piePagina) / 1000 * 9;
            float centeredX = (PDRectangle.A4.getWidth() - textWidth) / 2;
            contentStream.beginText();
            contentStream.newLineAtOffset(centeredX, yPosition);
            contentStream.showText(piePagina);
            contentStream.endText();

            contentStream.close();
            document.save(file);
        } finally {
            document.close();
        }
    }

    private List<String> splitTextIntoLines(String text, PDType1Font font, int fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] paragraphs = text.split("\n");

        for (String paragraph : paragraphs) {
            if (paragraph.trim().isEmpty()) {
                lines.add("");
                continue;
            }

            String[] words = paragraph.split(" ");
            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
                float textWidth = font.getStringWidth(testLine) / 1000 * fontSize;

                if (textWidth > maxWidth) {
                    if (currentLine.length() > 0) {
                        lines.add(currentLine.toString());
                        currentLine = new StringBuilder(word);
                    } else {
                        // Palabra muy larga, dividirla
                        lines.add(word);
                    }
                } else {
                    currentLine = new StringBuilder(testLine);
                }
            }

            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }
        }

        return lines;
    }

    @FXML
    private void handleCerrar() {
        Stage stage = (Stage) lblPaciente.getScene().getWindow();
        stage.close();
    }

    private void mostrarExito(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

