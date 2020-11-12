package br.pucpr.cg;

import br.pucpr.mage.Mesh;
import br.pucpr.mage.MeshBuilder;
import br.pucpr.mage.Shader;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

import static br.pucpr.mage.MathUtil.cross;
import static br.pucpr.mage.MathUtil.sub;

public class MeshFactory {
    public static Mesh createCube(Shader shader) {
        return new MeshBuilder(shader)
            .addVector3fAttribute("aPosition",
                //Face próxima
                -0.5f,  0.5f,  0.5f,  //0
                 0.5f,  0.5f,  0.5f,  //1
                -0.5f, -0.5f,  0.5f,  //2
                 0.5f, -0.5f,  0.5f,  //3
                //Face afastada
                -0.5f,  0.5f, -0.5f,  //4
                 0.5f,  0.5f, -0.5f,  //5
                -0.5f, -0.5f, -0.5f,  //6
                 0.5f, -0.5f, -0.5f,  //7
                //Face superior
                -0.5f,  0.5f,  0.5f,  //8
                 0.5f,  0.5f,  0.5f,  //9
                -0.5f,  0.5f, -0.5f,  //10
                 0.5f,  0.5f, -0.5f,  //11
                //Face inferior
                -0.5f, -0.5f,  0.5f,  //12
                 0.5f, -0.5f,  0.5f,  //13
                -0.5f, -0.5f, -0.5f,  //14
                 0.5f, -0.5f, -0.5f,  //15
                //Face direita
                0.5f, -0.5f,  0.5f,  //16
                0.5f,  0.5f,  0.5f,  //17
                0.5f, -0.5f, -0.5f,  //18
                0.5f,  0.5f, -0.5f,  //19
                //Face esquerda
                -0.5f, -0.5f,  0.5f,   //20
                -0.5f,  0.5f,  0.5f,   //21
                -0.5f, -0.5f, -0.5f,  //22
                -0.5f,  0.5f, -0.5f)  //23
            .addVector3fAttribute("aNormal",
                //Face próxima
                0.0f,  0.0f,  1.0f,
                0.0f,  0.0f,  1.0f,
                0.0f,  0.0f,  1.0f,
                0.0f,  0.0f,  1.0f,
                //Face afastada
                0.0f,  0.0f, -1.0f,
                0.0f,  0.0f, -1.0f,
                0.0f,  0.0f, -1.0f,
                0.0f,  0.0f, -1.0f,
                //Face superior
                0.0f,  1.0f,  0.0f,
                0.0f,  1.0f,  0.0f,
                0.0f,  1.0f,  0.0f,
                0.0f,  1.0f,  0.0f,
                //Face inferior
                0.0f, -1.0f,  0.0f,
                0.0f, -1.0f,  0.0f,
                0.0f, -1.0f,  0.0f,
                0.0f, -1.0f,  0.0f,
                //Face direita
                1.0f,  0.0f,  0.0f,
                1.0f,  0.0f,  0.0f,
                1.0f,  0.0f,  0.0f,
                1.0f,  0.0f,  0.0f,
                //Face esquerda
                -1.0f,  0.0f,  0.0f,
                -1.0f,  0.0f,  0.0f,
                -1.0f,  0.0f,  0.0f,
                -1.0f,  0.0f,  0.0f)
            .setIndexBuffer(
                //Face próxima
                0,  2,  3,
                0,  3,  1,
                //Face afastada
                4,  7,  6,
                4,  5,  7,
                //Face superior
                8, 11, 10,
                8,  9, 11,
                //Face inferior
                12, 14, 15,
                12, 15, 13,
                //Face direita
                16, 18, 19,
                16, 19, 17,
                //Face esquerda
                20, 23, 22,
                20, 21, 23)
            .create();
    }

    // Conversão de um índice no formato (x,z) para o linear, usado no index buffer
    // (x,z) = x + z * width
    private static int linearIndex(int x, int z, int width) {
       return x + z * width;
    }

    private static float calcLinear(float min, float max, float value, boolean inverse) {
        var range = max - min;
        var result = (value - min) / range;
        result = result < 0 ? 0 :
                (result > 1 ? 1 : result);
        return inverse ? 1 - result : result;
    }

    private static float calcPiramid(float min, float max, float value) {
        var mid = (min + max) / 2.0f;
        return value <= mid ?
                calcLinear(min, mid, value, false) :
                calcLinear(mid, max, value, true);
    }

