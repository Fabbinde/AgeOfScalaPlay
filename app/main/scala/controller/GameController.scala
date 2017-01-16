package main.scala.controller

import akka.actor._
import main.scala.util.GebauedeFactory
import main.scala.model.LagerGebauede
import main.scala.model.ProduzierendesGebauede
import main.scala.model.Ressource
import main.scala.model.Spiel
import main.scala.model.Wohngebauede
import main.scala.persist.PersistController
import main.scala.util.ConfigLoader
import main.scala.util.GebauedeEnum
import main.scala.util.RessourcenEnum
import main.scala.util.ResultEnum
import main.scala.persist.HighScorePersistController
import scala.concurrent.duration._
import main.scala.persist.FilePersistController
import main.scala.util.KategorieScoreEnum
import org.joda.time.Period
import main.scala.util.RessourcenContainer
import akka.dispatch.OnComplete
import java.net.InetAddress
import scala.swing.Publisher
import main.scala.model.Gebauede

// Also das Spiel wird immer neu erstellt und dem Controller zugewiesen. Das ist das einzige Attribut das var haben darf 
// (ein Objekt muss man immer aendern, sonst muesste man das Spiel immer wieder neu kompilieren)
class GameController(private var spiel: Spiel, private val alleVerfuegbarenGebauede: GebauedeFactory, private val startRessourcen: RessourcenContainer) extends Publisher {

  private val persistController: FilePersistController = new FilePersistController

  private val highscoreController: HighScorePersistController = new HighScorePersistController

  // Get actor system and scheduler
  private val actorSystem = ActorSystem()
  private val scheduler = actorSystem.scheduler
  // Set refresh calling function
  private val taskAktuallisieren = new Runnable { def run() { aktuallisiereSpielRessourcen } }
  // Get Executer
  private implicit val executor = actorSystem.dispatcher

  // Wait 5 seconds for loading data. Then refresh every 1 second
  private val cancellable = scheduler.schedule(
    initialDelay = 5 seconds,
    interval = 1 seconds,
    runnable = taskAktuallisieren)

  def spielStarten(autoLoad: Boolean) {

    if (autoLoad && !spielLaden) {
      // Spiel soll immutable sein, daher muss das Objekt immer neu gesetzt werden
      spiel = new Spiel("TestSpiel", startRessourcen, new GebauedeFactory)
      spiel = spiel.gebauedeHinzufuegen(ConfigLoader.erstelleDefaultGebauedeMitInfo(GebauedeEnum.KleinesLager).get)
    } else {
      neuesSpiel
    }

  }

  def neuesSpiel = {
    spiel = new Spiel("TestSpiel", startRessourcen, new GebauedeFactory)
    spiel = spiel.gebauedeHinzufuegen(ConfigLoader.erstelleDefaultGebauedeMitInfo(GebauedeEnum.KleinesLager).get)

  }

  def spielBeenden {
    cancellable.cancel()
  }

  def spielSpeichern: Boolean = {
    persistController.save(spiel)
  }

  def spielLaden: Boolean = {
    persistController.load match {
      case Some(i) => {
        spiel = i
      }
      case None => return false
    }
    true
  }

  def spielVorhanden: Boolean = persistController.fileExist

  def gebauedeBauen(gebauede: GebauedeEnum.Value): ResultEnum.Value = {

    kannGebautWerden(gebauede) match {
      case ResultEnum.zuWenigRessourcen => return ResultEnum.zuWenigRessourcen
      case ResultEnum.gebauedeFehlt     => return ResultEnum.gebauedeFehlt
      case ResultEnum.ok => {
        val default = ConfigLoader.erstelleDefaultGebauedeMitInfo(gebauede).get
        spiel = spiel.gebauedeHinzufuegen(default)
        default.benoetigteBauRessourcen.getAlleRessourcen.foreach { r =>
          spiel = spiel.ressourceAbziehen(r._1, r._2.getAnzahl)
        }
        default match {
          case p: ProduzierendesGebauede =>
          case w: Wohngebauede           => spiel = spiel.ressourceHinzufuegen(RessourcenEnum.Siedler, w.getPlatzFuerSiedler)
          case l: LagerGebauede          =>
        }
        return ResultEnum.gebauedeErstellt
      }
      case _ => return ResultEnum.fehler
    }

  }

  def gebauedeAbreissen(gebauede: GebauedeEnum.Value): ResultEnum.Value = {
    val anzahlGebauede = spiel.getAlleErrichteteGebauede.getAlle.size
    spiel = spiel.gebauedeEntfernen(gebauede)
    if (spiel.getAlleErrichteteGebauede.getAlle.size == anzahlGebauede) return ResultEnum.gebauedeNichtVorhanden
    val default = ConfigLoader.erstelleDefaultGebauedeMitInfo(gebauede).get
    default.benoetigteBauRessourcen.getAlleRessourcen.foreach { r =>
      spiel = spiel.ressourceHinzufuegen(r._1, r._2.getAnzahl / 2)
    }
    // Ressourcen hinzufuegen, aber nur 50%
    ResultEnum.gebauedeEntfernt
  }

