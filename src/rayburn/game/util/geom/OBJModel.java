package rayburn.game.util.geom;

import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;

/**
 * @author Kurt Shaffer
 * @date 4/29/14
 * <p></p>
 */
public class OBJModel
{
 	public ArrayList<Vector3f> verticies = new ArrayList<Vector3f>();
	public ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
	public ArrayList<ModelFace> faces = new ArrayList<ModelFace>();

	public OBJModel() {}

	public OBJModel(ArrayList<Vector3f> verticies, ArrayList<Vector3f> normals, ArrayList<ModelFace> faces)
	{
		this.verticies = verticies;
		this.normals = normals;
		this.faces = faces;
	}

	public boolean hasNormals()
	{
		return normals.size() > 0;
	}

	/**
	 * @author Kurt Shaffer
	 * @date 4/29/14
	 */
	public static class ModelFace
	{
	 	public int[] vertexIndexArray = {Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
		public int[] normalIndexArray = {Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};

		public ModelFace(int[] vertexIndexArray)
		{
			this(vertexIndexArray, new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE});
		}

		public ModelFace(int[] vertexIndexArray, int[] normalIndexArray)
		{
			assert vertexIndexArray.length == 3 && normalIndexArray.length == 3;

			this.vertexIndexArray = vertexIndexArray;
			this.normalIndexArray = normalIndexArray;
		}

		public boolean hasNormals()
		{
			return normalIndexArray[0] != Integer.MIN_VALUE;
		}
	}
}
