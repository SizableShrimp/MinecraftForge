/*
 * Minecraft Forge
 * Copyright (c) 2016-2021.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.client.event.sound;

import net.minecraftforge.eventbus.api.Event;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import com.mojang.blaze3d.audio.Channel;

public class SoundEvent extends Event
{
    private final SoundEngine manager;
    public SoundEvent(SoundEngine manager)
    {
        this.manager = manager;
    }

    public SoundEngine getManager()
    {
        return manager;
    }

    public static class SoundSourceEvent extends SoundEvent
    {
        private final SoundInstance sound;
        private final Channel source;
        private final String name;

        public SoundSourceEvent(SoundEngine manager, SoundInstance sound, Channel source)
        {
            super(manager);
            this.name = sound.getLocation().getPath();
            this.sound = sound;
            this.source = source;
        }

        public SoundInstance getSound()
        {
            return sound;
        }

        public Channel getSource()
        {
            return source;
        }

        public String getName()
        {
            return name;
        }
    }
}
