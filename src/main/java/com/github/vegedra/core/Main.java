/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    É proibida a reprodução, distribuição ou venda deste código
    sem a permissão expressa do autor.

    Main
*/

package com.github.vegedra.core;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {

    // Variaveis e objetos
    private UI ui;
    private static GameManager gameManager;
    private Player player = new Player();
    private ClickerHandler cHandler = new ClickerHandler();

    // Inicio
    public static void main(String[] args) {
        new Main();
    }

    // Construtor
    public Main() {
        // Passa o player pro UI
        ui = new UI(player);

        // Cria o gameManager
        gameManager = new GameManager(player, ui, cHandler);
        UI.gameManager = gameManager;

        // Cria a tela e interface
        ui.createUI(cHandler);

        // Inicia o timer
        gameManager.startPassiveIncome();

        // Carregar tooltips mais rapido
        ToolTipManager.sharedInstance().setInitialDelay(200);
        ToolTipManager.sharedInstance().setDismissDelay(10000);
    }

    // Ações e eventos (click)
    public static class ClickerHandler implements ActionListener {
        public void actionPerformed(ActionEvent event) {

            // Pega o evento
            String action = event.getActionCommand();

            // Eventos
            switch (action) {
                // Clique no circulo
                case "clicker":
                    gameManager.handleClick();
                    break;

                // Clique na roleta (nova carta)
                case "roll":
                    gameManager.rollCard();
                    break;

                default:
                    // Descartar cartas
                    if (action.startsWith("discard_")) {
                        int index = Integer.parseInt(action.split("_")[1]);
                        gameManager.handleDiscard(index);
                    }
            }
        }
    }
}