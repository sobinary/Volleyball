package learning;

import plays.Move;
import plays.MoveType;
import plays.Play;
import plays.PlayType;

import actors.Ball;
import actors.BallView;
import actors.Court;
import actors.Player;

import com.sobinary.volleyball.Core;
import com.sobinary.volleyball.Dispatch;
import com.sobinary.volleyball.HUD;


public class PhysReq   
{
	private static float GRO_BALL_Y;
	private static float GRO_TORS_Y;
	private static final float R_FRAC = 0.92f;
	private static final float R = R_FRAC * Player.TORSO_H;
	private static final float RHIT = 28;
	private static final float SAFE_T = 10;
	private static final float HIT_DIST = Ball.RAD + Player.TORSO_W/2;
	private static final int TAR_COUNT = 8;
	
	
	
	
	public static void init(Object...os)
	{
		for(Object o : os) HUD.me().lockTars(o, TAR_COUNT);
		for(Object o : os) HUD.me().lockLines(o, 2);

		GRO_TORS_Y = Player.LEG_H * 2;
		GRO_BALL_Y = (float) (R * Math.sin(Player.T_RAD_MIN)) + GRO_TORS_Y;
		GRO_BALL_Y+= (float)Math.sin(Player.T_RAD_MIN + Math.PI/2) * HIT_DIST;
	}
	
	private static void xTorHit(float bx, float by, float theta, float in[])
	{
		in[0] = (float)(Math.cos(theta + 3*Math.PI/2) * HIT_DIST + bx);
		in[1] = (float)(Math.sin(theta + 3*Math.PI/2) * HIT_DIST + by);
		in[0] = (float)(Math.cos(theta + Math.PI) * R + in[0]);
		in[1] = (float)(Math.sin(theta + Math.PI) * R + in[1]);	
	}
	 
	private static void groundPos(float theta, float ball_x, float in[], float dt)
	{
		xTorHit(ball_x, GRO_BALL_Y, theta, in);
		theta = (float)Math.asin(in[1] / GRO_TORS_Y);
		in[2] = (float)Math.toDegrees(Math.PI/2 - theta);
	}
  
	private static boolean runJump(float x, float xTo, float h, float dur_max, float lDeg0, float[]in, float dt)
	{
		float groAcc, airDur, groRunDur, dHit, dJmp, vxAir,
		lDeg, lfDur, lrDur, groRunRem;
  		
		dHit = (float)Math.abs(xTo - x);
		airDur = Core.polySysSolve_t(Player.G*dt, 0, h, 0);
		groRunDur = dur_max - airDur;

		if(groRunDur < 0) 
			return fail("[RunJump]Jump time too short("+(-groRunDur)+"), abort");
	
		Core.polyRunSysSolve_adj(groRunDur, airDur, dHit, in);
		groAcc = in[0];
		dJmp = in[1]; 
		vxAir = (dHit - dJmp) / airDur;
		 
		if(h > Player.J_MAX_H) 
			return fail("[RunJump]Target height too high, abort");
		
		lDeg = Player.degForJump(h, dt); 
		lfDur = Player.tLegFlexDeg(Math.abs(lDeg-lDeg0), dt);
		lrDur = Player.tLegRelDeg(lDeg, dt);
		groRunRem = groRunDur - (lfDur + lrDur);
		
		if(groRunRem < 0) 
			return fail("[RunJump]Flex/Rel time over by" + (-groRunRem) +" , abort");
		
		in[0] = groRunDur;
		in[1] = airDur;
		in[2] = groAcc * Math.signum(xTo - x) / dt;
		in[3] = vxAir * Math.signum(xTo - x) / dt;
		in[4] = lDeg;
		in[5] = lfDur;
		in[6] = lrDur;
		in[7] = airDur;
		return true;
	}
	  
