package de.htwg.mps.battleship.view.gui

import java.io.File
import java.util.concurrent.TimeUnit

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef}
import de.htwg.mps.battleship.Point
import de.htwg.mps.battleship.controller._
import de.htwg.mps.battleship.controller.command.{Fire, NewGame, QuitGame, SetShip}

import scalafx.Includes._
import scalafx.application.{JFXApp, Platform}
import scalafx.event
import scalafx.event.ActionEvent
import scalafx.geometry.{Insets, Orientation, Pos}
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.{Node, PerspectiveCamera, Scene}
import scalafx.scene.control._
import scalafx.scene.layout.{TilePane, _}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.input.MouseEvent
import scalafx.scene.media.{Media, MediaPlayer, MediaView}
import scalafx.scene.paint.{Color, LinearGradient, Stops}
import scalafx.scene.shape.Rectangle
import scalafx.stage.{Popup, PopupWindow}

class GUIBoarder(val controller: ActorRef, gameSize : Int) extends JFXApp {

  val startWidth = 1695
  val startHeight = 800
  var setShips: List[Point] = List()
  var fireShips: List[Point] = List()
  //var gameInformation : GameInformation
  implicit val timeout = akka.util.Timeout(5, TimeUnit.SECONDS)

  def buttonSetShips(visibility: Boolean) = new Button {
    text = "Set Ships"
    margin = Insets(10)
    minHeight = 40
    minWidth = 240
    visible = visibility
    onAction = (e: ActionEvent) => {
      if (setShips.length > 1) {
        controller ! SetShip(setShips(setShips.length - 2), setShips.last);
        setShips = List()
      }
    }
  }

  def buttonFire(visibility: Boolean) = new Button {
    text = "Fire"
    margin = Insets(10)
    minHeight = 40
    minWidth = 240
    visible = visibility
    onAction = (e: ActionEvent) => {
      if (fireShips.length > 0) {
        controller ! Fire(fireShips.last)
        fireShips = List()
      }
    }
  }

  def popupWindow: List[Node] = List(new ProgressIndicator {
    prefWidth = 50
    prefHeight = 50
  }, new Button {
    minWidth = 240
    minHeight = 30
  }
  )

  val popupButton = new Button {
    minWidth = 240
    minHeight = 30
  }
  val popupSpinner = new ProgressIndicator {
    maxWidth = 50
    maxHeight = 50
  }

  val popupDialog = new BorderPane {
    center = popupSpinner
    bottom = new Button {
      alignmentInParent = Pos.Center
    }
    //buttonTypes.add(ButtonType.Next)
    //content.add(popupButton)


    //show(stage)
  }

  def buttonNext(visibility: Boolean) = new Button {
    text = "Next Player"
    margin = Insets(10)
    minHeight = 40
    minWidth = 240
    visible = visibility
    //onAction = (e: ActionEvent) => {splitPanel.items.clear(); splitPanel.items ++= Seq(reg1,popupDialog)}
  }

  def shipPanel(shipId: Int, toSet: Int) = new BorderPane {
    maxHeight = 80
    maxWidth = 240
    center = shipImage(shipId)
    bottom = new Label(
      text = "Ship for " + shipId + " Fields.  To set: " + toSet
    )
  }

  def shipImage(shipId: Int) = new ImageView {
    image = new Image(this.getClass.getResourceAsStream("/Ship" + shipId + ".gif"))
    fitWidth = 140 + 20 * (shipId - 1)
    fitHeight = 35
  }

  def getMenuBarLeft(visSet: Boolean, visFire: Boolean, visNext: Boolean): List[Node] = {
    List(buttonSetShips(visSet),
      buttonFire(visFire), shipPanel(5, 1), shipPanel(4, 2), shipPanel(3, 2), shipPanel(2, 2), shipPanel(1, 2),
      buttonNext(visNext))
  }

  val reg1 = new TilePane() {
    orientation = Orientation.VERTICAL
    minWidth = 260
    maxWidth = 260
    vgap = 30
    children = getMenuBarLeft(true,false, false)
  }

  def rightSplit(playerBoard: Array[Array[FieldState.Value]], enemeyBoard: Array[Array[FieldState.Value]]) = new SplitPane {

    items ++= Seq(game(Pos.TopLeft, Color.Green, false, playerBoard), game(Pos.TopRight, Color.Red, false, enemeyBoard))
  }

  def reg2(playerBoard: Array[Array[FieldState.Value]], enemeyBoard: Array[Array[FieldState.Value]]) = new VBox {
    children = List(new BorderPane() {
      minHeight = 20
      left = new Label {
        text = "Current Player"
      }
      right = new Label {
        text = "Enemy Player"
      }
    }, rightSplit(playerBoard, enemeyBoard))
  }

