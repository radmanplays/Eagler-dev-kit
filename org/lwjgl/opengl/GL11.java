package org.lwjgl.opengl;

import net.lax1dude.eaglercraft.v1_8.internal.buffer.ByteBuffer;
import net.lax1dude.eaglercraft.v1_8.internal.buffer.FloatBuffer;
import net.lax1dude.eaglercraft.v1_8.internal.buffer.IntBuffer;
import net.lax1dude.eaglercraft.v1_8.opengl.EaglercraftGPU;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.lax1dude.eaglercraft.v1_8.opengl.RealOpenGLEnums;
import net.lax1dude.eaglercraft.v1_8.opengl.VertexFormat;
import net.lax1dude.eaglercraft.v1_8.opengl.WorldRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import java.util.Arrays;

import static net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager.*;
import static net.lax1dude.eaglercraft.v1_8.opengl.EaglercraftGPU.*;

public class GL11 extends RealOpenGLEnums {

    private static final Tessellator tessellator = Tessellator.getInstance();
    private static final WorldRenderer worldRenderer = tessellator.getWorldRenderer();
    private static boolean isDrawing = false;
    private static double[] polygonVertexData = new double[48];
    private static int polygonVertexCount = 0;
    private static boolean isPolygonMode = false;

    private static int currentMode = -1;

    private static boolean useColor = false;
    private static boolean useTexCoord = false;
    private static boolean vertexFormatInitialized = false;

    public static void glEnable(int p1) {
        if (p1 >= GL_LIGHT0 && p1 <= GL_LIGHT7) {
            GlStateManager.enableMCLight(p1 - GL_LIGHT0, 1.0f, 0.0, 0.0, -1.0, 0.0);
            return;
        }
        switch (p1) {
            case GL_DEPTH_TEST: enableDepth(); break;
            case GL_CULL_FACE: enableCull(); break;
            case GL_BLEND: enableBlend(); break;
            case GL_RESCALE_NORMAL: break;
            case GL_TEXTURE_2D: enableTexture2D(); break;
            case GL_LIGHTING: enableLighting(); break;
            case GL_ALPHA_TEST: enableAlpha(); break;
            case GL_FOG: enableFog(); break;
            case GL_COLOR_MATERIAL: enableColorMaterial(); break;
            case GL_TEXTURE_GEN_S:
            case GL_TEXTURE_GEN_T:
            case GL_TEXTURE_GEN_R:
            case GL_TEXTURE_GEN_Q: enableTexGen(); break;
            case GL_POLYGON_OFFSET_FILL: enablePolygonOffset(); break;
            case GL_SCISSOR_TEST: enableScissor(); break;
            default: break;
        }
    }

    public static void glDisable(int p1) {
        if (p1 >= GL_LIGHT0 && p1 <= GL_LIGHT7) {
            GlStateManager.disableMCLight(p1 - GL_LIGHT0);
            return;
        }
        switch (p1) {
            case GL_DEPTH_TEST: disableDepth(); break;
            case GL_CULL_FACE: disableCull(); break;
            case GL_BLEND: disableBlend(); break;
            case GL_RESCALE_NORMAL: break;
            case GL_TEXTURE_2D: disableTexture2D(); break;
            case GL_LIGHTING: disableLighting(); break;
            case GL_ALPHA_TEST: disableAlpha(); break;
            case GL_FOG: disableFog(); break;
            case GL_COLOR_MATERIAL: disableColorMaterial(); break;
            case GL_TEXTURE_GEN_S:
            case GL_TEXTURE_GEN_T:
            case GL_TEXTURE_GEN_R:
            case GL_TEXTURE_GEN_Q: disableTexGen(); break;
            case GL_POLYGON_OFFSET_FILL: disablePolygonOffset(); break;
            case GL_SCISSOR_TEST: disableScissor(); break;
            default: break;
        }
    }

    public static void glPointSize(float size) {
        // not implemented
    }

    public static void glBegin(int mode) {
        if (isDrawing) {
            throw new IllegalStateException("glBegin called without glEnd!");
        }
        isDrawing = true;
        currentMode = mode;
        useColor = false;
        useTexCoord = false;
        vertexFormatInitialized = false;
        isPolygonMode = (mode == GL_POLYGON);
        if (isPolygonMode) {
            polygonVertexCount = 0;
        }
    }

