package learning;

import java.util.Arrays;

import plays.Play;

import actors.Ball;
import actors.BallView;
import actors.Court;
import actors.Player;
import actors.PlayerView;
import actors.WorldView;

import com.sobinary.volleyball.Core;


public class BasicBrain extends Brain
{
	public BasicBrain(Player body, PlayerView foe, BallView ball, String name) 
	{
		super(body, foe, ball, name);
		this.state = State.Idle;
	}
	
	@Override
	public boolean think(Play play, float dt)
	{
		return quickState(play, dt);
//		return quickThink(play, dt);
	}

	boolean quickThink(Play play, float dt)
	{
		if(play != null)
		{
			Arrays.fill(in, 0);
			ball.update();
			return all(play, dt);
		}
		return false;
	}
	
	boolean quickState(Play play, float dt)
	{
		switch(state)
		{
			case Idle: 
				return false;
		
			case Seeking: 
				ball.update();
				Arrays.fill(in, 0);
				if( all(play, dt) ) 
				{
					state = State.Executing;
					return true;
				}
				return false;
				
			case Executing:
				if(body.isExecuting() == false) state = State.ObservHit;
				return false;
				
			case ObservHit:
				if(WorldView.me().isColliding(foe))
				{
					foe.update();
					if(foe.y > 1) state = State.Seeking;
					else state = State.ObservMove;
				}
				return false;

			case ObservMove: 
				foe.update();
				if(foe.vX == 0) state = State.Seeking;
				else state = State.ObservHit;
				return false;
				
			case WaitComboHit:
				if(WorldView.me().isColliding(foe)) state = State.Seeking; 
				return false;
				
			default: 
				return false;
		}
	}

	
	
	
	
	
	
	
	
	
	
	  
	/********************************API CALLS*********************************/
	
	boolean all(Play play, float dt)
	{
		if(ball.vX < 0 || ball.vX == 0) return false;
		
		if(air(play, dt)) return true;
		if(comu(play, dt)) return true;  
		if(comp(play, dt)) return true;
		if(gro(play, dt)) return true;
		
		return false;
	}
	
	boolean gro(Play play, float dt)
	{
		in[0] = 200;
		in[1] = Ball.RAD;
		in[2] = (float)Math.random();
		in[3] = 0.1f;
		return PhysReq.groBump(play, body, ball, in, dt);
	}
	 
	boolean air(Play play, float dt)
	{
		in[0] = 0.5f;
		in[1] = 0.8f;
		in[2] = 0.8f;
		return PhysReq.airSmash(play, body, ball, in, dt);
	}
	
	boolean comp(Play play, float dt)
	{
		in[0] = Core.randFloat(0, Court.W/2);
		in[1] = Ball.RAD;
		in[2] = 0.6f;
		in[3] = 0.4f;
		in[4] = 0.9f;
		in[5] = 0.9f;
		return PhysReq.comboBump(play, body, ball, in, dt);
	}
	 
	boolean comu(Play play, float dt)
	{
		in[0] = (float)Math.random();
		in[1] = 0.7f;
		in[2] = 0.6f;
		in[3] = 0.9f;
		in[4] = 0.6f;
		return PhysReq.comboSmash(play, body, ball, in, dt);
	}
}






