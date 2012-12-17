package agent;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.awt.EventQueue;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import pojo.Song;
import pojo.SongSellInfo;
import view.MusicView;
import agent.MusicProvider.Genre;

public class MusicSeeker extends Agent {

  /**
   * 
   */
  private static final long serialVersionUID = 3789029878086673023L;

  public final Set<DFAgentDescription> knownMusicDiscoveryServiceList = new HashSet<DFAgentDescription>();
  public final HashMap<Genre, HashMap<Song, SongSellInfo>> boughtSongs = new HashMap<Genre, HashMap<Song, SongSellInfo>>();
  
  @Override
  protected void setup() {
    
   System.out.println("Hello World. I’m an agent!");
   System.out.println("My local-name is " + getAID().getLocalName());
   System.out.println("My GUID is " + getAID().getName());
   final MusicSeeker agent = this;
   EventQueue.invokeLater(new Runnable() {
     @Override
     public void run() {
       try {
         MusicView frame = new MusicView(agent);
         frame.setVisible(true);
       } catch (Exception e) {
         e.printStackTrace();
       }
     }
   });
 
   addBehaviour(new UpdateMusicDiscoveryAgents(this));
   addBehaviour(new CheckSellerMessages());
  }
  
  public class CheckSellerMessages extends CyclicBehaviour {

    /**
     * 
     */
    private static final long serialVersionUID = 8708169638562335109L;

    public CheckSellerMessages() {
    }

    @Override
    public void action() {
      
      System.out.println(this.myAgent.getName() + " is checking for messages... ");
      ACLMessage msg = this.myAgent.receive();
      if (msg == null) { return; }
      
      if(msg.getPerformative() == ACLMessage.REFUSE) {
        System.out.println(msg.getSender().getName() + " refused me!");
        return;
      }
      
      HashMap<Song, SongSellInfo> songListReturned;
      try {
        songListReturned = (HashMap<Song, SongSellInfo>) msg.getContentObject();
      } catch (UnreadableException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      //if(songListReturned == null || songListReturned.size() == 0) { return; }
      
     
    }
  }
  
  private class UpdateMusicDiscoveryAgents extends TickerBehaviour {

    /**
     * 
     */
    private static final long serialVersionUID = -7468459312539756492L;

    private final MusicSeeker agent;
    public UpdateMusicDiscoveryAgents(MusicSeeker agent) {
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
}
