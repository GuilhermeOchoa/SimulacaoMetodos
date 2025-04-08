# Simulação e Métodos

Trabalhos da disciplina de simulação e métodos

## Funcionamento do Evento de Passagem

- Quando um cliente é atendido na Fila 1:
  - É gerado um evento do tipo `PASSAGEM`
  - O cliente é removido da Fila 1 (`fila1.Out()`)
  - O cliente é adicionado à Fila 2 se houver espaço (`fila2.In()`)
  - Se a Fila 2 estiver cheia, é contabilizada uma perda (`fila2.Loss()`)

- Se o cliente entra na Fila 2:
  - É agendado um evento de `SAIDA` para quando o atendimento na Fila 2 for concluído
  - A Fila 1 continua processando outros clientes enquanto isso

## Requisitos Atendidos

✅ Limite de 100.000 números aleatórios  
✅ Configuração específica das duas filas em tandem  
✅ Tratamento correto de perdas em ambas as filas  
✅ Coleta de estatísticas de tempo em cada estado  

## Funcionalidades Implementadas

### Eventos de Passagem
- Clientes são transferidos da Fila 1 para a Fila 2 após atendimento

### Limite de números aleatórios
- A simulação para após usar 100.000 números aleatórios

### Coleta de estatísticas
- Tempo em cada estado das filas
- Número de clientes perdidos
- Probabilidades de estado

### Configuração flexível
- Pode ser adaptado para diferentes cenários de filas em tandem

## Como Executar o Simulador

1. **Compilar**:
   ```bash
   javac Evento.java Escalonador.java Fila.java GeradorAleatorio.java SimuladorFilaTandem.java
2. **Executar**:
   ```bash
   java SimuladorFilaTandem
   
## ⚙️ Funcionamento do Evento de Passagem

```mermaid
graph TD
    A[Cliente atendido na Fila 1] --> B{Espaço na Fila 2?}
    B -->|Sim| C[Adiciona à Fila 2]
    B -->|Não| D[Contabiliza perda]
    C --> E[Agenda saída da Fila 2]
