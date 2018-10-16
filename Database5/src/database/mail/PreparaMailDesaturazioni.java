package database.mail;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import javax.swing.JOptionPane;
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
	private Connection connessioneDB=null;
	final int TUTTO_OK=0;
	final int ERRORE_CONNESSIONE_DRIVER=1;
	final int ERRORE_CONNESSIONE_DATABASE=2;
	final int errore_sql_tabella_Indirizzi=4;
	final int errore_chiusura_tabella_destinatari=5;
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
		//eseguire connessione a Driver
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
		Statement statement=null;
		ResultSet recordset=null;
		//inizializzo i valori di default dei campi della finestra
		usrtxt.setText(CostruisciDestinatariMail("UserMittente"));
		mittentetxt.setText(CostruisciDestinatariMail("Mittente"));
		if (getErrore()==0) {
			try {
				int i=0;
				int mailinviate=0;
				int contamaildainviare=0;
				InviaMailTim posta = new InviaMailTim(mittentetxt.getText(),usrtxt.getText(),"");
				/*istruzione setAutoCommit necessaria per rendere aggiornabile ogni record del recordset
				senza tale istruzione viene aggiornato solamente il primo record del recorset*/
				connessioneDB.setAutoCommit(false);
				// Step 2.B: Creating JDBC Statement
				//statement=connessioneDB.createStatement();//questo statement apre di default il recordset scrollabile solo in avanti ed in sola lettura
				//serve a dichiarere se il recordset potrà essere scorso solamente in avanti e potrà essere aggiornabile
				statement=connessioneDB.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
				// Step 2.C: Executing SQL &amp; retrieve data into recordSet
				recordset = statement.executeQuery("SELECT * FROM Desaturazioni_Locale WHERE DataInvioMail is Null AND TD is Not Null AND [N# LP] is Not Null AND WR is Not Null AND [Data programmata] is Not Null");
				// Conteggio record
				while (recordset.next()){
					i+=1;
					//Controllo se il campo AOL è scritto correttamente
					String Temp=recordset.getString(AOL);
					boolean controlloAOL=(Temp.matches("ABM")|| Temp.matches("LAZ")|| Temp.matches("ROM")|| Temp.matches("SAR")|| Temp.matches("TOE")|| Temp.matches("TOO")|| Temp.matches("LIG"));
					//Fine controllo campo AOL
					//Controllo se non ci sono stringhe vuote
					boolean controllodati=controlloAOL && (recordset.getString(NLP)!="")&&(recordset.getString(TD)!="");
					//Fine controllo stringhe vuote
					if (controllodati) {
						contamaildainviare++;
						if (avvio_o_simulazione) {
							//Costruire la mail
							destinatariotxt.setText(CostruisciDestinatariMail("DestinatarioA"));
							destinatarioCCtxt.setText(CostruisciDestinatariMail("DestinatarioCC"));
							// Costruisco il destinatario per conoscenza a seconda dell'AOL
							switch (recordset.getString(AOL)) {
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
							default:
								//Serve per aggiungere eventuali altre AOL. L'evento non si verificherà mai
								JOptionPane.showMessageDialog(null,
										"AOL non identificata: " + recordset.getString(AOL));
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
						oggettotxt.setText("Richiesta lavoro programmato "+ recordset.getString(SOLUZIONE)+ " Centrale "+ 
								recordset.getString(CENTRALE)+ " TD "+ recordset.getString(TD));
						// Fine oggetto mail
						//Costruisco il corpo della mail
						//Controllo se è il caso IPCOM o meno e definisco il testo finale del corpo della mail 
						String testofinale;
						if (recordset.getString(IPCOM).equals("Sì")) {
							testofinale="Riferimenti: IP-COM - MASTRANTONI PASQUALE - 335.7282189; \nspecialisti NOA: VETRANO ALESSIO 335.1440764 (per CE) e LUPI GIOVANNI 335.1342688 (per C1)";
						}else {
							testofinale="Per la consueta verifica di funzionalità/raggiungibilità i colleghi che interverranno dovranno contattare N.NOA/C.M.F tramite Help me";
						}
						//Conversione formato data
						ConversioneFormatoData convertidata=new ConversioneFormatoData();
						String dataprogrammataconvertita= convertidata.converti(recordset.getDate(DATA_PROGRAMMATA).toString(), "yyyy-MM-dd", " EEEE dd/MM/yyyy");
						//Fine conversione formato data
						corpomailtxt.setText("Si richiede l’Autorizzazione all’Esecuzione di Lavori Programmati inerenti l' "+ recordset.getString(SOLUZIONE) + " Centrale "
								+ recordset.getString(CENTRALE)+" "+recordset.getString(DSLAM)+ " TD "+ recordset.getString(TD)+ " per "+
								dataprogrammataconvertita+". \n" + "La richiesta è stata inserita nel portale LP con il numero "+
								recordset.getString(NLP)+".\nDi seguito sono riportate le WR per i tecnici ON SITE: \n" + 
								recordset.getString(WR)+ ".\n"+ testofinale +".\nSaluti \nMatteo Bassi");
						//Prima di inviare la mail apro la finestra di dialogo che chiede conferma invio mail
						int risposta=JOptionPane.showConfirmDialog(FinestraComando, "Vuoi inviare la mail?", "Conferma invio mail",JOptionPane.OK_CANCEL_OPTION);
						//La mail parte solo se si dà l'OK dalla finestra di dialogo
						if (risposta== JOptionPane.OK_OPTION) {
							//Inizio blocco invio  mail
							try {
								posta.Invia(destinatariotxt.getText(), destinatarioCCtxt.getText(), oggettotxt.getText(),
										corpomailtxt.getText());
								if (posta.getEsitoInvio() != 0) {
									setErrore(1);
									//---------	JOptionPane.showMessageDialog(null, "Posta  Inviata");
									mailinviate++;						
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
									setErrore(3);
									JOptionPane.showMessageDialog(null, "Errore 3 la posta non è partita");
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								setErrore(3);
								JOptionPane.showMessageDialog(null, "Errore 3 la posta non è partita (try/catch)");
								e.printStackTrace();
							}
							//Fine invio mail
						}//Fine if (JOptionPane...
					}//fine if (controllodati)
				}//fine ciclo while
				//L'istruzione connessioneDB.commit() serve per scrivere gli aggiornamenti sul database
				connessioneDB.commit();
				JOptionPane.showMessageDialog(FinestraComando, "Numero di righe: "+ i+ " Numero di mail:" + contamaildainviare + "; "+ "Mail inviate: "+ mailinviate);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				JOptionPane.showMessageDialog(FinestraComando, "Errore SQL");
			} finally {
				// Step 3: Closing database connection
				try {
					if (connessioneDB != null) {
						// cleanup resources, once after processing
						recordset.close();
						statement.close();
						// and then finally close connection
						/*la connessione non può essere chiusa in questo momento
						 *altrimenti chiudendo l'oggetto connessioneDB non posso
						 *più premere il bottone estrai dati. 
						 */
						connessioneDB.close();
						btnEstraiDati.setVisible(false);
						btnSimula.setVisible(false);
					}
				} catch (SQLException sqlex) {
					sqlex.printStackTrace();
					JOptionPane.showMessageDialog(null, "Errore in chiusura");
				}
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
			//CODICE PER COLLEGARE DATABASE ACCESS
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
	public String CostruisciDestinatariMail (String tipologia) {
		//-----------GESTIRE ERRORI IN CASO l'SQL non trovi match---------------
		Statement statement=null;
		ResultSet recordset=null;
		String risultato="";
		int i=0;
		try {
			//questo statement apre di default il recordset scrollabile solo in avanti ed in sola lettura
			statement=connessioneDB.createStatement();
			recordset = statement.executeQuery("SELECT * FROM Destinatari_Mail WHERE Destinatari_Mail.Destinatari = '"+ tipologia + "'");		
			while (recordset.next()){
				i+=1;
				if (recordset.isLast()) {
					risultato = risultato + recordset.getString("Mail");
				} else {
					risultato = risultato +  recordset.getString("Mail") + ", ";
				}
			}//Fine ciclo While
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setErrore(errore_sql_tabella_Indirizzi);
			JOptionPane.showMessageDialog(FinestraComando, "Errore SQL da CostruisciDestinatariMail() "+ getErrore());
		}finally {
			try {
				recordset.close();
				statement.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				setErrore(errore_chiusura_tabella_destinatari);
				JOptionPane.showMessageDialog(null, "Errore in chiusura da CostruisciDestinatariMail() " + getErrore());
			}
		}//Fine blocco Try/Cach/Finally
		System.out.println("Numero iterazioni: "+i);
		System.out.println(risultato);
		return risultato;
	}


}
