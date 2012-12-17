package pojo;


public class SongSellInfo {

  private final float avgRating;
  private final float price;
  
  public SongSellInfo(float avgRating, float price) {
    super();
    this.avgRating = avgRating;
    this.price = price;
  }
  
  public float getAvgRating() {
    return this.avgRating;
  }
  public float getPrice() {
    return this.price;
  }
}
