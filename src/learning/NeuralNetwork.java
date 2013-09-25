package learning;


import java.io.Serializable;
import java.util.Arrays;

import com.sobinary.volleyball.Core;


public class NeuralNetwork implements Serializable, LearningMachine
{
	public enum Defer{Now, Later, Never}
	
	private static final long serialVersionUID = 7973729346552487390L;
	private static final int DEFED_MAX = 30;
	public static final float DEF_L_RATE = 0.65f;
	public static final float DEF_STEEPNESS = 1f;
	
	private final int input_c, output_c, hidden_c, l1_c, l2_c;
	public final float l_weights[], r_weights[];
	public float lRate, steep;
	
	private transient float defed_in[][], defed_obs[][];
	private transient int defed_ind, time_its;
	public transient long backPassTime;
	private transient final float hD[], oD[], h[], o[];

	
	public NeuralNetwork(int inputs, int hidden, int outputs, float[]l, float[]r)
	{
		this.steep = DEF_STEEPNESS;
		this.lRate = DEF_L_RATE;
		this.time_its = 0;
		
		this.input_c = inputs;
		this.hidden_c = hidden;
		this.output_c = outputs;
		
		this.l1_c = inputs * hidden;
		this.l2_c = outputs * hidden;
		
		this.l_weights = (l == null) ? new float[l1_c] : l;
		this.r_weights = (r == null) ? new float[l2_c] : r;
		
		if(l==null)for(int i=0; i < l1_c; i++) l_weights[i] = Core.randFloat(-0.1f, 0.1f);
		if(r==null)for(int i=0; i < l2_c; i++) r_weights[i] = Core.randFloat(-0.1f, 0.1f);
		
		this.h = new float[hidden_c];
		this.o = new float[output_c];
		
		this.hD = new float[hidden_c];
		this.oD = new float[output_c];
	}
	
	public NeuralNetwork(int inputs, int hidden, int outputs)
	{
		this(inputs, hidden, outputs, null, null);
	}
	
	public void evalMany(float[][]in, float[][]out_res, float[][]hid_res)
	{
		for(int i=0; i < in.length; i++)
			evalOne(in[i], out_res[i], hid_res[i]);
	}

	
	public void evalOne(float[]in, float[]out_res)
	{
		this.evalOne(in, out_res, h);
	}
	
	private void evalOne(float[]in, float[]out_res, float[]hid_res)
	{
		float sum;
		for(int i=0; i < hidden_c; i++)
		{
			sum = 0;
			for(int j=0; j < input_c; j++)
				sum += l_weights[j*hidden_c + i] * in[j];
			hid_res[i] = sigmoid(sum);			 
		}

		for(int i=0; i < output_c; i++)
		{
			sum = 0;
			 for(int j=0; j < hidden_c; j++)
				 sum += r_weights[j*output_c + i] * hid_res[j];
			 out_res[i] = sigmoid(sum);
		}
	}

	public void addObservation(float[]obs)
	{
		if(defed_ind < DEFED_MAX)
		{
			this.defed_obs[defed_ind] = Arrays.copyOf(obs, output_c);
			this.defed_ind++;
		}
		else Core.print("Deferred buffer full");
	}

	public void learnDeferred(int its)
	{
		if(defed_in.length != defed_obs.length){
			Core.print("[LearnDeferred]Length mismatch: " + defed_in.length+" != " + defed_obs);
			return;
		}
		learnSet(defed_in, defed_obs, its);
	}

	private void learnOne(float[]in, float[]ob)
	{
		float kSum;
		int r_ind, l_ind;
		
		evalOne(in, o, h);
		
		for(int k=0; k < output_c; k++) //output error
			oD[k] = o[k] * (1-o[k]) * (ob[k] - o[k]);

		Core.print("Output err: " + Arrays.toString(oD));
		for(int j=0; j < hidden_c; j++) //hidden error
		{
			kSum = 0;
			for(int k=0; k < output_c; k++)
				kSum += r_weights[j*output_c + k] * oD[k];
			hD[j] = h[j] * (1-h[j]) * kSum;
		}

		for(int j=0; j < l2_c; j++) //w(hidden -> output)
		{
			l_ind = j / output_c;
			r_ind = j % output_c;
			r_weights[j] += lRate * oD[r_ind] * h[l_ind];
		}

		for(int j=0; j < l1_c; j++) //w(in -> hidden)
		{
			l_ind = j / hidden_c;
			r_ind = j % hidden_c;
			l_weights[j] += lRate * hD[r_ind] * in[l_ind];
		}
	}
	
	public void learnSet(float[][]ins, float[][]obs, int its) 
	{
		if(!checkDims(ins, obs)) return;
		
		for(int j=0; j < its; j++)
			for(int i=0; i < ins.length; i++)
				learnOne(ins[i], obs[i]);
	}
	
	
	
	
	
	
	
	
	/*******************************UTILS***********************************/
	
	private boolean checkDims(float[][]ins, float[][]obs)
	{
		if(ins.length != obs.length){Core.print("MM1"); return false;}
		if(ins[0].length != input_c){Core.print("MM2"); return false;}
		if(obs[0].length != output_c){Core.print("MM3"); return false;}
		return true;
	}
	
	private float sigmoid(float p)
	{
		return (float)(1 / (1+Math.pow(Math.E, -(p*steep))));
	}
	
	public void printWeights()
	{
		int w_ind;
		for(int i=0; i < input_c; i++)
		{
			for(int j=0; j < hidden_c; j++)
			{
				w_ind = i*hidden_c + j;
				Core.print("In["+i+"] --> w["+w_ind+"]="+l_weights[w_ind]+" --> Hid["+(j%hidden_c)+"]");
			}
		}
		
		for(int i=0; i < hidden_c; i++)
		{
			for(int j=0; j < output_c; j++)
			{
				w_ind = i*output_c + j;
				Core.print("Hid["+i+"] --> w["+w_ind+"]="+r_weights[w_ind]+" --> Out["+(j%output_c)+"]");
			}
		}
	}

	public void printStruct()
	{
		Core.print("l_weights: " + Arrays.toString(l_weights));
		Core.print("r_weights: " + Arrays.toString(r_weights));
		System.out.printf("NodeDims: %d x %d x %d\n", input_c, hidden_c, output_c);
		System.out.printf("WeightDims: %d x %d\n", l1_c, l2_c);
	}
	
	void calcTime(long start)
	{
		long tmp = System.currentTimeMillis() - start;
		backPassTime = (long)( ((float)(time_its * backPassTime) + tmp) / (float)(time_its+1));
		time_its++;
	}
}
