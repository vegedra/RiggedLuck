// Jogo clicker
// @author: Pedro Ivo (digitalcakestudio)

/* TODO:
    Taxa fixa na rolagem (tela que mostra opções - 10, 25, 50, 100)
    Receber melhorias pro clicker por meio das apostas - dependendo da combinação de simbolos recebe algum buff/debuff;
    Controle de RNG via loja e rolagens
    Receber x moedas a cada x segundos (AFK)

- GUI
- Interfaces e refatoração
- Quests
- Loja
 */

package com.github.vegedra;

public class Main {
    // Variaveis
    private static boolean running = true;
    private static int moedas = 1;

    // Getters e Setters
    public static int getMoedas() { return moedas; }
    public static void addMoedas(int quantidade) { moedas += quantidade; }
    public static void setMoedas(int quantidade) { moedas = quantidade; }

    // Melhorar depois
    static final String[] quests = {"Tente conseguir 100 moedas!", "Tente conseguir 200 moedas!",
            "Ganhe 5 rodadas!"};

    public static void main(String[] args) {
        while (running && getMoedas() > 0) {
            AFKManager.checkAFKRewards();
            mostrarMenu();
            processarInput();
        }
        encerrarJogo();
    }

    // Menu Inicial
    private static void mostrarMenu() {
        System.out.println("==============================");
        System.out.println("        JOGO CLICKER");
        System.out.println("          Moedas: " + getMoedas());
        System.out.println("       AFK: " + AFKManager.getAFKRate() + "moeda/10s");
        System.out.println("==============================");
        System.out.println("\nAperte [enter] para gerar moedas");
        System.out.println("digite [a] para apostar");
        System.out.println("digite [q] para sair");
        System.out.print("\n> ");
    }

    private static void processarInput() {
        String input = InputManager.scanner.nextLine().toUpperCase();

        switch (input) {
            case "":
                addMoedas(1);
                System.out.println("+1 moeda!");
            case "A": Slot.tela();
            case "Q": running = false;
            default: System.out.println("Comando inválido! Use ENTER, A ou Q");
        }
    }

    // Game Over
    private static void encerrarJogo() {
        System.out.println("\nObrigado por jogar!");
        System.out.println("Moedas ganhas nessa run: " + getMoedas());
        InputManager.close();
    }
}
