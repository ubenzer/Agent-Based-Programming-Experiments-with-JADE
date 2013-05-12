package agent;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import java.awt.EventQueue;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;

import misc.Logger;
import pojo.Song;
import pojo.SongRequestInfo;
import pojo.SongSellInfo;
import util.F.Tuple;
import util.Utils;
import view.MusicView;

public class MusicSeeker extends Agent {

  public final HashSet<DFAgentDescription> knownMusicDiscoveryServiceList = new HashSet<DFAgentDescription>();
  public final HashMap<Song.Genre, HashMap<Song, SongSellInfo>> boughtSongs = new HashMap<Song.Genre, HashMap<Song, SongSellInfo>>();
  private MusicSeeker agent = this;
  private MusicView ui;
  
  @Override
  protected void setup() {
   
   EventQueue.invokeLater(new Runnable() {
     @Override
     public void run() {
       Logger.info(agent, "Creating Music Seeker UI...");
       try {
         ui = new MusicView(agent);
         ui.setVisible(true);
       } catch (Exception e) {
         Logger.error(agent, e, "Couldn't create UI!");
       }
     }
   });
 
   addBehaviour(new UpdateMusicDiscoveryAgents());
  }
  
  public MusicView getUi() {
    return ui;
  }
  
  public final class FindAndPurchaseMusics extends SequentialBehaviour {
    private Set<DFAgentDescription> knownAgentsAtTimeBehaviourStarted;
    private Set<SongSellInfo> songOffers = new HashSet<SongSellInfo>();
    private Set<SongSellInfo> songsToBuy = new HashSet<SongSellInfo>();
    
    public FindAndPurchaseMusics(Song.Genre genre, float maxBudgetPerSongI, int maxSongCountI, float minRatingI, float totalBudgetI) {
      knownAgentsAtTimeBehaviourStarted = (HashSet<DFAgentDescription>) agent.knownMusicDiscoveryServiceList.clone();
      super.addSubBehaviour(new LookForMusic(genre, maxBudgetPerSongI, minRatingI));
      super.addSubBehaviour(new ListenLookForMusicAnswers());
      super.addSubBehaviour(new SelectMusic(songOffers, genre, maxBudgetPerSongI, maxSongCountI, minRatingI, totalBudgetI));
      super.addSubBehaviour(new BuyMusic(songsToBuy));
      super.addSubBehaviour(new ListenBuyMusicAnswers());
    }
    
    private class LookForMusic extends OneShotBehaviour {
     private float maxBudgetPerSong;
     private float minRating;
     private final Song.Genre genre;
     public LookForMusic(Song.Genre genre, float maxBudgetPerSong, float minRating) {
       this.genre = genre;
       this.maxBudgetPerSong = maxBudgetPerSong;
       this.minRating = minRating;
     }

    @Override
    public void action() {
      Logger.info(agent, "Sending song search request to music providers...");
      ACLMessage msg = new ACLMessage(ACLMessage.CFP);
      for(DFAgentDescription df: knownAgentsAtTimeBehaviourStarted) {
        msg.addReceiver(df.getName());
      }
      try {
        msg.setContentObject(new SongRequestInfo(genre, maxBudgetPerSong, minRating));
        this.myAgent.send(msg);
      } catch (Exception e) {
        Logger.error(agent, e, "Couldn't send song search request to music providers.");
      }
    }
   }
  
    private class ListenLookForMusicAnswers extends SimpleBehaviour {
      private int answerCount = 0;
      private final long TIMEOUT_MS = 15000;
      private final long WAIT_MS = 1000;
      private final long startTime;
      
      public ListenLookForMusicAnswers() {
        super();
        startTime = System.currentTimeMillis();
      }

