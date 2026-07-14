package ox.incognito;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class MacTransformer implements ClassFileTransformer, Opcodes {
    @Override
    public byte[] transform(
        ClassLoader loader,
        String className,
        Class<?> classBeingRedefined,
        ProtectionDomain protectionDomain,
        byte[] classfileBuffer
    ) {
        if (className == null || classfileBuffer == null) {
            return null;
        }
        if ("java/net/NetworkInterface".equals(className)) {
            return patchNetworkInterface(classfileBuffer);
        }
        if ("sun/management/RuntimeImpl".equals(className)) {
            return patchInputArgs(classfileBuffer);
        }
        if (className.endsWith("DesktopPlatformInfoProvider")
            || className.contains("PlatformInfo")) {
            return patchPlatformInfo(classfileBuffer);
        }
        return null;
    }

    private byte[] patchNetworkInterface(byte[] bytes) {
        try {
            ClassReader reader = new ClassReader(bytes);
            ClassWriter writer = new ClassWriter(reader, 0);
            ClassVisitor visitor = new ClassVisitor(ASM9, writer) {
                @Override
                public MethodVisitor visitMethod(
                    int access, String name, String descriptor, String signature, String[] exceptions
                ) {
                    MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                    if ("getHardwareAddress".equals(name) && "()[B".equals(descriptor)) {
                        return new GetHardwareAddressPatcher(mv);
                    }
                    return mv;
                }
            };
            reader.accept(visitor, 0);
            AgentLog.info("Bytecode patch applied to NetworkInterface.getHardwareAddress()");
            return writer.toByteArray();
        } catch (Exception e) {
            AgentLog.error("NetworkInterface patch failed: " + e.getMessage());
            return null;
        }
    }

    private byte[] patchInputArgs(byte[] bytes) {
        try {
            ClassReader reader = new ClassReader(bytes);
            ClassWriter writer = new ClassWriter(reader, 0);
            final boolean[] patched = {false};
            ClassVisitor visitor = new ClassVisitor(ASM9, writer) {
                @Override
                public MethodVisitor visitMethod(
                    int access, String name, String descriptor, String signature, String[] exceptions
                ) {
                    MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                    if ("getInputArguments".equals(name) && "()Ljava/util/List;".equals(descriptor)) {
                        patched[0] = true;
                        return new InputArgsFilterPatcher(mv);
                    }
                    return mv;
                }
            };
            reader.accept(visitor, 0);
            if (patched[0]) {
                AgentLog.info("RuntimeImpl.getInputArguments() PATCHED");
                return writer.toByteArray();
            }
            return null;
        } catch (Exception e) {
            AgentLog.error("Input-args patch failed: " + e.getMessage());
            return null;
        }
    }

    private byte[] patchPlatformInfo(byte[] bytes) {
        try {
            ClassReader reader = new ClassReader(bytes);
            ClassWriter writer = new ClassWriter(reader, 0);
            ClassVisitor visitor = new ClassVisitor(ASM9, writer) {
                @Override
                public MethodVisitor visitMethod(
                    int access, String name, String descriptor, String signature, String[] exceptions
                ) {
                    MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                    return new RuntimeCallPatcher(mv);
                }
            };
            reader.accept(visitor, 0);
            AgentLog.info("PlatformInfo machine-info PATCHED");
            return writer.toByteArray();
        } catch (Exception e) {
            AgentLog.error("PlatformInfo patch failed: " + e.getMessage());
            return null;
        }
    }

    private static final class GetHardwareAddressPatcher extends MethodVisitor {
        GetHardwareAddressPatcher(MethodVisitor mv) {
            super(ASM9, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(
                INVOKESTATIC,
                "ox/incognito/OxIncognitoAgent",
                "getFakeMacFor",
                "(Ljava/net/NetworkInterface;)[B",
                false
            );
            mv.visitVarInsn(ASTORE, 99);
            mv.visitVarInsn(ALOAD, 99);
            Label skip = new Label();
            mv.visitJumpInsn(IFNULL, skip);
            mv.visitVarInsn(ALOAD, 99);
            mv.visitInsn(ARETURN);
            mv.visitLabel(skip);
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            super.visitMaxs(Math.max(maxStack, 2), Math.max(maxLocals, 100));
        }
    }

    private static final class InputArgsFilterPatcher extends MethodVisitor {
        InputArgsFilterPatcher(MethodVisitor mv) {
            super(ASM9, mv);
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == ARETURN) {
                mv.visitMethodInsn(
                    INVOKESTATIC,
                    "ox/incognito/OxIncognitoAgent",
                    "filterInputArgs",
                    "(Ljava/util/List;)Ljava/util/List;",
                    false
                );
            }
            super.visitInsn(opcode);
        }
    }

    private static final class RuntimeCallPatcher extends MethodVisitor {
        RuntimeCallPatcher(MethodVisitor mv) {
            super(ASM9, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            if (opcode == INVOKEVIRTUAL && "java/lang/Runtime".equals(owner)) {
                if ("maxMemory".equals(name) && "()J".equals(descriptor)) {
                    super.visitInsn(POP);
                    super.visitMethodInsn(
                        INVOKESTATIC,
                        "ox/incognito/OxIncognitoAgent",
                        "getReportedMaxMemory",
                        "()J",
                        false
                    );
                    return;
                }
                if ("availableProcessors".equals(name) && "()I".equals(descriptor)) {
                    super.visitInsn(POP);
                    super.visitMethodInsn(
                        INVOKESTATIC,
                        "ox/incognito/OxIncognitoAgent",
                        "getReportedCpuCount",
                        "()I",
                        false
                    );
                    return;
                }
            }
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
    }
}
