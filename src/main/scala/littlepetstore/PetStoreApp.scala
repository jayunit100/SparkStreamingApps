package littlepetstore

import scala.Product
import reflect.runtime.universe._
import scala.io.BytePickle.PU
import littlepetstore.PetStoreActors.Animal;
import littlepetstore.PetStoreActors.Customer;
import littlepetstore.PetStoreActors.Employee;
import littlepetstore.PetStoreActors.Human;
import littlepetstore.PetStoreActors.PetStoreActor;

import scala.tools.cmd.ParserUtil

object PetStoreApp{


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

  /**
   * Demonstrate the way that implicits allow us
   * to create an API for Employees that differs from animals.
   */
  def demoImplicits() = {

    //implicit mapper from actor -> actions
    implicit def a2act[T](a: PetStoreActor[T]) = new PetStoreActorActions(a);
    //implicit mapper specifically for humans -> actions.
    //this allows us to complain.
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

    def actors = List(actor1,actor2);
    for(a <- actors){
      println(a)
    }
  }

  /**
   * Now, lets say we want to analyze all existing actors in the petstore
   * simulation.  For example, we want to confirm that the first two actors are
   * employee/animal.
   * Example of how to match cases to inspect the generic types in  a list.
   */
  def demoCases() = {
    def actor1 = new PetStoreActor[Employee] {}
    def actor2 = new PetStoreActor[Employee] {}
    def actor3 = new PetStoreActor[Animal] {}

    var actors = List(actor1, actor2, actor3, actor3);
    println();
    actors match {
      case (_: Employee) :: (_: Employee) :: tail
      => {println("nice 2  employees to start with ")};
      case Nil
      => {println("list is nill.  not way to match.")}
      case _
      => {println("no match")}
    }


  }

  def processActor(a:PetStoreActor[Employee]) = {
    System.out.println("processing "+a);
  }

  /**
   * Example of some Classic functional idioms (fold/destructuring/etc)
   */
  def demoTuples() = {
    implicit def s2act(name: String) = new PetStoreActor[Employee](){
      override def toString() : String = {
        name
      }
    };
    //We can create  a management hierarchy like so,
    def boss : PetStoreActor[Employee] = new String("ASDF");

    System.out.println("org chart : " + boss);
  }

  def demoActors() = {
    implicit def s2act(name: String) = new PetStoreActor[Employee](){
      override def toString() : String = {
        name
      }
    };
    //We can create  a management hierarchy like so,
    def boss : PetStoreActor[Employee] = new String("ASDF");

    System.out.println("org chart : " + boss);
  }


  class PetStore {

    demoTuples();
    demoImplicits();
    demoCases();
    demoActors();
  }

  def main(args: Array[String]) {

    new PetStore;
  }

}