      @Override
      public void action() {
        Logger.info(agent, "Recieving song search results... (%s / %s)", answerCount, knownAgentsAtTimeBehaviourStarted.size());
        ACLMessage msg = this.myAgent.receive();
        if (msg == null) { block(WAIT_MS); return; }
        
        try {
          if(!msg.getContentObject().getClass().equals(HashSet.class)) {
            Logger.warn(agent, "Unexcepted message received from agent %s.", msg.getSender().getName());
            return;
          }
          
          answerCount++;
          
          if(msg.getPerformative() == ACLMessage.REFUSE) {
            Logger.warn(agent, "%s refuesd the message!", msg.getSender().getName());
            return;
          }
          
          HashSet<SongSellInfo> songListReturned = (HashSet<SongSellInfo>) msg.getContentObject();
          if(songListReturned == null || songListReturned.size() == 0) { 
            Logger.warn(agent, "%s sent an empty list of songs!", msg.getSender().getName());
            return;
          }
          
          for(SongSellInfo s: songListReturned) {
            /* We take security serious */
            if(!s.getSellerAgent().equals(msg.getSender())) {
              Logger.error(agent, "Music seller agent isn't the agent he claims to be! Security! (for agent %s)", msg.getSender().getName());
              return;
            }
          }
          
          songOffers.addAll(songListReturned);
          
        } catch (Exception e) {
          Logger.error(agent, e, "Couldn't collect the song search results.");
        }
       
      }

      @Override
      public boolean done() {
        if(System.currentTimeMillis() - startTime > TIMEOUT_MS) {
          Logger.warn(agent, "Timeout occured while waiting for response.");
          return true;
        }
        
        if(answerCount >= knownAgentsAtTimeBehaviourStarted.size()) {
          return true;
        }
        
        return false;
      }
    }
  
    private class SelectMusic extends OneShotBehaviour {

      private Set<SongSellInfo> songsProposed;
      private Song.Genre genre;
      private float maxBudgetPerSongI;
      private int maxSongCountI;
      private float minRatingF;
      private float totalBudgetI;
      
      public SelectMusic(Set<SongSellInfo> songOffers, Song.Genre genre, float maxBudgetPerSongI, int maxSongCountI, float minRatingF, float totalBudgetI) {
        this.songsProposed = songOffers;
        this.genre = genre;
        this.maxBudgetPerSongI = maxBudgetPerSongI;
        this.maxSongCountI = maxSongCountI;
        this.minRatingF = minRatingF;
        this.totalBudgetI = totalBudgetI;
      }

      @Override
      public void action() {
        Map<Song, Float> ratingMap = new HashMap<Song, Float>();
        Map<Song, Float> priceMap = new HashMap<Song, Float>();
        Map<Song, SongSellInfo> ssiMap = new HashMap<Song, SongSellInfo>();
        for(SongSellInfo ssi: songsProposed) {
          Song song = ssi.getSong();
          
          Float maxRating = ratingMap.get(song);
          Float minPrice = priceMap.get(song);
          
          if(maxRating == null || maxRating < ssi.getAvgRating()) {
            ratingMap.put(song, ssi.getAvgRating());
          }
          
          if(minPrice == null || minPrice > ssi.getPrice()) {
            priceMap.put(song, ssi.getPrice());
            ssiMap.put(song, ssi);
          }
        }
        
        Set<SongSellInfo> filteredSongList = new HashSet<SongSellInfo>();
        float totalPrice = 0;
        
        /* Elaminate unmatched songs and find min prica and max rating for valid songs */
        for (SongSellInfo ssi: songsProposed) {
          Song song = ssi.getSong();
          
          if(filteredSongList.contains(ssi) || !this.genre.equals(ssi.getSong().getGenre()) || ratingMap.get(song) < this.minRatingF || priceMap.get(song) > this.maxBudgetPerSongI) {
            continue;
          }
          
          filteredSongList.add(ssiMap.get(song));
          totalPrice += ssiMap.get(song).getPrice();
        }
        
        if(filteredSongList.size() > this.maxSongCountI) {
          Iterator<SongSellInfo> i = filteredSongList.iterator();
          while (filteredSongList.size() > this.maxSongCountI) {
            SongSellInfo itemToDelete = i.next();
            totalPrice -= itemToDelete.getPrice();
            i.remove();
          }
        }
        
        if(totalPrice > this.totalBudgetI) {
          Iterator<SongSellInfo> i = filteredSongList.iterator();
          while (totalPrice > this.totalBudgetI) {
            SongSellInfo itemToDelete = i.next();
            totalPrice -= itemToDelete.getPrice();
            i.remove();
          }
        }
        
        songsToBuy.addAll(filteredSongList);
      }
    }
  
