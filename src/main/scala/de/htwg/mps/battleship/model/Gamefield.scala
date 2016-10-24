package de.htwg.mps.battleship.model

case class Gamefield(field: Array[Array[Field]], ships: List[Ship]) {
  //  var field = Array.fill[Field](size, size) { Field(false) }
  override def toString: String = "Gamefield: "
  
  def fire(point: Point) = {
    val newF = Field(true)
    val oldF = field(point.x)(point.y)
    val newField = field.map(_.map(compareFields(_, oldF, newF)))
    val newShips = ships.map(ship => ship.copy(ship.pos.map(compareFields(_, oldF, newF))))
    copy(newField, newShips)
  }
  
  def set(fieldList: List[Field]) {
    val newShip = Ship(fieldList)
    copy(field, newShip :: ships)
  }
  
  def compareFields(f1: Field, f2:Field, f3:Field) = if(f1 eq f2) f3 else f1
}