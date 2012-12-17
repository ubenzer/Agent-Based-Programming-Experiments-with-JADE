package agent;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import pojo.Song;

public class MusicProvider extends Agent {

  public static enum Genre { POP, CLASSIC, JAZZ, ROCK, COUNTRY }
  /**
   * 
   */
  private static final long serialVersionUID = -5369914972197884450L;
  final Map<Genre, HashSet<Song>> songList = initSongList();

  @Override
  public void setup() {
    
    /* Broadcast this agent */
    ServiceDescription sd = new ServiceDescription();
    sd.setType("MUSIC-DISCOVERY");
    
    DFAgentDescription df = new DFAgentDescription();
    df.addServices(sd);
    
    try {
      DFService.register(this, df);
      System.out.println("Broadcasting new music discovery agent, named: " + getName());
    } catch (FIPAException e) {
      e.printStackTrace();
    }
    
    addSongToSellList(new Song("Scorpions", "Hurricane 2000", Genre.ROCK));
    
    addBehaviour(new CheckBuyerMessages());
  }
  
  private static Map<Genre, HashSet<Song>> initSongList() {
    Map<Genre, HashSet<Song>> tbReturned = new HashMap<Genre, HashSet<Song>>();
    for(Genre g: Genre.values()) {
      tbReturned.put(g, new HashSet<Song>());
    }
    return tbReturned;
  }

  public void addSongToSellList(Song s) {
    this.songList.get(s.getGenre()).add(s);
  }
  
  public boolean removeSongFromSellList(Song s) {
    return this.songList.get(s.getGenre()).remove(s);
  }
  
  private class CheckBuyerMessages extends CyclicBehaviour {

    /**
     * 
     */
    private static final long serialVersionUID = -1100608993864604482L;

    public CheckBuyerMessages() {
    }

    @Override
    public void action() {
      
      System.out.println(this.myAgent.getName() + " is checking for messages... ");
      ACLMessage msg = this.myAgent.receive();
      if (msg == null) { return; }
      
      ACLMessage reply = msg.createReply();
      Genre requestedGenre = null;
      try {
        requestedGenre = (Genre)msg.getContentObject();
      } catch (UnreadableException e) {
        e.printStackTrace();
      }
      
      if(requestedGenre == null || MusicProvider.this.songList.get(requestedGenre).size() == 0) {
        reply.setPerformative(ACLMessage.REFUSE);
      } else {
        reply.setPerformative(ACLMessage.PROPOSE);
        try {
          reply.setContentObject(MusicProvider.this.songList.get(requestedGenre));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
   
      this.myAgent.send(reply);
    }
  }
}
