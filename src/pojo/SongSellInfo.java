package pojo;

import jade.core.AID;

import java.io.Serializable;

public class SongSellInfo implements Serializable, Cloneable {

  private final float avgRating;
  private final float price;
  private final AID sellerAgent;
  private final Song song;
  
  public SongSellInfo(float avgRating, float price, AID sellerAgent, Song song) {
    super();
    this.avgRating = avgRating;
    this.price = price;
    this.sellerAgent = sellerAgent;
    this.song = song;
  }
  public SongSellInfo(float avgRating, float price) {
    this(avgRating, price, null, null);
  }
  
  public float getAvgRating() {
    return this.avgRating;
  }
  public float getPrice() {
    return this.price;
  }
  public AID getSellerAgent() {
    return sellerAgent;
  }
  public Song getSong() {
    return song;
  }
 
  @Override
  public String toString() {
    return "SongSellInfo [avgRating=" + this.avgRating + ", price=" + this.price + ", sellerAgent=" + this.sellerAgent + ", song=" + this.song + "]";
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.sellerAgent == null) ? 0 : this.sellerAgent.hashCode());
    result = prime * result + ((this.song == null) ? 0 : this.song.hashCode());
    return result;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    SongSellInfo other = (SongSellInfo) obj;
    if (this.sellerAgent == null) {
      if (other.sellerAgent != null) {
        return false;
      }
    } else if (!this.sellerAgent.equals(other.sellerAgent)) {
      return false;
    }
    if (this.song == null) {
      if (other.song != null) {
        return false;
      }
    } else if (!this.song.equals(other.song)) {
      return false;
    }
    return true;
  }
  
  @Override
  public SongSellInfo clone() {
    return new SongSellInfo(this.getAvgRating(), this.getPrice(), this.getSellerAgent(), this.getSong());
  }
  
 
}
