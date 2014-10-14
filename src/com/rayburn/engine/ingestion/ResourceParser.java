package com.rayburn.engine.ingestion;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector3f;
import com.rayburn.engine.entity.geom.OBJModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Scanner;

import static org.lwjgl.opengl.GL11.GL_FLOAT;

import static org.lwjgl.opengl.GL11.glNormalPointer;
import static org.lwjgl.opengl.GL11.glVertexPointer;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;

/**
 * @author Will Stuckey
 * @date 11/26/13
 * <p>This class contains resources to parse external resource files.</p>
 */
public class ResourceParser
{
	/**
	 * Parses a .OBJ file for vertices. Supports triangles and quads. Ignores faces and normals.
	 *
	 * @author Lizzy
	 * @param file The location of the object file.
	 * @return Vertex Data
	 * @throws FileNotFoundException If the file is invalid.
	 */
	public static ArrayList<Float> parseOBJVerticies(File file) throws FileNotFoundException
	{
		//If a file is not given, it is null.
		if (file == null)
		{
			throw new FileNotFoundException("file not valid");
		}

		//If it is not a file, it is null.
		if (!file.isFile())
		{
			throw new FileNotFoundException("file not found");
		}

		ArrayList<Float> vertexData = new ArrayList<Float>();

		//Creates a scanner for the provided file.
		Scanner fileScanner = new Scanner(file);
		while (fileScanner.hasNextLine())
		{
			//Takes in a line
			String line = fileScanner.nextLine();

			//For lines that start with a "v"
			if (line.length() > 1 && line.charAt(0) == 'v')
			{
				//Lines split when a space is present.
			 	String[] lineElements = line.split(" ");
				for (int index = 1; index < lineElements.length; index += 1)
				{
					//Checking to make sure the string is a number. Then pulls out the number.
					float number = Float.parseFloat(lineElements[index]);
					vertexData.add(number);
				}
			}
		}

		return vertexData;
	}

	/**
	 * Parses a .OBJ file for normals. Ignores faces adn vertices.
	 *
	 * @author Lizzy
	 * @param file The location of the object file.
	 * @return Vertex Data
	 * @throws FileNotFoundException If the file is invalid.
	 */
	public static ArrayList<Float> parseOBJNormals(File file) throws FileNotFoundException
	{
		//If a file is not given, it is null.
		if (file == null)
		{
			throw new FileNotFoundException("file not valid");
		}

		//If it is not a file, it is null.
		if (!file.isFile())
		{
			throw new FileNotFoundException("file not found");
		}

		ArrayList<Float> normalData = new ArrayList<Float>();

		//Creates a scanner for the provided file.
		Scanner fileScanner = new Scanner(file);
		while (fileScanner.hasNextLine())
		{
			//Takes in a line
			String line = fileScanner.nextLine();

			//For lines that start with a "vn"
			if (line.length() > 2 && line.substring(0, 2).equals("vn"))
			{
				//Lines split when a space is present.
				String[] lineElements = line.split(" ");
				for (int index = 1; index < lineElements.length; index += 1)
				{
					//Checking to make sure the string is a number. Then pulls out the number.
					float number = Float.parseFloat(lineElements[index]);
					normalData.add(number);
				}
			}
		}

		return normalData;
	}