  def getAlleGebautenGebauede(): GebauedeFactory = spiel.getAlleErrichteteGebauede

  def getAlleVerfuegbarenGebauede: GebauedeFactory = alleVerfuegbarenGebauede

  def kannGebautWerden(gebauede: GebauedeEnum.Value): ResultEnum.Value = {
    val zuBauendesGebauedeOp = alleVerfuegbarenGebauede.getGebauede(gebauede)
    if (zuBauendesGebauedeOp.isDefined) {
      val zuBauendesGebauede = zuBauendesGebauedeOp.get
      zuBauendesGebauede.benoetigteBauRessourcen.getAlleRessourcen.map {
        case (typ: RessourcenEnum.Value, benoetigteRessource: Ressource) =>
          val meineRessource = spiel.ressourcen.getRessource(typ)
          if (meineRessource.getAnzahl < benoetigteRessource.getAnzahl) {
            return ResultEnum.zuWenigRessourcen
          }
      }
    }
    return ResultEnum.ok
  }

  // Aus der Config Datei die Werte fuer das Gebaeude auslesen
  def berechneGebauedeKosten(gebauede: GebauedeEnum.Value): RessourcenContainer = {
    alleVerfuegbarenGebauede.getGebauede(gebauede).get.benoetigteBauRessourcen
  }

  def getGameScore: Integer = {
    berechneAktuellePunktzahl
  }

  def getRessourcenAnzahl(ressource: RessourcenEnum.Value): Double = {
    spiel.getRessource(ressource).getAnzahl
  }

  def getMeineRessourcen: RessourcenContainer = {
    spiel.ressourcen
  }

  def getMeineSiedler: Double = {
    spiel.ressourcen.getRessource(RessourcenEnum.Siedler).getAnzahl
  }

  def getAktuelleProduktion: RessourcenContainer = {
    // TODO Das hier sollte mit val geloest werden irgendwie...
    var prod: RessourcenContainer = new RessourcenContainer
    spiel.getAlleErrichteteGebauede.getAlle.map {
      case p: ProduzierendesGebauede => prod = prod.addRessourcenByContainer(p.output)
      case w: Wohngebauede           =>
      case _                         =>
    }
    prod
  }

  def getAktuelleBetriebskosten: RessourcenContainer = {
    // TODO Das hier sollte mit val geloest werden irgendwie...
    var betriebs: RessourcenContainer = new RessourcenContainer
    spiel.getAlleErrichteteGebauede.getAlle.map {
      case p: ProduzierendesGebauede => betriebs = betriebs.addRessourcenByContainer(p.input)
      case w: Wohngebauede           => betriebs = betriebs.addRessourcenByContainer(w.input)
      case _                         =>
    }
    betriebs
  }

  def getRessourcenKapazitaet: Double = {
    spiel.getLagerKapazitaet
  }

  def aktuallisiereSpielRessourcen {
    spiel.getAlleErrichteteGebauede.getAlle.foreach {
      case p: ProduzierendesGebauede => {
        p.output.getAlleRessourcen.foreach(r => 
          spiel = spiel.ressourceHinzufuegen(r._2.getTyp, r._2.getAnzahl / 60))
        
          p.input.getAlleRessourcen.foreach(r => 
          spiel = spiel.ressourceAbziehen(r._2.getTyp, if ((r._2.getAnzahl / 60) <= 0) 0 else (r._2.getAnzahl / 60)))
      }
      case w: Wohngebauede => w.input.getAlleRessourcen.foreach(r => 
        spiel = spiel.ressourceAbziehen(r._2.getTyp, if ((r._2.getAnzahl / 60) <= 0) 0 else (r._2.getAnzahl / 60)))
      case _               =>
    }
  }

  def stoppeAsyncRessourcenActor() = cancellable.cancel()

  private def berechneAktuellePunktzahl(): Int = {
    spiel.berechneAktuellePunktzahl()
  }

  def aktuelleSpielzeitAlsPeriod: Period = {
    spiel.aktuelleSpielZeit
  }

  def aktuelleSpielzeitAlsString: String = {
    val p: Period = spiel.aktuelleSpielZeit
    "Tage: " + p.getDays + " | Stunden: " + p.getHours + " | Minuten: " + p.getMinutes + " | Sekunden: " + p.getSeconds
  }

  def spielName = spiel.name

  def setSpielName(name: String) = {
    spiel = spiel.setSpielName(name)
  }

  def getHost = InetAddress.getLocalHost.getHostName

  def getHighscore = highscoreController.load

  def saveHighscore = highscoreController.save(spiel)

}
