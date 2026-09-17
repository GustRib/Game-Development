package com.donos.zebra.ui;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiPanelStackTest {

    private static final class FakePanel implements UiPanelStack.Panel {
        private boolean open;
        private final List<String> closes;
        private final String name;

        FakePanel(String name, List<String> closes) {
            this.name = name;
            this.closes = closes;
        }

        void open() {
            open = true;
        }

        @Override
        public boolean isPanelOpen() {
            return open;
        }

        @Override
        public void closePanel() {
            open = false;
            closes.add(name);
        }
    }

    @Test
    void escClosesInLifoOrderAcrossInventoryEquipmentAndCrafting() {
        List<String> closes = new ArrayList<>();
        FakePanel inventory = new FakePanel("I", closes);
        FakePanel equipment = new FakePanel("P", closes);
        FakePanel crafting = new FakePanel("F", closes);
        UiPanelStack stack = new UiPanelStack();

        inventory.open();
        stack.push(inventory);
        equipment.open();
        stack.push(equipment);
        crafting.open();
        stack.push(crafting);

        assertTrue(stack.closeTop());
        assertEquals(List.of("F"), closes);
        assertFalse(crafting.isPanelOpen());
        assertTrue(equipment.isPanelOpen());

        assertTrue(stack.closeTop());
        assertEquals(List.of("F", "P"), closes);

        assertTrue(stack.closeTop());
        assertEquals(List.of("F", "P", "I"), closes);
        assertFalse(stack.closeTop());
    }

    @Test
    void closingViaToggleRemovesWithoutLeavingStaleEscEntry() {
        List<String> closes = new ArrayList<>();
        FakePanel inventory = new FakePanel("I", closes);
        FakePanel equipment = new FakePanel("P", closes);
        UiPanelStack stack = new UiPanelStack();

        inventory.open();
        stack.push(inventory);
        equipment.open();
        stack.push(equipment);

        // Simulate X / toggle close on equipment
        equipment.closePanel();
        stack.remove(equipment);

        assertTrue(stack.closeTop());
        assertEquals(List.of("P", "I"), closes);
        assertFalse(stack.hasOpenPanels());
    }

    @Test
    void reopenMovesPanelToTopOfStack() {
        List<String> closes = new ArrayList<>();
        FakePanel inventory = new FakePanel("I", closes);
        FakePanel equipment = new FakePanel("P", closes);
        UiPanelStack stack = new UiPanelStack();

        inventory.open();
        stack.push(inventory);
        equipment.open();
        stack.push(equipment);
        inventory.open();
        stack.push(inventory);

        assertTrue(stack.closeTop());
        assertEquals("I", closes.get(0));
        assertTrue(stack.closeTop());
        assertEquals("P", closes.get(1));
    }
}
