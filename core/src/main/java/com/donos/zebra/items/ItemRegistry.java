package com.donos.zebra.items;

import java.util.HashMap;
import java.util.Map;

/**
 * Funciona como o catálogo central de todos os itens existentes no jogo.
 * Garante consistência e centralização na criação das definições de itens.
 */
public class ItemRegistry {

    // Mapa interno para buscar itens dinamicamente pelo ID (útil para sistemas futuros em JSON)
    private static final Map<String, ItemDefinition> REGISTRY = new HashMap<>();

    // --- DECLARAÇÃO ESTÁTICA DOS ITENS (Seu catálogo) ---
    
    public static final ItemDefinition COPPER_ORE;
    public static final ItemDefinition IRON_ORE;
    public static final ItemDefinition IRON_SWORD;
    public static final ItemDefinition HEALTH_POTION;

    // Inicialização do catálogo
    static {
        // 1. Minério de Cobre (Recurso empilhável até 99, sem uso direto)
        COPPER_ORE = new ItemDefinition(
                "copper_ore",
                "Minério de Cobre",
                "Um minério bruto e esverdeado, usado para fundir barras de cobre.",
                ItemType.RESOURCE,
                99,  // maxStackSize
                5,   // valor de venda
                false // não consumível diretamente
        );

        // 2. Minério de Ferro (Recurso empilhável até 99)
        IRON_ORE = new ItemDefinition(
                "iron_ore",
                "Minério de Ferro",
                "Um minério pesado e resistente, essencial para ferramentas avançadas.",
                ItemType.RESOURCE,
                99,
                12,
                false
        );

        // 3. Espada de Ferro (Arma não empilhável, valor alto)
        IRON_SWORD = new ItemDefinition(
                "iron_sword",
                "Espada de Ferro",
                "Uma espada afiada feita de ferro batido. Força +15.",
                ItemType.WEAPON,
                1,   // Armas não acumulam no mesmo slot!
                150,
                false
        );

        // 4. Poção de Vida (Consumível empilhável até 10)
        HEALTH_POTION = new ItemDefinition(
                "health_potion",
                "Poção de Vida",
                "Recupera 50 pontos de vida instantaneamente.",
                ItemType.CONSUMABLE,
                10,  // Limite menor para poções
                25,
                true // É consumível!
        );

        // Registrando todos no mapa interno automaticamente
        register(COPPER_ORE);
        register(IRON_ORE);
        register(IRON_SWORD);
        register(HEALTH_POTION);
    }

    /**
     * Auxiliar interno para mapear o item pelo ID.
     */
    private static void register(ItemDefinition item) {
        REGISTRY.put(item.getId(), item);
    }

    /**
     * Busca uma definição de item a partir do seu ID único.
     * Muito útil para quando o jogo carregar dados salvos ou comandos de console.
     * * @param id O ID do item (ex: "copper_ore").
     * @return A definição do item ou null se não for encontrado.
     */
    public static ItemDefinition getItem(String id) {
        return REGISTRY.get(id);
    }
}