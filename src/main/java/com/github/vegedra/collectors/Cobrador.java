/*
    Copyright © 2026 [Pedro Ivo Rocha/Digital Cake Studio].
    Todos os direitos reservados.
    All rights reserved.

    É proibida a reprodução, distribuição ou venda deste código
    sem a permissão expressa do autor.

    Classe abstrata base para os inimigos do jogo.
    CobradorFactory instancia os tipos concretos via switch.
*/

package com.github.vegedra.collectors;

import com.github.vegedra.core.Player;

public abstract class Cobrador {

    // Tipos de debuff passivo que um cobrador pode aplicar enquanto ativo
    public enum Debuff {
        NONE,           // Sem efeito extra
        LUCK_DRAIN,     // Drena sorte por segundo
        CLICK_WEAKEN,   // Penaliza o valor de clique (calculado via CobradorManager)
        DOUBLE_DRAIN    // Drenagem de moedas em dobro após X segundos
    }

    // Modo adaptativo: definido no spawn com base na build do jogador
    public enum AdaptiveMode {
        NONE,                // Sem adaptação — comportamento padrão
        CLICK_DRAIN,         // Drena ouro por clique em vez de por segundo (build Clique)
        AMPLIFIED_DRAIN,     // Drenagem por segundo dobrada (build Passiva)
        LUCK_DRAIN_ADAPTIVE, // Drena sorte agressivamente a cada segundo (build Sorte)
        GLASS_CANNON         // Drenagem dobrada, mas morre mais rápido (build Risco)
    }

    // Atributos base — definidos por cada subclasse via super()
    protected final String id;
    protected final String name;
    protected final String description;
    protected final int drainPerSecond;
    protected final int paymentCost;
    protected final int luckPenaltyOnAttack;
    protected final String iconPath;    // Imagem
    protected final Debuff debuff;

    // Recompensas ao enfrentar e pagar — definidas por cada subclasse
    protected int luckOnPay         = 0;   // Sorte ganha ao pagar
    protected int coinRewardOnDefeat = 0;  // Moedas ganhas ao derrotar no combate
    protected int rareCardChance    = 0;   // % de chance de carta rara ao derrotar (0–100)

    // Modo adaptativo — definido pelo CobradorFactory no spawn
    private AdaptiveMode adaptiveMode = AdaptiveMode.NONE;

    // Estado de instância
    protected int secondsActive = 0;
    protected boolean active = true;

    // Construtor
    protected Cobrador(String id, String name, String description,
                       int drainPerSecond, int paymentCost,
                       int luckPenaltyOnAttack, String iconPath, Debuff debuff) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.drainPerSecond = drainPerSecond;
        this.paymentCost = paymentCost;
        this.luckPenaltyOnAttack = luckPenaltyOnAttack;
        this.iconPath = iconPath;
        this.debuff = debuff;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getDrainPerSecond() { return drainPerSecond; }
    public int getPaymentCost() { return paymentCost; }
    public int getLuckPenaltyOnAttack() { return luckPenaltyOnAttack; }
    public String getIconPath() { return iconPath; }
    public Debuff getDebuff() { return debuff; }
    public boolean isActive() { return active; }
    public int getSecondsActive() { return secondsActive; }
    public void tick() { secondsActive++; }     // Incrementa contador de tempo ativo
    public int getLuckOnPay() { return luckOnPay; }
    public int getCoinRewardOnDefeat() { return coinRewardOnDefeat; }
    public int getRareCardChance() { return rareCardChance; }
    public AdaptiveMode getAdaptiveMode() { return adaptiveMode; }
    public void setAdaptiveMode(AdaptiveMode mode) { this.adaptiveMode = mode; };

    // Retorna a drenagem efetiva por segundo, considerando o modo adaptativo (usado pela UI)
    public int getEffectiveDrainPerSecond() {
        switch (adaptiveMode) {
            case AMPLIFIED_DRAIN:
            case GLASS_CANNON:  return drainPerSecond * 2;
            case CLICK_DRAIN:   return 0; // não drena por segundo
            default:            return drainPerSecond;
        }
    }

    /**
     * Percentual das moedas do jogador a drenar por segundo.
     * Aplicado como max(flat, percent) em CobradorManager.tick().
     * Cada subtipo define o seu valor.
     */
    public abstract double getDrainPercent();

    // Retorna true se este cobrador drena por clique em vez de por segundo
    public boolean drainsPerClick() {
        return adaptiveMode == AdaptiveMode.CLICK_DRAIN;
    }

    // Contratos
    // Pagamento, retorna true se o valor bater, desativando o cobrador
    public abstract boolean receberPagamento(int moedasOferecidas);
    // Recebe ataque do jogador, retorna a penalidade e pode ou não desativar o cobrador
    public abstract int receberAtaque();
    // Aplica o debuff passivo no jogador
    public abstract void aplicarDebuff(Player player);
}