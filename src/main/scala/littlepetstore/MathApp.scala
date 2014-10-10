
package littlepetstore

/**
 * Created by jay on 8/6/14.
 */
object MathApp extends App {

  def pow(n: Int, k: Int): Int = {

    k match {
      case 0 => return 1;
      case 1 => return n;
      case 2 => return n * n;
      case kk if kk % 2 == 0 => return pow(pow(n, k / 2), 2);
      case _ => return pow(n, k / 2) * pow(n, (k + 1) / 2)
    }
  }


  def x: Int = 2;
  def y: Int = 4;
  println(pow(x, y))
}


