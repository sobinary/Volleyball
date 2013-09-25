package learning;

import plays.Play;
import actors.BallView;
import actors.Player;
import actors.PlayerView;
import android.content.Context;

import com.sobinary.volleyball.Core;




public class NeuralBrain extends Brain 
{
	enum State {nil, inc, out};
	
	NeuralNetwork att1_net, start_att, def1, def2, att1;
	Context cont;
	State oState, nState;
	float t1;
	float att1_ibuf[], att1_obuf[];
	boolean lastBallVis; 
	
	
	
	public NeuralBrain(Context cont, Player body, PlayerView foe, BallView ball) 
	{
		super(body, foe, ball, "Neural");
		
		this.cont = cont;
		this.oState = State.nil;
		this.nState = State.nil;
		
		this.att1_net = new NeuralNetwork(2, 2, 1);
		this.att1_ibuf = new float[2];
		this.att1_obuf = new float[1];
	}
	
	@Override
	public boolean think(Play play, float dTime) 
	{
		if(!ball.update()) nState = State.nil;
		else if(ball.vX > 0) nState = State.inc;
		else nState = State.out;

		if(oState != nState)
		{
			Core.print(name+"State Transition");
			if(nState == State.inc)incomingStart();
			else if(nState == State.out)outgoingStart();
		}
		else
		{
			
		}
		
		oState = nState;
		return false;
	}
	
	private void incomingStart()
	{
		Core.print(name+"My turn start");
	}
	
	private void outgoingStart()
	{
		Core.print(name+"His turn start");
	}
	
}
