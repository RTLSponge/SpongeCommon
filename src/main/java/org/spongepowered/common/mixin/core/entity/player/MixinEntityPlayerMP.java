/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.mixin.core.entity.player;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.entity.CombatHelper.getNewTracker;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Sets;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.ServersideAttributeMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C15PacketClientSettings;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S05PacketSpawnPosition;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.network.play.server.S48PacketResourcePackSend;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.stats.StatisticsFile;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.FoodStats;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldSettings;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.entity.GameModeData;
import org.spongepowered.api.data.manipulator.mutable.entity.JoinData;
import org.spongepowered.api.data.type.SkinPart;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.tab.TabList;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.ChangeGameModeEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.PlayerChangeClientSettingsEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.BookView;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.chat.ChatVisibility;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeGameModeData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeJoinData;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.effect.particle.SpongeParticleEffect;
import org.spongepowered.common.effect.particle.SpongeParticleHelper;
import org.spongepowered.common.entity.living.human.EntityHuman;
import org.spongepowered.common.entity.player.PlayerKickHelper;
import org.spongepowered.common.entity.player.tab.SpongeTabList;
import org.spongepowered.common.event.CauseTracker;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.interfaces.IMixinCommandSender;
import org.spongepowered.common.interfaces.IMixinCommandSource;
import org.spongepowered.common.interfaces.IMixinEntityPlayerMP;
import org.spongepowered.common.interfaces.IMixinPacketResourcePackSend;
import org.spongepowered.common.interfaces.IMixinServerScoreboard;
import org.spongepowered.common.interfaces.IMixinSubject;
import org.spongepowered.common.interfaces.IMixinTeam;
import org.spongepowered.common.interfaces.network.IMixinNetHandlerPlayServer;
import org.spongepowered.common.interfaces.text.IMixinTitle;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.text.chat.SpongeChatType;
import org.spongepowered.common.util.BookFaker;
import org.spongepowered.common.util.LanguageUtil;
import org.spongepowered.common.util.SkinUtil;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.storage.SpongePlayerDataHandler;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

