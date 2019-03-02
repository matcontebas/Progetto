package database.mail;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import Database.ConnessioneDB;
import Database.ConnessioneDriver;
import Posta.InviaMailTim;
import RicercaFile.FileDialogWindows;
import conversionedate.ConversioneFormatoData;
/**
 * Classe PreparaMailDesaturazioni implementa la classe astratta FinestraApplicativa
 * adattandola al file di lavoro Access
 * @author Matteo Bassi
 *
 */
public class PreparaMailDesaturazioni extends FinestraApplicativa {
	/**
	 * errore serve per controllare il flusso di programma
	 * 0: tutto regolare;
	 * 1: errore nella connessione al driver
	 * 2: errore nella connessione con il database
	 * 3: errore invio posta
	 * 4: errore sql nell'interrogazione della tabella Destinatari_Mail
	 * 5: errore chiusura tabella destinatari
	 */
	private int errore;
	private boolean esegui_o_simula; //simulazione true significa modalità normale false simulazione
	private Connection connessioneDB=null;
	private Statement statement=null;
	private ResultSet recordset=null;
	final int TUTTO_OK=0;
	final int ERRORE_CONNESSIONE_DRIVER=1;
	final int ERRORE_CONNESSIONE_DATABASE=2;
	final int ERRORE_INVIO_POSTA=3;
	final int ERRORE_SQL_TABELLA_PRINCIPALE=4;
	final int ERRORE_SQL_TABELLA_INDIRIZZI=5;
	final int ERRORE_CHIUSURA_TABELLA_DESTINATARI=6;
	final int CENTRALE = 6;//colonna 6 della tabella Desaturazioni
	final int DSLAM = 7;//colonna 7 della tabella Desaturazioni
	final int SOLUZIONE = 11;//colonna 11 della tabella Desaturazioni
	final int TD = 17;//colonna 17 della tabella Desaturazioni
	final int IPCOM = 21;//colonna 21 della tabella Desaturazioni
	final int DATA_PROGRAMMATA= 25;//colonna 25 della tabella Desaturazioni
	final int WR = 28;//colonna 28 della tabella Desaturazioni
	final int NLP=26; //colonna 26 del file desaturazioni
	final int AOL=30; //colonna 30 del file desaturazioni
	final int AZIONE=32; //colonna 32 della tabella desaturazioni
	final int DATA_INVIO_MAIL=34; //colonna 34 del file desaturazioni
	final String AGGIORNAMENTO_CAMPO_AZIONE="verifica esecuzione";
	/**
	 * Costruttore: inizializza usrtxt, mittentetxt, destinatariotxt, destinatarioCCtxt
	 * e crea la connessione con il Driver di database.
	 */
	public PreparaMailDesaturazioni() {
		//Costruttore
		//Inserisco l'event handler windowclosing per gestire la chiusura delle connessioni in uscita
		//FinestraComando.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		FinestraComando.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				JOptionPane.showMessageDialog(FinestraComando, "Finestra chiusa");
				try {
					if (connessioneDB != null) {
						if (recordset != null) {
							//L'istruzione connessioneDB.commit() serve per scrivere gli aggiornamenti sul database
							connessioneDB.commit();
							// cleanup resources, once after processing
							recordset.close();
						}
						if (statement != null) {
							statement.close();
						}
						// and then finally close connection
						connessioneDB.close();
					}
				} catch (SQLException sqlex) {
					sqlex.printStackTrace();
					JOptionPane.showMessageDialog(null, "Errore in chiusura Database event handler windowclosing");
				}

				FinestraComando.dispose();
				System.exit(0);
			}
		});
		//Fine event handler
		initialize2();
		//eseguire connessione a Driver: la classe ConnessioneDriver è nella mia libreria DatabaseLib.jar
		ConnessioneDriver driverconn=new  ConnessioneDriver();
		driverconn.connettiDriver();
		// controllo che la connessione al Driver sia andata a buon fine verificando
		// che il valore restituito dal metodo getErrore sia diverso da zero
		if (driverconn.getErrore() != TUTTO_OK) {
			//JOptionPane.showMessageDialog(FinestraComando, "Driver di database caricato");
			setErrore(TUTTO_OK);//errore=0 significa tutto regolare
		}
		else {
			JOptionPane.showMessageDialog(FinestraComando, "Driver di database non corretto");
			setErrore(ERRORE_CONNESSIONE_DRIVER); //errore=1 significa  nella connessione con il driver
		}
	}
	public void EstraiDatidaFile(boolean avvio_o_simulazione) {
		//inizializzo i valori di default dei campi della finestra
		usrtxt.setText(CostruisciDestinatariMail("UserMittente"));
		mittentetxt.setText(CostruisciDestinatariMail("Mittente"));
		//imposta la variabile privata esegui_o_simula al valore di avvio_o_simulazione
		setEsegui_o_simula(avvio_o_simulazione);
		if (getErrore()==TUTTO_OK) {
			try {
				int i=0;
				int mailinviate=0;
				int contamaildainviare=0;
				//boolean invioposta;
				//InviaMailTim posta = new InviaMailTim(mittentetxt.getText(),usrtxt.getText(),"");
				/*istruzione setAutoCommit necessaria per rendere aggiornabile ogni record del recordset
				senza tale istruzione viene aggiornato solamente il primo record del recorset*/
				connessioneDB.setAutoCommit(false);
				// Step 2.B: Creating JDBC Statement
				//statement=connessioneDB.createStatement();//questo statement apre di default il recordset scrollabile solo in avanti ed in sola lettura
				//serve a dichiarere se il recordset potrà essere scorso solamente in avanti e potrà essere aggiornabile
				statement=connessioneDB.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
				// Step 2.C: Executing SQL &amp; retrieve data into recordSet
				recordset = statement.executeQuery("SELECT * FROM Desaturazioni_Locale WHERE DataInvioMail is Null AND TD is Not Null AND [N# LP] is Not Null AND WR is Not Null AND [IP NW]is Not Null AND [Data programmata] is Not Null");
				// Conteggio record
				while (recordset.next()){
					i+=1;
				}//fine ciclo while
				if (i>0) {
					//Se ci sono record (i>0) faccio apparire l'oggetto panel che contiene i bottoni per muoversi all'interno del recordset
					pannello_Bottoni.setVisible(true);
					btnEstraiDati.setVisible(false);
					btnSimula.setVisible(false);
					btnRicercaFile.setVisible(false);
					//Posiziono il cursore sul primo record del recordset
					MoveFirst(recordset);
				}
				JOptionPane.showMessageDialog(FinestraComando, "Numero di righe: "+ i+ " Numero di mail:" + contamaildainviare + "; "+ "Mail inviate: "+ mailinviate);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				setErrore(ERRORE_SQL_TABELLA_PRINCIPALE);
				JOptionPane.showMessageDialog(FinestraComando, "Errore SQL Tabella principale" + getErrore());
			}
		} else {
			JOptionPane.showMessageDialog(FinestraComando, "Errore: "+getErrore()+" il pulsante non funzione");
		}
	} //Fine estrai dati
	public void CollegaFileAccess() {
		FileDialogWindows trovafileAccess=new FileDialogWindows("Access File","accdb","mdb");
		if (trovafileAccess.getEsito()==1) {
			btnEstraiDati.setVisible(true);
			btnSimula.setVisible(true);
			String PathDB=trovafileAccess.percorsofile();
			//JOptionPane.showMessageDialog(FinestraComando, "File selezionato: \n" + PathDB);
			setErrore(TUTTO_OK);
			//CODICE PER COLLEGARE DATABASE ACCESS: la classe ConnessioneDB è una classe di libreria mia contenuta in DatabaseLib.jar
			ConnessioneDB connettore=new ConnessioneDB();
			connessioneDB=connettore.connettiDB(PathDB);
			if (connettore.getErrore()!=0) {
				JOptionPane.showMessageDialog(FinestraComando, "Connessione a database stabilita");
				setErrore(TUTTO_OK);
			} else {
				setErrore(ERRORE_CONNESSIONE_DATABASE);
				JOptionPane.showMessageDialog(FinestraComando, "Connessione a database non riuscita "+ getErrore());
			}
		} else {
			JOptionPane.showMessageDialog(FinestraComando, "File NON selezionato");
		}
	}
	public void MoveFirst(ResultSet rs) {
		//JOptionPane.showMessageDialog(FinestraComando, "First");
		try {
			if (rs.first()) {
				//JOptionPane.showMessageDialog(FinestraComando, "First eseguito");
				//inserire ComponiMail
				ComponiMail(isEsegui_o_simula(),rs);
			} 
		} catch (Exception e) {
			// TODO: handle exception
			JOptionPane.showMessageDialog(FinestraComando, "Errore metodoMoveFirst");
		}
	}
	public void MovePrev(ResultSet rs) {
		//JOptionPane.showMessageDialog(FinestraComando, "Prev");
		try {
			if (rs.previous()) {
				//JOptionPane.showMessageDialog(FinestraComando, "Prev eseguito");
				ComponiMail(isEsegui_o_simula(),rs);			
			} else {
				//la seguente istruzione serve per riportare il cursore sul record corretto quando
				//premo MovePrev troppe volte
				rs.next();
			}
		} catch (Exception e) {
			// TODO: handle exception
			JOptionPane.showMessageDialog(FinestraComando, "Errore metodoMovePrev");
		}
	}
	public void MoveNext(ResultSet rs) {
		//JOptionPane.showMessageDialog(FinestraComando, "Next");
		try {
			if (rs.next()) {
				//JOptionPane.showMessageDialog(FinestraComando, "Next eseguito");
				ComponiMail(isEsegui_o_simula(),rs);
			} else {
				//la seguente istruzione serve per riportare il cursore sul record corretto quando
				//premo MoveNext troppe volte
				rs.previous();
			}
		} catch (Exception e) {
			// TODO: handle exception
			JOptionPane.showMessageDialog(FinestraComando, "Errore metodoMoveNext");
		}
	}
	public void MoveLast(ResultSet rs) {
		//JOptionPane.showMessageDialog(FinestraComando, "Last");
		try {
			if (rs.last()) {
				//JOptionPane.showMessageDialog(FinestraComando, "Last eseguito");
				ComponiMail(isEsegui_o_simula(),rs);
			} 
		} catch (Exception e) {
			// TODO: handle exception
			JOptionPane.showMessageDialog(FinestraComando, "Errore metodoMoveLast");
		}
	}
	private void initialize2() {
	    //Pannello per inserire i 4 bottoni
	    pannello_Bottoni = new JPanel();
	    pannello_Bottoni.setBackground(Color.ORANGE);
	    pannello_Bottoni.setBounds(184, 635, 350, 35);
	    FinestraComando.getContentPane().add(pannello_Bottoni);
	    pannello_Bottoni.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
	    pannello_Bottoni.setVisible(false);
	    //creazione btnNext
	    JButton btnNext = new JButton("Next");
	    btnNext.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent arg0) {
	    		MoveNext(getRecordset());
	    	}
	    });
	    //creazione btnFirst
	    JButton btnFirst = new JButton("First");
	    btnFirst.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent arg0) {
	    		MoveFirst(getRecordset());
	    	}
	    });
	    //creazione btnPrev
	    JButton btnPrev = new JButton("Prev");
	    btnPrev.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent arg0) {
	    		MovePrev(getRecordset());
	    	}
	    });
	    //creazione btnLast
	    JButton btnLast = new JButton("Last");
	    btnLast.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent arg0) {
	    		MoveLast(getRecordset());
	    	}
	    });
	    //creazione btnMail
	    JButton btnMail = new JButton("Mail");
	    btnMail.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent arg0) {
	    		boolean invioposta;
				boolean controllodati;
				InviaMailTim posta = new InviaMailTim(mittentetxt.getText(),usrtxt.getText(),"");		
				try {
					//Controllo se il campo AOL è scritto correttamente
					String Temp=recordset.getString(AOL);
					boolean controlloAOL=(Temp.matches("ABM")|| Temp.matches("LAZ")|| Temp.matches("ROM")|| Temp.matches("SAR")|| Temp.matches("TOE")|| Temp.matches("TOO")|| Temp.matches("LIG")||Temp.matches("LACP"));
					//Fine controllo campo AOL
					//Controllo se non ci sono stringhe vuote
					controllodati = controlloAOL && (recordset.getString(NLP)!="")&&(recordset.getString(TD)!="");
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					//Se qualcosa va male, non devo inviare mail quindi imposto controllodati a falso
					controllodati=false;
					JOptionPane.showMessageDialog(FinestraComando, "Errore lettura recordset btnMail");
					e.printStackTrace();
				}
				//Fine controllo stringhe vuote
				if (controllodati) {
					//contamaildainviare++;
					//ComponiMail compone il testo della mail e lo memorizza nei campi testo della finestra
					//ComponiMail(avvio_o_simulazione,recordset);
					//Chiamo il metodo inviamail che provvede all'invio della mail e all'aggiornamento del campo data di invio del DB
					invioposta=inviamail(recordset,posta);
					//Se l'invio della mail e l'aggiornamento è andato bene, incremento il contatore mail inviate.
					if (invioposta) {
						//mailinviate++;
					}

				}//fine if (controllodati)
	    	}
	    });
	    
	    pannello_Bottoni.add(btnFirst);
	    pannello_Bottoni.add(btnPrev);
	    pannello_Bottoni.add(btnNext);
	    pannello_Bottoni.add(btnLast);
	    pannello_Bottoni.add(btnMail);
	}
	/**
	 * @return the errore
	 */
	public int getErrore() {
		return errore;
	}
	/**
	 * @param errore the errore to set
	 */
	protected void setErrore(int errore) {
		this.errore = errore;
	}
	/**
	 * Il metodo costruisce una stringa con i destinatari di una mail 
	 * prendendoli dalla tabella del file Access Destinatari_Mail a seconda della tipologia
	 * @param tipologia è una stringa che individua i tipi di destinatario come indicati in tabella Destinatari_Mail
	 * (DestinatariA, DestinatariCC, Mittente, User, ABM, LAZ, SAR, etc)
	 * @return restituisce la stringa con il/i destinatari
	 */
	private String CostruisciDestinatariMail (String tipologia) {
		//-----------GESTIRE ERRORI IN CASO l'SQL non trovi match---------------
		Statement statement=null;
		ResultSet recordset=null;
		String risultato="";
		//int i=0;
		try {
			//questo statement apre di default il recordset scrollabile solo in avanti ed in sola lettura
			statement=connessioneDB.createStatement();
			recordset = statement.executeQuery("SELECT * FROM Destinatari_Mail WHERE Destinatari_Mail.Destinatari = '"+ tipologia + "'");		
			while (recordset.next()){
				//i+=1;
				if (recordset.isLast()) {
					risultato = risultato + recordset.getString("Mail");
				} else {
					risultato = risultato +  recordset.getString("Mail") + ", ";
				}
			}//Fine ciclo While
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setErrore(ERRORE_SQL_TABELLA_INDIRIZZI);
			JOptionPane.showMessageDialog(FinestraComando, "Errore SQL da CostruisciDestinatariMail() "+ getErrore());
		}finally {
			try {
				recordset.close();
				statement.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				setErrore(ERRORE_CHIUSURA_TABELLA_DESTINATARI);
				JOptionPane.showMessageDialog(null, "Errore in chiusura da CostruisciDestinatariMail() " + getErrore());
			}
		}//Fine blocco Try/Cach/Finally
		//System.out.println("Numero iterazioni: "+i);
		//System.out.println(risultato);
		return risultato;
	}

	private String CostruisciTestoMail (String tipo) {
		//-----------GESTIRE ERRORI IN CASO l'SQL non trovi match---------------
		Statement statement=null;
		ResultSet recordset=null;
		String risultato="";
		//int i=0;
		try {
			//questo statement apre di default il recordset scrollabile solo in avanti ed in sola lettura
			statement=connessioneDB.createStatement();
			recordset = statement.executeQuery("SELECT * FROM Testi_Mail WHERE Testi_Mail.TipoTesto = '"+ tipo + "'");		
			while (recordset.next()){
				//i+=1;
				if (recordset.isLast()) {
					risultato = risultato + recordset.getString("Testo");
				} else {
					risultato = risultato +  recordset.getString("Testo") + ", ";
				}
			}//Fine ciclo While
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setErrore(ERRORE_SQL_TABELLA_INDIRIZZI);
			JOptionPane.showMessageDialog(FinestraComando, "Errore SQL da CostruisciTestoMail() "+ getErrore());
		}finally {
			try {
				recordset.close();
				statement.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				setErrore(ERRORE_CHIUSURA_TABELLA_DESTINATARI);
				JOptionPane.showMessageDialog(null, "Errore in chiusura da CostruisciTestoMail() " + getErrore());
			}
		}//Fine blocco Try/Cach/Finally
		//System.out.println("Numero iterazioni: "+i);
		//System.out.println(risultato);
		return risultato;

	}
	/**
	 * Il metodo provvede all'invio della mail e all'aggiornamento del campo data invio mail nel DB
	 * @param recordset: riceve il recordset dalla funzione chiamante
	 * @param posta: riceve l'oggetto posta di tipo InviaMailTim (della mia libreria)
	 * @return: ritorna TRUE se tutto è andato bene, altrimenti FALSE
	 */
	private boolean inviamail(ResultSet recordset,InviaMailTim posta) {
		//Prima di inviare la mail apro la finestra di dialogo che chiede conferma invio mail
		int risposta=JOptionPane.showConfirmDialog(FinestraComando, "Vuoi inviare la mail?", "Conferma invio mail",JOptionPane.OK_CANCEL_OPTION);
		//La mail parte solo se si dà l'OK dalla finestra di dialogo																		
		if (risposta== JOptionPane.OK_OPTION) {
			//Inizio blocco invio  mail
			try {
				posta.Invia(destinatariotxt.getText(), destinatarioCCtxt.getText(), oggettotxt.getText(),
						corpomailtxt.getText());
				if (posta.getEsitoInvio() != 0) {
					setErrore(TUTTO_OK);
					//---------	JOptionPane.showMessageDialog(null, "Posta  Inviata");
					//mailinviate++;	
					try {
						//Gestione Data: serve convertire la variabile tipo LocalDate in formato data java.sql.Date
						//poichè il metodo updateDate richiede una data in formato java.sql
						LocalDate todayLocalDate = LocalDate.now();
						java.sql.Date sqlDate= java.sql.Date.valueOf(todayLocalDate);
						//Fine gestione data
						recordset.updateDate(DATA_INVIO_MAIL, sqlDate);
						recordset.updateString(AZIONE, AGGIORNAMENTO_CAMPO_AZIONE);
						recordset.updateRow();
						JOptionPane.showMessageDialog(FinestraComando, "Data  aggiornata OK");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(FinestraComando, "Data non aggiornata");
						e.printStackTrace();
					}
				} else {
					setErrore(ERRORE_INVIO_POSTA);
					JOptionPane.showMessageDialog(null, "Errore 3 la posta non è partita");
					return false;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				setErrore(ERRORE_INVIO_POSTA);
				JOptionPane.showMessageDialog(null, "Errore 3 la posta non è partita (try/catch)");
				e.printStackTrace();
				return false;
			}
			//Fine invio mail
			return true;
		} //Fine if (JOptionPane...
		return false;
	}
	/**
	 * Il metodo ComponiMail costruisce le stringhe della mail (destinatario, oggetto, corpo della mail) e le scrive nei campi testo della finestra
	 * @param esegui_o_simula: la variabile boolean stabilisce se sono in modalità simulazione (la mail viene inviata al mittente oppure se siamo
	 * in esecuzione normale (la mail viene inviata ai destinatari reali.
	 * @param rs è il recordset
	 */
	private void ComponiMail(boolean esegui_o_simula, ResultSet rs) {
		/*Nel momento in cui ho fatto un metodo specializzato per gestire la composizione del testo, è stato necessario inserirlo in
		un blocco try/catch per gestire le eccezioni sull'oggetto rs (result set) che rappresenta l'insieme dei record del database*/
		try {
			//---------------------Inizio Blocco costruzione testo mail----------------------------------------						
		if (esegui_o_simula) {
			//Costruire la mail
			destinatariotxt.setText(CostruisciDestinatariMail("DestinatarioA"));
			destinatarioCCtxt.setText(CostruisciDestinatariMail("DestinatarioCC"));
			// Costruisco il destinatario per conoscenza a seconda dell'AOL
			switch (rs.getString(AOL)) {
			case "ABM":
				destinatarioCCtxt.setText(destinatarioCCtxt.getText() + ", " + CostruisciDestinatariMail("ABM"));
				break;
			case "LAZ":
				destinatarioCCtxt.setText(destinatarioCCtxt.getText() + ", " + CostruisciDestinatariMail("LAZ"));
				break;
			case "SAR":
				destinatarioCCtxt.setText(destinatarioCCtxt.getText() + ", " + CostruisciDestinatariMail("SAR"));
				break;
			case "ROM":
				destinatarioCCtxt.setText(destinatarioCCtxt.getText() + ", " + CostruisciDestinatariMail("ROM"));
				break;
			case "TOE":
				destinatarioCCtxt.setText(destinatarioCCtxt.getText() + ", " + CostruisciDestinatariMail("TOE"));
				break;
			case "TOO":
				destinatarioCCtxt.setText(destinatarioCCtxt.getText() + ", " + CostruisciDestinatariMail("TOO"));
				break;
			case "LIG":
				destinatarioCCtxt.setText(destinatarioCCtxt.getText() + ", " + CostruisciDestinatariMail("LIG"));
				break;
			case "LACP":
				//inserire codice per LACP
				//JOptionPane.showMessageDialog(FinestraComando, "LACP");
				break;
			default:
				//Serve per aggiungere eventuali altre AOL. L'evento non si verificherà mai
				JOptionPane.showMessageDialog(null,
						"AOL non identificata: " + rs.getString(AOL));
			}
		} else {
			// modalità simulazione: la mail viene inviata a me
			//memorizzo in temp il Mittente dalla tabella Destinatari_Mail
			String temp=CostruisciDestinatariMail("Mittente");
			//in modalità simulazione il destinatario della mail è solo il mittente
			destinatariotxt.setText(temp);
			destinatarioCCtxt.setText(temp);
		}
		//Fine destinatario per conoscenza
		//Costruisco l'oggetto della mail------------------
		oggettotxt.setText("Richiesta lavoro programmato "+ rs.getString(SOLUZIONE)+ " Centrale "+ 
				rs.getString(CENTRALE)+ " "+ rs.getString(DSLAM)+ " TD "+ rs.getString(TD));
		// Fine oggetto mail
		//Costruisco il corpo della mail					
		//Conversione formato data
		ConversioneFormatoData convertidata=new ConversioneFormatoData();
		String dataprogrammataconvertita= convertidata.converti(rs.getDate(DATA_PROGRAMMATA).toString(), "yyyy-MM-dd", " EEEE dd/MM/yyyy");
		//Fine conversione formato data
		//questo switch serve per variare il corpo della mail in funzione del valore del campo AOL
		switch (rs.getString(AOL)) {
		case "LACP":
			corpomailtxt.setText("Si richiede l’Autorizzazione all’Esecuzione di Lavori Programmati inerenti l' "+ rs.getString(SOLUZIONE) + " Centrale "
					+ rs.getString(CENTRALE)+" "+rs.getString(DSLAM)+ " TD "+ rs.getString(TD)+ " per "+
					dataprogrammataconvertita+". \n" + "La richiesta è stata inserita nel portale LP con il numero "+
					rs.getString(NLP)+".\n" + CostruisciTestoMail("WRSpecialisti") + " \n" + 
					rs.getString(WR)+ ".\n"+ "Saluti \nMatteo Bassi");
			break;
			default:
				//il caso default comprende tutti i casi in cui AOL è valorizzata da ABM fino a TOO
				//Controllo se è il caso IPCOM o meno e definisco il testo finale del corpo della mail 
				String testofinale="";
				if (rs.getString(IPCOM).equals("Sì")) {
					testofinale=CostruisciTestoMail("IPCOM_SI");
				}else {
					testofinale=CostruisciTestoMail("IPCOM_NO");
				}
				corpomailtxt.setText("Si richiede l’Autorizzazione all’Esecuzione di Lavori Programmati inerenti l' "+ rs.getString(SOLUZIONE) + " Centrale "
						+ rs.getString(CENTRALE)+" "+rs.getString(DSLAM)+ " TD "+ rs.getString(TD)+ " per "+
						dataprogrammataconvertita+". \n" + "La richiesta è stata inserita nel portale LP con il numero "+
						rs.getString(NLP)+".\n" + CostruisciTestoMail("WRTecnici") + " \n" + 
						rs.getString(WR)+ ".\n"+ testofinale +"\nSaluti \nMatteo Bassi");	
		}
//-------------------------------Fine blocco costruzione testo mail------------------------------------------------						
		} catch (Exception e) {
			// TODO: handle exception
			JOptionPane.showMessageDialog(FinestraComando, "Errore generico nel metodo ComponiMail nell'oggetto PreparaDesaturazioni");
		}						
	}
	public ResultSet getRecordset() {
		return recordset;
	}
	public void setRecordset(ResultSet recordset) {
		this.recordset = recordset;
	}
	private boolean isEsegui_o_simula() {
		return esegui_o_simula;
	}
	private void setEsegui_o_simula(boolean esegui_o_simula) {
		this.esegui_o_simula = esegui_o_simula;
	}
}
