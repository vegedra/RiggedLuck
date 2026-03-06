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

    // Constructor
    public VisibilityManager(UI userInterface, GameManager gm) {
        // Para usar a classe UI aqui
        ui = userInterface;
        gameManager = gm;
    }

    // Mostrar a tela inicial
    public void showTitleScreen() {
        // Toca música de fundo
        Sound.BG1.playMusic();

        // Mostra título, esconde game
        ui.titlePanel.setVisible(true);
        ui.gamePanel.setVisible(false);

        // Atualiza o estado do jogo
        if (gameManager != null) {
            gameManager.state = GameManager.GameState.TITLE;
        }
    }

    // Mostrar a tela de jogo
    public void showGameScreen() {
        // Toca música de fundo
        Sound.BG2.playMusic();

        // Esconde título, mostra game
        ui.titlePanel.setVisible(false);
        ui.gamePanel.setVisible(true);

        // Mostra o texto inicial do jogo (apenas na primeira vez)
        if (gameManager != null && !gameManager.firstTimePlaying) {
            SwingUtilities.invokeLater(() -> ui.introMessage());
            gameManager.firstTimePlaying = true;
        }

        // Inicia o timer se ainda não foi iniciado
        if (gameManager != null && gameManager.timer == null) {
            gameManager.startPassiveIncome();
        }

        // Carregar tooltips mais rapido
        ToolTipManager.sharedInstance().setInitialDelay(200);
        ToolTipManager.sharedInstance().setDismissDelay(10000);

        // Atualiza o estado do jogo
        if (gameManager != null) {
            gameManager.state = GameManager.GameState.GAME;
        }
    }

    // Mostrar tela de game over - TODO
    public void showGameOverScreen() {
        // Para o timer
        if (gameManager != null && gameManager.timer != null) {
            gameManager.timer.stop();
        }

        // Mostra mensagem de game over
        JOptionPane.showMessageDialog(ui.window,
                "Game Over! Você sobreviveu por " + gameManager.formatTime(gameManager.secondsElapsed) +
                        "\nTotal de moedas ganhas: " + gameManager.player.getCoins(),
                "Fim de Jogo",
                JOptionPane.INFORMATION_MESSAGE);

        // Volta para a tela inicial
        showTitleScreen();

        // Atualiza o estado
        if (gameManager != null) {
            gameManager.state = GameManager.GameState.GAME_OVER;
        }
    }
}