package rayburn.game.util.geom;

import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_NORMAL_ARRAY;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_VERTEX_ARRAY;
import static org.lwjgl.opengl.GL11.glDisableClientState;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnableClientState;
import static org.lwjgl.opengl.GL11.glNormalPointer;
import static org.lwjgl.opengl.GL11.glVertexPointer;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;

/**
 * @author Kurt Shaffer
 * @date 4/29/14
 * <p></p>
 */
public class OBJModel
{
 	public ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
	public ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
	public ArrayList<ModelFace> faces = new ArrayList<ModelFace>();

	private int vertexBufferObjectID = Integer.MIN_VALUE;
	private int normalBufferObjectID = Integer.MIN_VALUE;

	private FloatBuffer vertexBuffer;
	private FloatBuffer normalBuffer;

	public OBJModel() {}

	public OBJModel(ArrayList<Vector3f> vertices,
	                ArrayList<Vector3f> normals,
	                ArrayList<ModelFace> faces)
	{
		this.vertices = vertices;
		this.normals = normals;
		this.faces = faces;
	}

	public boolean hasNormals()
	{
		return normals.size() > 0;
	}

	public void setVertexFloatBuffer(FloatBuffer vertexBuffer)
	{
		this.vertexBuffer = vertexBuffer;
	}

	public FloatBuffer getVertexFloatBuffer()
	{
		return this.vertexBuffer;
	}

	public void setNormalFloatBuffer(FloatBuffer normalBuffer)
	{
		this.normalBuffer = normalBuffer;
	}

	public FloatBuffer getNormalFloatBuffer()
	{
		return this.normalBuffer;
	}

	public void constructBuffers()
	{
		vertexBufferObjectID = glGenBuffers();
		normalBufferObjectID = glGenBuffers();
	}

	public void bindBuffers()
	{
		glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObjectID);
		glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
		glVertexPointer(3, GL_FLOAT, 0, 0L);

		glBindBuffer(GL_ARRAY_BUFFER, normalBufferObjectID);
		glBufferData(GL_ARRAY_BUFFER, normalBuffer, GL_STATIC_DRAW);
		glNormalPointer(GL_FLOAT, 0, 0L);

		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}

	public synchronized void drawBuffers()
	{
		glEnableClientState(GL_VERTEX_ARRAY);
		glEnableClientState(GL_NORMAL_ARRAY);

		glDrawArrays(GL_TRIANGLES, 0, this.faces.size() * 9);

		glDisableClientState(GL_NORMAL_ARRAY);
		glDisableClientState(GL_VERTEX_ARRAY);
	}

	public void destructBuffers()
	{
		glDeleteBuffers(vertexBufferObjectID);
		glDeleteBuffers(normalBufferObjectID);
	}

	/**
	 * @author Lizzy
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
