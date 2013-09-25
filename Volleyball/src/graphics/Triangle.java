package graphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Triangle 
{
	FloatBuffer vertices;
	
	public Triangle(boolean colors)
	{
		ByteBuffer bytes = ByteBuffer.allocateDirect(colors?72:24);
		bytes.order(ByteOrder.nativeOrder());
		vertices = bytes.asFloatBuffer();
	}
	
	public FloatBuffer setDims(int bottomY, int leftX, int rightX, int topY)
	{
		vertices.put( new float[]{
				leftX, bottomY,
				rightX, bottomY,
				(rightX - leftX) / 2, topY
		});
		vertices.flip();
		return vertices;
	}
	
	public void move(int x, int y)
	{
		float[]old = vertices.array();
		vertices.clear();
		vertices.put( new float[]{
				old[0] + x, old[1] + y,
				old[2] + x, old[3] + y,
				old[4] + x, old[5] + y,
		});
		vertices.flip();
	}
	
	public void draw(GL10 gl)
	{
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vertices);
		gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);
	}
	
}