	/**
	 * @author Lizzy
	 *
	 * @param file
	 * @param geom
	 * @return
	 * @throws FileNotFoundException
	 */
	public static int[] genWavefrontVBOSet(File file, OBJModel geom) throws FileNotFoundException
	{
		//If a file is not given, it is null.
		if (file == null)
		{
			throw new FileNotFoundException("file not valid");
		}

		//If it is not a file, it is null.
		if (!file.isFile())
		{
			throw new FileNotFoundException("file not found");
		}

		int lineCt = 0;
		Scanner scanner = new Scanner(file);
		while (scanner.hasNextLine())
		{
			System.out.println("LINE: " + ++lineCt);
			String[] elements = scanner.nextLine().split(" ");
			if (elements[0].equalsIgnoreCase("v"))
			{
				geom.vertices.add(new Vector3f(
						Float.parseFloat(elements[1]),
						Float.parseFloat(elements[2]),
						Float.parseFloat(elements[3])));
			}
			else if (elements[0].equalsIgnoreCase("vn"))
			{
				geom.normals.add(new Vector3f(
						Float.parseFloat(elements[1]),
						Float.parseFloat(elements[2]),
						Float.parseFloat(elements[3])));
			}
			else if (elements[0].equalsIgnoreCase("f"))
			{
				int[] verticies = {Integer.parseInt(elements[1].split("/")[0]),
						Integer.parseInt(elements[2].split("/")[0]),
						Integer.parseInt(elements[3].split("/")[0])
				};

				if (geom.hasNormals())
				{
					int[] normals = {Integer.parseInt(elements[1].split("/")[2]),
							Integer.parseInt(elements[2].split("/")[2]),
							Integer.parseInt(elements[3].split("/")[2])};
					geom.faces.add(new OBJModel.ModelFace(verticies, normals));
				}
				else
				{
					geom.faces.add(new OBJModel.ModelFace(verticies));
				}
			}
		}
		scanner.close();

		int vertexHandler = glGenBuffers();
		int normalHandler = glGenBuffers();
		FloatBuffer vertices = BufferUtils.createFloatBuffer(geom.faces.size() * 9);
		FloatBuffer normals = BufferUtils.createFloatBuffer(geom.faces.size() * 9);

		for (OBJModel.ModelFace face : geom.faces)
		{
			vertices.put(geom.vertices.get(face.vertexIndexArray[0] - 1).getX());
			vertices.put(geom.vertices.get(face.vertexIndexArray[0] - 1).getY());
			vertices.put(geom.vertices.get(face.vertexIndexArray[0] - 1).getZ());
			vertices.put(geom.vertices.get(face.vertexIndexArray[1] - 1).getX());
			vertices.put(geom.vertices.get(face.vertexIndexArray[1] - 1).getY());
			vertices.put(geom.vertices.get(face.vertexIndexArray[1] - 1).getZ());
			vertices.put(geom.vertices.get(face.vertexIndexArray[2] - 1).getX());
			vertices.put(geom.vertices.get(face.vertexIndexArray[2] - 1).getY());
			vertices.put(geom.vertices.get(face.vertexIndexArray[2] - 1).getZ());

			normals.put(geom.normals.get(face.normalIndexArray[0] - 1).getX());
			normals.put(geom.normals.get(face.normalIndexArray[0] - 1).getY());
			normals.put(geom.normals.get(face.normalIndexArray[0] - 1).getZ());
			normals.put(geom.normals.get(face.normalIndexArray[1] - 1).getX());
			normals.put(geom.normals.get(face.normalIndexArray[1] - 1).getY());
			normals.put(geom.normals.get(face.normalIndexArray[1] - 1).getZ());
			normals.put(geom.normals.get(face.normalIndexArray[2] - 1).getX());
			normals.put(geom.normals.get(face.normalIndexArray[2] - 1).getY());
			normals.put(geom.normals.get(face.normalIndexArray[2] - 1).getZ());
		}

		vertices.flip();
		normals.flip();

		glBindBuffer(GL_ARRAY_BUFFER, vertexHandler);
		glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
		glVertexPointer(3, GL_FLOAT, 0, 0L);

		glBindBuffer(GL_ARRAY_BUFFER, normalHandler);
		glBufferData(GL_ARRAY_BUFFER, normals, GL_STATIC_DRAW);
		glNormalPointer(GL_FLOAT, 0, 0L);

		glBindBuffer(GL_ARRAY_BUFFER, 0);

		return new int[]{vertexHandler, normalHandler};
	}

