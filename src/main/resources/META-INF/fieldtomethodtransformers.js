function initializeCoreMod() {
    return {
        'potion': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.effect.MobEffectInstance'
            },
            'transformer': function(classNode) {
                var asmapi=Java.type('net.minecraftforge.coremod.api.ASMAPI')
                var fn = asmapi.mapField('f_19502_') // potion field - remap to mcp if necessary
                asmapi.redirectFieldToMethod(classNode, fn, asmapi.mapMethod('m_19544_'))
                return classNode;
            }
        },
        'flowingfluidblock': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.level.block.LiquidBlock'
            },
            'transformer': function(classNode) {
                var asmapi=Java.type('net.minecraftforge.coremod.api.ASMAPI')
                var fn = asmapi.mapField('f_54689_') // fluid field - remap to mcp if necessary
                asmapi.redirectFieldToMethod(classNode, fn, 'getFluid') // forge added method, doesn't need mapping
                return classNode;
            }
        },
        'bucketitem': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.item.BucketItem'
            },
            'transformer': function(classNode) {
                var asmapi=Java.type('net.minecraftforge.coremod.api.ASMAPI')
                var fn = asmapi.mapField('f_40687_') // containerFluid (wrongly named containedBlock) field - remap to mcp if necessary
                asmapi.redirectFieldToMethod(classNode, fn, 'getFluid') // forge added method, doesn't need mapping
                return classNode;
            }
        },
        'stairsblock': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.level.block.StairBlock'
            },
            'transformer': function(classNode) {
                var asmapi=Java.type('net.minecraftforge.coremod.api.ASMAPI')
                var blockField = asmapi.mapField('f_56858_') // modelBlock - remap to mcp if necessary
                asmapi.redirectFieldToMethod(classNode, blockField, 'getModelBlock') // forge added method, doesn't need mapping
                var stateField = asmapi.mapField('f_56859_') // modelState - remap to mcp if necessary
                asmapi.redirectFieldToMethod(classNode, stateField, 'getModelState') // forge added method, doesn't need mapping
                return classNode;
            }
        },
        'flowerpotblock': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.level.block.FlowerPotBlock'
            },
            'transformer': function(classNode) {
                var asmapi=Java.type('net.minecraftforge.coremod.api.ASMAPI')
                var fn = asmapi.mapField('f_53525_') // flower - remap to mcp if necessary
                asmapi.redirectFieldToMethod(classNode, fn, asmapi.mapMethod('m_53560_'))
                return classNode;
            }
        },
        'fishbucketitem': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.item.FishBucketItem'
            },
            'transformer': function(classNode) {
                var asmapi=Java.type('net.minecraftforge.coremod.api.ASMAPI')
                var fn = asmapi.mapField('f_41262_') // fishType - remap to mcp if necessary
                asmapi.redirectFieldToMethod(classNode, fn, asmapi.mapMethod('getFishType'))
                return classNode;
            }
        },
        'itemstack': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.item.ItemStack'
            },
            'transformer': function(classNode) {
                var asmapi=Java.type('net.minecraftforge.coremod.api.ASMAPI')
                var fn = asmapi.mapField('f_41589_') // item - remap to mcp if necessary
                asmapi.redirectFieldToMethod(classNode, fn, asmapi.mapMethod('m_41720_'))
                return classNode;
            }
        }
    }
}
