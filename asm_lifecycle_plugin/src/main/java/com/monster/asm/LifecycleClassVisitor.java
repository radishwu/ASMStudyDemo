package com.monster.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class LifecycleClassVisitor extends ClassVisitor {

    private String className;
    private String superName;

    public LifecycleClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
        this.superName = superName;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        System.out.println("ClassVisitor visitMethod name:" + name + ", superName:" + superName);
        MethodVisitor mv = cv.visitMethod(access, name, descriptor, signature, exceptions);

        if (superName.equals("androidx/appcompat/app/AppCompatActivity")) {
            if (name.startsWith("onCreate")) {
                return new LifecycleMethodVisitor(mv, className, name);
            }
        }
        return mv;
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }
}
