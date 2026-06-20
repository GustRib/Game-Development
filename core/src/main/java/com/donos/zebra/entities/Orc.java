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

        // Se estiver morto, roda a animação de morte e não faz mais nada
        if (isDead) {
            if (currentAnimation != animations.get("death")) {
                currentAnimation = animations.get("death");
                stateTime = 0f; // Reinicia o tempo para começar a animação de morte do primeiro frame
            }
            return;
        }

        // Diminui o temporizador de dano se ele foi atingido
        if (hurtTimer > 0) {
            hurtTimer -= delta;
        }

        float oldX = this.x;
        float oldY = this.y;

        chasePlayer(player, delta);

        float dx = this.x - oldX;
        float dy = this.y - oldY;
        boolean moving = (dx != 0 || dy != 0);

        // Define qual animação usar com base no estado atual
        if (animations != null) {
            if (hurtTimer > 0 && animations.containsKey("hurt")) {
                this.currentAnimation = animations.get("hurt");
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