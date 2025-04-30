import java.io.FileWriter;
import java.io.IOException;

public class TesteSimulador {
    public static void main(String[] args) {
        // Gera o arquivo de configuração
        gerarArquivoConfig("config-tandem.properties");
        
        // Executa o simulador
        SimuladorRedeFilas simulador = new SimuladorRedeFilas(
            "config-tandem.properties", // arquivo de configuração
            100000,                     // limite de números aleatórios
            42                          // seed
        );
        
        System.out.println("Iniciando simulação...");
        simulador.simular();
        simulador.imprimirResultados();
        System.out.println("\nSimulação concluída!");
    }
    
    private static void gerarArquivoConfig(String arquivo) {
        String conteudo = 
            "# Configuração para simulação de rede de filas em tandem\n" +
            "# Fila 1 (G/G/2/3) -> Fila 2 (G/G/1/5)\n\n" +
            "# Tempo inicial da simulação (primeiro cliente chega no tempo 2.0)\n" +
            "tempoInicial=2.0\n" +
            "# Fila onde o primeiro cliente chega\n" +
            "filaInicial=1\n\n" +
            "# Definição das filas\n" +
            "# Fila 1\n" +
            "fila.1.capacidade=3\n" +
            "fila.1.servidores=2\n" +
            "fila.1.minChegada=1.0\n" +
            "fila.1.maxChegada=4.0\n" +
            "fila.1.minAtendimento=3.0\n" +
            "fila.1.maxAtendimento=4.0\n\n" +
            "# Fila 2\n" +
            "fila.2.capacidade=5\n" +
            "fila.2.servidores=1\n" +
            "# Não tem chegadas externas - só recebe da fila 1\n" +
            "fila.2.minChegada=0.0\n" +
            "fila.2.maxChegada=0.0\n" +
            "fila.2.minAtendimento=2.0\n" +
            "fila.2.maxAtendimento=3.0\n\n" +
            "# Rotas entre as filas\n" +
            "# Clientes da Fila 1 sempre vão para a Fila 2\n" +
            "rota.1.origem=1\n" +
            "rota.1.destino=2\n" +
            "rota.1.probabilidade=1.0\n\n" +
            "# Clientes da Fila 2 saem do sistema (destino 0)\n" +
            "rota.2.origem=2\n" +
            "rota.2.destino=0\n" +
            "rota.2.probabilidade=1.0";
        
        try (FileWriter writer = new FileWriter(arquivo)) {
            writer.write(conteudo);
            System.out.println("Arquivo de configuração gerado: " + arquivo);
        } catch (IOException e) {
            System.err.println("Erro ao gerar arquivo de configuração: " + e.getMessage());
        }
    }
}