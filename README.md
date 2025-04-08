# SimulacaoMetodos
Trabalhos da disciplina de simulação e métodos

Funcionamento do Evento de Passagem
Quando um cliente é atendido na Fila 1:

É gerado um evento do tipo PASSAGEM

O cliente é removido da Fila 1 (fila1.Out())

O cliente é adicionado à Fila 2 se houver espaço (fila2.In())

Se a Fila 2 estiver cheia, é contabilizada uma perda (fila2.Loss())

Se o cliente entra na Fila 2:

É agendado um evento de SAIDA para quando o atendimento na Fila 2 for concluído

A Fila 1 continua processando outros clientes enquanto isso

Esta implementação atende completamente aos requisitos especificados, incluindo:

O limite de 100.000 números aleatórios

A configuração específica das duas filas em tandem

O tratamento correto de perdas em ambas as filas

A coleta de estatísticas de tempo em cada estado

Funcionalidades Implementadas
Eventos de Passagem: Clientes são transferidos da Fila 1 para a Fila 2 após atendimento

Limite de 100.000 números aleatórios: A simulação para após usar a quantidade especificada

Coleta de estatísticas:

Tempo em cada estado das filas

Número de clientes perdidos

Probabilidades de estado

Configuração flexível: Pode ser adaptado para diferentes cenários de filas em tandem
Compilar - javac Evento.java Escalonador.java Fila.java GeradorAleatorio.java SimuladorFilaTandem.java
Executar o simulador - java SimuladorFilaTandem
