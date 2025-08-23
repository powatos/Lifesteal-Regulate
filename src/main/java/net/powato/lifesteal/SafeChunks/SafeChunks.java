package net.powato.lifesteal.SafeChunks;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
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
import net.powato.lifesteal.networking.UpdateSafeChunksPayload;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
    public String ChunkData = ""; // "|X,Z,Dim,UUID|X,Z,Dim,UUID|X,Z,Dim,UUID" ...
    private ArrayList<ArrayList<String>> localData = new ArrayList<>();
    private static int maxChunks;

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

    public static void readMaxSafeChunks() {
        // TODO: add new json file if not found
        Gson gson = new Gson();
        Path path = FabricLoader.getInstance().getConfigDir().resolve("lifesteal.json");

        Map<String, Object> config;

        if (!Files.exists(path)){
            config = new HashMap<>();
            config.put("maxSafeChunks", 20);

            try (Writer writer = new FileWriter(path.toFile())){
                gson.toJson(config, writer);
            } catch (IOException e) {
                throw new RuntimeException("Failed to write config", e);
            }
        }

        try (Reader reader = new FileReader(path.toFile())) {
            Type type = new TypeToken<Map<String, Object>>() {
            }.getType();
            config = gson.fromJson(reader, type);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read config", e);
        }

        maxChunks = ((Number) config.getOrDefault("maxSafeChunks", 20)).intValue();
    }

    private void ConstructLocalData(){
        localData = new ArrayList<>();
        if (ChunkData.equals("")){ return; }


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
                    case 2:
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
            String onlyChunkData = Chunk.substring(0, Chunk.length() - 37); // strips uuid from data (removes comma at end)

            if (onlyChunkData.equals(comparison)){ // in safe chunk
                return true;
            }
        }

        return false;
    }

    /*
     *
     *
     * @return int - 0 for successful addition; -1 for chunk already added; -2 for player maxxed out chunks
     * */
    public int AddChunk(ServerWorld World, String data){

        String onlyChunkData = data.substring(0, data.length() - 36); // strips uuid from data (keeps comma at end)

        if (ChunkData.contains(onlyChunkData)){ // chunk exists
            return -1;
        }

        int playerTotalChunks = 0;
        for (ArrayList<String> Chunk : localData){
            String chunkUUID = Chunk.getLast();
            String playerUUID = data.split(",")[3];

            if (chunkUUID.equals(playerUUID)){
                playerTotalChunks++;
            }
        }

        if (playerTotalChunks >= maxChunks){
            return -2;
        }

        ChunkData += data;
        ConstructLocalData();

        markDirty();
        UpdateClients(World);

        return 0;
    }

    /*
    *
    *
    * @return int - 0 for successful removal; -1 for chunk not in database; -2 for unauthorized remove
    * */
    public int RemoveChunk(ServerWorld World, String data){
        String onlyChunkData = data.substring(0, data.length() - 36); // strips uuid from data (keeps comma at end)
        int result = ChunkData.indexOf(onlyChunkData);

        if (result == -1) {return -1;}

        String Chunk = ChunkData.substring(result, result + data.length());
        if (!Chunk.equals(data)){ // chunk was added by different uuid
            return -2;
        }

        // remove
        ChunkData = ChunkData.substring(0, result) + ChunkData.substring(result + data.length());
        ConstructLocalData();

        markDirty();
        UpdateClients(World);

        return 0;



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
