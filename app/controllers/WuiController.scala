package controllers

import javax.inject._
import play.api._
import play.api.mvc._

import services.Counter
import main.scala.controller.GameController
import main.scala.util.GebauedeFactory
import main.scala.view.Ui
import main.scala.view.Tui
import main.scala.util.RessourcenContainer
import main.scala.view.GuiFx
import main.scala.util.ConfigLoader
import main.scala.util.RessourcenEnum
import scala.swing.Publisher
import wui.WUI
import wui.WUI

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
class WuiController extends Publisher {

  
  
  val gebauede: GebauedeFactory = ConfigLoader.ladeGebauede

  val cont: RessourcenContainer = new RessourcenContainer
  val res = cont.addRessource(200, RessourcenEnum.Holz).
    addRessource(20, RessourcenEnum.Siedler).
    addRessource(200, RessourcenEnum.Gold).
    addRessource(200, RessourcenEnum.Stein).
    addRessource(200, RessourcenEnum.Nahrung)

  val controller: GameController = new GameController(null, gebauede, res)
  controller.spielStarten(false)
  val tui = new Tui(controller) with Ui

  val wui : WUI = new WUI(controller)
  
  
  def handle(command: String) = {
    //tui.readAndExecute(command)
    //Ok(views.html.index("Ausgeführt"))
  }



}
