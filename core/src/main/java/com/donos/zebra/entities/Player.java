package com.donos.zebra.entities;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.donos.zebra.world.LevelConstants;
import com.donos.zebra.items.Inventory;
import com.donos.zebra.items.ItemDefinition;
import com.donos.zebra.items.ItemRegistry;
import com.donos.zebra.items.ItemType;
import com.donos.zebra.util.CollisionMovement;

import java.util.Map;

public class Player implements Entity {

    static final float HITBOX_HALF_WIDTH = 5f;
    static final float HITBOX_HALF_HEIGHT = 6f;
    static final float HITBOX_OFFSET_X = 0f;
    static final float HITBOX_OFFSET_Y = -4f;

    private float currentHealth = 100f;
    private boolean isDead = false;

    private float hurtTimer = 0f;
    private static final float HURT_DURATION = 0.3f;

    private final PlayerInput input;
    private Map<String, Animation<TextureRegion>[]> animations;
    private Animation<TextureRegion>[] currentAnimation;
    private Animation<TextureRegion>[] previousAnimation;
    private String currentAnimationKey = AnimationConstants.ANIM_IDLE;

    private float x, y;
    private float stateTime;
    private float scale = 1f;

    private float offsetX = 0f;
    private float offsetY = 0f;

    private boolean isAttacking = false;
    private Direction lastDirection = Direction.DOWN;

    private Polygon hitbox;
    private float[] hitboxLocalVertices;
    private final Polygon scratchX = new Polygon();
    private final Polygon scratchY = new Polygon();
    private final float[] moveScratch = new float[2];

    // --- SISTEMA DE ITENS ---
    private final Inventory inventory = new Inventory(20); // Fonte única de verdade (20 slots)
    private boolean isInteracting = false;
    private boolean hasFirstSword = false;
    private ItemDefinition equippedWeapon = null;
    private ItemDefinition equippedHelmet = null;
    private ItemDefinition equippedChestplate = null;
    private ItemDefinition equippedBoots = null;

    public Player() {
        this(new PlayerInput());
    }

    public Player(AssetManager assetManager) {
        this(new PlayerInput(), assetManager);
    }

    Player(PlayerInput input) {
        this(input, PlayerAnimationLoader.loadAnimations());
    }

    public Player(PlayerInput input, Map<String, Animation<TextureRegion>[]> animations) {
        this.input = input;
        this.animations = animations;
        initState();
    }

    Player(PlayerInput input, AssetManager assetManager) {
        this.input = input;
        animations = PlayerAnimationLoader.loadAnimations(assetManager);
        initState();
    }

    private void initState() {
        currentAnimation = animations.get(AnimationConstants.ANIM_IDLE);
        previousAnimation = currentAnimation;
        stateTime = 0f;

        x = LevelConstants.DEFAULT_SPAWN_X;
        y = LevelConstants.DEFAULT_SPAWN_Y;

        float[] vertices = {
            -HITBOX_HALF_WIDTH, -HITBOX_HALF_HEIGHT,
            HITBOX_HALF_WIDTH, -HITBOX_HALF_HEIGHT,
            HITBOX_HALF_WIDTH, HITBOX_HALF_HEIGHT,
            -HITBOX_HALF_WIDTH, HITBOX_HALF_HEIGHT
        };
        hitbox = new Polygon(vertices);
        hitboxLocalVertices = vertices.clone();
        syncHitboxPosition();
    }

    private void syncHitboxPosition() {
        hitbox.setPosition(x + HITBOX_OFFSET_X, y + HITBOX_OFFSET_Y);
    }

    void setAnimations(Map<String, Animation<TextureRegion>[]> animations) {
        this.animations = animations;
        currentAnimation = animations.get(AnimationConstants.ANIM_IDLE);
        previousAnimation = currentAnimation;
        currentAnimationKey = AnimationConstants.ANIM_IDLE;
    }

    @Override
    public void update(float delta) {
        // Collision-aware update is invoked via update(delta, collisionPolygons).
    }

    public void update(float delta, Array<Polygon> collisionPolygons) {
        if (isDead) {
            setCurrentAnimation("death");
            updateAnimation(delta);
            return;
        }

        if (hurtTimer > 0) {
            hurtTimer -= delta;
        }

        boolean moving = false;
        Direction currentDirection = lastDirection;

        //Adicionada a trava !isInteracting para impedir andar/atacar com o menu de loot aberto
        if (!isInteracting && !isAttacking && hurtTimer <= 0) {
            Vector2 velocity = input.getIntendedVelocity(delta);
            move(velocity.x, velocity.y, collisionPolygons);
            moving = input.isMoving();
            currentDirection = input.getIntendedDirection();

            if (input.isAttacking()) {
                isAttacking = true;
                stateTime = 0f;
                setCurrentAnimation(AnimationConstants.ANIM_ATTACK);
            }
        }

        if (moving) {
            lastDirection = currentDirection;
        }

        updateAnimationState(moving);
        updateAnimation(delta);
    }

    private void updateAnimationState(boolean moving) {
        if (hurtTimer > 0) {
            setCurrentAnimation("hurt");
        } else if (isAttacking) {
            setCurrentAnimation(AnimationConstants.ANIM_ATTACK);
            if (currentAnimation[lastDirection.ordinal()].isAnimationFinished(stateTime)) {
                isAttacking = false;
                stateTime = 0f;
                setCurrentAnimation(AnimationConstants.ANIM_IDLE);
            }
        } else if (moving) {
            setCurrentAnimation(AnimationConstants.ANIM_RUN);
        } else {
            setCurrentAnimation(AnimationConstants.ANIM_IDLE);
        }
    }

