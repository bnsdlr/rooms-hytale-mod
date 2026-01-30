package de.bsdlr.rooms.room;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TranslationProperties {
    public static final BuilderCodec<TranslationProperties> CODEC = BuilderCodec.builder(TranslationProperties.class, TranslationProperties::new)
            .appendInherited(new KeyedCodec<>("Name", Codec.STRING), (data, s) -> data.name = s, data -> data.name, (o, p) -> o.name = p.name)
            .documentation("The translation key for the name of this item.")
            .metadata(new UIEditor(new UIEditor.LocalizationKeyField("server.rooms.{assetId}.name", true)))
            .add()
            .appendInherited(
                    new KeyedCodec<>("Description", Codec.STRING), (data, s) -> data.description = s, data -> data.description, (o, p) -> o.description = p.description
            )
            .documentation("The translation key for the description of this item.")
            .metadata(new UIEditor(new UIEditor.LocalizationKeyField("server.rooms.{assetId}.description", true)))
            .add()
            .build();
    @Nullable
    private String name;
    @Nullable
    private String description;

    TranslationProperties() {
    }

    public TranslationProperties(@Nonnull String name, @Nonnull String description) {
        this.name = name;
        this.description = description;
    }

    @Nullable
    public String getName() {
        return this.name;
    }

    @Nullable
    public String getDescription() {
        return this.description;
    }

}

