public class Fila {
    private final int capacidade;
    private final int numServidores;
    private final double minChegada;
    private final double maxChegada;
    private final double minAtendimento;
    private final double maxAtendimento;
    
    private int clientes;
    private int perdidos;
    private double[] temposEstado;
    
    public Fila(int capacidade, int numServidores, 
               double minChegada, double maxChegada,
               double minAtendimento, double maxAtendimento) {
        this.capacidade = capacidade;
        this.numServidores = numServidores;
        this.minChegada = minChegada;
        this.maxChegada = maxChegada;
        this.minAtendimento = minAtendimento;
        this.maxAtendimento = maxAtendimento;
        this.clientes = 0;
        this.perdidos = 0;
        this.temposEstado = new double[capacidade + 1];
    }
    
    // Métodos básicos de manipulação
    public int Status() { return clientes; }
    public int Capacity() { return capacidade; }
    public int Servers() { return numServidores; }
    
    public int Loss() { 
        perdidos++; 
        return perdidos;
    }
    
    public void In() {
        if (clientes < capacidade) {
            clientes++;
        } else {
            Loss();
        }
    }
    
    public void Out() {
        if (clientes > 0) {
            clientes--;
        }
    }
    
    // Métodos para a simulação
    public boolean podeAceitarCliente() {
        return clientes < capacidade;
    }
    
    public boolean temServidorLivre() {
        return clientes < numServidores;
    }
    
    public int getPerdidos() {
        return perdidos;
    }
    
    public void atualizarTempoEstado(double tempoDecorrido) {
        if (clientes >= 0 && clientes < temposEstado.length) {
            temposEstado[clientes] += tempoDecorrido;
        }
    }
    
    public double[] getTemposEstado() {
        return temposEstado;
    }
    
    public double getMinChegada() { return minChegada; }
    public double getMaxChegada() { return maxChegada; }
    public double getMinAtendimento() { return minAtendimento; }
    public double getMaxAtendimento() { return maxAtendimento; }
}