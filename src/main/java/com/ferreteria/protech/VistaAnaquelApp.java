package com.ferreteria.protech;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class VistaAnaquelApp extends Application {

    // 1. Definición del Enum de Estados
    public enum EstadoEspacio {
        DISPONIBLE,
        ALERTA_STOCK,
        LLENO
    }

    // 2. Clase interna del modelo de datos
    public static class EspacioAnaquel {
        private String id;
        private int fila;
        private int columna;
        private EstadoEspacio estado;
        private String nombreProducto;

        public EspacioAnaquel(String id, int fila, int columna, EstadoEspacio estado, String nombreProducto) {
            this.id = id;
            this.fila = fila;
            this.columna = columna;
            this.estado = estado;
            this.nombreProducto = nombreProducto;
        }

        public String getId() { return id; }
        public int getFila() { return fila; }
        public int getColumna() { return columna; }
        public EstadoEspacio getEstado() { return estado; }
        public String getNombreProducto() { return nombreProducto; }
    }

    @Override
    public void start(Stage primaryStage) {
        // Configuramos las dimensiones del anaquel
        int filas = 4;    // 4 Niveles
        int columnas = 5; // 5 Secciones por nivel
        
        // Generamos los datos de prueba
        List<EspacioAnaquel> datosAnaquel = generarDatosMock(filas, columnas);

        // Creamos la cuadrícula interactiva
        GridPane gridPane = crearCuadricula(filas, columnas, datosAnaquel);

        // Configuración de la ventana principal
        Scene scene = new Scene(gridPane, 800, 600);
        primaryStage.setTitle("Ferretería Pro-Tech - Vista Frontal Anaquel 2D");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Construye el GridPane basado en los parámetros y datos del modelo.
     */
    private GridPane crearCuadricula(int filas, int columnas, List<EspacioAnaquel> datos) {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(8); // Separación horizontal entre celdas (simula el metal del anaquel)
        grid.setVgap(8); // Separación vertical
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #1a1f2e;"); // Fondo oscuro general para contraste

        for (EspacioAnaquel espacio : datos) {
            // Creamos un StackPane que servirá como celda visual
            StackPane celda = new StackPane();
            celda.setPrefSize(140, 100);
            
            // Asignar color de fondo según el estado
            Color colorFondo = obtenerColorPorEstado(espacio.getEstado());
            celda.setBackground(new Background(new BackgroundFill(colorFondo, new CornerRadii(6), Insets.EMPTY)));
            
            // Efecto Hover (Ilumina la celda al pasar el mouse)
            celda.setOnMouseEntered(e -> celda.setOpacity(0.8));
            celda.setOnMouseExited(e -> celda.setOpacity(1.0));
            celda.setStyle("-fx-border-color: rgba(255,255,255,0.2); -fx-border-width: 2px; -fx-border-radius: 6px; -fx-cursor: hand;");

            // Añadir el texto del producto si no está disponible
            if (espacio.getEstado() != EstadoEspacio.DISPONIBLE) {
                Label lblProducto = new Label(espacio.getNombreProducto());
                lblProducto.setFont(Font.font("Inter", FontWeight.BOLD, 12));
                lblProducto.setTextFill(Color.WHITE);
                lblProducto.setWrapText(true);
                lblProducto.setAlignment(Pos.CENTER);
                celda.getChildren().add(lblProducto);
            } else {
                Label lblLibre = new Label("LIBRE");
                lblLibre.setFont(Font.font("Inter", FontWeight.BOLD, 12));
                lblLibre.setTextFill(Color.web("#00000055"));
                celda.getChildren().add(lblLibre);
            }

            // Añadir etiqueta pequeña para la ubicación (Ej: A-1-2)
            Label lblId = new Label(espacio.getId());
            lblId.setFont(Font.font("Inter", 10));
            lblId.setTextFill(Color.web("#ffffff88"));
            StackPane.setAlignment(lblId, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(lblId, new Insets(0, 5, 5, 0));
            celda.getChildren().add(lblId);

            // 5. INTERACTIVIDAD: Evento Clic
            celda.setOnMouseClicked(event -> {
                System.out.println("───────────────────────────────────────");
                System.out.println("📦 DETALLES DEL ESPACIO SELECCIONADO");
                System.out.println("ID: " + espacio.getId());
                System.out.println("Fila (Nivel): " + espacio.getFila());
                System.out.println("Columna (Sección): " + espacio.getColumna());
                System.out.println("Estado: " + espacio.getEstado());
                System.out.println("Producto: " + (espacio.getNombreProducto() == null ? "Ninguno" : espacio.getNombreProducto()));
                System.out.println("───────────────────────────────────────");
            });

            // Añadir la celda al GridPane
            // En GridPane, el sistema de coordenadas inicia en (0,0) arriba a la izquierda.
            // Restamos 1 a fila y columna para adaptarlo al index 0 de JavaFX.
            grid.add(celda, espacio.getColumna() - 1, espacio.getFila() - 1);
        }

        return grid;
    }

    /**
     * Mapeo de estados a colores visuales de la interfaz
     */
    private Color obtenerColorPorEstado(EstadoEspacio estado) {
        switch (estado) {
            case DISPONIBLE:
                return Color.web("#CCFF00");     // Verde/Amarillo brillante (Libre)
            case ALERTA_STOCK:
                return Color.web("#FF9F1C");     // Naranja (Stock Crítico)
            case LLENO:
                return Color.web("#457B9D");     // Azul oscuro (Lleno/Activo)
            default:
                return Color.web("#333333");
        }
    }

    /**
     * Generador de datos quemados (Mock) para poblar el anaquel
     */
    private List<EspacioAnaquel> generarDatosMock(int filas, int columnas) {
        List<EspacioAnaquel> lista = new ArrayList<>();
        
        for (int f = 1; f <= filas; f++) {
            for (int c = 1; c <= columnas; c++) {
                String id = "PAS-A-N" + f + "-S" + c; // Ej: PAS-A-N1-S1 (Pasillo A, Nivel 1, Sección 1)
                
                EstadoEspacio estado;
                String producto = null;

                // Generamos una distribución aleatoria predecible para que se vea realista
                if (f == 4 && c > 2) {
                    // Nivel más alto y a la derecha lo dejamos vacío
                    estado = EstadoEspacio.DISPONIBLE;
                } else if (f == 1 && c == 2 || f == 2 && c == 4) {
                    // Simulamos algunos puntos críticos
                    estado = EstadoEspacio.ALERTA_STOCK;
                    producto = "Taladro DeWalt";
                } else {
                    // El resto lleno
                    estado = EstadoEspacio.LLENO;
                    producto = "Martillos Stanley";
                }

                lista.add(new EspacioAnaquel(id, f, c, estado, producto));
            }
        }
        return lista;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
