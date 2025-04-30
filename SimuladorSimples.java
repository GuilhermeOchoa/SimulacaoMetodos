public class SimuladorSimples {
    public static void main(String[] args) {
        // Criação das filas conforme o modelo
        Fila fila1 = new Fila(1000, 1, 2.0, 4.0, 1.0, 2.0); // G/G/1
        Fila fila2 = new Fila(5, 2, 0.0, 0.0, 4.0, 8.0);    // G/G/2/5
        Fila fila3 = new Fila(10, 2, 0.0, 0.0, 5.0, 15.0);  // G/G/2/10
        
        // Configuração do simulador
        GeradorAleatorio gerador = new GeradorAleatorio(42);
        Escalonador escalonador = new Escalonador(100000);
        
        // Contadores e tempos
        double tempoAnterior = 0.0;
        int clientesFila1 = 0;
        int clientesFila2 = 0;
        int clientesFila3 = 0;
        
        // Agendamento do primeiro evento (chegada no tempo 2.0)
        escalonador.adicionarEvento(new Evento(2.0, "CHEGADA", -1, 0, 1));
        
        System.out.println("Iniciando simulação simplificada...");
        
        // Loop principal da simulação
        while (escalonador.temEventos()) {
            Evento evento = escalonador.proximoEvento();
            if (evento == null) break;
            
            // Atualiza tempos de estado
            double tempoDecorrido = escalonador.getTempoAtual() - tempoAnterior;
            fila1.atualizarTempoEstado(tempoDecorrido);
            fila2.atualizarTempoEstado(tempoDecorrido);
            fila3.atualizarTempoEstado(tempoDecorrido);
            tempoAnterior = escalonador.getTempoAtual();
            
            // Imprime status a cada 10000 eventos
            if (escalonador.getContadorEventos() % 10000 == 0) {
                System.out.println("Evento #" + escalonador.getContadorEventos() + 
                        " - Tempo: " + String.format("%.2f", escalonador.getTempoAtual()));
                System.out.println("  Fila 1: " + fila1.Status() + " clientes");
                System.out.println("  Fila 2: " + fila2.Status() + " clientes");
                System.out.println("  Fila 3: " + fila3.Status() + " clientes");
                System.out.println("  Processados - Fila 1: " + clientesFila1 + 
                        ", Fila 2: " + clientesFila2 + ", Fila 3: " + clientesFila3);
            }
            
            // Processa o evento
            if (evento.getTipo().equals("CHEGADA")) {
                // Chegada na Fila 1 (externa)
                if (evento.getFilaDestino() == 1 && evento.getFilaOrigem() == 0) {
                    clientesFila1++;
                    fila1.In();
                    
                    // Agenda próxima chegada externa
                    double tempoProximo = evento.getTempo() + 
                            gerador.gerarTempo(fila1.getMinChegada(), fila1.getMaxChegada());
                    escalonador.adicionarEvento(new Evento(
                            tempoProximo, "CHEGADA", -1, 0, 1));
                    
                    // Se houver servidor livre, agenda atendimento
                    if (fila1.Status() <= fila1.Servers()) {
                        double tempoSaida = evento.getTempo() + 
                                gerador.gerarTempo(fila1.getMinAtendimento(), fila1.getMaxAtendimento());
                        escalonador.adicionarEvento(new Evento(
                                tempoSaida, "SAIDA", 0, 1, -1));
                    }
                    
                    if (clientesFila1 % 100 == 0) {
                        System.out.println("Cliente #" + clientesFila1 + " chegou na Fila 1");
                    }
                }
                // Chegada na Fila 2 (de outra fila)
                else if (evento.getFilaDestino() == 2) {
                    clientesFila2++;
                    if (fila2.podeAceitarCliente()) {
                        fila2.In();
                        
                        // Se houver servidor livre, agenda atendimento
                        if (fila2.Status() <= fila2.Servers()) {
                            double tempoSaida = evento.getTempo() + 
                                    gerador.gerarTempo(fila2.getMinAtendimento(), fila2.getMaxAtendimento());
                            escalonador.adicionarEvento(new Evento(
                                    tempoSaida, "SAIDA", 0, 2, -1));
                        }
                    } else {
                        fila2.Loss();
                        System.out.println("PERDA: Cliente rejeitado na Fila 2 (capacidade atingida)");
                    }
                    
                    System.out.println("Cliente da Fila " + evento.getFilaOrigem() + 
                            " chegou na Fila 2");
                }
                // Chegada na Fila 3 (de outra fila)
                else if (evento.getFilaDestino() == 3) {
                    clientesFila3++;
                    if (fila3.podeAceitarCliente()) {
                        fila3.In();
                        
                        // Se houver servidor livre, agenda atendimento
                        if (fila3.Status() <= fila3.Servers()) {
                            double tempoSaida = evento.getTempo() + 
                                    gerador.gerarTempo(fila3.getMinAtendimento(), fila3.getMaxAtendimento());
                            escalonador.adicionarEvento(new Evento(
                                    tempoSaida, "SAIDA", 0, 3, -1));
                        }
                    } else {
                        fila3.Loss();
                        System.out.println("PERDA: Cliente rejeitado na Fila 3 (capacidade atingida)");
                    }
                    
                    System.out.println("Cliente da Fila " + evento.getFilaOrigem() + 
                            " chegou na Fila 3");
                }
            }
            else if (evento.getTipo().equals("SAIDA")) {
                // Saída da Fila 1
                if (evento.getFilaOrigem() == 1) {
                    fila1.Out();
                    
                    // Rotear cliente (80% para Fila 2, 20% para Fila 3)
                    double aleatorio = gerador.nextRandom();
                    if (aleatorio <= 0.8) {
                        // Encaminhar para Fila 2
                        escalonador.adicionarEvento(new Evento(
                                evento.getTempo(), "CHEGADA", -1, 1, 2));
                        System.out.println("Cliente da Fila 1 roteado para Fila 2 (aleatorio=" + 
                                String.format("%.4f", aleatorio) + ")");
                    } else {
                        // Encaminhar para Fila 3
                        escalonador.adicionarEvento(new Evento(
                                evento.getTempo(), "CHEGADA", -1, 1, 3));
                        System.out.println("Cliente da Fila 1 roteado para Fila 3 (aleatorio=" + 
                                String.format("%.4f", aleatorio) + ")");
                    }
                    
                    // Se ainda há clientes na fila, agenda próxima saída
                    if (fila1.Status() >= fila1.Servers()) {
                        double tempoSaida = evento.getTempo() + 
                                gerador.gerarTempo(fila1.getMinAtendimento(), fila1.getMaxAtendimento());
                        escalonador.adicionarEvento(new Evento(
                                tempoSaida, "SAIDA", 0, 1, -1));
                    }
                }
                // Saída da Fila 2
                else if (evento.getFilaOrigem() == 2) {
                    fila2.Out();
                    
                    // Rotear cliente (20% sai, 30% para Fila 1, 50% para Fila 3)
                    double aleatorio = gerador.nextRandom();
                    if (aleatorio <= 0.2) {
                        // Cliente sai do sistema
                        System.out.println("Cliente da Fila 2 saiu do sistema (aleatorio=" + 
                                String.format("%.4f", aleatorio) + ")");
                    } else if (aleatorio <= 0.5) { // 0.2 + 0.3 = 0.5
                        // Encaminhar para Fila 1
                        escalonador.adicionarEvento(new Evento(
                                evento.getTempo(), "CHEGADA", -1, 2, 1));
                        System.out.println("Cliente da Fila 2 roteado para Fila 1 (aleatorio=" + 
                                String.format("%.4f", aleatorio) + ")");
                    } else {
                        // Encaminhar para Fila 3
                        escalonador.adicionarEvento(new Evento(
                                evento.getTempo(), "CHEGADA", -1, 2, 3));
                        System.out.println("Cliente da Fila 2 roteado para Fila 3 (aleatorio=" + 
                                String.format("%.4f", aleatorio) + ")");
                    }
                    
                    // Se ainda há clientes na fila, agenda próxima saída
                    if (fila2.Status() >= fila2.Servers()) {
                        double tempoSaida = evento.getTempo() + 
                                gerador.gerarTempo(fila2.getMinAtendimento(), fila2.getMaxAtendimento());
                        escalonador.adicionarEvento(new Evento(
                                tempoSaida, "SAIDA", 0, 2, -1));
                    }
                }
                // Saída da Fila 3
                else if (evento.getFilaOrigem() == 3) {
                    fila3.Out();
                    
                    // Rotear cliente (30% sai, 70% para Fila 1)
                    double aleatorio = gerador.nextRandom();
                    if (aleatorio <= 0.3) {
                        // Cliente sai do sistema
                        System.out.println("Cliente da Fila 3 saiu do sistema (aleatorio=" + 
                                String.format("%.4f", aleatorio) + ")");
                    } else {
                        // Encaminhar para Fila 1
                        escalonador.adicionarEvento(new Evento(
                                evento.getTempo(), "CHEGADA", -1, 3, 1));
                        System.out.println("Cliente da Fila 3 roteado para Fila 1 (aleatorio=" + 
                                String.format("%.4f", aleatorio) + ")");
                    }
                    
                    // Se ainda há clientes na fila, agenda próxima saída
                    if (fila3.Status() >= fila3.Servers()) {
                        double tempoSaida = evento.getTempo() + 
                                gerador.gerarTempo(fila3.getMinAtendimento(), fila3.getMaxAtendimento());
                        escalonador.adicionarEvento(new Evento(
                                tempoSaida, "SAIDA", 0, 3, -1));
                    }
                }
            }
        }
        
        // Imprime resultados
        System.out.println("\n=== RESULTADOS DA SIMULAÇÃO ===");
        System.out.printf("Tempo global: %.2f\n", escalonador.getTempoAtual());
        System.out.printf("Números aleatórios usados: %d\n", gerador.getContador());
        
        System.out.println("\n1. Resultado da Fila 1: G/G/1, chegadas entre 2..4, atendimento entre 1..2:");
        System.out.println("Clientes processados: " + clientesFila1);
        System.out.println("Clientes perdidos: " + fila1.getPerdidos());
        System.out.println("Distribuição de probabilidades dos estados:");
        for (int i = 0; i < fila1.getTemposEstado().length; i++) {
            double prob = (fila1.getTemposEstado()[i] / escalonador.getTempoAtual()) * 100;
            System.out.printf("Estado %d: %.2f%% (Tempo: %.2f)\n", i, prob, fila1.getTemposEstado()[i]);
        }
        
        System.out.println("\n2. Resultado da Fila 2: G/G/2/5, atendimento entre 4..8:");
        System.out.println("Clientes processados: " + clientesFila2);
        System.out.println("Clientes perdidos: " + fila2.getPerdidos());
        System.out.println("Distribuição de probabilidades dos estados:");
        for (int i = 0; i < fila2.getTemposEstado().length; i++) {
            double prob = (fila2.getTemposEstado()[i] / escalonador.getTempoAtual()) * 100;
            System.out.printf("Estado %d: %.2f%% (Tempo: %.2f)\n", i, prob, fila2.getTemposEstado()[i]);
        }
        
        System.out.println("\n3. Resultado da Fila 3: G/G/2/10, atendimento entre 5..15:");
        System.out.println("Clientes processados: " + clientesFila3);
        System.out.println("Clientes perdidos: " + fila3.getPerdidos());
        System.out.println("Distribuição de probabilidades dos estados:");
        for (int i = 0; i < fila3.getTemposEstado().length; i++) {
            double prob = (fila3.getTemposEstado()[i] / escalonador.getTempoAtual()) * 100;
            System.out.printf("Estado %d: %.2f%% (Tempo: %.2f)\n", i, prob, fila3.getTemposEstado()[i]);
        }
        
        System.out.println("\n4. Tempo total de simulação: " + 
                String.format("%.2f", escalonador.getTempoAtual()));
    }
}