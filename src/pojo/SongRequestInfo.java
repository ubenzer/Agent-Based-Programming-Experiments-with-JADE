package pojo;

import java.io.Serializable;

import agent.MusicProvider.Genre;

public class SongRequestInfo implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -4333298041938820117L;
  public Genre genre;
  public float maxPricePerSong;
  public float minRating;
  
  public SongRequestInfo(Genre genre, float maxPricePerSong, float minRating) {
    super();
    this.genre = genre;
    this.maxPricePerSong = maxPricePerSong;
    this.minRating = minRating;
  }

}