  //val playerField = new TilePane{children = List(square,square,square)}
  // val enemeyField = new TilePane{children = List(square,square,square)}

  val backgroundImage = new ImageView {
    image = new Image(this.getClass.getResourceAsStream("/Battleship.jpg"))
    fitWidth = startWidth
    fitHeight = startHeight
    //fitHeight = Double.MaxValue
  }

  val splitPanel = new SplitPane {
    //background = backgroundImage
    orientation = Orientation.HORIZONTAL
    minHeight = Double.MaxValue
    items ++= Seq(reg1, reg2(null, null))
    id = "hiddenSplitter"
    //stylesheets += hiddenSplitPaneCss
  }

  val topBar = new ToolBar() {
    content = List(new Button {
      text = "New Game"
      minWidth = 75
      onAction = (e: ActionEvent) => controller ! NewGame()
    },
      new Button {
        text = "Exit Game"
        minWidth = 75
        onAction = (e: ActionEvent) => sys.exit(0)
      })
  }


  def topPanel = new VBox {
    children = List(
      new ToolBar {
        content = List(new Button {
          text = "New Game"
          minWidth = 75
          onAction = (e: ActionEvent) => children = List(topBar, splitPanel)
        },
          new Button {
            text = "Exit Game"
            minWidth = 75
            onAction = (e: ActionEvent) => sys.exit(0)
          },
          new Label {
            minWidth = Double.MaxValue
            minHeight = 20
          })
      }, backgroundImage)
  }


  stage = new JFXApp.PrimaryStage {
    title = "Battleship"
    minWidth = startWidth
    minHeight = startHeight
    maxHeight = startHeight
    maxWidth = startWidth
    //icons = backgroundImage
    scene = new Scene {
      root = topPanel

    }
  }

  def square(col: Int, row: Int, status: String, color: Color) = new ownRectagle(new Point(row, col)) {
    width = 70
    height = 70
    if (status.equals(FieldState.HIT.toString))
      fill = Color.Red
    else if (status.equals(FieldState.MISS.toString))
      fill = Color.Yellow
    else if (status.equals(FieldState.EMPTY.toString))
      fill = Color.LightBlue
    else if (status.equals(FieldState.SHIP.toString))
      fill = Color.Gray
    else if (status.equals(FieldState.SHOT.toString))
      fill = Color.Red


    //if(fired)fill = Color.Red else fill = Color.Bisque
    //fill = Color.Bisque
    stroke = Color.Burlywood
    //fill <== when(hover) choose Color.Green otherwise Color.Bisque
    onMouseClicked = (e: MouseEvent) => {
      if (status.equals(FieldState.EMPTY.toString)) {
        if (color.equals(Color.Green)) {
          setShips ::= p
          if (setShips.length > 2)
            true
        } else fireShips ::= p

        true
        fill = color
      }
    }
  }

  def game(pos: Pos, player: Color, disabling: Boolean, board: Array[Array[FieldState.Value]]): GridPane = new GridPane {
    alignmentInParent = pos
    disable = disabling

    val rowInfo = new RowConstraints(minHeight = 70, prefHeight = 70, maxHeight = 70)
    val colInfo = new ColumnConstraints(minWidth = 70, prefWidth = 70, maxWidth = 70)
    gridLinesVisible = true
    for (i <- 0 to 9) {
      rowConstraints += rowInfo
      columnConstraints += colInfo
    }
    for (i <- 0 to gameSize - 1)
      for (x <- 0 to gameSize - 1) {
        //GridPane.setConstraints(square(i,x,false, player), i, x)
        val state = if (board != null) board(x)(i).toString else FieldState.EMPTY.toString
        add(square(i, x, state, player), i, x)
      }
  }

  val borderGameField = new BorderPane() {
    minHeight = 20
    left = new Label {
      text = "Current Player"
    }
    right = new Label {
      text = "Enemy Player"
    }
  }

  def reDraw(gameInformation: GameInformation): Unit = {
    if (!gameInformation.boards.isEmpty ){

      splitPanel.items.clear()
      if(gameInformation.player.equals("player0")) splitPanel.items ++= Seq(reg1, reg2(gameInformation.boards(0), gameInformation.boards(1))) else splitPanel.items ++= Seq(reg1, reg2(gameInformation.boards(1), gameInformation.boards(0)))



      if (gameInformation.setableShips.isEmpty)
        reg1.children = getMenuBarLeft(false, true, false)
    }
  }

  def update(infos: UpdateUI): Unit = {
    val gameInformation = infos.gameInformation.filter(info => info.player == infos.currentPlayer).head
    //reg2.children = gamefiel(gameInformation)
    //
    //timeout.duration
      reDraw(gameInformation)
     //reDraw()

  }

  case class ownRectagle(p: Point) extends Rectangle()

}
