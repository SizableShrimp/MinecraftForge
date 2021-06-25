var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI')
var Opcodes = Java.type('org.objectweb.asm.Opcodes')
var VarInsn = Java.type('org.objectweb.asm.tree.VarInsnNode')
var FieldInsn = Java.type('org.objectweb.asm.tree.FieldInsnNode')
var MethodInsn = Java.type('org.objectweb.asm.tree.MethodInsnNode')

function initializeCoreMod() {
    return {
        'minecraft': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.Minecraft',
                'methodName': '<init>',
                'methodDesc': '(Lnet/minecraft/client/GameConfiguration;)V'
            },
            'transformer': function(methodNode) {
//            print(ASMAPI.methodNodeToString(methodNode))
            //PUTFIELD net/minecraft/client/Minecraft.field_110451_am : Lnet/minecraft/resources/IReloadableResourceManager;
                for (var i=0; i<methodNode.instructions.size(); i++) {
                    var ain = methodNode.instructions.get(i);
                    if (ain.getOpcode() == Opcodes.PUTFIELD && ain.name.equals("resourceManager")) {
                        print("found: "+ain);
                        var insn = ASMAPI.listOf(
                            new VarInsn(Opcodes.ALOAD, 0),
                            new VarInsn(Opcodes.ALOAD, 0),
                            new FieldInsn(Opcodes.GETFIELD, "net/minecraft/client/Minecraft", "resourcePackRepository", "Lnet/minecraft/resources/ResourcePackList;"),
                            new VarInsn(Opcodes.ALOAD, 0),
                            new FieldInsn(Opcodes.GETFIELD, "net/minecraft/client/Minecraft", "resourceManager", "Lnet/minecraft/resources/IReloadableResourceManager;"),
                            new VarInsn(Opcodes.ALOAD, 0),
                            new FieldInsn(Opcodes.GETFIELD, "net/minecraft/client/Minecraft", "clientPackSource", "Lnet/minecraft/client/resources/DownloadingPackFinder;"),
                            new MethodInsn(Opcodes.INVOKESTATIC, "net/minecraftforge/fmlclient/ClientModLoader", "begin", "(Lnet/minecraft/client/Minecraft;Lnet/minecraft/resources/ResourcePackList;Lnet/minecraft/resources/IReloadableResourceManager;Lnet/minecraft/client/resources/DownloadingPackFinder;)V", false)
                        );
                        methodNode.instructions.insert(ain, insn);
                    }
                }
                return methodNode;
            }
        },
    }
}
