package learning;

import plays.Play;
import actors.BallView;
import actors.Player;
import actors.PlayerView;


public abstract class Brain 
{
	protected enum State{Idle, Seeking, Executing, ObservHit, ObservMove, WaitComboHit};
	
	protected PlayerView foe;
	protected Player body;
	protected String name;
	protected BallView ball;
	protected float in[];
	
	protected State state;
	
	public Brain(Player body, PlayerView foe, BallView ball, String name)
	{
		this.body = body;
		this.foe = foe;
		this.ball = ball;
		this.name = "[" + ((body.dir==1) ? "Right" : "Left") +  name +"]";
		in = new float[24];
	}
	
	public abstract boolean think(Play play, float dt);
	
	public void setBasicState(boolean isReceiver)
	{
		if(isReceiver) this.state = State.Seeking;
		else state = State.ObservHit;
	}
	
}
