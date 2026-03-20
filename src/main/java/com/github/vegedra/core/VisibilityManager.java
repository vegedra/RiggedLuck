/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    É proibida a reprodução, distribuição ou venda deste código
    sem a permissão expressa do autor.

    Gerenciador de visibilidade das telas/estados
*/

package com.github.vegedra.core;

import com.github.vegedra.audio.Sound;

import javax.swing.*;

public class VisibilityManager {

    // Variaveis e objetos
    private UI ui;
    private GameManager gameManager;
    private final TimerManager timerManager;

    // Construtor
    public VisibilityManager(UI userInterface, GameManager gm, TimerManager tm) {
        ui = userInterface;
        gameManager = gm;
        timerManager = tm;
    }

    // Mostra a tela do menu inicial
    public void showTitleScreen() {
        Sound.BG1.playMusic();
        ui.switchTo("title");

        if (gameManager != null) {
            gameManager.state = GameManager.GameState.TITLE;
        }
    }

    // Mostra a tela de jogo
    public void showGameScreen() {
        Sound.BG2.playMusic();
        ui.switchTo("game");    // CardLayout shows game panel

        // Mostra a mensagem inicial
        if (gameManager != null && !gameManager.firstTimePlaying) {
            // Usa o modo de jogo atual
            ui.introMessage(gameManager.currentGameMode);
            gameManager.firstTimePlaying = true;
        }

        // Começa o timer
        if (gameManager != null && !timerManager.isRunning()) {
            timerManager.startTimer();
        }

        // Carregar tooltips
        ToolTipManager.sharedInstance().setInitialDelay(200);
        ToolTipManager.sharedInstance().setDismissDelay(10000);

        if (gameManager != null) {
            gameManager.state = GameManager.GameState.GAME;
        }
    }

    // Menu de pausa
    public void showPauseScreen() {
        ui.switchTo("pause");
    }
    public void showGamePanel() {
        ui.switchTo("game");
    }

    // Mostra a tela de game over
    public void showGameOverScreen() {
        // Para o timer
        if (gameManager != null) {
            timerManager.stopGameTimers();
        }

        // Mostra a mensagem de game over
        JOptionPane.showMessageDialog(ui.window,
                "Game Over! Você sobreviveu por " + gameManager.formatTime(timerManager.getSecondsElapsed()) +
                        "\nTotal de moedas ganhas: " + gameManager.player.getTotalCoinsEarned(),
                "Fim de Jogo",
                JOptionPane.INFORMATION_MESSAGE);

        // Retorna para a tela inicial
        showTitleScreen();

        if (gameManager != null) {
            gameManager.state = GameManager.GameState.GAME_OVER;
        }
    }
}