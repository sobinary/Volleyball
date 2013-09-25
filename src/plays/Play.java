package plays;


public class Play
{	
	private static final int MS = 16;
	public PlayType type;
	public Move[]moves;
	public int n, moveCount, doneSum;	  

	public Play()
	{			
		n  = moveCount = doneSum = 0;
		moves = new Move[MS];
		type = PlayType.UND; 			

		for(int i=0; i < MS; i++) 
			moves[i] = new Move();
		
		clear();
	}

	public Move addMove()
	{
		moveCount++;
		return moves[moveCount-1];
	}
	
	public boolean isDone()
	{
		return doneSum == moveCount;
	}
	
	public void clear()
	{
		n = 0;
		doneSum = 0;
		moveCount = 0;
		type = PlayType.UND;
		
		for(Move m: moves) m.clear();
	}
	
	public void refreshHard()
	{
		n = 0;
		doneSum = 0;
		for(Move m: moves) m.clear();
	}
	
	public void refreshLight()
	{
		n = 0;
		doneSum = 0;
		for(Move m: moves) m.refresh();
	}
	
	public String movesToString()
	{
		String res = "\n***Moves****";
		for(int i=0; i < moveCount; i++)
			res += "\n"+moves[i].toString();
		res += "\n******";
		return res;
	}

	public static boolean isAggressive(PlayType play)
	{
		switch(play)
		{
			case UND: return false;
			case Casual: return false;
			case Main: return false;
			default: return true;
		}
	}
	
	@Override
	public String toString()
	{
		return "Type: "+type+"\nMoves: "+moveCount+"\nDone: "+doneSum+ movesToString();
	}
}
