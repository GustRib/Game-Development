package com.donos.zebra.ui;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;
import com.donos.zebra.items.Inventory; 
import com.donos.zebra.items.ItemDefinition;

public class InventoryUI extends Table {

    private final DragAndDrop dragAndDrop;
    private final AssetManager assetManager;
    private final Inventory backendInventory;
    private final Skin skin; // Guardamos uma referência da skin para o ícone de arrasto

    public InventoryUI(Inventory backendInventory, Skin skin, AssetManager assetManager) {
        this.backendInventory = backendInventory;
        this.skin = skin;
        this.assetManager = assetManager;
        this.dragAndDrop = new DragAndDrop();

        // Remove a linha antiga this.setSize(200, 200); se ainda lá estiver.
        
        this.pad(10);
        this.top().left(); // Alinha o conteúdo interno da tabela ao topo esquerdo
        this.setVisible(false); 

        int totalSlots = 16; 
        for (int i = 0; i < totalSlots; i++) {
            InventorySlotActor slotActor = new InventorySlotActor(i, skin, assetManager);
            configurarDragAndDrop(slotActor);

            // Define um tamanho fixo idêntico ao tamanho da textura criada na Skin (40x40)
            this.add(slotActor).size(40, 40).pad(2);
            
            if ((i + 1) % 4 == 0) {
                this.row();
            }
        }
        
        // Força a tabela a calcular o seu tamanho real exato com base nos slots adicionados
        this.pack(); 
    }

    public void refresh() {
        for (com.badlogic.gdx.scenes.scene2d.Actor actor : this.getChildren()) {
            if (actor instanceof InventorySlotActor) {
                InventorySlotActor slotActor = (InventorySlotActor) actor;
                int index = slotActor.getSlotIndex();
                
                com.donos.zebra.items.ItemStack stack = backendInventory.getStackAt(index);
                
                if (stack != null) {
                    slotActor.setItem(stack.getDefinition());
                } else {
                    slotActor.setItem(null);
                }
            }
        }
    }

    private void configurarDragAndDrop(InventorySlotActor slot) {
        // --- 1. ORIGEM ---
        dragAndDrop.addSource(new Source(slot) {
            @Override
            public Payload dragStart(InputEvent event, float x, float y, int pointer) {
                InventorySlotActor actor = (InventorySlotActor) getActor();
                if (actor.isEmpty()) return null; 

                Payload payload = new Payload();
                payload.setObject(actor); 

                // Passando a skin para o ator de arrasto renderizar o fundo corretamente
                InventorySlotActor dragActor = new InventorySlotActor(actor.getSlotIndex(), skin, assetManager);
                dragActor.setItem(actor.getItem());
                dragActor.setSize(actor.getWidth(), actor.getHeight());
                payload.setDragActor(dragActor);

                return payload;
            }
        });

        // --- 2. ALVO ---
        dragAndDrop.addTarget(new Target(slot) {
            @Override
            public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
                return true; 
            }

            @Override
            public void drop(Source source, Payload payload, float x, float y, int pointer) {
                InventorySlotActor slotOrigem = (InventorySlotActor) payload.getObject();
                InventorySlotActor slotDestino = (InventorySlotActor) getActor();

                if (slotOrigem.getSlotIndex() == slotDestino.getSlotIndex()) return;

                // 1. Modifica no backend
                backendInventory.swapSlots(slotOrigem.getSlotIndex(), slotDestino.getSlotIndex());

                // 2. Atualiza a UI
                refresh();
            }
        }); // Chave de fechamento do Target corrigida!
    }

    /**
     * Método estático para gerar as texturas dos slots via código.
     */
    /**
     * Método estático para gerar as texturas dos slots e estilos de texto via código.
     */
    public static Skin createDefaultSkin(BitmapFont font) {
        Skin skin = new Skin();

        // ---- 1. REGISTRA A FONTE E O ESTILO DE LABEL (TEXTO) ----
        // Isso resolve o erro da LootUI e DialogueUI que precisam exibir textos!
        skin.add("default", font);
        
        com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle labelStyle = 
            new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);

        // ---- 2. ESTILO DOS SLOTS (CÓDIGO QUE JÁ REFEZ) ----
        // Slot Normal: Fundo escuro com borda cinza
        Pixmap pixmapNormal = new Pixmap(40, 40, Pixmap.Format.RGBA8888);
        pixmapNormal.setColor(new Color(0.2f, 0.2f, 0.2f, 0.8f));
        pixmapNormal.fill();
        pixmapNormal.setColor(Color.GRAY);
        pixmapNormal.drawRectangle(0, 0, 40, 40);
        Texture textureNormal = new Texture(pixmapNormal);
        pixmapNormal.dispose();

        // Slot Hover: Fundo mais claro com borda branca ao passar o mouse
        Pixmap pixmapHover = new Pixmap(40, 40, Pixmap.Format.RGBA8888);
        pixmapHover.setColor(new Color(0.3f, 0.3f, 0.3f, 0.9f));
        pixmapHover.fill();
        pixmapHover.setColor(Color.WHITE);
        pixmapHover.drawRectangle(0, 0, 40, 40);
        Texture textureHover = new Texture(pixmapHover);
        pixmapHover.dispose();

        com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle slotStyle = new com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle();
        slotStyle.up = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(textureNormal);
        slotStyle.over = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(textureHover);

        skin.add("slot-style", slotStyle);
        return skin;
    }
}