package rayburn.engine.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;
import static org.lwjgl.opengl.GL40.GL_TESS_CONTROL_SHADER;
import static org.lwjgl.opengl.GL40.GL_TESS_EVALUATION_SHADER;

/**
 * @author Will Stuckey
 * @date 11/26/13
 * <p>Class containing utility methods and constants for GL shader creation, and execution.</p>
 */
public final class ShaderUtil
{
	private ShaderUtil() {}

	/**
	 * Valid gl shader types
	 */
	public static enum shaderType
	{
		SHADER_VERTEX,
		SHADER_FRAGMENT,
		SHADER_GEOMETRY,
		SHADER_TESSELLATION_CONTROL,
		SHADER_TESSELLATION_EVALUATION;
	}

	/**
	 * Gets the gl shader id from a valid shader type
	 * @param type the type of shader
	 * @return the gl integer id
	 */
	public static int getShaderId(shaderType type)
	{
		int id = GL_VERTEX_SHADER;

		switch (type)
		{
			case SHADER_VERTEX: id = GL_VERTEX_SHADER; break;
			case SHADER_FRAGMENT: id = GL_FRAGMENT_SHADER; break;
			case SHADER_GEOMETRY: id = GL_GEOMETRY_SHADER; break;
			case SHADER_TESSELLATION_CONTROL: id = GL_TESS_CONTROL_SHADER; break;
			case SHADER_TESSELLATION_EVALUATION: id = GL_TESS_EVALUATION_SHADER; break;
		}

		return id;
	}

	/**
	 * Build, compile, and return the shader id from shader source
	 * @param source directory of the source
	 * @param shaderType shader type
	 * @return on success, shader id, on failure, Integer.MIN_VALUE
	 * @throws IOException
	 */
	public static int compileShader(String source, shaderType shaderType) throws IOException
	{
		return compileShader(new File(source), shaderType);
	}

	/**
	 * Build, compile, and return the shader id from shader source
	 * @param source File of the source
	 * @param shaderType shader type
	 * @return on success, shader id, on failure, Integer.MIN_VALUE
	 * @throws IOException
	 */
	public static int compileShader(File source, shaderType shaderType) throws IOException
	{
		return compileShader(new FileReader(source), shaderType);
	}

	/**
	 * Build, compile, and return the shader id from shader source
	 * @param source FileReader of the source
	 * @param shaderType shader type
	 * @return on success, shader id, on failure, Integer.MIN_VALUE
	 * @throws IOException
	 */
	public static int compileShader(FileReader source, shaderType shaderType) throws IOException
	{
		int shader = glCreateShader(getShaderId(shaderType));
		StringBuilder shaderSource = new StringBuilder();
		BufferedReader shaderBuilder = new BufferedReader(source);

		String line;
		while((line = shaderBuilder.readLine()) != null)
			shaderSource.append(line).append("\n");

		shaderBuilder.close();

		glShaderSource(shader, shaderSource);
		glCompileShader(shader);
		if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE)
			return Integer.MIN_VALUE;
		else
			return shader;
	}
}