    public static void glEnd() {
        if (!isDrawing) {
            throw new IllegalStateException("glEnd called without glBegin!");
        }
        if (isPolygonMode) {
            if (polygonVertexCount < 3) {
                throw new IllegalStateException("GL_POLYGON requires at least 3 vertices");
            }
            worldRenderer.begin(GL_TRIANGLES, DefaultVertexFormats.POSITION);
            final double x0 = polygonVertexData[0];
            final double y0 = polygonVertexData[1];
            final double z0 = polygonVertexData[2];

            for (int i = 1; i < polygonVertexCount - 1; i++) {
                worldRenderer.pos(x0, y0, z0).endVertex();
                worldRenderer.pos(polygonVertexData[i * 3], polygonVertexData[i * 3 + 1], polygonVertexData[i * 3 + 2]).endVertex();
                worldRenderer.pos(polygonVertexData[(i + 1) * 3], polygonVertexData[(i + 1) * 3 + 1], polygonVertexData[(i + 1) * 3 + 2]).endVertex();
            }
            tessellator.draw();
        } else if (vertexFormatInitialized) {
            tessellator.draw();
        }
        isDrawing = false;
        currentMode = -1;
    }

    public static void glVertex2i(int x, int y) {
        glVertex3d(x, y, 0.0);
    }

    public static void glVertex2d(double x, double y) {
        glVertex3d(x, y, 0.0);
    }

    public static void glVertex3d(double x, double y, double z) {
        if (!isDrawing) {
            throw new IllegalStateException("glVertex3d called outside glBegin/glEnd!");
        }
        if (isPolygonMode) {
            int requiredSize = (polygonVertexCount + 1) * 3;
            if (requiredSize > polygonVertexData.length) {
                polygonVertexData = Arrays.copyOf(polygonVertexData, polygonVertexData.length + (polygonVertexData.length >> 1));
            }
            int baseIndex = polygonVertexCount * 3;
            polygonVertexData[baseIndex] = x;
            polygonVertexData[baseIndex + 1] = y;
            polygonVertexData[baseIndex + 2] = z;
            polygonVertexCount++;
        } else {
            ensureStarted();
            worldRenderer.pos(x, y, z).endVertex();
        }
    }

    public static void glTexCoord2f(float u, float v) {
        if (!isDrawing) {
            throw new IllegalStateException("glTexCoord2f called outside glBegin/glEnd!");
        }
        useTexCoord = true;
        worldRenderer.tex(u, v);
    }

    private static void ensureStarted() {
        if (!vertexFormatInitialized) {
            vertexFormatInitialized = true;
            VertexFormat format;
            if (useColor && useTexCoord) {
                format = DefaultVertexFormats.POSITION_TEX_COLOR;
            } else if (useColor) {
                format = DefaultVertexFormats.POSITION_COLOR;
            } else if (useTexCoord) {
                format = DefaultVertexFormats.POSITION_TEX;
            } else {
                format = DefaultVertexFormats.POSITION;
            }
            worldRenderer.begin(currentMode, format);
        }
    }

    public static void glScissor(int x, int y, int width, int height) {
        GlStateManager.glScissor(x, y, width, height);
    }

    public static void glShadeModel(int i) {
        shadeModel(i);
    }

    public static void glClearDepth(float f) {
        clearDepth(f);
    }

    public static void glClearDepth(double d) {
        clearDepth((float) d);
    }

    public static void glDepthFunc(int f) {
        depthFunc(f);
    }

    public static void glAlphaFunc(int i, float f) {
        alphaFunc(i, f);
    }

    public static void glCullFace(int i) {
        cullFace(i);
    }

    public static void glMatrixMode(int i) {
        matrixMode(i);
    }

    public static void glLoadIdentity() {
        loadIdentity();
    }

    public static void glViewport(int i, int j, int width, int height) {
        viewport(i, j, width, height);
    }

    public static void glColorMask(boolean b, boolean c, boolean d, boolean e) {
        colorMask(b, c, d, e);
    }

    public static void glClearColor(float fogRed, float fogBlue, float fogGreen, float f) {
        clearColor(fogRed, fogBlue, fogGreen, f);
    }

    public static void glClear(int i) {
        clear(i);
    }

    public static void glTranslatef(float f, float g, float h) {
        translate(f, g, h);
    }

    public static void glTranslated(double f, double g, double h) {
        translate(f, g, h);
    }

    public static void glRotatef(float f, float g, float h, float i) {
        rotate(f, g, h, i);
    }

    public static void glRotated(double f, double g, double h, double i) {
        rotate((float)f, (float)g, (float)h, (float)i);
    }

    public static void glColor4f(float f, float g, float h, float i) {
        color(f, g, h, i);
        useColor = true;
    }

    public static void glColor4d(double f, double g, double h, double i) {
        color((float)f, (float)g, (float)h, (float)i);
        useColor = true;
    }

