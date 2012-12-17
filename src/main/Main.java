package main;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Main {
  
  public static void main(String args[]) {
    
    Profile p = new ProfileImpl();
    p.setParameter(Profile.MAIN_HOST, "localhost");
    p.setParameter(Profile.MAIN_PORT, "3250");
    
    Runtime r = Runtime.instance();
    ContainerController cc = r.createMainContainer(p);
    if (cc != null) {
      try {
        AgentController ac = cc.createNewAgent("insan1", "agent.MusicSeeker", null);
        ac.start();
        
        ac = cc.createNewAgent("SONY", "agent.MusicProvider", null);
        ac.start();
        
        ac = cc.createNewAgent("EROL KÖSE", "agent.MusicProvider", null);
        ac.start();
        
      } catch (StaleProxyException e) {
        e.printStackTrace();
      }
    }
     

  }
  
}
