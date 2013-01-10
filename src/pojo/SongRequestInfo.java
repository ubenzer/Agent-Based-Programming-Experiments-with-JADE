package pojo;

import java.io.Serializable;

import pojo.Song.Genre;


public class SongRequestInfo implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -4333298041938820117L;
  public Song.Genre genre;
  public float maxPricePerSong;
  public float minRating;
  
  public SongRequestInfo(Song.Genre genre, float maxPricePerSong, float minRating) {
    super();
    this.genre = genre;
    this.maxPricePerSong = maxPricePerSong;
    this.minRating = minRating;
  }

}
