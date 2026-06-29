package cliente;

import interfaces.EstadoJogo;
import interfaces.ServicoJogo21;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class ClienteGrafico extends JFrame {

    private ServicoJogo21 servico;
    private String nomeJogador;
    private EstadoJogo estadoAtual;

    // Componentes da Interface
    private JPanel painelMesa;
    private JPanel painelCartasJogador;
    private JPanel painelCartasDealer;
    private JLabel lblStatus;
    private JLabel lblSaldo;
    private JButton btnHit;
    private JButton btnStand;
    private JButton btnNovaRodada;
    private JButton btnSair;

    public ClienteGrafico() {
        // 1. Configurações Iniciais da Janela
        setTitle("UTFPR - Jogo 21 Distribuído (RMI)");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                btnSair.doClick(); // Simula o clique no botão Sair
            }
        });
        setLayout(new BorderLayout());

        // Conecta ao RMI antes de exibir a interface
        conectarServidor();
        solicitarNomeUsuario();

        // 2. Criação dos Painéis de Layout (Verde Feltro Clássico)
        Color verdeFeltro = new Color(0, 128, 0);

        painelMesa = new JPanel();
        painelMesa.setBackground(verdeFeltro);
        painelMesa.setLayout(new GridLayout(2, 1, 10, 10));

        painelCartasDealer = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        painelCartasDealer.setBackground(verdeFeltro);
        painelCartasDealer.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.WHITE), "Mão do Dealer (Banca)", 0, 0, null, Color.WHITE));

        painelCartasJogador = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        painelCartasJogador.setBackground(verdeFeltro);
        painelCartasJogador.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.WHITE), "Sua Mão", 0, 0, null, Color.WHITE));

        painelMesa.add(painelCartasDealer);
        painelMesa.add(painelCartasJogador);

        // 3. Painel de Controle Superior (Placar e Informações)
        JPanel painelTopo = new JPanel(new BorderLayout());
        painelTopo.setBackground(new Color(0, 100, 0));
        painelTopo.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        lblSaldo = new JLabel("Saldo: R$ 0 | Aposta: R$ 0");
        lblSaldo.setFont(new Font("Arial", Font.BOLD, 16));
        lblSaldo.setForeground(Color.YELLOW);

        lblStatus = new JLabel("Bem-vindo! Faça sua aposta para começar.");
        lblStatus.setFont(new Font("Arial", Font.PLAIN, 14));
        lblStatus.setForeground(Color.WHITE);

        painelTopo.add(lblSaldo, BorderLayout.WEST);
        painelTopo.add(lblStatus, BorderLayout.CENTER);

        // 4. Painel de Botões Inferiores
        JPanel painelBotoes = new JPanel();
        painelBotoes.setBackground(new Color(0, 100, 0));

        btnHit = new JButton("Pedir Carta (Hit)");
        btnStand = new JButton("Parar (Stand)");
        btnNovaRodada = new JButton("Nova Rodada (Apostar)");
        btnSair = new JButton("Sair do Jogo");

        // Estilização de botões retrô
        Dimension dimBotao = new Dimension(150, 40);
        btnHit.setPreferredSize(dimBotao);
        btnStand.setPreferredSize(dimBotao);
        btnNovaRodada.setPreferredSize(dimBotao);
        btnSair.setPreferredSize(dimBotao);
        btnSair.setBackground(new Color(178, 34, 34));
        btnSair.setForeground(Color.WHITE);

        painelBotoes.add(btnHit);
        painelBotoes.add(btnStand);
        painelBotoes.add(btnNovaRodada);
        painelBotoes.add(btnSair);

        // Adiciona os painéis principais na janela
        add(painelTopo, BorderLayout.NORTH);
        add(painelMesa, BorderLayout.CENTER);
        add(painelBotoes, BorderLayout.SOUTH);

        // Desativa botões de ação até a rodada iniciar
        btnHit.setEnabled(false);
        btnStand.setEnabled(false);

        // 5. Configuração dos Ouvintes de Evento (Listeners)
        configurarEventos();
    }

    private void conectarServidor() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            servico = (ServicoJogo21) registry.lookup("ServicoJogo21");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Não foi possível conectar ao Servidor RMI.\nCertifique-se de que ele está rodando.", "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    private void solicitarNomeUsuario() {
        while (true) {
            String input = JOptionPane.showInputDialog(this, "Digite seu nome de usuário:", "Autenticação", JOptionPane.QUESTION_MESSAGE);
            if (input == null) System.exit(0);
            
            nomeJogador = input.trim();
            if (nomeJogador.isEmpty()) {
                JOptionPane.showMessageDialog(this, "O nome não pode ser vazio.", "Aviso", JOptionPane.WARNING_MESSAGE);
                continue;
            }

            try {
                if (servico.verificarNomeDisponivel(nomeJogador)) {
                    break; // Nome válido e disponível
                } else {
                    JOptionPane.showMessageDialog(this, "Este nome já está em uso no servidor. Escolha outro.", "Nome Duplicado", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                System.exit(0);
            }
        }
    }

    private void configurarEventos() {
        // Ação do Botão Nova Rodada / Apostar
        btnNovaRodada.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                	int saldoDisponivel = servico.obterSaldo(nomeJogador);
                	if (saldoDisponivel <= 0) {
                	    JOptionPane.showMessageDialog(ClienteGrafico.this, 
                	        "Você faliu! Seu nome foi resetado. Conecte-se novamente para ganhar R$ 500.", 
                	        "Fim de Jogo", JOptionPane.ERROR_MESSAGE);
                	    
                	    try {
                	        servico.desconectar(nomeJogador);
                	    } catch (Exception ex) {}
                	    
                	    dispose();
                	    System.exit(0);
                	    return;
                	}

                    String inputAposta = JOptionPane.showInputDialog(ClienteGrafico.this, "Seu saldo: R$ " + saldoDisponivel + "\nQuanto deseja apostar?", "Nova Rodada", JOptionPane.QUESTION_MESSAGE);
                    if (inputAposta == null) return;

                    int aposta = Integer.parseInt(inputAposta.trim());
                    if (aposta <= 0 || aposta > saldoDisponivel) {
                        JOptionPane.showMessageDialog(ClienteGrafico.this, "Valor de aposta inválido!", "Aviso", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // Inicia a rodada no servidor
                    estadoAtual = servico.iniciarRodada(nomeJogador, aposta);
                    atualizarMesa();

                    // Controla o estado dos botões
                    btnHit.setEnabled(true);
                    btnStand.setEnabled(true);
                    btnNovaRodada.setEnabled(false);

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(ClienteGrafico.this, "Por favor, insira um número inteiro.", "Erro de Formatação", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Ação do Botão Hit
        btnHit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    estadoAtual = servico.pedirCarta(nomeJogador);
                    atualizarMesa();
                    verificarFimDeJogo();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Ação do Botão Stand
        btnStand.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    estadoAtual = servico.parar(nomeJogador);
                    atualizarMesa();
                    verificarFimDeJogo();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        btnSair.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Exibe uma confirmação para o usuário
                int resposta = JOptionPane.showConfirmDialog(
                        ClienteGrafico.this, 
                        "Deseja realmente sair do jogo?", 
                        "Confirmar Saída", 
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );
                
                if (resposta == JOptionPane.YES_OPTION) {
                	try {
                	    // Avisa o servidor para limpar os dados e aplicar a regra de falência
                	    servico.desconectar(nomeJogador);
                	} catch (Exception ex) {
                	    // Ignora erros de rede ao fechar
                	} finally {
                	    dispose();
                	    System.exit(0);
                	}
                }
            }
        });
    }

    private void atualizarMesa() {
        if (estadoAtual == null) return;

        // 1. Atualiza os textos de saldo e aposta no topo
        lblSaldo.setText("Saldo: R$ " + estadoAtual.getSaldoAtual() + " | Aposta: R$ " + estadoAtual.getApostaAtual());
        lblStatus.setText(estadoAtual.getMensagem());

        // 2. Limpa as cartas antigas da tela para renderizar as novas
        painelCartasJogador.removeAll();
        painelCartasDealer.removeAll();

        // 3. Renderiza as cartas do Jogador
        for (String carta : estadoAtual.getCartasJogador()) {
            painelCartasJogador.add(new ComponenteCarta(carta));
        }

        // 4. Renderiza as cartas do Dealer
        for (String carta : estadoAtual.getCartasVisiveisDealer()) {
            painelCartasDealer.add(new ComponenteCarta(carta));
        }

        // ==========================================
        // NOVIDADE: ATUALIZAÇÃO DINÂMICA DOS TOTAIS
        // ==========================================
        
        // Pega as pontuações que o servidor já calculou e enviou dentro do objeto estadoAtual
        int pontosJogador = estadoAtual.getPontuacaoJogador();
        
        // Para o Dealer, precisamos descobrir se a segunda carta ainda está oculta.
        // Se estiver oculta, a soma total dele não deve ser revelada na tela (padrão do Blackjack)
        String textoTotalDealer;
        if (estadoAtual.getCartasVisiveisDealer().contains("[CARTA OCULTA]")) {
            textoTotalDealer = "Mão do Dealer (Banca) - Total: ?";
        } else {
            // Se o jogo acabou ou o jogador deu 'Stand', o método calcularPontuacao rodou no servidor para o Dealer
            // Como o EstadoJogo guarda a pontuação real, podemos usar um método auxiliar ou apenas mostrar o estado final
            int pontosDealer = calcularPontuacaoLocal(estadoAtual.getCartasVisiveisDealer());
            textoTotalDealer = "Mão do Dealer (Banca) - Total: " + pontosDealer;
        }

        // Atualiza as bordas dos painéis com os novos títulos contendo as somas
        painelCartasJogador.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.WHITE), 
                "Sua Mão - Total: " + pontosJogador, 
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
                javax.swing.border.TitledBorder.DEFAULT_POSITION, 
                null, 
                Color.WHITE
        ));

        painelCartasDealer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.WHITE), 
                textoTotalDealer, 
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
                javax.swing.border.TitledBorder.DEFAULT_POSITION, 
                null, 
                Color.WHITE
        ));

        // Força a tela a se redesenhar completamente com os novos totais e componentes
        painelMesa.revalidate();
        painelMesa.repaint();
    }
    
    // Método auxiliar para o cliente calcular os pontos das cartas que ele está vendo na tela
    private int calcularPontuacaoLocal(List<String> mao) {
        int pontos = 0;
        int ases = 0;

        for (String carta : mao) {
            if (carta.equals("[CARTA OCULTA]")) continue;
            
            String valor = carta.split(" ")[0]; 
            if (valor.equals("J") || valor.equals("Q") || valor.equals("K")) {
                pontos += 10;
            } else if (valor.equals("A")) {
                ases++;
                pontos += 11;
            } else {
                pontos += Integer.parseInt(valor);
            }
        }

        while (pontos > 21 && ases > 0) {
            pontos -= 10;
            ases--;
        }
        return pontos;
    }

    private void verificarFimDeJogo() {
        if (!estadoAtual.getStatusJogo().equals("EM_ANDAMENTO")) {
            btnHit.setEnabled(false);
            btnStand.setEnabled(false);
            btnNovaRodada.setEnabled(true);
            
            // Exibe um pop-up notificando o resultado final da rodada
            JOptionPane.showMessageDialog(this, estadoAtual.getMensagem(), "Fim da Rodada", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Componente customizado interno que desenha uma carta física clássica
    private class ComponenteCarta extends JPanel {
        private String textoCarta;

        public ComponenteCarta(String textoCarta) {
            this.textoCarta = textoCarta;
            setPreferredSize(new Dimension(90, 130)); // Proporção exata de carta de baralho
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Desenha a borda da carta
            g2.setColor(Color.BLACK);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

            if (textoCarta.equals("[CARTA OCULTA]")) {
                // Desenha o fundo azul texturizado clássico do verso da carta
                g2.setColor(new Color(30, 144, 255));
                g2.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 24));
                g2.drawString("?", getWidth() / 2 - 7, getHeight() / 2 + 8);
            } else {
                // Decodifica o valor e o naipe (Ex: "A de ♠")
                String valor = textoCarta.split(" ")[0];
                boolean vermelho = textoCarta.contains("♥") || textoCarta.contains("♦");

                g2.setColor(vermelho ? Color.RED : Color.BLACK);
                g2.setFont(new Font("Arial", Font.BOLD, 18));
                
                // Desenha o valor no canto superior esquerdo
                g2.drawString(valor, 8, 22);

                // Desenha o texto completo centralizado
                g2.setFont(new Font("Arial", Font.PLAIN, 12));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(textoCarta)) / 2;
                g2.drawString(textoCarta, x, getHeight() / 2 + 5);
            }
        }
    }

    public static void main(String[] args) {
        // Garante que a interface rode na Thread de eventos correta do Swing
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClienteGrafico().setVisible(true);
            }
        });
    }
}