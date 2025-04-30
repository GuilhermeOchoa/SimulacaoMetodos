# Simulador de Rede de Filas

Este simulador permite modelar e simular qualquer topologia de rede de filas a partir de um arquivo de configuração em formato Properties.

## Requisitos

- Java 8 ou superior (sem dependências externas)

## Estrutura do Projeto

O projeto é composto pelos seguintes arquivos:

1. `SimuladorRedeFilas.java` - Classe principal do simulador
2. `Escalonador.java` - Gerencia a fila de eventos
3. `Evento.java` - Representa os eventos da simulação
4. `Fila.java` - Representa uma fila no sistema
5. `GeradorAleatorio.java` - Gerador de números aleatórios
6. `TesteSimulador.java` - Classe para teste do simulador

## Compilação

Para compilar o simulador, execute:

```bash
javac *.java
```

## Execução

Para executar o simulador:

```bash
java SimuladorRedeFilas config-tandem.properties
```

Ou, para usar a classe de teste que gera o arquivo de configuração automaticamente:

```bash
java TesteSimulador
```

## Arquivo de Configuração

O arquivo de configuração usa o formato Properties padrão do Java e deve seguir a seguinte estrutura:

```properties
# Tempo para o primeiro cliente chegar
tempoInicial=2.0
# Fila onde o primeiro cliente chega
filaInicial=1

# Definição das filas
fila.1.capacidade=3
fila.1.servidores=2
fila.1.minChegada=1.0
fila.1.maxChegada=4.0
fila.1.minAtendimento=3.0
fila.1.maxAtendimento=4.0

fila.2.capacidade=5
fila.2.servidores=1
fila.2.minChegada=0.0
fila.2.maxChegada=0.0
fila.2.minAtendimento=2.0
fila.2.maxAtendimento=3.0

# Rotas entre as filas
rota.1.origem=1
rota.1.destino=2
rota.1.probabilidade=1.0

rota.2.origem=2
rota.2.destino=0
rota.2.probabilidade=1.0
```

### Parâmetros:

- **Filas**:
  - `fila.N.capacidade`: Número máximo de clientes (incluindo os em atendimento)
  - `fila.N.servidores`: Número de servidores paralelos
  - `fila.N.minChegada` e `fila.N.maxChegada`: Intervalo para tempo entre chegadas (0 para filas internas sem chegadas externas)
  - `fila.N.minAtendimento` e `fila.N.maxAtendimento`: Intervalo para tempo de atendimento

- **Rotas**:
  - `rota.N.origem`: ID da fila de origem
  - `rota.N.destino`: ID da fila de destino (use 0 para indicar saída do sistema)
  - `rota.N.probabilidade`: Probabilidade do cliente seguir esta rota (entre 0 e 1)

## Resultado da Simulação

Ao final da simulação, o programa exibe:

1. Tempo global da simulação
2. Quantidade de números aleatórios utilizados
3. Para cada fila:
   - Configuração (parâmetros)
   - Número de clientes perdidos
   - Distribuição de probabilidades de cada estado
   - Tempos acumulados em cada estado

## Exemplo de Saída

```
=== RESULTADOS DA SIMULAÇÃO ===
Tempo global: 782.35
Números aleatórios usados: 100000

=== FILA 1 (G/G/2/3) ===
Chegadas: 1.0..4.0 | Atendimento: 3.0..4.0
Clientes perdidos: 354
Distribuição de probabilidades dos estados:
Estado 0: 8.71% (Tempo: 68.15)
Estado 1: 25.63% (Tempo: 200.50)
Estado 2: 34.18% (Tempo: 267.42)
Estado 3: 31.48% (Tempo: 246.28)

=== FILA 2 (G/G/1/5) ===
Atendimento: 2.0..3.0
Clientes perdidos: 218
Distribuição de probabilidades dos estados:
Estado 0: 4.35% (Tempo: 34.03)
Estado 1: 13.68% (Tempo: 107.06)
Estado 2: 18.24% (Tempo: 142.68)
Estado 3: 21.03% (Tempo: 164.53)
Estado 4: 20.92% (Tempo: 163.69)
Estado 5: 21.78% (Tempo: 170.35)
```

## Extensões Possíveis

O simulador pode ser estendido para incluir:

1. Diferentes distribuições estatísticas (além da distribuição uniforme)
2. Métricas adicionais como tempo médio de espera e throughput
3. Visualização gráfica dos resultados
4. Suporte a outros formatos de arquivo de configuração