import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import javax.sound.sampled.*;


public class Vector extends JFrame implements KeyListener {

    private BufferedImage buffer;
    private Graphics2D graPixel;

    private int puntoDePerspectiva = 500; // Declarar la variable puntoDePerspectiva

    private int xCabeza = 0;
    private int yCabeza = 0;
    private int zCabeza = 0;

    private double anguloX = 0; // Angulo de rotación en el eje X
    private double anguloY = 0; // Angulo de rotación en el eje Y
    private double anguloZ = 0; // Angulo de rotación en el eje Z

    private int directionX = 1;
    private int directionY = 1;
    private int directionZ = 1;

    private boolean rotarX = false;
    private boolean rotarY = false;
    private boolean rotarZ = false;

    private boolean mostrarVertices = true;
    private boolean mostrarVerticesUnidos = false;
    private boolean rellenarPoligono = true;

    private Clip backgroundMusic;

    private Color fillBody;
    private Color fillFace;

    // Definir los vértices de la cara en 3D
    private int[][] verticesCabeza = {
            {150, 400, 150},
            {150, 400, -150},
            {150, 700, 150},
            {150, 700, -150},
            {-150, 400, 150},
            {-150, 400, -150},
            {-150, 700, 150},
            {-150, 700, -150}
    };

    private int[][] verticesPata1 = {
            {130, 690, -90},
            {130, 690, -130},
            {130, 900, -40},
            {130, 900, -80},
            {90, 690, -90},
            {90, 690, -130},
            {90, 900, -40},
            {90, 900, -80}
    };

    // Estructura de las patas
    private int[][] verticesPata2;
    private int[][] verticesPata3;
    private int[][] verticesPata4;
    private int[][] verticesPata5;
    private int[][] verticesPata6;
    private int[][] verticesPata7;
    private int[][] verticesPata8;
    private int[][] verticesPata9;

    // Definir los vértices del ojo 1 en 3D
    private int[][] verticesOjo1 = {
            {-20, 510, -150},
            {-20, 510, -160},
            {-20, 495, -150},
            {-20, 495, -160},
            {-100, 510, -150},
            {-100, 510, -160},
            {-100, 495, -150},
            {-100, 495, -160}
    };

    // Definir los vértices del ojo 2 en 3D
    private int[][] verticesOjo2 = {
            {20, 510, -150},
            {20, 510, -160},
            {20, 495, -150},
            {20, 495, -160},
            {100, 510, -150},
            {100, 510, -160},
            {100, 495, -150},
            {100, 495, -160}
    };

    // Definir los vértices de la boca en 3D
    private int[][] verticesBoca = {
            {40, 615, -150},
            {40, 615, -160},
            {40, 600, -150},
            {40, 600, -160},
            {-40, 615, -150},
            {-40, 615, -160},
            {-40, 600, -150},
            {-40, 600, -160}
    };

    private int[][] caras = {
            {0, 1, 2, 3},  // Cara frontal
            {4, 6, 7, 5},  // Cara trasera
            {0, 4, 5, 1},  // Cara superior
            {2, 6, 7, 3},  // Cara inferior
            {0, 2, 6, 4},  // Cara izquierda
            {1, 3, 7, 5}   // Cara derecha
    };


