package com.donos.zebra.items;

/**
 * Categoriza os itens do jogo para definir regras de negócio rápidas,
 * como restrições de equipamentos, filtros de inventário e comércio.
 */
public enum ItemType {
    
    WEAPON("Arma"),
    ARMOR("Armadura"),
    ACCESSORY("Acessório"),
    RESOURCE("Recurso"),
    MATERIAL("Material de Forja"),
    CONSUMABLE("Consumível"),
    QUEST_ITEM("Item de Missão");

    private final String displayName;

    /**
     * Construtor do Enum.
     * @param displayName Nome amigável para exibição na interface do jogo.
     */
    ItemType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return O nome formatado da categoria (ex: "Material de Forja").
     */
    public String getDisplayName() {
        return displayName;
    }
}
