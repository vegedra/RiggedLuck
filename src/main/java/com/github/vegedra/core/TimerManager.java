/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    É proibida a reprodução, distribuição ou venda deste código
    sem a permissão expressa do autor.

    Gerenciador dos timers do jogo
*/

package com.github.vegedra.core;

import com.github.vegedra.cards.Card;
import com.github.vegedra.cards.CardManager;

import javax.swing.*;

public class TimerManager {

    // Variáveis e objetos
    private final Player player;
    //private final CardManager cm;
    private final GameManager gm;

    private Timer timer;
    private int secondsElapsed = 0;
    private int clicksThisSecond = 0;

    /*
    public TimerManager(Player player, CardManager cardManager, GameManager gm) {
        this.player = player;
        this.cm = cardManager;
        this.gm = gm;
    }
    Usa quando fizer o CardManager
     */

    // Construtor
    public TimerManager(Player player, GameManager gm) {
        this.player = player;
        this.gm = gm;
    }

    // Getter
    public int getSecondsElapsed() { return secondsElapsed; }
    public boolean isRunning() {
        return timer != null && timer.isRunning();
    }

    // Setters
    public void incrementClicksThisSecond() { this.clicksThisSecond++; }

    // Timer principal
    public void startTimer() {
        timer = new Timer(1000, e -> {
            if (gm.state == GameManager.GameState.GAME_OVER) { timer.stop(); return; }
            secondsElapsed++;

            // Geração passiva de ouro
            player.changeCoins(gm.computeTotalCPS());

            // Sorte passiva
            player.changeLuck(Math.round(gm.computeTotalLuckPerSecond()));

            // Oscilação + tendência natural da sorte
            updateLuck(clicksThisSecond);
            clicksThisSecond = 0;

            gm.updateRollCost();
            gm.updateCounter();
            gm.updateCPSLabel();
            gm.updateLuckLabel();
            // TODO: checkCollectorSpawn();

            // Dobra moedas passivas a cada 60s
            if (secondsElapsed % 60 == 0 && secondsElapsed > 0) {
                gm.activateJulgamento();
            }
            gm.checkGameOver();
        });
        timer.start();
    }

    // Oscilação da sorte (chamado a cada segundo, com cliques no período)
    private void updateLuck(int clicksInSecond) {
        // Tendência negativa cresce com o tempo
        float minutes = secondsElapsed / 60f;
        float tendency = 0.5f + minutes * 0.15f;
        tendency = Math.min(tendency, 3.5f);

        // Oscilação aleatória crescente
        float oscBase = 2f + minutes * 0.4f;
        oscBase = Math.min(oscBase, 12f);
        if (gm.hasCard("o_diabo"))  oscBase *= 2;
        float variation = (float)(Math.random() * oscBase * 2) - oscBase;

        // Bônus por clique (inclui bônus das cartas)
        float clickBonus = 0.2f; // base
        for (Card c : gm.getActiveCards()) {
            if (c != null) clickBonus += c.luckPerClick;
        }

        float delta = variation - tendency + (clickBonus * clicksInSecond);
        player.changeLuck(Math.round(delta));
        gm.updateLuckLabel();
    }

    // Pausar e continuar jogo
    public void pauseGame() {
        if (gm.state == GameManager.GameState.GAME) {
            gm.state = GameManager.GameState.PAUSED;
            stopGameTimers();           // Para o timer principal e o de mensagens
            gm.showPauseScreen();        // Mostra a tela de pausa
        }
    }
    public void resumeGame() {
        if (gm.state == GameManager.GameState.PAUSED) {
            gm.state = GameManager.GameState.GAME;
            startTimer();               // Reinicia o timer principal (cria um novo)
            gm.showGamePanel();         // Volta para o painel do jogo (sem reintrodução)
        }
    }

    // Reseta os contadores
    public void resetCounters() {
        secondsElapsed   = 0;
        clicksThisSecond = 0;
    }

    // Parar os timers
    public void stopGameTimers() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }
}