	private static boolean runStay(float m, float t_max, float speed, float[]in, float dt)
	{
		float run_dur, run_vel, run_velMin, run_velMax;
		
		run_velMin = m / (t_max - SAFE_T);
		run_velMax = Player.SIDE_V * dt * Math.signum(m);
		if(Math.abs(run_velMin) > Math.abs(run_velMax)) 
			return fail("[RunStay]Req speed too high, abort");
		 
		run_vel = run_velMin + speed*(run_velMax - run_velMin);
		run_dur = m / run_vel;

		in[0] = run_vel;
		in[1] = run_dur;
		return true;
	}
	
	private static boolean isGoodGroDest(BallView b, float xHit, float yHit, float ax, float ay, float dt)
	{
		if(xHit < Court.W/2 + 15 || xHit > Court.W - 15) return fail("[GroPut]Local dest out of bounds 1");
		
//		float tNet = Core.polySolve_t1(ax, b.vX*dt, b.x, Court.W/2);
//		float yNet = Core.polyEval(ay, b.vY*dt, b.y, tNet);
//		if(yNet < Court.NETY_LO) return fail("[GroPut]Local dest out of bounds 2");;
		
		return true;
	}
	 
	/**
	 * 
	 * @param in[0] X aim
	 * @param in[1] Y aim
	 * @param in[2] run up velocity. 0 is slowest, 1 is fastest
	 * @param in[3] energy conservation ratio, 0 is use minimum energy, 1 is full 
	 * @return true if the play could be constructed. Side effect: constructed play
	 */
	public static boolean groBump(Play play, Player pl, BallView b, float in[], float dt)
	{
		float xAim, yAim, xHit, yHit, ux, uy, k, tHitG, mx, my, t, d, kMin, kMax, px, py,
		vx, vy, theta, xCalves, run_vel, run_dur, speed, lDeg, yCalves, g, a, force, kMin2;
		
		xAim = in[0]; 
		yAim = in[1]; 
		speed = in[2];
		force = in[3];
		
		g = Ball.G * dt;
		a = -Ball.A * dt;
		
		tHitG = Core.polySolve_t0(g, b.vY*dt, b.y, GRO_BALL_Y);
		if(Float.isNaN(tHitG)) return fail("[Ground]Hit unreachable, abort");

		xHit = Core.polyEval(a, b.vX*dt, b.x, tHitG);
		yHit = GRO_BALL_Y;
		ux = (b.vX*dt) + (tHitG * a); 
		uy = (b.vY*dt) + (tHitG * g);
		if(!isGoodGroDest(b,xHit,yHit,a,g,dt)) return false;
		
		mx = 2*(xAim - xHit); 
		my = 2*(yAim - yHit);
		px = Court.W / 2;
		py = Court.NETY_LO + Ball.RAD + 10;
		
		a = Ball.A * dt;
		kMin = Core.cHitEvalMin(mx,my,a,g);
		kMin2 = Core.cHitEvalMinObst(xHit, yHit, xAim, yAim, px, py, a, g);
		kMin2 = (in[in.length-1] == 0) ? kMin2 : kMin; 
		kMin = Core.max(kMin, kMin2);
		kMax = Core.vectorLen2(ux, uy);
		if(kMin > kMax) return fail("[GroPut]Target dest unreachable: kMin > kMax");
		
		k = kMin + force * (kMax - kMin); 
		d = k / kMax;
		
		t = Core.cHitSolveK1(mx,my,a,g,k);
		if(Float.isNaN(t)) return fail("[GroPut]Target dest unreachable: t is NaN");

		vx = Core.polySolve_b(a, xHit, xAim, t);
		vy = Core.polySolve_b(g, yHit, yAim, t);
		ux = (float)Math.sqrt(d) * ux;
		uy = (float)Math.sqrt(d) * uy;
		
		theta = (float)Math.atan2(-(vx-ux), vy-uy);
 		
		groundPos(theta, xHit, in, dt);
		xCalves = in[0];
		yCalves = in[1];
		lDeg = in[2];
				
		if(!runStay(xCalves - pl.xCalves, tHitG, speed, in, dt)) return false;
		run_vel = in[0];
		run_dur = in[1];
		
		play.clear();
		play.type = PlayType.GroBump;
		
		Move tflex = play.addMove();
		tflex.type = MoveType.TorsoFlex;
		tflex.dur = Player.tTorFlexRad(theta, dt);
		tflex.delay = 0;
		tflex.addRequest("tAngle", Math.toDegrees(theta));
		tflex.addRequest("absorb", d);
		 
		Move lflex = play.addMove();
		lflex.type = MoveType.LegFlex;
		lflex.dur = Player.tLegFlexDeg(lDeg, dt);
		lflex.delay = 0;
		lflex.addRequest("lAngle", lDeg + 90);
		
		Move run_g = play.addMove();
		run_g.type = MoveType.SideVel;
		run_g.arg = run_vel / dt;
		run_g.dur = (int)run_dur;
		run_g.delay = 0;
		run_g.addRequest("xCalves", xCalves+"");
		run_g.addRequest("vX", 0);
	
		Move wait = play.addMove();
		wait.type = MoveType.UND;
		wait.delay = (int)(tHitG + 1);
		
		int l1 = HUD.me().findTarLock(pl);
		
		HUD.me().setTar(l1+0, xAim, yAim, pl.dir);
		HUD.me().setTar(l1+1, xCalves, yCalves, pl.dir);
		HUD.me().setTar(l1+2, xHit, yHit, pl.dir);
		
		if(in[in.length-1] == 0) cleanHUD(pl, t + tHitG);
		
		in[0] = t;
		in[1] = theta;
		in[2] = lDeg;
		in[3] = tHitG;
		in[4] = xCalves;
		in[5] = vx + (t * a);
		in[6] = vy + (t * g);
		in[7] = l1;
		in[8] = 3;
		
		return true;
	}

