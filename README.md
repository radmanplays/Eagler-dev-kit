# Eagler dev kit
Utils for porting mods/clients to eaglercraft and porting minecraft versions to browser

### WARNING: the lwjgl compatibility layer was made for 1.8 and may need some modifications to work on other versions

# How to use
1. download this repo or clone it by using `git clone https://github.com/radmanplays/Eagler-dev-kit.git`
2. copy all the folders in this repo(not the README.md file) into src/main/java folder of your client
3. add these lines to the files specified:

PlatformOpenGL LWJGL:
```java
public static void _wglScissor(int x, int y, int width, int height) {
  glScissor(x, y, width, height);
}
```
PlatformOpenGL TeaVM:
```java
public static void _wglScissor(int x, int y, int width, int height) {
  ctx.scissor(x, y, width, height);
}
```
GlStateManager.java:
```java
private static boolean scissorState = false;

public static void enableScissor() {
  if(!scissorState) {
    _wglEnable(RealOpenGLEnums.GL_SCISSOR_TEST);
    scissorState = true;
  }
}

public static void disableScissor() {
  if(scissorState) {
    _wglDisable(RealOpenGLEnums.GL_SCISSOR_TEST);
    scissorState = false;
  }
}

public static void glScissor(int x, int y, int width, int height) {
  _wglScissor(x, y, width, height);
}
```
PlatformOpenGL platform-api:

```java
public static native void _glScissor(int x, int y, int width, int height);
```
PlatformOpenGL WASM:
```java
@Import(module = "platformOpenGL", name = "glScissor")
public static native void _wglScissor(int x, int y, int width, int height);
```