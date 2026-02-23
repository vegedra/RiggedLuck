// Jogo clicker
// @author: Pedro Ivo (digitalcakestudio)

package com.github.vegedra;

public class AFKManager {
    // Variaveis - long é int só que 64 bits
    private static long lastCheckTime = System.currentTimeMillis();
    private static int afkRate = 1;     // 1 moeda a cada 10 seg
    private static final int INTERVALO_SEGUNDOS = 10;

    // Verifica se pode ganhar as moedas AFK
    public static void checkAFKRewards() {
        long currentTime = System.currentTimeMillis();
        long elapsedSeconds = (currentTime - lastCheckTime) / 1000;

        // Se passou os x segundos
        if (elapsedSeconds >= INTERVALO_SEGUNDOS) {
            int ciclos = (int) (elapsedSeconds / INTERVALO_SEGUNDOS);
            int moedasGanhas = ciclos * afkRate;

            if (moedasGanhas > 0) {
                Main.addMoedas(moedasGanhas);
                System.out.println("+" + moedasGanhas + " moedas AFK!");
            }
            lastCheckTime = currentTime;
        }
    }

    // Getters e Setters
    public static int getAFKRate() { return afkRate; }
    public static void setAFKRate(int rate) { afkRate = rate; }
}
