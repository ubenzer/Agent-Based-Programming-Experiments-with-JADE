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
import pojo.Song.Genre;
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
  
  public final class FindAndPurchaseMusics extends SequentialBehaviour {
    private Set<DFAgentDescription> knownAgentsAtTimeBehaviourStarted;
    private Set<SongSellInfo> songOffers = new HashSet<SongSellInfo>();
    private Set<SongSellInfo> songsToBuy;
    
    public FindAndPurchaseMusics(Song.Genre genre, float maxBudgetPerSongI, int maxSongCountI, int minRatingI, float totalBudgetI) {
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
     public LookForMusic(Song.Genre genre, float maxBudgetPerSong, int minRating) {
       this.genre = genre;
       this.maxBudgetPerSong = maxBudgetPerSong;
       this.minRating = minRating;
     }

    @Override
    public void action() {
      Logger.info(agent, "Müzik satıcılarına verilen kriterlere uygun bir müzik listesi isteği yollanıyor...");
      ACLMessage msg = new ACLMessage(ACLMessage.CFP);
      for(DFAgentDescription df: knownAgentsAtTimeBehaviourStarted) {
        msg.addReceiver(df.getName());
      }
      try {
        msg.setContent("MUSIC-SEARCH");
        msg.setContentObject(new SongRequestInfo(genre, maxBudgetPerSong, minRating));
        this.myAgent.send(msg);
      } catch (Exception e) {
        Logger.error(agent, e, "Müzik satıcılarına müzik arama isteği yollanamadı.");
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
        Logger.info(agent, "Müzik arama sonuçları alınıyor... (%s / %s)", answerCount, knownAgentsAtTimeBehaviourStarted.size());
        ACLMessage msg = this.myAgent.receive();
        if (msg == null) { block(WAIT_MS); return; }
        
        answerCount++;
        
        if(msg.getPerformative() == ACLMessage.REFUSE) {
          Logger.warn(agent, "%s beni reddetti!", msg.getSender().getName());
          return;
        }
        
        if(!"MUSIC-SEARCH".equals(msg.getContent())) {
          Logger.warn(agent, "%s şu anda beklemediğim bir mesaj attı bana.", msg.getSender().getName());
          return;
        }
        
        try {
          HashSet<SongSellInfo> songListReturned = (HashSet<SongSellInfo>) msg.getContentObject();
          if(songListReturned == null || songListReturned.size() == 0) { 
            Logger.warn(agent, "%s isimli etmen boş liste gönderdi.", msg.getSender().getName());
            return;
          }
          
          for(SongSellInfo s: songListReturned) {
            /* We take security serious */
            if(!s.getSellerAgent().equals(msg.getSender())) {
              Logger.error(agent, "Kimlik uyuşmazlığı tespit edildi! %s 'ten gelen bilgiler yok sayılıyor.", msg.getSender().getName());
              return;
            }
          }
          
          songOffers.addAll(songListReturned);
          
        } catch (Exception e) {
          Logger.error(agent, e, "Listeyi alamadım.");
        }
       
      }

      @Override
      public boolean done() {
        if(System.currentTimeMillis() - startTime > TIMEOUT_MS) {
          Logger.warn(agent, "Timeout occured when waiting for answers!");
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
      private int minRatingI;
      private float totalBudgetI;
      
      public SelectMusic(Set<SongSellInfo> songOffers, Song.Genre genre, float maxBudgetPerSongI, int maxSongCountI, int minRatingI, float totalBudgetI) {
        this.songsProposed = songOffers;
        this.genre = genre;
        this.maxBudgetPerSongI = maxBudgetPerSongI;
        this.maxSongCountI = maxSongCountI;
        this.minRatingI = minRatingI;
        this.totalBudgetI = totalBudgetI;
      }

      @Override
      public void action() {
        HashMap<SongSellInfo, Float> filteredSongList = new HashMap<SongSellInfo, Float>();
        float totalPrice = 0;
        
        /* Elaminate unmatched songs and find min prica and max rating for valid songs */
        Iterator<SongSellInfo> iter = songsProposed.iterator();
        while (iter.hasNext()) {
          SongSellInfo oneOffer = iter.next();
          Song oneOfferSong = oneOffer.getSong();
          
          if(!this.genre.equals(oneOffer.getSong().getGenre()) || oneOffer.getAvgRating() < this.minRatingI || oneOffer.getPrice() > this.maxBudgetPerSongI) {
            Logger.info(agent, "%s elendi, şartlar uygun değil.", oneOffer);
            continue;
          }
          
          SongSellInfo tbAdded = oneOffer;
          Float maxRating = oneOffer.getAvgRating();
          
          Iterator<SongSellInfo> innerIter = songsProposed.iterator();
          while (innerIter.hasNext()) {
            SongSellInfo oneOfferInner = iter.next();
            Song oneOfferSongInner = oneOffer.getSong();
            
            if(oneOfferInner.equals(oneOffer) || !oneOfferSongInner.equals(oneOfferSong)) {
              continue;
            }
            
            if(maxRating < oneOfferInner.getAvgRating()) {
              maxRating = oneOfferInner.getAvgRating();
            }
            if(oneOfferInner.getPrice() < tbAdded.getPrice()) {
              tbAdded = oneOfferInner;
            }
          }
          filteredSongList.put(tbAdded, maxRating);
          totalPrice += tbAdded.getPrice();
        }
        
        if(filteredSongList.size() > this.maxSongCountI) {
          Iterator<Map.Entry<SongSellInfo, Float>> i = filteredSongList.entrySet().iterator();
          while (filteredSongList.size() > this.maxSongCountI) {
            Map.Entry<SongSellInfo, Float> itemToDelete = i.next();
            totalPrice -= itemToDelete.getKey().getPrice();
            i.remove();
          }
        }
        
        if(totalPrice > this.totalBudgetI) {
          Iterator<Map.Entry<SongSellInfo, Float>> i = filteredSongList.entrySet().iterator();
          while (totalPrice > this.totalBudgetI) {
            Map.Entry<SongSellInfo, Float> itemToDelete = i.next();
            totalPrice -= itemToDelete.getKey().getPrice();
            i.remove();
          }
        }
        
        songsToBuy = filteredSongList.keySet();
      }
    }
  
    private class BuyMusic extends OneShotBehaviour {
      private Set<SongSellInfo> songsToBuyList;
      public BuyMusic(Set<SongSellInfo> songsToBuy) {
        this.songsToBuyList = songsToBuy;
      }

      @Override
      public void action() {
         Logger.info(agent, "Satın alınmasına karar verilen müziklerin siparişi veriliyor...");
         for(SongSellInfo ssi: songsToBuyList) {
           ACLMessage msg = new ACLMessage(ACLMessage.AGREE);
           msg.addReceiver(ssi.getSellerAgent());
           msg.setContent("BUY-MUSIC");
           try {
             msg.setContentObject(ssi);
             this.myAgent.send(msg);
           } catch (Exception e) {
             Logger.error(agent, e, "Müzik satın alma isteği yollanamadı.");
           }
         }
      }
    }
    
    private class ListenBuyMusicAnswers extends SimpleBehaviour {
      private int answerCount = 0;
      private final long TIMEOUT_MS = 15000;
      private final long WAIT_MS = 1000;
      private final long startTime;
      
      public ListenBuyMusicAnswers() {
        super();
        startTime = System.currentTimeMillis();
      }
      
      @Override
      public void action() {
        Logger.info(agent, "Müzik satın alma istekleri sonucu bekleniyor... (%s / %s)", answerCount, songsToBuy.size());
        ACLMessage msg = this.myAgent.receive();
        if (msg == null) { block(WAIT_MS); return; }
        
        answerCount++;
        
        if(msg.getPerformative() == ACLMessage.REFUSE) {
          Logger.warn(agent, "%s beni reddetti!", msg.getSender().getName());
          return;
        }
        
        if(!"BUY-MUSIC".equals(msg.getContent())) {
          Logger.warn(agent, "%s şu anda beklemediğim bir mesaj attı bana.", msg.getSender().getName());
          return;
        }
        
        try {
          Tuple<String, SongSellInfo> urlRequest = (Tuple<String, SongSellInfo>) msg.getContentObject();
          final String url = urlRequest._1;
          final SongSellInfo songInfo = urlRequest._2;
          if(Utils.isBlank(url) || songInfo == null) { 
            Logger.warn(agent, "%s isimli etmen müzik bilgisini vermedi, satın alamadım.", msg.getSender().getName());
            return;
          }
          
          Logger.info(agent, "Song: %s - %s R: %s P: %s - URL: %s", songInfo.getSong().getArtist(), songInfo.getSong().getName(), songInfo.getAvgRating(), songInfo.getPrice(), url);
          
          Runnable addIt = new Runnable() { 
            @Override
            public void run() {
              ui.addMessageToConsole("Agent: " + songInfo.getSellerAgent().getName() + " " +
                                     "Song: " + songInfo.getSong().getArtist() + " - " + songInfo.getSong().getName() + " " +
                                     "R: " + songInfo.getAvgRating() + " P:" + songInfo.getPrice() + " URL: " + url);
            }
          };
         
          SwingUtilities.invokeLater(addIt);   
        } catch (Exception e) {
          Logger.error(agent, e, "Şarkıyı alamadım.");
        }
      }

      @Override
      public boolean done() {
        if(System.currentTimeMillis() - startTime > TIMEOUT_MS) {
          Logger.warn(agent, "Timeout occured when waiting for answers!");
          return true;
        }
        
        if(answerCount >= knownAgentsAtTimeBehaviourStarted.size()) {
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
