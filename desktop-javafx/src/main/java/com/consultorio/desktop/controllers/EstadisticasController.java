package com.consultorio.desktop.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class EstadisticasController {

    private static final String BASE_URL = "http://localhost:8080/api";

    @FXML private Button btnGenerarGrafico;
    @FXML private Label lblTituloGrafico;
    @FXML private Label lblInfoGrafico;
    @FXML private StackPane contenedorGrafico;
    @FXML private VBox mensajeInicial;

    private String usuarioActual;
    private String rolActual;
    private Stage previousStage;
    private boolean wasMaximized;

    @FXML
    public void initialize() {
        System.out.println("✅ EstadisticasController inicializado");
    }

    public void setUsuario(String usuario, String rol, Stage previousStage, boolean wasMaximized) {
        this.usuarioActual = usuario;
        this.rolActual = rol;
        this.previousStage = previousStage;
        this.wasMaximized = wasMaximized;
    }

    @FXML
    private void handleVolver() {
        try {
            System.out.println("🔙 Volviendo al Dashboard...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/consultorio/desktop/fxml/dashboard.fxml"));
            Parent root = loader.load();

            DashboardController controller = loader.getController();

            Stage currentStage = (Stage) btnGenerarGrafico.getScene().getWindow();
            controller.setUsuario(usuarioActual, rolActual, currentStage);

            Stage stage = new Stage();
            stage.setTitle("Cosmos - Dashboard");

            Scene scene = new Scene(root, 1200, 800);

            // Cargar CSS
            try {
                var cssResource = getClass().getResource("/com/consultorio/desktop/styles/styles.css");
                if (cssResource != null) {
                    scene.getStylesheets().add(cssResource.toExternalForm());
                    System.out.println("✅ CSS cargado correctamente");
                } else {
                    System.out.println("⚠️ No se encontró el archivo CSS");
                }
            } catch (Exception e) {
                System.out.println("⚠️ No se pudo cargar CSS: " + e.getMessage());
            }

            stage.setScene(scene);

            // Mantener el mismo estado de maximizado
            if (wasMaximized) {
                stage.setMaximized(true);
            }

            stage.show();
            currentStage.close();

            System.out.println("✅ Dashboard cargado correctamente");

        } catch (Exception e) {
            System.out.println("Error al volver al dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAbrirConfiguracion() {
        try {
            System.out.println("📊 Abriendo panel de configuración...");

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/consultorio/desktop/fxml/configuracion-estadisticas.fxml")
            );
            Parent root = loader.load();

            ConfiguracionEstadisticasController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Configurar Gráfico Estadístico - Cosmos");

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.showAndWait();

            // Si se generó el gráfico, obtener configuración y generar
            if (controller.isGenerado()) {
                Map<String, Object> config = controller.getConfiguracion();
                String tema = (String) config.get("tema");
                String periodo = (String) config.get("periodo");
                LocalDate fechaInicio = (LocalDate) config.get("fechaInicio");

                System.out.println("🔄 Generando gráfico: " + tema);

                // Generar el gráfico según el tema
                switch (tema) {
                    case "Nuevos Pacientes":
                        generarGraficoNuevosPacientes(periodo, fechaInicio);
                        break;
                    case "Nivel Educativo":
                        generarGraficoNivelEducativo();
                        break;
                    case "Condición":
                        generarGraficoCondicion();
                        break;
                    case "Demanda Mensual":
                        generarGraficoDemandaMensual();
                        break;
                }
            }

        } catch (Exception e) {
            System.out.println("❌ Error abriendo configuración: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la configuración: " + e.getMessage());
        }
    }

    private void generarGraficoNuevosPacientes(String periodo, LocalDate fechaInicio) {
        System.out.println("🔄 Generando gráfico: Nuevos Pacientes");
        System.out.println("   📅 Periodo: " + periodo);
        System.out.println("   📅 Fecha inicio: " + fechaInicio);

        LocalDate fechaFin = calcularFechaFin(fechaInicio, periodo);
        System.out.println("   📅 Fecha fin calculada: " + fechaFin);

        new Thread(() -> {
            try {
                String url = String.format(
                        "%s/estadisticas/nuevos-pacientes?fechaInicio=%s&fechaFin=%s",
                        BASE_URL,
                        fechaInicio.toString(),
                        fechaFin.toString()
                );
                System.out.println("🔗 URL completa: " + url);

                java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(url))
                        .header("Content-Type", "application/json")
                        .GET()
                        .build();

                System.out.println("📤 Enviando petición GET...");
                java.net.http.HttpResponse<String> response = client.send(request,
                        java.net.http.HttpResponse.BodyHandlers.ofString());

                System.out.println("📥 Response status: " + response.statusCode());
                System.out.println("📥 Response body: " + response.body());

                if (response.statusCode() != 200) {
                    javafx.application.Platform.runLater(() ->
                        mostrarAlerta("Error", "Error al obtener datos: " + response.statusCode()));
                    return;
                }

                Map<String, Object> datos = parsearRespuesta(response.body());

                Platform.runLater(() -> {
                    int cantidad = ((Number) datos.get("cantidad")).intValue();
                    String periodoStr = (String) datos.get("periodo");
                    String inicio = (String) datos.get("fechaInicio");
                    String fin = (String) datos.get("fechaFin");

                    // Crear gráfico de barras
                    CategoryAxis xAxis = new CategoryAxis();
                    NumberAxis yAxis = new NumberAxis();
                    BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

                    barChart.setTitle("Nuevos Pacientes Registrados");
                    xAxis.setLabel("Periodo");
                    yAxis.setLabel("Cantidad");

                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Pacientes");
                    series.getData().add(new XYChart.Data<>(periodoStr, cantidad));

                    barChart.getData().add(series);
                    barChart.setLegendVisible(false);

                    // Actualizar UI
                    lblTituloGrafico.setText("Nuevos Pacientes Registrados");
                    lblInfoGrafico.setText(String.format(
                            "Periodo: %s (%s al %s) - Total: %d pacientes",
                            periodoStr, formatearFecha(inicio), formatearFecha(fin), cantidad
                    ));

                    // Ocultar mensaje inicial y mostrar gráfico
                    mensajeInicial.setVisible(false);
                    mensajeInicial.setManaged(false);

                    contenedorGrafico.getChildren().clear();
                    contenedorGrafico.getChildren().add(barChart);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    mostrarAlerta("Error", "No se pudo generar el gráfico: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        }).start();
    }

    private void generarGraficoNivelEducativo() {
        System.out.println("🔄 Generando gráfico: Nivel Educativo");

        new Thread(() -> {
            try {
                String url = BASE_URL + "/estadisticas/nivel-educativo";
                System.out.println("🔗 URL completa: " + url);

                java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(url))
                        .header("Content-Type", "application/json")
                        .GET()
                        .build();

                System.out.println("📤 Enviando petición GET...");
                java.net.http.HttpResponse<String> response = client.send(request,
                        java.net.http.HttpResponse.BodyHandlers.ofString());

                System.out.println("📥 Response status: " + response.statusCode());
                System.out.println("📥 Response body: " + response.body());

                if (response.statusCode() != 200) {
                    javafx.application.Platform.runLater(() ->
                        mostrarAlerta("Error", "Error al obtener datos: " + response.statusCode()));
                    return;
                }

                Map<String, Object> datos = parsearRespuesta(response.body());

                Platform.runLater(() -> {
                    int total = ((Number) datos.get("total")).intValue();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> porcentajes = (Map<String, Object>) datos.get("porcentajes");

                    // Crear gráfico de torta
                    PieChart pieChart = new PieChart();
                    pieChart.setTitle("Distribución por Nivel Educativo");

                    // Agregar datos solo si el porcentaje es mayor a 0
                    for (Map.Entry<String, Object> entry : porcentajes.entrySet()) {
                        double porcentaje = ((Number) entry.getValue()).doubleValue();
                        if (porcentaje > 0) {
                            String label = String.format("%s (%.1f%%)", entry.getKey(), porcentaje);
                            PieChart.Data slice = new PieChart.Data(label, porcentaje);
                            pieChart.getData().add(slice);
                        }
                    }

                    // Actualizar UI
                    lblTituloGrafico.setText("Distribución por Nivel Educativo");
                    lblInfoGrafico.setText(String.format(
                            "Total de pacientes activos: %d", total
                    ));

                    // Ocultar mensaje inicial y mostrar gráfico
                    mensajeInicial.setVisible(false);
                    mensajeInicial.setManaged(false);

                    contenedorGrafico.getChildren().clear();
                    contenedorGrafico.getChildren().add(pieChart);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    mostrarAlerta("Error", "No se pudo generar el gráfico: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        }).start();
    }




    private void generarGraficoCondicion() {
        System.out.println("🔄 Generando gráfico: Condición");

        new Thread(() -> {
            try {
                String url = BASE_URL + "/estadisticas/condicion";
                System.out.println("🔗 URL completa: " + url);

                java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(url))
                        .header("Content-Type", "application/json")
                        .GET()
                        .build();

                System.out.println("📤 Enviando petición GET...");
                java.net.http.HttpResponse<String> response = client.send(request,
                        java.net.http.HttpResponse.BodyHandlers.ofString());

                System.out.println("📥 Response status: " + response.statusCode());
                System.out.println("📥 Response body: " + response.body());

                if (response.statusCode() != 200) {
                    javafx.application.Platform.runLater(() ->
                        mostrarAlerta("Error", "Error al obtener datos: " + response.statusCode()));
                    return;
                }

                Map<String, Object> datos = parsearRespuesta(response.body());

                Platform.runLater(() -> {
                    int total = ((Number) datos.get("total")).intValue();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> porcentajes = (Map<String, Object>) datos.get("porcentajes");

                    // Crear gráfico de torta
                    PieChart pieChart = new PieChart();
                    pieChart.setTitle("Distribución por Condición");

                    // Agregar datos solo si el porcentaje es mayor a 0
                    for (Map.Entry<String, Object> entry : porcentajes.entrySet()) {
                        double porcentaje = ((Number) entry.getValue()).doubleValue();
                        if (porcentaje > 0) {
                            String label = String.format("%s (%.1f%%)", entry.getKey(), porcentaje);
                            PieChart.Data slice = new PieChart.Data(label, porcentaje);
                            pieChart.getData().add(slice);
                        }
                    }

                    // Actualizar UI
                    lblTituloGrafico.setText("Distribución por Condición");
                    lblInfoGrafico.setText(String.format(
                            "Total de pacientes activos: %d", total
                    ));

                    // Ocultar mensaje inicial y mostrar gráfico
                    mensajeInicial.setVisible(false);
                    mensajeInicial.setManaged(false);

                    contenedorGrafico.getChildren().clear();
                    contenedorGrafico.getChildren().add(pieChart);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    mostrarAlerta("Error", "No se pudo generar el gráfico: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        }).start();
    }

    private void generarGraficoDemandaMensual() {
        System.out.println("🔄 Generando gráfico: Demanda Mensual");

        new Thread(() -> {
            try {
                String url = BASE_URL + "/estadisticas/demanda-mensual";
                System.out.println("🔗 URL completa: " + url);

                java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(url))
                        .header("Content-Type", "application/json")
                        .GET()
                        .build();

                System.out.println("📤 Enviando petición GET...");
                java.net.http.HttpResponse<String> response = client.send(request,
                        java.net.http.HttpResponse.BodyHandlers.ofString());

                System.out.println("📥 Response status: " + response.statusCode());
                System.out.println("📥 Response body: " + response.body());

                if (response.statusCode() != 200) {
                    javafx.application.Platform.runLater(() ->
                        mostrarAlerta("Error", "Error al obtener datos: " + response.statusCode()));
                    return;
                }

                Map<String, Object> datos = parsearRespuesta(response.body());

                Platform.runLater(() -> {
                    int anio = ((Number) datos.get("anio")).intValue();

                    // Crear gráfico de barras
                    CategoryAxis xAxis = new CategoryAxis();
                    NumberAxis yAxis = new NumberAxis();
                    BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

                    barChart.setTitle("Demanda Mensual de Turnos");
                    xAxis.setLabel("Mes");
                    yAxis.setLabel("Porcentaje (%)");

                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Turnos");

                    // Meses del año
                    String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun",
                                     "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};

                    @SuppressWarnings("unchecked")
                    Map<String, Object> porcentajes = (Map<String, Object>) datos.get("porcentajesMensual");

                    // Agregar datos por mes
                    for (int i = 1; i <= 12; i++) {
                        String mes = String.valueOf(i);
                        double porcentaje = porcentajes.containsKey(mes) ?
                                ((Number) porcentajes.get(mes)).doubleValue() : 0.0;
                        series.getData().add(new XYChart.Data<>(meses[i-1], porcentaje));
                    }

                    barChart.getData().add(series);
                    barChart.setLegendVisible(false);

                    // Actualizar UI
                    lblTituloGrafico.setText("Demanda Mensual de Turnos " + anio);
                    lblInfoGrafico.setText("Distribución porcentual de turnos por mes del año en curso");

                    // Ocultar mensaje inicial y mostrar gráfico
                    mensajeInicial.setVisible(false);
                    mensajeInicial.setManaged(false);

                    contenedorGrafico.getChildren().clear();
                    contenedorGrafico.getChildren().add(barChart);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    mostrarAlerta("Error", "No se pudo generar el gráfico: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        }).start();
    }

    // Métodos auxiliares
    private LocalDate calcularFechaFin(LocalDate fechaInicio, String periodo) {
        switch (periodo) {
            case "Un día":
                return fechaInicio;
            case "Una semana":
                return fechaInicio.plusDays(6);
            case "Un mes":
                return fechaInicio.plusMonths(1).minusDays(1);
            case "Un semestre":
                return fechaInicio.plusMonths(6).minusDays(1);
            case "Un año":
                return fechaInicio.plusYears(1).minusDays(1);
            default:
                return fechaInicio;
        }
    }


    private String formatearFecha(String fecha) {
        try {
            LocalDate date = LocalDate.parse(fecha);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return date.format(formatter);
        } catch (Exception e) {
            return fecha;
        }
    }

    private Map<String, Object> parsearRespuesta(String json) {
        Map<String, Object> resultado = new HashMap<>();

        try {
            json = json.trim();
            if (!json.startsWith("{")) return resultado;

            // Extraer valores simples
            resultado.put("cantidad", extraerNumero(json, "cantidad"));
            resultado.put("total", extraerNumero(json, "total"));
            resultado.put("anio", extraerNumero(json, "anio"));
            resultado.put("periodo", extraerValor(json, "periodo"));
            resultado.put("fechaInicio", extraerValor(json, "fechaInicio"));
            resultado.put("fechaFin", extraerValor(json, "fechaFin"));

            // Extraer objetos anidados
            String conteo = extraerObjeto(json, "conteo");
            if (conteo != null) {
                resultado.put("conteo", parsearMapaAnidado(conteo));
            }

            String porcentajes = extraerObjeto(json, "porcentajes");
            if (porcentajes != null) {
                resultado.put("porcentajes", parsearMapaAnidado(porcentajes));
            }

            String porcentajesMensual = extraerObjeto(json, "porcentajesMensual");
            if (porcentajesMensual != null) {
                resultado.put("porcentajesMensual", parsearMapaAnidado(porcentajesMensual));
            }

        } catch (Exception e) {
            System.out.println("❌ Error parseando respuesta: " + e.getMessage());
            e.printStackTrace();
        }

        return resultado;
    }

    private String extraerValor(String json, String clave) {
        String buscar = "\"" + clave + "\":";
        int inicio = json.indexOf(buscar);
        if (inicio == -1) return null;

        inicio += buscar.length();
        while (inicio < json.length() && Character.isWhitespace(json.charAt(inicio))) {
            inicio++;
        }

        if (inicio >= json.length()) return null;

        if (json.charAt(inicio) == '"') {
            inicio++;
            int fin = json.indexOf("\"", inicio);
            if (fin == -1) return null;
            return json.substring(inicio, fin);
        } else {
            int fin = inicio;
            while (fin < json.length() &&
                    json.charAt(fin) != ',' &&
                    json.charAt(fin) != '}' &&
                    json.charAt(fin) != ']') {
                fin++;
            }
            return json.substring(inicio, fin).trim();
        }
    }

    private Number extraerNumero(String json, String clave) {
        String valor = extraerValor(json, clave);
        if (valor == null || valor.equals("null")) return 0;
        try {
            if (valor.contains(".")) {
                return Double.parseDouble(valor);
            } else {
                return Integer.parseInt(valor);
            }
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String extraerObjeto(String json, String clave) {
        int inicio = json.indexOf("\"" + clave + "\":");
        if (inicio == -1) return null;

        int startBrace = json.indexOf("{", inicio);
        if (startBrace == -1) return null;

        int nivel = 1;
        int i = startBrace + 1;
        while (i < json.length() && nivel > 0) {
            if (json.charAt(i) == '{') nivel++;
            else if (json.charAt(i) == '}') nivel--;
            i++;
        }

        return json.substring(startBrace, i);
    }

    private Map<String, Object> parsearMapaAnidado(String json) {
        Map<String, Object> mapa = new HashMap<>();

        try {
            json = json.substring(1, json.length() - 1).trim();

            String[] pares = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

            for (String par : pares) {
                String[] partes = par.split(":", 2);
                if (partes.length == 2) {
                    String clave = partes[0].trim().replace("\"", "");
                    String valor = partes[1].trim();

                    if (valor.startsWith("\"")) {
                        mapa.put(clave, valor.substring(1, valor.length() - 1));
                    } else {
                        try {
                            if (valor.contains(".")) {
                                mapa.put(clave, Double.parseDouble(valor));
                            } else {
                                mapa.put(clave, Integer.parseInt(valor));
                            }
                        } catch (NumberFormatException e) {
                            mapa.put(clave, valor);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error parseando mapa anidado: " + e.getMessage());
        }

        return mapa;
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
