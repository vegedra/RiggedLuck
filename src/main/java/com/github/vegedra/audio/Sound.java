package com.github.vegedra.audio;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

/* Uso dentro do jogo:
    Tocar efeito sonoro: Sound.CLICK.play();
    Tocar música: Sound.BG.playMusic();
    Mudar volume: Sound.musicVolume = Sound.Volume.HIGH;
                  Sound.updateMusicVolume();
 */

public enum Sound {

    // Efeitos sonoros
    CLICK("/sounds/hit.wav", false),
    HIT("/sounds/miss.wav", false),
    // Música
    BG1("/sounds/background1.wav", true),
    BG2("/sounds/bg2.wav", true);

    public enum Volume { MUTE, LOW, MEDIUM, HIGH }

    public static Volume soundVolume = Volume.MEDIUM;
    public static Volume musicVolume = Volume.MEDIUM;

    private Clip clip;
    private final boolean isMusic;  // Se vai tocar uma música
    private static Sound currentMusic;

    // Construtor
    Sound(String path, boolean isMusic) {
        this.isMusic = isMusic;

        // Carrega o audio
        try {
            URL url = getClass().getResource(path);
            if (url == null) {
                System.err.println("Áudio não encontrado: " + path);
                return;
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            clip = AudioSystem.getClip();
            clip.open(ais);

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Erro ao carregar som: " + path);
            e.printStackTrace();
        }
    }

    // Efeitos sonoros
    // Toca o efeito sonoro
    public void play() {
        if (isMusic) return;    // Se for música, para
        if (soundVolume == Volume.MUTE || clip == null) return;

        setVolume(soundVolume);
        restart();
    }

    // Música
    public void playMusic() {
        if (!isMusic) return;
        if (musicVolume == Volume.MUTE || clip == null) return;

        // Para a música atual se houver
        if (currentMusic != null && currentMusic != this) {
            currentMusic.stop();
        }

        currentMusic = this;

        setVolume(musicVolume);

        if (clip.isRunning()) clip.stop();
        clip.setFramePosition(0);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    // Para a execução
    public void stop() {
        if (clip == null) return;

        clip.stop();
        clip.setFramePosition(0);
    }

    // Controle de volume
    private void setVolume(Volume volume) {
        if (clip == null) return;
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) return;

        // Ganho em decibel
        FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

        float dB = switch (volume) {
            case LOW -> -25f;
            case MEDIUM -> -15f;
            case HIGH -> -5f;
            case MUTE -> gain.getMinimum();
        };

        gain.setValue(dB);
    }

    // Reinicia
    private void restart() {
        if (clip.isRunning()) clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    // Atualiza volume da música em tempo real
    public static void updateMusicVolume() {
        if (currentMusic != null) {
            currentMusic.setVolume(musicVolume);
        }
    }

    // Para a música
    public static void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic = null;
        }
    }

    // Libera memória ao fechar o jogo
    public static void closeAll() {
        for (Sound s : values()) {
            if (s.clip != null) {
                s.clip.stop();
                s.clip.flush();
                s.clip.close();
            }
        }
    }
}