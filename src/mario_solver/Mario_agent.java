/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mario_solver;

import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

/**
 *
 * @author Nassim
 */
public class Mario_agent implements ch.idsia.ai.agents.Agent  {

    protected boolean Action[] = new boolean[Environment.numberOfButtons];
    protected String Name = "MarioAgent";
    protected boolean PrintOnce = true;
    @Override
    public void reset()
    {
        Action = new boolean[Environment.numberOfButtons];
        Action[Mario.KEY_RIGHT] = true;
        Action[Mario.KEY_SPEED] = true;
    }

    /**
     * print the given matrix on the terminal
     * @param observation a matrix of bytes
     */
    private void showMatrix(byte [][] observation){
        for(byte[] v : observation){
            for(byte e: v){
                System.out.print(" "+e+" ");
            }
            System.out.println();
        }
    }
    
    /**
     * print a line separator on the terminal
     */
    private void Separator(){
        System.out.println("-----------------------------------------------------------------------------");
    }
    
    /**
     * did we see some enemies or obstacles in the map
     * @param obs total observation of the map
     * @return  true if obstacle or enemy on the map
     */
    private boolean ThereIsSomething(byte[][] obs){
       
        for(byte[] v : obs){
            for(byte e: v){
               if(e != 0)
                   return true;
            }            
        }
        return false;
    }
    
    private boolean EnemyInFrontSameLevel(byte[][] obs){
       
        if(obs[11][12] != 0 || obs[11][13] != 0 || obs[11][14] != 0 || obs[11][15] != 0)
            return true;
        if(obs[10][12] != 0 || obs[10][13] != 0 || obs[10][14] != 0 || obs[10][15] != 0)
            return true;        
        return false;
    }
    
    private boolean ShallJumpEnemy(byte[][] obs){
       
        if(obs[11][12] != 0 || obs[10][12] != 0)
            return true;
        return false;
    }
    
    private boolean isBigBlockage(byte[][] obs){
         if(obs[11][14] != 0 && obs[7][14] != 0)
            return true;
        return false;
    }
    
    private boolean NearBlockage(byte[][] obs){
        if(obs[11][12] != 0 || obs[11][13] != 0 || obs[11][14] != 0 || obs[11][15] != 0 )
            return true;
        return false;
    }
    private boolean RoadBlocked(byte[][] obs){
       
        if(obs[11][12] != 0 || obs[11][13] != 0 )
            return true;
        return false;
    }
    
    private boolean NearLitleHole(byte[][] obs){
        if(obs[12][12] == 0 && obs[12][13] == 0  )
            return true;
        return false;
    }
    
     private boolean NearBigHole(byte[][] obs){
        if(obs[12][12] == 0 && obs[12][13] == 0 && obs[12][14] == 0 && obs[12][15] == 0  )
            return true;
        return false;
    }
     
    private boolean NextToAHole (byte[][] obs){
        if((obs[12][12] == 0 || obs[12][13] == 0) && (obs[16][12] == 0 || obs[16][13] == 0))
            return true;
        return false;
    }
    
    private boolean NothingBehind (byte[][] obs){
        if(obs[11][10] != 0 || obs[11][9] != 0 || obs[12][10] == 0 || obs[12][9] == 0)
            return false;
        return true;
    }
    /**
     * analyse the observation matrix
     * @param observation the complete observation of the environment
     */
    private void scanObservation(Environment observation){
        byte[][] CompleteObs = observation.getLevelSceneObservation();
        byte[][] EnemiesObs = observation.getEnemiesObservation();
        float[] Mpos = observation.getMarioFloatPos();
        
        if(ThereIsSomething(CompleteObs)){
            Separator();
            for(int i = 0; i < Mpos.length; ++i)
                System.out.print(Mpos[i]+" | ");
            System.out.println();
            Separator();
            showMatrix(CompleteObs);
            Separator();
            showMatrix(EnemiesObs);
            Separator();
            PrintOnce = false;        
        }    
    }  
    
    // shortcuts begin ------------------------------ --------------------------------     
    private void run()
    {
        Action[Mario.KEY_RIGHT] = true;
        Action[Mario.KEY_SPEED] = true;
        Action[Mario.KEY_LEFT] = false;
    }
    private void walk()
    {
        Action[Mario.KEY_RIGHT] = true;
        Action[Mario.KEY_SPEED] = false;
        Action[Mario.KEY_LEFT] = false;
    }
    
    private void goback()
    {
        Action[Mario.KEY_RIGHT] = false;
        Action[Mario.KEY_SPEED] = false;
        Action[Mario.KEY_LEFT] = true;
    }
    
    private void jump()
    {
       Action[Mario.KEY_JUMP] = true;       
    }
     
    private void unjump()
    {
       Action[Mario.KEY_JUMP] = false;
    }
    
     private void standby()
     {
        Action[Mario.KEY_RIGHT] = false;
        Action[Mario.KEY_SPEED] = false;
        Action[Mario.KEY_LEFT]  = false;
        Action[Mario.KEY_JUMP]  = false;
     }
    // end shortcuts ------------------------------------------------------------------
    @Override
    public boolean[] getAction(Environment observation)
    {
             
        byte[][] Enemies = observation.getEnemiesObservation();
        byte[][] World = observation.getLevelSceneObservation();
        byte[][] Total = observation.getCompleteObservation();
        //show helper info
        //if(PrintOnce){
            scanObservation(observation);            
        //}
        
        //if nothing
        standby();
        
        //somthing is hapening
        walk();                
        if(EnemyInFrontSameLevel(Enemies)){
            if(observation.isMarioOnGround())
                if(NothingBehind(Total)){
                    //run();
                    //goback();
                    standby();
                }
            //run();
            if(ShallJumpEnemy(Enemies))
                jump();            
        }
        
        if(NearBlockage(World)){
            //if(isBigBlockage(World))
            //    run(); 
            if(RoadBlocked(World) && observation.mayMarioJump())
                jump();
            if(!observation.isMarioOnGround())
                jump();                        
        }
        
        if(NearLitleHole(World)){
            if(NearBigHole(World))
                run();
            if(NextToAHole(World) && observation.mayMarioJump())
                jump();
            if(!observation.isMarioOnGround())
                jump();
        }
        return Action;
    }

    @Override
    public AGENT_TYPE getType() {
        return AGENT_TYPE.AI;
    }

    @Override
    public String getName() {        return Name;    }

    @Override
    public void setName(String Name) { this.Name = Name;    }
}
