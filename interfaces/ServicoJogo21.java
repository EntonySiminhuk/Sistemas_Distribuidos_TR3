package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
//metodos que podem ser utilizados pelo cliente
public interface ServicoJogo21 extends Remote {
	boolean verificarNomeDisponivel(String nomeJogador) throws RemoteException;
	int obterSaldo(String nomeJogador) throws RemoteException;
    EstadoJogo iniciarRodada(String nomeJogador, int valorAposta) throws RemoteException;
    EstadoJogo pedirCarta(String nomeJogador) throws RemoteException;
    EstadoJogo parar(String nomeJogador) throws RemoteException;
}