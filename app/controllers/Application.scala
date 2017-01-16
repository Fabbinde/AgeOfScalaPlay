package controllers

import main.scala.util.RessourcenContainer
import main.scala.util.RessourcenEnum
import play.api.libs.json.Json
import play.api.libs.json.Writes
import play.api.mvc.Action
import play.api.mvc.Controller
import main.scala.run.Run
import main.scala.model.Spiel
import main.scala.controller.GameController
import main.scala.util.GebauedeEnum
import main.scala.model.Gebauede
import main.scala.util.GebauedeFactory
import main.scala.model.ProduzierendesGebauede
import play.api.libs.json.Json.JsValueWrapper
import main.scala.model.LagerGebauede
import main.scala.model.Wohngebauede
import play.api.libs.json.Json.JsValueWrapper

class Application extends Controller {

  val wuiController: WuiController = new WuiController

  def index = Action {
    // Run TUI, GUI and WUI at same time
    //val run = Run
    //run.run
    Ok(views.html.index(wuiController))
  }

  def newGame = Action {
    wuiController.controller.neuesSpiel
    Ok("Neues Spiel wurde gestartet")
  }

  def saveGame = Action {
    if (wuiController.controller.spielSpeichern) Ok("Spiel gespeichert") else Ok("Spiel konnte nicht gespeichert werden")
  }

  def loadGameFile = Action {
    if (wuiController.controller.spielLaden) Ok("Spiel wurde geladen") else Ok("Spiel konnte nicht geladen werden")
  }

  def loadData = Action {
    implicit val gameWrites = new Writes[GameController] {
      implicit val ressourcesWrites = new Writes[RessourcenContainer] {
        def writes(ressourcen: RessourcenContainer) = {
          Json.obj(
            "wood" -> ressourcen.getRessource(RessourcenEnum.Holz).getAnzahl.toInt,
            "food" -> ressourcen.getRessource(RessourcenEnum.Nahrung).getAnzahl.toInt,
            "stone" -> ressourcen.getRessource(RessourcenEnum.Stein).getAnzahl.toInt,
            "gold" -> ressourcen.getRessource(RessourcenEnum.Gold).getAnzahl.toInt,
            "settler" -> ressourcen.getRessource(RessourcenEnum.Siedler).getAnzahl.toInt)
        }

      }
      implicit val gebauedeWrites = new Writes[Gebauede] {
        def writes(geb: Gebauede) = {
          geb match {
            case l: LagerGebauede => Json.obj("_name" -> l._name,
              "anzahl" -> wuiController.controller.getAlleGebautenGebauede().getAnzahlGebauede   (GebauedeEnum.withName(l.name.toString())),
              "name" -> geb.name,
              "benoetigteBauRessourcen" -> l.benoetigteBauRessourcen,
              "information" -> l.information,
              "kannGebautWerden" -> wuiController.controller.kannGebautWerden(l.name),
              "kapazitaet" -> l.getKapazitaet)
            case w: Wohngebauede => Json.obj("_name" -> w._name,
              "anzahl" -> wuiController.controller.getAlleGebautenGebauede().getAnzahlGebauede(GebauedeEnum.withName(w.name.toString())),
              "name" -> w.name,
              "benoetigteBauRessourcen" -> w.benoetigteBauRessourcen,
              "information" -> w.information,
              "kannGebautWerden" -> wuiController.controller.kannGebautWerden(w.name),
              "platz" -> w.getPlatzFuerSiedler,
              "input" -> w.input)
            case p: ProduzierendesGebauede => Json.obj("_name" -> p._name,
              "anzahl" -> wuiController.controller.getAlleGebautenGebauede().getAnzahlGebauede(GebauedeEnum.withName(p.name.toString())),
              "name" -> p.name,
              "benoetigteBauRessourcen" -> p.benoetigteBauRessourcen,
              "information" -> p.information,
              "kannGebautWerden" -> wuiController.controller.kannGebautWerden(p.name),
              "produziert" -> p.output,
              "input" -> p.input)
          }

        }

      }
      implicit val benoetigtGebauedeWrites = new Writes[Map[main.scala.model.Gebauede, Int]] {
        def writes(map: Map[main.scala.model.Gebauede, Int]) = {

          Json.obj(map.map {
            case (g, i) =>
              val ret: (String, JsValueWrapper) = g._name -> i.toString()
              ret
          }.toSeq: _*)
        }
      }
      implicit val gebauedeFactoryWrites = new Writes[GebauedeFactory] {

        def writes(factory: GebauedeFactory) = {
          Json.obj("factory" -> factory.getAlle)
          /*Json.obj(factory.getAlle.map {
            
            case (g) =>
              //println(g._name)
              val ret: (String, JsValueWrapper) = "name" -> g 
              ret
          }.toSeq: _*)*/
        }

        def writes(prod: ProduzierendesGebauede) = {
          Json.obj(
            "name" -> prod._name,
            "benoetigteBauRessourcen" -> prod.benoetigteBauRessourcen,
            "benoetigtGebauede" -> prod.benoetigtGebauede)
        }

        def writes(lager: LagerGebauede) = {
          Json.obj(
            "name" -> lager._name,
            "benoetigteBauRessourcen" -> lager.benoetigteBauRessourcen,
            "benoetigtGebauede" -> lager.benoetigtGebauede)
        }

        def writes(wohn: Wohngebauede) = {
          Json.obj(
            "name" -> wohn._name,
            "benoetigteBauRessourcen" -> wohn.benoetigteBauRessourcen,
            "benoetigtGebauede" -> wohn.benoetigtGebauede)
        }

      }
      def writes(controller: GameController) = {
        Json.obj(
          "wood" -> controller.getMeineRessourcen.getRessource(RessourcenEnum.Holz).getAnzahl.toInt,
          "food" -> controller.getMeineRessourcen.getRessource(RessourcenEnum.Nahrung).getAnzahl.toInt,
          "stone" -> controller.getMeineRessourcen.getRessource(RessourcenEnum.Stein).getAnzahl.toInt,
          "gold" -> controller.getMeineRessourcen.getRessource(RessourcenEnum.Gold).getAnzahl.toInt,
          "settler" -> controller.getMeineRessourcen.getRessource(RessourcenEnum.Siedler).getAnzahl.toInt,
          "gameName" -> controller.spielName,
          "gameTime" -> controller.aktuelleSpielzeitAlsString,
          "score" -> controller.getGameScore.toString(),
          "production" -> controller.getAktuelleProduktion,
          "runningCosts" -> controller.getAktuelleBetriebskosten,
          "buildableBuildings" -> controller.getAlleVerfuegbarenGebauede)
      }
    }
    Ok(Json.toJson(wuiController.controller))

  }

  def buildBuilding(name: String) = Action {
    GebauedeEnum.withName(name) match {
      case g: GebauedeEnum.Value => Ok(wuiController.controller.gebauedeBauen(g).toString())
      case _                     => BadRequest("Gebäude nicht vorhanden")
    }
    
  }
  
  def getHighscore = Action {
    Ok(Json.toJson(wuiController.controller.getHighscore))
  }

}