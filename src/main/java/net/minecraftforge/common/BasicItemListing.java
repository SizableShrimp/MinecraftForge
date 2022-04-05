/*
 * Minecraft Forge - Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.trading.CarryableTrade;
import net.minecraft.world.item.trading.MerchantOffer;

/**
 * A default, exposed implementation of ITrade.  All of the other implementations of ITrade (in VillagerTrades) are not public.
 * This class contains everything needed to make a MerchantOffer, the actual "trade" object shown in trading guis.
 */
public class BasicItemListing implements ItemListing
{

    protected final CarryableTrade price;
    protected final CarryableTrade result;
    protected final int maxTrades;
    protected final int xp;
    protected final float priceMult;

    public BasicItemListing(CarryableTrade price, CarryableTrade result, int maxTrades, int xp, float priceMult)
    {
        this.price = price;
        this.result = result;
        this.maxTrades = maxTrades;
        this.xp = xp;
        this.priceMult = priceMult;
    }

    @Override
    @Nullable
    public MerchantOffer getOffer(Entity merchant, Random rand)
    {
        return new MerchantOffer(price, result, maxTrades, xp, priceMult);
    }

}
