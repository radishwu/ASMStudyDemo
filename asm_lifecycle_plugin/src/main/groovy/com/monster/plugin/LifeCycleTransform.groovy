package com.monster.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import com.monster.asm.LifecycleClassVisitor
import org.apache.commons.codec.digest.DigestUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

public class LifeCycleTransform extends Transform {

    @Override
    String getName() {
        return "LifeCycleTransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)

        // inputs 包含了 jar 包和目录。
        // 子 module 的 java 文件在编译过程中也会生成一个 jar 包然后编译到主工程中。
        transformInvocation.inputs.each {
            input ->

                // 遍历目录
                // 文件夹里面包含的是我们手写的类以及R.class、BuildConfig.class以及R$XXX.class等
                input.directoryInputs.each {
                    DirectoryInput directoryInput ->
                        directoryInput.file.eachFileRecurse {
                            File file ->
                                String name = file.name
                                if (name.endsWith(".class")
                                        && !name.startsWith("R\$")
                                        && "R.class" != name
                                        && "BuildConfig.class" != name) {
                                    System.out.println("find class:" + name)
                                    //对class文件进行读取和解析
                                    ClassReader classReader = new ClassReader(file.bytes)
                                    //对class文件的写入
                                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                                    //访问class文件相应的内容，解析到某一个结构就会通知到ClassVisitor的相应方法
                                    ClassVisitor classVisitor = new LifecycleClassVisitor(classWriter)
                                    //依次调用ClassVisitor接口的各个方法
                                    classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
                                    //toByteArray方法会将最终修改的字节码以byte数组形式放回
                                    byte[] bytes = classWriter.toByteArray()
                                    //通过文件流写入方式覆盖掉原先的内容，实现class文件的改写
                                    FileOutputStream outputStream = new FileOutputStream(file.path)
                                    outputStream.write(bytes)
                                    outputStream.close()
                                }
                        }
                        // 获取output目录
                        def dest = outputProvider.getContentLocation(directoryInput.name,
                                directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)

                        // 将input的目录复制到output指定目录
                        FileUtils.copyDirectory(directoryInput.file, dest)
                }


                // 遍历 jar，我们不需要对 jar 进行处理，所以直接跳过
                // 但是后面的 transform 可能需要处理，所以需要从输入流原封不动的写到输出流
                input.jarInputs.each {
                    jarInput ->
                        def jarName = jarInput.name
                        println("jar = " + jarInput.file.getAbsolutePath())
                        def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                        if (jarName.endsWith(".jar")) {
                            jarName = jarName.substring(0, jarName.length() - 4)
                        }
                        def dest = outputProvider.getContentLocation(jarName + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                        FileUtils.copyFile(jarInput.file, dest)
                }
        }
    }
}