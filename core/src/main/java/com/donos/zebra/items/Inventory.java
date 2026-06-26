package com.donos.zebra.items;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsável por armazenar e gerenciar os ItemStacks do jogador.
 * Centraliza toda a lógica de manipulação de itens, protegendo o estado interno.
 */
public class Inventory {

    private final ItemStack[] slots; // Array fixo representando os slots do inventário

    /**
     * Cria um inventário com um número definido de slots.
     * @param size Quantidade de slots disponíveis (ex: 24, 30).
     */
    public Inventory(int size) {
        this.slots = new ItemStack[size];
    }

    /**
     * Tenta adicionar uma quantidade de um item ao inventário.
     * Procura primeiro por pilhas existentes para agrupar e, se necessário, usa slots vazios.
     * * @param definition O molde do item a ser adicionado.
     * @param amount A quantidade a adicionar.
     * @return true se todos os itens couberam, false se o inventário ficou cheio e sobrou algo.
     */
    public boolean addItem(ItemDefinition definition, int amount) {
        int remaining = amount;

        // 1. Passo: Tentar agrupar em ItemStacks já existentes do mesmo item
        if (definition.getMaxStackSize() > 1) {
            for (int i = 0; i < slots.length; i++) {
                if (slots[i] != null && slots[i].getDefinition().getId().equals(definition.getId())) {
                    remaining = slots[i].add(remaining);
                    if (remaining == 0) return true; // Tudo foi agrupado com sucesso
                }
            }
        }

        // 2. Passo: Se ainda sobrou itens, tentar colocar em slots vazios
        while (remaining > 0) {
            int emptySlot = findEmptySlot();
            if (emptySlot == -1) {
                // Não há mais espaço no inventário!
                // Aqui você poderia fazer o restante dropar no chão do mapa
                return false; 
            }

            // Cria uma nova pilha com o que restou (o construtor do ItemStack já limita ao maxStackSize)
            slots[emptySlot] = new ItemStack(definition, remaining);
            
            // Subtrai o que realmente coube nesse novo slot
            int coube = Math.min(remaining, definition.getMaxStackSize());
            remaining -= coube;
        }

        return true;
    }

    /**
     * Remove uma quantidade específica de um item do inventário, consumindo de várias pilhas se necessário.
     * * @param definition O item a ser removido.
     * @param amount A quantidade a ser removida.
     * @return true se a remoção foi feita com sucesso, false se não havia a quantidade necessária.
     */
    public boolean removeItem(ItemDefinition definition, int amount) {
        if (!hasItemQuantity(definition, amount)) {
            return false; // Não tem o suficiente para remover
        }

        int toRemove = amount;
        // Percorre de trás para a frente ou de frente para trás para consumir as pilhas
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] != null && slots[i].getDefinition().getId().equals(definition.getId())) {
                int currentQty = slots[i].getQuantity();
                
                if (currentQty <= toRemove) {
                    toRemove -= currentQty;
                    slots[i] = null; // Esvazia o slot completamente
                } else {
                    slots[i].remove(toRemove);
                    toRemove = 0;
                }

                if (toRemove == 0) break;
            }
        }
        return true;
    }

    /**
     * Verifica a quantidade total que o jogador possui de um determinado item (somando todas as pilhas).
     */
    public int getItemCount(ItemDefinition definition) {
        int total = 0;
        for (ItemStack slot : slots) {
            if (slot != null && slot.getDefinition().getId().equals(definition.getId())) {
                total += slot.getQuantity();
            }
        }
        return total;
    }

    /**
     * Responde se o jogador possui pelo menos a quantidade solicitada de um item.
     * Ex: "O jogador tem 10 minérios de cobre?"
     */
    public boolean hasItemQuantity(ItemDefinition definition, int amount) {
        return getItemCount(definition) >= amount;
    }

    /**
     * Auxiliar para encontrar o índice do primeiro slot totalmente vazio.
     * @return O índice do slot, ou -1 se o inventário estiver completamente cheio.
     */
    private int findEmptySlot() {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == null) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Retorna uma cópia da lista de itens para leitura (ex: renderizar na UI), 
     * impedindo que classes externas modifiquem o array original diretamente.
     */
    public ItemStack[] getSlots() {
        return slots.clone(); 
    }

    /**
     * Exibe o estado atual do inventário de forma legível no console.
     */
    public void printInventory() {
        System.out.println("=== INVENTÁRIO DO JOGADOR ===");
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] != null) {
                System.out.println("Slot [" + i + "]: " + slots[i].getQuantity() + "x " + slots[i].getDefinition().getName());
            } else {
                System.out.println("Slot [" + i + "]: Vazio");
            }
        }
        System.out.println("=============================");
    }
}
