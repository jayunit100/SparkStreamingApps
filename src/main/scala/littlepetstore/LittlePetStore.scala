package littlepetstore

import scala.Product
import scala.io.BytePickle.PU
import littlepetstore.PetStoreActors.Animal;
import littlepetstore.PetStoreActors.Customer;
import littlepetstore.PetStoreActors.Employee;
import littlepetstore.PetStoreActors.Human;
import littlepetstore.PetStoreActors.PetStoreActor;

import scala.tools.cmd.ParserUtil

object LittlePetStore {

  class PetStoreHumanActorActions[T](self: PetStoreActor[T]) {
    def complain() = {
      System.out.println("im bored at this stupid petstore.")
    }
  }

  class PetStoreActorActions[T](self: PetStoreActor[T]) {
    def register() = {
      println("REGISTERING ACTOR " + this + " WITH SYSTEM.");
    }

    def die() = {
      System.out.println("Customer disappearing...")
    }

    def start() = {
      System.out.println("Customer appearing...")
    }
  }

  class PetStore {

    /**
     */
    implicit def a2act[T](a: PetStoreActor[T]) = new PetStoreActorActions(a);
    implicit def ha2act[T<:Human](a: PetStoreActor[T]) = new PetStoreHumanActorActions(a);

    def actor1 = new PetStoreActor[Employee] {}

    actor1.start();
    actor1.die();
    actor1.complain();

    /**
     * Notice that this actor cannot "complain", because it
     * does not inherit from Human.
     */
    def actor2 = new PetStoreActor[Animal] {}
    actor2.start();
    actor2.die();
    //actor2.complain();
  }

  def main(args: Array[String]) {
    new PetStore;
  }

}