	private static float coneStart(float xHit, float yHit)
	{
		float m = (yHit - (Court.NETY_LO+Ball.RAD)) / (xHit - Court.W/2); 
		float b = yHit - (m * xHit);
		return (Ball.RAD - b) / m;
	}
	
	/**
	 * 
	 * @param in[0] X destination
	 * @param in[1] Y destination
	 * @param in[2] net proximity ratio, 0 is farthest from net, 1 is closest
	 * @param in[3] hit force ratio, 0 is minimum hit force, 1 is maximum  
	 * @return true if the play could be constructed, and constructed play
	 */
	public static boolean airSmash(Play play, Player pl, BallView b, float[]in, float dt)
	{
		float xAim, yAim, xHit, yHit, ux, uy, tHit, m, p, omega, alpha, t, a, dHit, xMin,
		thetaf, force, g, tMin, tMax, initiative, trDur, tfDur, r, z, theta0, vT, zeta;
		
		dHit = in[0];
		initiative = in[1];
		force = in[2];
		g = Ball.G * dt;
		a = -Ball.A * dt;

		tMin = Core.polySolve_t1(a, b.vX*dt, b.x, Court.W/2 + 15);
		tMax = Core.polySolve_t0(g, b.vY*dt, b.y, Court.NETY_LO);
		if(Float.isNaN(tMin) || Float.isNaN(tMax)) return fail("[AirSmash]Local dest unreachable");
		tHit = (1 - initiative) * (tMax-tMin) + tMin;
		if(tMin > tMax) return fail("[AirSmash]Bad incoming motion");
		
		xHit = Core.polyEval(a, b.vX*dt, b.x, tHit);
		yHit = Core.polyEval(g, b.vY*dt, b.y, tHit);  
 
		xMin = coneStart(xHit, yHit);
		xAim =  (1 - dHit) * (xMin - 0) + 0;
		yAim = Ball.RAD;
		
		ux = -((b.vX * dt) + (tHit * a)); 
		uy = -((b.vY * dt) + (tHit * g));
		r = RHIT;
		z = Player.TR_RAD_ACC * dt;
		m = 2*(xAim - xHit); 
		p = 2*(yAim - yHit);
		a = -a;
		  
		alpha = Core.oHitSolveT0(m,p,a,g,ux,uy,z,r,0);
		omega = Core.oHitSolveNy(m,p,a,g,ux,uy);
		zeta = Core.oHitSolveNx(m,p,a,g,ux,uy);
		if(Float.isNaN(zeta)) zeta = omega;
		omega = Core.min(omega, zeta);
		t = (1-force) * (omega-alpha) + alpha;

		thetaf = Core.oHitEvalTf(m,p,a,g,ux,uy,t);
		theta0 = Core.oHitEvalT0(m,p,a,g,ux,uy,z,r,t);
		
		if(theta0 > (float)Math.PI/2)
			return fail("[ComboPush]Theta0 > PI/2: " + theta0);
		
		if(!isGoodOppHit(alpha, omega, theta0, thetaf)) return false;
		
		trDur = Core.polySolve_t1(z, 0, theta0, thetaf);
		tfDur = Player.tTorFlexRad(theta0, dt);
		vT = z * trDur;
		
		float xAir, yAir, gro_run_dur, gro_acc, vx_air, lDeg, lf_dur, 
		lr_dur, yAir2, air_dur;
		
		xTorHit(xHit, yHit, thetaf, in);
		xAir = in[0];
		yAir2 = in[1];
		yAir = yAir2 - Player.LEG_H * 2;
		
		if( !runJump(pl.xCalves, xAir, yAir, tHit, 0, in, dt)) return false;
		gro_run_dur = in[0];
		gro_acc = in[2];
		vx_air = in[3];  
		lDeg = in[4];
		lf_dur = in[5];
		lr_dur = in[6];
		air_dur = in[7];
		  
		play.clear();
		play.type = PlayType.AirSmash;
		
		Move tflex = play.addMove();
		tflex.type = MoveType.TorsoFlex;
		tflex.dur = (int)tfDur;
		tflex.delay = 0;
		tflex.addRequest("tAngle", Math.toDegrees(theta0));

		Move lflex = play.addMove();
		lflex.type = MoveType.LegFlex;
		lflex.dur = (int)lf_dur;
		lflex.delay = 0;
		lflex.addRequest("lAngle", lDeg + 90);
		
		Move run = play.addMove();
		run.type = MoveType.SideAcc;
		run.arg = gro_acc;
		run.delay = 0;
		run.dur = (int)gro_run_dur;
		
		Move glide = play.addMove();
		glide.type = MoveType.SideVel;
		glide.arg = vx_air;
		glide.delay = (int)gro_run_dur;
		glide.dur = (int)air_dur;
		
		Move jump = play.addMove();
		jump.type = MoveType.Jump;
		jump.delay = (int)(gro_run_dur - lr_dur);
		
		Move hit = play.addMove();
		hit.type = MoveType.Hit;
		hit.delay = (int)(tHit - trDur);

		Move safe = play.addMove();
		safe.type = MoveType.UND;
		safe.delay = (int)(tHit - 1);
		safe.dur = 2;
		safe.addRequest("tAngle", Math.toDegrees(thetaf));
		safe.addRequest("xTorso", xAir);  
		safe.addRequest("yTorso", yAir2);
		safe.addRequest("vT", Math.toDegrees(vT/dt));
		
		int l1 = HUD.me().findTarLock(pl);
		int l2 = HUD.me().findLineLock(pl);
		
		HUD.me().setTar(l1+0, xAim, yAim, pl.dir);
		HUD.me().setTar(l1+1, xAir, yAir, pl.dir);
		HUD.me().setTar(l1+2, xAir, yAir2, pl.dir);
		HUD.me().setTar(l1+3, xHit, yHit, pl.dir);
		
		HUD.me().setLineDest(l2+0, xHit, yHit, xMin, Ball.RAD, pl.dir);
		HUD.me().setLineDest(l2+1, xHit, yHit, 0, Ball.RAD, pl.dir);
		
		cleanHUD(pl, t + tHit);
		return true;
	}
	
	
	
	
	
	
	
