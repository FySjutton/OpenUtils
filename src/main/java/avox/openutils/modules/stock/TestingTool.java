package avox.openutils.modules.stock;

import com.mojang.serialization.DynamicOps;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static avox.openutils.OpenUtils.LOGGER;
import static avox.openutils.modules.stock.StockModule.stockItems;

public class TestingTool {
    public static final List<ItemStack> itemCache = new ArrayList<>();
    private static final Path cachePath = FabricLoader.getInstance().getConfigDir().resolve("testing_item_cache.dat");

    public static void save() {
        List<String> encoded = new ArrayList<>();
        for (ItemStack stack : itemCache) {
            String data = encode(stack);
            if (data != null) encoded.add(data);
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cachePath.toFile()))) {
            oos.writeObject(encoded);
            LOGGER.info("TestingTool: Saved " + encoded.size() + " items.");
        } catch (IOException e) {
            LOGGER.error("TestingTool: Failed to save.", e);
        }
    }

    public static void load() {
        if (!cachePath.toFile().exists()) {
            LOGGER.warn("TestingTool: No cache file found.");
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cachePath.toFile()))) {
            List<String> encoded = (List<String>) ois.readObject();
            itemCache.clear();

            for (String s : encoded) {
                ItemStack itemStack = decode(s);
                if (!itemStack.isEmpty()) {
                    itemCache.add(itemStack);
                    stockItems.add(new StockItem(itemStack));
                }
            }

            LOGGER.info("TestingTool: Loaded " + itemCache.size() + " items.");
        } catch (Exception e) {
            LOGGER.error("TestingTool: Failed to load.", e);
        }
    }

    public static String encode(ItemStack itemStack) {
        DynamicOps<NbtElement> ops = RegistryOps.of(NbtOps.INSTANCE, MinecraftClient.getInstance().world.getRegistryManager());
        Optional<NbtElement> optionalNbt = ItemStack.CODEC.encodeStart(ops, itemStack).resultOrPartial(System.err::println);

        if (optionalNbt.isEmpty()) {
            System.err.println("Failed to encode ItemStack: " + itemStack);
            return null;
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            NbtIo.writeCompound((NbtCompound) optionalNbt.get(), new DataOutputStream(outputStream));
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ItemStack decode(String data) {
        if (data == null) return ItemStack.EMPTY;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data))) {
            NbtCompound nbtCompound = NbtIo.readCompound(new DataInputStream(inputStream));

            ClientWorld world = MinecraftClient.getInstance().world;
            if (world == null) return ItemStack.EMPTY;

            DynamicOps<NbtElement> ops = RegistryOps.of(NbtOps.INSTANCE, world.getRegistryManager());
            return ItemStack.CODEC.parse(ops, nbtCompound)
                    .resultOrPartial(System.err::println)
                    .orElse(ItemStack.EMPTY);
        } catch (IOException e) {
            e.printStackTrace();
            return ItemStack.EMPTY;
        }
    }
}
