package net.powato.lifesteal.world;

import com.mojang.serialization.Codec;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.Optional;

public class SafeChunkClass extends PersistentState {
    // create the PersistentStateType using the Supplier + Codec + DataFixTypes constructor
    public static final PersistentStateType<SafeChunkClass> TYPE =
            new PersistentStateType<>(
                    "powato_safe_chunks",                  // id (unique)
                    SafeChunkClass::new,                   // supplier (constructor)
                    Codec.unit(new SafeChunkClass()),     // trivial codec placeholder
                    DataFixTypes.SAVED_DATA_COMMAND_STORAGE // choose appropriate DataFixTypes entry
            );
    private static final String KEY = "SafeChunkRegistry";
    private final NbtList ChunkList = new NbtList();

    public SafeChunkClass() {
        super();
    }


    public void AddChunk(ServerWorld world, int ChunkX, int ChunkZ){
        NbtCompound chunkData = new NbtCompound();
        chunkData.putInt("X", ChunkX);
        chunkData.putInt("Z", ChunkZ);
        chunkData.putString("Dimension", world.getRegistryKey().getValue().toString());

        ChunkList.add(chunkData);
        markDirty();
    }

    public void RemoveChunk(ServerWorld world, int ChunkX, int ChunkZ){

        ChunkList.removeIf(element -> {
            NbtCompound chunk = (NbtCompound) element;
            String currentDimension = world.getRegistryKey().getValue().toString();

            Optional<Integer> x = chunk.getInt("X");
            Optional<Integer> z = chunk.getInt("Z");
            Optional<String> dim = chunk.getString("Dimension");

            return x.isPresent() && z.isPresent() && dim.isPresent() &&
                    x.get() == ChunkX &&
                    z.get() == ChunkZ &&
                    dim.get().equals(currentDimension);
            }
        );

        markDirty();
    }



//    public void load(NbtCompound tag) {
//        System.out.println("Load called");
//        ChunkList.clear();
//
//        Optional<NbtList> optionalList = tag.getList("ChunkList");
//        if (optionalList.isPresent()) {
//            NbtList list = optionalList.get();
//            for (int i = 0; i < list.size(); i++) {
//                ChunkList.add(list.get(i));
//            }
//        }
//    }
//
//
//
//    public NbtCompound writeNbt() {
//        System.out.println("Write called");
//        NbtCompound tag = new NbtCompound();
//        tag.put("ChunkList", ChunkList);
//        return tag;
//    }


    public String getChunkList() {
        if (ChunkList == null || ChunkList.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ChunkList.size(); i++) {
            var element = ChunkList.get(i);
            if (!(element instanceof NbtCompound)) continue;

            NbtCompound c = (NbtCompound) element;
            int x = c.getInt("X").get();
            int z = c.getInt("Z").get();
            String dim = c.getString("Dimension").get();

            sb.append("X=").append(x)
                    .append(", Z=").append(z)
                    .append(", Dim=").append(dim);

            if (i < ChunkList.size() - 1) sb.append("\n");
        }

        return sb.toString();
    }

}
