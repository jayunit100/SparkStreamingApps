package littlepetstore

import scala.runtime.RichInt

/**
 * Created by jay on 7/29/14.
 */
object IDs{
  var intId : RichInt = 0

  def newId():RichInt= {
    val myNewId = Math.random()*10000
    intId = new RichInt(myNewId.toInt).intValue()
    return intId;
  }
}
object PetStoreActors {

  trait PetStoreActor[Type] {
    //Random integer id.
    def id = IDs.newId();
  }

  abstract class Customer extends PetStoreActor[Human] {
    def email = "nobody@hello.com";
  }

  trait Employee extends Human{
    def salary = 100.00f;
    def hello () : String = {
      "ASDF"
    }
  }
  trait Human {
    def fName = {"joe"};
    def lname = {"bloggs"}
  }

  trait Animal {
    def name = "fido"
  }

}
