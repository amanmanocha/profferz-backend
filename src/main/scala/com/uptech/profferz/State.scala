package com.uptech.profferz

trait Event  {
  def wishId: WishId
}
trait Command {
  def wishId: WishId
}
case class Acknowledged(id: String)
case class Error(e: Exception)
