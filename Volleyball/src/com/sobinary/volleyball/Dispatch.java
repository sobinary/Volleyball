package com.sobinary.volleyball;


import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;



public class Dispatch 
{
	private static final int QUEUE_SIZE = 5;
	private static final int MAX_REQS = 4;
	private static final int MAX_ARGS = 5;
	
	private static Dispatch me;
	private ServerSocket ss;
	private Socket controller;

	private ArrayList<Object>actors;
	private int n[], activeCount;
	private boolean isConnected;
	private String[][][]pendingReqs;
	
	private Dispatch()
	{
		me = this;
		actors = new ArrayList<Object>();
		n = new int[QUEUE_SIZE];
		pendingReqs = new String[QUEUE_SIZE][MAX_REQS][MAX_ARGS];
		Arrays.fill(n, -1);
	}

	public Dispatch(Object...obs)
	{
		this();
		actors.addAll(Arrays.asList(obs));
	}
	
	public void addActors(Object...obs)
	{
		actors.addAll(Arrays.asList(obs));
	}

	private Object getTarget(String name)
	{
		for(Object ob : actors)
			if(ob.toString().equals(name))
				return ob;
		return null;
	}

	public void update(boolean paused)
	{
		if(activeCount == 0) return;
		
		for(int i=0; i < QUEUE_SIZE; i++)
		{
			n[i]--;
			if(n[i] == 0)
			{
				request(pendingReqs[i]);
				n[i] = -1;
				activeCount--;
			}
		}
	}
	
	public static Dispatch me()
	{
		return me;
	}
	
	
	
	
	
	
	
	
	
	
	
	/*****************************REQUESTS***********************************/
	
	public static Class<?>inferType(String raw)
	{
		if(raw.matches("-?[0-9]+")) return Integer.TYPE;
		if(raw.matches("-?[0-9]+.?[0-9]*f?")) return Float.TYPE;
		if(raw.equals("true") || raw.equals("false")) return Boolean.TYPE;
		return null;
	}
	
	public static Object cast(Class<?>type, String raw) 
	{
		try
		{
			if(type.equals(Float.TYPE)) return Float.parseFloat(raw);
			if(type.equals(Integer.TYPE)) return Integer.parseInt(raw);
			if(type.equals(Boolean.TYPE)) return Boolean.parseBoolean(raw);
			if(type.equals(String.class)) return raw;
		}
		catch(Exception e)
		{
			Core.print("Error casting: " + e.getMessage());
		}
		return null;
	}
	
	private int getFreeN()
	{
		for(int i=0; i < QUEUE_SIZE; i++)
			if(n[i] < 0)
				return i;
		return -1;
	}
	
	public void futureRequest(float n, String[]...cmds)
	{
		int ii = getFreeN();
		if(ii < 0) return;
		
		this.n[ii] = (int)n;
		pendingReqs[ii] = Arrays.copyOf(cmds, cmds.length);
		activeCount++;
	}
	
	private void request(String[][]cmds)
	{
		for(String[]cmd : cmds)
			request(cmd);
	}
	
	public String request(String[]cmd)
	{
		Object target = getTarget(cmd[0]);
		
		if(target == null)
		{
			Core.print("[Disp]Bad target: " + cmd[0]);
			return null;
	 	}
		return request(Arrays.copyOfRange(cmd, 1, cmd.length), target);
	}
	
	public String request(String[]cmd, Object targ) 
	{
		if(cmd[0].equals("get"))
			return getReq(Arrays.copyOfRange(cmd, 1, cmd.length), targ);

		else if(cmd[0].equals("set"))
			return setReq(Arrays.copyOfRange(cmd, 1, cmd.length), targ);
		 
		else 
			return funcReq(cmd, targ);
	}
	
	private String funcReq(String[]cmd, Object targ)
	{
		try
		{
			Class<?> infTypes[] = new Class<?>[cmd.length - 1];

			for(int i=0; i < infTypes.length; i++)
			{
				infTypes[i] = inferType(cmd[i+1]);
				if(infTypes[i] == null) throw new Exception("Infering: " + cmd[i+1]);
			}
			  
			Method m = targ.getClass().getDeclaredMethod(cmd[0], infTypes);
			Class<?> reqTypes[] = m.getParameterTypes();
			if(reqTypes.length != cmd.length - 1) throw new Exception("Bad arg count for " + cmd[0]);
			Object args[] = new Object[reqTypes.length];
			
			for(int i=0; i < reqTypes.length; i++)
			{
				args[i] = cast(reqTypes[i], cmd[i+1]);
				if(args[i] == null) throw new Exception("Casting: " + cmd[i+1]);
			}
			
			m.setAccessible(true);
			Object res = m.invoke(targ, args);
			return res == null ? null : res.toString();
		}
		catch(Exception e)
		{
			Core.print("Invokation Error: " + e.getClass().getCanonicalName() + ": " + e.getMessage());
			return null;
		}
		
	}
	
	private String getReq(String[]cmd, Object targ)
	{
		try 
		{
			Field field = targ.getClass().getDeclaredField(cmd[0]);
			field.setAccessible(true);
			return field.get(targ).toString();
		}
		catch (Exception e) {
			Core.print("[DispGet]: " + e.getMessage());
			return null;
		}
	}
	
	public String setReq(String[]cmd, Object targ)
	{
		try 
		{
			Field field = targ.getClass().getDeclaredField(cmd[0]);
			field.setAccessible(true);
			
			if(field.getType().isAssignableFrom(float.class)) 
				field.setFloat(targ, Float.parseFloat(cmd[1]));
				
			else if(field.getType().isAssignableFrom(int.class)) 
				field.setInt(targ, Integer.parseInt(cmd[1]) );

			else if(field.getType().isAssignableFrom(boolean.class)) 
				field.setBoolean(targ, Boolean.parseBoolean(cmd[1]) );
			
			else if (field.getType().isAssignableFrom(String.class)) 
				field.set(targ, cmd[1] );
							
			return "success";
		}
		catch (Exception e) 
		{
			Core.print("[DispatchError]: " + e.getMessage());
			return "fail";
		}
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*****************************REMOTE******************************/
	
	public void endListen()
	{
		HUD.me().setConnectOff();
		isConnected = false;
		try 
		{
			if(ss.isBound())ss.close();
			if(controller.isBound())controller.close();
		} 
		catch (Exception e) 
		{
		}
	}  

	public boolean isConnected()
	{
		return this.isConnected;
	}
	
	public void listen()
	{  
		new Thread()
		{
			@Override
			public void run()
			{
				try 
				{  
					HUD.me().setConnectMid();
					ss = new ServerSocket(VolleyballActivity.PORT);
					ss.setSoTimeout(5000);
					controller = ss.accept();
					InputStream in = controller.getInputStream();
					HUD.me().setConnectOn();
					isConnected = true;
					
					while(true)
					{
						byte[]b = new byte[512];
						int s = in.read(b);
						 
						if(s < 0)
						{ 
							endListen(); 
							return; 
						}
						
						String[]cmd = new String(b, 0, s).split(" ");
						String last = cmd[cmd.length-1];
						cmd = Arrays.copyOf(cmd, cmd.length - 1);
						String res = Dispatch.this.request(cmd);
						
						if(last.equals("y"))
						{
							OutputStream out = controller.getOutputStream();
							res = res == null ? "null" : res;
							out.write(res.getBytes());
							out.flush();
						}
					}
				}
				catch (Exception e) 
				{
					Core.print("Run conn ex: " +e.getClass().getCanonicalName() +" : " + e.getMessage());
					endListen();
					return;
				}
			}
		}.start();
	}

	
}