    private void setCurrentAnimation(String key) {
        currentAnimationKey = key;
        currentAnimation = animations.get(key);
    }

    void move(float dx, float dy, Array<Polygon> collisionPolygons) {
        moveScratch[0] = x;
        moveScratch[1] = y;
        CollisionMovement.tryMove(
            moveScratch,
            HITBOX_OFFSET_X,
            HITBOX_OFFSET_Y,
            hitboxLocalVertices,
            dx,
            dy,
            collisionPolygons,
            scratchX,
            scratchY
        );
        x = moveScratch[0];
        y = moveScratch[1];
        syncHitboxPosition();
    }

    private void updateAnimation(float delta) {
        if (currentAnimation != previousAnimation) {
            stateTime = 0f;
            previousAnimation = currentAnimation;
        }
        stateTime += delta;
    }

    public void revive(float spawnX, float spawnY) {
        this.currentHealth = getMaxHealth();
        this.isDead = false;
        this.hurtTimer = 0f;
        this.isAttacking = false;
        this.isInteracting = false;
        
        setPosition(spawnX, spawnY);
        
        this.lastDirection = Direction.DOWN;
        setCurrentAnimation(AnimationConstants.ANIM_IDLE);
        this.previousAnimation = currentAnimation;
        this.stateTime = 0f;
    }

    @Override
    public void render(SpriteBatch batch) {
        TextureRegion currentFrame = currentAnimation[lastDirection.ordinal()].getKeyFrame(stateTime, !isDead);

        batch.draw(currentFrame,
            x + offsetX - (currentFrame.getRegionWidth() * scale) / 2f,
            y + offsetY - (currentFrame.getRegionHeight() * scale) / 2f,
            currentFrame.getRegionWidth() * scale,
            currentFrame.getRegionHeight() * scale);
    }

    @Override
    public void dispose() {
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        syncHitboxPosition();
    }

    public Polygon getHitbox() {
        return hitbox;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public String getCurrentAnimationKey() {
        return currentAnimationKey;
    }

    public void setOffsets(float offsetX, float offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    @Override
    public void takeDamage(float amount) {
        if (isDead) return;
        float mitigated = Math.max(1f, amount - getDefense());
        currentHealth -= mitigated;
        if (currentHealth <= 0) {
            currentHealth = 0;
            isDead = true;
            stateTime = 0f;
        } else {
            hurtTimer = HURT_DURATION;
            stateTime = 0f;
        }
    }

    @Override
    public boolean isDead() {
        return isDead;
    }

    @Override
    public float getCurrentHealth() {
        return currentHealth;
    }

    public float getMaxHealth(){
        return 100f;
    }

    /**
     * @return O inventário do jogador, única fonte de verdade para posse de itens.
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Define se o jogador está com uma janela de interação aberta (congelando movimentos).
     */
    public void setInteracting(boolean interacting) {
        this.isInteracting = interacting;
    }

    /**
     * @return true se o jogador estiver ocupado interagindo com um corpo/baú/NPC.
     */
    public boolean isInteracting() {
        return isInteracting;
    }

    public boolean hasFirstSword() {
        return hasFirstSword;
    }

    /**
     * Mentor opening reward: inventory sword + equip it so melee unlocks at damage 10.
     */
    public void grantFirstSword() {
        this.hasFirstSword = true;
        equipWeapon(ItemRegistry.IRON_SWORD);
    }

    public boolean hasWeaponEquipped() {
        return equippedWeapon != null;
    }

    public ItemDefinition getEquippedWeapon() {
        return equippedWeapon;
    }

    public ItemDefinition getEquippedHelmet() {
        return equippedHelmet;
    }

    public ItemDefinition getEquippedChestplate() {
        return equippedChestplate;
    }

    public ItemDefinition getEquippedBoots() {
        return equippedBoots;
    }

    public float getAttackDamage() {
        return equippedWeapon != null ? equippedWeapon.getAttackDamage() : 0f;
    }

    /** Sum of defense from all equipped armor pieces (0 for empty slots). */
    public float getDefense() {
        return slotDefense(equippedHelmet)
            + slotDefense(equippedChestplate)
            + slotDefense(equippedBoots);
    }

    private static float slotDefense(ItemDefinition piece) {
        return piece != null ? piece.getDefense() : 0f;
    }

    public void equipWeapon(ItemDefinition weapon) {
        if (weapon == null || weapon.getType() != ItemType.WEAPON) {
            return;
        }
        this.equippedWeapon = weapon;
        this.hasFirstSword = true;
    }

    /**
     * Equips armor into the slot defined by {@link ItemDefinition#getArmorSlot()},
     * replacing whatever was previously in that slot only.
     */
    public void equipArmor(ItemDefinition armor) {
        if (armor == null || armor.getType() != ItemType.ARMOR) {
            return;
        }
        switch (armor.getArmorSlot()) {
            case HELMET:
                equippedHelmet = armor;
                break;
            case CHESTPLATE:
                equippedChestplate = armor;
                break;
            case BOOTS:
                equippedBoots = armor;
                break;
            case NONE:
            default:
                break;
        }
    }

    /**
     * Auto-equip after a successful craft at the station.
     */
    public void equipCrafted(ItemDefinition item) {
        if (item == null) {
            return;
        }
        if (item.getType() == ItemType.WEAPON) {
            equipWeapon(item);
        } else if (item.getType() == ItemType.ARMOR) {
            equipArmor(item);
        }
    }
}