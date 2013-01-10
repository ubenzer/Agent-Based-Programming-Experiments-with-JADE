package pojo;

public class Song implements Cloneable {

  private final String artist;
  private final String name;
  private final Song.Genre genre;
  
  public Song(String artist, String name, Song.Genre genre) {
    super();
    this.artist = artist;
    this.name = name;
    this.genre = genre;
  }
  
  public String getArtist() {
    return this.artist;
  }
  public String getName() {
    return this.name;
  }
  public Song.Genre getGenre() {
    return this.genre;
  }

  @Override
  public String toString() {
    return "Song [artist=" + this.artist + ", name=" + this.name + ", genre=" + this.genre + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.artist == null) ? 0 : this.artist.toLowerCase().hashCode());
    result = prime * result + ((this.genre == null) ? 0 : this.genre.hashCode());
    result = prime * result + ((this.name == null) ? 0 : this.name.toLowerCase().hashCode());
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
    Song other = (Song) obj;
    if (this.artist == null) {
      if (other.artist != null) {
        return false;
      }
    } else if (!this.artist.equalsIgnoreCase(other.artist)) {
      return false;
    }
    if (this.genre != other.genre) {
      return false;
    }
    if (this.name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!this.name.equalsIgnoreCase(other.name)) {
      return false;
    }
    return true;
  }

  public static enum Genre { POP, CLASSIC, JAZZ, ROCK, COUNTRY }
}
