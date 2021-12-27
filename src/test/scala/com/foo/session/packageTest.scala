package com.foo
package session

import org.specs2._
import fs2.Stream

import com.foo.hit.Hit

class packageTest extends mutable.Specification {

  "Sessions" should {
    "be empty if there are no hits" in {
      Stream().through(toSessions).toList must equalTo(Nil)
    }

    "be created from a single hit" in {
      val userId = "foo"
      val hit: Msec = 0

      Stream(Hit(userId, hit)).through(toSessions).toList must equalTo(List(userId -> Session(1, hit, hit)))
    }

    "be created from combined hits" in {
      val userId = "foo"
      val hit1: Msec = 0
      val hit2: Msec = hit1 + 100 // within session expiration

      Stream(Hit(userId, hit1), Hit(userId, hit2)).through(toSessions).toList must equalTo(
        List(userId -> Session(2, hit1, hit2)))
    }

    "be created from combined and not combined hits" in {
      val userId = "foo"
      val hit1: Msec = 0
      val hit2: Msec = hit1 + 100 // within session expiration
      val hit3: Msec = hit2 + Session.ExpPeriod + 100500 // outside of session expiration

      Stream(Hit(userId, hit1), Hit(userId, hit2), Hit(userId, hit3)).through(toSessions).toList must containTheSameElementsAs(
        List(userId -> Session(2, hit1, hit2), userId -> Session(1, hit3, hit3)))
    }

    "not interfere for different users" in {
      val userId1 = "foo"
      val userId2 = "moo"
      val hit1: Msec = 0
      val hit2: Msec = hit1 + 100 // within a session
      val hit3: Msec = hit2 + 500 // within a session

      Stream(Hit(userId1, hit1), Hit(userId2, hit2), Hit(userId1, hit3)).through(toSessions).toList must containTheSameElementsAs(
        List(userId1 -> Session(2, hit1, hit3), userId2 -> Session(1, hit2, hit2)))
    }
  }
}
