package view;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import misc.Logger;
import pojo.Song;
import pojo.Song.Genre;
import pojo.SongSellInfo;
import util.Utils;
import agent.MusicProvider;

public class ProviderView extends JFrame {

  JPanel contentPane;
  JTextField txtArtist;
  JTextField txtMusicName;
  JTextField txtPrice;
  JTextField txtRating;
  JComboBox txtGenre;
  List lstSold;
  JList lstMusic;
  DefaultListModel lstMusicList;

  /**
   * Create the frame.
   * @param runnable 
   */
  public ProviderView(final MusicProvider agent) {
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        agent.addBehaviour(agent.new ShutdownAgent());
      }
    });
    setTitle("Provider: " + agent.getLocalName());
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setBounds(100, 100, 700, 561);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);
    contentPane.setLayout(null);
    
    JLabel lblInfo = new JLabel("Satışta olan müzikler:");
    lblInfo.setBounds(6, 6, 152, 21);
    lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
    lblInfo.setVerticalAlignment(SwingConstants.TOP);
    contentPane.add(lblInfo);
    
    JLabel lblInfo2 = new JLabel("Yeni Ekle:");
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
        
       Genre genre = (Song.Genre)txtGenre.getSelectedItem();
       String artist = txtArtist.getText();
       String musicName = txtMusicName.getText();
       float rating, price;
       try {
         rating = Integer.parseInt(txtRating.getText());
         price = Float.parseFloat(txtPrice.getText());
       } catch (NumberFormatException ex) {
         Logger.error(agent, ex, "Sayılar problemli.");
         return;
       }
       
       if(Utils.isBlank(artist) || Utils.isBlank(musicName) || rating < 0 || rating > 5 || price < 0) {
         Logger.error(agent, "Eksik veriler var.");
         return;
       }
       
       txtArtist.setText("");
       txtMusicName.setText("");
       txtRating.setText("");
       txtPrice.setText("");
       
       Song s = new Song(artist, musicName, genre);
       SongSellInfo ssi = new SongSellInfo(rating, price, agent.getAID(), s);
       
       if(lstMusicList.contains(ssi)) { Logger.warn(agent, "Bu parça zaten var?"); return; }
       
       lstMusicList.addElement(ssi);
       
       agent.addBehaviour(agent.new AddSong(ssi));
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
    
    JButton btnDelete = new JButton("Seçiliyi Sil");
    btnDelete.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        SongSellInfo ssi = (SongSellInfo) lstMusic.getSelectedValue();
        
        if(ssi == null) {
          Logger.warn(agent, "Seçim yok?");
          return;
        }
        
        lstMusicList.removeElement(ssi);
        agent.addBehaviour(agent.new RemoveSong(ssi));
      }
    });
    btnDelete.setBounds(6, 504, 410, 29);
    contentPane.add(btnDelete);
    
    JLabel lblInfo3 = new JLabel("Satılanlar:");
    lblInfo3.setVerticalAlignment(SwingConstants.TOP);
    lblInfo3.setHorizontalAlignment(SwingConstants.LEFT);
    lblInfo3.setBounds(422, 228, 81, 21);
    contentPane.add(lblInfo3);
    
    lstSold = new List();
    lstSold.setBounds(430, 255, 264, 243);
    contentPane.add(lstSold);
    
    lstMusic = new JList();
    lstMusic.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    lstMusicList = new DefaultListModel();
    
    lstMusic.setBounds(16, 27, 394, 471);
    lstMusic.setModel(lstMusicList);
    
    contentPane.add(lstMusic);
    
    for(Song.Genre g: Song.Genre.values()) {
      txtGenre.addItem(g);
    }
  }

  public void addBuyedItem(String msg) {
    lstSold.add(msg);
  }
}