    public Vector() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 1000);
        setLocationRelativeTo(null);

        buffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        graPixel = buffer.createGraphics();

        Graphics2D g2d = (Graphics2D) graPixel;
        g2d.setBackground(Color.decode("#8A2429"));
        g2d.clearRect(0, 0, getWidth(), getHeight());

        addKeyListener(this);  // Agregar el escuchador de teclas

        // Declaramos un valor para todas las coordenadas de las patas
        verticesPata2 = cloneAndTranslate(verticesPata1, -110, 0, 0);
        verticesPata3 = cloneAndTranslate(verticesPata1, -220, 0, 0);

        verticesPata4 = cloneAndTranslate(verticesPata1, 0, 0, 110);
        verticesPata5 = cloneAndTranslate(verticesPata1, -110, 0, 110);
        verticesPata6 = cloneAndTranslate(verticesPata1, -220, 0, 110);

        verticesPata7 = cloneAndTranslate(verticesPata1, 0, 0, 220);
        verticesPata8 = cloneAndTranslate(verticesPata1, -110, 0, 220);
        verticesPata9 = cloneAndTranslate(verticesPata1, -220, 0, 220);

        fillBody = Color.decode("#F5FAFC");
        fillFace = Color.decode("#575F6E");

        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("pigstep.wav").getAbsoluteFile());
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioInputStream);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }

        // Configurar temporizador para la rotación continua
        Timer timer = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (rotarX) {
                    anguloX += 0.01; // Ajusta la velocidad de rotación según sea necesario
                }
                if (rotarY) {
                    anguloY += 0.01;
                }
                if (rotarZ) {
                    anguloZ += 0.01;
                }
                drawPoligon3D();
            }
        });
        timer.start();
    }

    // Método para clonar y trasladar las coordenadas
    private int[][] cloneAndTranslate(int[][] original, int translateX, int translateY, int translateZ) {
        int[][] result = new int[original.length][original[0].length];
        for (int i = 0; i < original.length; i++) {
            for (int j = 0; j < original[i].length; j++) {
                result[i][j] = original[i][j];
            }
            // Aplicar traslación
            result[i][0] += translateX;
            result[i][1] += translateY;
            result[i][2] += translateZ;
        }
        return result;
    }

    public void putPixel(int x, int y, Color c) {
        if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight()) {
            buffer.setRGB(x, y, c.getRGB());
        }
    }

    public void clearMyScreen() {
        graPixel.clearRect(0, 0, getWidth(), getHeight());
    }

    // Algoritmo de Bresenham para dibujar una línea
    public void drawLineBresenham(int x1, int y1, int x2, int y2, Color c) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = (x1 < x2) ? 1 : -1;
        int sy = (y1 < y2) ? 1 : -1;
        int err = dx - dy;

        while (true) {
            putPixel(x1, y1, c);

            if (x1 == x2 && y1 == y2) {
                break;
            }

            int e2 = 2 * err;
            if (e2 > -dy) {
                err = err - dy;
                x1 = x1 + sx;
            }
            if (e2 < dx) {
                err = err + dx;
                y1 = y1 + sy;
            }
        }
    }

    public int[][] aplicarTransformacion(int[][] vertices, double[][] matrizTransformacion) {
        int[][] verticesTransformados = new int[vertices.length][3];
    
        for (int i = 0; i < vertices.length; i++) {
            int[] resultadoRotacion = vertices[i];
    
            // Aplicar la traslación antes de la rotación
            resultadoRotacion[0] += xCabeza;
            resultadoRotacion[1] += yCabeza;
            resultadoRotacion[2] += zCabeza;
    
            // Aplicar la transformación utilizando la matriz
            for (int j = 0; j < 3; j++) {
                double suma = 0;
                for (int k = 0; k < 3; k++) {
                    suma += resultadoRotacion[k] * matrizTransformacion[k][j];
                }
                verticesTransformados[i][j] = (int) suma;
            }
        }
    
        return verticesTransformados;
    }
    

    // Función para dibujar un conjunto de vértices proyectados en 2D
    public void dibujarVertices(int[][] vertices, int puntoDePerspectiva) {
        for (int i = 0; i < vertices.length; i++) {
            int x = (vertices[i][0] * puntoDePerspectiva) / (vertices[i][2] + puntoDePerspectiva) + getWidth() / 2;
            int y = (vertices[i][1] * puntoDePerspectiva) / (vertices[i][2] + puntoDePerspectiva) + getHeight() / 2;

            // Dibujar los vértices
            graPixel.setColor(Color.WHITE);
            graPixel.fillRect(x, y, 3, 3);
        }
    }

    // Función para dibujar un conjunto de líneas entre vértices
    public void dibujarLineas(int[][] vertices, Color fill) {
        for (int i = 0; i < vertices.length; i++) {
            int x1 = (vertices[i][0] * puntoDePerspectiva) / (vertices[i][2] + puntoDePerspectiva) + getWidth() / 2;
            int y1 = (vertices[i][1] * puntoDePerspectiva) / (vertices[i][2] + puntoDePerspectiva) + getHeight() / 2;

            // Conectar los vértices para formar el objeto
            for (int j = i + 1; j < vertices.length; j++) {
                int x2 = (vertices[j][0] * puntoDePerspectiva) / (vertices[j][2] + puntoDePerspectiva) + getWidth() / 2;
                int y2 = (vertices[j][1] * puntoDePerspectiva) / (vertices[j][2] + puntoDePerspectiva) + getHeight() / 2;

                drawLineBresenham(x1, y1, x2, y2, fill);
            }
        }
    }

    public void dibujarBordes(int[][] caras, int[][] vertices, Color fill) {
        // Crear un array para almacenar las distancias de las caras al espectador
        double[] distancias = new double[caras.length];
    
        // Calcular la distancia al espectador para cada cara
        for (int i = 0; i < caras.length; i++) {
            int[] cara = caras[i];
            int[] centro = {0, 0, 0};  // Puedes usar el centroide u otro punto representativo de la cara
    
            // Calcular la distancia euclidiana
            distancias[i] = Math.sqrt(Math.pow(centro[0] - xCabeza, 2) + Math.pow(centro[1] - yCabeza, 2) + Math.pow(centro[2] - zCabeza, 2));
        }
    
        // Ordenar las caras por distancia (de cercano a lejano)
        int[] ordenCaras = IntStream.range(0, caras.length)
                .boxed()
                .sorted(Comparator.comparingDouble(i -> -distancias[i]))
                .mapToInt(ele -> ele)
                .toArray();
    
        // Dibujar las caras en el orden correcto
        for (int i = 0; i < ordenCaras.length; i++) {
            int[] cara = caras[ordenCaras[i]];
            int[] puntosX = new int[cara.length];
            int[] puntosY = new int[cara.length];
    
            for (int j = 0; j < cara.length; j++) {
                int x = (vertices[cara[j]][0] * puntoDePerspectiva) / (vertices[cara[j]][2] + puntoDePerspectiva)
                        + getWidth() / 2;
                int y = (vertices[cara[j]][1] * puntoDePerspectiva) / (vertices[cara[j]][2] + puntoDePerspectiva)
                        + getHeight() / 2;
                puntosX[j] = x;
                puntosY[j] = y;
    
                // Dibujar bordes solo cuando hay al menos dos puntos
                if (j > 0) {
                    drawLineBresenham(puntosX[j - 1], puntosY[j - 1], puntosX[j], puntosY[j], fill);
                }
            }
        }
    
        // Repintar el JFrame para mostrar el cubo
        repaint();
    }
    
    // Función para dibujar el cubo y la pirámide
    public void drawPoligon3D() {
        // Crear la matriz de rotación
        double[][] matrizRotacion = {
                {Math.cos(anguloY * directionY) * Math.cos(anguloZ * directionZ), -Math.cos(anguloY * directionY) * Math.sin(anguloZ * directionZ), Math.sin(anguloY * directionY)},
                {Math.cos(anguloX * directionX) * Math.sin(anguloZ * directionZ) + Math.sin(anguloX * directionX) * Math.sin(anguloY * directionY) * Math.cos(anguloZ * directionZ), Math.cos(anguloX * directionX) * Math.cos(anguloZ * directionZ) - Math.sin(anguloX * directionX) * Math.sin(anguloY * directionY) * Math.sin(anguloZ * directionZ), -Math.sin(anguloX * directionX) * Math.cos(anguloY * directionY)},
                {Math.sin(anguloX * directionX) * Math.sin(anguloZ * directionZ) - Math.cos(anguloX * directionX) * Math.sin(anguloY * directionY) * Math.cos(anguloZ * directionZ), Math.sin(anguloX * directionX) * Math.cos(anguloZ * directionZ) + Math.cos(anguloX * directionX) * Math.sin(anguloY * directionY) * Math.sin(anguloZ * directionZ), Math.cos(anguloX * directionX) * Math.cos(anguloY * directionY)}
        };

        int[][] verticesPata1Rotados = aplicarTransformacion(verticesPata1, matrizRotacion);        // Aplicar la rotación a los vértices de la pata numero 1
        int[][] verticesPata2Rotados = aplicarTransformacion(verticesPata2, matrizRotacion);        // Aplicar la rotación a los vértices de la pata numero 2
        int[][] verticesPata3Rotados = aplicarTransformacion(verticesPata3, matrizRotacion);        // Aplicar la rotación a los vértices de la pata numero 3

        int[][] verticesPata4Rotados = aplicarTransformacion(verticesPata4, matrizRotacion);        // Aplicar la rotación a los vértices de la pata numero 4
        int[][] verticesPata5Rotados = aplicarTransformacion(verticesPata5, matrizRotacion);        // Aplicar la rotación a los vértices de la pata numero 5
        int[][] verticesPata6Rotados = aplicarTransformacion(verticesPata6, matrizRotacion);        // Aplicar la rotación a los vértices de la pata numero 6

        int[][] verticesPata7Rotados = aplicarTransformacion(verticesPata7, matrizRotacion);        // Aplicar la rotación a los vértices de la pata numero 7
        int[][] verticesPata8Rotados = aplicarTransformacion(verticesPata8, matrizRotacion);        // Aplicar la rotación a los vértices de la pata numero 8
        int[][] verticesPata9Rotados = aplicarTransformacion(verticesPata9, matrizRotacion);        // Aplicar la rotación a los vértices de la pata numero 9

        int[][] verticesCabezaRotados = aplicarTransformacion(verticesCabeza, matrizRotacion);      // Aplicar la rotación a los vértices de la cabesa

        int[][] verticesOjo1Rotados = aplicarTransformacion(verticesOjo1, matrizRotacion);          // Aplicar la rotación a los vértices del ojo numero 1
        int[][] verticesOjo2Rotados = aplicarTransformacion(verticesOjo2, matrizRotacion);          // Aplicar la rotación a los vértices del ojo numero 2

        int[][] verticesBocaRotados = aplicarTransformacion(verticesBoca, matrizRotacion);          // Aplicar la rotación a los vértices de la boca

        clearMyScreen();

        if(rellenarPoligono) {
            fillPoligon3D(caras, verticesPata1Rotados, fillBody);
            fillPoligon3D(caras, verticesPata2Rotados, fillBody);
            fillPoligon3D(caras, verticesPata3Rotados, fillBody);

            fillPoligon3D(caras, verticesPata4Rotados, fillBody);
            fillPoligon3D(caras, verticesPata5Rotados, fillBody);
            fillPoligon3D(caras, verticesPata6Rotados, fillBody);

            fillPoligon3D(caras, verticesPata7Rotados, fillBody);
            fillPoligon3D(caras, verticesPata8Rotados, fillBody);
            fillPoligon3D(caras, verticesPata9Rotados, fillBody);

            fillPoligon3D(caras, verticesCabezaRotados, fillBody);

            fillPoligon3D(caras, verticesOjo1Rotados, fillFace);
            fillPoligon3D(caras, verticesOjo2Rotados, fillFace);

            fillPoligon3D(caras, verticesBocaRotados, fillFace);


            dibujarBordes(caras, verticesPata1Rotados, Color.BLACK);           // Dibujar las líneas de la pata 1
            dibujarBordes(caras, verticesPata2Rotados, Color.BLACK);           // Dibujar las líneas de la pata 2
            dibujarBordes(caras, verticesPata3Rotados, Color.BLACK);           // Dibujar las líneas de la pata 3

            dibujarBordes(caras, verticesPata4Rotados, Color.BLACK);           // Dibujar las líneas de la pata 4
            dibujarBordes(caras, verticesPata5Rotados, Color.BLACK);           // Dibujar las líneas de la pata 5
            dibujarBordes(caras, verticesPata6Rotados, Color.BLACK);           // Dibujar las líneas de la pata 6

            dibujarBordes(caras, verticesPata7Rotados, Color.BLACK);           // Dibujar las líneas de la pata 7
            dibujarBordes(caras, verticesPata8Rotados, Color.BLACK);           // Dibujar las líneas de la pata 8
            dibujarBordes(caras, verticesPata9Rotados, Color.BLACK);           // Dibujar las líneas de la pata 9

            dibujarBordes(caras, verticesCabezaRotados, Color.BLACK);          // Dibujar las líneas de la cabeza

            dibujarBordes(caras, verticesOjo1Rotados, Color.BLACK);            // Dibujar las líneas del ojo 1
            dibujarBordes(caras, verticesOjo2Rotados, Color.BLACK);            // Dibujar las líneas del ojo 1

            dibujarBordes(caras, verticesBocaRotados, Color.BLACK);            // Dibujar las líneas de la boca
        }


        // Dibujar los vértices
        else if(mostrarVertices) {
            dibujarVertices(verticesPata1Rotados, puntoDePerspectiva);
            dibujarVertices(verticesPata2Rotados, puntoDePerspectiva);
            dibujarVertices(verticesPata3Rotados, puntoDePerspectiva);

            dibujarVertices(verticesPata4Rotados, puntoDePerspectiva);
            dibujarVertices(verticesPata5Rotados, puntoDePerspectiva);
            dibujarVertices(verticesPata6Rotados, puntoDePerspectiva);

            dibujarVertices(verticesPata7Rotados, puntoDePerspectiva);
            dibujarVertices(verticesPata8Rotados, puntoDePerspectiva);
            dibujarVertices(verticesPata9Rotados, puntoDePerspectiva);

            dibujarVertices(verticesCabezaRotados, puntoDePerspectiva);

            dibujarVertices(verticesOjo1Rotados, puntoDePerspectiva);
            dibujarVertices(verticesOjo2Rotados, puntoDePerspectiva);

            dibujarVertices(verticesBocaRotados, puntoDePerspectiva);
        }
        
        else if(mostrarVerticesUnidos) {
            dibujarLineas(verticesPata1Rotados, fillBody);           // Dibujar las líneas de la pata 1
            dibujarLineas(verticesPata2Rotados, fillBody);           // Dibujar las líneas de la pata 2
            dibujarLineas(verticesPata3Rotados, fillBody);           // Dibujar las líneas de la pata 3

            dibujarLineas(verticesPata4Rotados, fillBody);           // Dibujar las líneas de la pata 4
            dibujarLineas(verticesPata5Rotados, fillBody);           // Dibujar las líneas de la pata 5
            dibujarLineas(verticesPata6Rotados, fillBody);           // Dibujar las líneas de la pata 6

            dibujarLineas(verticesPata7Rotados, fillBody);           // Dibujar las líneas de la pata 7
            dibujarLineas(verticesPata8Rotados, fillBody);           // Dibujar las líneas de la pata 8
            dibujarLineas(verticesPata9Rotados, fillBody);           // Dibujar las líneas de la pata 9

            dibujarLineas(verticesCabezaRotados, fillBody);          // Dibujar las líneas de la cabeza

            dibujarLineas(verticesOjo1Rotados, fillFace);            // Dibujar las líneas del ojo 1
            dibujarLineas(verticesOjo2Rotados, fillFace);            // Dibujar las líneas del ojo 1

            dibujarLineas(verticesBocaRotados, fillFace);            // Dibujar las líneas de la boca
        }

        // Repintar el JFrame para mostrar el cubo y la pirámide
        repaint();
    }

    public void fillPoligon3D(int[][] caras, int[][] vertices, Color fill) {
        // Crear un array para almacenar las distancias de las caras al espectador
        double[] distancias = new double[caras.length];

        // Calcular la distancia al espectador para cada cara
        for (int i = 0; i < caras.length; i++) {
            int[] cara = caras[i];
            int[] centro = {0, 0, 0};  // Puedes usar el centroide u otro punto representativo de la cara

            // Calcular la distancia euclidiana
            distancias[i] = Math.sqrt(Math.pow(centro[0] - xCabeza, 2) + Math.pow(centro[1] - yCabeza, 2) + Math.pow(centro[2] - zCabeza, 2));
        }

        // Ordenar las caras por distancia (de cercano a lejano)
        int[] ordenCaras = IntStream.range(0, caras.length)
                .boxed()
                .sorted(Comparator.comparingDouble(i -> -distancias[i]))
                .mapToInt(ele -> ele)
                .toArray();

        // Dibujar las caras en el orden correcto
        for (int i = 0; i < ordenCaras.length; i++) {
            int[] cara = caras[ordenCaras[i]];
            int[] puntosX = new int[cara.length];
            int[] puntosY = new int[cara.length];

            for (int j = 0; j < cara.length; j++) {
                int x = (vertices[cara[j]][0] * puntoDePerspectiva) / (vertices[cara[j]][2] + puntoDePerspectiva)
                        + getWidth() / 2;
                int y = (vertices[cara[j]][1] * puntoDePerspectiva) / (vertices[cara[j]][2] + puntoDePerspectiva)
                        + getHeight() / 2;
                puntosX[j] = x;
                puntosY[j] = y;
            }

            // Rellenar la cara con un color
            fillPolygonScanLine(puntosX, puntosY, fill);
        }

        // Repintar el JFrame para mostrar el cubo
        repaint();
    }


    public void fillPolygonScanLine(int[] xPoints, int[] yPoints, Color c) {
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;

        // Encontrar el rango horizontal del polígono
        for (int x : xPoints) {
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
        }

        List<Integer> intersections = new ArrayList<>();

        // Escanear cada línea vertical dentro del rango horizontal
        for (int x = minX; x <= maxX; x++) {
            intersections.clear();

            for (int i = 0; i < xPoints.length; i++) {
                int x1 = xPoints[i];
                int y1 = yPoints[i];
                int x2 = xPoints[(i + 1) % xPoints.length];
                int y2 = yPoints[(i + 1) % xPoints.length];

                if ((x1 <= x && x2 > x) || (x2 <= x && x1 > x)) {
                    // Calcula la intersección vertical con la línea
                    double y = y1 + (double) (x - x1) * (y2 - y1) / (x2 - x1);
                    intersections.add((int) y);
                }
            }

            // Ordena las intersecciones de arriba a abajo
            intersections.sort(Integer::compareTo);

            // Rellena el espacio entre las intersecciones
            for (int i = 0; i < intersections.size(); i += 2) {
                int startY = intersections.get(i);
                int endY = intersections.get(i + 1);
                for (int y = startY; y < endY; y++) {
                    putPixel(x, y, c);
                }

                repaint();
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(buffer, 0, 0, this);
    }

    // Métodos de la interfaz KeyListener
    @Override
    public void keyTyped(KeyEvent e) {
        // No necesitamos implementar este método en este caso
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Capturar teclas presionadas y ajustar los estados de rotación y traslación
        if (e.getKeyChar() == 'x') {
            rotarX = true;
            directionX = 1;
            System.out.println("DIreccion angulo X: " + directionX);
        } else if (e.getKeyChar() == 'y') {
            rotarY = true;
            directionY = 1;
            System.out.println("DIreccion angulo Y: " + directionY);
        } else if (e.getKeyChar() == 'z') {
            rotarZ = true;
            directionZ = 1;
            System.out.println("DIreccion angulo Z: " + directionZ);
        } else if (e.getKeyChar() == 'i') {
            rotarX = true;
            directionX = -1;
            System.out.println("DIreccion angulo X: " + directionX);
        } else if (e.getKeyChar() == 'o') {
            rotarY = true;
            directionY = -1;
            System.out.println("DIreccion angulo Y: " + directionY);
        } else if (e.getKeyChar() == 'p') {
            rotarZ = true;
            directionZ = -1;
            System.out.println("DIreccion angulo Z: " + directionZ);
        } else if (e.getKeyChar() == 'q') {
            mostrarVerticesUnidos = true;
            mostrarVertices = false;
        } else if (e.getKeyChar() == 'w') {
            rellenarPoligono = true;
            mostrarVerticesUnidos = false;
        } else if (e.getKeyChar() == 'e') {
            rellenarPoligono = false;
            mostrarVerticesUnidos = false;
            mostrarVertices = true;
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            xCabeza -= 10; // Ajusta la velocidad de traslación según sea necesario
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            xCabeza += 10;
        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
            zCabeza -= 10;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            zCabeza += 10;
        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            // Restaurar el cubo y la pirámide a su posición estática
            restartValues();
        }
    }

    public void restartValues() {
            anguloX = 0;
            anguloY = 0;
            anguloZ = 0;
            xCabeza = 0;
            yCabeza = 0;
            zCabeza = 0;
            rotarX = false;
            rotarY = false;
            rotarZ = false;
            drawPoligon3D();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // No necesitamos implementar este método en este caso
    }

    public void pause(int milis) {
        try {
            Thread.sleep(milis);

        } catch (Exception e) {
            System.out.println("Error en la funcion pause: " + e.getMessage());
        }
    }

    public void tutututuTUN() {
        pause(50);
        mostrarVertices = false;
        rellenarPoligono = false;
        mostrarVerticesUnidos = true;

        pause(50);
        mostrarVerticesUnidos = false;
        rellenarPoligono = true;

        pause(50);
        rellenarPoligono = false;
        mostrarVertices = true;

        pause(50);
        mostrarVertices = false;
        rellenarPoligono = false;
        mostrarVerticesUnidos = true;

        pause(50);
        mostrarVerticesUnidos = false;
        rellenarPoligono = true;

        pause(50);
        rellenarPoligono = false;
        mostrarVertices = true;

        pause(50);
        mostrarVertices = false;
        rellenarPoligono = false;
        mostrarVerticesUnidos = true;

        pause(50);
        mostrarVerticesUnidos = false;
        rellenarPoligono = true;

        pause(50);
        rellenarPoligono = false;
        mostrarVertices = true;

        pause(50);
        mostrarVertices = false;
        rellenarPoligono = false;
        mostrarVerticesUnidos = true;

        pause(50);
        mostrarVerticesUnidos = false;
        rellenarPoligono = true;

        pause(50);
        rellenarPoligono = false;
        mostrarVertices = true;

        pause(50);
        mostrarVertices = false;
        mostrarVerticesUnidos = false;
        rellenarPoligono = true;
    }

    public void TU_TU_TU_TU(int ciclos) {
        for(int i = 0; i < ciclos; i++) {
            pause(698);
            mostrarVertices = false;
            rellenarPoligono = false;
            mostrarVerticesUnidos = true;

            pause(698);
            mostrarVertices = true;
            rellenarPoligono = false;
            mostrarVerticesUnidos = false;

            pause(698);
            mostrarVertices = false;
            rellenarPoligono = true;
            mostrarVerticesUnidos = false;
        }
    }

    public static void main(String[] args) throws Exception {

        Vector vector = new Vector();
        vector.backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);

        vector.setVisible(true);
        
        int contador = 0;
        vector.yCabeza --;
        while (contador < 21) {
            
            vector.pause(990);

            if(contador == 20) {
                vector.tutututuTUN();
                vector.tutututuTUN();
            }

            contador++;
        }

        vector.restartValues();
        vector.pause(1500);

        vector.rotarY = true;
        vector.directionY = -1;

        vector.fillBody = Color.PINK;
        vector.fillFace = Color.MAGENTA;

        vector.pause(4700);
        vector.tutututuTUN();

        vector.fillBody = Color.decode("#97FF00");
        vector.fillFace = Color.YELLOW;

        vector.pause(4700);
        vector.tutututuTUN();

        vector.fillBody = Color.decode("#003EFF");
        vector.fillFace = Color.CYAN;

        vector.pause(4700);
        vector.tutututuTUN();

        vector.fillBody = Color.decode("#F5FAFC");
        vector.fillFace = Color.decode("#575F6E");

        vector.pause(4700);
        vector.tutututuTUN();
        vector.restartValues();

        contador = 0;
        vector.yCabeza ++;
        while (contador < 4) {
            
            vector.pause(100);
            contador++;
        }

        vector.restartValues();

        vector.rotarX = true;
        vector.directionX = 1;

        vector.TU_TU_TU_TU(12);

        vector.restartValues();

        vector.rotarX = true;
        vector.rotarY = true;
        vector.rotarZ = true;
        vector.directionX = -1;
        vector.directionY = 1;
        vector.directionZ = -1;

        vector.pause(30000);
        vector.restartValues();


        vector.rotarY = true;
        vector.directionY = 1;

        vector.pause(8500);

        vector.rotarX = false;
        vector.rotarY = false;
        vector.rotarZ = false;
        vector.drawPoligon3D();

        vector.zCabeza -= 1;
        vector.pause(1000);
        vector.TU_TU_TU_TU(6);
        vector.pause(100);
        vector.TU_TU_TU_TU(6);
        vector.tutututuTUN();
    }
}
