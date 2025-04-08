public class GeradorAleatorio {
    private static final long M = (long) Math.pow(2, 31);
    private static final long a = 1664525;
    private static final long c = 1013904223;
    private long seed;
    private int contador;
    
    public GeradorAleatorio(long seed) {
        this.seed = seed;
        this.contador = 0;
    }
    
    public double nextRandom() {
        seed = (a * seed + c) % M;
        contador++;
        return (double) seed / M;
    }
    
    public double gerarTempo(double min, double max) {
        return min + (max - min) * nextRandom();
    }
    
    public int getContador() {
        return contador;
    }
}