    public static void glBindTexture(int i, int var110) {
        if (i != GL_TEXTURE_2D) {
            throw new RuntimeException("Only 2D texture types are supported!");
        }
        bindTexture(var110);
    }

    public static void glBlendFunc(int i, int j) {
        blendFunc(i, j);
    }

    public static void glPushMatrix() {
        pushMatrix();
    }

    public static void glPopMatrix() {
        popMatrix();
    }

    public static void glScalef(float f, float var35, float var352) {
        scale(f, var35, var352);
    }

    public static void glDepthMask(boolean b) {
        depthMask(b);
    }

    public static void glCallLists(IntBuffer p1) {
        while (p1.hasRemaining()) {
            glCallList(p1.get());
        }
    }

    public static void glDeleteLists(int list, int range) {
        for (int i = 0; i < range; ++i) {
            EaglercraftGPU.glDeleteLists(list + i);
        }
    }

    public static void glOrtho(double d, double var3, double var2, double e, double f, double g) {
        ortho(d, var3, var2, e, f, g);
    }

    public static void glGenTextures(IntBuffer idBuffer) {
        for (int i = idBuffer.position(); i < idBuffer.limit(); i++) {
            idBuffer.put(i, generateTexture());
        }
    }

    public static void glGetFloat(int glModelviewMatrix, FloatBuffer modelviewBuff) {
        getFloat(glModelviewMatrix, modelviewBuff);
    }

    public static void glColor3f(float f, float g, float h) {
        color(f, g, h);
        useColor = true;
    }

    public static void glColorMaterial(int i, int j) {
    }

    public static void glPolygonOffset(float f, float g) {
        doPolygonOffset(f, g);
    }

    public static void glScaled(double f, double f1, double f2) {
        glScalef((float)f, (float)f1, (float)f2);
    }

    public static void glDeleteTexture(int texture) {
        deleteTexture(texture);
    }

    public static void glDeleteTextures(IntBuffer buffer) {
        while (buffer.hasRemaining()) {
            glDeleteTexture(buffer.get());
        }
    }

    public static void glFogf(int type, float param) {
        switch(type) {
            case GL_FOG_DENSITY: setFogDensity(param); return;
            case GL_FOG_START: setFogStart(param); return;
            case GL_FOG_END: setFogEnd(param); return;
            default: return;
        }
    }

    public static void glFogi(int type, int param) {
        switch(type) {
            case GL_FOG_MODE: setFog(param); return;
            default: return;
        }
    }

    public static void glBlendFuncSeparate(int i, int j, int k, int l) {
        tryBlendFuncSeparate(i, j, k, l);
    }

    public static int glGetError() {
        return EaglercraftGPU.glGetError();
    }

    public static void glLineWidth(float width) {
        EaglercraftGPU.glLineWidth(width);
    }

    public static void glFog(int param, FloatBuffer buf) {
        EaglercraftGPU.glFog(param, buf);
    }

    public static void glNormal3f(float x, float y, float z) {
        EaglercraftGPU.glNormal3f(x, y, z);
    }

    public static int glGenLists(int count) {
        return EaglercraftGPU.glGenLists();
    }

    public static void glDeleteList(int list) {
        EaglercraftGPU.glDeleteLists(list);
    }

    public static void glCallList(int displayList) {
        EaglercraftGPU.glCallList(displayList);
    }

    public static void glRotateZYXRad(float x, float y, float z) {
        GlStateManager.rotateZYXRad(x, y, z);
    }

    public static void glNewList(int target, int op) {
        EaglercraftGPU.glNewList(target, op);
    }

    public static void glEndList() {
        EaglercraftGPU.glEndList();
    }

    public static String glGetString(int param) {
        return EaglercraftGPU.glGetString(param);
    }

    public static void glTexParameteri(int target, int param, int value) {
        EaglercraftGPU.glTexParameteri(target, param, value);
    }

    public static void glTexImage2D(int target, int level, int internalFormat, int w, int h, int unused, int format, int type, ByteBuffer pixels) {
        EaglercraftGPU.glTexImage2D(target, level, internalFormat, w, h, unused, format, type, pixels);
    }

    public static void glTexSubImage2D(int target, int level, int x, int y, int w, int h, int format, int type, IntBuffer pixels) {
        EaglercraftGPU.glTexSubImage2D(target, level, x, y, w, h, format, type, pixels);
    }

    public static void glFlushList(int list) {
        EaglercraftGPU.flushDisplayList(list);
    }

    public static void glTexParameterf(int target, int param, float value) {
        EaglercraftGPU.glTexParameterf(target, param, value);
    }
}