	private static boolean isValidInputComboPush(float in[])
	{
		if(in[0] < 0 || in[0] > Court.W/2) return fail("[ComboPush]Bad xAim");
		if(in[1] < 0 || in[1] > Court.H) return fail("[ComboPush]Bad yAim");
		if(in[2] < 0 || in[2] > 1) return fail("[ComboPush]Bad initiative");
		if(in[3] < 0 || in[3] > 1) return fail("[ComboPush]Bad height");
		if(in[4] < 0 || in[4] > 1) return fail("[ComboPush]Bad speed");
		if(in[5] < 0 || in[5] > 1) return fail("[ComboPush]Bad force");
		
		return true;
	}
	
	public static boolean comboSmash(Play play, Player pl, BallView b, float in[], float dt)
	{
		if(!isValidInputComboPush(in)) return false;
		
		float height, initiative, xAim, yAim, xHit, yHit, t0, lDeg0, speed, 
		dur, force, xCalves, m, p, a, g, ux, uy, mu, mu2, alpha, z, r, t, theta00, theta0, thetaf,
		tfDur, trDur, vT, dHit, xMin;

		int s1;
		
		dHit = in[0];
		initiative = in[1];
		height = in[2];
		speed = in[3];
		force = in[4]; 
		
		in[in.length-1] = 1; 
		in[0] = xHit = Court.W/2 + (1 - initiative) * (pl.xCalves - Court.W/2);
		in[1] = yHit = Court.NETY_LO + height * (Player.J_MAX_H - Court.NETY_LO);
		in[2] = speed;
		in[3] = 0.4f;
		  
		if(!groBump(play, pl, b, in, dt)) return fail("[ComboPut]Ground failure");
		t0 = in[0];
		theta00 = in[1];
		lDeg0 = in[2];
		dur = in[3];
		xCalves = in[4];
		ux = in[5];
		uy = in[6];
		s1 = (int)in[8];
		
		xMin = coneStart(xHit, yHit);
		xAim =  (1 - dHit) * (xMin - 0) + 0;
		yAim = Ball.RAD;
		
		m = 2*(xAim - xHit);
		p = 2*(yAim - yHit);
		a = Ball.A * dt;
		g = Ball.G * dt;
		r = RHIT;
		z = Player.TR_RAD_ACC * dt;

		mu = Core.sHitSolveV(p,g,uy);
		mu2 = Core.sHitSolveV(m,a,ux);
		if(Float.isNaN(mu)) return fail("[ComboPush]Mu does not exist", pl);
		if(Float.isNaN(mu2) || Float.isInfinite(mu2)) mu2 = mu;
		mu = Core.min(mu, mu2);
		alpha = Core.sHitSolveT0(m,p,a,g,ux,uy,z,r);
		if(Float.isNaN(alpha)) return fail("[ComboPush]Alpha does not exist", pl);
		
		t = alpha + (1-force) * (mu - alpha);
		thetaf = Core.sHitEvalTf(m,p,a,g,ux,uy,t);
		theta0 = Core.sHitEvalT0(m,p,a,g,ux,uy,z,r,t);
		if(theta0 > (float)Math.PI/2) return fail("[ComboPush]Theta0 > PI/2: ", pl);
		
		trDur = Core.polySolve_t1(z, 0, theta0, thetaf);
		tfDur = Player.tTorFlexRadAbs(Math.abs(theta0-theta00), dt);
		vT = z * trDur;
		
		float xAir, yAir, gro_run_dur, gro_acc, vx_air, lDeg, lf_dur, 
		lr_dur, yAir2, air_dur;
		
		xTorHit(xHit, yHit, thetaf, in);
		xAir = in[0];
		yAir2 = in[1];
		yAir = yAir2 - Player.LEG_H * 2;
		
		if( !runJump(xCalves, xAir, yAir, t0, lDeg0, in, dt) ) return false;
		gro_run_dur = in[0];
		gro_acc = in[2];
		vx_air = in[3];  
		lDeg = in[4];
		lf_dur = in[5];
		lr_dur = in[6];
		air_dur = in[7];
	
		play.type = PlayType.ComboSmash;
		
		Move tflex = play.addMove();
		tflex.type = MoveType.TorsoFlex;
		tflex.dur = (int)tfDur;
		tflex.delay = (int)(dur + 2);
		tflex.arg = Math.signum(theta00 - theta0);
		tflex.addRequest("tAngle", Math.toDegrees(theta0));

		Move lflex = play.addMove();
		lflex.type = MoveType.LegFlex;
		lflex.dur = (int)(lf_dur);
		lflex.delay = (int)(0 + dur);
		lflex.arg = Math.signum(lDeg - lDeg0);
		lflex.addRequest("lAngle", lDeg + 90);
		
		Move run = play.addMove();
		run.type = MoveType.SideAcc;
		run.arg = gro_acc;
		run.delay = (int)(0 + dur);
		run.dur = (int)gro_run_dur;
		
		Move glide = play.addMove();
		glide.type = MoveType.SideVel;
		glide.arg = vx_air;
		glide.delay = (int)(gro_run_dur + dur);
		glide.dur = (int)air_dur;
		
		Move jump = play.addMove();
		jump.type = MoveType.Jump;
		jump.delay = (int)(dur + gro_run_dur - lr_dur);
		
		Move hit = play.addMove();
		hit.type = MoveType.Hit;
		hit.delay = (int)(dur + t0 - trDur);

		Move safe = play.addMove();
		safe.type = MoveType.UND;
		safe.delay = (int)(dur + t0 - 2);
		safe.dur = 2;
		safe.addRequest("tAngle", Math.toDegrees(thetaf));
		safe.addRequest("xTorso", xAir);  
		safe.addRequest("yTorso", yAir2);
		safe.addRequest("absorb", 2345435);
		safe.addRequest("vT", Math.toDegrees(vT/dt));
		
		int l3 = HUD.me().findLineLock(pl);
		
		HUD.me().setTar(s1 + 1, xAim, yAim, pl.dir);
		HUD.me().setTar(s1 + 2, xAir, yAir, pl.dir);
		HUD.me().setTar(s1 + 3, xAir, yAir2, pl.dir);
		HUD.me().setTar(s1 + 4, xHit, yHit, pl.dir);
		
		HUD.me().setLineDest(l3+0, xHit, yHit, xMin, Ball.RAD, pl.dir);
		HUD.me().setLineDest(l3+1, xHit, yHit, 0, Ball.RAD, pl.dir);
		
		cleanHUD(pl, dur + t0 + t);
		return true;
 	}

	
	
	
	
