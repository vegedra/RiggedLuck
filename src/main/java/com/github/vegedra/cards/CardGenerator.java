/*
    Rigged Luck Copyright (C) Pedro Ivo Rocha de Deus / Digital Cake Studio - All Rights Reserved

    This source code is protected under international copyright law.  
    All rights reserved and protected by the copyright holders.
    This file is confidential and only available to authorized individuals with the
    permission of the copyright holders.  If you encounter this file and do not have
    permission, please contact the copyright holders and delete this file.

    Card Generator File
*/

package com.github.vegedra.cards;

import java.util.Random;

public class CardGenerator {

    Random random = new Random();

    // Gerar cartas para a roleta de acordo com Sorte atual
    public Card generateCard(int luck) {

        int roll = random.nextInt(100);

        // Sorte influencia
        roll += luck / 5;

        // Carta fraca - TODO: Criar os tipos inspirados em tarot
        if (roll < 30) {
            return createWeakCard();
        }
        else if (roll < 70) {
            return createNormalCard();
        }
        else {
            return createStrongCard();
        }
    }

    // Criar cartas fracas - TODO: Adicionar mais cartas e sortear uma
    private Card createWeakCard() {
        return new Card(
                "weak_click",
                "Carta Fraca",
                "Gera 1 moeda por clique.",
                1,
                0
        );
    }

    // Criar cartas normais
    private Card createNormalCard() {
        return new Card(
                "normal_generator",
                "Moeda Viva",
                "Gera 2 moedas passivamente.",
                0,
                2
        );
    }

    // Criar carta forte
    private Card createStrongCard() {
        return new Card(
                "strong_click",
                "Bênção Dourada",
                "Gera 5 moedas por clique.",
                5,
                0
        );
    }
}
