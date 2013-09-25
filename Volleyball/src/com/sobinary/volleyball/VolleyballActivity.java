package com.sobinary.volleyball;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;


//bruce anderson: baplnr@comcast.net

//morgan.burks@gmail.com

public class VolleyballActivity extends Activity
{
	public static final int PORT = 9003;
	
	private Engine engine;
	private GLSurfaceView glView;  

	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Display d = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        
        engine = new Engine(this, d.getWidth(), d.getHeight() );
        glView = new GLSurfaceView(this);    
        
        glView.setOnTouchListener(engine);
        glView.setRenderer(engine);
        setContentView(glView);        
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    	glView.onResume();
    }
    
    @Override
    public void onPause()
    {
    	super.onPause();
    	glView.onPause();
    	engine.xPause();
    }    
    
    @Override
    public void onStop()
    {
    	super.onStop();
    	engine.xStop();
    }
}
    
    
    

    
    
    