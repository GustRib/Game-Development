package com.donos.zebra.entities;

import java.util.Map;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Polygon;

public class Orc extends Enemy {
    private final Map<String, Animation<TextureRegion>[]> animations;
    private Animation<TextureRegion>[] currentAnimation;
    private float stateTime = 0f;
    private Direction facingDirection = Direction.DOWN;
    private Polygon hitbox;

    // Controladores de estado visual
    private float hurtTimer = 0f;
    private static final float HURT_DURATION = 0.4f; // Tempo que ele pisca em vermelho

    private float attackCooldownTimer = 0f;
    private static final float ATTACK_COOLDOWN = 1.5f; // Ele bate a cada 1.5 segundos
    private static final float ATTACK_RANGE = 16f;

    public Orc(float x, float y, Map<String, Animation<TextureRegion>[]> orcAnimations) {
        super(x, y, 40f, 35f, 100f);
        this.animations = orcAnimations;
        
        if (animations != null && animations.containsKey(AnimationConstants.ANIM_IDLE)) {
            this.currentAnimation = animations.get(AnimationConstants.ANIM_IDLE);
        }

        float[] vertices = { -5f, -6f, 5f, -6f, 5f, 6f, -5f, 6f };
        this.hitbox = new Polygon(vertices);
        this.hitbox.setPosition(x, y);
    }

    public void updateEnemy(Player player, float delta) {
        stateTime += delta;

        if (isDead) {
            if (currentAnimation != animations.get("death")) {
                currentAnimation = animations.get("death");
                stateTime = 0f;
            }
            return;
        }

        if (hurtTimer > 0) {
            hurtTimer -= delta;
        }

        // Diminui o tempo de espera do ataque
        if (attackCooldownTimer > 0) {
            attackCooldownTimer -= delta;
        }

        float oldX = this.x;
        float oldY = this.y;

        // Só persegue e se move se o jogador não estiver morto
        if (!player.isDead()) {
            chasePlayer(player, delta);
        }

        float dx = this.x - oldX;
        float dy = this.y - oldY;
        boolean moving = (dx != 0 || dy != 0);

        // Lógica de ataque: Se estiver perto e o cooldown zerou, o orc ataca!
        float distanceToPlayer = com.badlogic.gdx.math.Vector2.dst(this.x, this.y, player.getX(), player.getY());            com.badlogic.gdx.math.Vector2.dst(this.x, this.y, player.getX(), player.getY());
            
        boolean attacking = false;
        if (!player.isDead() && distanceToPlayer <= ATTACK_RANGE && attackCooldownTimer <= 0) {
            attackCooldownTimer = ATTACK_COOLDOWN;
            player.takeDamage(15f); // Orc arranca 15 de vida
            attacking = true;
            this.stateTime = 0f; // Reinicia para tocar o frame de ataque
        }

        // Define qual animação usar com base no estado atual
        if (animations != null) {
            if (hurtTimer > 0 && animations.containsKey("hurt")) {
                this.currentAnimation = animations.get("hurt");
            } else if (attacking && animations.containsKey(AnimationConstants.ANIM_ATTACK)) {
                this.currentAnimation = animations.get(AnimationConstants.ANIM_ATTACK);
            } else if (moving) {
                if (Math.abs(dx) > Math.abs(dy)) {
                    facingDirection = dx > 0 ? Direction.RIGHT : Direction.LEFT;
                } else {
                    facingDirection = dy > 0 ? Direction.UP : Direction.DOWN;
                }
                this.currentAnimation = animations.get(AnimationConstants.ANIM_RUN); 
            } else {
                this.currentAnimation = animations.get(AnimationConstants.ANIM_IDLE);
            }
        }
    }

    @Override
    public void takeDamage(float amount) {
        super.takeDamage(amount);
        // Ativa o visual de dano apenas se ele sobreviver ao golpe
        if (!isDead) {
            this.hurtTimer = HURT_DURATION;
            this.stateTime = 0f; // Reinicia para ver a animação do dano desde o começo
        }
    }

    @Override
    public void update(float delta) {}

    @Override
    public void render(SpriteBatch batch) {
        // Garantimos que ele renderiza mesmo se isDead for true para podermos ver o corpo
        if (currentAnimation == null) return;

        int dirIndex = facingDirection.ordinal();
        if (dirIndex >= currentAnimation.length) {
            dirIndex = 0; 
        }

        // Se for a animação de morte, não colocamos em loop (passamos "false" no getKeyFrame)
        boolean looping = !isDead && (currentAnimation != animations.get("hurt"));
        TextureRegion currentFrame = currentAnimation[dirIndex].getKeyFrame(stateTime, looping);

        batch.draw(currentFrame,
            x - currentFrame.getRegionWidth() / 2f,
            y - currentFrame.getRegionHeight() / 2f,
            currentFrame.getRegionWidth(),
            currentFrame.getRegionHeight());
    }

    @Override
    public Polygon getHitbox() { return hitbox; }

    @Override
    public void dispose() {}
}