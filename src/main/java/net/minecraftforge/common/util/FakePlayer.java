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

package net.minecraftforge.common.util;

import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.stats.Stat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.UUID;

//Preliminary, simple Fake Player class
public class FakePlayer extends ServerPlayer
{
    public FakePlayer(ServerLevel world, GameProfile name)
    {
        super(world.getServer(), world, name, new ServerPlayerGameMode(world));
    }

    @Override public Vec3 position(){ return new Vec3(0, 0, 0); }
    @Override public BlockPos blockPosition(){ return BlockPos.ZERO; }
    @Override public void displayClientMessage(Component chatComponent, boolean actionBar){}
    @Override public void sendMessage(Component component, UUID senderUUID) {}
    @Override public void awardStat(Stat par1StatBase, int par2){}
    //@Override public void openGui(Object mod, int modGuiId, World world, int x, int y, int z){}
    @Override public boolean isInvulnerableTo(DamageSource source){ return true; }
    @Override public boolean canHarmPlayer(Player player){ return false; }
    @Override public void die(DamageSource source){ return; }
    @Override public void tick(){ return; }
    @Override public void updateOptions(ServerboundClientInformationPacket pkt){ return; }
    @Override @Nullable public MinecraftServer getServer() { return ServerLifecycleHooks.getCurrentServer(); }
}
