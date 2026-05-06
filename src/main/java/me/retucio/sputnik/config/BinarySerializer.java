package me.retucio.sputnik.config;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BinarySerializer {

    private static final int MAGIC_NUMBER = 0x53505554;  // "SPUT"
    private static final int VERSION = 2;

    private static final byte TYPE_NULL = 0;
    private static final byte TYPE_BOOLEAN = 1;
    private static final byte TYPE_INT = 2;
    private static final byte TYPE_DOUBLE = 3;
    private static final byte TYPE_STRING = 4;
    private static final byte TYPE_MAP = 5;
    private static final byte TYPE_INT_ARRAY = 6;
    private static final byte TYPE_FRAME_DATA = 7;

    public static void writeConfig(ClientConfig config, File file)
            throws IOException {
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)))) {

            // cabezal
            out.writeInt(MAGIC_NUMBER);
            out.writeInt(VERSION);

            // mapas
            writeStringBooleanMap(out, config.moduleStates);
            writeSettingsMap(out, config.settings);
            writeStringIntArrayMap(out, config.settingsPanels);
            writeExtendableFrames(out, config.extendablePanels);
            writeStringIntArrayMap(out, config.hudPositions);
            writeStringBooleanMap(out, config.hudVisibilities);
            writeStringStringMap(out, config.hudImagePaths);
            writeStringStringMap(out, config.friends);

            // posición de la barra de búsqueda
            if (config.searchBarPosition != null) {
                out.writeBoolean(true);
                out.writeInt(config.searchBarPosition[0]);
                out.writeInt(config.searchBarPosition[1]);
            } else {
                out.writeBoolean(false);
            }
        }
    }

    public static ClientConfig readConfig(File file)
            throws IOException {
        if (!file.exists() || file.length() == 0) {
            throw new IOException("archivo de config. vacío o inexistente");
        }

        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {

            if (file.length() < 8) {
                throw new IOException("archivo demasiado pequeño para ser válido");
            }

            // verificar cabezal
            int magic = in.readInt();
            if (magic != MAGIC_NUMBER) {
                throw new IOException("formato de archivo inválido: " + magic);
            }

            int version = in.readInt();
            if (version != VERSION) {
                throw new IOException("versión inválida: " + version);
            }

            ClientConfig config = new ClientConfig();

            // leer mapas
            config.moduleStates = readStringBooleanMap(in);
            config.settings = readSettingsMap(in);
            config.settingsPanels = readStringIntArrayMap(in);
            config.extendablePanels = readExtendablePanels(in);
            config.hudPositions = readStringIntArrayMap(in);
            config.hudVisibilities = readStringBooleanMap(in);
            config.hudImagePaths = readStringStringMap(in);
            config.friends = readStringStringMap(in);

            // leer pos. de la barra de búsqueda
            if (in.readBoolean()) {
                config.searchBarPosition = new int[]{in.readInt(), in.readInt()};
            }

            return config;
        }
    }

    private static void writeStringBooleanMap(DataOutputStream out, Map<String, Boolean> map)
            throws IOException {
        if (map == null) {
            out.writeInt(0);
            return;
        }
        out.writeInt(map.size());
        for (Map.Entry<String, Boolean> entry : map.entrySet()) {
            writeString(out, entry.getKey());
            out.writeBoolean(entry.getValue());
        }
    }

    private static Map<String, Boolean> readStringBooleanMap(DataInputStream in)
            throws IOException {
        int size = in.readInt();
        Map<String, Boolean> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            String key = readString(in);
            boolean value = in.readBoolean();
            map.put(key, value);
        }
        return map;
    }

    private static void writeSettingsMap(DataOutputStream out, Map<String, Object> map)
            throws IOException {
        if (map == null) {
            out.writeInt(0);
            return;
        }
        out.writeInt(map.size());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            writeString(out, entry.getKey());
            writeSettingValue(out, entry.getValue());
        }
    }

    private static Map<String, Object> readSettingsMap(DataInputStream in)
            throws IOException {
        int size = in.readInt();
        Map<String, Object> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            String key = readString(in);
            Object value = readSettingValue(in);
            map.put(key, value);
        }
        return map;
    }

    private static void writeSettingValue(DataOutputStream out, Object value)
            throws IOException {
        switch (value) {
            case Boolean b -> {
                out.writeByte(TYPE_BOOLEAN);
                out.writeBoolean(b);
            }
            case Integer i -> {
                out.writeByte(TYPE_INT);
                out.writeInt(i);
            }
            case Double v -> {
                out.writeByte(TYPE_DOUBLE);
                out.writeDouble(v);
            }
            case String s -> {
                out.writeByte(TYPE_STRING);
                writeString(out, s);
            }
            case Map<?, ?> map -> {
                out.writeByte(TYPE_MAP);
                writeGenericMap(out, map);
            }  // fallback
            case null, default -> out.writeByte(TYPE_NULL);
        }
    }

    private static Object readSettingValue(DataInputStream in) throws IOException {
        byte type = in.readByte();
        return switch (type) {
            case TYPE_BOOLEAN -> in.readBoolean();
            case TYPE_INT -> in.readInt();
            case TYPE_DOUBLE -> in.readDouble();
            case TYPE_STRING -> readString(in);
            case TYPE_MAP -> readGenericMap(in);
            default -> null;
        };
    }

    private static void writeGenericMap(DataOutputStream out, Map<?, ?> map)
            throws IOException {
        if (map == null) {
            out.writeInt(0);
            return;
        }
        out.writeInt(map.size());
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            writeObject(out, entry.getKey());
            writeObject(out, entry.getValue());
        }
    }

    private static Map<Object, Object> readGenericMap(DataInputStream in)
            throws IOException {
        int size = in.readInt();
        Map<Object, Object> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            Object key = readObject(in);
            Object value = readObject(in);
            map.put(key, value);
        }
        return map;
    }

    private static void writeObject(DataOutputStream out, Object obj)
            throws IOException {
        switch (obj) {
            case String s -> {
                out.writeByte(TYPE_STRING);
                writeString(out, s);
            }
            case Boolean b -> {
                out.writeByte(TYPE_BOOLEAN);
                out.writeBoolean(b);
            }
            case Number number -> {
                if (obj instanceof Integer) {
                    out.writeByte(TYPE_INT);
                    out.writeInt((Integer) obj);
                } else if (obj instanceof Double) {
                    out.writeByte(TYPE_DOUBLE);
                    out.writeDouble((Double) obj);
                }
            }
            default -> {
                out.writeByte(TYPE_STRING);
                writeString(out, obj.toString());
            }
        }
    }

    private static Object readObject(DataInputStream in) throws IOException {
        byte type = in.readByte();
        return switch (type) {
            case TYPE_STRING -> readString(in);
            case TYPE_BOOLEAN -> in.readBoolean();
            case TYPE_INT -> in.readInt();
            case TYPE_DOUBLE -> in.readDouble();
            default -> null;
        };
    }

    private static void writeStringIntArrayMap(DataOutputStream out, Map<String, int[]> map)
            throws IOException {
        if (map == null) {
            out.writeInt(0);
            return;
        }
        out.writeInt(map.size());
        for (Map.Entry<String, int[]> entry : map.entrySet()) {
            writeString(out, entry.getKey());
            int[] array = entry.getValue();
            if (array == null) {
                out.writeInt(0);
            } else {
                out.writeInt(array.length);
                for (int value : array) {
                    out.writeInt(value);
                }
            }
        }
    }

    private static Map<String, int[]> readStringIntArrayMap(DataInputStream in)
            throws IOException {
        int size = in.readInt();
        Map<String, int[]> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            String key = readString(in);
            int arraySize = in.readInt();
            int[] array = new int[arraySize];
            for (int j = 0; j < arraySize; j++) {
                array[j] = in.readInt();
            }
            map.put(key, array);
        }
        return map;
    }

    private static void writeExtendableFrames(DataOutputStream out, Map<String, ClientConfig.PanelData> map)
            throws IOException {
        if (map == null) {
            out.writeInt(0);
            return;
        }
        out.writeInt(map.size());
        for (Map.Entry<String, ClientConfig.PanelData> entry : map.entrySet()) {
            writeString(out, entry.getKey());
            ClientConfig.PanelData data = entry.getValue();
            out.writeInt(data.x());
            out.writeInt(data.y());
            out.writeBoolean(data.extended());
        }
    }

    private static Map<String, ClientConfig.PanelData> readExtendablePanels(DataInputStream in) throws IOException {
        int size = in.readInt();
        Map<String, ClientConfig.PanelData> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            String key = readString(in);
            int x = in.readInt();
            int y = in.readInt();
            boolean extended = in.readBoolean();
            map.put(key, new ClientConfig.PanelData(x, y, extended));
        }
        return map;
    }

    private static void writeStringStringMap(DataOutputStream out, Map<String, String> map)
        throws IOException {
        if (map == null) {
            out.writeInt(0);
            return;
        }
        out.writeInt(map.size());
        for (Map.Entry<String, String> entry : map.entrySet()) {
            writeString(out, entry.getKey());
            writeString(out, entry.getValue());
        }
    }

    private static Map<String, String> readStringStringMap(DataInputStream in)
        throws IOException {
        int size = in.readInt();
        Map<String, String> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            String key = readString(in);
            String value = readString(in);
            map.put(key, value);
        }
        return map;
    }

    private static void writeString(DataOutputStream out, String str) throws IOException {
        if (str == null) {
            out.writeInt(-1);
            return;
        }
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    private static String readString(DataInputStream in) throws IOException {
        int length = in.readInt();
        if (length == -1) return null;
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
