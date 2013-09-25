package plays;


public final class Move
{
	public static final int MAX_REQS = 5;
	public MoveType type = MoveType.UND;
	public boolean active = true;
	public int delay, n, nReq;
	public int dur = Integer.MIN_VALUE;
	public float arg, arg2;
	public String[][]reqs;
	
	public Move()
	{
		reqs = new String[MAX_REQS][2];
	}
	
	public void addRequest(String name, double val)
	{
		addRequest(name, (float)val+"");
	}
	
	public void addRequest(String name, String val)
	{
		reqs[nReq][0] = name;
		reqs[nReq][1] = val;
		nReq++;
	}
	
	public void clear()
	{
		nReq = 0;
		n = 0;
		delay = 0;
		dur = 0;
		arg = 0f;
		arg2 = 0f;
		active = true;
	}
	
	public void refresh()
	{
		nReq = 0;
		n = 0;
		active = true;
	}
	
	@Override
	public String toString()
	{
		return "[Move]"+type+"{Del="+delay+", Dur="+dur+", Arg="+arg+"}";
		
	}
}