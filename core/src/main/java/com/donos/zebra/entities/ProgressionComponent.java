package com.donos.zebra.entities;


public class ProgressionComponent {

    private final int maxLevel = 3;
    private int currentLevel;
    private int currentXp;

    private int maxHp;
    private int attackDamage;
    
    // sinalizar à classe Player que houve um Level Up
    private boolean justLeveledUp;

    private final int[] xpNeeded = {0, 100, 250, 0};
    private final int[] hpPool   = {0, 100, 130, 170};
    private final int[] dmgPool  = {0,  10,  14,  20};

    public ProgressionComponent() {
        this.currentLevel = 1;
        this.currentXp = 0;
        this.justLeveledUp = false;
        updateStats(); // Inicializa atributos do Nível 1
    }


    public void addXp(int amount) {

        if (currentLevel >= maxLevel) {
            return;
        }

        currentXp += amount;

        while (currentLevel < maxLevel && currentXp >= xpNeeded[currentLevel]) {
            currentXp -= xpNeeded[currentLevel];
            currentLevel++;
            updateStats();
            justLeveledUp = true;
        }

        if (currentLevel >= maxLevel) {
            currentXp = 0;
        }
    }

    private void updateStats() {
        this.maxHp = hpPool[currentLevel];
        this.attackDamage = dmgPool[currentLevel];
    }

    public boolean checkAndConsumeLevelUp() {
        if (justLeveledUp) {
            justLeveledUp = false;
            return true;
        }
        return false;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public int getCurrentXp() {
        return currentXp;
    }

    public int getXpNeededForNextLevel() {
        if (currentLevel >= maxLevel) return 1; 
        return xpNeeded[currentLevel];
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getAttackDamage() {
        return attackDamage;
    }
    
    public boolean isMaxLevel() {
        return currentLevel >= maxLevel;
    }
}