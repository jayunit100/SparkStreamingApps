package littlepetstore

/**
 * Created by jay on 8/14/14.
 */
object PetStoreDataStrucs {

  sealed trait LinkedList[+E]

  case class Node[+E](val head : E , val tail : LinkedList[E]  )
    extends LinkedList[E]{
  }

  case object Empty extends LinkedList[Nothing]

  object LinkedList {

    //Factory method, like a default constructor.
    //Allows you to do "LinkedList(1,2,3)"
    def apply[E]( items : E* ) : LinkedList[E] = {
      if (items.isEmpty) {
        Empty
      } else {
        Node( items.head, apply(items.tail : _*) )
      }
    }
  }
  class RUN{
    System.out.println("ASDF")
    val n = LinkedList(1)
    System.out.println(n)
  }

  def main(args: Array[String]) {

    new RUN;
  }


}
