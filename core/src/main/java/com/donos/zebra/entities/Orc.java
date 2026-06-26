package com.donos.zebra.entities;

import java.util.Map;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Polygon;
import com.donos.zebra.items.ItemRegistry;
import com.donos.zebra.items.ItemStack;

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
    
    private static final float ATTACK_RANGE = 24f; 
    
    private float attackVisualTimer = 0f;
    private static final float ATTACK_ANIM_DURATION = 0.6f; // Tempo total dos 6 frames de ataque (6 * 0.1s)

    public Orc(float x, float y, Map<String, Animation<TextureRegion>[]> orcAnimations) {
        super(x, y, 40f, 35f, 100f);
        this.animations = orcAnimations;
        
        if (animations != null && animations.containsKey(AnimationConstants.ANIM_IDLE)) {
            this.currentAnimation = animations.get(AnimationConstants.ANIM_IDLE);
        }

        float[] vertices = { -5f, -6f, 5f, -6f, 5f, 6f, -5f, 6f };
        this.hitbox = new Polygon(vertices);
        this.hitbox.setPosition(x, y);

        // ADICIONAR LOOT FIXO NO SPAWN ---
        // Adiciona exatamente 3 unidades de Minério de Cobre para testes(remover depois)
        this.lootTable.add(new ItemStack(ItemRegistry.COPPER_ORE, 3));
    }

    public void updateEnemy(Player player, float delta) {
        stateTime += delta;

        if (isDead) {
            if (currentAnimation != animations.get("death")) {
                currentAnimation = animations.get("death");
                stateTime = 0f;
            }
            return; // Bloqueia a IA, movimento e ataques. O corpo fica estático no chão pronto para ser looteado.
        }

        if (hurtTimer > 0) {
            hurtTimer -= delta;
        }

        // Atualiza o tempo restante da animação do golpe
        if (attackVisualTimer > 0) {
            attackVisualTimer -= delta;
        }

        // Diminui o tempo de espera do ataque
        if (attackCooldownTimer > 0) {
            attackCooldownTimer -= delta;
        }

        float oldX = this.x;
        float oldY = this.y;

        // O Orc FICA PARADO enquanto estiver desferindo o golpe (attackVisualTimer > 0)
        if (!player.isDead() && attackVisualTimer <= 0) {
            chasePlayer(player, delta);
        }

        float dx = this.x - oldX;
        float dy = this.y - oldY;
        boolean moving = (dx != 0 || dy != 0);

        // Lógica de ataque: Se estiver perto e o cooldown zerou, o orc inicia o ataque!
        float distanceToPlayer = com.badlogic.gdx.math.Vector2.dst(this.x, this.y, player.getX(), player.getY());
            
        if (!player.isDead() && distanceToPlayer <= ATTACK_RANGE && attackCooldownTimer <= 0) {
            attackCooldownTimer = ATTACK_COOLDOWN;
            attackVisualTimer = ATTACK_ANIM_DURATION; // Ativa a janela de tempo da animação
            player.takeDamage(15f); // Orc arranca 15 de vida
            this.stateTime = 0f; // Reinicia para tocar o frame de ataque do começo
        }

        // Define qual animação usar com base no estado atual
        if (animations != null) {
            if (hurtTimer > 0 && animations.containsKey("hurt")) {
                this.currentAnimation = animations.get("hurt");
            } else if (attackVisualTimer > 0 && animations.containsKey(AnimationConstants.ANIM_ATTACK)) {
                // Mantém a animação de ataque ativa enquanto o cronômetro visual não zerar!
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
            this.attackVisualTimer = 0f; // Interrompe o ataque se ele tomar um golpe (opcional)
        }
    }

    @Override
    public void update(float delta) {}

    @Override
    public void render(SpriteBatch batch) {
        if (currentAnimation == null) return;

        int dirIndex = facingDirection.ordinal();
        if (dirIndex >= currentAnimation.length) {
            dirIndex = 0; 
        }

        // Não loopamos animações de Morte, Dano ou Ataque para não ficarem repetindo loucamente
        boolean looping = !isDead && 
                            (currentAnimation != animations.get("hurt")) && 
                            (currentAnimation != animations.get(AnimationConstants.ANIM_ATTACK));
                        
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