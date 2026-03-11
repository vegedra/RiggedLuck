/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    É proibida a reprodução, distribuição ou venda deste código
    sem a permissão expressa do autor.

    Splash Art
*/

package com.github.vegedra.core;

import javax.swing.*;

public class SplashScreen extends JWindow {

    // Construtor
    public SplashScreen() {
        // Carrega e cria a splash art
        ImageIcon splash = new ImageIcon(getClass().getResource("/images/splash.jpg"));
        JLabel label = new JLabel(splash);
        add(label);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // Fecha depois de 2.5 segundos
        Timer timer = new Timer(2500, e -> {
            dispose();
            // Inicia o jogo
            new Main();
        });
        timer.setRepeats(false);
        timer.start();
    }
}