package net.powato.lifesteal.SafeChunks;

import com.mojang.serialization.Codec;
import com.nimbusds.jose.util.ArrayUtils;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentState;
import static net.powato.lifesteal.LifestealRegulate.MOD_ID;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.powato.lifesteal.networking.ShowSafeChunksPayload;
import net.powato.lifesteal.networking.UpdateSafeChunksPayload;

import java.util.ArrayList;
import java.util.Arrays;

/*
* Overview
* https://wiki.fabricmc.net/tutorial:persistent_states
*
*
* */
public class SafeChunks extends PersistentState {
    // create the PersistentStateType using the Supplier + Codec + DataFixTypes constructor

    private static final String KEY = "SafeChunkRegistry";
    private final NbtList ChunkList = new NbtList();
    public String ChunkData = ""; // "|X,Z,Dim|X,Z,Dim|X,Z,Dim" ...
    private ArrayList<ArrayList<String>> localData = new ArrayList<>();

    private SafeChunks(){

        ConstructLocalData();
    }
    private SafeChunks(String NewChunkData) {
        this.ChunkData = NewChunkData;
        ConstructLocalData();
    }

    private static final Codec<SafeChunks> CODEC = Codec.STRING.fieldOf("ChunkData").codec().xmap(
            SafeChunks::new,
            SafeChunks::GetChunkData
    );

    private static final PersistentStateType<SafeChunks> type = new PersistentStateType<>(
            (String) MOD_ID,
            SafeChunks::new,
            CODEC,
            null
    );

    public static SafeChunks getServerState(MinecraftServer server){
        ServerWorld serverWorld = server.getWorld(World.OVERWORLD);
        assert serverWorld != null;

        SafeChunks state = serverWorld.getPersistentStateManager().getOrCreate(type);

        // Consider turning this on if data isn't being saved properly to force saving every server instance regardless of whether a change was made or not
        // state.markDirty();

        return state;
    }

    private void ConstructLocalData(){
        localData = new ArrayList<>();
        if (ChunkData == ""){ return; }


        String current = "";
        int step = 0; // 0=x, 1=z
        for (int i=1; i<ChunkData.length(); i++){ // skip first pipe element
            char spot = ChunkData.charAt(i);

            if (spot == ','){

                switch (step){
                    case 0:
                        localData.add(new ArrayList<String>());
                        localData.getLast().add(current);
                        break;
                    case 1:
                        localData.getLast().add(current);
                        break;

                }

                current = "";
                step++;

                continue;
            } else if (spot == '|'){
                localData.getLast().add(current);
                current = "";
                step = 0;

                continue;
            }

            current += spot;

        }
        localData.getLast().add(current);
    }

    public boolean isSafeChunk(ChunkPos chunkPos, RegistryKey<World> dimension){
        String comparison = chunkPos.x + "," + chunkPos.z + "," + dimension.getValue().toString();

        ArrayList<String> Chunks = new ArrayList<> (Arrays.asList(ChunkData.split("\\|")));
        Chunks.remove(0);

        for (String Chunk: Chunks){
            if (Chunk.equals(comparison)){ // in safe chunk
                return true;
            }
        }

        return false;
    }

    /*
     *
     *
     * @return int - 0 for successful addition; -1 for chunk already added
     * */
    public int AddChunk(ServerWorld World, String data){

        if (ChunkData.indexOf(data) != -1){ // chunk exists
            return -1;
        }

        ChunkData += "|" + data;
        ConstructLocalData();

        markDirty();
        UpdateClients(World);

        return 0;
    }

    /*
    *
    *
    * @return int - 0 for successful removal; -1 for chunk not in database
    * */
    public int RemoveChunk(ServerWorld World, String data){
        int result = ChunkData.indexOf(data);

        if (result != -1){
            ChunkData = ChunkData.substring(0, result-1) + ChunkData.substring(result + data.length());
            ConstructLocalData();

            markDirty();
            UpdateClients(World);

            return 0;

        } else {
            return -1;
        }

    }
    /*
     *
     *
     * @return int - 0 for successful reset; -1 for already empty database; -2 for mismatch error
     * */
    public int ResetChunks(ServerWorld World){
        boolean cdEmpty = ChunkData.isEmpty();
        boolean ldEmpty = localData.isEmpty();

        if ( (cdEmpty || ldEmpty) && !(cdEmpty && ldEmpty)){ // if only 1 is empty but not both (XOR)
            return -2;
        } else if (cdEmpty && ldEmpty){
            return -1;
        }


        ChunkData = "";
        ConstructLocalData();

        markDirty();
        UpdateClients(World);

        return 0;
    }

    public String GetChunkData(){
        return ChunkData;
    }

    private void UpdateClients(ServerWorld World){

        for (ServerPlayerEntity Player : World.getServer().getPlayerManager().getPlayerList()){
            ServerPlayNetworking.send(Player, new UpdateSafeChunksPayload(ChunkData));
        }

    }

}