@Mixin(EntityPlayerMP.class)
public abstract class MixinEntityPlayerMP extends MixinEntityPlayer implements Player, IMixinSubject, IMixinEntityPlayerMP, IMixinCommandSender,
        IMixinCommandSource {

    private EntityPlayerMP mcPlayer = (EntityPlayerMP)(Object) this;
    public int newExperience = 0;
    public int newLevel = 0;
    public int newTotalExperience = 0;
    public boolean keepsLevel = false;
    private boolean sleepingIgnored;

    private final User user = SpongeImpl.getGame().getServiceManager().provideUnchecked(UserStorageService.class).getOrCreate((GameProfile) getGameProfile());

    @Shadow @Final public MinecraftServer mcServer;
    @Shadow @Final public ItemInWorldManager theItemInWorldManager;
    @Shadow private String translator;
    @Shadow public NetHandlerPlayServer playerNetServerHandler;
    @Shadow public int lastExperience;
    @Shadow private EntityPlayer.EnumChatVisibility chatVisibility = EntityPlayer.EnumChatVisibility.FULL;
    @Shadow private boolean chatColours;
    @Shadow public boolean playerConqueredTheEnd;
    @Shadow private float lastHealth;
    @Shadow private int lastFoodLevel;

    @Shadow public abstract void setSpectatingEntity(Entity entityToSpectate);
    @Shadow public abstract void sendPlayerAbilities();
    @Shadow public abstract void func_175145_a(StatBase p_175145_1_);
    @Shadow public abstract StatisticsFile getStatFile();

    private Set<SkinPart> skinParts = Sets.newHashSet();
    private int viewDistance;
    private TabList tabList = new SpongeTabList((EntityPlayerMP) (Object) this);

    private WorldSettings.GameType pendingGameType;

    private Scoreboard spongeScoreboard = Sponge.getGame().getServer().getServerScoreboard().get();

    @Nullable private Vector3d velocityOverride = null;


    @Inject(method = "onUpdateEntity", at=@At(value = "HEAD"))
    private void checkCombatLog(CallbackInfo ci){
        ((IMixinNetHandlerPlayServer) this.playerNetServerHandler).checkDisconnectTimer();
    }

    @Inject(method = "removeEntity", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/NetHandlerPlayServer;sendPacket(Lnet/minecraft/network/Packet;)V"))
    private void onRemoveEntity(Entity entityIn, CallbackInfo ci) {
        if (entityIn instanceof EntityHuman) {
            ((EntityHuman) entityIn).onRemovedFrom((EntityPlayerMP) (Object) this);
        }
    }

    @SuppressWarnings("rawtypes")
    @Redirect(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Scoreboard;getObjectivesFromCriteria(Lnet/minecraft/scoreboard/IScoreObjectiveCriteria;)Ljava/util/Collection;"))
    public Collection onGetObjectivesFromCriteria(net.minecraft.scoreboard.Scoreboard this$0, IScoreObjectiveCriteria criteria) {
        return this.getWorldScoreboard().getObjectivesFromCriteria(criteria);
    }

    /**
     * @author blood - May 12th, 2016
     *
     * @reason SpongeForge requires an overwrite so we do it here instead. This handles player death events.
     */
    @Overwrite
    public void onDeath(DamageSource cause) {
        if (SpongeCommonEventFactory.callDestructEntityEventDeath((EntityLivingBase)(Object) this, cause) == null) {
            return;
        }

        if (this.worldObj.getGameRules().getBoolean("showDeathMessages")) {
            Team team = this.getTeam();

            if (team != null && team.getDeathMessageVisibility() != Team.EnumVisible.ALWAYS) {
                if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OTHER_TEAMS) {
                    this.mcServer.getConfigurationManager().sendMessageToAllTeamMembers((EntityPlayerMP)(Object) this, this.getCombatTracker().getDeathMessage());
                } else if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OWN_TEAM) {
                    this.mcServer.getConfigurationManager().sendMessageToTeamOrEvryPlayer((EntityPlayerMP)(Object) this, this.getCombatTracker().getDeathMessage());
                }
            } else {
                this.mcServer.getConfigurationManager().sendChatMsg(this.getCombatTracker().getDeathMessage());
            }
        }

        if (!this.worldObj.getGameRules().getBoolean("keepInventory")) {
            this.captureItemDrops = true;
            this.capturedItemDrops.clear();

            this.inventory.dropAllItems();

            this.captureItemDrops = false;
            if (this.capturedItemDrops.size() > 0) {
                IMixinWorld spongeWorld = (IMixinWorld) this.worldObj;
                final CauseTracker causeTracker = spongeWorld.getCauseTracker();
                causeTracker.setIgnoreSpawnEvents(true);
                DropItemEvent.Destruct event = SpongeCommonEventFactory.callDropItemEventDestruct((EntityPlayerMP)(Object) this, cause, this.capturedItemDrops);
                if (!event.isCancelled()) {
                    for (net.minecraft.entity.item.EntityItem item : this.capturedItemDrops) {
                        this.worldObj.spawnEntityInWorld(item);
                    }
                    this.inventory.clear();
                }
                causeTracker.setIgnoreSpawnEvents(false);
            }
        }

        for (ScoreObjective scoreobjective : this.worldObj.getScoreboard().getObjectivesFromCriteria(IScoreObjectiveCriteria.deathCount)) {
            Score score = this.getWorldScoreboard().getValueFromObjective(this.getName(), scoreobjective);
            score.func_96648_a();
        }

        EntityLivingBase entitylivingbase = this.getAttackingEntity();

        if (entitylivingbase != null) {
            EntityList.EntityEggInfo entitylist$entityegginfo = EntityList.entityEggs.get(Integer.valueOf(EntityList.getEntityID(entitylivingbase)));

            if (entitylist$entityegginfo != null) {
                this.triggerAchievement(entitylist$entityegginfo.field_151513_e);
            }

            entitylivingbase.addToPlayerScore((EntityPlayerMP)(Object) this, this.scoreValue);
        }

        this.triggerAchievement(StatList.deathsStat);
        this.func_175145_a(StatList.timeSinceDeathStat);
        this.getCombatTracker().reset();
    }

    /**
     * @author blood - May 30th, 2016
     *
     * @reason - adjusted to support {@link DisplaceEntityEvent.Teleport}
     *
     * @param dimensionId The id of target dimension.
     */
    @Overwrite
    public void travelToDimension(int dimensionId) {
        // If leaving The End via End's Portal
        if (this.worldObj.provider instanceof WorldProviderEnd && dimensionId == 1) {
            this.worldObj.removeEntity(this.mcPlayer);
            // this will trigger respawn logic in NetHandlerPlayServer.processPlayer
            this.playerConqueredTheEnd = true;
            this.triggerAchievement(AchievementList.theEnd2);
            this.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(4, 0.0F));
            return;
        }

        // If going to The End from an overworld type dimension
        if (this.worldObj.provider instanceof WorldProviderSurface && dimensionId == 1) {
            this.triggerAchievement(AchievementList.theEnd);
            // This is already handled in ServerConfigurationManager transferEntityToWorld
            /*
            BlockPos blockpos = this.mcServer.worldServerForDimension(dimensionId).getSpawnCoordinate();

            if (blockpos != null) {
                this.playerNetServerHandler.setPlayerLocation((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), 0.0F, 0.0F);
            }
            */

            dimensionId = 1;
        } else {
            this.triggerAchievement(AchievementList.portal);
        }

        this.mcServer.getConfigurationManager().transferPlayerToDimension(this.mcPlayer, dimensionId);

        // This has been moved below to refreshXpHealthAndFood
        /*
        this.lastExperience = -1;
        this.lastHealth = -1.0F;
        this.lastFoodLevel = -1;
        */
    }

    @Override
    public GameProfile getProfile() {
        return this.user.getProfile();
    }

    @Override
    public boolean isOnline() {
        return this.user.isOnline();
    }

    @Override
    public Optional<Player> getPlayer() {
        return Optional.of(this);
    }

    @Override
    public User getUserObject() {
        return this.user;
    }

    // Post before the player values are updated
    @Inject(method = "handleClientSettings", at = @At("HEAD"))
    public void processClientSettingsEvent(C15PacketClientSettings packet, CallbackInfo ci) {
        PlayerChangeClientSettingsEvent event = SpongeEventFactory.createPlayerChangeClientSettingsEvent(Cause.of(NamedCause.source(this)),
                (ChatVisibility) (Object) packet.getChatVisibility(), SkinUtil.fromFlags(packet.getModelPartFlags()),
                LanguageUtil.LOCALE_CACHE.getUnchecked(packet.getLang()), this, packet.isColorsEnabled(), packet.view);
        SpongeImpl.postEvent(event);
    }

    @Inject(method = "handleClientSettings", at = @At("RETURN"))
    public void processClientSettings(C15PacketClientSettings packet, CallbackInfo ci) {
        this.skinParts = SkinUtil.fromFlags(packet.getModelPartFlags()); // Returned set is immutable
        this.viewDistance = packet.view;
    }

    @Override
    public Locale getLocale() {
        return LanguageUtil.LOCALE_CACHE.getUnchecked(this.translator);
    }

    @Override
    public int getViewDistance() {
        return this.viewDistance;
    }

    @Override
    public ChatVisibility getChatVisibility() {
        return (ChatVisibility) (Object) this.chatVisibility;
    }

    @Override
    public boolean isChatColorsEnabled() {
        return this.chatColours;
    }

    @Override
    public Set<SkinPart> getDisplayedSkinParts() {
        return this.skinParts;
    }

    @Override
    public void sendMessage(ChatType type, Text message) {
        checkNotNull(type, "type");
        checkNotNull(message, "message");

        IChatComponent component = SpongeTexts.toComponent(message);
        if (type == ChatTypes.ACTION_BAR) {
            component = SpongeTexts.fixActionBarFormatting(component);
        }

        this.playerNetServerHandler.sendPacket(new S02PacketChat(component, ((SpongeChatType) type).getByteId()));
    }

    @Override
    public void sendBookView(BookView bookView) {
        BookFaker.fakeBookView(bookView, this);
    }

    @Override
    public void sendTitle(Title title) {
        ((IMixinTitle) (Object) title).send((EntityPlayerMP) (Object) this);
    }

    @Override
    public void spawnParticles(ParticleEffect particleEffect, Vector3d position) {
        this.spawnParticles(particleEffect, position, Integer.MAX_VALUE);
    }

    @Override
    public void spawnParticles(ParticleEffect particleEffect, Vector3d position, int radius) {
        checkNotNull(particleEffect, "The particle effect cannot be null!");
        checkNotNull(position, "The position cannot be null");
        checkArgument(radius > 0, "The radius has to be greater then zero!");

        List<Packet<?>> packets = SpongeParticleHelper.toPackets((SpongeParticleEffect) particleEffect, position);

        if (!packets.isEmpty()) {
            if (position.sub(this.posX, this.posY, this.posZ).lengthSquared() < (long) radius * (long) radius) {
                for (Packet<?> packet : packets) {
                    this.playerNetServerHandler.sendPacket(packet);
                }
            }
        }
    }

    @Override
    public PlayerConnection getConnection() {
        return (PlayerConnection) this.playerNetServerHandler;
    }

    // this needs to be overridden from EntityPlayer so we can force a resend of the experience level
    @Override
    public void setLevel(int level) {
        super.experienceLevel = level;
        this.lastExperience = -1;
    }

    @Override
    public String getSubjectCollectionIdentifier() {
        return ((IMixinSubject) this.user).getSubjectCollectionIdentifier();
    }

    @Override
    public String getIdentifier() {
        return this.user.getIdentifier();
    }

    @Override
    public Tristate permDefault(String permission) {
        return ((IMixinSubject) this.user).permDefault(permission);
    }

    @Override
    public void refreshXpHealthAndFood() {
        this.lastExperience = -1;
        this.lastHealth = -1.0F;
        this.lastFoodLevel = -1;
    }

    @Override
    public void reset() {
        float experience = 0;
        boolean keepInventory = this.worldObj.getGameRules().getBoolean("keepInventory");

        if (this.keepsLevel || keepInventory) {
            experience = this.experience;
            this.newTotalExperience = this.experienceTotal;
            this.newLevel = this.experienceLevel;
        }

        this.clearActivePotions();
        this._combatTracker = getNewTracker(this);
        this.deathTime = 0;
        this.experience = 0;
        this.experienceLevel = this.newLevel;
        this.experienceTotal = this.newTotalExperience;
        this.fire = 0;
        this.fallDistance = 0;
        this.foodStats = new FoodStats();
        this.potionsNeedUpdate = true;
        this.openContainer = this.inventoryContainer;
        this.attackingPlayer = null;
        this.entityLivingToAttack = null;
        this.lastExperience = -1;
        this.setHealth(this.getMaxHealth());

        if (this.keepsLevel || keepInventory) {
            this.experience = experience;
        } else {
            this.addExperience(this.newExperience);
        }

        this.keepsLevel = false;
    }

    @Override
    public boolean isViewingInventory() {
        return this.openContainer != null;
    }

    @Override
    public void setScoreboard(Scoreboard scoreboard) {
        if (scoreboard == null) {
            scoreboard = Sponge.getGame().getServer().getServerScoreboard().get();
        }
        ((IMixinServerScoreboard) this.spongeScoreboard).removePlayer((EntityPlayerMP) (Object) this, true);
        this.spongeScoreboard = scoreboard;
        ((IMixinServerScoreboard) this.spongeScoreboard).addPlayer((EntityPlayerMP) (Object) this);
    }

    @Override
    public void initScoreboard() {
        ((IMixinServerScoreboard) this.spongeScoreboard).addPlayer((EntityPlayerMP) (Object) this);
    }

    @Override
    public Text getTeamRepresentation() {
        return Text.of(this.getName());
    }

    @Override
    public MessageChannel getDeathMessageChannel() {
        EntityPlayerMP player = (EntityPlayerMP) (Object) this;
        if (player.worldObj.getGameRules().getBoolean("showDeathMessages")) {
            @Nullable Team team = player.getTeam();

            if (team != null && team.getDeathMessageVisibility() != Team.EnumVisible.ALWAYS) {
                if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OTHER_TEAMS) {
                    return ((IMixinTeam) team).getTeamChannel(player);
                } else if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OWN_TEAM) {
                    return ((IMixinTeam) team).getNonTeamChannel();
                }
            } else {
                return this.getMessageChannel();
            }
        }

        return MessageChannel.TO_NONE;
    }

    @Override
    public net.minecraft.scoreboard.Scoreboard getWorldScoreboard() {
        return (net.minecraft.scoreboard.Scoreboard) this.spongeScoreboard;
    }

    @Override
    public Scoreboard getScoreboard() {
        return this.spongeScoreboard;
    }

    @Override
    public void kick() {
        kick(Text.of(SpongeImpl.getGame().getRegistry().getTranslationById("disconnect.disconnected").get()));
    }

    @Override
    public void kick(Text message) {
        final IChatComponent component = SpongeTexts.toComponent(message);
        PlayerKickHelper.kickPlayer((EntityPlayerMP) (Object) this, component);
    }

    @Override
    public void playSound(SoundType sound, Vector3d position, double volume) {
        this.playSound(sound, position, volume, 1);
    }

    @Override
    public void playSound(SoundType sound, Vector3d position, double volume, double pitch) {
        this.playSound(sound, position, volume, pitch, 0);
    }

    @Override
    public void playSound(SoundType sound, Vector3d position, double volume, double pitch, double minVolume) {
        this.playerNetServerHandler.sendPacket(new S29PacketSoundEffect(sound.getId(), position.getX(), position.getY(), position.getZ(),
                (float) Math.max(minVolume, volume), (float) pitch));
    }

    @Override
    public void sendResourcePack(ResourcePack pack) {
        S48PacketResourcePackSend packet = new S48PacketResourcePackSend();
        ((IMixinPacketResourcePackSend) packet).setResourcePack(pack);
        this.playerNetServerHandler.sendPacket(packet);
    }

    @Override
    public CommandSource asCommandSource() {
        return this;
    }

    @Override
    public ICommandSender asICommandSender() {
        return (ICommandSender) this;
    }

    @Override
    public boolean isSleepingIgnored() {
        return this.sleepingIgnored;
    }

    @Override
    public void setSleepingIgnored(boolean sleepingIgnored) {
        this.sleepingIgnored = sleepingIgnored;
    }

    @Override
    public Vector3d getVelocity() {
        if (this.velocityOverride != null) {
            return this.velocityOverride;
        }
        return super.getVelocity();
    }

    @Override
    public void setImplVelocity(Vector3d velocity) {
        super.setImplVelocity(velocity);
        this.velocityOverride = null;
    }

    @Override
    public void setVelocityOverride(@Nullable Vector3d velocity) {
        this.velocityOverride = velocity;
    }

    @SuppressWarnings("unchecked")
    @Override
    public CarriedInventory<? extends Carrier> getInventory() {
        return (CarriedInventory<? extends Carrier>) this.inventory;
    }

    @Inject(method = "setGameType(Lnet/minecraft/world/WorldSettings$GameType;)V", at = @At("HEAD"), cancellable = true)
    private void onSetGameType(WorldSettings.GameType gameType, CallbackInfo ci) {
        ChangeGameModeEvent.TargetPlayer event = SpongeEventFactory.createChangeGameModeEventTargetPlayer(Cause.of(NamedCause.source(this)),
                (GameMode) (Object) this.theItemInWorldManager.getGameType(), (GameMode) (Object) gameType, this);
        SpongeImpl.postEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
        this.pendingGameType = (WorldSettings.GameType) (Object) event.getGameMode();
    }

    /**
     * This injector must appear <b>after</b> {@link #onSetGameType} since it
     * assigns the {@link #pendingGameType} returned by the event to the actual
     * local variable in the method.
     */
    @ModifyVariable(method = "Lnet/minecraft/entity/player/EntityPlayerMP;setGameType(Lnet/minecraft/world/WorldSettings$GameType;)V", at = @At(value = "HEAD", remap = false), argsOnly = true)
    private WorldSettings.GameType assignPendingGameType(WorldSettings.GameType gameType) {
        return this.pendingGameType;
    }

    @Redirect(method = "onDeath", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/GameRules;getBoolean(Ljava/lang/String;)Z", ordinal = 0))
    public boolean onGetGameRules(GameRules gameRules, String gameRule) {
        return false; // Suppress death messages since this is handled together with the event calling
    }

    @Override
    public void resetAttributeMap() {
        // The name is wrong - it's used on the client and server
        this.attributeMap = new ServersideAttributeMap();
        this.applyEntityAttributes();

        // Re-create the array, so that attributes are properly re-added
        this.previousEquipment = new ItemStack[5];
    }

    @Override
    public TabList getTabList() {
        return this.tabList;
    }


    @Override
    public void setTargetedLocation(@Nullable Vector3d vec) {
        super.setTargetedLocation(vec);
        this.playerNetServerHandler.sendPacket(new S05PacketSpawnPosition(VecHelper.toBlockPos(this.getTargetedLocation())));
    }

    @Override
    public JoinData getJoinData() {
        return new SpongeJoinData(SpongePlayerDataHandler.getFirstJoined(this.getUniqueID()).get(), Instant.now());
    }

    @Override
    public Value<Instant> firstPlayed() {
        return new SpongeValue<>(Keys.FIRST_DATE_PLAYED, Instant.EPOCH, SpongePlayerDataHandler.getFirstJoined(this.getUniqueID()).get());
    }

    @Override
    public Value<Instant> lastPlayed() {
        return new SpongeValue<>(Keys.LAST_DATE_PLAYED, Instant.EPOCH, Instant.now());
    }

    // TODO implement with contextual data
//    @Override
//    public DisplayNameData getDisplayNameData() {
//        return null;
//    }

    @Override
    public GameModeData getGameModeData() {
        return new SpongeGameModeData((GameMode) (Object) this.theItemInWorldManager.getGameType());
    }

    @Override
    public Value<GameMode> gameMode() {
        return new SpongeValue<>(Keys.GAME_MODE, DataConstants.Catalog.DEFAULT_GAMEMODE,
                (GameMode) (Object) this.theItemInWorldManager.getGameType());
    }

    @Override
    @Nullable
    public Text getDisplayNameText() {
        return Text.of(getName());
    }

    @Override
    public void setDisplayName(@Nullable Text displayName) {
        // Do nothing
    }

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        manipulators.add(getJoinData());
        manipulators.add(getGameModeData());
    }

    @Override
    public void sendBlockChange(int x, int y, int z, BlockState state) {
        checkNotNull(state, "state");
        S23PacketBlockChange packet = new S23PacketBlockChange();
        packet.blockPosition = new BlockPos(x, y, z);
        packet.blockState = (IBlockState) state;
        this.playerNetServerHandler.sendPacket(packet);
    }

    @Override
    public void resetBlockChange(int x, int y, int z) {
        S23PacketBlockChange packet = new S23PacketBlockChange(this.worldObj, new BlockPos(x, y, z));
        this.playerNetServerHandler.sendPacket(packet);
    }

}
