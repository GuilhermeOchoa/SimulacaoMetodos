import java.io.FileWriter;
import java.io.IOException;

public class TesteRedeExemplo {
    public static void main(String[] args) {
        // Gera o arquivo de configuração
        gerarArquivoConfig("config-rede-exemplo.properties");
        
        // Executa o simulador
        SimuladorRedeFilas simulador = new SimuladorRedeFilas(
            "config-rede-exemplo.properties", // arquivo de configuração
            100000,                           // limite de números aleatórios
            42                                // seed
        );
        
        System.out.println("Iniciando simulação...");
        simulador.simular();
        
        // Imprime os resultados no formato solicitado
        imprimirResultadosFormatados(simulador);
        
        System.out.println("\nSimulação concluída!");
    }
    
    private static void imprimirResultadosFormatados(SimuladorRedeFilas simulador) {
        System.out.println("\n1. Resultado da Fila 1: G/G/1, chegadas entre 2..4, atendimento entre 1..2:");
        simulador.imprimirResultadoFila(1);
        
        System.out.println("\n2. Resultado da Fila 2: G/G/2/5, atendimento entre 4..8:");
        simulador.imprimirResultadoFila(2);
        
        System.out.println("\n3. Resultado da Fila 3: G/G/2/10, atendimento entre 5..15:");
        simulador.imprimirResultadoFila(3);
        
        System.out.println("\n4. Tempo total de simulação: " + 
                           String.format("%.2f", simulador.getTempoGlobal()));
    }
    
    private static void gerarArquivoConfig(String arquivo) {
        // Usando "\\" nos comentários para evitar problemas com escape de caracteres
        String conteudo = 
            "# Configuração para simulação da rede de filas do exemplo\n" +
            "# Topologia:\n" +
            "#\n" +
            "#                  /---------> Fila 2 ---0,2--> Saída\n" +
            "#                 /    0,8     G/G/2/5     \\\n" +
            "#                /              4..8min     0,5\n" +
            "# Entrada --> Fila 1                         \\\n" +
            "#     2..4min   G/G/1    0,2                 \\\n" +
            "#               1..2min   \\                   \\\n" +
            "#                          \\---> Fila 3 ---0,3--> Saída\n" +
            "#                                G/G/2/10    0,7\n" +
            "#                                5..15min     |\n" +
            "#                                             |\n" +
            "#                                             v\n" +
            "#                                          Fila 1\n" +
            "\n" +
            "# Tempo inicial da simulação\n" +
            "tempoInicial=2.0\n" +
            "# Fila onde o primeiro cliente chega\n" +
            "filaInicial=1\n" +
            "\n" +
            "# Definição das filas\n" +
            "# Fila 1 - Entrada do sistema (G/G/1)\n" +
            "fila.1.capacidade=1000\n" +
            "fila.1.servidores=1\n" +
            "fila.1.minChegada=2.0\n" +
            "fila.1.maxChegada=4.0\n" +
            "fila.1.minAtendimento=1.0\n" +
            "fila.1.maxAtendimento=2.0\n" +
            "\n" +
            "# Fila 2 - (G/G/2/5)\n" +
            "fila.2.capacidade=5\n" +
            "fila.2.servidores=2\n" +
            "fila.2.minChegada=0.0\n" +
            "fila.2.maxChegada=0.0\n" +
            "fila.2.minAtendimento=4.0\n" +
            "fila.2.maxAtendimento=8.0\n" +
            "\n" +
            "# Fila 3 - (G/G/2/10)\n" +
            "fila.3.capacidade=10\n" +
            "fila.3.servidores=2\n" +
            "fila.3.minChegada=0.0\n" +
            "fila.3.maxChegada=0.0\n" +
            "fila.3.minAtendimento=5.0\n" +
            "fila.3.maxAtendimento=15.0\n" +
            "\n" +
            "# Rotas entre as filas\n" +
            "# Da Fila 1 para Fila 2 (80% de probabilidade)\n" +
            "rota.1.origem=1\n" +
            "rota.1.destino=2\n" +
            "rota.1.probabilidade=0.8\n" +
            "\n" +
            "# Da Fila 1 para Fila 3 (20% de probabilidade)\n" +
            "rota.2.origem=1\n" +
            "rota.2.destino=3\n" +
            "rota.2.probabilidade=0.2\n" +
            "\n" +
            "# Da Fila 2 para Saída (20% de probabilidade)\n" +
            "rota.3.origem=2\n" +
            "rota.3.destino=0\n" +
            "rota.3.probabilidade=0.2\n" +
            "\n" +
            "# Da Fila 2 para Fila 1 (30% de probabilidade)\n" +
            "rota.4.origem=2\n" +
            "rota.4.destino=1\n" +
            "rota.4.probabilidade=0.3\n" +
            "\n" +
            "# Da Fila 2 para Fila 3 (50% de probabilidade)\n" +
            "rota.5.origem=2\n" +
            "rota.5.destino=3\n" +
            "rota.5.probabilidade=0.5\n" +
            "\n" +
            "# Da Fila 3 para Saída (30% de probabilidade)\n" +
            "rota.6.origem=3\n" +
            "rota.6.destino=0\n" +
            "rota.6.probabilidade=0.3\n" +
            "\n" +
            "# Da Fila 3 para Fila 1 (70% de probabilidade)\n" +
            "rota.7.origem=3\n" +
            "rota.7.destino=1\n" +
            "rota.7.probabilidade=0.7";
        
        try (FileWriter writer = new FileWriter(arquivo)) {
            writer.write(conteudo);
            System.out.println("Arquivo de configuração gerado: " + arquivo);
        } catch (IOException e) {
            System.err.println("Erro ao gerar arquivo de configuração: " + e.getMessage());
        }
    }
}