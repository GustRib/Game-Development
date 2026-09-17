package com.donos.zebra.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * LIFO stack of open UI panels for ESC-to-close ordering.
 */
public final class UiPanelStack {

    public interface Panel {
        boolean isPanelOpen();

        void closePanel();
    }

    private final List<Panel> stack = new ArrayList<>();

    public void push(Panel panel) {
        if (panel == null) {
            return;
        }
        stack.remove(panel);
        stack.add(panel);
    }

    public void remove(Panel panel) {
        stack.remove(panel);
    }

    /** Closes the most recently opened panel still open. */
    public boolean closeTop() {
        while (!stack.isEmpty()) {
            Panel top = stack.remove(stack.size() - 1);
            if (top.isPanelOpen()) {
                top.closePanel();
                return true;
            }
        }
        return false;
    }

    public boolean hasOpenPanels() {
        for (Panel panel : stack) {
            if (panel.isPanelOpen()) {
                return true;
            }
        }
        return false;
    }

    public int size() {
        return stack.size();
    }

    public void clear() {
        stack.clear();
    }
}
