package interfaces;

import java.io.Serializable;
import java.util.List;

public class EstadoJogo implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<String> cartasJogador; //lista de cartas do jogador
    private int pontuacaoJogador;
    private List<String> cartasVisiveisDealer;
    private String statusJogo;// Status "EM_ANDAMENTO", "VITORIA", "DERROTA", "EMPATE"
    private String mensagem; // Para enviar recados
    private int saldoAtual;
    private int apostaAtual;
    public EstadoJogo(List<String> cartasJogador, int pontuacaoJogador, List<String> cartasVisiveisDealer, String statusJogo, String mensagem, int saldoAtual, int apostaAtual) {
        this.cartasJogador = cartasJogador;
        this.pontuacaoJogador = pontuacaoJogador;
        this.cartasVisiveisDealer = cartasVisiveisDealer;
        this.statusJogo = statusJogo;
        this.mensagem = mensagem;
        this.saldoAtual = saldoAtual;
        this.apostaAtual = apostaAtual;
    }

    public List<String> getCartasJogador() { 
    	return cartasJogador; 
    }
    public int getPontuacaoJogador() { 
    	return pontuacaoJogador; 
    }
    public List<String> getCartasVisiveisDealer() { 
    	return cartasVisiveisDealer;
    }
    public String getStatusJogo() { 
    	return statusJogo; 
    }
    public String getMensagem() { 
    	return mensagem; 
    }
    public int getSaldoAtual() {
    	return saldoAtual;
    }
    public int getApostaAtual() {
    	return apostaAtual;
    }

    @Override
    public String toString() {
        return "=== MESA (Aposta: R$" + apostaAtual + "| Saldo: R$ " + saldoAtual + ") ===\n" +
                "Suas Cartas: " + cartasJogador + " (Pontos: " + pontuacaoJogador + ")\n" +
                "Cartas Visíveis do Dealer: " + cartasVisiveisDealer + "\n" +
                "Status: " + statusJogo + " - " + mensagem + "\n" +
                "============";
    }
}