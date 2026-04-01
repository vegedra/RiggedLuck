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

    // Atributos base — definidos por cada subclasse via super()
    protected final String id;
    protected final String name;
    protected final String description;
    protected final int drainPerSecond;
    protected final int paymentCost;
    protected final int luckPenaltyOnAttack;
    protected final String iconPath;    // Imagem
    protected final Debuff debuff;

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
    // Incrementa contador de tempo ativo
    public void tick() { secondsActive++; }

    // Contratos

    // Pagamento, retorna true se o valor bater, desativando o cobrador
    public abstract boolean receberPagamento(int moedasOferecidas);
    // Recebe ataque do jogador, retorna a penalidade e pode ou não desativar o cobrador
    public abstract int receberAtaque();
    // Aplica o debuff passivo no jogador
    public abstract void aplicarDebuff(Player player);
}