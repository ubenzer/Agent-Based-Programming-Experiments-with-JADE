package agent;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;

import java.util.HashSet;
import java.util.Set;

import behaviour.UpdateMusicDiscoveryAgents;

public class Human extends Agent {

  /**
   * 
   */
  private static final long serialVersionUID = 3789029878086673023L;

  public final Set<DFAgentDescription> knownMusicDiscoveryServiceList = new HashSet<DFAgentDescription>();
  @Override
  protected void setup() {
    
   System.out.println("Hello World. I’m an agent!");
   System.out.println("My local-name is " + getAID().getLocalName());
   System.out.println("My GUID is " + getAID().getName());
   
   addBehaviour(new UpdateMusicDiscoveryAgents(this));
  }
  
}
