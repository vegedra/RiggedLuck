// Jogo clicker
// @author: Pedro Ivo (digitalcakestudio)

package com.github.vegedra;

import java.util.Scanner;

public class InputManager {
    public static final Scanner scanner = new Scanner(System.in);

    // Construtor
    private InputManager() {}

    // Receber inteiros de forma segura
    public static int getIntInput() {
        while (true) {
            try {
                return scanner.nextInt();
            } catch (Exception e) {
                System.out.println("Por favor, digite um número válido!");
                scanner.nextLine(); // limpar buffer
            }
        }
    }

    // Pausa o programa
    public static void pause() {
        System.out.println("\n\nPressione Enter para continuar.");

        // Limpa qualquer Enter que ficou no buffer
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }

        // Aguarda o usuario apertar Enter
        scanner.nextLine();
    }

    // Encerra o scanner
    public static void close() {
        scanner.close();
    }
}