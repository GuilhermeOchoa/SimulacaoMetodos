import java.util.PriorityQueue;

public class Escalonador {
    private final PriorityQueue<Evento> eventos;
    private double tempoAtual;
    private int contadorEventos;
    private final int limiteEventos;
    
    public Escalonador(int limiteEventos) {
        this.eventos = new PriorityQueue<>();
        this.tempoAtual = 0;
        this.contadorEventos = 0;
        this.limiteEventos = limiteEventos;
    }
    
    public void adicionarEvento(Evento evento) {
        if (evento.getTempo() < tempoAtual) {
            throw new IllegalArgumentException("Tempo do evento não pode ser menor que o tempo atual");
        }
        eventos.add(evento);
    }
    
    public Evento proximoEvento() {
        if (contadorEventos >= limiteEventos) {
            return null;
        }
        
        Evento evento = eventos.poll();
        if (evento != null) {
            tempoAtual = evento.getTempo();
            contadorEventos++;
        }
        return evento;
    }
    
    public double getTempoAtual() {
        return tempoAtual;
    }
    
    public int getContadorEventos() {
        return contadorEventos;
    }
    
    public boolean temEventos() {
        return !eventos.isEmpty() && contadorEventos < limiteEventos;
    }
}