package scaffvis.layout

import scala.annotation.tailrec

/**
  * Implementation of the Squarified Treemap algorithm, described in https://www.win.tue.nl/~vanwijk/stm.pdf
  */
object TreemapLayout {

  case class InputEntry[T](id: T, weight: Double)

  case class OutputEntry[T](id: T, rect: Rect)

  /*
    val targetAspectRatio = 1.0 //target w:h
    criteria
    square: max(h:w, w:h) ~ 1
    rectangle: multiply/divide by target?
   */

  val defaultCanvas = Rect(0, 0, 1, 1)

  def layout[T](entries: Seq[InputEntry[T]]): Seq[OutputEntry[T]] = layout(entries, defaultCanvas)

  def layout[T](entries: Seq[InputEntry[T]], canvas: Rect): Seq[OutputEntry[T]] = layout(normalize(sortByWeight(entries), canvas.area), canvas, Nil)

  /**
    * returns normalized data areas, so that sum of normalized gives target wum
    *
    * @param targetSum target sum
    * @param data      input data
    * @return normalized data
    */
  def normalize[T](data: Seq[InputEntry[T]], targetSum: Double): Seq[InputEntry[T]] = {
    val currentSum = data.map(_.weight).sum
    val scale = targetSum / currentSum
    data.map(x => x.copy[T](weight = x.weight * scale))
  }

  def sortByWeight[T](data: Seq[InputEntry[T]]): Seq[InputEntry[T]] = {
    data.sortWith(_.weight > _.weight)
  }

  /**
    *
    * @param entries normalized/scaled areas
    * @param canvas target - where to paing
    * @param accumulator should be Nil, used in recursion
    * @return
    */
  @tailrec
  private def layout[T](entries: Seq[InputEntry[T]], canvas: Rect, accumulator: Seq[OutputEntry[T]]): Seq[OutputEntry[T]] = {
    if (entries.isEmpty) {
      return accumulator
    }

    //wlog we are placing a row, but actually we'll use the smaller dimension

    val (nextRow, tailAreas) = selectRow(entries, canvas.minSize)

    val (rects, newCanvas) = placeRow(nextRow.map(_.weight), canvas)

    val newEntries = nextRow.zip(rects).map{case (entry, rect) => OutputEntry(id = entry.id, rect = rect)}

    //2) recursion on the rest
    layout(tailAreas, newCanvas, accumulator ++ newEntries)
  }

  /**
    * @param areas
    * @param canvas
    * @return (placed rectangles, in order of input; new canvas
    */
  def placeRow(areas: Seq[Double], canvas: Rect): (Seq[Rect], Rect) = {
    val areaSubSums = areas.scanLeft(0D)(_ + _)
    val rowTotalArea = areaSubSums.last
    val divisionPoints = areaSubSums.map(_ * canvas.minSize / rowTotalArea) //from 0 to width

    val height = rowTotalArea / canvas.minSize

    val rectangles = if (canvas.w <= canvas.h) {
      //horizontal/row
      val xs = divisionPoints.map(_ + canvas.x)
      val y = canvas.y
      val h = height
      xs.sliding(2).map { case Seq(x1, x2) => Rect(x1, y, x2 - x1, h) }
    } else {
      //vertical/column
      val ys = divisionPoints.map(_ + canvas.y)
      val x = canvas.x
      val w = height
      ys.sliding(2).map { case Seq(y1, y2) => Rect(x, y1, w, y2 - y1) }
    }

    val newCanvas = if (canvas.w <= canvas.h) {
      //horizontal/row
      Rect(canvas.x, canvas.y + height, canvas.w, canvas.h - height)
    } else {
      //vertical/column
      Rect(canvas.x + height, canvas.y, canvas.w - height, canvas.h)
    }

    (rectangles.toSeq, newCanvas)
  }


  def worstAspectRatio(minArea: Double, maxArea: Double, totalArea: Double, width: Double): Double = {
    //height = totalArea/width
    //width of one = area*width/totalArea
    //aspect ratio (w/h) = area * width^2 / totalArea^2

    val coef = width * width / totalArea / totalArea
    val maxAspectRatio = maxArea * coef
    val minAspectRatio = minArea * coef

    Math.max(maxAspectRatio, 1 / maxAspectRatio)
  }

  /**
    * select members for the next row
    *
    * @param width width
    * @return (nextRow, theRest)
    */
  def selectRow[T](data: Seq[InputEntry[T]], width: Double): (Seq[InputEntry[T]], Seq[InputEntry[T]]) = {
    val head = data.head
    var tail = data.tail

    var totalArea = head.weight
    var minArea = totalArea
    var maxArea = totalArea

    var worstAR = worstAspectRatio(minArea, maxArea, totalArea, width)
    var row = collection.mutable.Buffer(head)

    while (tail.nonEmpty) {

      //lookahead
      val next = tail.head

      var totalAreaN = totalArea + next.weight
      var minAreaN = Math.min(minArea, next.weight)
      var maxAreaN = Math.max(maxArea, next.weight)
      var worstARN = worstAspectRatio(minAreaN, maxAreaN, totalAreaN, width)

      if (worstARN > worstAR) {
        //we stop when AR starts getting worse
        return (row, tail)
      }

      //update
      minArea = minAreaN
      maxArea = maxAreaN
      totalArea = totalAreaN
      worstAR = worstARN
      row = row += next
      tail = tail.tail
    }

    //we took everything
    (row, tail)
  }
}
