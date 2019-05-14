package com.uptech.profferz

trait Event
trait Command {
  def wishId: WishId
}
case class Acknowledged(id: String)
case class Error(e: Exception)