    public static Mesh loadTerrain(Shader shader, String name, float scale, int texRepeat) {
        try {
            var img = ImageIO.read(new File(name));

            var width = img.getWidth();
            var depth = img.getHeight();

            //Vértices (posições) da malha
            //----------------------------

            //Subtraímos metade da altura e largura para garantir que fique no centro
            var offset = new Vector3f(width / 2.0f, 0.0f, depth / 2.0f);
            var positions = new ArrayList<Vector3f>();
            var maxHeight = 0;
            for (var z = 0; z < depth; z++) {
                for (var x = 0; x < width; x++) {
                    var dx = x != width-1 ? 1 : -1;
                    var dz = z != depth - 1 ? 1 : -1;

                    //Suavização do terreno
                    var tone1 = new Color(img.getRGB(x, z)).getRed();
                    var tone2 = new Color(img.getRGB(x+dx, z)).getRed();
                    var tone3 = new Color(img.getRGB(x, z+dz)).getRed();
                    var tone4 = new Color(img.getRGB(x+dx, z+dz)).getRed();
                    var tone = (tone1 + tone2 + tone3 + tone4) / 4.0f;

                    if (tone1 > maxHeight) maxHeight = tone1;

                    positions.add(new Vector3f(x, tone * scale, z).sub(offset));
                }
            }

            //Index buffer
            //------------

            //Os índices são criados a cada quadrado da malha. Observe que há uma linha e coluna a menos de quadrados
            //do que há de vértices.
            var indices = new ArrayList<Integer>();
            for (var z = 0; z < depth-1; z++) {
                for (var x = 0; x < width-1; x++) {
                    var zero = linearIndex(x, z, width);
                    var one = linearIndex(x+1, + z, width);
                    var two = linearIndex(x, z+1, width);
                    var three = linearIndex(x+1, z+1, width);

                    indices.add(zero);
                    indices.add(three);
                    indices.add(one);

                    indices.add(zero);
                    indices.add(two);
                    indices.add(three);
                }
            }

            //Normais de superfície
            //---------------------
            var normals = new ArrayList<Vector3f>();
            for (var i = 0; i < positions.size(); i++) {
                normals.add(new Vector3f());
            }

            //Percorremos triangulo por triangulo
            for (var i = 0; i < indices.size(); i += 3) {
                //Índices dos 3 vértices
                var i1 = indices.get(i);
                var i2 = indices.get(i+1);
                var i3 = indices.get(i+2);

                //Três vértices do triangulo
                var v1 = positions.get(i1);
                var v2 = positions.get(i2);
                var v3 = positions.get(i3);

                //Vetores dos lados.
                var side1 = sub(v2, v1);
                var side2 = sub(v3, v1);

                //Vetor que faz 90 graus com o lado. Seu tamanho ainda será o dobro da área do triangulo.
                //Isso é util pois ele será acumulado no vértice, assim triangulos maiores terão um peso maior.
                var normal = cross(side1, side2);

                //Somamos o vetor resultante em cada vértice. Lembre-se que há vértices coincidentes entre vários
                //triangulos da malha. A soma causará um vetor na direção aproximada da média dos vetores.
                normals.get(i1).add(normal);
                normals.get(i2).add(normal);
                normals.get(i3).add(normal);
            }

            //Normalizamos todos os vetores, para que tenham tamanho 1.
            for (var normal : normals) {
                normal.normalize();
            }

            //Calculo das texturas
            var tu = 1.0f / (width-1) * texRepeat;
            var tv = 1.0f / (depth-1) * texRepeat;

            var texCoords = new ArrayList<Vector2f>();
            for (var z = 0; z < depth; z++) {
                for (var x = 0; x < width; x++) {
                    texCoords.add(new Vector2f(x * tu, z * tv));
                }
            }

            //Calculo dos pesos
            var texWeights = new ArrayList<Vector4f>();
            for (var z = 0; z < depth; z++) {
                for (var x = 0; x < width; x++) {
                    var tone = new Color(img.getRGB(x, z)).getRed();
                    var h = tone / (float)maxHeight;

                    var weight = new Vector4f(
                            calcLinear(0.75f, 1.00f, h, false),
                            calcPiramid(0.50f, 0.80f, h),
                            calcPiramid(0.15f, 0.60f, h),
                            calcLinear(0.00f, 0.16f, h, true)
                    );
                    texWeights.add(weight);
                }
            }

            //Montamos a malha.
            return new MeshBuilder(shader)
                    .addVector3fAttribute("aPosition", positions)
                    .addVector3fAttribute("aNormal", normals)
                    .addVector2fAttribute("aTexCoord", texCoords)
                    .addVector4fAttribute("aTexWeight", texWeights)
                    .setIndexBuffer(indices)
                    .create();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            return null;
        }
    }
}