	/**
	 * 
	 * @param in[0] X destination
	 * @param in[1] Y destination
	 * @param in[2] net proximity ratio, 0 is farthest, 1 closest
	 * @param in[3] height ratio, 0 is net level, 1 is high as possible
	 * @param in[4] run up speed ratio, 0 is slowest 
	 * @param in[5] hit  altitude ratio, 0 is lowest, 1 is highest 
	 * @return true if the play could be constructed, and constructed play
	 */
	public static boolean comboBump(Play play, Player pl, BallView b, float in[], float dt)
	{
		float height, initiative, xAim, yAim, xHit, yHit, vx, vy, t0, lDeg0, theta0, speed, 
		dur, force, xCalves;
		int s1;
		
		xAim = in[0];
		yAim = in[1];
		initiative = in[2];
		height = in[3];
		speed = in[4];
		force = in[5];
		
		in[in.length-1] = 1;
		in[0] = xHit = Court.W/2 + (1 - initiative) * (pl.xCalves - Court.W/2);
		in[1] = yHit = height * (Player.J_MAX_H - Court.NETY_LO) + Court.NETY_LO;
		in[2] = speed;
		in[3] = 0.6f;
		  
		if(!groBump(play, pl, b, in, dt)) return fail("[ComboPut]Ground failure");
		t0 = in[0];
		theta0 = in[1];
		lDeg0 = in[2];
		dur = in[3];
		xCalves = in[4];
		vx = in[5];
		vy = in[6];
		s1 = (int)in[8];
		
		in[in.length-1] = 1;
		in[0] = xHit;
		in[1] = yHit;
		in[2] = xAim;
		in[3] = yAim;
		in[4] = t0;
		in[5] = vx;
		in[6] = vy;
		in[7] = force;
		in[8] = theta0;
		in[9] = lDeg0;
		in[10] = dur;
		in[11] = xCalves;
		in[13] = s1;

		return airBump(play, pl, b, in, dt);
 	}

