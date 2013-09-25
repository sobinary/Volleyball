package com.sobinary.volleyball;



import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
    
public class Core       
{  
	private static final float h = 0.0001f;
	private static final float CHEAT = 0.0f;
	
	
	public static void printe(String custom, Throwable e)
	{
		if( e == null)   
		{    
			print(custom + ": NULL EXCEPTION");
			return;
		}    
		else  
		{
			java.io.StringWriter traceText = new StringWriter();
			java.io.PrintWriter pWriter = new PrintWriter(traceText,true);
			e.printStackTrace(pWriter);
			pWriter.close();
			print( custom + traceText.toString() ); 			
		}
	}
	
	public static boolean checkConnection(Context cont)
	{
		ConnectivityManager cm = (ConnectivityManager) cont.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return  netInfo != null && netInfo.isConnectedOrConnecting();
	}
	
	public static void printe(Throwable e)
	{
		printe("", e);
	}

	public static void print(String s)
	{
		Log.d("volleyball", s);
	}
	
	public static void print(Object s)
	{
		Log.d("volleyball", s.toString());
	}

	public static Object load(String fname , Context cont)
	{
		try
		{
			FileInputStream fis = cont.openFileInput(fname);
			ObjectInputStream ois = new ObjectInputStream(fis);
			Object loadedObject = ois.readObject();
			ois.close();
			fis.close();
			return loadedObject;
		}
		catch(Exception e)
		{
			print("[IO.load]Load "+fname+" failed: "+ e.getMessage() );
			return null;
		}
	}

