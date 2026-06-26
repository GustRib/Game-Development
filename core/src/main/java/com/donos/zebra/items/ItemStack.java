package com.donos.zebra.items;

/**
 * Representa uma pilha real de itens no inventário do jogador.
 * Une uma definição imutável (o que é o item) com uma quantidade mutável (quantos existem).
 */
public class ItemStack {

    private final ItemDefinition definition; // Referência para a "receita" do item
    private int quantity;                    // Quantidade atual nesta pilha

    /**
     * Cria uma pilha de itens com uma quantidade inicial.
     * * @param definition A definição do item.
     * @param quantity A quantidade inicial (será limitada ao maxStackSize do item).
     */
    public ItemStack(ItemDefinition definition, int quantity) {
        this.definition = definition;
        this.quantity = Math.max(1, Math.min(quantity, definition.getMaxStackSize()));
    }

    /**
     * Cria uma pilha contendo apenas 1 unidade do item.
     * Muito útil para quando o jogador coleta um item individual do chão.
     */
    public ItemStack(ItemDefinition definition) {
        this(definition, 1);
    }

    // --- MÉTODOS DE LOGICA DE NEGÓCIO ---

    /**
     * Verifica se ainda há espaço para adicionar mais itens nesta pilha.
     */
    public boolean isFull() {
        return quantity >= definition.getMaxStackSize();
    }

    /**
     * Calcula quanto espaço ainda resta nesta pilha antes de atingir o limite.
     */
    public int getRemainingSpace() {
        return definition.getMaxStackSize() - quantity;
    }

    /**
     * Adiciona uma quantidade de itens à pilha, respeitando o limite máximo.
     * * @param amount Quantidade a ser adicionada.
     * @return O que sobrou (excedente) que não coube nesta pilha.
     */
    public int add(int amount) {
        int spaceLeft = getRemainingSpace();
        
        if (amount <= spaceLeft) {
            this.quantity += amount;
            return 0; // Tudo coube na pilha
        } else {
            this.quantity = definition.getMaxStackSize();
            return amount - spaceLeft; // Retorna o que sobrou para o inventário criar outra pilha
        }
    }

    /**
     * Remove uma quantidade de itens da pilha.
     * * @param amount Quantidade a ser removida.
     * @return true se a remoção foi bem-sucedida, false se tentou remover mais do que existia.
     */
    public boolean remove(int amount) {
        if (amount <= this.quantity) {
            this.quantity -= amount;
            return true;
        }
        return false; // Não há itens suficientes nesta pilha
    }

    /**
     * Verifica se a pilha ficou vazia (quantidade igual a zero), 
     * indicando que ela deve ser removida do inventário.
     */
    public boolean isEmpty() {
        return quantity <= 0;
    }

    // --- GETTERS ---

    public ItemDefinition getDefinition() {
        return definition;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return "ItemStack{" +
                "item=" + definition.getName() +
                ", quantity=" + quantity +
                '/' + definition.getMaxStackSize() +
                '}';
    }
}
