package view;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import misc.Logger;
import pojo.Song;
import agent.MusicSeeker;

public class MusicView extends JFrame {

  JPanel contentPane;
  JTextField minRating;
  JTextField maxBudgetPerSong;
  JTextField totalBudget;
  JTextField maxSongCount;
  JComboBox musicType;
  List console;
  JButton search;
  
  public void addMessageToConsole(String message) {
    console.add(message);
  }
  
  /**
   * Create the frame.
   * @param runnable 
   */
  public MusicView(final MusicSeeker agent) {
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        agent.addBehaviour(agent.new ShutdownAgent());
      }
    });
    setTitle("Seeker: " + agent.getLocalName());
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setBounds(100, 100, 618, 416);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);
    contentPane.setLayout(null);
    
    JLabel lblInfo = new JLabel("Merhaba. Ben senin müzik bulucu etmeninim. Bana aşağıdaki bilgileri ver.");
    lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
    lblInfo.setVerticalAlignment(SwingConstants.TOP);
    lblInfo.setBounds(6, 6, 628, 21);
    contentPane.add(lblInfo);
    
    JLabel lblInfo2 = new JLabel("Ben senin için tüm müzik satıcılarını gezerim.");
    lblInfo2.setVerticalAlignment(SwingConstants.TOP);
    lblInfo2.setHorizontalAlignment(SwingConstants.CENTER);
    lblInfo2.setBounds(6, 23, 628, 21);
    contentPane.add(lblInfo2);
    
    JLabel lblGenre = new JLabel("Aradığın müzik tipi:");
    lblGenre.setHorizontalAlignment(SwingConstants.RIGHT);
    lblGenre.setBounds(16, 56, 170, 16);
    contentPane.add(lblGenre);
    
    JLabel lblMinRating = new JLabel("En az rating:");
    lblMinRating.setHorizontalAlignment(SwingConstants.RIGHT);
    lblMinRating.setBounds(16, 81, 170, 16);
    contentPane.add(lblMinRating);
    
    JLabel lblPricePerMusic = new JLabel("Müzik başına max bütçe:");
    lblPricePerMusic.setHorizontalAlignment(SwingConstants.RIGHT);
    lblPricePerMusic.setBounds(16, 109, 170, 16);
    contentPane.add(lblPricePerMusic);
    
    JLabel lblTotalBudget = new JLabel("Toplam bütçe:");
    lblTotalBudget.setHorizontalAlignment(SwingConstants.RIGHT);
    lblTotalBudget.setBounds(16, 137, 170, 16);
    contentPane.add(lblTotalBudget);
    
    JLabel lblMaxSongCount = new JLabel("Max. şarkı sayısı:");
    lblMaxSongCount.setHorizontalAlignment(SwingConstants.RIGHT);
    lblMaxSongCount.setBounds(16, 165, 170, 16);
    contentPane.add(lblMaxSongCount);
    
    search = new JButton("Buluve");
    search.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        
        search.setEnabled(false);
 
       Song.Genre genre = (Song.Genre)musicType.getSelectedItem();
       int minRatingI, maxSongCountI;
       float maxBudgetPerSongI, totalBudgetI;
       try {
         minRatingI = Integer.parseInt(minRating.getText());
         maxSongCountI = Integer.parseInt(maxSongCount.getText());
         maxBudgetPerSongI = Float.parseFloat(maxBudgetPerSong.getText());
         totalBudgetI = Float.parseFloat(totalBudget.getText());
       } catch (NumberFormatException ex) {
        console.add("Sayılar adam gibi diil.");
        Logger.error(agent, ex, "Sayılar problemli.");
        return;
       }
       
       if(totalBudgetI < maxBudgetPerSongI) {
         console.add("Aga o paraya müziği ben nerden bulam?");
         Logger.error(agent, "Toplam bütçe bir şarkınınkinden küçük.");
         return;
       }
       
       disableUI();
       
       agent.addBehaviour(agent.new FindAndPurchaseMusics(genre, maxBudgetPerSongI, maxSongCountI, minRatingI, totalBudgetI));
      }
    });
    search.setBounds(431, 160, 170, 29);
    contentPane.add(search);
    
    musicType = new JComboBox();
    musicType.setBounds(198, 52, 221, 27);
    contentPane.add(musicType);
    
    minRating = new JTextField();
    minRating.setBounds(198, 75, 221, 28);
    contentPane.add(minRating);
    minRating.setColumns(10);
    
    maxBudgetPerSong = new JTextField();
    maxBudgetPerSong.setColumns(10);
    maxBudgetPerSong.setBounds(198, 103, 221, 28);
    contentPane.add(maxBudgetPerSong);
    
    totalBudget = new JTextField();
    totalBudget.setColumns(10);
    totalBudget.setBounds(198, 131, 221, 28);
    contentPane.add(totalBudget);
    
    maxSongCount = new JTextField();
    maxSongCount.setColumns(10);
    maxSongCount.setBounds(198, 159, 221, 28);
    contentPane.add(maxSongCount);
    
    console = new List();
    console.setBounds(16, 195, 594, 189);
    contentPane.add(console);
    
    for(Song.Genre g: Song.Genre.values()) {
      musicType.addItem(g);
    }
  }

  public void enableUI() {
    minRating.setEnabled(true);
    maxBudgetPerSong.setEnabled(true);
    totalBudget.setEnabled(true);
    maxSongCount.setEnabled(true);
    musicType.setEnabled(true);
    search.setEnabled(true);
  }
  
  public void disableUI() {
    minRating.setEnabled(false);
    maxBudgetPerSong.setEnabled(false);
    totalBudget.setEnabled(false);
    maxSongCount.setEnabled(false);
    musicType.setEnabled(false);
    search.setEnabled(false);
  }
  
  
}
