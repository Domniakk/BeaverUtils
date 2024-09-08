package dev.bebomny.beaver.beaverutils.helpers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class RenderUtils {

    public static int getRainbowColorAsInt() {
        float[] rainbow = getRainbowColor();
        return (int) rainbow[0] | (int) rainbow[1] << 8 | (int) rainbow[2] << 16;
    }


    /**
     * @param red   Red color limit (Max 256)
     * @param green Green color limit (Max 256)
     * @param blue  Blue color limit (Max 256)
     * @param alpha Transparency limit (Max 256)
     * @return A compiled integer to be used in draw functions to limit color changing
     */
    public static int getColorChangeLimits(int red, int green, int blue, int alpha) {
        //float[] rainbow = getRainbowColor();
        //return getRainbowColorAsInt() | 0xFF << 24;
        red = Math.clamp(red, 0, 256);
        green = Math.clamp(green, 0, 256);
        blue = Math.clamp(blue, 0, 256);
        alpha = Math.clamp(alpha, 0, 256);
        return red | green << 8 | blue << 16 | alpha << 24;
    }

    /**
     * Generates a float array containing red, green, blue and alpha values of a rainbow color generated by
     * functions with relation to time <br>
     * <b>red</b> - f(x) = 0.5 + 0.5 * sin(x * pi) <br>
     * <b>green</b> - f(x) = 0.5 + 0.5 * sin((x + 4/3) * pi) <br>
     * <b>blue</b> - f(x) = 0.5 + 0.5 * sin((x + 8/3) * pi) <br>
     * <b>alpha</b> - 0xFF
     *
     * @return A float array with 4 RGBA values
     */
    public static float[] getRainbowColor() {
        float x = System.currentTimeMillis() % 2000 / 1000F;

        float[] rainbow = new float[4];
        rainbow[0] = 0.5F + 0.5F * MathHelper.sin(x * (float) Math.PI);
        rainbow[1] = 0.5F + 0.5F * MathHelper.sin((x + 4F / 3F) * (float) Math.PI);
        rainbow[2] = 0.5F + 0.5F * MathHelper.sin((x + 8F / 3F) * (float) Math.PI);
        rainbow[3] = 0xFF;
        return rainbow;
    }

    public static void drawLine(Vec3d vecStart, Vec3d vecEnd, MatrixStack matrixStack) {
        Matrix4f matrices = matrixStack.peek().getPositionMatrix();
        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION);
        RenderSystem.setShader(GameRenderer::getPositionProgram);

        bufferBuilder.vertex(matrices, (float) vecStart.getX(), (float) vecStart.getY(), (float) vecStart.getZ());
        bufferBuilder.vertex(matrices, (float) vecEnd.getX(), (float) vecEnd.getY(), (float) vecEnd.getZ());

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    public static void drawTexture(Identifier textureId, int texturesInImage, int textureNum, MatrixStack matrixStack) {
        Matrix4f matrices = matrixStack.peek().getPositionMatrix();
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator
                .begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        RenderSystem.setShaderTexture(0, textureId);
        //RenderSystem.setShaderColor(0x00, 0xFF, 0x00, 0xFF);
        //RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        //RenderSystem.setShaderTexture(1, textureId);

        bufferBuilder.vertex(matrices, (float) -0.5, (float) 1, (float) 0).texture(0, (float) (textureNum - 1) / texturesInImage);
        bufferBuilder.vertex(matrices, (float) 0.5, (float) 1, (float) 0).texture(1, (float) (textureNum - 1) / texturesInImage);
        bufferBuilder.vertex(matrices, (float) 0.5, (float) 0, (float) 0).texture(1, (float) textureNum / texturesInImage);
        bufferBuilder.vertex(matrices, (float) -0.5, (float) 0, (float) 0).texture(0, (float) textureNum / texturesInImage);


        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        //RenderSystem.enableCull();
        //RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    public static void drawOutlinedBox(MatrixStack matrixStack) {
        drawOutlinedBox(new Box(0, 0, 0, 1, 1, 1), matrixStack);
    }

    public static void drawOutlinedBox(Box box, VertexBuffer vertexBuffer) {
        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION);
        drawOutlinedBoxVertex(box, bufferBuilder);
        BuiltBuffer buffer = bufferBuilder.end();

        vertexBuffer.bind();
        vertexBuffer.upload(buffer);
        VertexBuffer.unbind();
    }

    public static void drawOutlinedBox(Box box, MatrixStack matrixStack) {
        //Matrix4f matrices = matrixStack.peek().getPositionMatrix();
        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION);
        RenderSystem.setShader(GameRenderer::getPositionProgram);
        //RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);

        drawOutlinedBoxVertex(box, bufferBuilder, matrixStack);

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    public static void drawOutlinedBoxRainbow(Box box, MatrixStack matrixStack) {
        drawOutlinedBoxWithColor(box, matrixStack, getRainbowColor(), getColorChangeLimits(0xFF, 0xFF, 0xFF, 0xFF));
    }

    public static void drawOutlinedBoxWithColor(Box box, MatrixStack matrixStack, float[] argbColor, int colorChangeLimits) {
        //Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(argbColor[0], argbColor[1], argbColor[2], argbColor[3]);
        //RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);

        drawOutlinedBoxVertexWithColor(box, bufferBuilder, matrixStack, colorChangeLimits);

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        //RenderSystem.enableCull();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    private static void drawOutlinedBoxVertex(Box box, BufferBuilder bufferBuilder) {
        //should be 12
        //example for a box with line lengths of 1
        //0,0,0 -> 1,0,0
        bufferBuilder.vertex((float) box.minX, (float) box.minY, (float) box.minZ);
        bufferBuilder.vertex((float) box.maxX, (float) box.minY, (float) box.minZ);
        //0,0,0 -> 0,1,0
        bufferBuilder.vertex((float) box.minX, (float) box.minY, (float) box.minZ);
        bufferBuilder.vertex((float) box.minX, (float) box.maxY, (float) box.minZ);
        //0,0,0 -> 0,0,1
        bufferBuilder.vertex((float) box.minX, (float) box.minY, (float) box.minZ);
        bufferBuilder.vertex((float) box.minX, (float) box.minY, (float) box.maxZ);
        //1,0,0 -> 1,0,1
        bufferBuilder.vertex((float) box.maxX, (float) box.minY, (float) box.minZ);
        bufferBuilder.vertex((float) box.maxX, (float) box.minY, (float) box.maxZ);
        //0,0,1 -> 1,0,1
        bufferBuilder.vertex((float) box.minX, (float) box.minY, (float) box.maxZ);
        bufferBuilder.vertex((float) box.maxX, (float) box.minY, (float) box.maxZ);
        //1,0,0 -> 1,1,0
        bufferBuilder.vertex((float) box.maxX, (float) box.minY, (float) box.minZ);
        bufferBuilder.vertex((float) box.maxX, (float) box.maxY, (float) box.minZ);
        //1,0,1 -> 1,1,1
        bufferBuilder.vertex((float) box.maxX, (float) box.minY, (float) box.maxZ);
        bufferBuilder.vertex((float) box.maxX, (float) box.maxY, (float) box.maxZ);
        //0,0,1 -> 0,1,1
        bufferBuilder.vertex((float) box.minX, (float) box.minY, (float) box.maxZ);
        bufferBuilder.vertex((float) box.minX, (float) box.maxY, (float) box.maxZ);
        //0,1,0 -> 0,1,1
        bufferBuilder.vertex((float) box.minX, (float) box.maxY, (float) box.minZ);
        bufferBuilder.vertex((float) box.minX, (float) box.maxY, (float) box.maxZ);
        //0,1,0 -> 1,1,0
        bufferBuilder.vertex((float) box.minX, (float) box.maxY, (float) box.minZ);
        bufferBuilder.vertex((float) box.maxX, (float) box.maxY, (float) box.minZ);
        //0,1,1 -> 1,1,1
        bufferBuilder.vertex((float) box.minX, (float) box.maxY, (float) box.maxZ);
        bufferBuilder.vertex((float) box.maxX, (float) box.maxY, (float) box.maxZ);
        //1,1,0 -> 1,1,1
        bufferBuilder.vertex((float) box.maxX, (float) box.maxY, (float) box.minZ);
        bufferBuilder.vertex((float) box.maxX, (float) box.maxY, (float) box.maxZ);
    }

    private static void drawOutlinedBoxVertex(Box box, BufferBuilder bufferBuilder, MatrixStack matrixStack) {
        Matrix4f matrices = matrixStack.peek().getPositionMatrix();

        //should be 12
        //example for a box with line lengths of 1
        //0,0,0 -> 1,0,0
        bufferBuilder.vertex(matrices, (float) box.minX, (float) box.minY, (float) box.minZ);
        bufferBuilder.vertex(matrices, (float) box.maxX, (float) box.minY, (float) box.minZ);
        //0,0,0 -> 0,1,0
        bufferBuilder.vertex(matrices, (float) box.minX, (float) box.minY, (float) box.minZ);
        bufferBuilder.vertex(matrices, (float) box.minX, (float) box.maxY, (float) box.minZ);
        //0,0,0 -> 0,0,1
        bufferBuilder.vertex(matrices, (float) box.minX, (float) box.minY, (float) box.minZ);
        bufferBuilder.vertex(matrices, (float) box.minX, (float) box.minY, (float) box.maxZ);
        //1,0,0 -> 1,0,1
        bufferBuilder.vertex(matrices, (float) box.maxX, (float) box.minY, (float) box.minZ);
        bufferBuilder.vertex(matrices, (float) box.maxX, (float) box.minY, (float) box.maxZ);
        //0,0,1 -> 1,0,1
        bufferBuilder.vertex(matrices, (float) box.minX, (float) box.minY, (float) box.maxZ);
        bufferBuilder.vertex(matrices, (float) box.maxX, (float) box.minY, (float) box.maxZ);
        //1,0,0 -> 1,1,0
        bufferBuilder.vertex(matrices, (float) box.maxX, (float) box.minY, (float) box.minZ);
        bufferBuilder.vertex(matrices, (float) box.maxX, (float) box.maxY, (float) box.minZ);
        //1,0,1 -> 1,1,1
        bufferBuilder.vertex(matrices, (float) box.maxX, (float) box.minY, (float) box.maxZ);
        bufferBuilder.vertex(matrices, (float) box.maxX, (float) box.maxY, (float) box.maxZ);
        //0,0,1 -> 0,1,1
        bufferBuilder.vertex(matrices, (float) box.minX, (float) box.minY, (float) box.maxZ);
        bufferBuilder.vertex(matrices, (float) box.minX, (float) box.maxY, (float) box.maxZ);
        //0,1,0 -> 0,1,1
        bufferBuilder.vertex(matrices, (float) box.minX, (float) box.maxY, (float) box.minZ);
        bufferBuilder.vertex(matrices, (float) box.minX, (float) box.maxY, (float) box.maxZ);
        //0,1,0 -> 1,1,0
        bufferBuilder.vertex(matrices, (float) box.minX, (float) box.maxY, (float) box.minZ);
        bufferBuilder.vertex(matrices, (float) box.maxX, (float) box.maxY, (float) box.minZ);
        //0,1,1 -> 1,1,1
        bufferBuilder.vertex(matrices, (float) box.minX, (float) box.maxY, (float) box.maxZ);
        bufferBuilder.vertex(matrices, (float) box.maxX, (float) box.maxY, (float) box.maxZ);
        //1,1,0 -> 1,1,1
        bufferBuilder.vertex(matrices, (float) box.maxX, (float) box.maxY, (float) box.minZ);
        bufferBuilder.vertex(matrices, (float) box.maxX, (float) box.maxY, (float) box.maxZ);
    }

    private static void drawOutlinedBoxVertexWithColor(Box box, BufferBuilder bufferBuilder, MatrixStack matrixStack, int argbColor) {
        Matrix4f matrices = matrixStack.peek().getPositionMatrix();
        Matrix3f normalMatrix = matrixStack.peek().getNormalMatrix();

        Vector3f normalVec = getNormal((float) box.minX, (float) box.minY, (float) box.minZ, (float) box.maxX, (float) box.maxY, (float) box.maxZ);

        //0,0,0 -> 1,0,0
        bufferBuilder.vertex(matrices, (float) box.minX, (float) box.minY, (float) box.minZ).color(argbColor)/*.normal(matrixStack.peek(), normalVec.x(), normalVec.y(), normalVec.z())*/;
        bufferBuilder.vertex(matrices, (float) box.maxX, (float) box.minY, (float) box.minZ).color(argbColor)/*.normal(matrixStack.peek(), normalVec.x(), normalVec.y(), normalVec.z())*/;
        //0,0,0 -> 0,1,0
        bufferBuilder.vertex(matrices, (float) box.minX, (float) box.minY, (float) box.minZ).color(argbColor)/*.normal(matrixStack.peek(), normalVec.x(), normalVec.y(), normalVec.z())*/;
        bufferBuilder.vertex(matrices, (float) box.minX, (float) box.maxY, (float) box.minZ).color(argbColor)/*.normal(matrixStack.peek(), normalVec.x(), normalVec.y(), normalVec.z())*/;
        //0,0,0 -> 0,0,1
        bufferBuilder.vertex(matrices, (float) box.minX, (float) box.minY, (float) box.minZ).color(argbColor)/*.normal(matrixStack.peek(), normalVec.x(), normalVec.y(), normalVec.z())*/;
        bufferBuilder.vertex(matrices, (float) box.minX, (float) box.minY, (float) box.maxZ).color(argbColor)/*.normal(matrixStack.peek(), normalVec.x(), normalVec.y(), normalVec.z())*/;
        //1,0,0 -> 1,0,1
        bufferBuilder.vertex(matrices, (float) box.maxX, (float) box.minY, (float) box.minZ).color(argbColor)/*.normal(matrixStack.peek(), normalVec.x(), normalVec.y(), normalVec.z())*/;
        bufferBuilder.vertex(matrices, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(argbColor)/*.normal(matrixStack.peek(), normalVec.x(), normalVec.y(), normalVec.z())*/;
        //0,0,1 -> 1,0,1
        bufferBuilder.vertex(matrices, (float) box.minX, (float) box.minY, (float) box.maxZ).color(argbColor)/*.normal(matrixStack.peek(), normalVec.x(), normalVec.y(), normalVec.z())*/;
        bufferBuilder.vertex(matrices, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(argbColor)/*.normal(matrixStack.peek(), normalVec.x(), normalVec.y(), normalVec.z())*/;
        //1,0,0 -> 1,1,0.normal(matrixStack.peek(), normalVec.x(), normalVec.y(), normalVec.z())
        bufferBuilder.vertex(matrices, (float) box.maxX, (float) box.minY, (float) box.minZ).color(argbColor)/*.normal(matrixStack.peek(), normalVec.x(), normalVec.y(), normalVec.z())*/;
        bufferBuilder.vertex(matrices, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(argbColor)/*.normal(matrixStack.peek(), normalVec.x(), normalVec.y(), normalVec.z())*/;
        //1,0,1 -> 1,1,1
        bufferBuilder.vertex(matrices, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(argbColor)/*.normal(matrixStack.peek(), normalVec.x(), normalVec.y(), normalVec.z())*/;
        bufferBuilder.vertex(matrices, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(argbColor)/*.normal(matrixStack.peek(), normalVec.x(), normalVec.y(), normalVec.z())*/;
        //0,0,1 -> 0,1,1
        bufferBuilder.vertex(matrices, (float) box.minX, (float) box.minY, (float) box.maxZ).color(argbColor)/*.normal(matrixStack.peek(), normalVec.x(), normalVec.y(), normalVec.z())*/;
        bufferBuilder.vertex(matrices, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(argbColor)/*.normal(matrixStack.peek(), normalVec.x(), normalVec.y(), normalVec.z())*/;
        //0,1,0 -> 0,1,1
        bufferBuilder.vertex(matrices, (float) box.minX, (float) box.maxY, (float) box.minZ).color(argbColor)/*.normal(matrixStack.peek(), normalVec.x(), normalVec.y(), normalVec.z())*/;
        bufferBuilder.vertex(matrices, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(argbColor)/*.normal(matrixStack.peek(), normalVec.x(), normalVec.y(), normalVec.z())*/;
        //0,1,0 -> 1,1,0
        bufferBuilder.vertex(matrices, (float) box.minX, (float) box.maxY, (float) box.minZ).color(argbColor)/*.normal(matrixStack.peek(), normalVec.x(), normalVec.y(), normalVec.z())*/;
        bufferBuilder.vertex(matrices, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(argbColor)/*.normal(matrixStack.peek(), normalVec.x(), normalVec.y(), normalVec.z())*/;
        //0,1,1 -> 1,1,1
        bufferBuilder.vertex(matrices, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(argbColor)/*.normal(matrixStack.peek(), normalVec.x(), normalVec.y(), normalVec.z())*/;
        bufferBuilder.vertex(matrices, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(argbColor)/*.normal(matrixStack.peek(), normalVec.x(), normalVec.y(), normalVec.z())*/;
        //1,1,0 -> 1,1,1
        bufferBuilder.vertex(matrices, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(argbColor)/*.normal(matrixStack.peek(), normalVec.x(), normalVec.y(), normalVec.z())*/;
        bufferBuilder.vertex(matrices, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(argbColor)/*.normal(matrixStack.peek(), normalVec.x(), normalVec.y(), normalVec.z())*/;
    }

    public static Vector3f getNormal(float x1, float y1, float z1, float x2, float y2, float z2) {
        float xNormal = x2 - x1;
        float yNormal = y2 - y1;
        float zNormal = z2 - z1;
        float normalSqrt = MathHelper.sqrt(xNormal * xNormal + yNormal * yNormal + zNormal * zNormal);

        return new Vector3f(xNormal / normalSqrt, yNormal / normalSqrt, zNormal / normalSqrt);
    }
}
