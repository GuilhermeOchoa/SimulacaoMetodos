import java.io.*;
import java.util.*;

/**
 * Simulador de redes de filas que usa formato Properties para configuração
 */
public class SimuladorRedeFilasSimples {
    private final Map<Integer, Fila> filas;
    private final Map<Integer, List<Rota>> rotas;
    private final Escalonador escalonador;
    private final GeradorAleatorio gerador;
    private double tempoAnterior;
    private Map<Integer, Integer> clientesRoteados;
    private boolean debug = false;
    
    /**
     * Construtor do simulador
     * 
     * @param arquivoConfig Caminho para o arquivo de configuração
     * @param limiteAleatorios Limite de números aleatórios a serem gerados
     * @param seed Semente para o gerador de números aleatórios
     */
    public SimuladorRedeFilasSimples(String arquivoConfig, int limiteAleatorios, long seed) {
        this.filas = new HashMap<>();
        this.rotas = new HashMap<>();
        this.gerador = new GeradorAleatorio(seed);
        this.escalonador = new Escalonador(limiteAleatorios);
        this.tempoAnterior = 0;
        this.clientesRoteados = new HashMap<>();
        
        carregarConfiguracao(arquivoConfig);
    }
    
    /**
     * Carrega a configuração do arquivo Properties
     * 
     * @param arquivoConfig Caminho para o arquivo de configuração
     */
    private void carregarConfiguracao(String arquivoConfig) {
        Properties props = new Properties();
        
        try (FileInputStream fis = new FileInputStream(arquivoConfig)) {
            props.load(fis);
            
            // Carregar informações básicas
            double tempoInicial = Double.parseDouble(props.getProperty("tempoInicial", "0.0"));
            int filaInicial = Integer.parseInt(props.getProperty("filaInicial", "1"));
            
            // Carregar filas
            int numFilas = 0;
            while (props.containsKey("fila." + (numFilas + 1) + ".capacidade")) {
                numFilas++;
                
                int id = numFilas;
                int capacidade = Integer.parseInt(props.getProperty("fila." + id + ".capacidade"));
                int numServidores = Integer.parseInt(props.getProperty("fila." + id + ".servidores"));
                double minChegada = Double.parseDouble(props.getProperty("fila." + id + ".minChegada", "0.0"));
                double maxChegada = Double.parseDouble(props.getProperty("fila." + id + ".maxChegada", "0.0"));
                double minAtendimento = Double.parseDouble(props.getProperty("fila." + id + ".minAtendimento"));
                double maxAtendimento = Double.parseDouble(props.getProperty("fila." + id + ".maxAtendimento"));
                
                filas.put(id, new Fila(capacidade, numServidores, minChegada, maxChegada, minAtendimento, maxAtendimento));
                clientesRoteados.put(id, 0);
            }
            
            // Carregar rotas
            int numRotas = 0;
            while (props.containsKey("rota." + (numRotas + 1) + ".origem")) {
                numRotas++;
                
                int origem = Integer.parseInt(props.getProperty("rota." + numRotas + ".origem"));
                int destino = Integer.parseInt(props.getProperty("rota." + numRotas + ".destino"));
                double probabilidade = Double.parseDouble(props.getProperty("rota." + numRotas + ".probabilidade"));
                
                if (!rotas.containsKey(origem)) {
                    rotas.put(origem, new ArrayList<>());
                }
                rotas.get(origem).add(new Rota(destino, probabilidade));
            }
            
            // Configurar evento inicial
            escalonador.adicionarEvento(new Evento(tempoInicial, Evento.CHEGADA, -1, 0, filaInicial));
            
            // Imprimir informações sobre a configuração carregada
            System.out.println("Configuração carregada com sucesso:");
            System.out.println("- Número de filas: " + filas.size());
            System.out.println("- Número de rotas: " + contarRotas());
            
            for (int idFila : filas.keySet()) {
                Fila fila = filas.get(idFila);
                System.out.printf("- Fila %d: G/G/%d/%d | Atendimento: %.1f..%.1f\n", 
                        idFila, fila.Servers(), fila.Capacity(), 
                        fila.getMinAtendimento(), fila.getMaxAtendimento());
                
                if (fila.getMaxChegada() > 0) {
                    System.out.printf("  Chegadas externas: %.1f..%.1f\n", 
                            fila.getMinChegada(), fila.getMaxChegada());
                }
            }
            
            for (int origem : rotas.keySet()) {
                for (Rota rota : rotas.get(origem)) {
                    System.out.printf("- Rota: %d → %s (prob: %.2f)\n", 
                            origem, 
                            rota.getDestino() == 0 ? "saída" : String.valueOf(rota.getDestino()), 
                            rota.getProbabilidade());
                }
            }
            
        } catch (IOException e) {
            System.err.println("Erro ao carregar o arquivo de configuração: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Conta o número total de rotas no sistema
     */
    private int contarRotas() {
        int total = 0;
        for (List<Rota> rotasList : rotas.values()) {
            total += rotasList.size();
        }
        return total;
    }
    
    /**
     * Simula a rede de filas até que o limite de eventos seja atingido
     */
    public void simular() {
        System.out.println("\nIniciando simulação...");
        int contadorEventos = 0;
        
        while (escalonador.temEventos()) {
            Evento evento = escalonador.proximoEvento();
            if (evento == null) break;
            
            contadorEventos++;
            
            // Atualiza tempos de estado para todas as filas
            double tempoDecorrido = escalonador.getTempoAtual() - tempoAnterior;
            for (Fila fila : filas.values()) {
                fila.atualizarTempoEstado(tempoDecorrido);
            }
            tempoAnterior = escalonador.getTempoAtual();
            
            // Imprime progresso a cada 10000 eventos
            if (contadorEventos % 10000 == 0) {
                System.out.printf("Processando evento #%d (tempo: %.2f)\n", 
                        contadorEventos, escalonador.getTempoAtual());
                
                if (debug) {
                    for (int i : filas.keySet()) {
                        System.out.printf("  Fila %d: %d clientes (processados: %d, perdidos: %d)\n", 
                                i, filas.get(i).Status(), clientesRoteados.get(i), filas.get(i).getPerdidos());
                    }
                }
            }
            
            // Processa o evento
            switch (evento.getTipo()) {
                case Evento.CHEGADA:
                    processarChegada(evento);
                    break;
                case Evento.SAIDA:
                    processarSaida(evento);
                    break;
            }
        }
        
        System.out.println("Simulação concluída!");
        System.out.println("Total de eventos processados: " + contadorEventos);
        System.out.println("Tempo global: " + String.format("%.2f", escalonador.getTempoAtual()));
        System.out.println("Números aleatórios usados: " + gerador.getContador());
    }
    
    /**
     * Processa um evento de chegada
     */
    private void processarChegada(Evento evento) {
        int idFila = evento.getFilaDestino();
        Fila fila = filas.get(idFila);
        
        // Incrementa contador de clientes para esta fila
        clientesRoteados.put(idFila, clientesRoteados.get(idFila) + 1);
        
        if (debug && evento.getFilaOrigem() > 0) {
            System.out.printf("CHEGADA: Cliente da Fila %d chegou na Fila %d (tempo: %.2f)\n",
                    evento.getFilaOrigem(), idFila, evento.getTempo());
        }
        
        if (fila.podeAceitarCliente()) {
            fila.In();
            
            if (fila.temServidorLivre()) {
                // Cliente começa a ser atendido imediatamente
                double tempoAtendimento = gerador.gerarTempo(
                    fila.getMinAtendimento(), 
                    fila.getMaxAtendimento()
                );
                
                escalonador.adicionarEvento(new Evento(
                    evento.getTempo() + tempoAtendimento,
                    Evento.SAIDA,
                    0, // Servidor (simplificado)
                    idFila,
                    -1
                ));
            }
            
            // Se for uma fila com chegadas externas, agenda próxima chegada
            if (fila.getMaxChegada() > 0) {
                double tempoProximaChegada = evento.getTempo() + gerador.gerarTempo(
                    fila.getMinChegada(), 
                    fila.getMaxChegada()
                );
                
                escalonador.adicionarEvento(new Evento(
                    tempoProximaChegada,
                    Evento.CHEGADA,
                    -1,
                    0,
                    idFila
                ));
            }
        } else {
            fila.Loss();
            
            if (debug) {
                System.out.printf("PERDA: Cliente rejeitado na Fila %d (capacidade atingida, tempo: %.2f)\n", 
                        idFila, evento.getTempo());
            }
        }
    }
    
    /**
     * Processa um evento de saída
     */
    private void processarSaida(Evento evento) {
        int idFila = evento.getFilaOrigem();
        Fila fila = filas.get(idFila);
        fila.Out();
        
        // Rotear cliente para próxima fila ou saída do sistema
        boolean clienteRoteado = false;
        
        if (rotas.containsKey(idFila)) {
            int proximaFila = selecionarProximaFila(idFila);
            
            if (proximaFila > 0) {
                // Cliente vai para outra fila
                Evento eventoChegada = new Evento(
                    evento.getTempo(),
                    Evento.CHEGADA,
                    -1,
                    idFila,
                    proximaFila
                );
                escalonador.adicionarEvento(eventoChegada);
                clienteRoteado = true;
                
                if (debug) {
                    System.out.printf("ROTEAMENTO: Cliente da Fila %d roteado para Fila %d (tempo: %.2f)\n", 
                            idFila, proximaFila, evento.getTempo());
                }
            } else {
                if (debug) {
                    System.out.printf("SAÍDA: Cliente da Fila %d saiu do sistema (tempo: %.2f)\n", 
                            idFila, evento.getTempo());
                }
            }
        }
        
        // Se ainda há clientes na fila, agenda próxima saída
        if (fila.Status() >= fila.Servers()) {
            double tempoAtendimento = gerador.gerarTempo(
                fila.getMinAtendimento(),
                fila.getMaxAtendimento()
            );
            
            escalonador.adicionarEvento(new Evento(
                evento.getTempo() + tempoAtendimento,
                Evento.SAIDA,
                evento.getServidor(),
                idFila,
                -1
            ));
        }
    }
    
    /**
     * Seleciona a próxima fila para um cliente com base nas probabilidades
     */
    private int selecionarProximaFila(int idFilaOrigem) {
        List<Rota> rotasDisponiveis = rotas.get(idFilaOrigem);
        double aleatorio = gerador.nextRandom();
        double somaProbabilidade = 0.0;
        
        for (Rota rota : rotasDisponiveis) {
            somaProbabilidade += rota.getProbabilidade();
            if (aleatorio <= somaProbabilidade) {
                return rota.getDestino();
            }
        }
        
        // Se não encontrar nenhuma rota (quando a soma das probabilidades < 1)
        // ou para o último destino quando a soma é exatamente 1
        return rotasDisponiveis.get(rotasDisponiveis.size() - 1).getDestino();
    }
    
    /**
     * Imprime os resultados da simulação
     */
    public void imprimirResultados() {
        System.out.println("\n=== RESULTADOS DA SIMULAÇÃO ===");
        System.out.printf("Tempo global: %.2f\n", escalonador.getTempoAtual());
        System.out.printf("Números aleatórios usados: %d\n", gerador.getContador());
        
        for (Map.Entry<Integer, Fila> entry : filas.entrySet()) {
            int idFila = entry.getKey();
            Fila fila = entry.getValue();
            
            System.out.printf("\n=== FILA %d (G/G/%d/%d) ===\n", 
                    idFila, fila.Servers(), fila.Capacity());
            
            if (fila.getMaxChegada() > 0) {
                System.out.printf("Chegadas: %.1f..%.1f | ", 
                        fila.getMinChegada(), fila.getMaxChegada());
            }
            
            System.out.printf("Atendimento: %.1f..%.1f\n",
                    fila.getMinAtendimento(), fila.getMaxAtendimento());
            
            System.out.println("Clientes processados: " + clientesRoteados.get(idFila));
            System.out.println("Clientes perdidos: " + fila.getPerdidos());
            System.out.println("Distribuição de probabilidades dos estados:");
            
            for (int i = 0; i < fila.getTemposEstado().length; i++) {
                double prob = (fila.getTemposEstado()[i] / escalonador.getTempoAtual()) * 100;
                System.out.printf("Estado %d: %.2f%% (Tempo: %.2f)\n", 
                        i, prob, fila.getTemposEstado()[i]);
            }
        }
    }
    
    /**
     * Imprime os resultados de uma fila específica
     */
    public void imprimirResultadoFila(int idFila) {
        if (!filas.containsKey(idFila)) {
            System.out.println("Fila " + idFila + " não encontrada.");
            return;
        }
        
        Fila fila = filas.get(idFila);
        
        System.out.println("Clientes processados: " + clientesRoteados.get(idFila));
        System.out.println("Clientes perdidos: " + fila.getPerdidos());
        System.out.println("Distribuição de probabilidades dos estados:");
        
        for (int i = 0; i < fila.getTemposEstado().length; i++) {
            double prob = (fila.getTemposEstado()[i] / escalonador.getTempoAtual()) * 100;
            System.out.printf("Estado %d: %.2f%% (Tempo: %.2f)\n", 
                    i, prob, fila.getTemposEstado()[i]);
        }
    }
    
    /**
     * Exporta os resultados para um arquivo
     */
    public void exportarResultados(String arquivoSaida) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(arquivoSaida))) {
            writer.println("=== RESULTADOS DA SIMULAÇÃO ===");
            writer.printf("Tempo global: %.2f\n", escalonador.getTempoAtual());
            writer.printf("Números aleatórios usados: %d\n", gerador.getContador());
            
            for (Map.Entry<Integer, Fila> entry : filas.entrySet()) {
                int idFila = entry.getKey();
                Fila fila = entry.getValue();
                
                writer.printf("\n=== FILA %d (G/G/%d/%d) ===\n", 
                        idFila, fila.Servers(), fila.Capacity());
                
                if (fila.getMaxChegada() > 0) {
                    writer.printf("Chegadas: %.1f..%.1f | ", 
                            fila.getMinChegada(), fila.getMaxChegada());
                }
                
                writer.printf("Atendimento: %.1f..%.1f\n",
                        fila.getMinAtendimento(), fila.getMaxAtendimento());
                
                writer.println("Clientes processados: " + clientesRoteados.get(idFila));
                writer.println("Clientes perdidos: " + fila.getPerdidos());
                writer.println("Distribuição de probabilidades dos estados:");
                
                for (int i = 0; i < fila.getTemposEstado().length; i++) {
                    double prob = (fila.getTemposEstado()[i] / escalonador.getTempoAtual()) * 100;
                    writer.printf("Estado %d: %.2f%% (Tempo: %.2f)\n", 
                            i, prob, fila.getTemposEstado()[i]);
                }
            }
            
            System.out.println("Resultados exportados para: " + arquivoSaida);
        }
    }
    
    /**
     * Define se o modo debug está ativado
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    
    /**
     * Obtém o tempo global da simulação
     */
    public double getTempoGlobal() {
        return escalonador.getTempoAtual();
    }
    
    /**
     * Método principal
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java SimuladorRedeFilasSimples <arquivo_config> [arquivo_saida]");
            System.out.println("Formato suportado: .properties");
            return;
        }
        
        String arquivoConfig = args[0];
        String arquivoSaida = (args.length > 1) ? args[1] : null;
        
        SimuladorRedeFilasSimples simulador = new SimuladorRedeFilasSimples(
            arquivoConfig,
            100000,     // limite de números aleatórios
            42          // seed
        );
        
        // Ativar debug se desejar ver mensagens detalhadas
        // simulador.setDebug(true);
        
        simulador.simular();
        simulador.imprimirResultados();
        
        if (arquivoSaida != null) {
            try {
                simulador.exportarResultados(arquivoSaida);
            } catch (IOException e) {
                System.err.println("Erro ao exportar resultados: " + e.getMessage());
            }
        }
    }
}