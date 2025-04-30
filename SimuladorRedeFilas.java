import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class SimuladorRedeFilas {
    private final Map<Integer, Fila> filas;
    private final Map<Integer, List<Rota>> rotas;
    private final Escalonador escalonador;
    private final GeradorAleatorio gerador;
    private double tempoAnterior;
    
    public SimuladorRedeFilas(String arquivoConfig, int limiteAleatorios, long seed) {
        this.filas = new HashMap<>();
        this.rotas = new HashMap<>();
        this.gerador = new GeradorAleatorio(seed);
        this.escalonador = new Escalonador(limiteAleatorios);
        this.tempoAnterior = 0;
        
        carregarConfiguracao(arquivoConfig);
    }
    
    private void carregarConfiguracao(String arquivoConfig) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(arquivoConfig)) {
            props.load(fis);
            
            // Carregar informações básicas
            double tempoInicial = Double.parseDouble(props.getProperty("tempoInicial", "0.0"));
            int filaInicial = Integer.parseInt(props.getProperty("filaInicial", "1"));
            
            // Descobrir quantas filas existem
            int numFilas = 0;
            while (props.containsKey("fila." + (numFilas + 1) + ".capacidade")) {
                numFilas++;
            }
            
            // Carregar filas
            for (int i = 1; i <= numFilas; i++) {
                int id = i;
                int capacidade = Integer.parseInt(props.getProperty("fila." + i + ".capacidade"));
                int numServidores = Integer.parseInt(props.getProperty("fila." + i + ".servidores"));
                double minChegada = Double.parseDouble(props.getProperty("fila." + i + ".minChegada", "0.0"));
                double maxChegada = Double.parseDouble(props.getProperty("fila." + i + ".maxChegada", "0.0"));
                double minAtendimento = Double.parseDouble(props.getProperty("fila." + i + ".minAtendimento"));
                double maxAtendimento = Double.parseDouble(props.getProperty("fila." + i + ".maxAtendimento"));
                
                filas.put(id, new Fila(capacidade, numServidores, minChegada, maxChegada, minAtendimento, maxAtendimento));
            }
            
            // Descobrir quantas rotas existem
            int numRotas = 0;
            while (props.containsKey("rota." + (numRotas + 1) + ".origem")) {
                numRotas++;
            }
            
            // Carregar rotas
            for (int i = 1; i <= numRotas; i++) {
                int origem = Integer.parseInt(props.getProperty("rota." + i + ".origem"));
                int destino = Integer.parseInt(props.getProperty("rota." + i + ".destino"));
                double probabilidade = Double.parseDouble(props.getProperty("rota." + i + ".probabilidade"));
                
                if (!rotas.containsKey(origem)) {
                    rotas.put(origem, new ArrayList<>());
                }
                rotas.get(origem).add(new Rota(destino, probabilidade));
            }
            
            // Configurar evento inicial de chegada
            escalonador.adicionarEvento(new Evento(
                tempoInicial, Evento.CHEGADA, -1, 0, filaInicial
            ));
            
        } catch (IOException e) {
            System.err.println("Erro ao carregar o arquivo de configuração: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void simular() {
        while (escalonador.temEventos()) {
            Evento evento = escalonador.proximoEvento();
            if (evento == null) break;
            
            // Atualiza tempos de estado para todas as filas
            double tempoDecorrido = escalonador.getTempoAtual() - tempoAnterior;
            for (Fila fila : filas.values()) {
                fila.atualizarTempoEstado(tempoDecorrido);
            }
            tempoAnterior = escalonador.getTempoAtual();
            
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
    }
    
    private void processarChegada(Evento evento) {
        int idFila = evento.getFilaDestino();
        Fila fila = filas.get(idFila);
        
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
        }
    }
    
    private void processarSaida(Evento evento) {
        int idFila = evento.getFilaOrigem();
        Fila fila = filas.get(idFila);
        fila.Out();
        
        // Rotear cliente para próxima fila ou saída do sistema
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
            }
            // Se proximaFila <= 0, cliente sai do sistema
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
            
            System.out.println("Clientes perdidos: " + fila.getPerdidos());
            System.out.println("Distribuição de probabilidades dos estados:");
            
            for (int i = 0; i < fila.getTemposEstado().length; i++) {
                double prob = (fila.getTemposEstado()[i] / escalonador.getTempoAtual()) * 100;
                System.out.printf("Estado %d: %.2f%% (Tempo: %.2f)\n", 
                        i, prob, fila.getTemposEstado()[i]);
            }
        }
    }
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java SimuladorRedeFilas <arquivo.properties>");
            return;
        }
        
        SimuladorRedeFilas simulador = new SimuladorRedeFilas(
            args[0],    // arquivo de configuração
            100000,     // limite de números aleatórios
            42          // seed
        );
        
        simulador.simular();
        simulador.imprimirResultados();
    }
}

// Classe auxiliar para representar rotas entre filas
class Rota {
    private final int destino;
    private final double probabilidade;
    
    public Rota(int destino, double probabilidade) {
        this.destino = destino;
        this.probabilidade = probabilidade;
    }
    
    public int getDestino() {
        return destino;
    }
    
    public double getProbabilidade() {
        return probabilidade;
    }
}