package com.github.bnt4.enhancedsurvival.util.item;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ItemSerialization {

    /**
     * Serializes an {@link ItemStack} to a Base64-encoded string using {@link BukkitObjectOutputStream}.
     * If an error occurs, null is returned.
     *
     * @param itemStack item to serialize
     * @return serialized item as string or null in case of an exception
     */
    public static String serializeItemStack(ItemStack itemStack) {
        if (itemStack == null) return null;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(stream)) {

            dataOutput.writeObject(itemStack);
            dataOutput.flush();

            return Base64.getEncoder().encodeToString(stream.toByteArray());
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Deserializes an {@link ItemStack} from a string using {@link BukkitObjectInputStream}.
     * Supports both new single-encoded Base64 strings and legacy double-encoded strings
     * previously produced by Base64Coder.
     * If an error occurs, null is returned.
     *
     * @param s string to create the item from
     * @return deserialized item or null in case of an exception
     */
    public static ItemStack deserializeItemStack(String s) {
        if (s == null || s.equals("null") || s.trim().isEmpty()) return null;
        try {
            // First decode using MIME decoder to tolerate whitespace/newlines
            byte[] bytes = Base64.getMimeDecoder().decode(s.trim());

            // Handle legacy double-encoded strings if present
            try {
                String inner = new String(bytes, StandardCharsets.UTF_8).trim();
                // Check if the decoded payload is itself another Base64 string
                if (inner.matches("^[A-Za-z0-9+/=\\r\\n]+$")) {
                    byte[] secondPass = Base64.getMimeDecoder().decode(inner);
                    // Verify magic bytes for Java serialized object (0xACED)
                    if (secondPass.length >= 2 && secondPass[0] == (byte) 0xAC && secondPass[1] == (byte) 0xED) {
                        bytes = secondPass;
                    }
                }
            } catch (Exception ignored) {
                // If secondary decode fails, proceed with initial decoded bytes
            }

            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
                 BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
                return (ItemStack) dataInput.readObject();
            }
        } catch (Exception ex) {
            return null;
        }
    }
}
