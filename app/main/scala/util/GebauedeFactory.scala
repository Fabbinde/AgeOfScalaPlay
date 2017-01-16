package main.scala.util

import main.scala.model.Gebauede

class GebauedeFactory(private val gebauede: List[Gebauede] = List()) extends Serializable {

  def getAnzahlGebauede(typ: GebauedeEnum.Value) : Int = gebauede.count { g => g.name == typ }
  def getGebauede(typ: GebauedeEnum.Value) : Option[Gebauede] = gebauede.find { g => g.name == typ }
  def getAlle : List[Gebauede] = gebauede.sortBy(_.kategorie).sortBy(_.name)
  def gebauedeHinzufuegen(newGebauede: Gebauede): GebauedeFactory = new GebauedeFactory(List(newGebauede) ::: gebauede)
  def gebauedeEntfernen(newGebauede: Gebauede): GebauedeFactory = new GebauedeFactory(gebauede diff List(newGebauede))
  
}
