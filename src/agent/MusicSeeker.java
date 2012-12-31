package agent;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
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

import misc.Logger;
import pojo.Song;
import pojo.SongRequestInfo;
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
  private MusicSeeker agent = this;
  private MusicView ui;
  
  @Override
  protected void setup() {
   
   EventQueue.invokeLater(new Runnable() {
     @Override
     public void run() {
       Logger.info(agent, "İnsanlarla iletişim kurmak için UI yaratılıyor...");
       try {
         MusicView ui = new MusicView(agent);
         ui.setVisible(true);
       } catch (Exception e) {
         Logger.error(agent, e, "UI yaratılamadı!");
       }
     }
   });
 
   addBehaviour(new UpdateMusicDiscoveryAgents());
  }
  
  public MusicView getUi() {
    return ui;
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
  
  public final class FindAndPurchaseMusics extends SequentialBehaviour {

    public FindAndPurchaseMusics(Genre genre, float maxBudgetPerSongI, int maxSongCountI, int minRatingI, float totalBudgetI) {
      super.addSubBehaviour(new LookForMusic(genre, maxBudgetPerSongI, minRatingI));
      //super.listenAnswers // TODO
      //super.addSubBehaviour(new BuyMusic(Set<Music> musicList));
    }
    
    private class LookForMusic extends OneShotBehaviour {
     private float maxBudgetPerSong;
     private float minRating;
     private final Genre genre;
     public LookForMusic(Genre genre, float maxBudgetPerSong, int minRating) {
       this.genre = genre;
       this.maxBudgetPerSong = maxBudgetPerSong;
       this.minRating = minRating;
     }

    @Override
    public void action() {
      Logger.info(agent, "Müzik satıcılarına verilen kriterlere uygun bir müzik listesi isteği yollanıyor...");
      ACLMessage msg = new ACLMessage(ACLMessage.CFP);
      for(DFAgentDescription df: agent.knownMusicDiscoveryServiceList) {
        msg.addReceiver(df.getName());
      }
      try {
        msg.setContentObject(new SongRequestInfo(genre, maxBudgetPerSong, minRating));
        this.myAgent.send(msg);
      } catch (Exception e) {
        Logger.error(agent, e, "Müzik satıcılarına müzik arama isteği yollanamadı.");
      }
    }
   }
  }
  private final class UpdateMusicDiscoveryAgents extends TickerBehaviour {

    public UpdateMusicDiscoveryAgents() {
      super(agent, 10000);
    }

    @Override
    protected void onTick() {
      Logger.info(agent, "Müzik satma hizmeti sunan etmen listesi güncelleniyor...");
      ServiceDescription sd = new ServiceDescription();
      sd.setType("MUSIC-DISCOVERY");
      DFAgentDescription df = new DFAgentDescription();
      df.addServices(sd);
      try {
        DFAgentDescription[] result = DFService.search(this.myAgent, df);
        agent.knownMusicDiscoveryServiceList.clear();
        for(DFAgentDescription dfad: result) {
          agent.knownMusicDiscoveryServiceList.add(dfad);
        }
        Logger.info(agent, "Müzik satma hizmeti sunan etmenler güncelledi. %s etmen bulundu.", result.length);
      } catch (FIPAException e) {
        Logger.error(agent,  e, "Müzik satma hizmeti sunan etmenler güncellnirken hata oluştu.");
      }
    }

  }
  
  public class ShutdownAgent extends OneShotBehaviour {

    @Override
    public void action() {
      agent.doDelete();
    }

  }
}
