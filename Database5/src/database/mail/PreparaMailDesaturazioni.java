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
	 */
	private int errore;
	private Connection connessioneDB=null;
	final int CENTRALE = 6;//colonna 6 della tabella Desaturazioni
	final int DSLAM = 7;//colonna 7 della tabella Desaturazioni
	final int SOLUZIONE = 11;//colonna 11 della tabella Desaturazioni
	final int TD = 17;//colonna 17 della tabella Desaturazioni
	final int IPCOM = 21;//colonna 21 della tabella Desaturazioni
	final int DATA_PROGRAMMATA= 25;//colonna 25 della tabella Desaturazioni
	final int WR = 28;//colonna 28 della tabella Desaturazioni
	final int NLP=26; //colonna 26 del file desaturazioni
	final int AOL=30; //colonna 30 del file desaturazioni
	final int DATA_INVIO_MAIL=34; //colonna 34 del file desaturazioni
	final String ABM="enzo.cialone@telecomitalia.it, gianluca.dicrescenzo@telecomitalia.it, angelomario.blasioli@telecomitalia.it, marco.vigilante@telecomitalia.it, domenico.montebello@telecomitalia.it, paolo.digirolamo@telecomitalia.it";
	final String LAZ="giovanna.gerbasio@telecomitalia.it, mario.micci@telecomitalia.it";
	final String SAR="mauro.mostallino@telecomitalia.it";
	final String ROM="enrico.digiacomo@telecomitalia.it, luca.parlanti@telecomitalia.it, simona.sbandi@telecomitalia.it";
	final String TOE="gianni.emanuelifrancioli@telecomitalia.it, Paolo.Bruschini@telecomitalia.it, tommaso.scotti@telecomitalia.it, graziano.folli@telecomitalia.it";
	final String TOO="marco.paoli@telecomitalia.it, JMToscanaOvest@telecomitalia.it";
	final String LIG="mauro.mazzitello@telecomitalia.it, vittorio.piacenza@telecomitalia.it";
	/**
	 * Costruttore: inizializza usrtxt, mittentetxt, destinatariotxt, destinatarioCCtxt
	 * e crea la connessione con il Driver di database.
	 */
	public PreparaMailDesaturazioni() {
		//Costruttore
		//inizializzo i valori di default dei campi della finestra
		usrtxt.setText("08043160");
		mittentetxt.setText("matteo.bassi@telecomitalia.it");
		destinatariotxt.setText("matteo.bassi@telecomitalia.it");
		destinatarioCCtxt.setText("matteo.bassi@telecomitalia.it");
		//eseguire connessione a Driver
		ConnessioneDriver driverconn=new  ConnessioneDriver();
		driverconn.connettiDriver();
		// controllo che la connessione al Driver sia andata a buon fine verificando
		// che il valore restituito dal metodo getErrore sia diverso da zero
		if (driverconn.getErrore() != 0) {
			//Inserire codice
			//JOptionPane.showMessageDialog(FinestraComando, "Driver di database caricato");
			setErrore(0);//errore=0 significa tutto regolare
		}
		else {
			//System.out.println("Driver di database non corretto");
			JOptionPane.showMessageDialog(FinestraComando, "Driver di database non corretto");
			setErrore(1); //errore=1 significa  nella connessione con il driver
		}
	}
	public void EstraiDatidaFile() {
		Statement statement=null;
		ResultSet recordset=null;

		if (getErrore()==0) {
			//inserire codice con sql
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
				//recordset = statement.executeQuery("SELECT * FROM Desaturazioni WHERE [N# LP] = '1693/2018'");
				recordset = statement.executeQuery("SELECT * FROM Desaturazioni_Locale WHERE DataInvioMail is Null AND TD is Not Null AND [N# LP] is Not Null AND WR is Not Null AND [Data programmata] is Not Null");
				// Conteggio record
				while (recordset.next()){
					//System.out.println(recordset.getString(CENTRALE)+ " - "+ recordset.getInt(3)+ " - "+recordset.getString(AOL));
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
						//Costruire la mail
						destinatariotxt.setText("antonio.argenziano@telecomitalia.it, massimo.dominici@telecomitalia.it, andrea.turella@telecomitalia.it");
						destinatarioCCtxt.setText("fabrizio.adanti@telecomitalia.it, adriano.calvini@telecomitalia.it, paolo.bartolini@telecomitalia.it, mauro.dinicola@telecomitalia.it, "
								+ "emanuela.chetta@telecomitalia.it, raffaele.guarracino@telecomitalia.it, tommaso.leonetti@telecomitalia.it, giorgio.mecocci@telecomitalia.it, "
								+ "massimo.papini@telecomitalia.it, pasquale.mastrantoni@telecomitalia.it, alessio.vetrano@telecomitalia.it, "
								+ "andrea.turella@telecomitalia.it,  maurizio.cabras@telecomitalia.it,  alessandro.calabretta@telecomitalia.it,  "
								+ "laura.vignozzi@telecomitalia.it, giovannipietro.farris@telecomitalia.it, matteo.bassi@telecomitalia.it, claudia.vaccari@telecomitalia.it, "
								+ "nicola.noferi@telecomitalia.it, sergio.nobili@telecomitalia.it, demetrio.festa@telecomitalia.it, beatrice.pedani@telecomitalia.it, gabriele.piccini@telecomitalia.it, davide.dambrosio@telecomitalia.it");
						// Costruisco il destinatario per conoscenza a seconda dell'AOL
						switch (recordset.getString(AOL)) {
						case "ABM":
							destinatarioCCtxt.setText(destinatarioCCtxt.getText()+ ", "+ ABM);
							break;
						case "LAZ":
							destinatarioCCtxt.setText(destinatarioCCtxt.getText()+ ", "+ LAZ);
							break;
						case "SAR":
							destinatarioCCtxt.setText(destinatarioCCtxt.getText()+ ", " + SAR);
							break;
						case "ROM":
							destinatarioCCtxt.setText(destinatarioCCtxt.getText()+ ", " + ROM);
							break;
						case "TOE":
							destinatarioCCtxt.setText(destinatarioCCtxt.getText()+ ", " + TOE);
							break;
						case "TOO":
							destinatarioCCtxt.setText(destinatarioCCtxt.getText()+ ", " + TOO);
							break;
						case "LIG":
							destinatarioCCtxt.setText(destinatarioCCtxt.getText()+ ", " + LIG);
							break;
						default:
							//Serve per aggiungere eventuali altre AOL. L'evento non si verificherà mai
							JOptionPane.showMessageDialog(null, "AOL non identificata: " + recordset.getString(AOL));
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
	public void SimulaEstraiDati() {
		Statement statement=null;
		ResultSet recordset=null;

		if (getErrore()==0) {
			//inserire codice con sql
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
				//recordset = statement.executeQuery("SELECT * FROM Desaturazioni WHERE [N# LP] = '1693/2018'");
				recordset = statement.executeQuery("SELECT * FROM Desaturazioni_Locale WHERE DataInvioMail is Null AND TD is Not Null AND [N# LP] is Not Null AND WR is Not Null AND [Data programmata] is Not Null");
				// Conteggio record
				while (recordset.next()){
					//System.out.println(recordset.getString(CENTRALE)+ " - "+ recordset.getInt(3)+ " - "+recordset.getString(AOL));
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
						//Costruire la mail
						//-----------------DESTINATARIO FITTIZIO-----------------------
						//destinatariotxt.setText("antonio.argenziano@telecomitalia.it, massimo.dominici@telecomitalia.it, andrea.turella@telecomitalia.it");
						destinatariotxt.setText("matteo.bassi@telecomitalia.it");
						destinatarioCCtxt.setText("fabrizio.adanti@telecomitalia.it, adriano.calvini@telecomitalia.it, paolo.bartolini@telecomitalia.it, roberto.cinerelli@telecomitalia.it, mauro.dinicola@telecomitalia.it, "
								+ "emanuela.chetta@telecomitalia.it, raffaele.guarracino@telecomitalia.it, tommaso.leonetti@telecomitalia.it, giorgio.mecocci@telecomitalia.it, "
								+ "massimo.papini@telecomitalia.it, pasquale.mastrantoni@telecomitalia.it, alessio.vetrano@telecomitalia.it, "
								+ "andrea.turella@telecomitalia.it,  maurizio.cabras@telecomitalia.it,  alessandro.calabretta@telecomitalia.it,  "
								+ "laura.vignozzi@telecomitalia.it, giovannipietro.farris@telecomitalia.it, matteo.bassi@telecomitalia.it, "
								+ "antonio.colavito@telecomitalia.it, sergio.nobili@telecomitalia.it, demetrio.festa@telecomitalia.it, beatrice.pedani@telecomitalia.it, gabriele.piccini@telecomitalia.it, davide.dambrosio@telecomitalia.it");
						// Costruisco il destinatario per conoscenza a seconda dell'AOL
						switch (recordset.getString(AOL)) {
						case "ABM":
							destinatarioCCtxt.setText(destinatarioCCtxt.getText()+ ", "+ ABM);
							break;
						case "LAZ":
							destinatarioCCtxt.setText(destinatarioCCtxt.getText()+ ", "+ LAZ);
							break;
						case "SAR":
							destinatarioCCtxt.setText(destinatarioCCtxt.getText()+ ", " + SAR);
							break;
						case "ROM":
							destinatarioCCtxt.setText(destinatarioCCtxt.getText()+ ", " + ROM);
							break;
						case "TOE":
							destinatarioCCtxt.setText(destinatarioCCtxt.getText()+ ", " + TOE);
							break;
						case "TOO":
							destinatarioCCtxt.setText(destinatarioCCtxt.getText()+ ", " + TOO);
							break;
						case "LIG":
							destinatarioCCtxt.setText(destinatarioCCtxt.getText()+ ", " + LIG);
							break;
						default:
							//Serve per aggiungere eventuali altre AOL. L'evento non si verificherà mai
							JOptionPane.showMessageDialog(null, "AOL non identificata: " + recordset.getString(AOL));
						}
						//------------------ DESTINATARIO FITTIZIO----------------------------
						destinatarioCCtxt.setText("matteo.bassi@telecomitalia.it");
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
	}//Fine metodo Simula
	public void CollegaFileAccess() {
		FileDialogWindows trovafileAccess=new FileDialogWindows("Access File","accdb","mdb");
		if (trovafileAccess.getEsito()==1) {
			btnEstraiDati.setVisible(true);
			btnSimula.setVisible(true);
			String PathDB=trovafileAccess.percorsofile();
			//JOptionPane.showMessageDialog(FinestraComando, "File selezionato: \n" + PathDB);
			setErrore(0);
			//INSERIRE IL CODICE PER COLLEGARE DATABASE ACCESS
			ConnessioneDB connettore=new ConnessioneDB();
			connessioneDB=connettore.connettiDB(PathDB);
			if (connettore.getErrore()!=0) {
				JOptionPane.showMessageDialog(FinestraComando, "Connessione a database stabilita");
				setErrore(0);
			} else {
				JOptionPane.showMessageDialog(FinestraComando, "Connessione a database non riuscita");
				setErrore(2);
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
	public void setErrore(int errore) {
		this.errore = errore;
	}


}
