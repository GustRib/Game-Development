package com.donos.zebra.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.donos.zebra.quests.QuestDefinition;
import com.donos.zebra.quests.QuestInstance;
import com.donos.zebra.quests.QuestLog;
import com.donos.zebra.quests.QuestObjectiveDefinition;
import com.donos.zebra.quests.QuestObjectiveProgress;
import com.donos.zebra.quests.QuestRegistry;
import com.donos.zebra.quests.QuestStatus;

/**
 * Full quest journal (opened with Q or by clicking the tracker).
 */
public class QuestJournalUI extends Table {

    public interface Listener {
        void onClose();

        void onLocate(QuestDefinition quest, QuestObjectiveDefinition objective);
    }

    private final Skin skin;
    private final Table listTable;
    private final Label detailTitle;
    private final Label detailDescription;
    private final Table objectivesTable;
    private final TextButton locateButton;
    private QuestLog questLog;
    private String selectedQuestId;
    private Listener listener;

    public QuestJournalUI(Skin skin) {
        this.skin = skin;
        ensureButtonStyles(skin);
        setFillParent(true);
        setBackground(solid(new Color(0f, 0f, 0f, 0.45f)));
        setVisible(false);
        setTouchable(Touchable.enabled);

        Table panel = new Table();
        panel.setBackground(solid(new Color(0.08f, 0.1f, 0.12f, 0.96f)));
        panel.pad(16);
        panel.defaults().top().left();

        Label header = new Label("QUESTS", skin);
        header.setColor(new Color(0.95f, 0.9f, 0.55f, 1f));

        TextButton closeBtn = new TextButton("X", skin, "window-close");
        closeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                close();
            }
        });

        Table top = new Table();
        top.add(header).expandX().left();
        top.add(closeBtn).size(28, 28).right();

        listTable = new Table();
        listTable.defaults().left().growX().padBottom(4);
        ScrollPane listScroll = new ScrollPane(listTable, skin);
        listScroll.setFadeScrollBars(false);

        detailTitle = new Label("", skin);
        detailTitle.setColor(new Color(0.95f, 0.9f, 0.55f, 1f));
        detailTitle.setWrap(true);
        detailDescription = new Label("", skin);
        detailDescription.setWrap(true);
        objectivesTable = new Table();
        objectivesTable.defaults().left().growX().padBottom(3);

        locateButton = new TextButton("LOCALIZAR", skin, "pause-menu");
        locateButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (listener == null || questLog == null || selectedQuestId == null) {
                    return;
                }
                QuestDefinition def = QuestRegistry.get(selectedQuestId);
                QuestInstance qi = questLog.getInstance(selectedQuestId);
                if (def == null || qi == null) {
                    return;
                }
                int idx = qi.status == QuestStatus.COMPLETED
                    ? def.objectives.size() - 1
                    : qi.currentObjectiveIndex;
                QuestObjectiveDefinition obj = def.getObjective(idx);
                if (obj != null) {
                    listener.onLocate(def, obj);
                }
            }
        });

        Table detail = new Table();
        detail.defaults().left().growX();
        detail.add(detailTitle).width(360).padBottom(8).row();
        detail.add(detailDescription).width(360).padBottom(10).row();
        detail.add(new Label("Objetivos", skin)).padBottom(4).row();
        detail.add(objectivesTable).width(360).padBottom(12).row();
        detail.add(locateButton).left();

        Table body = new Table();
        body.add(listScroll).width(200).height(320).padRight(14);
        body.add(detail).width(380).height(320).top();

        panel.add(top).growX().padBottom(12).row();
        panel.add(body).grow();

        add(panel).center();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void open(QuestLog log, String selectQuestId) {
        this.questLog = log;
        this.selectedQuestId = selectQuestId;
        if (selectedQuestId == null && log != null) {
            selectedQuestId = log.getTrackedQuestId();
        }
        rebuild();
        setVisible(true);
        toFront();
    }

    public void close() {
        setVisible(false);
        if (listener != null) {
            listener.onClose();
        }
    }

    public boolean isOpen() {
        return isVisible();
    }

    public String getSelectedQuestId() {
        return selectedQuestId;
    }

    public void refresh() {
        if (isVisible()) {
            rebuild();
        }
    }

    private void rebuild() {
        listTable.clearChildren();
        if (questLog == null) {
            return;
        }

        listTable.add(sectionLabel("EM ANDAMENTO")).growX().row();
        boolean anyActive = false;
        for (QuestInstance qi : questLog.getActiveQuests()) {
            anyActive = true;
            addQuestButton(qi, false);
        }
        if (!anyActive) {
            listTable.add(muted("(nenhuma)")).padLeft(6).row();
        }

        listTable.add(sectionLabel("CONCLUIDAS")).padTop(10).growX().row();
        boolean anyDone = false;
        for (QuestInstance qi : questLog.getCompletedQuests()) {
            anyDone = true;
            addQuestButton(qi, true);
        }
        if (!anyDone) {
            listTable.add(muted("(nenhuma ainda)")).padLeft(6).row();
        }

        if (selectedQuestId == null) {
            if (!questLog.getActiveQuests().isEmpty()) {
                selectedQuestId = questLog.getActiveQuests().get(0).questId;
            } else if (!questLog.getCompletedQuests().isEmpty()) {
                selectedQuestId = questLog.getCompletedQuests().get(0).questId;
            }
        }
        showDetails(selectedQuestId);
    }

    private void addQuestButton(QuestInstance qi, boolean completed) {
        QuestDefinition def = QuestRegistry.get(qi.questId);
        if (def == null) {
            return;
        }
        String label = (completed ? "✓ " : "") + def.title;
        final String id = qi.questId;
        TextButton btn = new TextButton(label, skin, "pause-menu");
        if (id.equals(selectedQuestId)) {
            btn.setColor(0.95f, 0.9f, 0.55f, 1f);
        }
        btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                selectedQuestId = id;
                rebuild();
            }
        });
        listTable.add(btn).growX().row();
    }

    private void showDetails(String questId) {
        objectivesTable.clearChildren();
        QuestDefinition def = QuestRegistry.get(questId);
        QuestInstance qi = questLog != null ? questLog.getInstance(questId) : null;
        if (def == null || qi == null) {
            detailTitle.setText("");
            detailDescription.setText("Selecione uma quest.");
            locateButton.setDisabled(true);
            return;
        }
        detailTitle.setText(def.title);
        detailDescription.setText(def.description);
        locateButton.setDisabled(qi.status != QuestStatus.ACTIVE);

        for (int i = 0; i < def.objectives.size(); i++) {
            QuestObjectiveDefinition obj = def.getObjective(i);
            QuestObjectiveProgress prog = qi.getProgress(i);
            boolean done = prog != null && prog.completed;
            boolean current = qi.status == QuestStatus.ACTIVE && i == qi.currentObjectiveIndex;
            int cur = prog == null ? 0 : prog.currentAmount;
            String mark = done ? "✓ " : (current ? "▶ " : "○ ");
            String line = mark + obj.description;
            if (obj.requiredAmount > 1) {
                line += "  " + cur + "/" + obj.requiredAmount;
            }
            Label row = new Label(line, skin);
            if (done) {
                row.setColor(new Color(0.55f, 0.9f, 0.6f, 1f));
            } else if (current) {
                row.setColor(Color.WHITE);
            } else {
                row.setColor(new Color(0.65f, 0.65f, 0.7f, 1f));
            }
            row.setWrap(true);
            objectivesTable.add(row).width(360).row();
        }
    }

    private Label sectionLabel(String text) {
        Label l = new Label(text, skin);
        l.setColor(new Color(0.7f, 0.75f, 0.8f, 1f));
        return l;
    }

    private Label muted(String text) {
        Label l = new Label(text, skin);
        l.setColor(new Color(0.55f, 0.55f, 0.6f, 1f));
        return l;
    }

    /**
     * Reuses existing project button styles; skin has no "default" TextButtonStyle.
     */
    private static void ensureButtonStyles(Skin skin) {
        if (skin == null) {
            return;
        }
        if (!skin.has("pause-menu", TextButton.TextButtonStyle.class)) {
            TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
            style.font = skin.getFont("default");
            style.fontColor = Color.WHITE;
            style.up = solid(new Color(0.22f, 0.22f, 0.28f, 1f));
            style.over = solid(new Color(0.35f, 0.32f, 0.2f, 1f));
            style.down = solid(new Color(0.45f, 0.38f, 0.18f, 1f));
            style.checked = solid(new Color(0.4f, 0.35f, 0.18f, 1f));
            skin.add("pause-menu", style);
        }
    }

    private static Drawable solid(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(texture);
    }
}
