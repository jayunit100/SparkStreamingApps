package littlepetstore

/**
 * Created by jay on 7/29/14.
 */
object PetStoreActors {

  trait PetStoreActor[Type] {

  }

  abstract class Customer extends PetStoreActor[Human]{
    def email = "nobody@hello.com";
  }

  trait Employee extends Human{
    def salary = 100.00f;
  }
  trait Human {
    def fName = {"joe"};
    def lname = {"bloggs"}
  }

  trait Animal {
    def name = "fido"
  }

}
