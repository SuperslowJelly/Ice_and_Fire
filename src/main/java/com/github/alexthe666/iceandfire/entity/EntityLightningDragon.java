package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.api.event.DragonFireEvent;
import com.github.alexthe666.iceandfire.entity.util.DragonUtils;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.github.alexthe666.iceandfire.message.MessageDragonSyncFire;
import com.github.alexthe666.iceandfire.misc.IafSoundRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import java.util.Random;

public class EntityLightningDragon extends EntityDragonBase {

    public static final float[] growth_stage_1 = new float[]{1F, 3F};
    public static final float[] growth_stage_2 = new float[]{3F, 7F};
    public static final float[] growth_stage_3 = new float[]{7F, 12.5F};
    public static final float[] growth_stage_4 = new float[]{12.5F, 20F};
    public static final float[] growth_stage_5 = new float[]{20F, 30F};

    public static final ResourceLocation FEMALE_LOOT = new ResourceLocation("iceandfire", "entities/dragon/fire_dragon_female");
    public static final ResourceLocation MALE_LOOT = new ResourceLocation("iceandfire", "entities/dragon/fire_dragon_male");
    public static final ResourceLocation SKELETON_LOOT = new ResourceLocation("iceandfire", "entities/dragon/fire_dragon_skeleton");
    private static final DataParameter<Boolean> HAS_LIGHTNING_TARGET = EntityDataManager.createKey(EntityDragonBase.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float> LIGHTNING_TARGET_X = EntityDataManager.createKey(EntityDragonBase.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> LIGHTNING_TARGET_Y = EntityDataManager.createKey(EntityDragonBase.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> LIGHTNING_TARGET_Z = EntityDataManager.createKey(EntityDragonBase.class, DataSerializers.FLOAT);
    public static Animation ANIMATION_FIRECHARGE;


    public EntityLightningDragon(World worldIn) {
        this(IafEntityRegistry.LIGHTNING_DRAGON, worldIn);
    }

    public EntityLightningDragon(EntityType t, World worldIn) {
        super(t, worldIn, DragonType.LIGHTNING, 1, 1 + IafConfig.dragonAttackDamage, IafConfig.dragonHealth * 0.04, IafConfig.dragonHealth, 0.15F, 0.4F);
        this.setPathPriority(PathNodeType.DANGER_FIRE, 0.0F);
        this.setPathPriority(PathNodeType.DAMAGE_FIRE, 0.0F);
        this.setPathPriority(PathNodeType.LAVA, 8.0F);
        ANIMATION_SPEAK = Animation.create(20);
        ANIMATION_BITE = Animation.create(35);
        ANIMATION_SHAKEPREY = Animation.create(65);
        ANIMATION_TAILWHACK = Animation.create(40);
        ANIMATION_FIRECHARGE = Animation.create(30);
        ANIMATION_WINGBLAST = Animation.create(50);
        ANIMATION_ROAR = Animation.create(40);
        ANIMATION_EPIC_ROAR = Animation.create(60);
        this.growth_stages = new float[][]{growth_stage_1, growth_stage_2, growth_stage_3, growth_stage_4, growth_stage_5};
    }

    @Override
    protected void registerData() {
        super.registerData();
        this.dataManager.register(HAS_LIGHTNING_TARGET, false);
        this.dataManager.register(LIGHTNING_TARGET_X, 0.0F);
        this.dataManager.register(LIGHTNING_TARGET_Y, 0.0F);
        this.dataManager.register(LIGHTNING_TARGET_Z, 0.0F);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new SwimGoal(this));
    }

    public String getVariantName(int variant) {
        switch (variant) {
            default:
                return "red_";
            case 1:
                return "green_";
            case 2:
                return "bronze_";
            case 3:
                return "gray_";
        }
    }

    public Item getVariantScale(int variant) {
        switch (variant) {
            default:
                return IafItemRegistry.DRAGONSCALES_ELECTRIC;
            case 1:
                return IafItemRegistry.DRAGONSCALES_AMYTHEST;
            case 2:
                return IafItemRegistry.DRAGONSCALES_COPPER;
            case 3:
                return IafItemRegistry.DRAGONSCALES_BLACK;
        }
    }

    public Item getVariantEgg(int variant) {
        switch (variant) {
            default:
                return IafItemRegistry.DRAGONEGG_ELECTRIC;
            case 1:
                return IafItemRegistry.DRAGONEGG_AMYTHEST;
            case 2:
                return IafItemRegistry.DRAGONEGG_COPPER;
            case 3:
                return IafItemRegistry.DRAGONEGG_BLACK;
        }
    }

    public void setHasLightningTarget(boolean lightning_target) {
        this.dataManager.set(HAS_LIGHTNING_TARGET, lightning_target);
    }

    public boolean hasLightningTarget() {
        return this.dataManager.get(HAS_LIGHTNING_TARGET).booleanValue();
    }

    public void setLightningTargetVec(float x, float y, float z) {
        this.dataManager.set(LIGHTNING_TARGET_X, x);
        this.dataManager.set(LIGHTNING_TARGET_Y, y);
        this.dataManager.set(LIGHTNING_TARGET_Z, z);
    }

    public float getLightningTargetX() {
        return this.dataManager.get(LIGHTNING_TARGET_X);
    }

    public float getLightningTargetY() {
        return this.dataManager.get(LIGHTNING_TARGET_Y);
    }

    public float getLightningTargetZ() {
        return this.dataManager.get(LIGHTNING_TARGET_Z);
    }

    @Override
    public Item getSummoningCrystal() {
        return IafItemRegistry.SUMMONING_CRYSTAL_FIRE;
    }

    public boolean canBeSteered() {
        return true;
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        this.getLookController().setLookPositionWithEntity(entityIn, 30.0F, 30.0F);
        if (!this.isPlayingAttackAnimation()) {
            switch (groundAttack) {
                case BITE:
                    this.setAnimation(ANIMATION_BITE);
                    break;
                case TAIL_WHIP:
                    this.setAnimation(ANIMATION_TAILWHACK);
                    break;
                case SHAKE_PREY:
                    boolean flag = false;
                    if (new Random().nextInt(2) == 0 && isDirectPathBetweenPoints(this, this.getPositionVector().add(0, this.getHeight() / 2, 0), entityIn.getPositionVector().add(0, entityIn.getHeight() / 2, 0)) &&
                            entityIn.getWidth() < this.getWidth() * 0.5F && this.getControllingPassenger() == null && this.getDragonStage() > 1 && !(entityIn instanceof EntityDragonBase) && !DragonUtils.isAnimaniaMob(entityIn)) {
                        this.setAnimation(ANIMATION_SHAKEPREY);
                        flag = true;
                        entityIn.startRiding(this);
                    }
                    if (!flag) {
                        groundAttack = IafDragonAttacks.Ground.BITE;
                        this.setAnimation(ANIMATION_BITE);
                    }
                    break;
                case WING_BLAST:
                    this.setAnimation(ANIMATION_WINGBLAST);
                    break;
            }
        }
        return false;
    }

    @Override
    public void livingTick() {
        super.livingTick();
        if (!world.isRemote && this.getAttackTarget() != null) {
            if (this.getBoundingBox().grow(2.5F + this.getRenderSize() * 0.33F, 2.5F + this.getRenderSize() * 0.33F, 2.5F + this.getRenderSize() * 0.33F).intersects(this.getAttackTarget().getBoundingBox())) {
                attackEntityAsMob(this.getAttackTarget());
            }
            if (this.groundAttack == IafDragonAttacks.Ground.FIRE && (usingGroundAttack || this.onGround)) {
                shootFireAtMob(this.getAttackTarget());
            }
            if (this.airAttack == IafDragonAttacks.Air.TACKLE && !usingGroundAttack && this.getDistanceSq(this.getAttackTarget()) < 100) {
                double difX = this.getAttackTarget().getPosX() - this.getPosX();
                double difY = this.getAttackTarget().getPosY() + this.getAttackTarget().getHeight() - this.getPosY();
                double difZ = this.getAttackTarget().getPosZ() - this.getPosZ();
                this.setMotion(this.getMotion().add(difX * 0.1D, difY * 0.1D, difZ * 0.1D));
                if (this.getBoundingBox().grow(1 + this.getRenderSize() * 0.5F, 1 + this.getRenderSize() * 0.5F, 1 + this.getRenderSize() * 0.5F).intersects(this.getAttackTarget().getBoundingBox())) {
                    attackEntityAsMob(this.getAttackTarget());
                    usingGroundAttack = true;
                    randomizeAttacks();
                    setFlying(false);
                    setHovering(false);
                }
            }
        }
        if(!isBreathingFire()){
            this.setHasLightningTarget(false);
        }
    }


    @Override
    protected void breathFireAtPos(BlockPos burningTarget) {
        if (this.isBreathingFire()) {
            if (this.isActuallyBreathingFire()) {
                rotationYaw = renderYawOffset;
                if (this.ticksExisted % 5 == 0) {
                    this.playSound(IafSoundRegistry.FIREDRAGON_BREATH, 4, 1);
                }
                stimulateFire(burningTarget.getX() + 0.5F, burningTarget.getY() + 0.5F, burningTarget.getZ() + 0.5F, 1);
            }
        } else {
            this.setBreathingFire(true);
        }
    }

    public void riderShootFire(Entity controller) {
        if (this.getRNG().nextInt(5) == 0 && !this.isChild()) {
            if (this.getAnimation() != ANIMATION_FIRECHARGE) {
                this.setAnimation(ANIMATION_FIRECHARGE);
            } else if (this.getAnimationTick() == 20) {
                rotationYaw = renderYawOffset;
                Vec3d headVec = this.getHeadPosition();
                this.playSound(IafSoundRegistry.FIREDRAGON_BREATH, 4, 1);
                double d2 = controller.getLookVec().x;
                double d3 = controller.getLookVec().y;
                double d4 = controller.getLookVec().z;
                float inaccuracy = 1.0F;
                d2 = d2 + this.rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
                d3 = d3 + this.rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
                d4 = d4 + this.rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
                EntityDragonFireCharge entitylargefireball = new EntityDragonFireCharge(IafEntityRegistry.FIRE_DRAGON_CHARGE, world, this, d2, d3, d4);
                float size = this.isChild() ? 0.4F : this.isAdult() ? 1.3F : 0.8F;
                entitylargefireball.setPosition(headVec.x, headVec.y, headVec.z);
                if (!world.isRemote) {
                    world.addEntity(entitylargefireball);
                }
            }
        } else {
            if (this.isBreathingFire()) {
                if (this.isActuallyBreathingFire()) {
                    rotationYaw = renderYawOffset;
                    if (this.ticksExisted % 5 == 0) {
                        this.playSound(IafSoundRegistry.FIREDRAGON_BREATH, 4, 1);
                    }
                    RayTraceResult mop = rayTraceRider(controller, 10 * this.getDragonStage(), 1.0F);
                    if (mop != null) {
                        stimulateFire(mop.getHitVec().x, mop.getHitVec().y, mop.getHitVec().z, 1);
                    }
                }
            } else {
                this.setBreathingFire(true);
            }
        }
    }

    @Override
    public ResourceLocation getDeadLootTable() {
        if (this.getDeathStage() >= (this.getAgeInDays() / 5) / 2) {
            return SKELETON_LOOT;
        } else {
            return isMale() ? MALE_LOOT : FEMALE_LOOT;
        }
    }

    private void shootFireAtMob(LivingEntity entity) {
        if (this.usingGroundAttack && this.groundAttack == IafDragonAttacks.Ground.FIRE || !this.usingGroundAttack && (this.airAttack == IafDragonAttacks.Air.SCORCH_STREAM || this.airAttack == IafDragonAttacks.Air.HOVER_BLAST)) {
            if (this.usingGroundAttack && this.getRNG().nextInt(5) == 0 || !this.usingGroundAttack && this.airAttack == IafDragonAttacks.Air.HOVER_BLAST) {
                if (this.getAnimation() != ANIMATION_FIRECHARGE) {
                    this.setAnimation(ANIMATION_FIRECHARGE);
                } else if (this.getAnimationTick() == 20) {
                    rotationYaw = renderYawOffset;
                    Vec3d headVec = this.getHeadPosition();
                    double d2 = entity.getPosX() - headVec.x;
                    double d3 = entity.getPosY() - headVec.y;
                    double d4 = entity.getPosZ() - headVec.z;
                    float inaccuracy = 1.0F;
                    d2 = d2 + this.rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
                    d3 = d3 + this.rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
                    d4 = d4 + this.rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
                    this.playSound(IafSoundRegistry.FIREDRAGON_BREATH, 4, 1);
                    EntityDragonFireCharge entitylargefireball = new EntityDragonFireCharge(IafEntityRegistry.FIRE_DRAGON_CHARGE, world, this, d2, d3, d4);
                    float size = this.isChild() ? 0.4F : this.isAdult() ? 1.3F : 0.8F;
                    entitylargefireball.setPosition(headVec.x, headVec.y, headVec.z);
                    if (!world.isRemote) {
                        world.addEntity(entitylargefireball);
                    }
                    if (!entity.isAlive() || entity == null) {
                        this.setBreathingFire(false);
                    }
                    this.randomizeAttacks();
                }
            } else {
                if (this.isBreathingFire()) {
                    if (this.isActuallyBreathingFire()) {
                        rotationYaw = renderYawOffset;
                        if (this.ticksExisted % 5 == 0) {
                            this.playSound(IafSoundRegistry.FIREDRAGON_BREATH, 4, 1);
                        }
                        stimulateFire(entity.getPosX(), entity.getPosY(), entity.getPosZ(), 1);
                        if (!entity.isAlive() || entity == null) {
                            this.setBreathingFire(false);
                            this.randomizeAttacks();
                        }
                    }
                } else {
                    this.setBreathingFire(true);
                }
            }
        }
        this.faceEntity(entity, 360, 360);
    }

    public void stimulateFire(double burnX, double burnY, double burnZ, int syncType) {
        if (MinecraftForge.EVENT_BUS.post(new DragonFireEvent(this, burnX, burnY, burnZ))) return;
        if (syncType == 1 && !world.isRemote) {
            //sync with client
            IceAndFire.sendMSGToAll(new MessageDragonSyncFire(this.getEntityId(), burnX, burnY, burnZ, 0));
        }
        if (syncType == 2 && world.isRemote) {
            //sync with server
            IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageDragonSyncFire(this.getEntityId(), burnX, burnY, burnZ, 0));
        }
        if (syncType == 3 && !world.isRemote) {
            //sync with client, fire bomb
            IceAndFire.sendMSGToAll(new MessageDragonSyncFire(this.getEntityId(), burnX, burnY, burnZ, 5));
        }
        if (syncType == 4 && world.isRemote) {
            //sync with server, fire bomb
            IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageDragonSyncFire(this.getEntityId(), burnX, burnY, burnZ, 5));
        }
        if (syncType > 2 && syncType < 6) {
            if (this.getAnimation() != ANIMATION_FIRECHARGE) {
                this.setAnimation(ANIMATION_FIRECHARGE);
            } else if (this.getAnimationTick() == 20) {
                rotationYaw = renderYawOffset;
                Vec3d headVec = this.getHeadPosition();
                double d2 = burnX - headVec.x;
                double d3 = burnY - headVec.y;
                double d4 = burnZ - headVec.z;
                float inaccuracy = 1.0F;
                d2 = d2 + this.rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
                d3 = d3 + this.rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
                d4 = d4 + this.rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
                this.playSound(IafSoundRegistry.FIREDRAGON_BREATH, 4, 1);
                EntityDragonFireCharge entitylargefireball = new EntityDragonFireCharge(IafEntityRegistry.FIRE_DRAGON_CHARGE, world, this, d2, d3, d4);
                float size = this.isChild() ? 0.4F : this.isAdult() ? 1.3F : 0.8F;
                entitylargefireball.setPosition(headVec.x, headVec.y, headVec.z);
                if (!world.isRemote) {
                    world.addEntity(entitylargefireball);
                }
            }
            return;
        }
        this.getNavigator().clearPath();
        this.burnParticleX = burnX;
        this.burnParticleY = burnY;
        this.burnParticleZ = burnZ;
        Vec3d headPos = getHeadPosition();
        double d2 = burnX - headPos.x;
        double d3 = burnY - headPos.y;
        double d4 = burnZ - headPos.z;
        float particleScale = MathHelper.clamp(this.getRenderSize() * 0.08F, 0.55F, 3F);
        double distance = Math.max(2.5F * this.getDistanceSq(burnX, burnY, burnZ), 0);
        double conqueredDistance = burnProgress / 40D * distance;
        int increment = (int) Math.ceil(conqueredDistance / 100);
        for (int i = 0; i < conqueredDistance; i += increment) {
            double progressX = headPos.x + d2 * (i / (float) distance);
            double progressY = headPos.y + d3 * (i / (float) distance);
            double progressZ = headPos.z + d4 * (i / (float) distance);
            if (canPositionBeSeen(progressX, progressY, progressZ)) {
                if (world.isRemote && rand.nextInt(5) == 0) {
                    //IceAndFire.PROXY.spawnDragonParticle("dragonfire", headPos.x, headPos.y, headPos.z, 0, 0, 0, this);
                    setHasLightningTarget(true);
                    setLightningTargetVec((float)burnX, (float)burnY, (float)burnZ);
                }
            } else {
                if (!world.isRemote) {
                    RayTraceResult result = this.world.rayTraceBlocks(new RayTraceContext(new Vec3d(this.getPosX(), this.getPosY() + (double) this.getEyeHeight(), this.getPosZ()), new Vec3d(progressX, progressY, progressZ), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));
                    BlockPos pos = new BlockPos(result.getHitVec());
                    IafDragonDestructionManager.destroyAreaFire(world, pos, this);
                    setHasLightningTarget(true);
                    setLightningTargetVec((float)result.getHitVec().x, (float)result.getHitVec().y, (float)result.getHitVec().z);
                }
            }
        }
        if (burnProgress >= 40D && canPositionBeSeen(burnX, burnY, burnZ)) {
            double spawnX = burnX + (rand.nextFloat() * 3.0) - 1.5;
            double spawnY = burnY + (rand.nextFloat() * 3.0) - 1.5;
            double spawnZ = burnZ + (rand.nextFloat() * 3.0) - 1.5;
            setHasLightningTarget(true);
            setLightningTargetVec((float)spawnX, (float)spawnY, (float)spawnZ);
            if (!world.isRemote) {
                IafDragonDestructionManager.destroyAreaFire(world, new BlockPos(spawnX, spawnY, spawnZ), this);
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isTeen() ? IafSoundRegistry.FIREDRAGON_TEEN_IDLE : this.isAdult() ? IafSoundRegistry.FIREDRAGON_ADULT_IDLE : IafSoundRegistry.FIREDRAGON_CHILD_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource p_184601_1_) {
        return this.isTeen() ? IafSoundRegistry.FIREDRAGON_TEEN_HURT : this.isAdult() ? IafSoundRegistry.FIREDRAGON_ADULT_HURT : IafSoundRegistry.FIREDRAGON_CHILD_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.isTeen() ? IafSoundRegistry.FIREDRAGON_TEEN_DEATH : this.isAdult() ? IafSoundRegistry.FIREDRAGON_ADULT_DEATH : IafSoundRegistry.FIREDRAGON_CHILD_DEATH;
    }

    @Override
    public SoundEvent getRoarSound() {
        return this.isTeen() ? IafSoundRegistry.FIREDRAGON_TEEN_ROAR : this.isAdult() ? IafSoundRegistry.FIREDRAGON_ADULT_ROAR : IafSoundRegistry.FIREDRAGON_CHILD_ROAR;
    }

    @Override
    public Animation[] getAnimations() {
        return new Animation[]{IAnimatedEntity.NO_ANIMATION, EntityDragonBase.ANIMATION_EAT, EntityDragonBase.ANIMATION_SPEAK, EntityDragonBase.ANIMATION_BITE, EntityDragonBase.ANIMATION_SHAKEPREY, EntityFireDragon.ANIMATION_TAILWHACK, EntityFireDragon.ANIMATION_FIRECHARGE, EntityFireDragon.ANIMATION_WINGBLAST, EntityFireDragon.ANIMATION_ROAR, EntityFireDragon.ANIMATION_EPIC_ROAR};
    }

    public boolean isBreedingItem(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() != null && stack.getItem() == IafItemRegistry.FIRE_STEW;
    }

    protected void spawnDeathParticles() {
        for (int k = 0; k < 3; ++k) {
            double d2 = this.rand.nextGaussian() * 0.02D;
            double d0 = this.rand.nextGaussian() * 0.02D;
            double d1 = this.rand.nextGaussian() * 0.02D;
            if (world.isRemote) {
                this.world.addParticle(ParticleTypes.FLAME, this.getPosX() + (double) (this.rand.nextFloat() * this.getWidth() * 2.0F) - (double) this.getWidth(), this.getPosY() + (double) (this.rand.nextFloat() * this.getHeight()), this.getPosZ() + (double) (this.rand.nextFloat() * this.getWidth() * 2.0F) - (double) this.getWidth(), d2, d0, d1);
            }
        }
    }

    protected void spawnBabyParticles() {
        for (int i = 0; i < 5; i++) {
            float radiusAdd = i * 0.15F;
            float headPosX = (float) (this.getPosX() + 1.8F * getRenderSize() * (0.3F + radiusAdd) * Math.cos((rotationYaw + 90) * Math.PI / 180));
            float headPosZ = (float) (this.getPosY() + 1.8F * getRenderSize() * (0.3F + radiusAdd) * Math.sin((rotationYaw + 90) * Math.PI / 180));
            float headPosY = (float) (this.getPosZ() + 0.5 * getRenderSize() * 0.3F);
            world.addParticle(ParticleTypes.LARGE_SMOKE, headPosX, headPosY, headPosZ, 0, 0, 0);
        }
    }

    protected ItemStack getSkull() {
        return new ItemStack(IafItemRegistry.DRAGON_SKULL_FIRE);
    }

    public ItemStack getHorn() {
        return new ItemStack(IafItemRegistry.DRAGON_HORN_FIRE);
    }

}
