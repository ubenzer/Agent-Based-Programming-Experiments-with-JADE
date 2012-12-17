package view;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import agent.MusicProvider.Genre;
import agent.MusicSeeker;
import behaviour.LookForMusic;


public class MusicView extends JFrame {

  /**
   * 
   */
  private static final long serialVersionUID = -1778944522529076269L;
  JPanel contentPane;
  JTextField minRating;
  JTextField maxBudgetPerSong;
  JTextField totalBudget;
  JTextField maxSongCount;
  JComboBox musicType;
  List console;
  JTextField maxSearchTime;
  /**
   * Create the frame.
   * @param runnable 
   */
  public MusicView(final MusicSeeker agent) {
    setTitle("Agent");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setBounds(100, 100, 640, 480);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);
    contentPane.setLayout(null);
    
    JLabel lblNewLabel = new JLabel("Merhaba. Ben senin müzik bulucu etmeninim. Bana aşağıdaki bilgileri ver.");
    lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
    lblNewLabel.setVerticalAlignment(SwingConstants.TOP);
    lblNewLabel.setBounds(6, 6, 628, 21);
    contentPane.add(lblNewLabel);
    
    JLabel label = new JLabel("Ben senin için tüm müzik satıcılarını gezerim.");
    label.setVerticalAlignment(SwingConstants.TOP);
    label.setHorizontalAlignment(SwingConstants.CENTER);
    label.setBounds(6, 23, 628, 21);
    contentPane.add(label);
    
    JLabel lblAradnMzikTipi = new JLabel("Aradığın müzik tipi:");
    lblAradnMzikTipi.setHorizontalAlignment(SwingConstants.RIGHT);
    lblAradnMzikTipi.setBounds(16, 56, 170, 16);
    contentPane.add(lblAradnMzikTipi);
    
    JLabel label_1 = new JLabel("En az rating:");
    label_1.setHorizontalAlignment(SwingConstants.RIGHT);
    label_1.setBounds(16, 81, 170, 16);
    contentPane.add(label_1);
    
    JLabel label_2 = new JLabel("Müzik başına max bütçe:");
    label_2.setHorizontalAlignment(SwingConstants.RIGHT);
    label_2.setBounds(16, 109, 170, 16);
    contentPane.add(label_2);
    
    JLabel label_3 = new JLabel("Toplam bütçe:");
    label_3.setHorizontalAlignment(SwingConstants.RIGHT);
    label_3.setBounds(16, 137, 170, 16);
    contentPane.add(label_3);
    
    JLabel label_4 = new JLabel("Max. şarkı sayısı:");
    label_4.setHorizontalAlignment(SwingConstants.RIGHT);
    label_4.setBounds(16, 165, 170, 16);
    contentPane.add(label_4);
    
    JButton search = new JButton("Buluve");
    search.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(@SuppressWarnings("unused") ActionEvent e) {
 
       Genre genre = (Genre)musicType.getSelectedItem();
       int minRatingI, maxSongCountI;
       long maxTimeL;
       float maxBudgetPerSongI, totalBudgetI;
       try {
         minRatingI = Integer.parseInt(minRating.getText());
         maxSongCountI = Integer.parseInt(maxSongCount.getText());
         maxBudgetPerSongI = Float.parseFloat(maxBudgetPerSong.getText());
         totalBudgetI = Float.parseFloat(totalBudget.getText());
         maxTimeL = Long.parseLong(maxSearchTime.getText());
       } catch (NumberFormatException e1) {
        console.add("Sayılar adam gibi diil.");
        e1.printStackTrace();
        return;
       }
       
       if(totalBudgetI < maxBudgetPerSongI) {
         console.add("Aga o paraya müziği ben nerden bulam?");
         return;
       }
       
       agent.addBehaviour(new LookForMusic(agent, genre, maxBudgetPerSongI, maxSongCountI, maxBudgetPerSongI, minRatingI, maxTimeL));
      }
    });
    search.setBounds(6, 224, 117, 29);
    contentPane.add(search);
    
    JButton cancel = new JButton("Daha fazla bulma, iptal");
    cancel.setEnabled(false);
    cancel.setBounds(124, 224, 182, 29);
    contentPane.add(cancel);
    
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
    console.setBounds(16, 259, 594, 189);
    contentPane.add(console);
    
    JLabel lblEnFazlaArama = new JLabel("En fazla arama süresi (sn):");
    lblEnFazlaArama.setHorizontalAlignment(SwingConstants.RIGHT);
    lblEnFazlaArama.setBounds(16, 193, 170, 16);
    contentPane.add(lblEnFazlaArama);
    
    maxSearchTime = new JTextField();
    maxSearchTime.setColumns(10);
    maxSearchTime.setBounds(198, 187, 221, 28);
    contentPane.add(maxSearchTime);
    
    for(Genre g: Genre.values()) {
      musicType.addItem(g);
    }
  }
}
