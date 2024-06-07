package de.xenonlightz.keepinventory.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.xenonlightz.keepinventory.bridge.PlayerEntityBridge;

import java.util.Optional;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements PlayerEntityBridge {
    /**
     * The per-player keepInventory flag.
     */
    private Optional<Boolean> $zireael_keepInventory;

    /**
     * Getter for $wotq_keepInventory.
     *
     * @return value of $wotq_keepInventory
     */
    public Optional<Boolean> $zireael_getKeepInventory() {
        return $zireael_keepInventory;
    }

    /**
     * Setter for $wotq_keepInventory
     *
     * @param value value of $wotq_keepInventory
     */
    public void $zireael_setKeepInventory(Optional<Boolean> value) {
        $zireael_keepInventory = value;
    }

    /**
     * Injected at the end of the constructor.
     *
     * @param callback
     */
    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void onInit(CallbackInfo callback) {
        $zireael_keepInventory = Optional.empty();
    }

    /**
     * Injected at the end of readCustomDataFromTag
     *
     * @param tag tag to read the data from
     * @param callback
     */
    @Inject(method = "readCustomDataFromNbt", at = @At(value = "RETURN"))
    public void onReadCustomDataFromTag(NbtCompound nbt, CallbackInfo callback) {
        if ( nbt.contains( PlayerEntityBridge.NBT_NAME ) ) {
            $zireael_keepInventory = Optional.of( nbt.getBoolean( PlayerEntityBridge.NBT_NAME ) );
        } else {
            $zireael_keepInventory = Optional.empty();
        }
    }

    /**
     * Injected at the end of writeCustomDataToTag.
     *
     * @param tag tag to write the data to
     * @param callback
     */
    @Inject(method = "writeCustomDataToNbt", at = @At(value = "RETURN"))
    public void onWriteCustomDataToTag( NbtCompound nbt, CallbackInfo callback ) {
        $zireael_keepInventory.ifPresent(value -> nbt.putBoolean(PlayerEntityBridge.NBT_NAME , value));
    }

    /**
     * Redirects call to getBoolean(GameRules.KEEP_INVENTORY) in dropInventory.
     *
     * Only interferes if key is KEEP_INVENTORY.
     *
     * @param rules object getBoolean is being called on
     * @param key argument getBoolean is being called with
     *
     * @return $wotq_keepInventory if it is true or false, otherwise the gamerule's value
     */
    @Redirect(method = "dropInventory", at = @At(value = "INVOKE", target = "net/minecraft/world/GameRules.getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    public boolean onDropInventory(GameRules rules, GameRules.Key<GameRules.BooleanRule> key) {
        if (key.getName().equals("keepInventory")) {
            return this.$zireael_keepInventory.orElseGet( () -> rules.getBoolean(key) );
        } else {
            return rules.getBoolean(key);
        }
    }

    /**
     * Redirects call to getBoolean(GameRules.KEEP_INVENTORY) in getCurrentExperience (more like getDroppedExperience).
     *
     * Only interferes if key is KEEP_INVENTORY.
     *
     * @param rules object getBoolean is being called on
     * @param key argument getBoolean is being called with
     *
     * @return $wotq_keepInventory if it is true or false, otherwise the gamerule's value
     */
    @Redirect(method = "getXpToDrop", at = @At(value = "INVOKE", target = "net/minecraft/world/GameRules.getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    public boolean onGetCurrentExperience( GameRules rules, 
                                           GameRules.Key<GameRules.BooleanRule> key ) {
        if (key.getName().equals("keepInventory")) {
            return this.$zireael_keepInventory.orElseGet( () -> rules.getBoolean(key) );
        } else {
            return rules.getBoolean(key);
        }
    }
}
