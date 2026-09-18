package com.donos.zebra.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Polygon;
import com.donos.zebra.Interaction.Interactable;
import com.donos.zebra.items.ItemRegistry;
import com.donos.zebra.quests.QuestDefinition;
import com.donos.zebra.quests.QuestIds;
import com.donos.zebra.quests.QuestInstance;
import com.donos.zebra.quests.QuestLog;
import com.donos.zebra.quests.QuestObjectiveDefinition;
import com.donos.zebra.quests.QuestObjectiveType;
import com.donos.zebra.quests.QuestRegistry;
import com.donos.zebra.quests.QuestStatus;
import com.donos.zebra.ui.DialogueUI;
import com.donos.zebra.world.OpeningQuest;

/**
 * Village mentor: First Sword opening slice, then sequential quest offers 2–7,
 * with shop available only after Orc Cleanup turn-in.
 */
public class MentorNpc implements Entity, Interactable {

    private final float x, y;
    private final float radius = 26f;
    private boolean gavePickaxe = false;
    private final DialogueUI dialogueUI;
    private Runnable openShop;
    private QuestLog questLog;

    private final java.util.Map<String, Animation<TextureRegion>[]> animations;
    private float stateTime = 0f;

    private final Polygon dummyHitbox;

    public MentorNpc(float x, float y, DialogueUI dialogueUI,
                       java.util.Map<String, Animation<TextureRegion>[]> animations) {
        this.x = x;
        this.y = y;
        this.dialogueUI = dialogueUI;
        this.animations = animations;

        this.dummyHitbox = new Polygon(new float[]{0, 0, 16, 0, 16, 16, 0, 16});
        this.dummyHitbox.setPosition(x - 8f, y);
    }

    public void setOpenShop(Runnable openShop) {
        this.openShop = openShop;
    }

    public void setQuestLog(QuestLog questLog) {
        this.questLog = questLog;
    }

    public boolean hasGavePickaxe() {
        return gavePickaxe;
    }

    public void setGavePickaxe(boolean gavePickaxe) {
        this.gavePickaxe = gavePickaxe;
    }

    @Override
    public void update(float delta) {
        stateTime += delta;
    }

    @Override
    public void render(SpriteBatch batch) {
        Animation<TextureRegion>[] idle = animations.get(AnimationConstants.ANIM_IDLE);
        if (idle != null && idle.length > 0 && idle[0] != null) {
            TextureRegion currentFrame = idle[0].getKeyFrame(stateTime, true);

            float targetHeight = 48f;
            float targetWidth = targetHeight;

            batch.draw(
                currentFrame,
                x - targetWidth / 2f,
                y,
                targetWidth,
                targetHeight
            );
        }
    }

    @Override
    public void takeDamage(float amount) {
    }

