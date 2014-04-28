package rayburn.game.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

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
			if (line.length() > 2 && line.substring(0, 1).equals("vn"))
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
}
