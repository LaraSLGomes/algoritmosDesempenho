# Análise de Desempenho de Algoritmos de Ordenação em Ambientes Concorrentes

Este projeto tem o objetivo principal de analisar e comparar o desempenho de diferentes algoritmos de ordenação executados de forma **serial** e **paralela** em Java.

## 🚀 Funcionalidades

- **Interface Gráfica (Swing):** Orquestração amigável dos testes e visualização do progresso.
- **Benchmark Automatizado:** Executa testes variando o tamanho do conjunto de dados (10.000 a 200.000 elementos) e a quantidade de *threads* (2, 4 e 8 núcleos) utilizando o `ForkJoinPool`.
- **Algoritmos Implementados:** Merge Sort, Quick Sort, Counting Sort e Bubble Sort (Paralelizado via *Odd-Even Transposition*).
- **Exportação de Dados:** Gera automaticamente um arquivo `resultados.csv` contendo as médias de tempo das execuções (5 amostras por configuração).
- **Gráfico Dinâmico:** Plota nativamente no *JFrame* uma representação visual básica contrastando o tempo de execução no eixo Y e o tamanho do vetor no eixo X.

## 📋 Pré-requisitos

Para compilar e executar o projeto, você precisa ter o **Java Development Kit (JDK) 17** (ou superior) instalado na sua máquina.

## 🔧 Como Compilar e Executar

1. Clone este repositório para a sua máquina local:
```bash
git clone [https://github.com/LaraSLGomes/algoritmosDesempenho.git](https://github.com/LaraSLGomes/algoritmosDesempenho.git)
```

2. Navegue até o diretório onde os arquivos `.java` estão guardados:
```bash
cd caminho/para/a/pasta
```

3. Compile os arquivos fonte:
```bash
javac AnaliseDesempenhoApp.java Algoritmos.java
```

4. Execute a aplicação principal:
```bash
java AnaliseDesempenhoApp
```

5. Na interface que será aberta, clique em **"Executar Benchmark e Gerar Gráfico"**. Aguarde a finalização dos testes (o log indicará o progresso). No final, o gráfico dinâmico será desenhado e o arquivo `resultados.csv` será gerado na raiz da pasta.

## 📁 Estrutura dos Arquivos

- `AnaliseDesempenhoApp.java`: Contém a lógica da interface gráfica (Swing), o motor de repetição do benchmark, a medição de tempo e a renderização do gráfico dinâmico.
- `Algoritmos.java`: Contém as implementações puras (seriais e paralelas via `ForkJoinTask`) do Merge Sort, Quick Sort, Bubble Sort e Counting Sort.
- `resultados.csv`: Arquivo gerado dinamicamente contendo os dados brutos coletados.

## 👥 Autores

- **Lara Stephanny Lima Gomes**
- **Ana Karolina Silva Ferreira**