package wui

import main.scala.controller.GameController
import scala.swing.Reactor

class WUI(controller: GameController) extends Reactor  {

  listenTo(controller)
  
}
