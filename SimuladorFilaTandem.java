public class SimuladorFilaTandem {
    private final Fila fila1;
    private final Fila fila2;
    private final Escalonador escalonador;
    private final GeradorAleatorio gerador;
    private double tempoAnterior;
    
    public SimuladorFilaTandem(Fila fila1, Fila fila2, int limiteEventos, long seed) {
        this.fila1 = fila1;
        this.fila2 = fila2;
        this.gerador = new GeradorAleatorio(seed);
        this.escalonador = new Escalonador(limiteEventos);
        this.tempoAnterior = 0;
        
        // Agenda primeira chegada na fila1 no tempo 1.5
        escalonador.adicionarEvento(new Evento(
            1.5, Evento.CHEGADA, -1, 0, 1
        ));
    }
    
    public void simular() {
        while (escalonador.temEventos()) {
            Evento evento = escalonador.proximoEvento();
            if (evento == null) break;
            
            // Atualiza tempos de estado
            double tempoDecorrido = escalonador.getTempoAtual() - tempoAnterior;
            fila1.atualizarTempoEstado(tempoDecorrido);
            fila2.atualizarTempoEstado(tempoDecorrido);
            tempoAnterior = escalonador.getTempoAtual();
            
            // Processa o evento
            switch (evento.getTipo()) {
                case Evento.CHEGADA:
                    processarChegada(evento);
                    break;
                case Evento.SAIDA:
                    processarSaida(evento);
                    break;
                case Evento.PASSAGEM:
                    processarPassagem(evento);
                    break;
            }
        }
    }
    
    private void processarChegada(Evento evento) {
        Fila fila = (evento.getFilaDestino() == 1) ? fila1 : fila2;
        
        if (fila.podeAceitarCliente()) {
            fila.In();
            
            if (fila.temServidorLivre()) {
                double tempoAtendimento = gerador.gerarTempo(
                    fila.getMinAtendimento(), 
                    fila.getMaxAtendimento()
                );
                
                String tipoProximoEvento = (evento.getFilaDestino() == 1) ? Evento.PASSAGEM : Evento.SAIDA;
                int proximaFila = (evento.getFilaDestino() == 1) ? 2 : -1;
                
                escalonador.adicionarEvento(new Evento(
                    evento.getTempo() + tempoAtendimento,
                    tipoProximoEvento,
                    0, // Servidor padrão
                    evento.getFilaDestino(),
                    proximaFila
                ));
            }
            
            // Agenda próxima chegada externa apenas para fila1
            if (evento.getFilaDestino() == 1) {
                double tempoProximaChegada = evento.getTempo() + gerador.gerarTempo(
                    fila1.getMinChegada(), 
                    fila1.getMaxChegada()
                );
                
                escalonador.adicionarEvento(new Evento(
                    tempoProximaChegada,
                    Evento.CHEGADA,
                    -1,
                    0,
                    1
                ));
            }
        } else {
            fila.Loss();
        }
    }
    
    private void processarSaida(Evento evento) {
        Fila fila = (evento.getFilaOrigem() == 1) ? fila1 : fila2;
        fila.Out();
        
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
                evento.getFilaOrigem(),
                -1
            ));
        }
    }
    
    private void processarPassagem(Evento evento) {
        // Cliente sai da fila1
        fila1.Out();
        
        // Cliente tenta entrar na fila2
        if (fila2.podeAceitarCliente()) {
            fila2.In();
            
            if (fila2.temServidorLivre()) {
                double tempoAtendimento = gerador.gerarTempo(
                    fila2.getMinAtendimento(),
                    fila2.getMaxAtendimento()
                );
                
                escalonador.adicionarEvento(new Evento(
                    evento.getTempo() + tempoAtendimento,
                    Evento.SAIDA,
                    0,
                    2,
                    -1
                ));
            }
        } else {
            fila2.Loss();
        }
        
        // Verifica se precisa agendar próxima saída da fila1
        if (fila1.Status() >= fila1.Servers()) {
            double tempoAtendimento = gerador.gerarTempo(
                fila1.getMinAtendimento(),
                fila1.getMaxAtendimento()
            );
            
            escalonador.adicionarEvento(new Evento(
                evento.getTempo() + tempoAtendimento,
                Evento.PASSAGEM,
                evento.getServidor(),
                1,
                2
            ));
        }
    }
    
    public void imprimirResultados() {
        System.out.println("\n=== RESULTADOS DA SIMULAÇÃO ===");
        System.out.printf("Tempo global: %.2f\n", escalonador.getTempoAtual());
        System.out.printf("Números aleatórios usados: %d\n", gerador.getContador());
        
        System.out.println("\n=== FILA 1 (G/G/2/3) ===");
        System.out.printf("Chegadas: %.1f..%.1f | Atendimento: %.1f..%.1f\n",
                fila1.getMinChegada(), fila1.getMaxChegada(),
                fila1.getMinAtendimento(), fila1.getMaxAtendimento());
        System.out.println("Clientes perdidos: " + fila1.getPerdidos());
        System.out.println("Distribuição de probabilidades dos estados:");
        for (int i = 0; i < fila1.getTemposEstado().length; i++) {
            double prob = (fila1.getTemposEstado()[i] / escalonador.getTempoAtual()) * 100;
            System.out.printf("Estado %d: %.2f%% (Tempo: %.2f)\n", i, prob, fila1.getTemposEstado()[i]);
        }
        
        System.out.println("\n=== FILA 2 (G/G/1/5) ===");
        System.out.printf("Atendimento: %.1f..%.1f\n",
                fila2.getMinAtendimento(), fila2.getMaxAtendimento());
        System.out.println("Clientes perdidos: " + fila2.getPerdidos());
        System.out.println("Distribuição de probabilidades dos estados:");
        for (int i = 0; i < fila2.getTemposEstado().length; i++) {
            double prob = (fila2.getTemposEstado()[i] / escalonador.getTempoAtual()) * 100;
            System.out.printf("Estado %d: %.2f%% (Tempo: %.2f)\n", i, prob, fila2.getTemposEstado()[i]);
        }
    }
    
    public static void main(String[] args) {
        // Configuração conforme especificado
        Fila fila1 = new Fila(
            3,    // capacidade (G/G/2/3)
            2,    // numServidores
            1.0,  // minChegada
            4.0,  // maxChegada
            3.0,  // minAtendimento
            4.0   // maxAtendimento
        );
        
        Fila fila2 = new Fila(
            5,    // capacidade (G/G/1/5)
            1,    // numServidores
            0,    // minChegada (não recebe chegadas externas)
            0,    // maxChegada
            2.0,  // minAtendimento
            3.0   // maxAtendimento
        );
        
        SimuladorFilaTandem simulador = new SimuladorFilaTandem(
            fila1,
            fila2,
            100000, // limite de números aleatórios
            42      // seed
        );
        
        simulador.simular();
        simulador.imprimirResultados();
    }
}