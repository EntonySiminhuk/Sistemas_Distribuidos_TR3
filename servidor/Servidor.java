package servidor;

import interfaces.ServicoJogo21;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Servidor {
    public static void main(String[] args) {
        try {
            ServicoJogo21 servico = new ServicoJogo21Impl();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("ServicoJogo21", servico);  

            System.out.println("[SERVIDOR] Servidor do Jogo 21 rodando na porta 1099. Aguardando jogadores...");

        } catch (Exception e) {
            System.err.println("[SERVIDOR] Erro ao iniciar o servidor:");
            e.printStackTrace();
        }
    }
}