	public static boolean airBump(Play play, Player pl, BallView b, float in[], float dt)
	{
		boolean absolute = (in[in.length-1] == 1);
		float xHit, yHit, xAim, yAim, t1, a, g, ux, uy, m, p, k, kmin, kmax, theta, vx, vy,
		t, d, force, theta0, lDeg0, xCalves;
		int t0, s1;
		
		a = -Ball.A * dt;
		g = Ball.G * dt;  

		if(absolute)
		{
			xHit = in[0];
			yHit = in[1];
			xAim = in[2];
			yAim = in[3];
			t1 = in[4];
			ux = in[5];
			uy = in[6];
			force = in[7];
			theta0 = in[8];
			lDeg0 = in[9];
			t0 = (int)in[10];
			xCalves = in[11];
			s1 = (int)in[13] + 1;
		} 

		else
		{
			float initiative, tMin, tMax;
			xAim = in[0];
			yAim = in[1];
			initiative = in[2];
			force = in[3];

			tMin = Core.polySolve_t1(a, b.vX*dt, b.x, Court.W/2+30);
			tMax = Core.polySolve_t0(g, b.vY*dt, b.y, Court.NETY_LO);
			t1 = (1-initiative) * (tMax-tMin) + tMin;
			
			if(tMin > tMax) return fail("[AirPut]Bad incoming motion", pl);
			xHit = Core.polyEval(a, b.vX*dt, b.x, t1);
			yHit = Core.polyEval(g, b.vY*dt, b.y, t1);
			
			ux = ((b.vX * dt) + (t1 * a)); 
			uy = ((b.vY * dt) + (t1 * g));
			
			theta0 = (float)(Math.PI/2);
			lDeg0 = 0;
			t0 = 0;
			xCalves = pl.xCalves;
			
			s1 = HUD.me().findTarLock(pl);
		}
		 
		a = Ball.A * dt;
		m = 2*(xAim - xHit);
		p = 2*(yAim - yHit);
		
		kmin = Core.cHitEvalMin(m,p,a,g);
		kmax = Core.vectorLen2(ux, uy);
		k = kmin + force * (kmax - kmin); 
		d = k / kmax;
		 
		t = Core.cHitSolveK1(m,p,a,g,k);
		if(Float.isNaN(t)) return fail("[Ground]Dest unreachable, abort", pl);

		vx = Core.polySolve_b(a, xHit, xAim, t);
		vy = Core.polySolve_b(g, yHit, yAim, t);
		ux = ux * (float)Math.sqrt(d);
		uy = uy * (float)Math.sqrt(d);
		 
		theta = (float)Math.atan2(-(vx-ux), vy-uy);
		if(theta < 0) return fail("[AirPut]Torso theta < 0, abort", pl);
		
		float xAir, yAir, gro_run_dur, gro_acc, vx_air, lDeg, lfDur, 
		lrDur, yAir2, air_dur, tfDur;
		
		xTorHit(xHit, yHit, theta, in);
		xAir = in[0];
		yAir2 = in[1];
		yAir = yAir2 - Player.LEG_H * 2;
		
		if( !runJump(xCalves, xAir, yAir, t1, lDeg0, in, dt)) return false;
		gro_run_dur = in[0];
		gro_acc = in[2];
		vx_air = in[3];  
		lDeg = in[4];
		lfDur = in[5];
		lrDur = in[6];
		air_dur = in[7];

		tfDur = absolute ? 
			Player.tTorFlexRadAbs(Math.abs(theta-theta0), dt) :
			Player.tTorFlexRad(theta, dt);
		
		play.type = in[in.length-1] == 1 ? PlayType.ComboBump : PlayType.AirBump;	
			
		Move torsoFlex = play.addMove();
		torsoFlex.type = MoveType.TorsoFlex;
		torsoFlex.dur = (int)tfDur;
		torsoFlex.arg = Math.signum(theta0 - theta);;
		torsoFlex.delay = (int)(0 + t0 + 3);
		torsoFlex.addRequest("tAngle", Math.toDegrees(theta));
		torsoFlex.addRequest("absorb",d);

		Move legFlex = play.addMove();
		legFlex.type = MoveType.LegFlex;
		legFlex.dur = (int)lfDur;
		legFlex.delay = (int)(0 + t0);
		legFlex.arg = Math.signum(lDeg - lDeg0);
		legFlex.addRequest("lAngle", lDeg + 90);
		
		Move run = play.addMove();
		run.type = MoveType.SideAcc;
		run.arg = gro_acc;
		run.delay = (int)(0 + t0);
		run.dur = (int)gro_run_dur;
		
		Move glide = play.addMove();
		glide.type = MoveType.SideVel;
		glide.arg = vx_air;
		glide.delay = (int)(gro_run_dur + t0);
		glide.dur = (int)air_dur;
		
		Move jump = play.addMove();
		jump.type = MoveType.Jump;
		jump.delay = (int)(gro_run_dur - lrDur + t0);
		
		HUD.me().setTar(s1+0, xAim, yAim, pl.dir);
		HUD.me().setTar(s1+1, xHit, yHit, pl.dir);
		HUD.me().setTar(s1+2, xAir, yAir2, pl.dir);
		
		cleanHUD(pl, t + t0);
		return true;
	}

