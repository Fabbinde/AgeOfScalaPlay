package main.scala.dsl

import scala.Tuple5

object RessourceDSL {

  abstract class RessourceClass {}
  object Wood extends RessourceClass {}
  object Stone extends RessourceClass {}
  object Gold extends RessourceClass {}
  object Food extends RessourceClass {}
  object Settler extends RessourceClass {}

  abstract class ContainerAction(name: String) { def stype: String }

  // z.B. 100.of bzw. 100 of  
  class PimpedInt(amount: Int) {
    def of(ressource: RessourceClass) = {
      List((amount, ressource))
    }
  }

  implicit def pimpInt(i: Int) = new PimpedInt(i)

  class Ressources {
    def fill(qi: List[Tuple2[Int, RessourceClass]]) = {
      this
    }
    def remove(qi: Tuple2[Int, RessourceClass]) = {
      this
    }
  }

  def by(param: List[Tuple2[Int, RessourceClass]]) = param

  def main(args: Array[String]): Unit = {
    new Ressources fill by(100 of Wood)
  }
}