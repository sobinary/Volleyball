package com.sobinary.volleyball;

import java.util.Arrays;
import java.util.HashMap;

import graphics.Rectangle;
import graphics.Texture;

import javax.microedition.khronos.opengles.GL10;


import actors.Ball;
import actors.Court;
import android.content.Context;

public class HUD 
{
	private static final int MAX_TRS = 20;
	private static final int RAD_TAR = Ball.RAD;
	public static final int LEN_CON = 70;
	
	private static final int MAX_LNS = 10;
	
	private static HUD me;

	private Texture tarsTex, conTex;
	private Rectangle lines[];
	
	private float xTar[], yTar[], conAng;
	private float xLine[], yLine[], sLine[], aLine[];
	
	private boolean freeTars[];
	private boolean freeLines[];
	
	private HashMap<Object, Integer> tarMap, lineMap;
	
	public HUD()
	{
		conAng = 270f;
		
		xTar = new float[MAX_TRS];
		yTar = new float[MAX_TRS];
		
		xLine = new float[MAX_LNS];
		yLine = new float[MAX_LNS];
		sLine = new float[MAX_LNS];
		aLine = new float[MAX_LNS];
		
		freeTars = new boolean[MAX_TRS];
		freeLines = new boolean[MAX_LNS];
		
		tarMap = new HashMap<Object, Integer>();
		lineMap = new HashMap<Object, Integer>();
		
		Arrays.fill(xTar, -50);
		Arrays.fill(yTar, -50);
		Arrays.fill(freeTars, true);
		Arrays.fill(freeLines, true);
		
		me = this;
	}
 
	
	 
	
	
	/*****************************GENERAL*******************************/
	
	
	private int getFreeSpace(boolean ray[], int n, boolean lock)
	{
		int flag = 0;
		for(int i = 0; i < ray.length ; i++)
		{
			if(ray[i])
			{
				flag = 0;
				for(int j=i; j < i + n; j++)
					if(!ray[j])
						flag = 1;
				
				if(flag == 0)
				{
					if(lock) Arrays.fill(ray, i, i + n, false);
					return i;
				}
			}
		}
		return -1;
	}
	 
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*****************************LINES*******************************/
	
	
	public void lockLines(Object key, int n)
	{
		lineMap.put(key, getFreeSpace(freeLines, n , true));
	}
	
	public int lockLines(int n)
	{
		return getFreeSpace(freeLines, n , true);
	}
	
	public int findLineLock(Object key)
	{
		return lineMap.get(key); 
	}
	
	public void setLineDest(int i, float x, float y, float xTo, float yTo, float dir)
	{
		if(i < 0 || i > MAX_LNS - 1) return;
		
		float offset = (dir == 1) ? 0 : Court.W;		
		x = (offset-x)*-dir;
		xTo = (offset-xTo)*-dir;

		xLine[i] = x;
		yLine[i] = y;
		aLine[i] = (float)Math.toDegrees( Math.atan2(yTo-y, xTo-x) );
		sLine[i] = Core.dist(x, y, xTo, yTo);
	}

	public void setLineRad(int i, float x, float y, float theta, float d, float dir)
	{
		if(i < 0 || i > MAX_LNS - 1) return;
		
		float offset = (dir == 1) ? 0 : Court.W;		
		x = (offset-x)*-dir;

		xLine[i] = x;
		yLine[i] = y;
		aLine[i] = (float)Math.toDegrees(theta);
		sLine[i] = d > 0 ? d : Court.W * 2;
	}
	
	public void unlockLines(int i, int n, boolean hide)
	{
		if(i < 0 || i + n > MAX_LNS - 1) return;
		
		Arrays.fill(freeLines, i, i + n, true);
		if(hide) hideLines(i, n);
	}
	
	public void hideLines(int i, int n)
	{
		Arrays.fill(xLine, i, i + n, 0);
		Arrays.fill(yLine, i, i + n, 0);
		Arrays.fill(aLine, i, i + n, 0);
		Arrays.fill(sLine, i, i + n, 0);
	}
	
	
	
	
	
	
	
	/*****************************TARGETS******************************/
	
	
	public void lockTars(Object key, int n)
	{
		tarMap.put(key, getFreeSpace(freeTars, n, true));
	}
	
	public int findTarLock(Object key)
	{
		return tarMap.get(key);
	}
	
	public int lockTars(int n)
	{
		return getFreeSpace(freeTars, n, true);
	}
	
	public void setTar(int i, float x, float y, float dir)
	{
		if(i < 0 || i > MAX_TRS - 1) return;

		float offset = (dir == 1) ? 0 : Court.W;		
		x = (offset-x)*-dir;
		xTar[i] = x;
		yTar[i] = y;
	}
	
	public void unlockTars(int i, int n, boolean hide)
	{
		if(i < 0 || i + n > MAX_TRS - 1) return;
		Arrays.fill(freeTars, i, i + n, true);
		if(hide) hideTars(i, n);
	}
	
	public void hideTars(int i, int n)
	{
		Arrays.fill(xTar, i, i + n, -RAD_TAR);
		Arrays.fill(yTar, i, i + n, -RAD_TAR);
	}
	
	public void unlockAllTars()
	{
		Arrays.fill(freeTars, true);
	}
	
	
	
	
	
	
	
	 
	
	/*************************ONLINE BUTTON*****************************/
	
	
	
	public void setConnectOn()
	{
		conAng = 90f;
	}
	
	public void setConnectOff()
	{
		conAng = 270f;
	}
	
	public void setConnectMid()
	{
		conAng = 0f;
	}
	
	 
	
	
	
	
	
	
	/*****************************GRAPHICS******************************/
	
	public void draw(GL10 gl)
	{
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		
		for(int i=0; i < MAX_TRS; i++)
		{
			gl.glLoadIdentity();
			gl.glTranslatef(xTar[i], yTar[i], 0);
			tarsTex.draw(gl, i);
		}

		for(int i=0; i < MAX_LNS; i++)
		{
			gl.glLoadIdentity();
			gl.glTranslatef(xLine[i], yLine[i], 0);
			gl.glRotatef(aLine[i] - 90, 0, 0, 1);
			gl.glScalef(1, sLine[i], 1);
			lines[i].draw(gl);
		}
		
		gl.glLoadIdentity();
		gl.glTranslatef(LEN_CON/2, Court.H - LEN_CON/2, 0);
		gl.glRotatef(conAng - 90, 0, 0, 1);
		conTex.draw(gl);
	}
	  
	public void initGraphics( Context cont, GL10 gl)
	{
		Rectangle r2 = new Rectangle(false, true);
		r2.setVertices(-RAD_TAR, RAD_TAR, RAD_TAR, -RAD_TAR);
		tarsTex = new Texture(cont, gl, "crosshair.png", r2, MAX_TRS);
		
		lines = new Rectangle[MAX_LNS];
		for(int i=0; i < MAX_LNS; i++)
		{
			lines[i] = new Rectangle(false, true);
			lines[i].setVertices(-0.4f, 0.4f, 1, 0);
		}
		
		r2 = new Rectangle(false, true);
		r2.setVertices(-LEN_CON/2, LEN_CON/2, LEN_CON/2, -LEN_CON/2);
		conTex = new Texture(cont, gl, "power_button.png", r2);
	}
	
	public static HUD me()
	{
		return me;
	}
	
	@Override
	public String toString()
	{
		return "hud";
	}
}
