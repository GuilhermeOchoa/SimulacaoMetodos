public class Evento implements Comparable<Evento> {
    public static final String CHEGADA = "CHEGADA";
    public static final String SAIDA = "SAIDA";
    public static final String PASSAGEM = "PASSAGEM";
    
    private final double tempo;
    private final String tipo;
    private final int servidor;
    private final int filaOrigem;
    private final int filaDestino;
    
    public Evento(double tempo, String tipo, int servidor, int filaOrigem, int filaDestino) {
        this.tempo = tempo;
        this.tipo = tipo;
        this.servidor = servidor;
        this.filaOrigem = filaOrigem;
        this.filaDestino = filaDestino;
    }
    
    public double getTempo() { return tempo; }
    public String getTipo() { return tipo; }
    public int getServidor() { return servidor; }
    public int getFilaOrigem() { return filaOrigem; }
    public int getFilaDestino() { return filaDestino; }
    
    @Override
    public int compareTo(Evento outro) {
        return Double.compare(this.tempo, outro.tempo);
    }
    
    @Override
    public String toString() {
        String origem = filaOrigem == 0 ? "Ext" : String.valueOf(filaOrigem);
        String destino = filaDestino == -1 ? "-" : 
                        (filaDestino == 0 ? "Ext" : String.valueOf(filaDestino));
        
        return String.format("Evento[%.2f, %s, %s→%s]", 
               tempo, tipo, origem, destino);
    }
}