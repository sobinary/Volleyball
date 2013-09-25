package graphics;


import java.io.PrintWriter;
import java.io.StringWriter;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

public class Texture 
{
	int textureIds[];
	Rectangle rect;
	Bitmap bitmap;
	
	public Texture(Context cont, GL10 gl, String fname, Rectangle rect)
	{
		this(cont, gl, fname, rect, 1);
	}
	
	public Texture(Context cont, GL10 gl, String fname, Rectangle rect, int count)
	{
		this.rect = rect;
		try
		{
			textureIds = new int[count];
			gl.glGenTextures(count, textureIds, 0);
			bitmap = BitmapFactory.decodeStream( cont.getAssets().open(fname) );
			for(int i=0; i < count; i++)
			{
				gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIds[i]);
				GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
				
				gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
				gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
				GLES20.glEnable(GLES20.GL_BLEND);
				GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
			}
			bitmap.recycle();
		} 
		catch(Exception e)
		{
			printe(e);
		}
	}
	
	public void draw(GL10 gl)
	{
		draw(gl, 0);
	}

	public void draw(GL10 gl, int i)
	{
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIds[i]);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		rect.vertices.position(0);
		gl.glVertexPointer(2, GL10.GL_FLOAT, rect.vertexSize, rect.vertices);
		rect.vertices.position(2);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, rect.vertexSize, rect.vertices);
		gl.glDrawElements(GL10.GL_TRIANGLES, 6, GL10.GL_UNSIGNED_SHORT, rect.indices);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
	}
	
	public static void printe(Throwable e)
	{
		java.io.StringWriter traceText = new StringWriter();
		java.io.PrintWriter pWriter = new PrintWriter(traceText,true);
		e.printStackTrace(pWriter);
		pWriter.close();
		Log.d("volleyball", traceText.toString() ); 
	}

}
