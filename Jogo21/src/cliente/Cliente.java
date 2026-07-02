package cliente;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import interfaces.EstadoJogo;
import interfaces.ServicoJogo21;

public class Cliente {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099); // obtem uma referencia do registro 

            ServicoJogo21 servico = (ServicoJogo21) registry.lookup("ServicoJogo21"); // define os metodos q o usauario pode chamar

            Scanner scanner = new Scanner(System.in);
            System.out.println("===============================");
            System.out.println("      BEM-VINDO AO JOGO 21     ");
            System.out.println("===============================");
            String nome = "";
            boolean conectado = false;
            
            // nao pode haver nomes iguais ou vazios
            while (!conectado) {
                System.out.print("Digite seu nome: ");
                nome = scanner.nextLine().trim();
          
                if (nome.isEmpty()) {
                    System.out.println("[AVISO] O nome não pode ser vazio!");
                    continue;
                }

                if (servico.verificarNomeDisponivel(nome)) {
                    conectado = true;
                } else {
                    System.out.println("[ERRO] Este nome já está em uso no servidor. Escolha outro.");
                }
            }

            // Loop principal do jogo
            while (true) {
                int saldoDisponivel = servico.obterSaldo(nome);
                System.out.println("\nSeu saldo atual: R$ " + saldoDisponivel);

                // Se o saldo zerar, impede o loop de continuar
                if (saldoDisponivel <= 0) {
                    System.out.println("Você faliu! O jogo acabou para você.");
                    break;
                }

                int aposta = 0;
                while (true) {
                    System.out.print("Quanto deseja apostar para esta rodada? R$ ");
                    try {
                        aposta = Integer.parseInt(scanner.nextLine());
                        if (aposta > 0 && aposta <= saldoDisponivel) {
                            break; // Aposta válida, sai do loop de validação
                        }
                        System.out.println("[AVISO] Valor inválido! Escolha um valor entre R$ 1 e R$ " + saldoDisponivel);
                    } catch (NumberFormatException e) {
                        System.out.println("[AVISO] Por favor, digite um número inteiro válido.");
                    }
                }

                System.out.println("\nIniciando nova rodada...");
                
                EstadoJogo estado = servico.iniciarRodada(nome, aposta);
                System.out.println("\n" + estado);

                while (estado.getStatusJogo().equals("EM_ANDAMENTO")) {
                    System.out.println("\nO que você deseja fazer?");
                    System.out.println("1 - Pedir carta (Hit)");
                    System.out.println("2 - Parar (Stand)");
                    System.out.print("Sua escolha: ");

                    String opcao = scanner.nextLine();

                    if (opcao.equals("1")) {
                        estado = servico.pedirCarta(nome);
                        System.out.println("\n" + estado);
                    } else if (opcao.equals("2")) {
                        estado = servico.parar(nome);
                        System.out.println("\n" + estado);
                    } else {
                        System.out.println("Opção inválida! Digite 1 ou 2.");
                    }
                }

                System.out.print("\nDeseja jogar outra rodada? (S/N): ");
                String jogarNovamente = scanner.nextLine();
                if (!jogarNovamente.equalsIgnoreCase("S")) {
                    System.out.println("Obrigado por jogar, " + nome + "! Até a próxima.");
                    break;
                }
            }
            scanner.close();

        } catch (Exception e) {
            System.err.println("[CLIENTE] Ocorreu um erro de conexão:");
            e.printStackTrace();
        }
    }
}