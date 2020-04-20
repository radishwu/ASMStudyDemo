# ASMStudyDemo
 just learning gradle plugin and asm.

#### imitate by MycoyJiang

#### Transform

为gradle编译项目时的一个task，.class文件转换成.dex的流程中会执行task，可以在transform中可以获取所有.class文件做自定义事情。抽象方法如下：

* getName

  transform的名字，会生成transformClassesWith+name+For+编译类型的task。

* getInputType

  用于指明Transform的输入类型，可以作为输入过滤的手段。

  ```java
  /* TransformManager */
  
  // 代表 javac 编译成的 class 文件，常用
  public static final Set<ContentType> CONTENT_CLASS;
  public static final Set<ContentType> CONTENT_JARS;
  // 这里的 resources 单指 java 的资源
  public static final Set<ContentType> CONTENT_RESOURCES;
  public static final Set<ContentType> CONTENT_NATIVE_LIBS;
  public static final Set<ContentType> CONTENT_DEX;
  public static final Set<ContentType> CONTENT_DEX_WITH_RESOURCES;
  public static final Set<ContentType> DATA_BINDING_BASE_CLASS_LOG_ARTIFACT;
  ```

* getScopes

  用于指明Transform的作用域。

  ```java
  /* TransformManager */
  
  public static final Set<Scope> EMPTY_SCOPES = ImmutableSet.of();
  public static final Set<ScopeType> PROJECT_ONLY;
  public static final Set<Scope> SCOPE_FULL_PROJECT; // 常用 代表所有Project
  public static final Set<ScopeType> SCOPE_FULL_WITH_IR_FOR_DEXING;
  public static final Set<ScopeType> SCOPE_FULL_WITH_FEATURES;
  public static final Set<ScopeType> SCOPE_FULL_WITH_IR_AND_FEATURES;
  public static final Set<ScopeType> SCOPE_FEATURES;
  public static final Set<ScopeType> SCOPE_FULL_LIBRARY_WITH_LOCAL_JARS;
  public static final Set<ScopeType> SCOPE_IR_FOR_SLICING;
  ```

* isIncremental

  指明该 Transform 是否支持增量编译。需要注意的是，即使返回了 true ，在某些情况下运行时，它还是会返回 false 的。

* transfrom

  可以在重写该方法获取所有.class文件实现自定义逻辑。

#### some questions

* Android gradle 3.6+有问题，transform获取不到资源.class，打包结果也不存在，无法运行demo。