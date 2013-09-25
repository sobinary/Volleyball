package com.sobinary.volleyball;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


import learning.BasicBrain;
import learning.Brain;
import learning.PhysReq;
import actors.Ball;
import actors.BallView;
import actors.Court;
import actors.Player;
import actors.PlayerView;
import actors.WorldView;
import android.content.Context;
import android.hardware.Sensor;
import android.opengl.GLSurfaceView.Renderer;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;




public class Engine implements Renderer, OnTouchListener
{
	public static final double DT = 1d/50d;
	public static final float DTF = (float)DT;
	private static final long DT_NANO = (long)(DT * 1000000000d);

	private Context cont;
	private static Engine me;
	
	private int pHeight, pWidth, nanoErrN, lock, stepSize;
	private float p2lx, p2ly, dt, vBall[], buf[], buf2[], nanoErrSum;

	private HUD hud;
	private Dispatch dispatch;
	private Player leftP, rightP;
	private Ball ball;
	private Court court;
	private WorldView world; 
	
	private boolean paused, zCam;
	public long n, cmaInd, startTime;
	
	
	public Engine(Context cont, int pWidth, int pHeight)
	{    
		this.cont = cont;  
		Engine.me = this;
		
		this.p2lx = Court.W / (float)pWidth;
		this.p2ly = Court.H / (float)pHeight;    		
		this.pWidth = pWidth;  
		this.pHeight = pHeight;
		this.dt = (float)DT;
		
		this.vBall = new float[7];
		this.buf = new float[8];
		this.buf2 = new float[8];

		initActors();
		this.dispatch = new Dispatch(leftP, rightP, ball, hud, this);

		lock = hud.lockLines(1);
		this.stepSize = 2;
		autoFight(false);
		
		PhysReq.init(leftP, rightP);
	}  
	 
	private void initActors()
	{
		hud = new HUD();
		court = new Court();
		
		ball = new Ball();
		leftP = new Player(false, 1);
		rightP = new Player(true, 1);

		BallView leftB = new BallView(ball, true);
		BallView rightB = new BallView(ball, false);

		PlayerView vLeftP = new PlayerView(leftP);
		PlayerView vRightP = new PlayerView(rightP);

		Brain lBrain = new BasicBrain(leftP, vRightP, leftB, "Basic");
		Brain rBrain = new BasicBrain(rightP, vLeftP, rightB, "Basic");
		
		leftP.setBrain(lBrain);
		rightP.setBrain(rBrain);
		
		world = new WorldView();
		world.setPlayerView(vLeftP);
		world.setPlayerView(vRightP);
	}
	
	private void initGraphics(GL10 gl)
	{
		leftP.initGraphics(cont, gl);
		rightP.initGraphics(cont, gl);
		ball.initGraphics(cont, gl);
		court.initGraphics(cont, gl);
		hud.initGraphics(cont, gl);
	}


	
	
	
	
	
	
	
	
	
	/*****************************SIM LOOP********************************/
	
	
	@Override
	public void onDrawFrame(GL10 gl) 
	{  
		blit(gl);
		update();
		render(gl);
	}

	private void render(GL10 gl)
	{
		court.draw(gl);
		hud.draw(gl);
		ball.draw(gl);
		leftP.draw(gl);
		rightP.draw(gl);
	}
	
	private void update()
	{
		dispatch.update(paused);
		if(paused) return;
		calcTime();

		detectCol();
	
		ball.update(dt);
		leftP.update(dt);
		rightP.update(dt);
		
		n++;
		world.clearCol();
	}
 	 
	void calcTime()
	{
		final int FST_MIN = 99;
		if(n > FST_MIN)
		{
			long elapsed = System.nanoTime() - startTime;
			int rem = (int)(DT_NANO - elapsed);
			if(rem > 0)
			{	
				try{Thread.sleep(0, rem);}
				catch(Exception e){}
			}
			else
			{
				nanoErrN++;
				nanoErrSum += -rem;
			}
		}
		startTime = System.nanoTime();
	}

	public void xStop()
	{
		float sum = (nanoErrSum / nanoErrN) / 1000000;
		Core.print(nanoErrN + " overtimes averaging " + sum + " milliseconds");
		dispatch.endListen();
	}
	
	public void xPause()
	{
		dispatch.endListen();
	}
	

	
	
	
	
	
	
	
	
	
	
	/**************************COLLISION DETECTION*******************************/
	
	
	private void detectCol()
	{
		if(!detectCol(rightP, false))
			detectCol(leftP, true);
	}
	
	private boolean detectCol(Player pl, boolean fake)
	{
		ball.getState(vBall, fake);
		if(broadPhase(pl))
			if(narrowPhase(pl))
				return applyCol(pl, fake);
		return false;
	}
	 
	private boolean broadPhase(Player pl)
	{
		if(pl.vT != 0f) return true;
	
		if(pl.tAngle != 90f) 
			return Core.dist(vBall[0], vBall[1], pl.xTorso, pl.yTorso) < Player.TORSO_H * 4f;
		else
			return Core.dist(vBall[0], vBall[1], pl.xTorso, pl.yTorso) < Player.TORSO_H * 1f;
	}
	    
