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

import pojo.Song;
import pojo.Song.Genre;

import agent.MusicSeeker;


public class ProviderView extends JFrame {

  JPanel contentPane;
  JTextField txtArtist;
  JTextField txtMusicName;
  JTextField txtPrice;
  JTextField txtRating;
  JComboBox txtGenre;
  List console;
  
  public void addMessageToConsole(String message) {
    console.add(message);
  }
  
  /**
   * Create the frame.
   * @param runnable 
   */
  public ProviderView(final MusicSeeker agent) {
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        agent.addBehaviour(agent.new ShutdownAgent());
      }
    });
    setTitle("Provider: " + agent.getLocalName());
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setBounds(100, 100, 700, 409);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);
    contentPane.setLayout(null);
    
    JLabel lblInfo = new JLabel("Satışta olan müzikler:");
    lblInfo.setBounds(6, 6, 152, 21);
    lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
    lblInfo.setVerticalAlignment(SwingConstants.TOP);
    contentPane.add(lblInfo);
    
    JLabel lblInfo2 = new JLabel("Yeni Ekle");
    lblInfo2.setBounds(422, 6, 63, 21);
    lblInfo2.setVerticalAlignment(SwingConstants.TOP);
    lblInfo2.setHorizontalAlignment(SwingConstants.LEFT);
    contentPane.add(lblInfo2);
    
    JLabel lblGenre = new JLabel("Müzik tipi:");
    lblGenre.setBounds(422, 39, 81, 16);
    lblGenre.setHorizontalAlignment(SwingConstants.RIGHT);
    contentPane.add(lblGenre);
    
    JLabel lblRating = new JLabel("Rating:");
    lblRating.setBounds(422, 159, 81, 16);
    lblRating.setHorizontalAlignment(SwingConstants.RIGHT);
    contentPane.add(lblRating);
    
    JLabel lblPrice = new JLabel("Fiyat:");
    lblPrice.setBounds(422, 129, 81, 16);
    lblPrice.setHorizontalAlignment(SwingConstants.RIGHT);
    contentPane.add(lblPrice);
    
    JLabel lblMusicName = new JLabel("Müzik adı:");
    lblMusicName.setBounds(422, 99, 81, 16);
    lblMusicName.setHorizontalAlignment(SwingConstants.RIGHT);
    contentPane.add(lblMusicName);
    
    JLabel lblArtist = new JLabel("Artist:");
    lblArtist.setBounds(422, 71, 81, 16);
    lblArtist.setHorizontalAlignment(SwingConstants.RIGHT);
    contentPane.add(lblArtist);
    
    final JButton btnAdd = new JButton("Ekle");
    btnAdd.setBounds(515, 184, 179, 29);
    btnAdd.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        
        btnAdd.setEnabled(false);
 
       Song.Genre genre = (Song.Genre)txtGenre.getSelectedItem();
       int minRatingI, maxSongCountI;
       float maxBudgetPerSongI, totalBudgetI;
       try {
         minRatingI = Integer.parseInt(txtArtist.getText());
         maxSongCountI = Integer.parseInt(txtRating.getText());
         maxBudgetPerSongI = Float.parseFloat(txtMusicName.getText());
         totalBudgetI = Float.parseFloat(txtPrice.getText());
       } catch (NumberFormatException e1) {
        console.add("Sayılar adam gibi diil.");
        e1.printStackTrace();
        return;
       }
       
       if(totalBudgetI < maxBudgetPerSongI) {
         console.add("Aga o paraya müziği ben nerden bulam?");
         return;
       }
       
       agent.addBehaviour(agent.new FindAndPurchaseMusics(genre, maxBudgetPerSongI, maxSongCountI, minRatingI, totalBudgetI));
      }
    });
    contentPane.add(btnAdd);
    
    txtGenre = new JComboBox();
    txtGenre.setBounds(515, 33, 179, 28);
    contentPane.add(txtGenre);
    
    txtArtist = new JTextField();
    txtArtist.setBounds(515, 63, 179, 28);
    contentPane.add(txtArtist);
    txtArtist.setColumns(10);
    
    txtMusicName = new JTextField();
    txtMusicName.setBounds(515, 93, 179, 28);
    txtMusicName.setColumns(10);
    contentPane.add(txtMusicName);
    
    txtPrice = new JTextField();
    txtPrice.setBounds(516, 123, 179, 28);
    txtPrice.setColumns(10);
    contentPane.add(txtPrice);
    
    txtRating = new JTextField();
    txtRating.setBounds(515, 153, 179, 28);
    txtRating.setColumns(10);
    contentPane.add(txtRating);
    
    console = new List();
    console.setBounds(10, 27, 406, 350);
    contentPane.add(console);
    
    JButton btnDelete = new JButton("Seçiliyi Sil");
    btnDelete.setBounds(422, 348, 272, 29);
    contentPane.add(btnDelete);
    
    for(Song.Genre g: Song.Genre.values()) {
      txtGenre.addItem(g);
    }
  }
}
