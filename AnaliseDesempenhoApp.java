import javax.swing.*;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;

public class AnaliseDesempenhoApp extends JFrame {

    private JTextArea logArea;
    private JButton btnExecutar;
    private JPanel painelGrafico;
    private StringBuilder csvDados;

    private final int[] TAMANHOS = {10000, 50000, 100000, 200000};
    private final int[] THREADS_PARALELO = {2, 4, 8};
    private final int AMOSTRAS = 5;
    
    private List<ResultadoGrafico> resultadosGrafico = new ArrayList<>();

    public AnaliseDesempenhoApp() {
        setTitle("Análise de Desempenho - Algoritmos de Ordenação");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollLog = new JScrollPane(logArea);
        scrollLog.setPreferredSize(new Dimension(400, 600));

        btnExecutar = new JButton("Executar Benchmark e Gerar Gráfico");
        btnExecutar.setFont(new Font("Arial", Font.BOLD, 14));
        btnExecutar.addActionListener(e -> executarTestes());

        painelGrafico = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                desenharGraficoDinamico(g);
            }
        };
        painelGrafico.setBackground(Color.WHITE);

        add(btnExecutar, BorderLayout.NORTH);
        add(painelGrafico, BorderLayout.CENTER);
        add(scrollLog, BorderLayout.EAST);
    }

    private void executarTestes() {
        btnExecutar.setEnabled(false);
        resultadosGrafico.clear();
        logArea.setText("Iniciando benchmark...\npode demorar alguns minutos.\n\n");
        csvDados = new StringBuilder("Algoritmo,Tipo,Tamanho,Threads,Amostra,Tempo_ms\n");

        Thread t = new Thread(() -> {
            Random rand = new Random();

            for (int tamanho : TAMANHOS) {
                logArea.append(">> A testar tamanho: " + tamanho + "\n");
                int[] arrayBase = new int[tamanho];
                for (int i = 0; i < tamanho; i++) {
                    arrayBase[i] = rand.nextInt(100000);
                }

                testarAlgoritmo("Merge Sort", arrayBase, tamanho);
                testarAlgoritmo("Quick Sort", arrayBase, tamanho);
                testarAlgoritmo("Counting Sort", arrayBase, tamanho);
                
                if (tamanho <= 50000) { 
                    testarAlgoritmo("Bubble Sort", arrayBase, tamanho);
                }
            }

            guardarCSV();
            logArea.append("\nConcluído! Ficheiro 'resultados.csv' gerado.\n");
            
            SwingUtilities.invokeLater(() -> {
                btnExecutar.setEnabled(true);
                painelGrafico.repaint(); 
            });
        });
        t.start();
    }

    private void testarAlgoritmo(String nome, int[] arrayBase, int tamanho) {
        long totalTempoSerial = 0;
        for (int amostra = 1; amostra <= AMOSTRAS; amostra++) {
            int[] arr = Arrays.copyOf(arrayBase, arrayBase.length);
            long tempo = medirTempo(() -> executarSerial(nome, arr));
            registarCSV(nome, "Serial", tamanho, 1, amostra, tempo);
            totalTempoSerial += tempo;
        }
        resultadosGrafico.add(new ResultadoGrafico(nome + " (Serial)", tamanho, totalTempoSerial / AMOSTRAS));
        logArea.append(nome + " Serial (" + tamanho + ") - OK\n");

        for (int numThreads : THREADS_PARALELO) {
            long totalTempoParalelo = 0;
            for (int amostra = 1; amostra <= AMOSTRAS; amostra++) {
                int[] arr = Arrays.copyOf(arrayBase, arrayBase.length);
                long tempo = medirTempo(() -> executarParalelo(nome, arr, numThreads));
                registarCSV(nome, "Paralelo", tamanho, numThreads, amostra, tempo);
                totalTempoParalelo += tempo;
            }
            resultadosGrafico.add(new ResultadoGrafico(nome + " (Paralelo " + numThreads + "T)", tamanho, totalTempoParalelo / AMOSTRAS));
            logArea.append(nome + " Paralelo " + numThreads + "T (" + tamanho + ") - OK\n");
        }
    }

    private long medirTempo(Runnable rotina) {
        long inicio = System.nanoTime();
        rotina.run();
        return (System.nanoTime() - inicio) / 1_000_000;
    }

    private void registarCSV(String algoritmo, String tipo, int tamanho, int threads, int amostra, long tempoMs) {
        csvDados.append(String.format("%s,%s,%d,%d,%d,%d\n", algoritmo, tipo, tamanho, threads, amostra, tempoMs));
    }

    private void guardarCSV() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("resultados.csv"))) {
            writer.print(csvDados.toString());
        } catch (IOException e) {
            logArea.append("Erro ao guardar CSV: " + e.getMessage() + "\n");
        }
    }

    private void executarSerial(String algoritmo, int[] arr) {
        switch (algoritmo) {
            case "Merge Sort" -> MergeSort.sortSerial(arr, 0, arr.length - 1);
            case "Quick Sort" -> QuickSort.sortSerial(arr, 0, arr.length - 1);
            case "Counting Sort" -> CountingSort.sortSerial(arr);
            case "Bubble Sort" -> BubbleSort.sortSerial(arr);
        }
    }

    private void executarParalelo(String algoritmo, int[] arr, int numThreads) {
        ForkJoinPool customPool = new ForkJoinPool(numThreads); 
        try {
            switch (algoritmo) {
                case "Merge Sort" -> customPool.invoke(new MergeSort.MergeSortTask(arr, 0, arr.length - 1));
                case "Quick Sort" -> customPool.invoke(new QuickSort.QuickSortTask(arr, 0, arr.length - 1));
                case "Counting Sort" -> customPool.invoke(new CountingSort.CountingSortTask(arr));
                case "Bubble Sort" -> BubbleSort.sortParallelOddEven(arr, customPool);
            }
        } finally {
            customPool.shutdown();
        }
    }

    private void desenharGraficoDinamico(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int largura = painelGrafico.getWidth();
        int altura = painelGrafico.getHeight();
        int margem = 60;
        
        g2d.drawLine(margem, altura - margem, largura - margem, altura - margem); 
        g2d.drawLine(margem, altura - margem, margem, margem); 
        g2d.drawString("Tempo (ms)", margem - 50, margem - 10);
        g2d.drawString("Tamanho do Array", largura / 2 - 50, altura - 20);

        if (resultadosGrafico.isEmpty()) {
            g2d.drawString("Execute o benchmark para visualizar o gráfico dinâmico.", largura / 2 - 150, altura / 2);
            return;
        }

        long maxTempo = resultadosGrafico.stream().mapToLong(r -> r.tempoMedio).max().orElse(1);
        int maxTamanho = Arrays.stream(TAMANHOS).max().orElse(1);

        g2d.setColor(Color.BLUE);
        desenharLinhaAlgoritmo(g2d, "Merge Sort (Serial)", maxTempo, maxTamanho, largura, altura, margem);
        
        g2d.setColor(Color.RED);
        desenharLinhaAlgoritmo(g2d, "Merge Sort (Paralelo 8T)", maxTempo, maxTamanho, largura, altura, margem);

        g2d.setColor(Color.BLUE);
        g2d.fillRect(largura - 200, 50, 10, 10);
        g2d.drawString("Merge Sort (Serial)", largura - 180, 60);
        g2d.setColor(Color.RED);
        g2d.fillRect(largura - 200, 70, 10, 10);
        g2d.drawString("Merge Sort (Paralelo 8T)", largura - 180, 80);
    }

    private void desenharLinhaAlgoritmo(Graphics2D g2d, String filtro, long maxTempo, int maxTamanho, int largura, int altura, int margem) {
        int eixoXReal = largura - 2 * margem;
        int eixoYReal = altura - 2 * margem;

        int ultimoX = -1, ultimoY = -1;
        
        for (ResultadoGrafico r : resultadosGrafico) {
            if (r.nomeAlgoritmo.equals(filtro)) {
                int x = margem + (int) ((r.tamanho / (double) maxTamanho) * eixoXReal);
                int y = (altura - margem) - (int) ((r.tempoMedio / (double) maxTempo) * eixoYReal);
                
                g2d.fillOval(x - 4, y - 4, 8, 8);
                if (ultimoX != -1) {
                    g2d.drawLine(ultimoX, ultimoY, x, y);
                }
                ultimoX = x;
                ultimoY = y;
            }
        }
    }

    class ResultadoGrafico {
        String nomeAlgoritmo;
        int tamanho;
        long tempoMedio;
        ResultadoGrafico(String n, int tam, long tempo) { this.nomeAlgoritmo = n; this.tamanho = tam; this.tempoMedio = tempo; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AnaliseDesempenhoApp().setVisible(true));
    }
}