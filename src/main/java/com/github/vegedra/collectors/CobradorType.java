/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    É proibida a reprodução, distribuição ou venda deste código
    sem a permissão expressa do autor.

    Enumeração dos tipos de cobrador disponíveis.
    Usado pela CobradorFactory para instanciar o tipo correto.
*/

package com.github.vegedra.collectors;

public enum CobradorType {
    BASICO,       // Comum, drenagem leve, sem debuff
    AGRESSOR,     // Drena mais moedas e sorte passivamente
    TENAZ,        // Resistente a ataques, enfraquece cliques
    AMALDICOADO   // Raro, drenagem pesada, sorte em queda livre
}