    @Override
    public boolean isDead() {
        return false;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public Polygon getHitbox() {
        return dummyHitbox;
    }

    @Override
    public float getCurrentHealth() {
        return 100f;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void onInteract(Player player) {
        if (!gavePickaxe) {
            handleFirstSwordIntro(player);
            return;
        }

        if (questLog == null) {
            if (!player.hasFirstSword()) {
                handleFirstSwordActive(player);
            } else {
                showText(
                    "Mentor: A vila precisa de voce.\n"
                        + "Nao deixe a corrupcao se espalhar!"
                );
            }
            return;
        }

        QuestInstance first = questLog.getInstance(QuestIds.QUEST_FIRST_SWORD);
        if (first != null && first.status == QuestStatus.ACTIVE) {
            handleFirstSwordActive(player);
            return;
        }

        // Priority: turn in active mentor talk objective
        QuestDefinition turnIn = questLog.findActiveMentorTurnIn();
        if (turnIn != null) {
            handleMentorTurnIn(player, turnIn);
            return;
        }

        // Offer next chain quest
        QuestDefinition offer = questLog.findOfferableMentorQuest();
        if (offer != null) {
            offerQuest(player, offer);
            return;
        }

        // Progress reminder for active non-talk objectives (talk only — shop is R)
        QuestDefinition activeReminder = findActiveNonTalkReminder();
        if (activeReminder != null) {
            showText(progressHint(activeReminder));
            return;
        }

        showText(
            "Mentor: A vila precisa de voce.\n"
                + (isShopUnlocked()
                ? "Use [R] para abrir a loja quando quiser."
                : "Nao deixe a corrupcao se espalhar!")
        );
    }

    @Override
    public boolean hasSecondaryInteract() {
        return isShopUnlocked() && openShop != null;
    }

    @Override
    public String getSecondaryPromptText() {
        return hasSecondaryInteract() ? "[R] Abrir loja" : null;
    }

    @Override
    public void onSecondaryInteract(Player player) {
        if (!hasSecondaryInteract()) {
            return;
        }
        if (dialogueUI != null) {
            dialogueUI.hideDialogue();
        }
        openShop.run();
    }

    private boolean isShopUnlocked() {
        return questLog != null && questLog.isShopUnlocked();
    }

    private void handleFirstSwordIntro(Player player) {
        showText(
            "Mentor: A corrupcao ja toma a vila...\n"
                + "Voce nao aguenta esses monstros desarmado.\n"
                + "Tome esta picareta — traga "
                + OpeningQuest.COPPER_ORE_REQUIRED
                + " minerios de cobre do norte.\n"
                + "Eu forjo sua primeira espada."
        );
        player.getInventory().addItem(ItemRegistry.STONE_PICKAXE, 1);
        gavePickaxe = true;
        if (questLog != null) {
            questLog.reportTalkNpc(QuestIds.NPC_MENTOR);
        }
    }

    private void handleFirstSwordActive(Player player) {
        if (player.hasFirstSword()) {
            return;
        }
        int oreCount = player.getInventory().getItemCount(ItemRegistry.COPPER_ORE);
        if (oreCount < OpeningQuest.COPPER_ORE_REQUIRED) {
            showText(
                "Mentor: Ainda falta cobre. A veia fica ao norte da vila.\n"
                    + "Volte com "
                    + OpeningQuest.COPPER_ORE_REQUIRED
                    + " minerios e eu forjo sua lamina.\n"
                    + "(Voce tem " + oreCount + "/"
                    + OpeningQuest.COPPER_ORE_REQUIRED + ")"
            );
            return;
        }

        boolean removed = player.getInventory().removeItem(
            ItemRegistry.COPPER_ORE, OpeningQuest.COPPER_ORE_REQUIRED);
        if (!removed) {
            showText("Mentor: Hmm... algo deu errado com o cobre. Tente de novo.");
            return;
        }

        player.getInventory().addItem(ItemRegistry.IRON_SWORD, 1);
        player.grantFirstSword();
        if (questLog != null) {
            // Inventory turn-in may skip gameplay collect events — sync quest objectives.
            questLog.reportCollectItem(QuestIds.ITEM_COPPER_ORE, OpeningQuest.COPPER_ORE_REQUIRED);
            questLog.reportTalkNpc(QuestIds.NPC_MENTOR);
        }
        showText(
            "Mentor: Bom trabalho. Com este cobre...\n"
                + "Eis sua primeira espada. Agora enfrente esses invasores!\n"
                + "Fale comigo quando estiver pronto para a proxima tarefa."
        );
    }

    private void offerQuest(Player player, QuestDefinition def) {
        questLog.startQuest(def.id);
        showText(offerDialogue(def));
    }

    private void handleMentorTurnIn(Player player, QuestDefinition def) {
        QuestInstance qi = questLog.getInstance(def.id);
        if (qi == null) {
            return;
        }
        questLog.reportTalkNpc(QuestIds.NPC_MENTOR);
        // After talk, quest may be completed
        if (questLog.isCompleted(def.id)) {
            questLog.grantReward(def, player);
            showText(turnInDialogue(def));
        } else {
            showText("Mentor: Continue. Ainda ha mais a fazer nesta tarefa.");
        }
    }

    private void openShopIfAllowed(Player player) {
        // Kept for tests / legacy callers; production shop uses onSecondaryInteract (R).
        onSecondaryInteract(player);
        if (!hasSecondaryInteract()) {
            if (questLog != null && !questLog.isShopUnlocked()) {
                showText(
                    "Mentor: Ainda nao. Limpe os orcs da vila primeiro,\n"
                        + "depois use [R] para comercio."
                );
            }
        }
    }

    private QuestDefinition findActiveNonTalkReminder() {
        for (QuestInstance qi : questLog.getActiveQuests()) {
            QuestDefinition def = QuestRegistry.get(qi.questId);
            if (def == null) {
                continue;
            }
            QuestObjectiveDefinition obj = def.getObjective(qi.currentObjectiveIndex);
            if (obj != null && obj.type != QuestObjectiveType.TALK_TO_NPC) {
                return def;
            }
        }
        return null;
    }

    private static String offerDialogue(QuestDefinition def) {
        if (QuestIds.QUEST_ORC_CLEANUP.equals(def.id)) {
            return "Mentor: Sua lamina esta pronta. Agora limpe os orcs!\n"
                + "Derrote 5 deles e volte. So entao abrirei minha loja ([R]).";
        }
        if (QuestIds.QUEST_COPPER_CHESTPLATE.equals(def.id)) {
            return "Mentor: Bom trabalho com os orcs. Minha loja esta aberta ([R]).\n"
                + "Agora forje um Peitoral de Cobre na forja da taverna.";
        }
        if (QuestIds.QUEST_FULL_COPPER_SET.equals(def.id)) {
            return "Mentor: O peitoral ajuda, mas nao basta.\n"
                + "Complete o conjunto: capacete, luvas e botas.\n"
                + "Entao liberarei a receita da Espada de Ferro.";
        }
        if (QuestIds.QUEST_IRON_BLADE_TRIAL.equals(def.id)) {
            return "Mentor: Eis a Espada de Ferro na forja.\n"
                + "Equipe-a e derrote 10 orcs. Ao voltar, despertarei o Redemoinho.";
        }
        if (QuestIds.QUEST_WHIRLWIND_PROVING.equals(def.id)) {
            return "Mentor: Domine o Redemoinho.\n"
                + "Derrote 10 inimigos com essa habilidade.\n"
                + "Ao concluir, liberarei o Golpe Flamejante.";
        }
        if (QuestIds.QUEST_FLAME_STRIKE_PROVING.equals(def.id)) {
            return "Mentor: Agora o Golpe Flamejante.\n"
                + "Derrote 10 inimigos com o fogo — a queimadura tambem conta.";
        }
        return "Mentor: Uma nova tarefa o aguarda.\n" + def.title;
    }

    private static String turnInDialogue(QuestDefinition def) {
        if (QuestIds.QUEST_ORC_CLEANUP.equals(def.id)) {
            return "Mentor: Excelente. A vila respira melhor.\n"
                + "Minha loja esta aberta — use [R] perto de mim.\n"
                + "Pegue estas 50 pratas. Fale comigo para a proxima missao.";
        }
        if (QuestIds.QUEST_COPPER_CHESTPLATE.equals(def.id)) {
            return "Mentor: Um peitoral solido. Aqui estao 30 pratas.\n"
                + "Volte para completar o conjunto de cobre.";
        }
        if (QuestIds.QUEST_FULL_COPPER_SET.equals(def.id)) {
            return "Mentor: Armadura completa! Aqui estao 60 pratas.\n"
                + "A receita da Espada de Ferro esta liberada na forja.";
        }
        if (QuestIds.QUEST_IRON_BLADE_TRIAL.equals(def.id)) {
            return "Mentor: Voce dominou a lamina de ferro.\n"
                + "80 pratas — e o Redemoinho agora e seu.";
        }
        if (QuestIds.QUEST_WHIRLWIND_PROVING.equals(def.id)) {
            return "Mentor: O Redemoinho e seu.\n"
                + "1 ouro, 10 pocoes medias, e o Golpe Flamejante liberado.";
        }
        if (QuestIds.QUEST_FLAME_STRIKE_PROVING.equals(def.id)) {
            return "Mentor: O fogo obedece a voce.\n"
                + "1 ouro e 10 pocoes medias. A vila tem um verdadeiro guerreiro.";
        }
        return "Mentor: Missao concluida. Bem feito.";
    }

    private static String progressHint(QuestDefinition def) {
        return "Mentor: Ainda na tarefa \"" + def.title + "\".\n"
            + "Consulte o rastreador de quests (canto superior direito).\n"
            + "(Use [R] para a loja, se ja estiver aberta.)";
    }

    private void showText(String text) {
        if (dialogueUI != null) {
            dialogueUI.showText(text);
        }
    }

    @Override
    public float getInteractionRadius() {
        return radius;
    }

    @Override
    public String getPromptText() {
        return "[E] Falar com o Mentor";
    }
}
