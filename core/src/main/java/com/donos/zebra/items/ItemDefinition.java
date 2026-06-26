package com.donos.zebra.items;

/**
 * Representa a "receita" ou "molde" estático de um item.
 * Contém apenas propriedades imutáveis que definem o que o item é.
 */
public class ItemDefinition {
    
    private final String id;             // ID único no sistema (ex: "copper_ore", "iron_sword")
    private final String name;           // Nome exibido para o jogador (ex: "Minério de Cobre")
    private final String description;    // Descrição do item
    private final ItemType type;         // Categoria do item para regras de negócio
    private final int maxStackSize;      // Limite de empilhamento (ex: 99 para recursos, 1 para armas)
    private final int value;             // Valor de compra/venda na loja
    private final boolean isConsumable;  // Define se o item some ao ser usado (ex: poções, comida)
    private final String iconPath;       // NOVA PROPRIEDADE: Caminho do asset visual do ícone (ex: "items/copper_ore.png")

    /**
     * Construtor completo para definir as propriedades fixas do item.
     */
    public ItemDefinition(String id, String name, String description, ItemType type, 
                          int maxStackSize, int value, boolean isConsumable, String iconPath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.maxStackSize = maxStackSize;
        this.value = value;
        this.isConsumable = isConsumable;
        this.iconPath = iconPath;
    }

    // --- GETTERS (Sem Setters, para garantir a imutabilidade) ---

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ItemType getType() {
        return type;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public int getValue() {
        return value;
    }

    public boolean isConsumable() {
        return isConsumable;
    }

    /**
     * @return O caminho interno na pasta de assets para a textura do ícone.
     */
    public String getIconPath() {
        return iconPath;
    }

    @Override
    public String toString() {
        return "ItemDefinition{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                '}';
    }
}