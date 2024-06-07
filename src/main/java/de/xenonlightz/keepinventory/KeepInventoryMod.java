package de.xenonlightz.keepinventory;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;

import de.xenonlightz.keepinventory.bridge.PlayerEntityBridge;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static net.minecraft.command.argument.EntityArgumentType.getPlayers;
import static net.minecraft.command.argument.EntityArgumentType.players;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class KeepInventoryMod implements ModInitializer {
    /**
     * Initialize the mod.
     *
     * All this does is register the command.
     */
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(this::register);
    }

    /**
     * Register the /keepinventory command with the CommandDispatcher.
     *
     * @param dispatcher the CommandDispatcher to register with
     * @param dedicated  whether or not the server is dedicated (unused)
     */
    void register(CommandDispatcher<ServerCommandSource> dispatcher, 
                  CommandRegistryAccess registryAccess, 
                  CommandManager.RegistrationEnvironment environment) {
        Command<ServerCommandSource> self = context -> {
            execute(
              context.getSource(), 
              Collections.singleton(context.getSource().getPlayer()),
              context.getNodes().get(1).getNode()
            );
            return 1;
        };

        Command<ServerCommandSource> others = context -> {
            execute(
              context.getSource(), 
              getPlayers(context, "targets"), 
              context.getNodes().get(2).getNode()
            );
            return 1;
        };

        dispatcher
          .register(
            literal("keepinventory")
              .requires(source -> source.getEntity() instanceof ServerPlayerEntity)
              .then(literal("true").executes(self))
              .then(literal("false").executes(self))
              .then(literal("default").executes(self))
          );

        dispatcher
          .register(
            literal("keepinventory")
              .then(
                argument("targets", players())
                  .requires(source -> source.hasPermissionLevel(4))
                  .then(literal("true").executes(others))
                  .then(literal("false").executes(others))
                  .then(literal("default").executes(others))
              )
          );
    }

    /**
     * Execute the /keepinventory command.
     *
     * This sets the keepInventory state of all target players to the given value.
     *
     * @param source    the source executing the command
     * @param players   the target players
     * @param valueNode the CommandNode containing the literal value (true, false,
     *                  default)
     */
    private void execute(ServerCommandSource source,
                         Collection<ServerPlayerEntity> players,
                         CommandNode<ServerCommandSource> valueNode) {

        final var value = valueNode.getName().equals("true") ? Optional.of(true)
                        : valueNode.getName().equals("false") ? Optional.of(false)
                        : Optional.<Boolean>empty();

                        
        players.forEach(player -> {
          var playerBridge = PlayerEntityBridge.from( player );
          playerBridge.$zireael_setKeepInventory( value );
        } );

        if (players.size() == 1) {
            source.sendFeedback(
                () -> Text.literal("Set keepInventory to ")
                        .append( valueNode.getName() )
                        .append( " for " )
                        .append( players.iterator().next().getDisplayName() ),
                false );
        } else {
            source.sendFeedback(
                () -> Text.literal( "Set keepInventory to " )
                        .append( valueNode.getName() )
                        .append( " for " )
                        .append( String.valueOf( players.size() ) )
                        .append( " players" ),
                false 
            );
        }
    }
}
