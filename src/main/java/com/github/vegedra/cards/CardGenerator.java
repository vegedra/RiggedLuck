/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    É proibida a reprodução, distribuição ou venda deste código
    sem a permissão expressa do autor.

    *TODO: Em vez de criar cartas manualmente, consultar a lista criada a partir do Json e selecionar uma com base na raridade e na sorte.
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
