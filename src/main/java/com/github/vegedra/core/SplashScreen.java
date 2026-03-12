/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    Splash Art
*/

package com.github.vegedra.core;

import com.github.vegedra.cards.CardLoader;

import javax.swing.*;

public class SplashScreen extends JWindow {

    // Tela de logo da Digital Cake Studio - carrega o jogo enquanto isso
    public SplashScreen() {
        ImageIcon splash = new ImageIcon(getClass().getResource("/images/splash.jpg"));
        JLabel label = new JLabel(splash);
        add(label);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // Pré-carrega as cartas em background enquanto a splash é exibida
        new Thread(() -> {
            CardLoader.loadAll();
            System.out.println("[SplashScreen] Cartas pré-carregadas.");
        }).start();

        // Fecha depois de 2.5 segundos
        Timer timer = new Timer(2500, e -> {
            dispose();
            new Main();
        });
        timer.setRepeats(false);
        timer.start();
    }
}