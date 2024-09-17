package com.eseabsolute.zhonglv12et.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import oshi.util.tuples.Triplet;

import java.util.HashMap;
import java.util.Map;

public class ZhongLv12ETClient implements ClientModInitializer {
    public MinecraftClient client;

    private static final Map<Identifier, Triplet<Text, Boolean, Integer>> INSTRUMENT_MAP = new HashMap<>();
    static {
        // Triplet: instrumentName, isPercussion, initialOctave
        INSTRUMENT_MAP.put(new Identifier("minecraft:block.note_block.banjo"), new Triplet<>(Text.translatable("instrument.banjo"), true, 3));
        INSTRUMENT_MAP.put(new Identifier("minecraft:block.note_block.basedrum"), new Triplet<>(Text.translatable("instrument.basedrum"), false, 0));
        INSTRUMENT_MAP.put(new Identifier("minecraft:block.note_block.bass"), new Triplet<>(Text.translatable("instrument.bass"), true, 1));
        INSTRUMENT_MAP.put(new Identifier("minecraft:block.note_block.bell"), new Triplet<>(Text.translatable("instrument.bell"), true, 5));
        INSTRUMENT_MAP.put(new Identifier("minecraft:block.note_block.bit"), new Triplet<>(Text.translatable("instrument.bit"), true, 3));
        INSTRUMENT_MAP.put(new Identifier("minecraft:block.note_block.chime"), new Triplet<>(Text.translatable("instrument.chime"), true, 5));
        INSTRUMENT_MAP.put(new Identifier("minecraft:block.note_block.cow_bell"), new Triplet<>(Text.translatable("instrument.cow_bell"), true, 4));
        INSTRUMENT_MAP.put(new Identifier("minecraft:block.note_block.didgeridoo"), new Triplet<>(Text.translatable("instrument.didgeridoo"), true, 1));
        INSTRUMENT_MAP.put(new Identifier("minecraft:block.note_block.flute"), new Triplet<>(Text.translatable("instrument.flute"), true, 4));
        INSTRUMENT_MAP.put(new Identifier("minecraft:block.note_block.guitar"), new Triplet<>(Text.translatable("instrument.guitar"), true, 2));
        INSTRUMENT_MAP.put(new Identifier("minecraft:block.note_block.harp"), new Triplet<>(Text.translatable("instrument.harp"), true, 3));
        INSTRUMENT_MAP.put(new Identifier("minecraft:block.note_block.hat"), new Triplet<>(Text.translatable("instrument.hat"), false, 0));
        INSTRUMENT_MAP.put(new Identifier("minecraft:block.note_block.iron_xylophone"), new Triplet<>(Text.translatable("instrument.iron_xylophone"), true, 3));
        INSTRUMENT_MAP.put(new Identifier("minecraft:block.note_block.pling"), new Triplet<>(Text.translatable("instrument.pling"), true, 3));
        INSTRUMENT_MAP.put(new Identifier("minecraft:block.note_block.snare"), new Triplet<>(Text.translatable("instrument.snare"), false, 0));
        INSTRUMENT_MAP.put(new Identifier("minecraft:block.note_block.xylophone"), new Triplet<>(Text.translatable("instrument.xylophone"), true, 5));
    }




    @Override
    public void onInitializeClient() {
        client = MinecraftClient.getInstance();
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            client.getSoundManager().registerListener(this::onPlaySound);
        });
    }

    private void onPlaySound(SoundInstance sound, WeightedSoundSet weightedSoundSet) {
        Identifier soundId = sound.getId();
        if (soundId.toString().startsWith("minecraft:block.note_block")) {
            Triplet<Text, Boolean, Integer> instrument = INSTRUMENT_MAP.get(soundId);
            if (instrument == null) { return; }
            ClientPlayerEntity player = client.player;
            if (player == null) { return; }
            Text toneType = instrument.getA();
            if (instrument.getB()) { // not percussion
                Text noteName = getNoteNameFromPitch(sound.getPitch(), instrument.getC());
                Text noteInfoDisplayMsg = Text.translatable("info.note", toneType, noteName);
                player.sendMessage(noteInfoDisplayMsg);
            } else {
                Text noteInfoDisplayMsg = Text.translatable("info.percussion", toneType);
                player.sendMessage(noteInfoDisplayMsg);
            }
        }
    }

    private Text getNoteNameFromPitch(float pitch, int initialOctave) {
        String[] notes = {"F#", "G", "G#", "A", "A#", "B", "C", "C#", "D", "D#", "E", "F"};
        boolean[] need2Params = {true, false, true, false, true, false, false, true, false, true, false, false};

        // 12-ET, common ratio = $2^{\frac{1}{12}}$
        float adjustedPitch = (float) (Math.log(pitch / 0.5) / Math.log(2) * 12);

        int noteIndex = Math.round(adjustedPitch) % 12;
        if (noteIndex < 0) noteIndex += 12;
        int octaveOffset = Math.round(adjustedPitch) / 12;
        int range = initialOctave + octaveOffset;

        if (need2Params[noteIndex]) {
            return Text.translatable("note.%s".formatted(notes[noteIndex]), range, range);
        } else {
            return Text.translatable("note.%s".formatted(notes[noteIndex]), range);
        }
    }
}
