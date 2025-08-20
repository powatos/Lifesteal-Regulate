package net.powato.lifesteal.commands;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.render.block.entity.LightmapCoordinatesRetriever;
import net.minecraft.command.argument.CoordinateArgument;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.chunk.Chunk;
import net.powato.lifesteal.world.SafeChunkClass;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class AddSafeChunkCommand {


    public static void register(com.mojang.brigadier.CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("lifesteal_R")

                .then(literal("AddChunk")
                    .executes(context -> {
                        context.getSource().sendFeedback(() -> Text.literal("Add Called" ), false);

                        ServerPlayerEntity Player = context.getSource().getEntity() instanceof ServerPlayerEntity p ? p : null; // null if source is not Player, Player if source is Player
                        if (Player == null){
                            context.getSource().sendError(Text.literal("This command must be run by a Player!"));
                            return 0;
                        }
                        ServerWorld World = Player.getWorld();
                        Chunk CurrentChunk = World.getChunk(context.getSource().getPlayer().getBlockPos());

                        SafeChunkClass Registry = World.getPersistentStateManager().getOrCreate(SafeChunkClass.TYPE);
                        Registry.AddChunk(World, CurrentChunk.getPos().x, CurrentChunk.getPos().z);

                        context.getSource().sendFeedback(() -> Text.literal(Registry.getChunkList()), false);
                        return 1;
                    })
                )

                .then(literal("RemoveChunk")
                    .executes(context -> {
                        context.getSource().sendFeedback(() -> Text.literal("Remove Called"), false);


                        return 1;
                    })
                )

        );
    }

}