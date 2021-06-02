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

package net.minecraftforge.event.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * This event is fired when a projectile entity impacts something.
 * This event is fired via {@link ForgeEventFactory#onProjectileImpact(Entity, RayTraceResult)}
 * Subclasses of this event exist for more specific types of projectile.
 * This event is fired for all vanilla projectiles by Forge,
 * custom projectiles should fire this event and check the result in a similar fashion.
 * This event is cancelable. When canceled, the impact will not be processed.
 * Killing or other handling of the entity after event cancellation is up to the modder.
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 */
@Cancelable
public class ProjectileImpactEvent extends EntityEvent
{
    private final HitResult ray;

    public ProjectileImpactEvent(Entity entity, HitResult ray)
    {
        super(entity);
        this.ray = ray;
    }

    public HitResult getRayTraceResult()
    {
        return ray;
    }

    @Cancelable
    public static class Arrow extends ProjectileImpactEvent
    {
        private final AbstractArrow arrow;

        public Arrow(AbstractArrow arrow, HitResult ray)
        {
            super(arrow, ray);
            this.arrow = arrow;
        }

        public AbstractArrow getArrow()
        {
            return arrow;
        }
    }

    @Cancelable
    public static class Fireball extends ProjectileImpactEvent
    {
        private final AbstractHurtingProjectile fireball;

        public Fireball(AbstractHurtingProjectile fireball, HitResult ray)
        {
            super(fireball, ray);
            this.fireball = fireball;
        }

        public AbstractHurtingProjectile getFireball()
        {
            return fireball;
        }
    }

    @Cancelable
    public static class Throwable extends ProjectileImpactEvent
    {
        private final ThrowableProjectile throwable;

        public Throwable(ThrowableProjectile throwable, HitResult ray)
        {
            super(throwable, ray);
            this.throwable = throwable;
        }

        public ThrowableProjectile getThrowable()
        {
            return throwable;
        }
    }

    /**
     * Event is cancellable, causes firework to ignore the current hit and continue on its journey.
     */
    @Cancelable
    public static class FireworkRocket extends ProjectileImpactEvent
    {
        private final FireworkRocketEntity fireworkRocket;

        public FireworkRocket(FireworkRocketEntity fireworkRocket, HitResult ray)
        {
            super(fireworkRocket, ray);
            this.fireworkRocket = fireworkRocket;
        }

        public FireworkRocketEntity getFireworkRocket()
        {
            return fireworkRocket;
        }
    }
}