	public static boolean save(String fname , Object o , Context cont)
	{
		try
		{
			if(o == null) print("WARNING: saving null");
			FileOutputStream fos = cont.openFileOutput(fname , Context.MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(o);
			oos.close();
			fos.close(); 
			return true;
		}
		catch(Exception e)
		{
			printe("[IO.save]Error saving object: ", e );
			return false;
		}
	}

	
	
	
	
	
	
	public static float cut(float m, float p)
	{
		return m - m%p;
	}
	
	public static float mod(float n, float m)
	{
		return (n < 0) ? (m - (Math.abs(n) % m)) % m : (n % m);
	}
	
	public static float min(float one, float two)
	{
		return (one < two) ? one : two;
	}

	public static float max(float one, float two)
	{
		return (one > two) ? one : two;
	}
	
	public static float max(float one, float two, float three)
	{
		return max(max(one, two), max(two, three));
	}
	
	public static int randInt(int min, int max)
	{
		return min + (int)(Math.random() * ((max - min) + 1));
	}
	
	public static float randFloat(float min, float max)
	{
		return min + (float)(Math.random() * ((max - min) + 1));
	}
	
	public static boolean isCloseEnough(float target, float cand, float error)
	{
		return isInBounds(cand, target - error, target+error, 0.0f, true); 
	}
	
	public static boolean isInBounds(float cand, float lo, float hi, float error, boolean canFlip)
	{
		if(!canFlip)
			return (cand >= (lo - error) && cand <= (hi + error) );
		else
			return ((cand >= lo - error && cand <= hi + error) || (cand >= hi - error && cand <= lo + error) );
	}
	
	public static boolean isInCircleBounds(float xTouch, float yTouch, float xTarget, float yTarget , int radTarget)
	{
		double dist = Math.sqrt( Math.pow(xTouch-xTarget , 2) + Math.pow(yTouch-yTarget, 2) );
		return (radTarget - dist > 0);
	}


	public static void sleep(int millis)
	{
		try{Thread.sleep(millis);}catch(Exception e){}
	}
	
	
	
	
	
	
	
	

	/******************TRIGONOMETRY***********************/
	
	public static float angleBetweenVecs(float vx, float vy, float vx2, float vy2)
	{
		float dotProduct = Core.dotProduct(vx, vy, vx2, vy2);
		float lenProduct = Core.vectorLen(vx, vy) * Core.vectorLen(vx2, vy2);		
		return (float)Math.toDegrees( Math.acos(dotProduct / lenProduct) );
	}

	public static void angForce_deg(float theta, float v_theta, float r, float[]out)
	{
		theta = (float)Math.toRadians(theta);
		v_theta = (float)Math.toRadians(v_theta);
		out[0] = (float)-Math.sin(theta) * v_theta * r;
		out[1] = (float)Math.cos(theta) * v_theta * r;
	}
	 
	
	
	
	
	
	
	
	
	
	/******************GEOMETRY***********************/
	
	public static float dist(double x, double y)
	{
		return dist(0,0,x,y);
	}
	
	public static float dist(double x1, double y1, double x2, double y2)
	{ 
		double a = x1-x2;
		double b = y1-y2;
		return (float)Math.sqrt( a*a + b*b);
	}
	
	public static void closestPoint(float Ax, float Ay, float Bx, float By, float Cx, float Cy, float[]out)
	{
		float tmin = - ( (Bx-Ax)*(Ax-Cx)+(By-Ay)*(Ay-Cy) ) / 
		( (Bx-Ax)*(Bx-Ax) + (By-Ay)*(By-Ay) );
		out[0] = tmin * (Bx-Ax) + Ax;
		out[1] = tmin * (By-Ay) + Ay;
		out[2] = tmin;
	}

	public static void intesect(float Ax, float Ay, float Bx, float By, float Cx, 
			float Cy, float Dx, float Dy, float[]out)
	{
		out[0] = ((Dx - Cx)*(Ay - Cy) - (Dy - Cy)*(Ax - Cx))
				/((Dy - Cy)*(Bx - Ax) - (Dx - Cx)*(By - Ay));
		
		out[1] = ((Bx - Ax)*(Ay - Cy) - (By - Ay)*(Ax - Cx))
				/((Dy - Cy)*(Bx - Ax) - (Dx - Cx)*(By - Ay));
	}
	
	public static void ortho_dist(float Ax, float Ay, float Bx, float By, float Cx, 
			float Cy, float Dx, float Dy, float h, float[]out)
	{
		float c = Dx - Cx, d = Dy - Cy, z = Bx - Ax, y = By - Ay, x=Cx-Ax, w=Cy-Ay;
		
		out[0] = (float)((c*c*w*y*y*y - c*d*x*y*y*y - c*d*w*y*y*z + d*d*x*y*y*z + 
			     c*c*w*y*z*z - c*d*x*y*z*z - c*d*w*z*z*z + d*d*x*z*z*z - 
			     Math.sqrt(h * (y*y + z*z) * Math.pow(c*d*y*y + c*c*y*z - d*d*y*z - c*d*z*z, 2)) )/
			     (Math.pow(c*y - d*z, 2)*(y*y + z*z))); 
 
		out[1] = (float)(((c*d*x*y*y*y) + c*d*w*y*y*z - c*x*y*y*z + d*d*x*y*y*z + 
				c*c*w*y*z*z -d*d*w*y*z*z + c*d*x*y*z*z - c*d*w*z*z*z - 
			     Math.sqrt(h * (y*z + z*z) * Math.pow(-(c*d*y*y) - c*c*y*z + d*d*y*z + c*d*z*z,2) ) )/
			     ((d*y + c*z)*Math.pow(c*y - d*z, 2)));
	}
	
	
	
	
	
	
	
	
	/******************VECTORS***********************/
	
	public static float vectorLen(float x, float y)
	{
		return (float)Math.sqrt( (x*x) + (y*y) ); 
	}

	public static float vectorLen2(float x, float y)
	{
		return (x*x) + (y*y); 
	}

	public static float dotProduct(float vx1, float vy1, float vx2, float vy2)
	{
		return vx1*vx2 + vy1*vy2;
	}
	
	public static void normalize(float[]v)
	{
		float norm = (float)Math.sqrt(v[0]*v[0] + v[1]*v[1]);
		v[0] = v[0] / norm;
		v[1] = v[1] / norm;
	}
	
	public static void proj(float vox, float voy, float vsx, float vsy, float[]in)
	{
		float mul = dotProduct(vsx, vsy, vox, voy) / vectorLen2(vsx, vsy);
		in[0] = vsx * mul;
		in[1] = vsy * mul;
	}
	
	public static void refl(float vox, float voy, double vsx, double vsy, float[]in)
	{
		proj(vox, voy, (float)vsx, (float)vsy, in);
		in[0] = 2*in[0] - vox;
		in[1] = 2*in[1] - voy;
	}
	
	
	
	
	
	
	
	
	/******************SINGLE VARIABLE POLY***********************/

	public static float polyEval(float a, float b, float c, float t)
	{
		return  0.5f*a*t*t + b*t + c;
	}

	public static void polySolve_t(float a, float b, float c, float d, float[]buf)
	{
		buf[0] = polySolve_t0(a,b,c,d);
		buf[1] = polySolve_t1(a,b,c,d);
	}

	public static float polySolve_t0(float a, float b, float c, float d)
	{
		a *= 0.5f;
		float delta = b*b - (4 * a * (c-d));
		return (float)(-b - Math.sqrt(delta)) / (2*a);
	}

	public static float polySolve_t1(float a, float b, float c, float d)
	{
		a *= 0.5f;
		float delta = b*b - (4 * a * (c-d));
		return (float)(-b + Math.sqrt(delta)) / (2*a);
	}
	
	public static float polySolve_a(float b, float c, float d, float t)
	{
		return -(2f*(c - d - b*t)) / (t*t);
	}

	public static float polySolve_b(float a, float c, float d, float t)
	{
		return (2*(d-c) - a*t*t) / (2*t);
	}

	public static float polySolve_c(float a, float b, float d, float t)
	{
		return d - (0.5f*a*t*t + b*t);
	}










	/******************TWO VARIABLE POLY***********************/

	public static void polySysSolve_bt(float a, float c, float d, float vf, float[]out)
	{
		out[0] = polySysSolve_b(a,c,d,vf);
		out[1] = polySysSolve_t(a,c,d,vf);
	}

	public static float polySysSolve_b(float a, float c, float d, float vf)
	{
		return (float)Math.sqrt(2*a*c - 2*a*d + vf*vf);
	}

	public static float polySysSolve_t(float a, float c, float d, float vf)
	{
		return (vf - polySysSolve_b(a,c,d,vf)) / (a);
	}
	
	public static void polyRunSysSolve_adj(float t_g, float t_a, float d_h, float[]out)
	{
		out[0] = (2 * d_h) / (2 * t_a * t_g + t_g*t_g);
		out[1] = (t_g * d_h) / (2 * t_a + t_g);
	}

	public static float vyForPeak(float d, float g)
	{
		return (float)Math.sqrt(-2*g*d);
	}

	







	/**************************CONSERVE SOLVE**************************/


	public static float cHitSolveK0(float m, float p, float a, float g, float k)
	{
		return (float)
		Math.sqrt((2*k + a*m + g*p - Math.sqrt(4*k*k - 
		Math.pow(g*m - a*p,2) + 4*k*(a*m + g*p)))/(a*a + g*g));
	}

	public static float cHitSolveK1(float m, float p, float a, float g, float k)
	{
		return (float)
		Math.sqrt((2*k + a*m + g*p + Math.sqrt(4*k*k - 
		Math.pow(g*m - a*p,2) + 4*k*(a*m + g*p)))/(a*a + g*g));
	}

	public static float cHitSolveMin(float m, float p, float a, float g)
	{
		return (float)(Math.pow(m*m + p*p, 1d/4d) / Math.pow(a*a + g*g, 1d/4d));
	}

	public static float cHitEvalMin(float m, float p, float a, float g)
	{
		float t = cHitSolveMin(m,p,a,g);
		return cHitEvalT(m,p,a,g,t);
	}

	public static float cHitEvalT(float m, float p, float a, float g, float t)
	{
		return (float)( Math.pow((m-a*t*t)/(2*t),2) + Math.pow((p-g*t*t)/(2*t),2) ); 
	}
	 
	public static float cHitEvalMinObstVx(float cx, float dx, float px, float ax, float t)
	{
		return (float) ((t*t * ax + 2*cx - 2*dx - Math.sqrt(Math.pow(t*t*ax + 2*cx - 2*dx,2) - 
				  8 * t*t * ax * (cx - px)) ) / (2*t*ax));	
	}
	 
	public static float cHitEvalMinObstVy(float cy, float dy, float py, float ay, float alpha)
	{
		return (float)((Math.sqrt(2) * Math.sqrt((alpha - 1) * cy - alpha * dy + py) )/
				Math.sqrt((alpha - 1) * alpha * ay));
	}
	
	public static float cHitEvalMinObst(float cx, float cy, float dx, float dy, float px, float py, float ax, float ay)
	{
		float alpha = cHitEvalMinObstVx(cx,dx,px,ax,200f) / 200f;
		float t = cHitEvalMinObstVy(cy,dy,py,ay,alpha);
		float mx = 2*(dx - cx);
		float my = 2*(dy - cy);
		return cHitEvalT(mx,my,ax,ay,t);
	}
	
	
	









	/**************************SAME BOUNCE SOLVE**************************/


	public static float sHitEvalTf(float m, float p, float a, float g, float u, float v, float t)
	{
		return (float)Math.atan2( -sHitEvalV(m,a,u,t) , sHitEvalV(p,g,v,t) );
	}

	public static float sHitEvalT0(float m, float p, float a, float g, float u, float v, float z, float r, float t)
	{
		float vx = sHitEvalV(m,a,u,t);
		float vy = sHitEvalV(p,g,v,t);
		return (float)Math.atan2(-vx, vy) - (vx*vx + vy*vy) / (2 * r*r * z);
	}
 
	public static float sHitSlopeT0(float y, float m, float p, float a, float g, float u, float v, float z, float r, float t)
	{
		return (sHitEvalT0(m,p,a,g,u,v,z,r,t+h) - y) / h;
	}

	public static float sHitSolveT0(float m, float p, float a, float g, float u, float v, float z, float r)
	{
		float y, s, b, t=10, i=0;
		do
		{
			if(i > 20) return Float.NaN;
			y = sHitEvalT0(m,p,a,g,u,v,z,r,t);
			s = sHitSlopeT0(y,m,p,a,g,u,v,z,r,t);
			b = y - s*t;
			t = (0 - b) / s;
			i++;
		}
		while(Math.abs(y) > 0.1);
		return t;
	}

	public static float sHitEvalV(float m, float a, float u, float t)
	{
		return (m - a*t*t) / (2*t) - u;
		
	}
	public static float sHitSolveV(float m, float a, float u)
	{
		return (float)((-u - Math.sqrt(a*m + u*u)) / (a));
	}


	
	
	
	
	
	
	
	
	
	
	
	/**********************************OPPOSITE BOUNCE SOLVE******************************/

	
	public static float oHitSlopeT0(float y, float m, float p, float a, float g, float ux, 
			float uy, float z, float r, float t)
	{
		return (oHitEvalT0(m,p,a,g,ux,uy,z,r,t+h) - y) / h;
	}
	
	public static float oHitEvalTf(float m, float p, float a, float g, float ux, float uy, float t)
	{
		return (float)Math.atan2( -oHitEvalNx(m,p,a,g,ux,uy,t) , oHitEvalNy(m,p,a,g,ux,uy,t) );
	}
	
	public static float oHitEvalT0(float m, float p, float a, float g, float ux, float uy, float z, float r, float t)
	{
		float nx = oHitEvalNx(m,p,a,g,ux,uy,t);
		float ny = oHitEvalNy(m,p,a,g,ux,uy,t);
		return (float)Math.atan2(-nx,ny) - (nx*nx + ny*ny)/(2 * r*r * z) - CHEAT;
	}

	public static float oHitSlopeNx(float y, float m, float p, float a, float g, float ux, float uy, float t)
	{
		return (oHitEvalNx(m,p,a,g,ux,uy,t+h) - y) / h;
	}

	public static float oHitSlopeNy(float y, float m, float p, float a, float g, float ux, float uy, float t)
	{
		return (oHitEvalNy(m,p,a,g,ux,uy,t+h) - y) / h;
	}

	public static float oHitEvalNx(float m, float p, float a, float g, float ux, float uy, float t)
	{
		return (float)(((m + t*(-a*t + 2*ux))*(m*m + p*p - 2*a*m*t*t - 2*g*p*t*t + 
		t*t*(a*a*t*t + g*g*t*t - 4*(ux*ux + uy*uy))))/(2*t*(m*m + p*p - 
		2*m*t*(a*t - 2*ux) - 2*p*t*(g*t - 2*uy) + 
		t*t*(a*a*t*t + g*g*t*t - 4*a*t*ux - 4*g*t*uy + 4*(ux*ux + uy*uy)))));
	}

	public static float oHitEvalNy(float m, float p, float a, float g, float ux, float uy, float t)
	{
		return (float)(((p + t*(-g*t + 2*uy))*(m*m + p*p - 2*a*m*t*t - 2*g*p*t*t + 
		t*t*(a*a*t*t + g*g*t*t - 4*(ux*ux + uy*uy))))/(2*t*(m*m + p*p - 
		2*m*t*(a*t - 2*ux) - 2*p*t*(g*t - 2*uy) + 
		t*t*(a*a*t*t + g*g*t*t - 4*a*t*ux - 4*g*t*uy + 4*(ux*ux + uy*uy)))));
	}

	public static float oHitSolveNy(float m, float p, float a, float g, float ux, float uy)
	{
		float t=20, s, bb, y;
		do
		{
			y = oHitEvalNy(m,p,a,g,ux,uy,t);
			s = oHitSlopeNy(y,m,p,a,g,ux,uy,t);
			bb = y - s*t;
			t = (0 - bb) / s;
		}
		while(Math.abs(y) > 0.1f);
		return t;
	}

	public static float oHitSolveNx(float m, float p, float a, float g, float ux, float uy)
	{
		float t=20, s, bb, y;
		do
		{
			y = oHitEvalNx(m,p,a,g,ux,uy,t);
			s = oHitSlopeNx(y,m,p,a,g,ux,uy,t);
			bb = y - s*t;
			t = (0 - bb) / s;
		}
		while(Math.abs(y) > 0.1f);
		return t;
	}

	
	public static float oHitSolveT0(float m, float p, float a, float g, float ux, float uy, float z, float r, float k)
	{
		int i = 0;
		float s,bb, y, t=20;
		do
		{ 
			if(i > 6) return Float.NaN;
			y = oHitEvalT0(m,p,a,g,ux,uy,z,r,t);
			s = oHitSlopeT0(y,m,p,a,g,ux,uy,z,r,t); 
			bb = y - s*t;
			t = (k - bb) / s;
			i++;
		}
		while(Math.abs(y) > 0.1f);
		return t;
	}

	
	
	

	
}







