package database.mail;

import java.awt.GridLayout;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import RicercaFile.FileDialogWindows;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

abstract class FinestraApplicativa {

	protected JFrame FinestraComando;
	protected JTextField usrtxt;
	protected JTextField mittentetxt;
	protected JTextField destinatariotxt;
	protected JTextField destinatarioCCtxt;
	protected JTextField oggettotxt;
	protected JTextArea corpomailtxt;
	private JPanel panel;
	protected JButton btnEstraiDati;
	protected JButton btnSimula;
	protected FileDialogWindows trovafileAccess;


	/**
	 * Costruttore Finestra applicativa
	 */
	public FinestraApplicativa() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		FinestraComando = new JFrame("Finestra di comando");
		FinestraComando.setBounds(100, 100, 718, 713);
		FinestraComando.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		FinestraComando.getContentPane().setLayout(null);
		
		usrtxt = new JTextField();
		usrtxt.setToolTipText("Inserire la matricola TIM");
		//usrtxt.setText("08043160");
		usrtxt.setBounds(246, 41, 87, 26);
		FinestraComando.getContentPane().add(usrtxt);
		usrtxt.setColumns(10);
		
		JLabel lblUser = new JLabel("User");
		lblUser.setBounds(68, 44, 69, 20);
		FinestraComando.getContentPane().add(lblUser);
		
		mittentetxt = new JTextField();
		mittentetxt.setToolTipText("Inserire mittente della mail");
		//mittentetxt.setText("matteo.bassi@telecomitalia.it");
		mittentetxt.setBounds(246, 97, 230, 26);
		FinestraComando.getContentPane().add(mittentetxt);
		mittentetxt.setColumns(10);
		
		JLabel lblMittente = new JLabel("Mittente");
		lblMittente.setBounds(68, 100, 69, 20);
		FinestraComando.getContentPane().add(lblMittente);
		
		destinatariotxt = new JTextField();
		destinatariotxt.setToolTipText("Destinatari A:");
		//destinatariotxt.setText("matteo.bassi@telecomitalia.it");
		destinatariotxt.setBounds(246, 161, 230, 26);
		FinestraComando.getContentPane().add(destinatariotxt);
		destinatariotxt.setColumns(10);
		
		JLabel lblDestinatario = new JLabel("Destinatario A:");
		lblDestinatario.setBounds(68, 164, 163, 20);
		FinestraComando.getContentPane().add(lblDestinatario);
		
		destinatarioCCtxt = new JTextField();
		destinatarioCCtxt.setToolTipText("Destinatari CC");
		destinatarioCCtxt.setBounds(246, 233, 230, 26);
		FinestraComando.getContentPane().add(destinatarioCCtxt);
		destinatarioCCtxt.setColumns(10);
		
		JLabel lblDestinatarioCc = new JLabel("Destinatario CC:");
		lblDestinatarioCc.setBounds(68, 236, 130, 20);
		FinestraComando.getContentPane().add(lblDestinatarioCc);
		
		oggettotxt = new JTextField();
		oggettotxt.setToolTipText("Oggetto della mail");
		oggettotxt.setBounds(246, 306, 230, 26);
		FinestraComando.getContentPane().add(oggettotxt);
		oggettotxt.setColumns(10);
		
		JLabel lblOggetto = new JLabel("Oggetto");
		lblOggetto.setBounds(68, 309, 69, 20);
		FinestraComando.getContentPane().add(lblOggetto);
		
		panel= new JPanel();
		panel.setBounds(15, 348, 666, 180);
		panel.setLayout(new GridLayout(1,1));
		panel.setBorder(new TitledBorder ( new EtchedBorder (), "Corpo della mail" ));
		
		
		corpomailtxt = new JTextArea();
		corpomailtxt.setLineWrap(true);
		corpomailtxt.setToolTipText("Corpo della mail");
		corpomailtxt.setBounds(247, 367, 392, 161);
	    JScrollPane scroll = new JScrollPane ( corpomailtxt );
	    scroll.setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
	    panel.add(scroll);
	    FinestraComando.getContentPane().add(panel);
	    
	    btnEstraiDati = new JButton("Leggi File");
	    btnEstraiDati.setVisible(false);
	    btnEstraiDati.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent arg0) {
	    		//JOptionPane.showMessageDialog(FinestraComando, "Estrai Dati");
	    		EstraiDatidaFile();
	    	}
	    });
	    btnEstraiDati.setToolTipText("Analizza il file");
	    btnEstraiDati.setBounds(68, 564, 122, 47);
	    FinestraComando.getContentPane().add(btnEstraiDati);
	    
	    JButton btnRicercaFile = new JButton("Ricerca File");
	    btnRicercaFile.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		CollegaFileAccess();
	    		/*trovafileAccess=new FileDialogWindows("Access File","accdb","mdb");
	    		if (trovafileAccess.getEsito()==1) {
	    			btnEstraiDati.setVisible(true);
	    			btnSimula.setVisible(true);
		    		JOptionPane.showMessageDialog(FinestraComando, "File selezionato");
	    		} else {
		    		JOptionPane.showMessageDialog(FinestraComando, "File NON selezionato");
	    		}*/
	    	}
	    });
	    btnRicercaFile.setToolTipText("Ricerca il file da elaborare sul PC");
	    btnRicercaFile.setBounds(283, 564, 122, 47);
	    FinestraComando.getContentPane().add(btnRicercaFile);
	    
	    btnSimula = new JButton("Simula");
	    btnSimula.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		JOptionPane.showMessageDialog(FinestraComando, "Simula");
	    		SimulaEstraiDati();
	    	}
	    });
	    btnSimula.setToolTipText("Simulazione mail");
	    btnSimula.setBounds(488, 564, 122, 47);
	    btnSimula.setVisible(false);
	    FinestraComando.getContentPane().add(btnSimula);
	    FinestraComando.setVisible(true);
	
	    
	}
	abstract void EstraiDatidaFile();
	abstract void SimulaEstraiDati();
	abstract void CollegaFileAccess();
}