    private class BuyMusic extends OneShotBehaviour {
      private Set<SongSellInfo> songsToBuyList;
      public BuyMusic(Set<SongSellInfo> songsToBuy) {
        this.songsToBuyList = songsToBuy;
      }

      @Override
      public void action() {
        if(songsToBuyList == null || songsToBuyList.size() == 0) { Logger.warn(agent, "Hiç müzik bulunamadı."); return; }
         Logger.info(agent, "Ordering song purchase...");
         for(SongSellInfo ssi: songsToBuyList) {
           ACLMessage msg = new ACLMessage(ACLMessage.AGREE);
           msg.addReceiver(ssi.getSellerAgent());
           try {
             msg.setContentObject(ssi);
             this.myAgent.send(msg);
           } catch (Exception e) {
             Logger.error(agent, e, "Couldn't send song purchase request.");
           }
         }
      }
    }
    
    private class ListenBuyMusicAnswers extends SimpleBehaviour {
      private int songCount = 0;
      private final long TIMEOUT_MS = 15000;
      private final long WAIT_MS = 1000;
      private long startTime;
      private boolean isFirstRun = true;
      
      public ListenBuyMusicAnswers() {
        super();
      }
      
      @Override
      public void action() {
        if(isFirstRun) { startTime = System.currentTimeMillis(); } 
        if(songsToBuy.size() == 0) { return; }
        Logger.info(agent, "Waiting for song purchase results... (%s / %s)", songCount, songsToBuy.size());
        ACLMessage msg = this.myAgent.receive();
        if (msg == null) { block(WAIT_MS); return; }
        
        try {
          if(!msg.getContentObject().getClass().equals(Tuple.class)) {
            Logger.warn(agent, "Agent %s sent an unexpected message.", msg.getSender().getName());
            return;
          }
          
          songCount++;
          
          if(msg.getPerformative() == ACLMessage.REFUSE) {
            Logger.warn(agent, "%s refused me!", msg.getSender().getName());
            return;
          }
          
          Tuple<String, SongSellInfo> urlRequest = (Tuple<String, SongSellInfo>) msg.getContentObject();
          final String url = urlRequest._1;
          final SongSellInfo songInfo = urlRequest._2;
          if(Utils.isBlank(url) || songInfo == null) { 
            Logger.warn(agent, "Agent %s didn't return order result info, buying song failed.", msg.getSender().getName());
            return;
          }
          
          Logger.info(agent, "Satın alındı: %s [URL: %s]", songInfo.toString(), url);
          
          Runnable addIt = new Runnable() { 
            @Override
            public void run() {
              ui.addMessageToConsole(songInfo.toString() + " [URL: " + url + "]");
            }
          };
         
          SwingUtilities.invokeLater(addIt);
        } catch (Exception e) {
          Logger.error(agent, e, "Couldn't purchase song.");
        }
      }

      @Override
      public boolean done() {
        Runnable enableUI = new Runnable() { 
          @Override
          public void run() {
            ui.enableUI();
          }
        };
        
        if(System.currentTimeMillis() - startTime > TIMEOUT_MS) {
          Logger.warn(agent, "Timeout occured while waiting for response.");
          SwingUtilities.invokeLater(enableUI);
          return true;
        }
        
        if(songCount >= songsToBuy.size()) {
          SwingUtilities.invokeLater(enableUI);
          return true;
        }
        
        return false;
      }
    }
  }
  
  private final class UpdateMusicDiscoveryAgents extends TickerBehaviour {

    public UpdateMusicDiscoveryAgents() {
      super(agent, 10000);
    }

    @Override
    protected void onTick() {
      //Logger.info(agent, "Müzik satma hizmeti sunan etmen listesi güncelleniyor...");
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
        //Logger.info(agent, "Müzik satma hizmeti sunan etmenler güncelledi. %s etmen bulundu.", result.length);
      } catch (FIPAException e) {
        Logger.error(agent,  e, "An error occured while updating music provider agent list from DF.");
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
