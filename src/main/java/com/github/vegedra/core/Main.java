/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    É proibida a reprodução, distribuição ou venda deste código
    sem a permissão expressa do autor.

    Main
*/

package com.github.vegedra.core;

import com.github.vegedra.audio.Sound;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {

    // Variaveis e objetos
    private static UI ui;
    private static GameManager gameManager;
    private static TimerManager timerManager;
    private Player player = new Player();
    private ClickerHandler cHandler = new ClickerHandler();
    private static VisibilityManager vm;

    public static int gameMode;

    // Inicio
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SplashScreen());
    }

    // Construtor
    public Main() {
        // Passa o player pro UI
        ui = new UI(player);

        // Cria o gameManager
        gameManager = new GameManager(player, ui, cHandler, null);
        ui.setTimerManager(gameManager.getTimerManager());

        UI.gameManager = gameManager;
        timerManager = gameManager.getTimerManager();

        // Cria o visibility manager
        vm = new VisibilityManager(ui, gameManager, gameManager.getTimerManager());

        // Configura o vm no gameManager
        gameManager.setVisibilityManager(vm);

        // Cria a tela e interface
        ui.createUI(cHandler);

        // Carrega a tela inicial
        vm.showTitleScreen();
    }

    
    // Ações e eventos (click)
    public static class ClickerHandler implements ActionListener {
        public void actionPerformed(ActionEvent event) {

            // Pega o evento
            String action = event.getActionCommand();

            // Eventos
            switch (action) {
                // Iniciar jogo - menu inicial
                case "start":
                    // Define o modo de jogo e toca sfx
                    gameMode = ui.gameModeSelect();
                    Sound.CLICK.play();

                    // Reseta o jogo e o inicia, trocando de tela e fazendo as mudanças pro modo de jogo
                    if (gameMode != -1) {
                        gameManager.resetGame();
                        gameManager.configureGameMode(gameMode);
                        vm.showGameScreen();
                    }
                    break;

                // Sair do jogo - menu inicial
                case "exit":
                    int option = JOptionPane.showConfirmDialog(
                            ui.window,
                            "Tem certeza que deseja sair?",
                            "Sair",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (option == JOptionPane.YES_OPTION) {
                        gameManager.shutdown();
                        System.exit(0);
                    }
                    Sound.CLICK.play();
                    break;

                // Clique no circulo - game
                case "clicker":
                    gameManager.handleClick();
                    break;

                // Clique na roleta (nova carta)
                case "roll":
                    gameManager.rollCard();
                    break;

                // Pausou o jogo
                case "pause":
                    timerManager.pauseGame();
                    break;
                case "resume":
                    timerManager.resumeGame();
                    break;
                case "exitToMenu":
                    timerManager.stopGameTimers();
                    ui.switchTo("title");
                    gameManager.state = GameManager.GameState.TITLE;
                    Sound.BG1.playMusic();
                    break;

                case "activate":
                default:
                    // Se usou carta de uso único
                    if (action.startsWith("activate_")) {
                        int index = Integer.parseInt(action.split("_")[1]);
                        gameManager.activateCard(index);
                    }

                    // Descartar cartas
                    if (action.startsWith("discard_")) {
                        int index = Integer.parseInt(action.split("_")[1]);
                        gameManager.handleDiscard(index);
                    }
            }
        }
    }
}
