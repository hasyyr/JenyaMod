package ru.jenya;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class JenyaEntity extends PathAwareEntity implements GeoEntity {
    private static final RawAnimation IDLE =
        RawAnimation.begin().thenLoop("animation.ellie.idle");
    private static final RawAnimation WALK =
        RawAnimation.begin().thenLoop("animation.ellie.walk");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public JenyaEntity(EntityType<? extends PathAwareEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return PathAwareEntity.createMobAttributes()
            .add(EntityAttributes.MAX_HEALTH, 24.0)
            .add(EntityAttributes.MOVEMENT_SPEED, 0.28)
            .add(EntityAttributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void initGoals() {
        // Companion behavior: approach a nearby player, then idle when close.
        this.goalSelector.add(1, new FollowNearbyPlayerGoal(this, 1.05, 12.0, 2.5));
        this.goalSelector.add(2, new WanderAroundFarGoal(this, 0.9));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(4, new LookAroundGoal(this));
    }

    private static final class FollowNearbyPlayerGoal extends Goal {
        private final JenyaEntity jenya;
        private final double speed;
        private final double followRange;
        private final double stopDistance;
        private PlayerEntity target;

        private FollowNearbyPlayerGoal(JenyaEntity jenya, double speed, double followRange, double stopDistance) {
            this.jenya = jenya;
            this.speed = speed;
            this.followRange = followRange;
            this.stopDistance = stopDistance;
        }

        @Override
        public boolean canStart() {
            PlayerEntity candidate = this.jenya.getWorld().getClosestPlayer(this.jenya, this.followRange);
            if (candidate == null || candidate.isSpectator() || candidate.isCreative()) {
                return false;
            }
            if (this.jenya.squaredDistanceTo(candidate) <= this.stopDistance * this.stopDistance) {
                return false;
            }
            this.target = candidate;
            return true;
        }

        @Override
        public boolean shouldContinue() {
            return this.target != null
                && this.target.isAlive()
                && !this.target.isSpectator()
                && this.jenya.squaredDistanceTo(this.target) > this.stopDistance * this.stopDistance
                && this.jenya.squaredDistanceTo(this.target) <= this.followRange * this.followRange;
        }

        @Override
        public void start() {
            if (this.target != null) {
                this.jenya.getNavigation().startMovingTo(this.target, this.speed);
            }
        }

        @Override
        public void stop() {
            this.target = null;
            this.jenya.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (this.target != null && this.jenya.squaredDistanceTo(this.target) > this.stopDistance * this.stopDistance) {
                this.jenya.getNavigation().startMovingTo(this.target, this.speed);
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(test -> {
            if (test.isMoving()) {
                return test.setAndContinue(WALK);
            }
            return test.setAndContinue(IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }
}
