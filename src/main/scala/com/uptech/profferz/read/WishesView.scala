package com.uptech.profferz.read

import akka.persistence.eventstore.query.scaladsl.EventStoreReadJournal
import akka.persistence.query.PersistenceQuery
import com.uptech.profferz.WishActor.WishAdded
import com.uptech.profferz.{Event, Wish, WishId}


object WishesView {
  import com.uptech.profferz.Profferz._
  var wishes: Map[WishId, Wish] = Map.empty

  def startPopulatingView = {
    val readJournal =
      PersistenceQuery(system).readJournalFor[EventStoreReadJournal](EventStoreReadJournal.Identifier)
    val persistenceIds = readJournal.persistenceIds()
    persistenceIds.runForeach(id => {
      readJournal.eventsByPersistenceId(id, 0, Long.MaxValue).runForeach(e => {
        WishesView.onEvent(WishId(e.persistenceId), e.event.asInstanceOf[Event])
      })
    })
  }

  def onEvent(wishId: WishId, event: Event): Unit = {
    event match {
      case addWish@WishAdded(id, userId, wishDetails) => {
        wishes = wishes + ((id, Wish(id, userId, wishDetails, Map.empty)))
      }
      case e: Event => {
        wishes = wishes + ((e.wishId, wishes(e.wishId).updateState(event)))
      }
    }
  }
}
