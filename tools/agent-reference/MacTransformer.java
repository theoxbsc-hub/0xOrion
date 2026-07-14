/*
 * Decompiled with CFR 0.152.
 */
package incognito;

import incognito.IncognitoAgent;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class MacTransformer
implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader classLoader, String string, Class<?> clazz, ProtectionDomain protectionDomain, byte[] byArray) {
        if ("java/net/NetworkInterface".equals(string)) {
            return this.patchNetworkInterface(byArray);
        }
        if ("sun/management/RuntimeImpl".equals(string)) {
            return this.patchInputArgs(byArray);
        }
        return null;
    }

    private byte[] patchInputArgs(byte[] byArray) {
        try {
            ClassReader classReader = new ClassReader(byArray);
            ClassWriter classWriter = new ClassWriter(classReader, 0);
            final boolean[] blArray = new boolean[]{false};
            ClassVisitor classVisitor = new ClassVisitor(this, 589824, classWriter){
                final /* synthetic */ MacTransformer this$0;
                {
                    this.this$0 = macTransformer;
                    super(n, classVisitor);
                }

                @Override
                public MethodVisitor visitMethod(int n, String string, String string2, String string3, String[] stringArray) {
                    MethodVisitor methodVisitor = super.visitMethod(n, string, string2, string3, stringArray);
                    if ("getInputArguments".equals(string) && "()Ljava/util/List;".equals(string2)) {
                        blArray[0] = true;
                        return new InputArgsFilterPatcher(methodVisitor);
                    }
                    return methodVisitor;
                }
            };
            classReader.accept(classVisitor, 0);
            if (blArray[0]) {
                System.out.println("[Incognito Agent] RuntimeImpl.getInputArguments() PATCHED (launch args now filtered to a stock-Orion-like list for reflection reads)");
                return classWriter.toByteArray();
            }
            return null;
        }
        catch (Exception exception) {
            System.err.println("[Incognito Agent] Input-args patch failed: " + exception.getMessage());
            return null;
        }
    }

    private byte[] patchReflectionResolver(byte[] byArray) {
        try {
            ClassReader classReader = new ClassReader(byArray);
            ClassWriter classWriter = new ClassWriter(classReader, 0);
            final boolean[] blArray = new boolean[]{false};
            ClassVisitor classVisitor = new ClassVisitor(this, 589824, classWriter){
                final /* synthetic */ MacTransformer this$0;
                {
                    this.this$0 = macTransformer;
                    super(n, classVisitor);
                }

                @Override
                public MethodVisitor visitMethod(int n, String string, String string2, String string3, String[] stringArray) {
                    MethodVisitor methodVisitor = super.visitMethod(n, string, string2, string3, stringArray);
                    if ("_2090".equals(string) && "(Ljava/lang/String;)Ljava/lang/Class;".equals(string2)) {
                        blArray[0] = true;
                        return new ReflectionGuardPatcher(methodVisitor);
                    }
                    return methodVisitor;
                }
            };
            classReader.accept(classVisitor, 0);
            if (blArray[0]) {
                System.out.println("[Incognito Agent] Reflection resolver class327._2090 PATCHED (x64 class names now report NOT FOUND to server reflection checks, like stock Orion)");
                return classWriter.toByteArray();
            }
            System.err.println("[Incognito Agent] class327._2090 not found \u2014 reflection guard NOT applied (gamepack may have been re-obfuscated); failing open");
            return null;
        }
        catch (Exception exception) {
            System.err.println("[Incognito Agent] Reflection resolver patch failed: " + exception.getMessage());
            return null;
        }
    }

    private byte[] patchPlatformInfo(byte[] byArray) {
        try {
            ClassReader classReader = new ClassReader(byArray);
            ClassWriter classWriter = new ClassWriter(classReader, 0);
            ClassVisitor classVisitor = new ClassVisitor(589824, classWriter){

                @Override
                public MethodVisitor visitMethod(int n, String string, String string2, String string3, String[] stringArray) {
                    MethodVisitor methodVisitor = super.visitMethod(n, string, string2, string3, stringArray);
                    return new RuntimeCallPatcher(methodVisitor);
                }
            };
            classReader.accept(classVisitor, 0);
            IncognitoAgent.markPlatformPatched();
            System.out.println("[Incognito Agent] PlatformInfo machine-info PATCHED (login maxMemory/availableProcessors now report the spoofed per-profile values)");
            return classWriter.toByteArray();
        }
        catch (Exception exception) {
            System.err.println("[Incognito Agent] PlatformInfo patch failed: " + exception.getMessage());
            return null;
        }
    }

    private byte[] patchNetworkInterface(byte[] byArray) {
        try {
            ClassReader classReader = new ClassReader(byArray);
            ClassWriter classWriter = new ClassWriter(classReader, 0);
            ClassVisitor classVisitor = new ClassVisitor(589824, classWriter){

                @Override
                public MethodVisitor visitMethod(int n, String string, String string2, String string3, String[] stringArray) {
                    MethodVisitor methodVisitor = super.visitMethod(n, string, string2, string3, stringArray);
                    if ("getHardwareAddress".equals(string) && "()[B".equals(string2)) {
                        return new GetHardwareAddressPatcher(methodVisitor);
                    }
                    return methodVisitor;
                }
            };
            classReader.accept(classVisitor, 0);
            System.out.println("[Incognito Agent] Bytecode patch applied to NetworkInterface.getHardwareAddress()");
            return classWriter.toByteArray();
        }
        catch (Exception exception) {
            System.err.println("[Incognito Agent] Bytecode patch failed: " + exception.getMessage());
            return null;
        }
    }

    private static class RuntimeCallPatcher
    extends MethodVisitor {
        RuntimeCallPatcher(MethodVisitor methodVisitor) {
            super(589824, methodVisitor);
        }

        @Override
        public void visitMethodInsn(int n, String string, String string2, String string3, boolean bl) {
            if (n == 182 && "java/lang/Runtime".equals(string)) {
                if ("maxMemory".equals(string2) && "()J".equals(string3)) {
                    super.visitInsn(87);
                    super.visitMethodInsn(184, "incognito/IncognitoAgent", "getReportedMaxMemory", "()J", false);
                    return;
                }
                if ("availableProcessors".equals(string2) && "()I".equals(string3)) {
                    super.visitInsn(87);
                    super.visitMethodInsn(184, "incognito/IncognitoAgent", "getReportedCpuCount", "()I", false);
                    return;
                }
            }
            super.visitMethodInsn(n, string, string2, string3, bl);
        }
    }

    private static class GetHardwareAddressPatcher
    extends MethodVisitor {
        GetHardwareAddressPatcher(MethodVisitor methodVisitor) {
            super(589824, methodVisitor);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            this.mv.visitVarInsn(25, 0);
            this.mv.visitMethodInsn(184, "incognito/IncognitoAgent", "getFakeMacFor", "(Ljava/net/NetworkInterface;)[B", false);
            this.mv.visitVarInsn(58, 99);
            this.mv.visitVarInsn(25, 99);
            Label label = new Label();
            this.mv.visitJumpInsn(198, label);
            this.mv.visitVarInsn(25, 99);
            this.mv.visitInsn(176);
            this.mv.visitLabel(label);
        }

        @Override
        public void visitMaxs(int n, int n2) {
            super.visitMaxs(Math.max(n, 2), Math.max(n2, 100));
        }
    }

    private static class ReflectionGuardPatcher
    extends MethodVisitor {
        ReflectionGuardPatcher(MethodVisitor methodVisitor) {
            super(589824, methodVisitor);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            this.mv.visitVarInsn(25, 0);
            this.mv.visitMethodInsn(184, "incognito/IncognitoAgent", "guardReflectionName", "(Ljava/lang/String;)V", false);
        }

        @Override
        public void visitMaxs(int n, int n2) {
            super.visitMaxs(Math.max(n, 1), n2);
        }
    }

    private static class InputArgsFilterPatcher
    extends MethodVisitor {
        InputArgsFilterPatcher(MethodVisitor methodVisitor) {
            super(589824, methodVisitor);
        }

        @Override
        public void visitInsn(int n) {
            if (n == 176) {
                this.mv.visitMethodInsn(184, "incognito/IncognitoAgent", "filterInputArgs", "(Ljava/util/List;)Ljava/util/List;", false);
            }
            super.visitInsn(n);
        }

        @Override
        public void visitMaxs(int n, int n2) {
            super.visitMaxs(Math.max(n, 1), n2);
        }
    }
}