	public static OBJModel genModelFromWavefrontOBJ(File file) throws FileNotFoundException
	{
		//If a file is not given, it is null.
		if (file == null)
		{
			throw new FileNotFoundException("file not valid");
		}

		//If it is not a file, it is null.
		if (!file.isFile())
		{
			throw new FileNotFoundException("file not found");
		}

		OBJModel geom = new OBJModel();

		int lineCt = 0;
		Scanner scanner = new Scanner(file);
		while (scanner.hasNextLine())
		{
			System.out.println("LINE: " + ++lineCt);
			String[] elements = scanner.nextLine().split(" ");
			if (elements[0].equalsIgnoreCase("v"))
			{
				geom.vertices.add(new Vector3f(
						Float.parseFloat(elements[1]),
						Float.parseFloat(elements[2]),
						Float.parseFloat(elements[3])));
			}
			else if (elements[0].equalsIgnoreCase("vn"))
			{
				geom.normals.add(new Vector3f(
						Float.parseFloat(elements[1]),
						Float.parseFloat(elements[2]),
						Float.parseFloat(elements[3])));
			}
			else if (elements[0].equalsIgnoreCase("f"))
			{
				int[] vertices = {Integer.parseInt(elements[1].split("/")[0]),
						Integer.parseInt(elements[2].split("/")[0]),
						Integer.parseInt(elements[3].split("/")[0])
				};

				if (geom.hasNormals())
				{
					int[] normals = {Integer.parseInt(elements[1].split("/")[2]),
							Integer.parseInt(elements[2].split("/")[2]),
							Integer.parseInt(elements[3].split("/")[2])};
					geom.faces.add(new OBJModel.ModelFace(vertices, normals));
				}
				else
				{
					geom.faces.add(new OBJModel.ModelFace(vertices));
				}
			}
		}
		scanner.close();

		FloatBuffer vertices = BufferUtils.createFloatBuffer(geom.faces.size() * 9);
		FloatBuffer normals = BufferUtils.createFloatBuffer(geom.faces.size() * 9);

		for (OBJModel.ModelFace face : geom.faces)
		{
			vertices.put(geom.vertices.get(face.vertexIndexArray[0] - 1).getX());
			vertices.put(geom.vertices.get(face.vertexIndexArray[0] - 1).getY());
			vertices.put(geom.vertices.get(face.vertexIndexArray[0] - 1).getZ());
			vertices.put(geom.vertices.get(face.vertexIndexArray[1] - 1).getX());
			vertices.put(geom.vertices.get(face.vertexIndexArray[1] - 1).getY());
			vertices.put(geom.vertices.get(face.vertexIndexArray[1] - 1).getZ());
			vertices.put(geom.vertices.get(face.vertexIndexArray[2] - 1).getX());
			vertices.put(geom.vertices.get(face.vertexIndexArray[2] - 1).getY());
			vertices.put(geom.vertices.get(face.vertexIndexArray[2] - 1).getZ());

			normals.put(geom.normals.get(face.normalIndexArray[0] - 1).getX());
			normals.put(geom.normals.get(face.normalIndexArray[0] - 1).getY());
			normals.put(geom.normals.get(face.normalIndexArray[0] - 1).getZ());
			normals.put(geom.normals.get(face.normalIndexArray[1] - 1).getX());
			normals.put(geom.normals.get(face.normalIndexArray[1] - 1).getY());
			normals.put(geom.normals.get(face.normalIndexArray[1] - 1).getZ());
			normals.put(geom.normals.get(face.normalIndexArray[2] - 1).getX());
			normals.put(geom.normals.get(face.normalIndexArray[2] - 1).getY());
			normals.put(geom.normals.get(face.normalIndexArray[2] - 1).getZ());
		}

		vertices.flip();
		normals.flip();

		geom.setVertexFloatBuffer(vertices);
		geom.setNormalFloatBuffer(normals);

		return geom;
	}
}
