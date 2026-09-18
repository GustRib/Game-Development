package com.donos.zebra.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.donos.zebra.quests.QuestDefinition;
import com.donos.zebra.quests.QuestInstance;
import com.donos.zebra.quests.QuestLog;
import com.donos.zebra.quests.QuestObjectiveDefinition;
import com.donos.zebra.quests.QuestObjectiveProgress;
import com.donos.zebra.quests.QuestObjectiveType;

/**
 * Compact top-right quest tracker. Click opens the quest journal.
 */
public class QuestTrackerHUD extends Table {

    private final Label headerLabel;
    private final Label titleLabel;
    private final Label objectiveLabel;
    private final Label progressLabel;
    private Runnable onClicked;

    public QuestTrackerHUD(Skin skin) {
        setBackground(solid(new Color(0.06f, 0.08f, 0.1f, 0.82f)));
        pad(10, 12, 10, 12);
        defaults().left().growX();
        setTouchable(Touchable.enabled);

        headerLabel = new Label("QUEST ATUAL", skin);
        headerLabel.setColor(new Color(0.75f, 0.8f, 0.85f, 1f));
        titleLabel = new Label("-", skin);
        titleLabel.setColor(new Color(0.95f, 0.9f, 0.55f, 1f));
        titleLabel.setWrap(true);
        objectiveLabel = new Label("", skin);
        objectiveLabel.setWrap(true);
        objectiveLabel.setColor(Color.WHITE);
        progressLabel = new Label("", skin);
        progressLabel.setColor(new Color(0.7f, 0.9f, 0.75f, 1f));

        add(headerLabel).padBottom(4).row();
        add(titleLabel).padBottom(6).width(200).row();
        add(objectiveLabel).width(200).row();
        add(progressLabel).padTop(4).row();

        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onClicked != null) {
                    onClicked.run();
                }
            }
        });
        pack();
    }

    public void setOnClicked(Runnable onClicked) {
        this.onClicked = onClicked;
    }

    public void refresh(QuestLog log) {
        if (log == null) {
            setVisible(false);
            return;
        }
        QuestInstance qi = log.getTrackedInstance();
        QuestDefinition def = log.getTrackedDefinition();
        QuestObjectiveDefinition obj = log.getCurrentObjective();
        if (qi == null || def == null || obj == null) {
            setVisible(false);
            return;
        }
        setVisible(true);
        titleLabel.setText(def.title);
        objectiveLabel.setText(obj.description);
        QuestObjectiveProgress prog = log.getCurrentObjectiveProgress();
        int cur = prog == null ? 0 : prog.currentAmount;
        int req = obj.requiredAmount;
        boolean done = prog != null && prog.completed;
        if (obj.type == QuestObjectiveType.COLLECT_ITEM || obj.type == QuestObjectiveType.KILL_ENEMY) {
            progressLabel.setText((done ? "✓ " : "□ ") + cur + "/" + req);
        } else if (done) {
            progressLabel.setText("✓ Concluído");
        } else {
            progressLabel.setText("□ Concluído");
        }
        invalidateHierarchy();
        pack();
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
