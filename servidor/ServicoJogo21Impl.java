package servidor;

import interfaces.EstadoJogo;
import interfaces.ServicoJogo21;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServicoJogo21Impl extends UnicastRemoteObject implements ServicoJogo21 {

    private Map<String, PartidaInterna> partidasAtivas; // Armazena os dados da partida de cada jogador
    private Map<String, Integer> saldosJogadores = new ConcurrentHashMap<>(); // Armazena o saldo dos jogadores
    private static final long serialVersionUID = 1L;

    public ServicoJogo21Impl() throws RemoteException {
        super(); // Chama o construtor da UnicastRemoteObject
        this.partidasAtivas = new ConcurrentHashMap<>(); 
    }
    
    @Override
    //verifica o se o nome está disponivel para adicionar a lista de jogadores com R$500
    public boolean verificarNomeDisponivel(String nomeJogador) throws RemoteException {
        boolean disponivel = !partidasAtivas.containsKey(nomeJogador);
        if (disponivel && !saldosJogadores.containsKey(nomeJogador)) {
            saldosJogadores.put(nomeJogador, 500); // Saldo inicial de R$ 500
        }
        return disponivel;
    }
    
    @Override
    public int obterSaldo(String nomeJogador) throws RemoteException {
        return saldosJogadores.getOrDefault(nomeJogador, 0);
    }

    @Override
    public EstadoJogo iniciarRodada(String nomeJogador, int valorAposta) throws RemoteException {
        int saldoAtual = saldosJogadores.getOrDefault(nomeJogador, 0);

        if (valorAposta <= 0 || valorAposta > saldoAtual) {
            throw new IllegalArgumentException("Aposta inválida, seu saldo eh R$ " + saldoAtual);
        }

        // Reduz a aposta do saldo do jogador temporariamente
        saldosJogadores.put(nomeJogador, saldoAtual - valorAposta);
        
        PartidaInterna partida = new PartidaInterna(valorAposta);

        // Jogador compra duas cartas
        partida.cartasJogador.add(partida.comprarCarta());
        partida.cartasJogador.add(partida.comprarCarta());

        // Dealer compra duas cartas
        partida.cartasDealer.add(partida.comprarCarta());
        partida.cartasDealer.add(partida.comprarCarta());

        partidasAtivas.put(nomeJogador, partida); 

        return gerarEstado(nomeJogador, partida, "EM_ANDAMENTO", "Rodada iniciada. Sua vez.");
    }

    @Override
    public EstadoJogo pedirCarta(String nomeJogador) throws RemoteException {
        PartidaInterna partida = partidasAtivas.get(nomeJogador);
        if (partida == null) return null;

        partida.cartasJogador.add(partida.comprarCarta());

        int pontosJogador = calcularPontuacao(partida.cartasJogador);

        if (pontosJogador > 21) {
            partidasAtivas.remove(nomeJogador);
            return gerarEstadoCompleto(nomeJogador, partida, "DERROTA", "Você estourou 21.");
        }

        return gerarEstado(nomeJogador, partida, "EM_ANDAMENTO", "Você pediu uma carta.");
    }

    @Override
    public EstadoJogo parar(String nomeJogador) throws RemoteException {
        PartidaInterna partida = partidasAtivas.get(nomeJogador);
        if (partida == null) return null;

        int pontosJogador = calcularPontuacao(partida.cartasJogador);
        int pontosDealer = calcularPontuacao(partida.cartasDealer);
        
        while (pontosDealer < 17) {
            partida.cartasDealer.add(partida.comprarCarta());
            pontosDealer = calcularPontuacao(partida.cartasDealer);
        }

        partidasAtivas.remove(nomeJogador);

        //busca o valor associado ao jogador
        int saldoAtual = saldosJogadores.getOrDefault(nomeJogador, 0);

        if (pontosDealer > 21) {
        	//atualiza o saldo
            saldosJogadores.put(nomeJogador, saldoAtual + (partida.valorAposta * 2));
            return gerarEstadoCompleto(nomeJogador, partida, "VITORIA", "O Dealer estourou! Você venceu.");
        } else if (pontosJogador > pontosDealer) {
            saldosJogadores.put(nomeJogador, saldoAtual + (partida.valorAposta * 2));
            return gerarEstadoCompleto(nomeJogador, partida, "VITORIA", "Sua pontuação foi maior. Você venceu.");
        } else {
            return gerarEstadoCompleto(nomeJogador, partida, "DERROTA", "O Dealer venceu.");
        }
    }

    private int calcularPontuacao(List<String> mao) {
        int pontos = 0;
        int ases = 0;

        for (String carta : mao) {
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

        while (pontos > 21 && ases > 0) {//caso o as estore o valor, ele vale 1
            pontos -= 10;
            ases--;
        }
        return pontos;
    }

    private EstadoJogo gerarEstado(String nomeJogador, PartidaInterna partida, String status, String mensagem) {
        List<String> cartasVisiveisDealer = new ArrayList<>();
        cartasVisiveisDealer.add(partida.cartasDealer.get(0));
        cartasVisiveisDealer.add("[CARTA OCULTA]");

        return new EstadoJogo(
                partida.cartasJogador,
                calcularPontuacao(partida.cartasJogador),
                cartasVisiveisDealer,
                status,
                mensagem,
                saldosJogadores.getOrDefault(nomeJogador, 0), 
                partida.valorAposta
        );
    }

    private EstadoJogo gerarEstadoCompleto(String nomeJogador, PartidaInterna partida, String status, String mensagem) {
        return new EstadoJogo(
                partida.cartasJogador,
                calcularPontuacao(partida.cartasJogador),
                partida.cartasDealer,
                status,
                mensagem + " (Dealer fez " + calcularPontuacao(partida.cartasDealer) + " pontos)",
                saldosJogadores.getOrDefault(nomeJogador, 0),
                partida.valorAposta
        );
    }

    private class PartidaInterna {
        List<String> baralho;
        List<String> cartasJogador;
        List<String> cartasDealer;
        int valorAposta;

        public PartidaInterna(int aposta) {
            cartasJogador = new ArrayList<>();
            cartasDealer = new ArrayList<>();
            baralho = novoBaralho();
            valorAposta = aposta;
        }

        public String comprarCarta() {
            return baralho.remove(0); 
        }

        private List<String> novoBaralho() {
            List<String> novo = new ArrayList<>();
            String[] naipes = {"♠", "♥", "♦", "♣"};
            String[] valores = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
            for (String n : naipes) {
                for (String v : valores) {
                    novo.add(v + " de " + n);
                }
            }
            Collections.shuffle(novo); 
            return novo;
        }
    }
}