	private static boolean fail(String s, Player pl)
	{
		int l1 = HUD.me().findTarLock(pl);
		int l2 = HUD.me().findLineLock(pl);
		
		HUD.me().hideTars(l1, TAR_COUNT);
		HUD.me().hideLines(l2, 2);

		return fail(s);
	}

	private static boolean fail(String s)
	{
//		Core.print(s);
		return false;
	}

	private static void cleanHUD(Object key, float t)
	{
		int l1 = HUD.me().findTarLock(key);
		int l2 = HUD.me().findLineLock(key);
		
		String[][]cmds = new String[][]
		{
				{"hud", "hideTars", l1+"", TAR_COUNT+""},
				{"hud", "hideLines", l2+"", 2+""}
		};
		Dispatch.me().futureRequest(t + 10, cmds);
	}
	
	static boolean isGoodOppHit(float alpha, float omega, float theta0, float thetaf)
	{
		if(Float.isNaN(alpha)) return fail("Alhpa Nan");
		if(Float.isNaN(omega)) return fail("Omega Nan");
		if(Float.isNaN(theta0)) return fail("Theta0 Nan");
		if(Float.isNaN(thetaf)) return fail("Thetaf Nan");
		
		if(alpha < 20) return fail("Alhpa too small");
		if(omega > 130) return fail("Omega too big");
		if(theta0 < 0) return fail("Theta0 negative");
		
		return true;
	}

	
	
}
