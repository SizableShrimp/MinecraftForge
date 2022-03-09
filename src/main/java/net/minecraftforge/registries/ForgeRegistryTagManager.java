/*
 * Minecraft Forge - Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.registries;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.tags.IReverseTag;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.registries.tags.ITagManager;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Supplier;
import java.util.stream.Stream;

class ForgeRegistryTagManager<V extends IForgeRegistryEntry<V>> implements ITagManager<V>
{
    private final ForgeRegistry<V> owner;
    private volatile Map<TagKey<V>, ITag<V>> tags = new IdentityHashMap<>();

    ForgeRegistryTagManager(ForgeRegistry<V> owner)
    {
        this.owner = owner;
    }

    void bind(Map<TagKey<V>, HolderSet.Named<V>> holderTags)
    {
        IdentityHashMap<TagKey<V>, ITag<V>> newTags = new IdentityHashMap<>(this.tags);
        Set<TagKey<V>> bound = new HashSet<>();

        holderTags.forEach((key, holderSet) -> {
            bound.add(key);
            ((ForgeRegistryTag<V>) newTags.computeIfAbsent(key, ForgeRegistryTag::new)).bind(holderSet);
        });

        // Forcefully unbind any tags that didn't get bound
        Sets.difference(this.tags.keySet(), bound).forEach(key -> ((ForgeRegistryTag<V>) newTags.get(key)).bind(null));

        this.tags = newTags;
    }

    @NotNull
    @Override
    public ITag<V> getTag(TagKey<V> name)
    {
        ITag<V> tag = this.tags.get(name);

        if (tag == null)
        {
            // Create empty tag
            tag = new ForgeRegistryTag<>(name);

            // Mojang uses volatile and sets the tag map this way to not have the performance penalties of synced read access.
            // However, this can generate a lot of new maps. We should to look into performance alternatives.
            IdentityHashMap<TagKey<V>, ITag<V>> map = new IdentityHashMap<>(this.tags);
            map.put(name, tag);
            this.tags = map;
        }

        return tag;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @NotNull
    @Override
    public Optional<IReverseTag<V>> getReverseTag(V value)
    {
        // All Holders are implementors of IReverseTag
        return (Optional<IReverseTag<V>>) (Optional) this.owner.getHolder(value);
    }

    @Override
    public boolean isKnownTagName(TagKey<V> name)
    {
        ITag<V> tag = this.tags.get(name);
        return tag != null && tag.isBound();
    }

    @NotNull
    @Override
    public Iterator<ITag<V>> iterator()
    {
        return Iterators.unmodifiableIterator(this.tags.values().iterator());
    }

    @NotNull
    @Override
    public Stream<ITag<V>> stream()
    {
        return this.tags.values().stream();
    }

    @NotNull
    @Override
    public Stream<TagKey<V>> getTagNames()
    {
        return this.tags.keySet().stream();
    }

    @NotNull
    @Override
    public TagKey<V> createTagKey(ResourceLocation location)
    {
        return TagKey.create(this.owner.getRegistryKey(), location);
    }

    @NotNull
    @Override
    public TagKey<V> createOptionalTagKey(ResourceLocation location, @NotNull Set<Supplier<V>> defaults)
    {
        TagKey<V> tagKey = createTagKey(location);

        this.owner.getHolderHelper().ifPresent(h -> h.addOptionalTag(tagKey, defaults));

        return tagKey;
    }
}