	boolean narrowPhase(Player p)
	{
		if(!p.unlockCol(dt)) return false;
		 
		Core.closestPoint(p.xLo, p.yLo, p.xHi, p.yHi, vBall[0], vBall[1], buf);
		float dist = Core.dist(vBall[0], vBall[1], buf[0], buf[1]);
		
		boolean b = (buf[2] >= 0f - 0.2f) && (buf[2] <= 1f + 0.2f);
		b &= buf[0] >= Core.min(p.xLo, p.xHi);
		b &= buf[0] <= Core.max(p.xLo, p.xHi);
		
		hud.setLineDest(lock, vBall[0], vBall[1], buf[0], buf[1], p.dir);
		
		return (b && dist < Ball.RAD);
	}


	
	
	
	
	
	
	
	
	
	
	
	
	
	/**************************COLLISION APPLICATION*******************************/
	
		
	private boolean applyCol(Player pl, boolean fake)
	{
		if(pl.tAngle != 90 && pl.yCalves > 0f)
		{
//			no();
//			Dispatch.me().futureRequest(60, new String[]{toString(),"go"});
		}
		
		reflect(pl);		
		addHit(pl);
		ball.propagate(vBall, fake);			
		ball.g = Ball.G;
		pl.absorb = 1;
		pl.lockCol();
		hud.setLineDest(lock, -4, -4, -4, -4, 0);
		world.setCol(pl);
		return true;
	}	

	void reflect(Player pl)
	{
		if(pl.absorb > 1f) return;
		
		float thetaf = (float)Math.toRadians(pl.tAngle); 
		float sign = Math.signum(vBall[2]);
		
		Core.refl(-vBall[2], -vBall[3], Math.cos(Math.PI/2 + thetaf), Math.sin(Math.PI/2 + thetaf), buf2);
		vBall[2] = buf2[0] * (float)Math.sqrt(pl.absorb); 
		vBall[3] = buf2[1] * (float)Math.sqrt(pl.absorb);
		
		if(Math.signum(vBall[2]) != sign) ball.gFlip();
	}
		
	void addHit(Player pl)
	{
		if(pl.tAngle == 90f || pl.vT == 0f) return;
		
		float r = 28;
		float vT = (float)Math.toRadians(pl.vT);
		
		float mx = (float)Math.cos(Math.toRadians(pl.tAngle + 90));
		float my = (float)Math.sin(Math.toRadians(pl.tAngle + 90));
		
		vBall[2] = vBall[2] + (mx*vT*r);
		vBall[3] = vBall[3] + (my*vT*r);

		pl.preemptTorso();
	}
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/***********************************GRAPHICS*************************************/
	
	

	public void onSurfaceCreated(GL10 gl, EGLConfig config) 
	{
		gl.glClearColor(0, 0, 0, 1);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);			
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrthof(0, Court.W, 0, Court.H, 1, -1);
		gl.glViewport(0, 0, pWidth, pHeight);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		initGraphics(gl);
	}  

	private void blit(GL10 gl)
	{
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);			
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		  
		if(zCam)
		{
			float left = leftP.xEdge;
			float right = rightP.xEdge;
			float hi = Core.max(vBall[1], Court.H);

			float xMid = ((right - left) / 2) + left;
			float xRat = (right - left) / Court.W;
			float yRat = hi / Court.H;  
			float rat = Core.max(xRat, yRat);

			float x = (rat * Court.W) / 2;
			float y = rat * Court.H;

			gl.glOrthof(xMid - x, xMid + x, 0, y, 1, -1);
		}
		else
		{
			gl.glOrthof(0, Court.W, 0, Court.H, 1, -1);			
		}
	}
	   
	
	public void onSurfaceChanged(GL10 gl, int width, int height) 
	{
	}
	
	
	
	
	
	
	
	
	
	
	
	/***********************************MISC*********************************/
	
	
	private float toLogicalX(float x)
	{
		return p2lx * x;
	}
	
	private float toLogicalY(float y)
	{
		return p2ly * (pHeight - y);
	}
	
	public void onAccuracyChanged(Sensor sensor, int accuracy){}

	public boolean onTouch(View v, MotionEvent touch) 
	{
		int action = touch.getAction();
		float x = toLogicalX( touch.getX() );
		float y = toLogicalY( touch.getY() );

		switch(action)
		{						
			case MotionEvent.ACTION_DOWN:
			{
				if(x < HUD.LEN_CON && y > Court.H - HUD.LEN_CON)
				{
					if(dispatch.isConnected()) dispatch.endListen();
					else dispatch.listen();
				}
				else
				{
					go();
					ball.test(x, y, dt);

					if(ball.x < Court.W/2)
					{
						rightP.col = true;
						world.setCol(leftP);
						leftP.setBasicState(false);
						rightP.setBasicState(true);
					}
					else 
					{
						leftP.col = true;
						world.setCol(rightP);
						leftP.setBasicState(true);
						rightP.setBasicState(false);
					}
				}
			}
		}
		return true;
	}
	
	public void toggleGo()
	{
		paused = false;
		Dispatch.me().futureRequest(stepSize, new String[]{toString(),"no"});
	}
	
	public void no()
	{
		paused = true;
	}

	public void go()
	{
		paused = false;
	}

	public static Engine me()
	{
		return me;
	}

	public static float getDt()
	{
		return (float)Engine.DT;
	}

	public void autoFight(boolean state)
	{
		leftP.overCol = state;
		rightP.overCol = state;
	}
	
	Player otherPlayer(Player pl)
	{
		return pl == leftP ? rightP : leftP;
	}
	
	@Override
	public String toString()
	{
		return "engine";
	}
	
}