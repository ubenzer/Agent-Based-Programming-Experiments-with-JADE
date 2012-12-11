package behaviour;

import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;

import java.io.IOException;

import agent.Human;
import agent.MusicProvider.Genre;

public class LookForMusic extends TickerBehaviour {

  private float balance;
  private int maxSongCount;
  private float maxPricePerSong;
  private float minRating;
  private long maxTime; /* in ms*/
  private final Genre genre;
  private final Human myAgent;
  public LookForMusic(Human h, Genre genre, float balance, int maxSongCount, float maxPricePerSong, float minRating, long maxTime) {
    super(h, 2000);
    this.genre = genre;
    this.myAgent = h;
    this.balance = balance;
    this.maxPricePerSong = maxPricePerSong;
    this.maxSongCount = maxSongCount;
    this.minRating = minRating;
    this.maxTime = maxTime;
  }

  /**
   * 
   */
  private static final long serialVersionUID = 1743393184305592964L;

  @Override
  protected void onTick() {
    for(DFAgentDescription df: this.myAgent.knownMusicDiscoveryServiceList) {
      ACLMessage msg = new ACLMessage(ACLMessage.CFP);
      msg.addReceiver(df.getName());
      try {
        msg.setContentObject(this.genre);
      } catch (IOException e) {
        e.printStackTrace();
      }
      this.myAgent.send(msg);
    }
  }

}
