package com.donos.zebra.entities;

import com.badlogic.gdx.math.Vector2;
import com.donos.zebra.items.ItemStack;
import java.util.ArrayList;
import java.util.List;

public abstract class Enemy implements Entity {
    protected float x, y;
    protected float maxHealth;
    protected float currentHealth;
    protected boolean isDead = false;
    
    //Controle de Loot e Estado de Saque
    protected boolean isLooted = false;
    protected final List<ItemStack> lootTable = new ArrayList<>();

    protected float speed;
    protected float aggroRange;

    public Enemy(float x, float y, float maxHealth, float speed, float aggroRange) {
        this.x = x;
        this.y = y;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.speed = speed;
        this.aggroRange = aggroRange;
    }

    // IA Simples para seguir o jogador na sua direção
    protected void chasePlayer(Player player, float delta) {
        if (isDead || player.isDead()) return;

        float distance = Vector2.dst(this.x, this.y, player.getX(), player.getY());
        
        // Persegue se estiver no alcance de detecção, mas não colado demais
        if (distance <= aggroRange && distance > 8f) { 
            float dirX = player.getX() - this.x;
            float dirY = player.getY() - this.y;
            
            // Normaliza o vetor para manter a velocidade uniforme em qualquer ângulo
            float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
            if (length > 0) {
                dirX /= length;
                dirY /= length;
            }

            // Atualiza a posição do monstro
            this.x += dirX * speed * delta;
            this.y += dirY * speed * delta;
            
            if (getHitbox() != null) {
                getHitbox().setPosition(this.x, this.y);
            }
        }
    }

    @Override
    public void takeDamage(float amount) {
        if (isDead) return;
        currentHealth -= amount;
        System.out.println("Monstro tomou dano! Vida atual: " + currentHealth + "/" + maxHealth);
        if (currentHealth <= 0) {
            currentHealth = 0;
            isDead = true;
        }
    }

    /**
     * Verifica se o corpo do inimigo está elegível para ser looteado.
     * Um inimigo pode ser saqueado apenas se estiver morto, não tiver sido looteado ainda
     * e possuir itens em seu inventário interno.
     */
    public boolean hasLootAvailable() {
        return isDead && !isLooted && !lootTable.isEmpty();
    }

    /**
     * Retorna a lista de itens contida no corpo do inimigo.
     */
    public List<ItemStack> getLootTable() {
        return lootTable;
    }

    /**
     * Marca o corpo do inimigo como looteado e limpa seus itens internos.
     * Garante que o loot exista apenas uma única vez no mundo.
     */
    public void clearLoot() {
        this.lootTable.clear();
        this.isLooted = true;
        System.out.println("O corpo do inimigo foi completamente saqueado e esvaziado.");
    }

    /**
     * Verifica se o inimigo já foi completamente limpo pelo jogador.
     */
    public boolean isLooted() {
        return isLooted;
    }

    @Override
    public boolean isDead() { return isDead; }

    @Override
    public float getX() { return x; }

    @Override
    public float getY() { return y; }

    @Override
    public float getCurrentHealth() { return currentHealth; }

    public float getMaxHealth() { 
        return maxHealth; 
    }
}