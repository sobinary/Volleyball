package graphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Rectangle 
{
	public ShortBuffer indices;
	public FloatBuffer vertices;
	public int vertexSize;
	
	public Rectangle(boolean color, boolean tex)
	{
		ByteBuffer bytes = ByteBuffer.allocateDirect(12);
		bytes.order(ByteOrder.nativeOrder());
		indices = bytes.asShortBuffer();
		indices.put( new short[]{
				0, 1, 2,
				2, 3, 0
		});
		indices.flip();
		
		vertexSize = 4 * (2 + (color?2:0) + (tex?2:0) );
		bytes = ByteBuffer.allocateDirect( vertexSize * 4 );
		bytes.order(ByteOrder.nativeOrder());
		vertices = bytes.asFloatBuffer();
	}

	public void setVertices(float left, float right, float top, float bottom)
	{
		vertices.put( new float[]{
				left, bottom, 0.0f, 1.0f,
				right, bottom, 1.0f, 1.0f,
				right, top, 1.0f, 0.0f,
				left, top, 0.0f, 0.0f
		});			
		vertices.flip();
	}
	
	public void setVertices()
	{
		int left = 100; int right = 132; int top = 132; int bottom = 100;
		
		if(vertexSize == 8 )
		{
			vertices.put( new float[]{
					left, bottom,
					right, bottom,
					right, top,
					left, top
			});
		}
		else
		{
			vertices.put( new float[]{
					left, bottom, 0.0f, 1.0f,
					right, bottom, 1.0f, 1.0f,
					right, top, 1.0f, 0.0f,
					left, top, 0.0f, 0.0f
			});			
		}
		vertices.flip();
	}
	
	public void draw(GL10 gl)
	{
		gl.glVertexPointer(2, GL10.GL_FLOAT, vertexSize, vertices);
		gl.glDrawElements(GL10.GL_TRIANGLES, 6, GL10.GL_UNSIGNED_SHORT, indices);
		
	}
	
	public void change()
	{
		if(vertexSize == 8)
		{
			vertices.put( new float[]{
					vertices.get(0) + 0.5f, vertices.get(1),
					vertices.get(2) + 0.5f, vertices.get(3),
					vertices.get(4) + 0.5f, vertices.get(5),
					vertices.get(6) + 0.5f, vertices.get(7)
			});
			vertices.flip();
		}

		else
		{
			vertices.put( new float[]{
					vertices.get(0) + 0.5f, vertices.get(1),
					vertices.get(4) + 0.5f, vertices.get(5),
					vertices.get(8) + 0.5f, vertices.get(9),
					vertices.get(12) + 0.5f, vertices.get(13)
			});
			vertices.flip();			
		}
	}
	
}
