// Jogo clicker
// @author: Pedro Ivo (digitalcakestudio)

package com.github.vegedra;

import java.util.Random;

public class Slot {
    // Emojis de cartas no início da classe
    static final String[] CARD_SYMBOLS = {"♠\uFE0F", "♥\uFE0F", "♦\uFE0F", "♣\uFE0F", "⭐"};

    // Tela de apostar
    public static void tela() {
        final int[] OPCOES_APOSTA = {10, 25, 50, 100};

        System.out.println("*************************");
        System.out.println("Simbolos: " + String.join(" ", CARD_SYMBOLS));
        System.out.println("*************************");

        while (Main.getMoedas() > 0) {
            System.out.println("Moedas: $" + Main.getMoedas());
            System.out.println("Escolha sua aposta:");
            for (int i = 0; i < OPCOES_APOSTA.length; i++) {
                System.out.println("[" + (i+1) + "] " + OPCOES_APOSTA[i] + " moedas");
            }
            System.out.println("[0] Voltar ao menu");
            System.out.print("> ");

            int escolha = InputManager.getIntInput();
            InputManager.scanner.nextLine();

            if (escolha == 0) {
                System.out.println("Voltando ao menu...");
                break;
            }
            if (escolha < 1 || escolha > OPCOES_APOSTA.length) {
                System.out.println("Opção inválida!");
                continue;
            }
            int bet = OPCOES_APOSTA[escolha - 1];

            if (bet > Main.getMoedas()) {
                System.out.println("Sem moedas...");
                continue;
            }
            Main.addMoedas(-bet);

            // Aposta
            System.out.println("Rolando...");
            String[] row = spinRow();
            printRow(row);
            int payout = getPayout(row, bet);

            // Ganhou
            if (payout > 0) {
                System.out.println("Você ganhou " + payout + " moedas!");
                Main.addMoedas(payout);
            } else {
                System.out.println("Você perdeu!");
            }

            // Replay?
            System.out.print("Jogar novamente? (S/N): ");
            String playAgain = InputManager.scanner.nextLine().toUpperCase();

            if (!playAgain.equals("S")) {
                break;
            }
        }
        System.out.println("GAME OVER! Moedas: " + Main.getMoedas());
    }

    // Rolagem da aposta
    static String[] spinRow() {
        String[] row = new String[3];
        Random random = new Random();

        for (int i = 0; i < 3; i++) {
            row[i] = CARD_SYMBOLS[random.nextInt(CARD_SYMBOLS.length)];
        }

        return row;
    }

    static void printRow(String[] row) {
        System.out.println("**************");
        System.out.println(" " + String.join(" | ", row));
        System.out.println("**************");
    }

    static int getPayout(String[] row, int bet) {
        // Três símbolos iguais
        if (row[0].equals(row[1]) && row[1].equals(row[2])) {
            switch (row[0]) {
                case "♠\uFE0F": return bet * 3;
                case "♥\uFE0F": return bet * 4;
                case "♦\uFE0F": return bet * 5;
                case "♣\uFE0F": return bet * 10;
                case "⭐":     return bet * 20;
                default: return 0;
            }
        }

        // Dois primeiros iguais
        else if (row[0].equals(row[1])) {
            switch (row[0]) {
                case "♠\uFE0F": return bet * 1;
                case "♥\uFE0F": return bet * 2;
                case "♦\uFE0F": return bet * 3;
                case "♣\uFE0F": return bet * 5;
                case "⭐":     return bet * 10;
                default: return 0;
            }
        }

        // Dois últimos iguais
        else if (row[1].equals(row[2])) {
            switch (row[1]) {
                case "♠\uFE0F": return bet * 1;
                case "♥\uFE0F": return bet * 2;
                case "♦\uFE0F": return bet * 3;
                case "♣\uFE0F": return bet * 5;
                case "⭐":     return bet * 10;
                default: return 0;
            }
        }

        return 0;
    }
}