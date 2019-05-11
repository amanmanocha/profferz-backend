package com.uptech.profferz

trait Event
trait Command

case class Acknowledged(id: String)
case class Error(id: String, message: String)
