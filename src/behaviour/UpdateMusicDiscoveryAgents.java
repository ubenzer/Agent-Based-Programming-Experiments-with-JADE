package behaviour;

import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import agent.Human;

public class UpdateMusicDiscoveryAgents extends TickerBehaviour {

  /**
   * 
   */
  private static final long serialVersionUID = -7468459312539756492L;

  private final Human agent;
  public UpdateMusicDiscoveryAgents(Human agent) {
    super(agent, 5000);
    this.agent = agent;
  }

  @Override
  protected void onTick() {
    ServiceDescription sd = new ServiceDescription();
    sd.setType("MUSIC-DISCOVERY");
    DFAgentDescription df = new DFAgentDescription();
    df.addServices(sd);
    try {
      DFAgentDescription[] result = DFService.search(this.myAgent, df);
      System.out.println("Updating music discovery agent list from DF... " + result.length + " found.");
      this.agent.knownMusicDiscoveryServiceList.clear();
      for(DFAgentDescription dfad: result) {
        this.agent.knownMusicDiscoveryServiceList.add(dfad);
      }
    } catch (FIPAException e) {
      e.printStackTrace();
    }
  }

}
